package br.edu.scoa.academico.dominio.curriculo;
/** A disciplina a cursar exige aprovação prévia na disciplina exigida. */
public record PreRequisito(long disciplinaId,long disciplinaExigidaId) {
    public PreRequisito {
        if(disciplinaId<=0||disciplinaExigidaId<=0)throw new FalhaPreRequisito(FalhaPreRequisito.Codigo.FORA_DA_MATRIZ);
        if(disciplinaId==disciplinaExigidaId)throw new FalhaPreRequisito(FalhaPreRequisito.Codigo.AUTO_REFERENCIA);
    }
}

