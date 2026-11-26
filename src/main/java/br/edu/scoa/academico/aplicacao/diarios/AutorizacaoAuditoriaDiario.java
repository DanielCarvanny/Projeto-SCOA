package br.edu.scoa.academico.aplicacao.diarios;

public interface AutorizacaoAuditoriaDiario<S> {
    long usuarioId(S sessao);
    boolean ehCoordenador(S sessao);
}
