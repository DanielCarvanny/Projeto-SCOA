package br.edu.scoa.academico.dominio.curriculo;
/** Referência fixa ao conteúdo histórico da disciplina. */
public record ComponenteCurricular(long disciplinaId, long disciplinaRevisao, TipoComponente tipo,
        int periodoIdeal, int cargaHorariaConsiderada) {
    public ComponenteCurricular {
        if(disciplinaId<=0 || disciplinaRevisao<=0 || tipo==null || periodoIdeal<=0 || cargaHorariaConsiderada<=0)
            throw new FalhaMatriz(FalhaMatriz.Codigo.COMPONENTE_INVALIDO);
    }
}

