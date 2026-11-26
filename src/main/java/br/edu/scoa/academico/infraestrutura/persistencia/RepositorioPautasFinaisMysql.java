package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.diarios.RepositorioPautasFinais;
import br.edu.scoa.academico.dominio.diarios.EstadoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.EventoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;
import br.edu.scoa.academico.dominio.diarios.PautaFinal;
import br.edu.scoa.academico.dominio.diarios.TipoEventoPauta;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario.Codigo.*;

public final class RepositorioPautasFinaisMysql implements RepositorioPautasFinais {
    private final ConfiguracaoBanco banco;

    public RepositorioPautasFinaisMysql(ConfiguracaoBanco banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override public PautaFinal consultar(long turmaId) {
        validarTurma(turmaId);
        try (var conexao = banco.abrir()) {
            long versaoAtual = versaoDiario(conexao, turmaId, false);
            return lerPauta(conexao, turmaId, false).orElseGet(() -> PautaFinal.aberta(turmaId, versaoAtual));
        } catch (SQLException erro) { throw indisponivel(erro); }
    }

    @Override public List<EventoPautaFinal> historico(long turmaId) {
        validarTurma(turmaId);
        var eventos = new ArrayList<EventoPautaFinal>();
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT * FROM pauta_final_auditoria WHERE turma_id=? ORDER BY registrado_em,id")) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) eventos.add(new EventoPautaFinal(resultado.getLong("id"), turmaId,
                        TipoEventoPauta.valueOf(resultado.getString("evento")),
                        estado(resultado.getString("estado_anterior")), EstadoPautaFinal.valueOf(resultado.getString("estado_novo")),
                        resultado.getString("observacao"), resultado.getLong("autor_id"),
                        resultado.getTimestamp("registrado_em").toInstant(), UUID.fromString(resultado.getString("operacao_id"))));
            }
            return List.copyOf(eventos);
        } catch (SQLException erro) { throw indisponivel(erro); }
    }

    @Override public long versaoDiario(long turmaId) {
        validarTurma(turmaId);
        try (var conexao = banco.abrir()) { return versaoDiario(conexao, turmaId, false); }
        catch (SQLException erro) { throw indisponivel(erro); }
    }

    @Override public PautaFinal registrarAnalise(long turmaId, long revisaoEsperada, String observacao,
                                                  long autorId, java.time.Instant instante, UUID operacaoId) {
        validarOperacao(turmaId, revisaoEsperada, observacao, autorId, instante, operacaoId);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                long versaoAtual = bloquearTurma(conexao, turmaId);
                var repetida = eventoPorOperacao(conexao, operacaoId);
                if (repetida.isPresent()) {
                    validarRepeticao(repetida.get(), turmaId, TipoEventoPauta.ANALISE_REGISTRADA, observacao, autorId);
                    var atual = lerPauta(conexao, turmaId, true).orElse(PautaFinal.aberta(turmaId, versaoAtual));
                    conexao.commit();
                    return atual;
                }
                var atual = lerPauta(conexao, turmaId, true).orElse(PautaFinal.aberta(turmaId, versaoAtual));
                validarRevisao(atual, revisaoEsperada);
                if (atual.estado() == EstadoPautaFinal.HOMOLOGADA) throw new FalhaAuditoriaDiario(PAUTA_HOMOLOGADA);
                long revisaoNova = atual.revisao() + 1;
                if (atual.revisao() == 0) {
                    try (var comando = conexao.prepareStatement(
                            "INSERT INTO pauta_final(turma_id,estado,revisao,versao_diario,homologada_por,homologada_em) VALUES(?,'ABERTA',?,?,NULL,NULL)")) {
                        comando.setLong(1, turmaId); comando.setLong(2, revisaoNova); comando.setLong(3, versaoAtual);
                        comando.executeUpdate();
                    }
                } else {
                    try (var comando = conexao.prepareStatement(
                            "UPDATE pauta_final SET revisao=?,versao_diario=? WHERE turma_id=? AND revisao=? AND estado='ABERTA'")) {
                        comando.setLong(1, revisaoNova); comando.setLong(2, versaoAtual);
                        comando.setLong(3, turmaId); comando.setLong(4, revisaoEsperada);
                        if (comando.executeUpdate() != 1) throw new FalhaAuditoriaDiario(CONFLITO_REVISAO);
                    }
                }
                inserirEvento(conexao, turmaId, TipoEventoPauta.ANALISE_REGISTRADA,
                        atual.revisao() == 0 ? null : atual.estado(), EstadoPautaFinal.ABERTA,
                        revisaoNova, observacao, autorId, instante, operacaoId);
                conexao.commit();
                return lerPauta(conexao, turmaId, false).orElseThrow(() -> new SQLException("Pauta não retornada."));
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAuditoriaDiario falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062) throw new FalhaAuditoriaDiario(OPERACAO_REUTILIZADA);
                throw indisponivel(erro);
            }
        } catch (SQLException erro) { throw indisponivel(erro); }
    }

    @Override public PautaFinal homologar(long turmaId, long revisaoEsperada, long versaoDiarioEsperada,
                                           long autorId, java.time.Instant instante, UUID operacaoId) {
        validarOperacao(turmaId, revisaoEsperada, "", autorId, instante, operacaoId);
        if (versaoDiarioEsperada < 0) throw new FalhaAuditoriaDiario(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                long versaoAtual = bloquearTurma(conexao, turmaId);
                var repetida = eventoPorOperacao(conexao, operacaoId);
                if (repetida.isPresent()) {
                    validarRepeticao(repetida.get(), turmaId, TipoEventoPauta.HOMOLOGADA, "", autorId);
                    var atual = lerPauta(conexao, turmaId, true).orElseThrow(() -> new FalhaAuditoriaDiario(PAUTA_NAO_ANALISADA));
                    conexao.commit();
                    return atual;
                }
                var atual = lerPauta(conexao, turmaId, true).orElseThrow(() -> new FalhaAuditoriaDiario(PAUTA_NAO_ANALISADA));
                validarRevisao(atual, revisaoEsperada);
                if (atual.estado() == EstadoPautaFinal.HOMOLOGADA) throw new FalhaAuditoriaDiario(PAUTA_HOMOLOGADA);
                if (versaoAtual != versaoDiarioEsperada) throw new FalhaAuditoriaDiario(CONFLITO_DIARIO);
                long revisaoNova = atual.revisao() + 1;
                try (var comando = conexao.prepareStatement(
                        "UPDATE pauta_final SET estado='HOMOLOGADA',revisao=?,versao_diario=?,homologada_por=?,homologada_em=? WHERE turma_id=? AND revisao=? AND estado='ABERTA'")) {
                    comando.setLong(1, revisaoNova); comando.setLong(2, versaoAtual); comando.setLong(3, autorId);
                    comando.setTimestamp(4, Timestamp.from(instante)); comando.setLong(5, turmaId); comando.setLong(6, revisaoEsperada);
                    if (comando.executeUpdate() != 1) throw new FalhaAuditoriaDiario(CONFLITO_REVISAO);
                }
                inserirEvento(conexao, turmaId, TipoEventoPauta.HOMOLOGADA, atual.estado(),
                        EstadoPautaFinal.HOMOLOGADA, revisaoNova, "", autorId, instante, operacaoId);
                conexao.commit();
                return lerPauta(conexao, turmaId, false).orElseThrow(() -> new SQLException("Pauta não retornada."));
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAuditoriaDiario falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062) throw new FalhaAuditoriaDiario(OPERACAO_REUTILIZADA);
                throw indisponivel(erro);
            }
        } catch (SQLException erro) { throw indisponivel(erro); }
    }

    @Override public PautaFinal reabrir(long turmaId, long revisaoEsperada, String motivo,
                                         long autorId, java.time.Instant instante, UUID operacaoId) {
        validarOperacao(turmaId, revisaoEsperada, motivo, autorId, instante, operacaoId);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                bloquearTurma(conexao, turmaId);
                var repetida = eventoPorOperacao(conexao, operacaoId);
                if (repetida.isPresent()) {
                    validarRepeticao(repetida.get(), turmaId, TipoEventoPauta.REABERTA, motivo, autorId);
                    var atual = lerPauta(conexao, turmaId, true).orElseThrow(() -> new FalhaAuditoriaDiario(PAUTA_NAO_ANALISADA));
                    conexao.commit();
                    return atual;
                }
                var atual = lerPauta(conexao, turmaId, true).orElseThrow(() -> new FalhaAuditoriaDiario(PAUTA_NAO_ANALISADA));
                validarRevisao(atual, revisaoEsperada);
                if (atual.estado() != EstadoPautaFinal.HOMOLOGADA) throw new FalhaAuditoriaDiario(PAUTA_NAO_ANALISADA);
                long revisaoNova = atual.revisao() + 1;
                try (var comando = conexao.prepareStatement(
                        "UPDATE pauta_final SET estado='ABERTA',revisao=?,homologada_por=NULL,homologada_em=NULL WHERE turma_id=? AND revisao=? AND estado='HOMOLOGADA'")) {
                    comando.setLong(1, revisaoNova); comando.setLong(2, turmaId); comando.setLong(3, revisaoEsperada);
                    if (comando.executeUpdate() != 1) throw new FalhaAuditoriaDiario(CONFLITO_REVISAO);
                }
                inserirEvento(conexao, turmaId, TipoEventoPauta.REABERTA, atual.estado(),
                        EstadoPautaFinal.ABERTA, revisaoNova, motivo, autorId, instante, operacaoId);
                conexao.commit();
                return lerPauta(conexao, turmaId, false).orElseThrow(() -> new SQLException("Pauta não retornada."));
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAuditoriaDiario falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062) throw new FalhaAuditoriaDiario(OPERACAO_REUTILIZADA);
                throw indisponivel(erro);
            }
        } catch (SQLException erro) { throw indisponivel(erro); }
    }

    private static long bloquearTurma(Connection conexao, long turmaId) throws SQLException {
        return versaoDiario(conexao, turmaId, true);
    }

    private static long versaoDiario(Connection conexao, long turmaId, boolean bloquear) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT versao_diario FROM turma WHERE id=?" + (bloquear ? " FOR UPDATE" : ""))) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) throw new FalhaAuditoriaDiario(TURMA_NAO_ENCONTRADA);
                return resultado.getLong(1);
            }
        }
    }

    private static Optional<PautaFinal> lerPauta(Connection conexao, long turmaId, boolean bloquear) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT * FROM pauta_final WHERE turma_id=?" + (bloquear ? " FOR UPDATE" : ""))) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty();
                var homologadaEm = resultado.getTimestamp("homologada_em");
                long homologadaPor = resultado.getLong("homologada_por");
                Long autor = resultado.wasNull() ? null : homologadaPor;
                return Optional.of(new PautaFinal(turmaId, EstadoPautaFinal.valueOf(resultado.getString("estado")),
                        resultado.getLong("revisao"), resultado.getLong("versao_diario"), autor,
                        homologadaEm == null ? null : homologadaEm.toInstant()));
            }
        }
    }

    private static Optional<EventoPautaFinal> eventoPorOperacao(Connection conexao, UUID operacaoId) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT * FROM pauta_final_auditoria WHERE operacao_id=?")) {
            comando.setString(1, operacaoId.toString());
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty();
                return Optional.of(new EventoPautaFinal(resultado.getLong("id"), resultado.getLong("turma_id"),
                        TipoEventoPauta.valueOf(resultado.getString("evento")), estado(resultado.getString("estado_anterior")),
                        EstadoPautaFinal.valueOf(resultado.getString("estado_novo")), resultado.getString("observacao"),
                        resultado.getLong("autor_id"), resultado.getTimestamp("registrado_em").toInstant(),
                        UUID.fromString(resultado.getString("operacao_id"))));
            }
        }
    }

    private static void inserirEvento(Connection conexao, long turmaId, TipoEventoPauta evento,
                                      EstadoPautaFinal anterior, EstadoPautaFinal novo, long revisao,
                                      String observacao, long autorId, java.time.Instant instante,
                                      UUID operacaoId) throws SQLException {
        try (var comando = conexao.prepareStatement(
                "INSERT INTO pauta_final_auditoria(turma_id,evento,estado_anterior,estado_novo,revisao_pauta,observacao,autor_id,registrado_em,origem,operacao_id) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            comando.setLong(1, turmaId); comando.setString(2, evento.name());
            comando.setString(3, anterior == null ? null : anterior.name()); comando.setString(4, novo.name());
            comando.setLong(5, revisao); comando.setString(6, observacao); comando.setLong(7, autorId);
            comando.setTimestamp(8, Timestamp.from(instante)); comando.setString(9, "ACA08_APLICACAO");
            comando.setString(10, operacaoId.toString()); comando.executeUpdate();
        }
    }

    private static void validarRepeticao(EventoPautaFinal evento, long turmaId, TipoEventoPauta tipo,
                                          String observacao, long autorId) {
        if (evento.turmaId() != turmaId || evento.evento() != tipo || evento.autorId() != autorId
                || !evento.observacao().equals(observacao)) throw new FalhaAuditoriaDiario(OPERACAO_REUTILIZADA);
    }

    private static void validarRevisao(PautaFinal atual, long esperada) {
        if (atual.revisao() != esperada) throw new FalhaAuditoriaDiario(CONFLITO_REVISAO);
    }

    private static void validarOperacao(long turmaId, long revisao, String observacao, long autorId,
                                        java.time.Instant instante, UUID operacaoId) {
        validarTurma(turmaId);
        if (revisao < 0 || observacao == null || observacao.length() > 1000 || autorId <= 0
                || instante == null || operacaoId == null) throw new FalhaAuditoriaDiario(ENTRADA_INVALIDA);
    }

    private static void validarTurma(long turmaId) {
        if (turmaId <= 0) throw new FalhaAuditoriaDiario(ENTRADA_INVALIDA);
    }

    private static EstadoPautaFinal estado(String valor) {
        return valor == null ? null : EstadoPautaFinal.valueOf(valor);
    }

    private static FalhaAuditoriaDiario indisponivel(Throwable erro) {
        return new FalhaAuditoriaDiario(PERSISTENCIA_INDISPONIVEL);
    }

    private static void reverter(Connection conexao, Throwable erro) {
        try { conexao.rollback(); } catch (SQLException falha) { erro.addSuppressed(falha); }
    }
}
