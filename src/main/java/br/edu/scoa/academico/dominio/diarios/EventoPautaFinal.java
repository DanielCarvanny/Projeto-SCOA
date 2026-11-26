package br.edu.scoa.academico.dominio.diarios;

import java.time.Instant;
import java.util.UUID;

public record EventoPautaFinal(long id, long turmaId, TipoEventoPauta evento, EstadoPautaFinal estadoAnterior,
                               EstadoPautaFinal estadoNovo, String observacao, long autorId,
                               Instant registradoEm, UUID operacaoId) {
    public EventoPautaFinal {
        observacao = observacao == null ? "" : observacao.strip();
        if (id < 0 || turmaId <= 0 || evento == null || estadoNovo == null || autorId <= 0
                || registradoEm == null || operacaoId == null || observacao.length() > 1000) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
    }
}
