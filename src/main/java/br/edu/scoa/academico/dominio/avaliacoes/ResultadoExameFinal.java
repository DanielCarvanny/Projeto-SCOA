package br.edu.scoa.academico.dominio.avaliacoes;

public record ResultadoExameFinal(Nota mediaFinal, boolean aprovado) {
    public ResultadoExameFinal {
        if (mediaFinal == null) throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
    }
}
