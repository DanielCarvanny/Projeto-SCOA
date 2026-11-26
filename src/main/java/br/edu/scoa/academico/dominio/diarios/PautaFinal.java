package br.edu.scoa.academico.dominio.diarios;

import java.time.Instant;

public record PautaFinal(long turmaId, EstadoPautaFinal estado, long revisao, long versaoDiario,
                         Long homologadaPor, Instant homologadaEm) {
    public PautaFinal {
        boolean possuiDadosHomologacao = homologadaPor != null || homologadaEm != null;
        if (turmaId <= 0 || estado == null || revisao < 0 || versaoDiario < 0
                || (estado == EstadoPautaFinal.HOMOLOGADA && (homologadaPor == null || homologadaEm == null))
                || (estado == EstadoPautaFinal.ABERTA && possuiDadosHomologacao)
                || (homologadaPor != null && homologadaPor <= 0)) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
    }

    public static PautaFinal aberta(long turmaId, long versaoDiario) {
        return new PautaFinal(turmaId, EstadoPautaFinal.ABERTA, 0, versaoDiario, null, null);
    }
}
