package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.professores.*;
import java.sql.*;
import java.util.Optional;

public final class RepositorioProfessoresMysql implements RepositorioProfessores {
    private final ConfiguracaoBanco banco;
    public RepositorioProfessoresMysql(ConfiguracaoBanco banco) { this.banco=banco; }
    public Professor cadastrar(long pessoaId,String titulacao,SituacaoFuncional situacao,long autor) {
        String titulo=validar(pessoaId,titulacao,situacao,autor);
        try(var c=banco.abrir()) {
            c.setAutoCommit(false);
            try {
                long id;
                try(var s=c.prepareStatement("INSERT INTO professor(pessoa_id,titulacao,situacao_funcional) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    s.setLong(1,pessoaId); s.setString(2,titulo); s.setString(3,situacao.name()); s.executeUpdate();
                    try(var r=s.getGeneratedKeys()) { if(!r.next()) throw new SQLException("ID não retornado"); id=r.getLong(1); }
                }
                var novo=new Professor(id,pessoaId,titulo,situacao); auditar(c,null,novo,autor); c.commit(); return novo;
            } catch(SQLException|RuntimeException erro) { reverter(c,erro); throw erro; }
        } catch(SQLException erro) {
            if(erro.getErrorCode()==1062) throw new FalhaProfessor(FalhaProfessor.Codigo.PROFESSOR_JA_CADASTRADO);
            if(erro.getErrorCode()==1452) throw new FalhaProfessor(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA);
            throw new PersistenciaAcademicaException(erro);
        }
    }
    public Optional<Professor> consultar(long id) {
        if(id<=0) throw new FalhaProfessor(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        try(var c=banco.abrir()) { return consultar(c,id,false); }
        catch(SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }
    public Optional<Professor> consultarPorPessoa(long pessoaId) {
        if(pessoaId<=0) throw new FalhaProfessor(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        try(var c=banco.abrir();var s=c.prepareStatement("SELECT id,pessoa_id,titulacao,situacao_funcional FROM professor WHERE pessoa_id=?")) {
            s.setLong(1,pessoaId);
            try(var r=s.executeQuery()) {return r.next()?Optional.of(new Professor(r.getLong(1),r.getLong(2),r.getString(3),SituacaoFuncional.valueOf(r.getString(4)))):Optional.empty();}
        } catch(SQLException erro) {throw new PersistenciaAcademicaException(erro);}
    }
    public Professor atualizar(Professor esperado,String titulacao,SituacaoFuncional situacao,long autor) {
        if(esperado==null) throw new FalhaProfessor(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        String titulo=validar(esperado.id(),titulacao,situacao,autor);
        try(var c=banco.abrir()) {
            c.setAutoCommit(false);
            try {
                var atual=consultar(c,esperado.id(),true).orElseThrow(()->new FalhaProfessor(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO));
                if(!atual.equals(esperado)) throw new FalhaProfessor(FalhaProfessor.Codigo.CONFLITO_EDICAO);
                try(var s=c.prepareStatement("UPDATE professor SET titulacao=?,situacao_funcional=? WHERE id=?")) {
                    s.setString(1,titulo); s.setString(2,situacao.name()); s.setLong(3,atual.id()); s.executeUpdate();
                }
                var novo=new Professor(atual.id(),atual.pessoaId(),titulo,situacao); auditar(c,atual,novo,autor); c.commit(); return novo;
            } catch(SQLException|RuntimeException erro) { reverter(c,erro); throw erro; }
        } catch(SQLException erro) { throw new PersistenciaAcademicaException(erro); }
    }
    private Optional<Professor> consultar(Connection c,long id,boolean bloquear) throws SQLException {
        try(var s=c.prepareStatement("SELECT id,pessoa_id,titulacao,situacao_funcional FROM professor WHERE id=?"+(bloquear?" FOR UPDATE":""))) {
            s.setLong(1,id);
            try(var r=s.executeQuery()) {
                if(!r.next()) return Optional.empty();
                return Optional.of(new Professor(r.getLong(1),r.getLong(2),r.getString(3),SituacaoFuncional.valueOf(r.getString(4))));
            }
        }
    }
    private void auditar(Connection c,Professor antes,Professor novo,long autor) throws SQLException {
        try(var s=c.prepareStatement("INSERT INTO professor_auditoria(professor_id,usuario_id,operacao,titulacao_anterior,titulacao_nova,situacao_anterior,situacao_nova) VALUES(?,?,?,?,?,?,?)")) {
            s.setLong(1,novo.id()); s.setLong(2,autor); s.setString(3,antes==null?"CADASTRO":"ATUALIZACAO");
            s.setString(4,antes==null?null:antes.titulacao()); s.setString(5,novo.titulacao());
            s.setString(6,antes==null?null:antes.situacaoFuncional().name()); s.setString(7,novo.situacaoFuncional().name()); s.executeUpdate();
        }
    }
    private String validar(long id,String titulo,SituacaoFuncional situacao,long autor) {
        if(id<=0||autor<=0) throw new FalhaProfessor(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        return GestaoProfessores.validarDados(titulo,situacao);
    }
    private void reverter(Connection c,Exception erro) { try { c.rollback(); } catch(SQLException falha) { erro.addSuppressed(falha); } }
}
