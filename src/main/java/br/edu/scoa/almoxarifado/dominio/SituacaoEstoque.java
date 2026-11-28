package br.edu.scoa.almoxarifado.dominio;

public record SituacaoEstoque(long produtoId, int saldo, int estoqueMinimo, int estoqueMaximo,
                             AlertaEstoque alerta) {
    public SituacaoEstoque {
        if (produtoId <= 0 || saldo < 0 || estoqueMinimo < 0 || estoqueMaximo < estoqueMinimo || alerta == null)
            throw new IllegalArgumentException("Situação de estoque inválida.");
    }

    public static AlertaEstoque calcularAlerta(int saldo, int minimo, int maximo) {
        if (saldo < 0 || minimo < 0 || maximo < minimo) throw new IllegalArgumentException("Limites de estoque inválidos.");
        if (saldo < minimo) return AlertaEstoque.ABAIXO_MINIMO;
        if (saldo > maximo) return AlertaEstoque.ACIMA_MAXIMO;
        return AlertaEstoque.NORMAL;
    }
}
