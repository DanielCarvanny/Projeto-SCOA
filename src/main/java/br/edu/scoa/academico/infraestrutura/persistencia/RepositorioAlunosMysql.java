package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.alunos.RepositorioGestaoAlunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import java.util.Optional;
import java.sql.*;
import br.edu.scoa.academico.aplicacao.alunos.CadastroAlunoException;
import br.edu.scoa.academico.aplicacao.alunos.ErroCadastroAluno;

public final class RepositorioAlunosMysql implements RepositorioGestaoAlunos {
    private final ConfiguracaoBanco banco;
    public RepositorioAlunosMysql(ConfiguracaoBanco banco) { this.banco = banco; }
    public Aluno cadastrar(long pessoaId, SituacaoCadastral situacao, long usuarioId) {
        validar(pessoaId, situacao, usuarioId);
        try (var c = banco.abrir()) {
            c.setAutoCommit(false);
            try {
                long id;
                try (var s = c.prepareStatement("INSERT INTO aluno (pessoa_id, situacao_cadastral) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setLong(1, pessoaId);
                    s.setString(2, situacao.name());
                    s.executeUpdate();
                    try (var chaves = s.getGeneratedKeys()) {
                        if (!chaves.next()) throw new SQLException("Identificador não retornado.");
                        id = chaves.getLong(1);
                    }
                }
                auditar(c, id, usuarioId, "CADASTRO", null, situacao);
                c.commit();
                return new Aluno(id, pessoaId, situacao);
            } catch (SQLException | RuntimeException erro) {
                reverter(c, erro);
                throw erro;
            }
        } catch (SQLException erro) {
            if (erro.getErrorCode() == 1062) throw new CadastroAlunoException(ErroCadastroAluno.ALUNO_JA_CADASTRADO);
            if (erro.getErrorCode() == 1452) throw new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA);
            throw new PersistenciaAcademicaException(erro);
        }
    }
    public Optional<Aluno> consultar(long alunoId) {
        if (alunoId <= 0) throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
        try (var c = banco.abrir()) {
            return consultar(c, alunoId, false);
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }
    public Aluno atualizarSituacao(long alunoId, SituacaoCadastral situacao, long usuarioId) {
        validar(alunoId, situacao, usuarioId);
        try (var c = banco.abrir()) {
            c.setAutoCommit(false);
            try {
                var anterior = consultar(c, alunoId, true).orElseThrow(
                        () -> new CadastroAlunoException(ErroCadastroAluno.ALUNO_NAO_ENCONTRADO));
                try (var s = c.prepareStatement("UPDATE aluno SET situacao_cadastral=? WHERE id=?")) {
                    s.setString(1, situacao.name());
                    s.setLong(2, alunoId);
                    s.executeUpdate();
                }
                auditar(c, alunoId, usuarioId, "ATUALIZACAO", anterior.situacaoCadastral(), situacao);
                c.commit();
                return new Aluno(alunoId, anterior.pessoaId(), situacao);
            } catch (SQLException | RuntimeException erro) {
                reverter(c, erro);
                throw erro;
            }
        } catch (SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }

    private Optional<Aluno> consultar(Connection c, long id, boolean bloquear) throws SQLException {
        String sql = "SELECT id, pessoa_id, situacao_cadastral FROM aluno WHERE id=?";
        if (bloquear) sql += " FOR UPDATE";
        try (var s = c.prepareStatement(sql)) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) return Optional.empty();
                return Optional.of(new Aluno(r.getLong("id"), r.getLong("pessoa_id"),
                        SituacaoCadastral.valueOf(r.getString("situacao_cadastral"))));
            }
        }
    }

    private void auditar(Connection c, long alunoId, long usuarioId, String operacao,
                         SituacaoCadastral anterior, SituacaoCadastral nova) throws SQLException {
        try (var s = c.prepareStatement("INSERT INTO aluno_auditoria (aluno_id,usuario_id,operacao,situacao_anterior,situacao_nova) VALUES (?,?,?,?,?)")) {
            s.setLong(1, alunoId); s.setLong(2, usuarioId); s.setString(3, operacao);
            s.setString(4, anterior == null ? null : anterior.name());
            s.setString(5, nova.name());
            s.executeUpdate();
        }
    }

    private void validar(long id, SituacaoCadastral situacao, long autor) {
        if (id <= 0 || autor <= 0 || situacao == null) throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
    }

    private void reverter(Connection c, Exception original) {
        try { c.rollback(); } catch (SQLException rollback) { original.addSuppressed(rollback); }
    }
}
