package br.edu.scoa.almoxarifado.aplicacao;

import br.edu.scoa.almoxarifado.dominio.Localizacao;
import br.edu.scoa.almoxarifado.dominio.Produto;

import java.util.List;

public final class GestaoAlmoxarifado {
    private final RepositorioAlmoxarifado repositorio;

    public GestaoAlmoxarifado(RepositorioAlmoxarifado repositorio) {
        this.repositorio = java.util.Objects.requireNonNull(repositorio);
    }

    public Localizacao cadastrarLocalizacao(String nome, String descricao) {
        return repositorio.salvar(Localizacao.nova(nome, descricao));
    }

    public List<Localizacao> consultarLocalizacoes() { return repositorio.listarLocalizacoes(); }

    public Localizacao consultarLocalizacao(long id) {
        return repositorio.consultarLocalizacao(id)
                .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada."));
    }

    public Produto cadastrarProduto(String nome, br.edu.scoa.almoxarifado.dominio.TipoProduto tipo,
                                    String unidade, String codigoBarras, long localizacaoId,
                                    int estoqueMinimo, int estoqueMaximo) {
        var localizacao = consultarLocalizacao(localizacaoId);
        if (!localizacao.ativa()) throw new IllegalArgumentException("Localização inativa.");
        return repositorio.salvar(Produto.novo(nome, tipo, unidade, codigoBarras,
                localizacaoId, estoqueMinimo, estoqueMaximo));
    }

    public List<Produto> consultarProdutos() { return repositorio.listarProdutos(); }

    public Produto consultarProduto(long id) {
        return repositorio.consultarProduto(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }
}
