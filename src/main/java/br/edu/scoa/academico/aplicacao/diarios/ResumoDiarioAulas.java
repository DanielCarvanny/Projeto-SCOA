package br.edu.scoa.academico.aplicacao.diarios;

import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;

import java.util.List;

public record ResumoDiarioAulas(long turmaId, long versaoDiario, List<PendenciaDiarioAula> pendenciasObrigatorias) {
    public ResumoDiarioAulas {
        if (turmaId <= 0 || versaoDiario < 0 || pendenciasObrigatorias == null) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
        pendenciasObrigatorias = List.copyOf(pendenciasObrigatorias);
    }
}
