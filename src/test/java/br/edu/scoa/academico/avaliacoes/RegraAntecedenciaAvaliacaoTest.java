package br.edu.scoa.academico.avaliacoes;

import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.RegraAntecedenciaAvaliacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegraAntecedenciaAvaliacaoTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 24);

    @Test void permiteDataComExatamenteSeteDiasCorridosDeAntecedencia() {
        assertDoesNotThrow(() -> RegraAntecedenciaAvaliacao.validar(HOJE.plusDays(7), HOJE));
        assertDoesNotThrow(() -> RegraAntecedenciaAvaliacao.validar(HOJE.plusDays(8), HOJE));
    }

    @Test void rejeitaDataComMenosDeSeteDiasCorridos() {
        var falha = assertThrows(FalhaAvaliacao.class,
                () -> RegraAntecedenciaAvaliacao.validar(HOJE.plusDays(6), HOJE));
        assertEquals(FalhaAvaliacao.Codigo.PRAZO_MINIMO_SETE_DIAS, falha.codigo());
    }

    @Test void rejeitaDatasNulas() {
        assertThrows(FalhaAvaliacao.class, () -> RegraAntecedenciaAvaliacao.validar(null, HOJE));
        assertThrows(FalhaAvaliacao.class, () -> RegraAntecedenciaAvaliacao.validar(HOJE, null));
    }
}
