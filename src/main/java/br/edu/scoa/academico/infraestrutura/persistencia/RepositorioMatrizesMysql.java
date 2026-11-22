package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.curriculo.RepositorioMatrizes;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import static br.edu.scoa.academico.dominio.curriculo.FalhaMatriz.Codigo.*;

public final class RepositorioMatrizesMysql implements RepositorioMatrizes {
    private final ConfiguracaoBanco banco;
    public RepositorioMatrizesMysql(ConfiguracaoBanco banco) {this.banco=banco;}
    public MatrizCurricular salvar(MatrizCurricular matriz,long autor,Instant instante) {
        matriz.exigirEditavel();
        if(matriz.id()==0 && matriz.matrizOrigemId()!=null)throw new FalhaMatriz(IDENTIFICACAO_INVALIDA);
        try(var conexao=banco.abrir()){
            conexao.setAutoCommit(false);
            try{
                long id=matriz.id(), revisao=matriz.revisao()+1;
                if(id!=0) {
                    try(var comando=conexao.prepareStatement("SELECT curso_id,situacao,versao_registro FROM matriz_curricular WHERE id=? FOR UPDATE")){
                        comando.setLong(1,id);
                        try(var resultado=comando.executeQuery()){
                            if(!resultado.next())throw new FalhaMatriz(NAO_ENCONTRADA);
                            if(!"EM_ELABORACAO".equals(resultado.getString("situacao")))throw new FalhaMatriz(MATRIZ_NAO_EDITAVEL);
                            if(resultado.getLong("versao_registro")!=matriz.revisao())throw new FalhaMatriz(CONFLITO_EDICAO);
                            if(resultado.getLong("curso_id")!=matriz.cursoId())throw new FalhaMatriz(CURSO_IMUTAVEL);
                        }
                    }
                }
                // As FKs verificam curso e revisão de disciplina dentro desta transação.
                String sql=id==0
                    ?"INSERT INTO matriz_curricular(curso_id,codigo,versao,inicio_vigencia,fim_vigencia,situacao,versao_registro) VALUES(?,?,?,?,?,'EM_ELABORACAO',?)"
                    :"UPDATE matriz_curricular SET curso_id=?,codigo=?,versao=?,inicio_vigencia=?,fim_vigencia=?,versao_registro=? WHERE id=?";
                try(var comando=conexao.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                    comando.setLong(1,matriz.cursoId());comando.setString(2,matriz.codigo());comando.setString(3,matriz.versao());
                    comando.setObject(4,matriz.inicioVigencia());comando.setObject(5,matriz.fimVigencia());comando.setLong(6,revisao);
                    if(id!=0)comando.setLong(7,id);
                    comando.executeUpdate();
                    if(id==0)try(var chaves=comando.getGeneratedKeys()){if(!chaves.next())throw new SQLException("Sem identificador.");id=chaves.getLong(1);}
                }
                if(matriz.id()!=0){
                    var anteriores=lerPreRequisitos(conexao,id);
                    var mantidas=new HashSet<Long>();for(var componente:matriz.componentes())mantidas.add(componente.disciplinaId());
                    if(anteriores.stream().anyMatch(p->!mantidas.contains(p.disciplinaId())||!mantidas.contains(p.disciplinaExigidaId())))
                        throw new FalhaPreRequisito(FalhaPreRequisito.Codigo.COMPONENTE_UTILIZADO);
                }
                try(var comando=conexao.prepareStatement("DELETE FROM pre_requisito WHERE matriz_id=?")){
                    comando.setLong(1,id);comando.executeUpdate();
                }
                sincronizarComponentes(conexao,id,matriz.componentes());
                try(var comando=conexao.prepareStatement("INSERT INTO pre_requisito(matriz_id,disciplina_id,disciplina_exigida_id) VALUES(?,?,?)")){
                    for(var relacao:matriz.preRequisitos()){
                        comando.setLong(1,id);comando.setLong(2,relacao.disciplinaId());comando.setLong(3,relacao.disciplinaExigidaId());comando.addBatch();
                    }
                    comando.executeBatch();
                }
                auditar(conexao,id,revisao,autor,instante);
                var salva=ler(conexao,id);
                conexao.commit();
                return salva;
            }catch(SQLException|RuntimeException erro){
                try{conexao.rollback();}catch(SQLException falha){erro.addSuppressed(falha);}
                if(erro instanceof FalhaMatriz falha)throw falha;
                if(erro instanceof FalhaPreRequisito falha)throw falha;
                if(erro instanceof SQLException sql){
                    if(sql.getErrorCode()==1062)throw new FalhaMatriz(MATRIZ_DUPLICADA);
                    if(sql.getErrorCode()==1452)throw new FalhaMatriz(REFERENCIA_INEXISTENTE);
                }
                throw new FalhaMatriz(INDISPONIVEL);
            }
        }catch(SQLException erro){throw new FalhaMatriz(INDISPONIVEL);}
    }
    @FunctionalInterface private interface Operacao { MatrizCurricular executar(Connection conexao)throws SQLException; }
    private MatrizCurricular transacao(Operacao operacao){
        try(var conexao=banco.abrir()){
            conexao.setAutoCommit(false);
            try{var resultado=operacao.executar(conexao);conexao.commit();return resultado;}
            catch(SQLException|RuntimeException erro){
                try{conexao.rollback();}catch(SQLException falha){erro.addSuppressed(falha);}
                if(erro instanceof FalhaMatriz f)throw f;
                if(erro instanceof FalhaPreRequisito f)throw f;
                if(erro instanceof SQLException sql && sql.getErrorCode()==1062)throw new FalhaMatriz(MATRIZ_DUPLICADA);
                if(erro instanceof SQLException sql && sql.getErrorCode()==1452)throw new FalhaMatriz(REFERENCIA_INEXISTENTE);
                throw new FalhaMatriz(INDISPONIVEL);
            }
        }catch(SQLException erro){throw new FalhaMatriz(INDISPONIVEL);}
    }
    private MatrizCurricular bloquear(Connection conexao,long id,long revisao)throws SQLException{
        try(var comando=conexao.prepareStatement("SELECT versao_registro FROM matriz_curricular WHERE id=? FOR UPDATE")){
            comando.setLong(1,id);try(var resultado=comando.executeQuery()){
                if(!resultado.next())throw new FalhaMatriz(NAO_ENCONTRADA);
                if(resultado.getLong(1)!=revisao)throw new FalhaMatriz(CONFLITO_EDICAO);
            }
        }
        // Reconstrói o agregado com validação de composição, datas e grafo no estado real.
        return ler(conexao,id);
    }
    private void evento(Connection conexao,long id,long revisao,String evento)throws SQLException{
        try(var comando=conexao.prepareStatement("UPDATE matriz_historico SET evento=? WHERE matriz_id=? AND revisao=?")){
            comando.setString(1,evento);comando.setLong(2,id);comando.setLong(3,revisao);comando.executeUpdate();
        }
    }
    public MatrizCurricular publicar(long id,long revisao,long autor,Instant instante){
        return transacao(conexao->{
            var atual=bloquear(conexao,id,revisao);
            var publicada=atual.publicar();
            try(var comando=conexao.prepareStatement("UPDATE matriz_curricular SET situacao='VIGENTE',versao_registro=? WHERE id=?")){
                comando.setLong(1,publicada.revisao());comando.setLong(2,id);comando.executeUpdate();
            }
            auditar(conexao,id,publicada.revisao(),autor,instante);evento(conexao,id,publicada.revisao(),"PUBLICACAO");
            return publicada;
        });
    }
    public MatrizCurricular copiar(long origemId,long revisao,String codigo,String versao,
            java.time.LocalDate inicio,java.time.LocalDate fim,long autor,Instant instante){
        return transacao(conexao->{
            var origem=bloquear(conexao,origemId,revisao);
            var nova=origem.novaVersao(codigo,versao,inicio,fim);
            long id;
            try(var comando=conexao.prepareStatement("INSERT INTO matriz_curricular(curso_id,codigo,versao,inicio_vigencia,fim_vigencia,situacao,versao_registro,matriz_origem_id) VALUES(?,?,?,?,?,'EM_ELABORACAO',1,?)",Statement.RETURN_GENERATED_KEYS)){
                comando.setLong(1,nova.cursoId());comando.setString(2,nova.codigo());comando.setString(3,nova.versao());
                comando.setObject(4,nova.inicioVigencia());comando.setObject(5,nova.fimVigencia());comando.setLong(6,origemId);
                comando.executeUpdate();try(var chaves=comando.getGeneratedKeys()){if(!chaves.next())throw new SQLException("Sem identificador.");id=chaves.getLong(1);}
            }
            sincronizarComponentes(conexao,id,nova.componentes());
            try(var comando=conexao.prepareStatement("INSERT INTO pre_requisito(matriz_id,disciplina_id,disciplina_exigida_id) VALUES(?,?,?)")){
                for(var relacao:nova.preRequisitos()){
                    comando.setLong(1,id);comando.setLong(2,relacao.disciplinaId());comando.setLong(3,relacao.disciplinaExigidaId());comando.addBatch();
                }comando.executeBatch();
            }
            auditar(conexao,id,1,autor,instante);evento(conexao,id,1,"COPIA");
            return nova.identificada(id,1);
        });
    }
    private void sincronizarComponentes(Connection conexao,long id,List<ComponenteCurricular> componentes)throws SQLException {
        // Upsert preserva IDs existentes; a matriz está bloqueada até o commit.
        String sql="INSERT INTO componente_curricular(matriz_curricular_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada)"
            +" VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE disciplina_revisao=?,tipo=?,periodo_ideal=?,carga_horaria_considerada=?";
        try(var comando=conexao.prepareStatement(sql)){
            for(var componente:componentes){
                comando.setLong(1,id);comando.setLong(2,componente.disciplinaId());comando.setLong(3,componente.disciplinaRevisao());
                comando.setString(4,componente.tipo().name());comando.setInt(5,componente.periodoIdeal());comando.setInt(6,componente.cargaHorariaConsiderada());
                comando.setLong(7,componente.disciplinaRevisao());comando.setString(8,componente.tipo().name());
                comando.setInt(9,componente.periodoIdeal());comando.setInt(10,componente.cargaHorariaConsiderada());comando.addBatch();
            }
            comando.executeBatch();
        }
        String marcadores=String.join(",",Collections.nCopies(componentes.size(),"?"));
        try(var comando=conexao.prepareStatement("DELETE FROM componente_curricular WHERE matriz_curricular_id=? AND disciplina_id NOT IN ("+marcadores+")")){
            comando.setLong(1,id);int indice=2;for(var componente:componentes)comando.setLong(indice++,componente.disciplinaId());
            comando.executeUpdate();
        }
    }
    private void auditar(Connection conexao,long id,long revisao,long autor,Instant instante)throws SQLException{
        try(var comando=conexao.prepareStatement("INSERT INTO matriz_historico(matriz_id,revisao,curso_id,codigo,versao,inicio_vigencia,fim_vigencia,situacao,autor_id,instante,operacao_id,matriz_origem_id)"
                +" SELECT id,versao_registro,curso_id,codigo,versao,inicio_vigencia,fim_vigencia,situacao,?,?,?,matriz_origem_id FROM matriz_curricular WHERE id=?")){
            comando.setLong(1,autor);comando.setTimestamp(2,Timestamp.from(instante));comando.setString(3,UUID.randomUUID().toString());comando.setLong(4,id);comando.executeUpdate();
        }
        try(var comando=conexao.prepareStatement("INSERT INTO componente_historico(matriz_id,matriz_revisao,componente_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada)"
                +" SELECT matriz_curricular_id,?,id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada FROM componente_curricular WHERE matriz_curricular_id=?")){
            comando.setLong(1,revisao);comando.setLong(2,id);comando.executeUpdate();
        }
        try(var comando=conexao.prepareStatement("INSERT INTO pre_requisito_historico(matriz_id,matriz_revisao,disciplina_id,disciplina_exigida_id,tipo) SELECT matriz_id,?,disciplina_id,disciplina_exigida_id,tipo FROM pre_requisito WHERE matriz_id=?")){
            comando.setLong(1,revisao);comando.setLong(2,id);comando.executeUpdate();
        }
    }
    private List<PreRequisito> lerPreRequisitos(Connection conexao,long id)throws SQLException{
        var relacoes=new ArrayList<PreRequisito>();
        try(var comando=conexao.prepareStatement("SELECT disciplina_id,disciplina_exigida_id FROM pre_requisito WHERE matriz_id=? ORDER BY disciplina_id,disciplina_exigida_id")){
            comando.setLong(1,id);try(var resultado=comando.executeQuery()){
                while(resultado.next())relacoes.add(new PreRequisito(resultado.getLong(1),resultado.getLong(2)));
            }
        }
        return List.copyOf(relacoes);
    }
    private MatrizCurricular ler(Connection conexao,long id)throws SQLException {
        try(var comando=conexao.prepareStatement("SELECT * FROM matriz_curricular WHERE id=?")){
            comando.setLong(1,id);
            try(var resultado=comando.executeQuery()){
                if(!resultado.next())throw new FalhaMatriz(NAO_ENCONTRADA);
                var componentes=new ArrayList<ComponenteCurricular>();
                try(var itens=conexao.prepareStatement("SELECT * FROM componente_curricular WHERE matriz_curricular_id=? ORDER BY disciplina_id")){
                    itens.setLong(1,id);
                    try(var item=itens.executeQuery()){
                        while(item.next())componentes.add(new ComponenteCurricular(item.getLong("disciplina_id"),item.getLong("disciplina_revisao"),
                            TipoComponente.valueOf(item.getString("tipo")),item.getInt("periodo_ideal"),item.getInt("carga_horaria_considerada")));
                    }
                }
                return new MatrizCurricular(id,resultado.getLong("curso_id"),resultado.getString("codigo"),resultado.getString("versao"),
                    resultado.getObject("inicio_vigencia",java.time.LocalDate.class),resultado.getObject("fim_vigencia",java.time.LocalDate.class),
                    SituacaoMatriz.valueOf(resultado.getString("situacao")),resultado.getLong("versao_registro"),componentes,lerPreRequisitos(conexao,id),
                    resultado.getObject("matriz_origem_id",Long.class));
            }
        }
    }
    public MatrizCurricular consultar(long id){
        try(var conexao=banco.abrir()){
            conexao.setReadOnly(true);conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);conexao.setAutoCommit(false);
            var matriz=ler(conexao,id);conexao.commit();return matriz;
        }catch(SQLException erro){throw new FalhaMatriz(INDISPONIVEL);}
    }
    public List<MatrizCurricular> pesquisar(long cursoId,String termo){
        try(var conexao=banco.abrir()){
            conexao.setReadOnly(true);conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);conexao.setAutoCommit(false);
            var ids=new ArrayList<Long>();
            try(var comando=conexao.prepareStatement("SELECT id FROM matriz_curricular WHERE (?=0 OR curso_id=?) AND (LOCATE(UPPER(?),UPPER(codigo))>0 OR LOCATE(UPPER(?),UPPER(versao))>0) ORDER BY codigo,versao,id")){
                comando.setLong(1,cursoId);comando.setLong(2,cursoId);comando.setString(3,termo);comando.setString(4,termo);
                try(var resultado=comando.executeQuery()){while(resultado.next())ids.add(resultado.getLong(1));}
            }
            var matrizes=new ArrayList<MatrizCurricular>();for(long id:ids)matrizes.add(ler(conexao,id));
            conexao.commit();return List.copyOf(matrizes);
        }catch(SQLException erro){throw new FalhaMatriz(INDISPONIVEL);}
    }
}

