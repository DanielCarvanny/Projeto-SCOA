package br.edu.scoa.almoxarifado.aplicacao;

import br.edu.scoa.almoxarifado.dominio.MovimentacaoEstoque;
import br.edu.scoa.almoxarifado.dominio.SituacaoEstoque;
import br.edu.scoa.almoxarifado.dominio.TipoMovimentacao;

import java.time.Instant;

public interface RepositorioMovimentacoesEstoque {
    MovimentacaoEstoque movimentar(long produtoId, TipoMovimentacao tipo, int quantidade,
                                   long usuarioAutorId, Instant instanteUtc, String origem, String operacaoId);
    SituacaoEstoque consultarSituacao(long produtoId);
}
