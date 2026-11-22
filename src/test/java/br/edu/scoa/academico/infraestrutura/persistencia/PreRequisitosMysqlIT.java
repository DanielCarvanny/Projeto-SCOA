package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreRequisitosMysqlIT extends MatrizesMysqlIT {
    @Test void falhaNoSnapshotDesfazRelacoesERevisao()throws Exception{
        var matriz=base();
        try(var c=banco.abrir();var s=c.createStatement()){
            s.execute("CREATE TRIGGER teste_pre_historico BEFORE INSERT ON pre_requisito_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");
        }
        try{
            assertThrows(FalhaMatriz.class,()->salvar(matriz.comPreRequisitos(List.of(new PreRequisito(segunda.id(),disciplina.id())))));
            assertEquals(matriz,repo.consultar(matriz.id()));
        }finally{try(var c=banco.abrir();var s=c.createStatement()){s.execute("DROP TRIGGER IF EXISTS teste_pre_historico");}}
    }
    @Test void naoRemoveComponenteERelacoesSilenciosamenteNaMesmaGravacao(){
        var salva=salvar(base().comPreRequisitos(List.of(new PreRequisito(segunda.id(),disciplina.id()))));
        var forjada=new MatrizCurricular(salva.id(),salva.cursoId(),salva.codigo(),salva.versao(),salva.inicioVigencia(),null,
            salva.situacao(),salva.revisao(),List.of(componente(segunda)));
        assertEquals(FalhaPreRequisito.Codigo.COMPONENTE_UTILIZADO,assertThrows(FalhaPreRequisito.class,()->salvar(forjada)).codigo());
        var semRelacoes=salvar(salva.comPreRequisitos(List.of()));
        assertEquals(1,salvar(semRelacoes.remover(disciplina.id())).componentes().size());
    }
    MatrizCurricular base(){return salvar(nova(List.of(componente(disciplina),componente(segunda))));}
    @Test void reabreRelacoesEAuditaInclusaoERemocao()throws Exception{
        var matriz=base();var relacao=new PreRequisito(segunda.id(),disciplina.id());
        var salva=salvar(matriz.comPreRequisitos(List.of(relacao)));
        assertEquals(List.of(relacao),repo.consultar(salva.id()).preRequisitos());
        salvar(salva.comPreRequisitos(List.of()));
        assertTrue(repo.consultar(salva.id()).preRequisitos().isEmpty());
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM pre_requisito_historico")){
            r.next();assertEquals(1,r.getInt(1));
        }
    }
    @Test void alteracaoDeComponentePreservaRelacoes(){
        var matriz=base().comPreRequisitos(List.of(new PreRequisito(segunda.id(),disciplina.id())));
        var salva=salvar(matriz);
        var editada=new MatrizCurricular(salva.id(),salva.cursoId(),salva.codigo(),salva.versao(),salva.inicioVigencia(),salva.fimVigencia(),
            salva.situacao(),salva.revisao(),List.of(new ComponenteCurricular(disciplina.id(),1,TipoComponente.ELETIVO,3,40),componente(segunda)),salva.preRequisitos());
        assertEquals(salva.preRequisitos(),repo.consultar(salvar(editada).id()).preRequisitos());
        assertThrows(FalhaPreRequisito.class,()->salva.remover(disciplina.id()));
    }
    @Test void relacoesOpostasConcorrentesNaoFormamCiclo()throws Exception{
        var matriz=base();var largada=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)){
            var a=executor.submit(()->editarDepois(largada,matriz.comPreRequisitos(List.of(new PreRequisito(disciplina.id(),segunda.id())))));
            var b=executor.submit(()->editarDepois(largada,matriz.comPreRequisitos(List.of(new PreRequisito(segunda.id(),disciplina.id())))));
            largada.countDown();assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,repo.consultar(matriz.id()).preRequisitos().size());
    }
}
