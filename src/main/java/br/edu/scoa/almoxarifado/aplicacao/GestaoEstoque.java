package br.edu.scoa.almoxarifado.aplicacao;

import br.edu.scoa.almoxarifado.dominio.MovimentacaoEstoque;
import br.edu.scoa.almoxarifado.dominio.SituacaoEstoque;
import br.edu.scoa.almoxarifado.dominio.TipoMovimentacao;

import java.time.Clock;
import java.util.Objects;

public final class GestaoEstoque {
    private final IdentidadeParaMovimentacao identidade;
    private final RepositorioMovimentacoesEstoque repositorio;
    private final Clock relogio;

    public GestaoEstoque(IdentidadeParaMovimentacao identidade, RepositorioMovimentacoesEstoque repositorio, Clock relogio) {
        this.identidade = Objects.requireNonNull(identidade);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public MovimentacaoEstoque entrada(Object sessao, long produtoId, int quantidade, String origem, String operacaoId) {
        return registrar(sessao, produtoId, TipoMovimentacao.ENTRADA, quantidade, origem, operacaoId);
    }

    public MovimentacaoEstoque saida(Object sessao, long produtoId, int quantidade, String origem, String operacaoId) {
        return registrar(sessao, produtoId, TipoMovimentacao.SAIDA, quantidade, origem, operacaoId);
    }

    private MovimentacaoEstoque registrar(Object sessao, long produtoId, TipoMovimentacao tipo, int quantidade,
                                           String origem, String operacaoId) {
        if (produtoId <= 0 || quantidade <= 0) throw new IllegalArgumentException("Produto e quantidade devem ser positivos.");
        var autor = identidade.exigirCoordenadorAtivo(sessao);
        return repositorio.movimentar(produtoId, tipo, quantidade, autor, relogio.instant(), origem, operacaoId);
    }

    public SituacaoEstoque consultarSituacao(long produtoId) { return repositorio.consultarSituacao(produtoId); }
}
