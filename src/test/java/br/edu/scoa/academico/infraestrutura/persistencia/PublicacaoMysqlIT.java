package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PublicacaoMysqlIT extends MatrizesMysqlIT {
    @Test void publicacaoDisputaComEdicaoSemPerderAtualizacao()throws Exception{
        var original=base();var inicio=new CountDownLatch(1);
        Callable<Boolean> publicar=()->{inicio.await();try{repo.publicar(original.id(),original.revisao(),7,AGORA);return true;}
            catch(FalhaMatriz erro){assertEquals(FalhaMatriz.Codigo.CONFLITO_EDICAO,erro.codigo());return false;}};
        Callable<Boolean> editar=()->{inicio.await();try{salvar(original.comPreRequisitos(List.of()));return true;}
            catch(FalhaMatriz erro){assertTrue(erro.codigo()==FalhaMatriz.Codigo.CONFLITO_EDICAO||erro.codigo()==FalhaMatriz.Codigo.MATRIZ_NAO_EDITAVEL);return false;}};
        try(var executor=Executors.newFixedThreadPool(2)){
            var a=executor.submit(publicar);var b=executor.submit(editar);inicio.countDown();
            boolean publicou=a.get(10,TimeUnit.SECONDS);assertNotEquals(publicou,b.get(10,TimeUnit.SECONDS));
            var atual=repo.consultar(original.id());assertEquals(2,atual.revisao());
            assertEquals(publicou?1:0,atual.preRequisitos().size());
        }
    }
    @Test void versoesPublicadasCoexistemComAuditoria()throws Exception{
        var original=base();var publicada=repo.publicar(original.id(),1,7,AGORA);
        var copia=repo.copiar(publicada.id(),publicada.revisao(),"ADS","2028",LocalDate.of(2028,1,1),null,7,AGORA);
        repo.publicar(copia.id(),copia.revisao(),7,AGORA);
        assertEquals(SituacaoMatriz.VIGENTE,repo.consultar(original.id()).situacao());
        assertEquals(SituacaoMatriz.VIGENTE,repo.consultar(copia.id()).situacao());
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT evento,COUNT(*) FROM matriz_historico GROUP BY evento")){
            var eventos=new HashMap<String,Integer>();while(r.next())eventos.put(r.getString(1),r.getInt(2));
            assertEquals(2,eventos.get("PUBLICACAO"));assertEquals(1,eventos.get("COPIA"));assertEquals(1,eventos.get("ALTERACAO"));
        }
    }
    MatrizCurricular base(){return salvar(nova(List.of(componente(disciplina),componente(segunda)))
        .comPreRequisitos(List.of(new PreRequisito(segunda.id(),disciplina.id()))));}
    @Test void publicaUmaVezEPreservaOrigemNaCopia()throws Exception{
        var original=base();var publicada=repo.publicar(original.id(),original.revisao(),7,AGORA);
        assertEquals(SituacaoMatriz.VIGENTE,publicada.situacao());
        assertThrows(FalhaMatriz.class,()->salvar(original));
        assertThrows(FalhaMatriz.class,()->repo.publicar(original.id(),original.revisao(),7,AGORA));
        var copia=repo.copiar(publicada.id(),publicada.revisao(),"ADS","2028",LocalDate.of(2028,1,1),null,7,AGORA);
        assertEquals(publicada.id(),copia.matrizOrigemId());assertNotEquals(publicada.id(),copia.id());
        assertEquals(publicada.componentes(),copia.componentes());assertEquals(publicada.preRequisitos(),copia.preRequisitos());
        assertEquals(SituacaoMatriz.EM_ELABORACAO,copia.situacao());
        var editada=salvar(copia.comPreRequisitos(List.of()));
        assertEquals(publicada.id(),repo.consultar(editada.id()).matrizOrigemId());
        assertEquals(1,repo.consultar(publicada.id()).preRequisitos().size());
        try(var c=banco.abrir();var s=c.prepareStatement("SELECT COUNT(DISTINCT id) FROM componente_curricular WHERE matriz_curricular_id IN (?,?)")){
            s.setLong(1,publicada.id());s.setLong(2,copia.id());try(var r=s.executeQuery()){r.next();assertEquals(4,r.getInt(1));}
        }
    }
    @Test void naoCopiaRascunhoNemRevisaoDesatualizada(){
        var original=base();assertThrows(FalhaMatriz.class,()->repo.copiar(original.id(),1,"ADS","2028",LocalDate.of(2028,1,1),null,7,AGORA));
        var editada=salvar(original);
        assertEquals(FalhaMatriz.Codigo.CONFLITO_EDICAO,assertThrows(FalhaMatriz.class,()->repo.publicar(original.id(),1,7,AGORA)).codigo());
        assertEquals(editada,repo.consultar(original.id()));
    }
    @Test void duasCopiasComMesmaIdentificacaoNaoDuplicam()throws Exception{
        var original=base();var publicada=repo.publicar(original.id(),1,7,AGORA);var inicio=new CountDownLatch(1);
        Callable<Boolean> copiar=()->{
            inicio.await();
            try{repo.copiar(publicada.id(),publicada.revisao(),"ADS","2028",LocalDate.of(2028,1,1),null,7,AGORA);return true;}
            catch(FalhaMatriz erro){assertEquals(FalhaMatriz.Codigo.MATRIZ_DUPLICADA,erro.codigo());return false;}
        };
        try(var executor=Executors.newFixedThreadPool(2)){
            var a=executor.submit(copiar);var b=executor.submit(copiar);inicio.countDown();
            assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(2,repo.pesquisar(curso.id(),"").size());
    }
    @Test void falhaNaAuditoriaDesfazPublicacaoECopia()throws Exception{
        var original=base();
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TRIGGER teste_matriz_auditoria BEFORE INSERT ON matriz_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");}
        assertThrows(FalhaMatriz.class,()->repo.publicar(original.id(),1,7,AGORA));
        assertEquals(original,repo.consultar(original.id()));
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("DROP TRIGGER teste_matriz_auditoria");}
        var publicada=repo.publicar(original.id(),1,7,AGORA);
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TRIGGER teste_matriz_auditoria BEFORE INSERT ON matriz_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");}
        assertThrows(FalhaMatriz.class,()->repo.copiar(publicada.id(),publicada.revisao(),"ADS","2028",LocalDate.of(2028,1,1),null,7,AGORA));
        assertEquals(1,repo.pesquisar(curso.id(),"").size());
    }
}
