package br.edu.scoa.academico.dominio.curriculo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoTest {
    @Test void cursoNormalizaCodigoEPreservaNome() {
        var curso = new Curso(" ads ", " Análise de Sistemas ", 2400);
        assertEquals("ADS", curso.codigo());
        assertEquals("Análise de Sistemas", curso.nome());
        assertEquals(2400, curso.cargaHorariaTotal());
    }
    @Test void disciplinaExigeEmenta() {
        var erro = assertThrows(FalhaCurriculo.class, () -> new Disciplina("POO", "Objetos", " ", 80));
        assertEquals(FalhaCurriculo.Codigo.EMENTA_INVALIDA, erro.codigo());
    }
    @Test void cargaPrecisaSerPositivaNosDoisCatalogos() {
        assertEquals(FalhaCurriculo.Codigo.CARGA_HORARIA_INVALIDA,
            assertThrows(FalhaCurriculo.class, () -> new Curso("ADS", "Sistemas", 0)).codigo());
        assertThrows(FalhaCurriculo.class, () -> new Disciplina("POO", "Objetos", "Classes", -1));
    }
    @Test void validaCamposSemMisturarMensagens() {
        assertEquals(FalhaCurriculo.Codigo.CODIGO_INVALIDO,
            assertThrows(FalhaCurriculo.class, () -> new Curso(" ", "Nome", 80)).codigo());
        assertEquals(FalhaCurriculo.Codigo.NOME_INVALIDO,
            assertThrows(FalhaCurriculo.class, () -> new Curso("A", " ", 80)).codigo());
        assertThrows(FalhaCurriculo.class, () -> new Curso("X".repeat(41), "Nome", 80));
        assertThrows(FalhaCurriculo.class, () -> new Curso("A", "X".repeat(201), 80));
    }
    @Test void respeitaLimitesDoModeloFisico() {
        assertDoesNotThrow(() -> new Curso("X".repeat(40), "N".repeat(200), 1));
        assertDoesNotThrow(() -> new Disciplina("D", "Nome", "x".repeat(65535), 1));
        assertEquals(FalhaCurriculo.Codigo.EMENTA_INVALIDA,
            assertThrows(FalhaCurriculo.class, () -> new Disciplina("D", "Nome", "á".repeat(32768), 1)).codigo());
    }
}
