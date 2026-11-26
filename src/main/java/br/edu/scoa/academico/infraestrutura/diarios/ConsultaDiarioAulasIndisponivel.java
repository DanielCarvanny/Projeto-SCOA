package br.edu.scoa.academico.infraestrutura.diarios;

import br.edu.scoa.academico.aplicacao.diarios.ConsultaDiarioAulas;
import br.edu.scoa.academico.aplicacao.diarios.ResumoDiarioAulas;
import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;

/** Fail-closed até o ACA-04 fornecer o provedor real; não representa integração concluída. */
public final class ConsultaDiarioAulasIndisponivel implements ConsultaDiarioAulas {
    @Override public ResumoDiarioAulas consultarPendenciasObrigatorias(long turmaId) {
        throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.INTEGRACAO_ACA04_INDISPONIVEL);
    }
}
