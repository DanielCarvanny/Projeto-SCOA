package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.matriculas.RepositorioInscricoes;
import br.edu.scoa.academico.dominio.matriculas.FalhaMatricula;
import br.edu.scoa.academico.dominio.matriculas.InscricaoTurma;
import br.edu.scoa.academico.dominio.matriculas.SituacaoInscricao;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class RepositorioInscricoesMysql implements RepositorioInscricoes {
    private final ConfiguracaoBanco banco;

    public RepositorioInscricoesMysql(ConfiguracaoBanco banco) { this.banco = banco; }

    @Override public InscricaoTurma inscrever(InscricaoTurma inscricao, UUID operacao, long autor, Instant instante) {
        if (operacao == null || autor <= 0 || instante == null) throw new FalhaMatricula(FalhaMatricula.Codigo.INSCRICAO_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                long capacidade;
                try (var consulta = conexao.prepareStatement("SELECT capacidade FROM turma WHERE id=? AND situacao='ABERTA' FOR UPDATE")) {
                    consulta.setLong(1, inscricao.turmaId());
                    try (var resultado = consulta.executeQuery()) {
                        if (!resultado.next()) throw new FalhaMatricula(FalhaMatricula.Codigo.TURMA_NAO_ABERTA);
                        capacidade = resultado.getLong(1);
                    }
                }
                try (var consulta = conexao.prepareStatement("SELECT COUNT(*) FROM inscricao_turma WHERE turma_id=? AND situacao='ATIVA'")) {
                    consulta.setLong(1, inscricao.turmaId());
                    try (var resultado = consulta.executeQuery()) {
                        resultado.next();
                        if (resultado.getLong(1) >= capacidade) throw new FalhaMatricula(FalhaMatricula.Codigo.VAGA_INDISPONIVEL);
                    }
                }
                try (var consulta = conexao.prepareStatement("SELECT id FROM inscricao_turma WHERE matricula_id=? AND turma_id=? AND situacao='ATIVA' FOR UPDATE")) {
                    consulta.setLong(1, inscricao.matriculaId());
                    consulta.setLong(2, inscricao.turmaId());
                    try (var resultado = consulta.executeQuery()) {
                        if (resultado.next()) throw new FalhaMatricula(FalhaMatricula.Codigo.DUPLICIDADE);
                    }
                }
                long id;
                try (var comando = conexao.prepareStatement("INSERT INTO inscricao_turma(matricula_id,turma_id,situacao,inscrita_em,versao_registro,operacao_criacao) VALUES(?,?,'ATIVA',?,1,?)", Statement.RETURN_GENERATED_KEYS)) {
                    comando.setLong(1, inscricao.matriculaId());
                    comando.setLong(2, inscricao.turmaId());
                    comando.setTimestamp(3, Timestamp.from(instante));
                    comando.setString(4, operacao.toString());
                    comando.executeUpdate();
                    try (var chaves = comando.getGeneratedKeys()) {
                        if (!chaves.next()) throw new SQLException("Identificador de inscrição não retornado.");
                        id = chaves.getLong(1);
                    }
                }
                auditar(conexao, id, "INSCRICAO", autor, instante, operacao);
                conexao.commit();
                return new InscricaoTurma(id, inscricao.matriculaId(), inscricao.turmaId(),
                        SituacaoInscricao.ATIVA, instante, 1);
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaMatricula falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new FalhaMatricula(FalhaMatricula.Codigo.OPERACAO_REUTILIZADA);
                throw new PersistenciaAcademicaException((SQLException) erro);
            }
        } catch (SQLException erro) {
            throw new PersistenciaAcademicaException(erro);
        }
    }

    @Override public InscricaoTurma cancelar(InscricaoTurma esperada, UUID operacao, long autor, Instant instante) {
        if (esperada == null || operacao == null || autor <= 0 || instante == null)
            throw new FalhaMatricula(FalhaMatricula.Codigo.INSCRICAO_INVALIDA);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                try (var comando = conexao.prepareStatement("UPDATE inscricao_turma SET situacao='CANCELADA',versao_registro=versao_registro+1 WHERE id=? AND situacao='ATIVA' AND versao_registro=?")) {
                    comando.setLong(1, esperada.id());
                    comando.setLong(2, esperada.revisao());
                    if (comando.executeUpdate() != 1) throw new FalhaMatricula(FalhaMatricula.Codigo.CONFLITO_EDICAO);
                }
                auditar(conexao, esperada.id(), "CANCELAMENTO", autor, instante, operacao);
                conexao.commit();
                return new InscricaoTurma(esperada.id(), esperada.matriculaId(), esperada.turmaId(),
                        SituacaoInscricao.CANCELADA, esperada.inscritaEm(), esperada.revisao() + 1);
            } catch (SQLException | RuntimeException erro) {
                reverter(conexao, erro);
                if (erro instanceof FalhaMatricula falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new FalhaMatricula(FalhaMatricula.Codigo.OPERACAO_REUTILIZADA);
                throw new PersistenciaAcademicaException((SQLException) erro);
            }
        } catch (SQLException erro) {
            throw new PersistenciaAcademicaException(erro);
        }
    }

    @Override public List<InscricaoTurma> ativasPorMatricula(long matriculaId) {
        return buscarAtivas("matricula_id", matriculaId);
    }

    @Override public List<InscricaoTurma> ativasPorTurma(long turmaId) {
        return buscarAtivas("turma_id", turmaId);
    }

    @Override public List<InscricaoTurma> porMatricula(long matriculaId) {
        if (matriculaId <= 0) throw new FalhaMatricula(FalhaMatricula.Codigo.INSCRICAO_INVALIDA);
        var itens = new ArrayList<InscricaoTurma>();
        try (var conexao = banco.abrir();
             var comando = conexao.prepareStatement("SELECT * FROM inscricao_turma WHERE matricula_id=? ORDER BY inscrita_em,id")) {
            comando.setLong(1, matriculaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) itens.add(ler(resultado));
            }
            return List.copyOf(itens);
        } catch (SQLException erro) {
            throw new PersistenciaAcademicaException(erro);
        }
    }

    private List<InscricaoTurma> buscarAtivas(String coluna, long id) {
        if (id <= 0) throw new FalhaMatricula(FalhaMatricula.Codigo.INSCRICAO_INVALIDA);
        var itens = new ArrayList<InscricaoTurma>();
        try (var conexao = banco.abrir();
             var comando = conexao.prepareStatement("SELECT * FROM inscricao_turma WHERE " + coluna + "=? AND situacao='ATIVA' ORDER BY id")) {
            comando.setLong(1, id);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) itens.add(ler(resultado));
            }
            return List.copyOf(itens);
        } catch (SQLException erro) {
            throw new PersistenciaAcademicaException(erro);
        }
    }

    private static InscricaoTurma ler(ResultSet resultado) throws SQLException {
        return new InscricaoTurma(resultado.getLong("id"), resultado.getLong("matricula_id"),
                resultado.getLong("turma_id"), SituacaoInscricao.valueOf(resultado.getString("situacao")),
                resultado.getTimestamp("inscrita_em").toInstant(), resultado.getLong("versao_registro"));
    }

    private static void auditar(Connection conexao, long inscricaoId, String evento, long autor,
                                Instant instante, UUID operacao) throws SQLException {
        try (var comando = conexao.prepareStatement("INSERT INTO inscricao_auditoria(inscricao_id,evento,autor_id,instante,operacao_id) VALUES(?,?,?,?,?)")) {
            comando.setLong(1, inscricaoId);
            comando.setString(2, evento);
            comando.setLong(3, autor);
            comando.setTimestamp(4, Timestamp.from(instante));
            comando.setString(5, operacao.toString());
            comando.executeUpdate();
        }
    }

    private static void reverter(Connection conexao, Throwable erro) {
        try { conexao.rollback(); } catch (SQLException falha) { erro.addSuppressed(falha); }
    }
}
