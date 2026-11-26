package br.edu.scoa.academico.aplicacao.diarios;

/** Contrato do ACA-04. O provedor deve incrementar versaoDiario atomicamente em toda alteração relevante. */
public interface ConsultaDiarioAulas {
    ResumoDiarioAulas consultarPendenciasObrigatorias(long turmaId);
}
