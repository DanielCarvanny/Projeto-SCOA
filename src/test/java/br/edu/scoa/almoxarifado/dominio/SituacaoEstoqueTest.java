package br.edu.scoa.almoxarifado.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoEstoqueTest {
    @Test void alertaObedeceAosLimites() {
        assertEquals(AlertaEstoque.ABAIXO_MINIMO, SituacaoEstoque.calcularAlerta(2, 3, 8));
        assertEquals(AlertaEstoque.NORMAL, SituacaoEstoque.calcularAlerta(3, 3, 8));
        assertEquals(AlertaEstoque.NORMAL, SituacaoEstoque.calcularAlerta(8, 3, 8));
        assertEquals(AlertaEstoque.ACIMA_MAXIMO, SituacaoEstoque.calcularAlerta(9, 3, 8));
    }
}
