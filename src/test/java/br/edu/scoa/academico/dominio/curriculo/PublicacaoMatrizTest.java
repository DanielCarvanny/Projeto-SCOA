package br.edu.scoa.academico.dominio.curriculo;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PublicacaoMatrizTest {
    MatrizCurricular matriz(){
        return new MatrizCurricular(1,2,"ADS","2027.1",LocalDate.of(2027,1,1),LocalDate.of(2027,12,31),
            SituacaoMatriz.EM_ELABORACAO,1,List.of(new ComponenteCurricular(10,3,TipoComponente.OBRIGATORIO,1,80)));
    }
    @Test void publicarNaoAntecipaVigencia(){
        var publicada=matriz().publicar();
        assertEquals(SituacaoMatriz.VIGENTE,publicada.situacao());
        assertFalse(publicada.estaVigente(LocalDate.of(2026,12,31)));
        assertTrue(publicada.estaVigente(LocalDate.of(2027,1,1)));
        assertTrue(publicada.estaVigente(LocalDate.of(2027,12,31)));
        assertFalse(publicada.estaVigente(LocalDate.of(2028,1,1)));
        assertFalse(matriz().estaVigente(LocalDate.of(2027,2,1)));
    }
    @Test void publicadaNaoPermiteEdicaoNemSegundaPublicacao(){
        var publicada=matriz().publicar();
        assertThrows(FalhaMatriz.class,publicada::exigirEditavel);
        assertThrows(FalhaMatriz.class,publicada::publicar);
    }
    @Test void copiaPreservaConteudoMasNasceEmElaboracao(){
        var origem=matriz().publicar();
        var copia=origem.novaVersao("ADS","2028.1",LocalDate.of(2028,1,1),null);
        assertEquals(0,copia.id());assertEquals(0,copia.revisao());assertEquals(1,copia.matrizOrigemId());
        assertEquals(origem.componentes(),copia.componentes());assertEquals(origem.preRequisitos(),copia.preRequisitos());
        assertEquals(SituacaoMatriz.EM_ELABORACAO,copia.situacao());
        assertEquals("2027.1",origem.versao());
    }
    @Test void naoCopiaRascunhoNemIdentificacaoDuplicada(){
        assertThrows(FalhaMatriz.class,()->matriz().novaVersao("ADS","2028",LocalDate.of(2028,1,1),null));
        assertThrows(FalhaMatriz.class,()->matriz().publicar().novaVersao("ADS","2027.1",LocalDate.of(2028,1,1),null));
    }
}
