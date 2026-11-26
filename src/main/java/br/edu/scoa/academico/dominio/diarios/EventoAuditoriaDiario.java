package br.edu.scoa.academico.dominio.diarios;

import java.time.Instant;
import java.util.UUID;

public record EventoAuditoriaDiario(String categoria, long entidadeId, Long inscricaoId, String evento,
                                   String detalhes, long autorId, Instant registradoEm, String origem,
                                   UUID operacaoId) {
    public EventoAuditoriaDiario {
        categoria = categoria == null ? "" : categoria.strip();
        evento = evento == null ? "" : evento.strip();
        detalhes = detalhes == null ? "" : detalhes.strip();
        origem = origem == null ? "" : origem.strip();
        if (categoria.isBlank() || entidadeId <= 0 || (inscricaoId != null && inscricaoId <= 0)
                || evento.isBlank() || autorId <= 0 || registradoEm == null || origem.isBlank() || operacaoId == null) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
    }
}
