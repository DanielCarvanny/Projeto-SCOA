package br.edu.scoa.academico.dominio.curriculo;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatrizCurricularTest {
    static ComponenteCurricular componente(long id) {
        return new ComponenteCurricular(id, 1, TipoComponente.OBRIGATORIO, 1, 80);
    }
    static MatrizCurricular matriz(List<ComponenteCurricular> componentes) {
        return new MatrizCurricular(0, 1, " ads ", "2027.1", LocalDate.of(2027,1,1), null,
                SituacaoMatriz.EM_ELABORACAO, 0, componentes);
    }
    @Test void naoPermiteMatrizVaziaMesmoEmElaboracao() {
        assertEquals(FalhaMatriz.Codigo.MATRIZ_VAZIA,
            assertThrows(FalhaMatriz.class, () -> matriz(List.of())).codigo());
    }
    @Test void validaComponentesDuplicados() {
        assertEquals(FalhaMatriz.Codigo.DISCIPLINA_REPETIDA,
            assertThrows(FalhaMatriz.class, () -> matriz(List.of(componente(1), componente(1)))).codigo());
    }
    @Test void naoRemoveUltimoComponente() {
        assertThrows(FalhaMatriz.class, () -> matriz(List.of(componente(1))).remover(1));
    }
    @Test void listaEhImutavelECodigoNormalizado() {
        var entrada = new ArrayList<>(List.of(componente(1)));
        var matriz = matriz(entrada); entrada.clear();
        assertEquals("ADS", matriz.codigo()); assertEquals(1, matriz.componentes().size());
        assertThrows(UnsupportedOperationException.class, () -> matriz.componentes().clear());
    }
    @Test void validaDatasEValoresPositivos() {
        assertThrows(FalhaMatriz.class, () -> new ComponenteCurricular(1,1,TipoComponente.ELETIVO,0,80));
        assertThrows(FalhaMatriz.class, () -> new ComponenteCurricular(1,0,TipoComponente.ELETIVO,1,80));
        assertThrows(FalhaMatriz.class, () -> new ComponenteCurricular(1,1,null,1,80));
        assertThrows(FalhaMatriz.class, () -> new MatrizCurricular(0,1,"M","1",LocalDate.of(2027,2,1),
            LocalDate.of(2027,1,1),SituacaoMatriz.EM_ELABORACAO,0,List.of(componente(1))));
    }
    @Test void preservaRevisaoDisciplinaESomaApenasObrigatorias() {
        var matriz = matriz(List.of(componente(1),new ComponenteCurricular(2,3,TipoComponente.ELETIVO,2,40)));
        assertEquals(80, matriz.cargaObrigatoria());
        assertEquals(3, matriz.componentes().getLast().disciplinaRevisao());
        assertEquals(1, matriz.remover(2).componentes().size());
    }
    @Test void publicadaEEncerradaNaoSaoEditaveis() {
        for (var situacao : List.of(SituacaoMatriz.VIGENTE,SituacaoMatriz.ENCERRADA)) {
            var matriz = new MatrizCurricular(1,1,"M","1",LocalDate.of(2027,1,1),null,situacao,1,List.of(componente(1),componente(2)));
            assertEquals(FalhaMatriz.Codigo.MATRIZ_NAO_EDITAVEL,
                assertThrows(FalhaMatriz.class, () -> matriz.remover(1)).codigo());
        }
    }
}
