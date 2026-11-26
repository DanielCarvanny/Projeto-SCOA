package br.edu.scoa.academico.dominio.avaliacoes;

import java.util.Optional;

public record ResultadoFinalAcademico(Optional<Nota> mediaProvas, Optional<Nota> notaExame,
                                      Optional<Nota> mediaFinal, SituacaoResultadoFinal situacao) {
    public ResultadoFinalAcademico {
        if (mediaProvas == null || notaExame == null || mediaFinal == null || situacao == null) {
            throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        }
        if (situacao == SituacaoResultadoFinal.PENDENTE_PROVAS && mediaProvas.isPresent()) {
            throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        }
        if (situacao == SituacaoResultadoFinal.PENDENTE_EXAME
                && (mediaProvas.isEmpty() || mediaFinal.isPresent())) {
            throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        }
        if ((situacao == SituacaoResultadoFinal.APROVADO || situacao == SituacaoResultadoFinal.REPROVADO)
                && mediaProvas.isEmpty()) {
            throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        }
    }
}
