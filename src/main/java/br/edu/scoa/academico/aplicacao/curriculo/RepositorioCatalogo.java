package br.edu.scoa.academico.aplicacao.curriculo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface RepositorioCatalogo {
    /** Cadastro usa id/versao zero; edição usa revisão esperada. Escrita e histórico são atômicos. */
    ItemCatalogo salvar(ItemCatalogo item, long autorId, Instant instante);
    List<ItemCatalogo> pesquisar(TipoCatalogo tipo, String termo);
    Optional<ItemCatalogo> consultar(TipoCatalogo tipo, long id);
    List<RevisaoCatalogo> historico(TipoCatalogo tipo, long id);
}

