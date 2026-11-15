package br.edu.scoa.academico.infraestrutura.identidade;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.infraestrutura.persistencia.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import static br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.Codigo.*;

public final class RepositorioIdentidadeMysql implements RepositorioIdentidade {
    private final ConfiguracaoBanco banco;
    private final Senhas senhas;
    private final String hashFicticio;
    public RepositorioIdentidadeMysql(ConfiguracaoBanco banco,Senhas senhas){this.banco=banco;this.senhas=senhas;this.hashFicticio=senhas.gerar(UUID.randomUUID().toString().toCharArray());}
    private interface Trabalho<T>{T executar(Connection c)throws SQLException;}
    private <T>T transacao(Trabalho<T> trabalho){
        try(var c=banco.abrir()){
            c.setAutoCommit(false);
            try{T resultado=trabalho.executar(c);c.commit();return resultado;}
            catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
        }catch(SQLException e){if(e.getErrorCode()==1062)throw new FalhaIdentidade(CONTA_DUPLICADA);throw new PersistenciaAcademicaException(e);}
    }
    private void auditar(Connection c,Long autor,String operacao,String entidade,Long id)throws SQLException{
        try(var s=c.prepareStatement("INSERT INTO identidade_auditoria(usuario_id,operacao,entidade,entidade_id) VALUES(?,?,?,?)")){
            if(autor==null)s.setNull(1,Types.INTEGER);else s.setLong(1,autor);s.setString(2,operacao);s.setString(3,entidade);if(id==null)s.setNull(4,Types.INTEGER);else s.setLong(4,id);s.executeUpdate();
        }
    }
    private Optional<PessoaParaCadastro> pessoa(Connection c,String coluna,Object valor,boolean lock)throws SQLException{
        // coluna é escolhida somente pelos métodos internos abaixo.
        try(var s=c.prepareStatement("SELECT id,cpf,nome,email,telefone FROM pessoa WHERE "+coluna+"=?"+(lock?" FOR UPDATE":""))){
            s.setObject(1,valor);try(var r=s.executeQuery()){if(!r.next())return Optional.empty();
                if(r.getString(2)==null||r.getString(3)==null||r.getString(4)==null)throw new FalhaIdentidade(PESSOA_INCOMPLETA);
                return Optional.of(new PessoaParaCadastro(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),Objects.toString(r.getString(5),"")));}
        }
    }
    public Optional<PessoaParaCadastro> pessoaPorCpf(String cpf){return transacao(c->pessoa(c,"cpf",cpf,false));}
    public Optional<PessoaParaCadastro> pessoaPorId(long id){return transacao(c->pessoa(c,"id",id,false));}
    public PessoaParaCadastro criarPessoa(String cpf,String nome,String email,String telefone,long autor){
        return transacao(c->criarPessoa(c,cpf,nome,email,telefone,autor));
    }
    private PessoaParaCadastro criarPessoa(Connection c,String cpf,String nome,String email,String telefone,Long autor)throws SQLException{
        bloquearAdministracao(c);
        var existente=pessoa(c,"cpf",cpf,true);if(existente.isPresent())return existente.get();
        long proximoId;
        try(var s=c.createStatement();var r=s.executeQuery("SELECT COALESCE(MAX(id),0)+1 FROM pessoa")){r.next();proximoId=r.getLong(1);}
        boolean criada=true;
        try(var s=c.prepareStatement("INSERT INTO pessoa(cpf,nome,email,telefone,id) VALUES(?,?,?,?,?)")){
            s.setString(1,cpf);s.setString(2,nome);s.setString(3,email);s.setString(4,telefone);s.setLong(5,proximoId);
            try{s.executeUpdate();}catch(SQLException e){if(e.getErrorCode()!=1062)throw e;criada=false;}
        }
        var resultado=pessoa(c,"cpf",cpf,true).orElseThrow(()->new FalhaIdentidade(PESSOA_INCOMPLETA));
        if(criada)auditar(c,autor,"PESSOA_CRIADA","PESSOA",resultado.pessoaId());return resultado;
    }
    public PessoaParaCadastro atualizarPessoa(PessoaParaCadastro esperada,String nome,String email,String telefone,long autor){
        return transacao(c->{
            var atual=pessoa(c,"id",esperada.pessoaId(),true).orElseThrow(()->new FalhaIdentidade(PESSOA_INCOMPLETA));
            if(!atual.equals(esperada))throw new FalhaDadosPessoais(FalhaDadosPessoais.Codigo.CONFLITO_EDICAO);
            try(var s=c.prepareStatement("UPDATE pessoa SET nome=?,email=?,telefone=? WHERE id=?")){s.setString(1,nome);s.setString(2,email);s.setString(3,telefone);s.setLong(4,atual.pessoaId());s.executeUpdate();}
            auditar(c,autor,"PESSOA_ATUALIZADA","PESSOA",atual.pessoaId());return new PessoaParaCadastro(atual.pessoaId(),atual.cpf(),nome,email,telefone);
        });
    }
    public VinculosAcademicos vinculos(long pessoaId){return transacao(c->vinculos(c,pessoaId,false));}
    private VinculosAcademicos vinculos(Connection c,long pessoaId,boolean bloquear)throws SQLException{
        boolean aluno=false,professor=false,completo=false;
        String trava=bloquear?" FOR SHARE":"";
        try(var s=c.prepareStatement("SELECT situacao_cadastral FROM aluno WHERE pessoa_id=?"+trava)){
            s.setLong(1,pessoaId);try(var r=s.executeQuery()){aluno=r.next()&&r.getString(1)!=null;}
        }
        try(var s=c.prepareStatement("SELECT titulacao,situacao_funcional FROM professor WHERE pessoa_id=?"+trava)){
            s.setLong(1,pessoaId);try(var r=s.executeQuery()){if(r.next()){professor=true;completo=r.getString(2)!=null&&br.edu.scoa.academico.dominio.professores.Titulacao.aceita(r.getString(1));}}
        }
        return new VinculosAcademicos(aluno,professor,completo);
    }
    private Conta lerConta(ResultSet r)throws SQLException{return new Conta(r.getLong("id"),r.getLong("pessoa_id"),r.getString("login"),Perfil.valueOf(r.getString("perfil")),r.getBoolean("esta_ativo"),r.getLong("versao"));}
    private Optional<Conta> conta(Connection c,String coluna,long id)throws SQLException{
        try(var s=c.prepareStatement("SELECT id,pessoa_id,login,perfil,esta_ativo,versao FROM usuario WHERE "+coluna+"=?")){s.setLong(1,id);try(var r=s.executeQuery()){return r.next()?Optional.of(lerConta(r)):Optional.empty();}}
    }
    public Optional<Conta> conta(long id){return transacao(c->conta(c,"id",id));}
    public Optional<Conta> contaPorPessoa(long id){return transacao(c->conta(c,"pessoa_id",id));}
    private void bloquearAdministracao(Connection c)throws SQLException{try(var s=c.createStatement();var r=s.executeQuery("SELECT id FROM controle_acesso WHERE id=1 FOR UPDATE")){if(!r.next())throw new SQLException("Controle de acesso ausente");}}
    private Conta criarConta(Connection c,long pessoaId,String login,String hash,Perfil perfil,Long autor)throws SQLException{
        pessoa(c,"id",pessoaId,false).orElseThrow(()->new FalhaIdentidade(PESSOA_INCOMPLETA));
        if(perfil!=Perfil.COORDENADOR) vinculos(c,pessoaId,true).exigirPerfil(perfil);
        long id;
        try(var s=c.prepareStatement("INSERT INTO usuario(pessoa_id,login,senha_hash,perfil) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            s.setLong(1,pessoaId);s.setString(2,login);s.setString(3,hash);s.setString(4,perfil.name());s.executeUpdate();try(var r=s.getGeneratedKeys()){r.next();id=r.getLong(1);}
        }
        auditar(c,autor==null?id:autor,"CONTA_CRIADA","USUARIO",id);return conta(c,"id",id).orElseThrow();
    }
    public Conta criarConta(long pessoaId,String login,String hash,Perfil perfil,long autor){return transacao(c->{bloquearAdministracao(c);return criarConta(c,pessoaId,login,hash,perfil,autor);});}
    public Conta inicializar(String cpf,String nome,String email,String login,String hash){
        return transacao(c->{bloquearAdministracao(c);
            try(var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM usuario")){r.next();if(r.getInt(1)>0)throw new FalhaIdentidade(INICIALIZACAO_JA_REALIZADA);}
            var p=criarPessoa(c,cpf,nome,email,"",null);return criarConta(c,p.pessoaId(),login,hash,Perfil.COORDENADOR,null);
        });
    }
    public Conta atualizarConta(Conta esperada,Perfil perfil,boolean ativa,long autor){
        return transacao(c->{bloquearAdministracao(c);var atual=conta(c,"id",esperada.id()).orElseThrow(()->new FalhaIdentidade(CONTA_NAO_ENCONTRADA));
            if(!atual.equals(esperada))throw new FalhaIdentidade(CONFLITO);
            if(atual.perfil()==Perfil.COORDENADOR&&atual.estaAtiva()&&(!ativa||perfil!=Perfil.COORDENADOR))
                try(var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM usuario WHERE perfil='COORDENADOR' AND esta_ativo=TRUE")){r.next();if(r.getInt(1)<=1)throw new FalhaIdentidade(ULTIMO_COORDENADOR);}
            if(perfil!=atual.perfil()) vinculos(c,atual.pessoaId(),true).exigirPerfil(perfil);
            try(var s=c.prepareStatement("UPDATE usuario SET perfil=?,esta_ativo=?,versao=versao+1 WHERE id=?")){s.setString(1,perfil.name());s.setBoolean(2,ativa);s.setLong(3,atual.id());s.executeUpdate();}
            auditar(c,autor,"CONTA_ATUALIZADA","USUARIO",atual.id());return conta(c,"id",atual.id()).orElseThrow();
        });
    }
    public Conta autenticar(String login,char[] senha,Instant agora){
        Conta resultado=transacao(c->{
            try(var s=c.prepareStatement("SELECT * FROM usuario WHERE login=? FOR UPDATE")){
                s.setString(1,login);try(var r=s.executeQuery()){
                    if(!r.next()){senhas.confere(senha,hashFicticio);auditar(c,null,"LOGIN_RECUSADO","USUARIO",null);return null;}
                    var conta=lerConta(r);int tentativas=r.getInt("tentativas");Timestamp bloqueio=r.getTimestamp("bloqueado_ate");
                    boolean bloqueada=bloqueio!=null&&agora.isBefore(bloqueio.toInstant());
                    boolean senhaCorreta=senhas.confere(senha,r.getString("senha_hash"));
                    if(bloqueada||!conta.estaAtiva()){auditar(c,conta.id(),"LOGIN_RECUSADO","USUARIO",conta.id());return null;}
                    if(bloqueio!=null)tentativas=0;
                    if(senhaCorreta){
                        try(var u=c.prepareStatement("UPDATE usuario SET tentativas=0,bloqueado_ate=NULL WHERE id=?")){u.setLong(1,conta.id());u.executeUpdate();}
                        auditar(c,conta.id(),"LOGIN_REALIZADO","USUARIO",conta.id());return conta;
                    }
                    tentativas++;
                    try(var u=c.prepareStatement("UPDATE usuario SET tentativas=?,bloqueado_ate=? WHERE id=?")){
                        u.setInt(1,tentativas);if(tentativas>=5)u.setTimestamp(2,Timestamp.from(agora.plusSeconds(300)));else u.setNull(2,Types.TIMESTAMP);u.setLong(3,conta.id());u.executeUpdate();}
                    auditar(c,conta.id(),"LOGIN_RECUSADO","USUARIO",conta.id());return null;
                }
            }
        });
        if(resultado==null)throw new FalhaIdentidade(CREDENCIAIS_INVALIDAS);return resultado;
    }
    public void registrarSaida(long autor){transacao(c->{auditar(c,autor,"LOGOUT","USUARIO",autor);return null;});}
}
