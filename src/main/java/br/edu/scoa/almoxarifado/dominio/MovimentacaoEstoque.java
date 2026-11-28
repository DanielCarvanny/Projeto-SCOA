package br.edu.scoa.almoxarifado.dominio;

import java.time.Instant;

public record MovimentacaoEstoque(long id, long produtoId, TipoMovimentacao tipo, int quantidade,
                                  long usuarioAutorId, Instant instanteUtc, String origem,
                                  String operacaoId, int saldoResultante) {
    public MovimentacaoEstoque {
        origem = origem == null ? null : origem.trim();
        operacaoId = operacaoId == null ? null : operacaoId.trim();
        if (id < 0 || produtoId <= 0 || tipo == null || quantidade <= 0 || usuarioAutorId <= 0
                || instanteUtc == null || origem == null || origem.isBlank() || origem.length() > 120
                || operacaoId == null || operacaoId.isBlank() || operacaoId.length() > 100
                || saldoResultante < 0) throw new IllegalArgumentException("Movimentação inválida.");
    }
}
