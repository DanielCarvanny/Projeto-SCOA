package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.academico.aplicacao.curriculo.RepositorioPeriodos;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import static br.edu.scoa.academico.dominio.curriculo.FalhaPeriodo.Codigo.*;

public final class RepositorioPeriodosMysql implements RepositorioPeriodos {
    private final ConfiguracaoBanco banco;
    public RepositorioPeriodosMysql(ConfiguracaoBanco banco){this.banco=banco;}
    @FunctionalInterface private interface Trabalho{PeriodoLetivo executar(Connection conexao)throws SQLException;}
    private PeriodoLetivo transacao(Trabalho trabalho){
        try(var c=banco.abrir()){
            c.setAutoCommit(false);
            try{var salvo=trabalho.executar(c);c.commit();return salvo;}
            catch(SQLException|RuntimeException erro){
                try{c.rollback();}catch(SQLException falha){erro.addSuppressed(falha);}
                if(erro instanceof FalhaPeriodo falha)throw falha;
                throw new FalhaPeriodo(INDISPONIVEL);
            }
        }catch(SQLException erro){throw new FalhaPeriodo(INDISPONIVEL);}
    }
    public PeriodoLetivo salvar(PeriodoLetivo p,UUID operacao,long autor,Instant instante){
        p.exigirEditavel();if(operacao==null)throw new FalhaPeriodo(OPERACAO_REUTILIZADA);
        return transacao(c->{
            long id=p.id();
            if(id==0){
                if(p.situacao()!=SituacaoPeriodoLetivo.PLANEJADO)throw new FalhaPeriodo(TRANSICAO_INVALIDA);
                // INSERT + unicidade serializam confirmações concorrentes da mesma operação.
                try(var s=c.prepareStatement("INSERT INTO periodo_letivo(descricao,data_inicio,data_fim,inicio_matricula,fim_matricula,inicio_inscricao,fim_inscricao,situacao,versao_registro,operacao_criacao) VALUES(?,?,?,?,?,?,?,?,1,?)",Statement.RETURN_GENERATED_KEYS)){
                    preencher(s,p);s.setString(9,operacao.toString());
                    try{s.executeUpdate();}catch(SQLException erro){
                        if(erro.getErrorCode()!=1062)throw erro;
                        return repeticao(c,p,operacao,autor);
                    }
                    try(var r=s.getGeneratedKeys()){if(!r.next())throw new SQLException("Sem ID");id=r.getLong(1);}
                }
            }else{
                var atual=bloquear(c,id,p.revisao());atual.exigirEditavel();
                if(atual.situacao()!=p.situacao())throw new FalhaPeriodo(TRANSICAO_INVALIDA);
                try(var s=c.prepareStatement("UPDATE periodo_letivo SET descricao=?,data_inicio=?,data_fim=?,inicio_matricula=?,fim_matricula=?,inicio_inscricao=?,fim_inscricao=?,situacao=?,versao_registro=versao_registro+1 WHERE id=?")){
                    preencher(s,p);s.setLong(9,id);s.executeUpdate();
                }
            }
            var salvo=p.identificado(id,p.revisao()+1);auditar(c,salvo,p.id()==0?"CADASTRO":"EDICAO",operacao,autor,instante);return salvo;
        });
    }
    private PeriodoLetivo repeticao(Connection c,PeriodoLetivo entrada,UUID operacao,long autor)throws SQLException{
        try(var s=c.prepareStatement("SELECT periodo_id AS id,revisao AS versao_registro,h.* FROM periodo_historico h WHERE operacao_id=? AND revisao=1 FOR UPDATE")){
            s.setString(1,operacao.toString());try(var r=s.executeQuery()){
                if(!r.next())throw new FalhaPeriodo(OPERACAO_REUTILIZADA);
                var salvo=ler(r);
                if(r.getLong("autor_id")!=autor||!salvo.equals(entrada.identificado(salvo.id(),1)))throw new FalhaPeriodo(OPERACAO_REUTILIZADA);
                return salvo;
            }
        }
    }
    public PeriodoLetivo transicionar(long id,long revisao,SituacaoPeriodoLetivo destino,long autor,Instant instante){
        return transacao(c->{
            var atual=bloquear(c,id,revisao);
            if(destino==null||destino==SituacaoPeriodoLetivo.PLANEJADO)throw new FalhaPeriodo(TRANSICAO_INVALIDA);
            var novo=(destino==SituacaoPeriodoLetivo.ABERTO?atual.abrir():atual.encerrar()).identificado(id,revisao+1);
            try(var s=c.prepareStatement("UPDATE periodo_letivo SET situacao=?,versao_registro=? WHERE id=?")){
                s.setString(1,novo.situacao().name());s.setLong(2,novo.revisao());s.setLong(3,id);s.executeUpdate();
            }
            auditar(c,novo,destino==SituacaoPeriodoLetivo.ABERTO?"ABERTURA":"ENCERRAMENTO",UUID.randomUUID(),autor,instante);return novo;
        });
    }
    private PeriodoLetivo bloquear(Connection c,long id,long revisao)throws SQLException{
        try(var s=c.prepareStatement("SELECT * FROM periodo_letivo WHERE id=? FOR UPDATE")){
            s.setLong(1,id);try(var r=s.executeQuery()){
                if(!r.next())throw new FalhaPeriodo(NAO_ENCONTRADO);var p=ler(r);
                if(p.revisao()!=revisao)throw new FalhaPeriodo(CONFLITO_EDICAO);return p;
            }
        }
    }
    private static void preencher(PreparedStatement s,PeriodoLetivo p)throws SQLException{
        s.setString(1,p.descricao());s.setObject(2,p.dataInicio());s.setObject(3,p.dataFim());
        s.setObject(4,LocalDateTime.ofInstant(p.inicioMatricula(),ZoneOffset.UTC));s.setObject(5,LocalDateTime.ofInstant(p.fimMatricula(),ZoneOffset.UTC));
        s.setObject(6,LocalDateTime.ofInstant(p.inicioInscricao(),ZoneOffset.UTC));s.setObject(7,LocalDateTime.ofInstant(p.fimInscricao(),ZoneOffset.UTC));s.setString(8,p.situacao().name());
    }
    private void auditar(Connection c,PeriodoLetivo p,String evento,UUID operacao,long autor,Instant instante)throws SQLException{
        try(var s=c.prepareStatement("INSERT INTO periodo_historico(descricao,data_inicio,data_fim,inicio_matricula,fim_matricula,inicio_inscricao,fim_inscricao,situacao,periodo_id,revisao,evento,autor_id,instante,origem,operacao_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,'ACADEMICO',?)")){
            preencher(s,p);s.setLong(9,p.id());s.setLong(10,p.revisao());s.setString(11,evento);s.setLong(12,autor);
            s.setObject(13,LocalDateTime.ofInstant(instante,ZoneOffset.UTC));s.setString(14,operacao.toString());s.executeUpdate();
        }
    }
    private static PeriodoLetivo ler(ResultSet r)throws SQLException{
        return new PeriodoLetivo(r.getLong("id"),r.getString("descricao"),r.getObject("data_inicio",LocalDate.class),r.getObject("data_fim",LocalDate.class),
            r.getObject("inicio_matricula",LocalDateTime.class).toInstant(ZoneOffset.UTC),r.getObject("fim_matricula",LocalDateTime.class).toInstant(ZoneOffset.UTC),
            r.getObject("inicio_inscricao",LocalDateTime.class).toInstant(ZoneOffset.UTC),r.getObject("fim_inscricao",LocalDateTime.class).toInstant(ZoneOffset.UTC),
            SituacaoPeriodoLetivo.valueOf(r.getString("situacao")),r.getLong("versao_registro"));
    }
    public PeriodoLetivo consultar(long id){
        try(var c=banco.abrir();var s=c.prepareStatement("SELECT * FROM periodo_letivo WHERE id=?")){
            s.setLong(1,id);try(var r=s.executeQuery()){if(!r.next())throw new FalhaPeriodo(NAO_ENCONTRADO);return ler(r);}
        }catch(SQLException erro){throw new FalhaPeriodo(INDISPONIVEL);}
    }
    public List<PeriodoLetivo> pesquisar(String descricao,SituacaoPeriodoLetivo situacao){
        var itens=new ArrayList<PeriodoLetivo>();
        try(var c=banco.abrir();var s=c.prepareStatement("SELECT * FROM periodo_letivo WHERE LOCATE(?,descricao)>0"+(situacao==null?"":" AND situacao=?")+" ORDER BY data_inicio DESC,id DESC")){
            s.setString(1,descricao==null?"":descricao.strip());if(situacao!=null)s.setString(2,situacao.name());
            try(var r=s.executeQuery()){while(r.next())itens.add(ler(r));}return List.copyOf(itens);
        }catch(SQLException erro){throw new FalhaPeriodo(INDISPONIVEL);}
    }
}
