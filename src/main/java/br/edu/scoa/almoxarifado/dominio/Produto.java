package br.edu.scoa.almoxarifado.dominio;

import java.util.Locale;

public record Produto(long id, String nome, TipoProduto tipo, String unidade,
                      String codigoBarras, long localizacaoId, int estoqueMinimo,
                      int estoqueMaximo, boolean ativo) {
    public Produto {
        if (id < 0 || nome == null || nome.isBlank() || nome.trim().length() > 160
                || tipo == null || unidade == null || !unidade.matches("[A-Za-z]{1,12}")
                || localizacaoId <= 0 || estoqueMinimo < 0 || estoqueMaximo < 0
                || estoqueMaximo < estoqueMinimo) {
            throw new IllegalArgumentException("Dados do produto inválidos.");
        }
        nome = nome.trim();
        unidade = unidade.trim().toUpperCase(Locale.ROOT);
        if (codigoBarras != null) {
            codigoBarras = codigoBarras.trim().toUpperCase(Locale.ROOT);
            if (!codigoBarras.matches("[A-Za-z0-9]{1,64}")) {
                throw new IllegalArgumentException("Código de barras deve ter de 1 a 64 caracteres alfanuméricos.");
            }
        }
    }

    public static Produto novo(String nome, TipoProduto tipo, String unidade, String codigoBarras,
                               long localizacaoId, int estoqueMinimo, int estoqueMaximo) {
        return new Produto(0, nome, tipo, unidade, codigoBarras, localizacaoId,
                estoqueMinimo, estoqueMaximo, true);
    }
}
