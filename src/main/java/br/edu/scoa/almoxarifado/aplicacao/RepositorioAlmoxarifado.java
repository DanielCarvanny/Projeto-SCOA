package br.edu.scoa.almoxarifado.aplicacao;

import br.edu.scoa.almoxarifado.dominio.Localizacao;
import br.edu.scoa.almoxarifado.dominio.Produto;

import java.util.List;
import java.util.Optional;

public interface RepositorioAlmoxarifado {
    Localizacao salvar(Localizacao localizacao);
    List<Localizacao> listarLocalizacoes();
    Optional<Localizacao> consultarLocalizacao(long id);
    Produto salvar(Produto produto);
    List<Produto> listarProdutos();
    Optional<Produto> consultarProduto(long id);
}
