package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.avaliacoes.RepositorioAvaliacoes;
import br.edu.scoa.academico.dominio.avaliacoes.*;
import br.edu.scoa.academico.dominio.diarios.EventoAuditoriaDiario;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.*;

public final class RepositorioAvaliacoesMysql implements RepositorioAvaliacoes {
    private final ConfiguracaoBanco banco;

    public RepositorioAvaliacoesMysql(ConfiguracaoBanco banco) { this.banco = Objects.requireNonNull(banco); }

    @Override public AvaliacaoAcademica salvar(AvaliacaoAcademica avaliacao, UUID operacao, long autorId, Instant instante) {
        validarAuditoria(operacao, autorId, instante);
        if (avaliacao == null || avaliacao.id() != 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                var repetida = porOperacaoAvaliacao(conexao, avaliacao, operacao, autorId, "CADASTRO");
                if (repetida.isPresent()) { conexao.commit(); return repetida.get(); }
                bloquearTurmaEExigirPautaAberta(conexao, avaliacao.turmaId());
                long id;
                try (var comando = conexao.prepareStatement(
                        "INSERT INTO avaliacao_academica(turma_id,titulo,descricao,data_aplicacao,tipo,versao_registro,operacao_criacao) VALUES(?,?,?,?,?,1,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    preencher(comando, avaliacao);
                    comando.setString(6, operacao.toString());
                    comando.executeUpdate();
                    try (var chaves = comando.getGeneratedKeys()) {
                        if (!chaves.next()) throw new SQLException("Identificador de avaliação não retornado.");
                        id = chaves.getLong(1);
                    }
                }
                auditarAvaliacao(conexao, id, null, avaliacao, autorId, instante, operacao, "CADASTRO");
                incrementarVersaoDiario(conexao, avaliacao.turmaId());
                conexao.commit();
                return avaliacao.identificada(id, 1);
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAvaliacao falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new FalhaAvaliacao(OPERACAO_REUTILIZADA);
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1452)
                    throw new FalhaAvaliacao(NAO_ENCONTRADA);
                throw new PersistenciaAcademicaException(erro);
            }
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public AvaliacaoAcademica atualizar(AvaliacaoAcademica avaliacao, UUID operacao, long autorId, Instant instante) {
        validarAuditoria(operacao, autorId, instante);
        if (avaliacao == null || avaliacao.id() <= 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                var repetida = porOperacaoAvaliacao(conexao, avaliacao, operacao, autorId, "ALTERACAO");
                if (repetida.isPresent()) { conexao.commit(); return repetida.get(); }
                var anterior = consultar(conexao, avaliacao.id(), true)
                        .orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
                bloquearTurmaEExigirPautaAberta(conexao, anterior.turmaId());
                if (anterior.revisao() != avaliacao.revisao()) throw new FalhaAvaliacao(CONFLITO_EDICAO);
                if (anterior.turmaId() != avaliacao.turmaId()) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
                if (anterior.tipo() != avaliacao.tipo() && possuiNotas(conexao, avaliacao.id()))
                    throw new FalhaAvaliacao(TIPO_AVALIACAO_COM_NOTAS);
                try (var comando = conexao.prepareStatement(
                        "UPDATE avaliacao_academica SET titulo=?,descricao=?,data_aplicacao=?,tipo=?,versao_registro=versao_registro+1 WHERE id=? AND versao_registro=?")) {
                    comando.setString(1, avaliacao.titulo());
                    comando.setString(2, avaliacao.descricao());
                    comando.setObject(3, avaliacao.dataAplicacao());
                    comando.setString(4, avaliacao.tipo().name());
                    comando.setLong(5, avaliacao.id());
                    comando.setLong(6, avaliacao.revisao());
                    if (comando.executeUpdate() != 1) throw new FalhaAvaliacao(CONFLITO_EDICAO);
                }
                auditarAvaliacao(conexao, avaliacao.id(), anterior, avaliacao, autorId, instante, operacao, "ALTERACAO");
                incrementarVersaoDiario(conexao, avaliacao.turmaId());
                conexao.commit();
                return avaliacao.identificada(avaliacao.id(), avaliacao.revisao() + 1);
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAvaliacao falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new FalhaAvaliacao(OPERACAO_REUTILIZADA);
                throw new PersistenciaAcademicaException(erro);
            }
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public Optional<AvaliacaoAcademica> consultar(long avaliacaoId) {
        if (avaliacaoId <= 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir()) { return consultar(conexao, avaliacaoId, false); }
        catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public List<AvaliacaoAcademica> porTurma(long turmaId) {
        if (turmaId <= 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        var itens = new ArrayList<AvaliacaoAcademica>();
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT * FROM avaliacao_academica WHERE turma_id=? ORDER BY data_aplicacao,id")) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) itens.add(lerAvaliacao(resultado));
            }
            return List.copyOf(itens);
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public List<RegistroNota> diarioTurma(long turmaId) {
        return consultarRegistros("WHERE a.turma_id=? AND i.situacao='ATIVA' AND m.situacao='ATIVA' ORDER BY a.data_aplicacao,p.nome,i.id,a.id", turmaId);
    }

    @Override public List<EventoAuditoriaDiario> historicoTurma(long turmaId) {
        if (turmaId <= 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        var eventos = new ArrayList<EventoAuditoriaDiario>();
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT 'AVALIACAO' categoria,a.id entidade_id,NULL inscricao_id,h.evento,"
                        + "CONCAT('Título: ',COALESCE(h.titulo_anterior,'(novo)'),' → ',h.titulo_novo,"
                        + "'; data: ',COALESCE(CAST(h.data_anterior AS CHAR),'(nova)'),' → ',CAST(h.data_nova AS CHAR),"
                        + "'; tipo: ',COALESCE(h.tipo_anterior,'(novo)'),' → ',h.tipo_novo) detalhes,"
                        + "h.autor_id,h.registrado_em,h.origem,h.operacao_id "
                        + "FROM avaliacao_academica_auditoria h JOIN avaliacao_academica a ON a.id=h.avaliacao_id WHERE a.turma_id=? "
                        + "UNION ALL SELECT 'NOTA',a.id,n.inscricao_id,"
                        + "CASE WHEN h.nota_anterior IS NULL THEN 'LANCAMENTO_NOTA' ELSE 'CORRECAO_NOTA' END,"
                        + "CONCAT('Nota: ',COALESCE(CAST(h.nota_anterior AS CHAR),'(inicial)'),' → ',CAST(h.nota_nova AS CHAR)),"
                        + "h.autor_id,h.registrado_em,h.origem,h.operacao_id "
                        + "FROM nota_avaliacao_auditoria h JOIN nota_avaliacao n ON n.id=h.nota_avaliacao_id "
                        + "JOIN avaliacao_academica a ON a.id=n.avaliacao_id WHERE a.turma_id=? ORDER BY registrado_em DESC,operacao_id")) {
            comando.setLong(1, turmaId);
            comando.setLong(2, turmaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) eventos.add(new EventoAuditoriaDiario(
                        resultado.getString("categoria"), resultado.getLong("entidade_id"),
                        (Long) resultado.getObject("inscricao_id"), resultado.getString("evento"),
                        resultado.getString("detalhes"), resultado.getLong("autor_id"),
                        resultado.getTimestamp("registrado_em").toInstant(), resultado.getString("origem"),
                        UUID.fromString(resultado.getString("operacao_id"))));
            }
            return List.copyOf(eventos);
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public long contarInscricoesAtivas(long turmaId) {
        if (turmaId <= 0) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT COUNT(*) FROM inscricao_turma i JOIN matricula_curso m ON m.id=i.matricula_id "
                        + "WHERE i.turma_id=? AND i.situacao='ATIVA' AND m.situacao='ATIVA'")) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) throw new SQLException("Contagem de inscrições não retornada.");
                return resultado.getLong(1);
            }
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    @Override public List<RegistroNota> notasAluno(long alunoId) {
        return consultarRegistros("WHERE m.aluno_id=? AND (i.situacao='ATIVA' OR n.id IS NOT NULL) ORDER BY pl.data_inicio,d.nome,t.codigo,a.data_aplicacao,a.id", alunoId);
    }

    @Override public RegistroNota lancarNota(long avaliacaoId, long inscricaoId, Nota nota, long revisaoEsperada,
                                             UUID operacao, long autorId, Instant instante) {
        validarAuditoria(operacao, autorId, instante);
        if (avaliacaoId <= 0 || inscricaoId <= 0 || nota == null || revisaoEsperada < 0)
            throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                var duplicada = porOperacaoNota(conexao, operacao);
                if (duplicada.isPresent()) {
                    var evento = duplicada.get();
                    if (evento.autorId() != autorId || evento.avaliacaoId() != avaliacaoId
                            || evento.inscricaoId() != inscricaoId || evento.nota().compareTo(nota.valor()) != 0)
                        throw new FalhaAvaliacao(OPERACAO_REUTILIZADA);
                    var atual = buscarRegistro(conexao, avaliacaoId, inscricaoId)
                            .orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
                    conexao.commit();
                    return atual;
                }
                long turmaId;
                TipoAvaliacao tipoAvaliacao;
                try (var comando = conexao.prepareStatement("SELECT turma_id FROM avaliacao_academica WHERE id=? FOR UPDATE")) {
                    comando.setLong(1, avaliacaoId);
                    try (var resultado = comando.executeQuery()) {
                        if (!resultado.next()) throw new FalhaAvaliacao(NAO_ENCONTRADA);
                        turmaId = resultado.getLong(1);
                    }
                }
                try (var comando = conexao.prepareStatement("SELECT tipo FROM avaliacao_academica WHERE id=?")) {
                    comando.setLong(1, avaliacaoId);
                    try (var resultado = comando.executeQuery()) {
                        if (!resultado.next()) throw new FalhaAvaliacao(NAO_ENCONTRADA);
                        tipoAvaliacao = TipoAvaliacao.valueOf(resultado.getString(1));
                    }
                }
                bloquearTurmaEExigirPautaAberta(conexao, turmaId);
                try (var comando = conexao.prepareStatement(
                        "SELECT i.id FROM inscricao_turma i JOIN matricula_curso m ON m.id=i.matricula_id "
                                + "WHERE i.id=? AND i.turma_id=? AND i.situacao='ATIVA' AND m.situacao='ATIVA' FOR UPDATE")) {
                    comando.setLong(1, inscricaoId);
                    comando.setLong(2, turmaId);
                    try (var resultado = comando.executeQuery()) {
                        if (!resultado.next()) throw new FalhaAvaliacao(INSCRICAO_NAO_ATIVA);
                    }
                }
                if (tipoAvaliacao == TipoAvaliacao.EXAME_FINAL) exigirElegibilidadeAoExame(conexao, turmaId, inscricaoId);
                var notaAtual = buscarNota(conexao, avaliacaoId, inscricaoId, true);
                long idNota;
                long revisaoNova;
                Nota anterior = null;
                if (notaAtual.isEmpty()) {
                    if (revisaoEsperada != 0) throw new FalhaAvaliacao(CONFLITO_EDICAO);
                    try (var comando = conexao.prepareStatement(
                            "INSERT INTO nota_avaliacao(avaliacao_id,inscricao_id,nota,versao_registro) VALUES(?,?,?,1)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        comando.setLong(1, avaliacaoId);
                        comando.setLong(2, inscricaoId);
                        comando.setBigDecimal(3, nota.valor());
                        comando.executeUpdate();
                        try (var chaves = comando.getGeneratedKeys()) {
                            if (!chaves.next()) throw new SQLException("Identificador de nota não retornado.");
                            idNota = chaves.getLong(1);
                        }
                    }
                    revisaoNova = 1;
                } else {
                    var atual = notaAtual.get();
                    if (atual.revisao() != revisaoEsperada) throw new FalhaAvaliacao(CONFLITO_EDICAO);
                    anterior = atual.nota();
                    idNota = atual.id();
                    revisaoNova = atual.revisao() + 1;
                    try (var comando = conexao.prepareStatement(
                            "UPDATE nota_avaliacao SET nota=?,versao_registro=? WHERE id=? AND versao_registro=?")) {
                        comando.setBigDecimal(1, nota.valor());
                        comando.setLong(2, revisaoNova);
                        comando.setLong(3, idNota);
                        comando.setLong(4, atual.revisao());
                        if (comando.executeUpdate() != 1) throw new FalhaAvaliacao(CONFLITO_EDICAO);
                    }
                }
                auditarNota(conexao, idNota, anterior, nota, autorId, instante, operacao);
                incrementarVersaoDiario(conexao, turmaId);
                var registro = buscarRegistro(conexao, avaliacaoId, inscricaoId)
                        .orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
                conexao.commit();
                return registro;
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaAvaliacao falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new FalhaAvaliacao(OPERACAO_REUTILIZADA);
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1452)
                    throw new FalhaAvaliacao(INSCRICAO_NAO_ATIVA);
                throw new PersistenciaAcademicaException(erro);
            }
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    private List<RegistroNota> consultarRegistros(String filtro, long id) {
        String sql = "SELECT a.id avaliacao_id,a.turma_id,a.titulo,a.descricao,a.data_aplicacao,a.tipo,a.versao_registro avaliacao_revisao, "
                + "i.id inscricao_id,m.aluno_id,p.nome nome_aluno,n.nota,n.versao_registro nota_revisao, "
                + "d.nome disciplina,t.codigo turma,pl.descricao periodo "
                + "FROM avaliacao_academica a JOIN turma t ON t.id=a.turma_id "
                + "JOIN disciplina d ON d.id=t.disciplina_id JOIN periodo_letivo pl ON pl.id=t.periodo_id "
                + "JOIN inscricao_turma i ON i.turma_id=t.id JOIN matricula_curso m ON m.id=i.matricula_id "
                + "JOIN aluno al ON al.id=m.aluno_id JOIN pessoa p ON p.id=al.pessoa_id "
                + "LEFT JOIN nota_avaliacao n ON n.avaliacao_id=a.id AND n.inscricao_id=i.id "
                + filtro;
        var itens = new ArrayList<RegistroNota>();
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) itens.add(lerRegistro(resultado));
            }
            return List.copyOf(itens);
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    private Optional<RegistroNota> buscarRegistro(Connection conexao, long avaliacaoId, long inscricaoId) throws SQLException {
        String filtro = "WHERE a.id=? AND i.id=?";
        String sql = "SELECT a.id avaliacao_id,a.turma_id,a.titulo,a.descricao,a.data_aplicacao,a.tipo,a.versao_registro avaliacao_revisao, "
                + "i.id inscricao_id,m.aluno_id,p.nome nome_aluno,n.nota,n.versao_registro nota_revisao, "
                + "d.nome disciplina,t.codigo turma,pl.descricao periodo "
                + "FROM avaliacao_academica a JOIN turma t ON t.id=a.turma_id JOIN disciplina d ON d.id=t.disciplina_id "
                + "JOIN periodo_letivo pl ON pl.id=t.periodo_id JOIN inscricao_turma i ON i.turma_id=t.id "
                + "JOIN matricula_curso m ON m.id=i.matricula_id JOIN aluno al ON al.id=m.aluno_id JOIN pessoa p ON p.id=al.pessoa_id "
                + "LEFT JOIN nota_avaliacao n ON n.avaliacao_id=a.id AND n.inscricao_id=i.id " + filtro;
        try (var comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, avaliacaoId);
            comando.setLong(2, inscricaoId);
            try (var resultado = comando.executeQuery()) { return resultado.next() ? Optional.of(lerRegistro(resultado)) : Optional.empty(); }
        }
    }

    private Optional<NotaAtual> buscarNota(Connection conexao, long avaliacaoId, long inscricaoId, boolean bloquear) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT id,nota,versao_registro FROM nota_avaliacao WHERE avaliacao_id=? AND inscricao_id=?" + (bloquear ? " FOR UPDATE" : ""))) {
            comando.setLong(1, avaliacaoId);
            comando.setLong(2, inscricaoId);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(new NotaAtual(resultado.getLong("id"),
                        new Nota(resultado.getBigDecimal("nota")), resultado.getLong("versao_registro"))) : Optional.empty();
            }
        }
    }

    private static boolean possuiNotas(Connection conexao, long avaliacaoId) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT 1 FROM nota_avaliacao WHERE avaliacao_id=? LIMIT 1 FOR UPDATE")) {
            comando.setLong(1, avaliacaoId);
            try (var resultado = comando.executeQuery()) { return resultado.next(); }
        }
    }

    private Optional<RegistroNotaOperacao> porOperacaoNota(Connection conexao, UUID operacao) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT n.avaliacao_id,n.inscricao_id,a.nota_nova,a.autor_id FROM nota_avaliacao_auditoria a "
                + "JOIN nota_avaliacao n ON n.id=a.nota_avaliacao_id WHERE a.operacao_id=?")) {
            comando.setString(1, operacao.toString());
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty();
                return Optional.of(new RegistroNotaOperacao(resultado.getLong("avaliacao_id"),
                        resultado.getLong("inscricao_id"), resultado.getBigDecimal("nota_nova"),
                        resultado.getLong("autor_id")));
            }
        }
    }

    private static void exigirElegibilidadeAoExame(Connection conexao, long turmaId, long inscricaoId)
            throws SQLException {
        var notasDasProvas = new ArrayList<Optional<Nota>>();
        String sql = "SELECT n.nota FROM avaliacao_academica a "
                + "LEFT JOIN nota_avaliacao n ON n.avaliacao_id=a.id AND n.inscricao_id=? "
                + "WHERE a.turma_id=? AND a.tipo='PROVA' ORDER BY a.id FOR UPDATE";
        try (var comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, inscricaoId);
            comando.setLong(2, turmaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    var nota = resultado.getBigDecimal(1);
                    notasDasProvas.add(nota == null ? Optional.empty() : Optional.of(new Nota(nota)));
                }
            }
        }
        var media = notasDasProvas.isEmpty() ? Optional.<java.math.BigDecimal>empty()
                : CalculoNotas.calcularMediaAritmetica(notasDasProvas);
        if (media.isEmpty()) throw new FalhaAvaliacao(NOTAS_PROVAS_PENDENTES);
        if (media.orElseThrow().compareTo(new java.math.BigDecimal("5.0")) < 0)
            throw new FalhaAvaliacao(ALUNO_NAO_ELEGIVEL_EXAME);
    }

    private Optional<AvaliacaoAcademica> porOperacaoAvaliacao(Connection conexao, AvaliacaoAcademica entrada,
            UUID operacao, long autor, String evento) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT avaliacao_id,evento,titulo_novo,descricao_nova,data_nova,tipo_novo,autor_id "
                + "FROM avaliacao_academica_auditoria WHERE operacao_id=?")) {
            comando.setString(1, operacao.toString());
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty();
                boolean igual = resultado.getLong("autor_id") == autor
                        && resultado.getString("evento").equals(evento)
                        && resultado.getString("titulo_novo").equals(entrada.titulo())
                        && resultado.getString("descricao_nova").equals(entrada.descricao())
                        && resultado.getObject("data_nova", LocalDate.class).equals(entrada.dataAplicacao())
                        && resultado.getString("tipo_novo").equals(entrada.tipo().name());
                long id = resultado.getLong("avaliacao_id");
                if (entrada.id() != 0 && entrada.id() != id) igual = false;
                if (!igual) throw new FalhaAvaliacao(OPERACAO_REUTILIZADA);
                return consultar(conexao, id, false);
            }
        }
    }

    private static Optional<AvaliacaoAcademica> consultar(Connection conexao, long id, boolean bloquear) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT * FROM avaliacao_academica WHERE id=?" + (bloquear ? " FOR UPDATE" : ""))) {
            comando.setLong(1, id);
            try (var resultado = comando.executeQuery()) { return resultado.next() ? Optional.of(lerAvaliacao(resultado)) : Optional.empty(); }
        }
    }

    private static AvaliacaoAcademica lerAvaliacao(ResultSet resultado) throws SQLException {
        return new AvaliacaoAcademica(resultado.getLong("id"), resultado.getLong("turma_id"),
                resultado.getString("titulo"), resultado.getString("descricao"),
                resultado.getObject("data_aplicacao", LocalDate.class),
                TipoAvaliacao.valueOf(resultado.getString("tipo")), resultado.getLong("versao_registro"));
    }

    private static RegistroNota lerRegistro(ResultSet resultado) throws SQLException {
        var notaSql = resultado.getBigDecimal("nota");
        Optional<Nota> nota = notaSql == null ? Optional.empty() : Optional.of(new Nota(notaSql));
        long revisao = resultado.getLong("nota_revisao");
        return new RegistroNota(new AvaliacaoAcademica(resultado.getLong("avaliacao_id"),
                resultado.getLong("turma_id"), resultado.getString("titulo"), resultado.getString("descricao"),
                resultado.getObject("data_aplicacao", LocalDate.class),
                TipoAvaliacao.valueOf(resultado.getString("tipo")), resultado.getLong("avaliacao_revisao")),
                resultado.getLong("inscricao_id"), resultado.getLong("aluno_id"), resultado.getString("nome_aluno"),
                nota, revisao, resultado.getString("disciplina"), resultado.getString("turma"), resultado.getString("periodo"));
    }

    private static void preencher(PreparedStatement comando, AvaliacaoAcademica avaliacao) throws SQLException {
        comando.setLong(1, avaliacao.turmaId());
        comando.setString(2, avaliacao.titulo());
        comando.setString(3, avaliacao.descricao());
        comando.setObject(4, avaliacao.dataAplicacao());
        comando.setString(5, avaliacao.tipo().name());
    }

    private static void auditarAvaliacao(Connection conexao, long id, AvaliacaoAcademica anterior,
                                         AvaliacaoAcademica nova, long autor, Instant instante,
                                         UUID operacao, String evento) throws SQLException {
        try (var comando = conexao.prepareStatement("INSERT INTO avaliacao_academica_auditoria "
                + "(avaliacao_id,evento,titulo_anterior,titulo_novo,data_anterior,data_nova,tipo_anterior,tipo_novo,descricao_anterior,descricao_nova,autor_id,registrado_em,origem,operacao_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            comando.setLong(1, id);
            comando.setString(2, evento);
            comando.setString(3, anterior == null ? null : anterior.titulo());
            comando.setString(4, nova.titulo());
            if (anterior == null) comando.setNull(5, Types.DATE); else comando.setObject(5, anterior.dataAplicacao());
            comando.setObject(6, nova.dataAplicacao());
            comando.setString(7, anterior == null ? null : anterior.tipo().name());
            comando.setString(8, nova.tipo().name());
            comando.setString(9, anterior == null ? null : anterior.descricao());
            comando.setString(10, nova.descricao());
            comando.setLong(11, autor);
            comando.setTimestamp(12, Timestamp.from(instante));
            comando.setString(13, "ACA07_APLICACAO");
            comando.setString(14, operacao.toString());
            comando.executeUpdate();
        }
    }

    private static void auditarNota(Connection conexao, long id, Nota anterior, Nota nova,
                                    long autor, Instant instante, UUID operacao) throws SQLException {
        try (var comando = conexao.prepareStatement("INSERT INTO nota_avaliacao_auditoria(nota_avaliacao_id,nota_anterior,nota_nova,autor_id,registrado_em,origem,operacao_id) VALUES(?,?,?,?,?,?,?)")) {
            comando.setLong(1, id);
            if (anterior == null) comando.setNull(2, Types.DECIMAL); else comando.setBigDecimal(2, anterior.valor());
            comando.setBigDecimal(3, nova.valor());
            comando.setLong(4, autor);
            comando.setTimestamp(5, Timestamp.from(instante));
            comando.setString(6, "ACA07_APLICACAO");
            comando.setString(7, operacao.toString());
            comando.executeUpdate();
        }
    }

    private static void validarAuditoria(UUID operacao, long autor, Instant instante) {
        if (operacao == null || autor <= 0 || instante == null) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
    }

    private static void bloquearTurmaEExigirPautaAberta(Connection conexao, long turmaId) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT id FROM turma WHERE id=? FOR UPDATE")) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) throw new FalhaAvaliacao(NAO_ENCONTRADA);
            }
        }
        try (var comando = conexao.prepareStatement("SELECT estado FROM pauta_final WHERE turma_id=? FOR UPDATE")) {
            comando.setLong(1, turmaId);
            try (var resultado = comando.executeQuery()) {
                if (resultado.next() && "HOMOLOGADA".equals(resultado.getString(1))) {
                    throw new FalhaAvaliacao(PAUTA_HOMOLOGADA);
                }
            }
        }
    }

    private static void incrementarVersaoDiario(Connection conexao, long turmaId) throws SQLException {
        try (var comando = conexao.prepareStatement(
                "UPDATE turma SET versao_diario=versao_diario+1 WHERE id=?")) {
            comando.setLong(1, turmaId);
            if (comando.executeUpdate() != 1) throw new FalhaAvaliacao(NAO_ENCONTRADA);
        }
    }

    private static void reverter(Connection conexao, Throwable erro) {
        try { conexao.rollback(); } catch (SQLException falha) { erro.addSuppressed(falha); }
    }

    private record NotaAtual(long id, Nota nota, long revisao) { }
    private record RegistroNotaOperacao(long avaliacaoId, long inscricaoId, java.math.BigDecimal nota, long autorId) { }
}
