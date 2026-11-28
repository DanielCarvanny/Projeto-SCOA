package br.edu.scoa.almoxarifado.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {
    @Test void aceitaProdutoValidoSemCodigoDeBarras() {
        var produto = Produto.novo("Papel A4", TipoProduto.MATERIAL_CONSUMO, "un", null, 1, 0, 100);
        assertNull(produto.codigoBarras());
        assertEquals("UN", produto.unidade());
    }

    @Test void normalizaCodigoParaMaiusculasDepoisDeRemoverEspacos() {
        var minusculo = Produto.novo("Produto A", TipoProduto.MATERIAL_CONSUMO, "UN", " abc1 ", 1, 0, 1);
        var maiusculo = Produto.novo("Produto B", TipoProduto.MATERIAL_CONSUMO, "UN", "ABC1", 1, 0, 1);
        assertEquals("ABC1", minusculo.codigoBarras());
        assertEquals(minusculo.codigoBarras(), maiusculo.codigoBarras());
    }

    @Test void rejeitaTipoUnidadeCodigoLocalEIntervaloInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", null, "UN", null, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "", null, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "UN", "código!", 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "UN", "X".repeat(65), 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "UN", null, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "UN", null, 1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> Produto.novo("Item", TipoProduto.MATERIAL_CONSUMO, "UN", null, 1, 2, 1));
    }

    @Test void localizacaoValidaEObrigatoria() {
        assertEquals("Depósito", Localizacao.nova(" Depósito ", "").nome());
        assertThrows(IllegalArgumentException.class, () -> Localizacao.nova(" ", ""));
    }
}
