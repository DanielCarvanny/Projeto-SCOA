package br.edu.scoa.academico.dominio.curriculo;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PreRequisitosTest {
    final Set<Long> disciplinas=Set.of(1L,2L,3L,4L);
    @Test void aceitaCadeiaEMultiplasExigencias(){
        assertDoesNotThrow(()->ValidadorPreRequisitos.validar(disciplinas,List.of(new PreRequisito(3,2),new PreRequisito(3,1),new PreRequisito(2,1))));
    }
    @Test void recusaAutoReferencia(){assertThrows(FalhaPreRequisito.class,()->new PreRequisito(1,1));}
    @Test void recusaDuplicidade(){
        assertEquals(FalhaPreRequisito.Codigo.DUPLICADO,assertThrows(FalhaPreRequisito.class,()->
            ValidadorPreRequisitos.validar(disciplinas,List.of(new PreRequisito(2,1),new PreRequisito(2,1)))).codigo());
    }
    @Test void recusaDisciplinaExterna(){
        assertEquals(FalhaPreRequisito.Codigo.FORA_DA_MATRIZ,assertThrows(FalhaPreRequisito.class,()->
            ValidadorPreRequisitos.validar(disciplinas,List.of(new PreRequisito(2,99)))).codigo());
    }
    @Test void recusaCicloDiretoEIndireto(){
        for(var relacoes:List.of(List.of(new PreRequisito(1,2),new PreRequisito(2,1)),
                List.of(new PreRequisito(1,2),new PreRequisito(2,3),new PreRequisito(3,1))))
            assertEquals(FalhaPreRequisito.Codigo.CICLO,assertThrows(FalhaPreRequisito.class,()->ValidadorPreRequisitos.validar(disciplinas,relacoes)).codigo());
    }
    @Test void matrizesIndependentesNaoCompartilhamGrafo(){
        ValidadorPreRequisitos.validar(disciplinas,List.of(new PreRequisito(1,2)));
        assertDoesNotThrow(()->ValidadorPreRequisitos.validar(disciplinas,List.of(new PreRequisito(2,1))));
    }
}
