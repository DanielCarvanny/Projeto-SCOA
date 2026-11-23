package br.edu.scoa.academico.dominio.turmas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SalaTest {

    @Test
    void salaValidaEhCriadaComSucesso() {
        var sala = new Sala(0, "A-101", "Bloco Didático 1", 45, "Projetor, Ar Condicionado", SituacaoSala.DISPONIVEL, 0);
        assertEquals("A-101", sala.identificacao());
        assertEquals("Bloco Didático 1", sala.localizacao());
        assertEquals(45, sala.capacidade());
        assertEquals("Projetor, Ar Condicionado", sala.recursos());
        assertEquals(SituacaoSala.DISPONIVEL, sala.situacao());
    }

    @Test
    void identificacaoVaziaOuNulaEhRecusada() {
        assertThrows(FalhaTurma.class, () -> new Sala(0, "", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, "   ", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, null, "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0));
    }

    @Test
    void localizacaoVaziaOuNulaEhRecusada() {
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "", 30, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "   ", 30, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", null, 30, "", SituacaoSala.DISPONIVEL, 0));
    }

    @Test
    void capacidadeMenorOuIgualAZeroEhRecusada() {
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "Bloco A", 0, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "Bloco A", -5, "", SituacaoSala.DISPONIVEL, 0));
    }

    @Test
    void situacaoNulaEhRecusada() {
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "Bloco A", 30, "", null, 0));
    }

    @Test
    void recursosNulosSaoTratadosComoStringVazia() {
        var sala = new Sala(0, "LAB-1", "Bloco A", 30, null, SituacaoSala.DISPONIVEL, 0);
        assertEquals("", sala.recursos());
    }

    @Test
    void revisaoSemIdOuIdSemRevisaoEhRecusado() {
        assertThrows(FalhaTurma.class, () -> new Sala(1, "LAB-1", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0));
        assertThrows(FalhaTurma.class, () -> new Sala(0, "LAB-1", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 1));
    }

    @Test
    void identificadorAtualizaIdERevisao() {
        var sala = new Sala(0, "LAB-1", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0);
        var salva = sala.identificado(10, 1);
        assertEquals(10, salva.id());
        assertEquals(1, salva.revisao());
        assertEquals("LAB-1", salva.identificacao());
    }
}
