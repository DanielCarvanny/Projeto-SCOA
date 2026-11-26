package br.edu.scoa.academico.aplicacao.boletins;

public final class FalhaBoletimAluno extends RuntimeException {
    public enum Codigo {
        NAO_AUTORIZADO,
        ALUNO_NAO_ENCONTRADO,
        CURSO_NAO_ENCONTRADO,
        MATRIZ_NAO_ENCONTRADA,
        DISCIPLINA_NAO_ENCONTRADA,
        TURMA_NAO_ENCONTRADA,
        PERIODO_NAO_ENCONTRADO,
        SALA_NAO_ENCONTRADA,
        DADOS_ACADEMICOS_INCONSISTENTES,
        CONSULTA_INDISPONIVEL
    }

    private final Codigo codigo;

    public FalhaBoletimAluno(Codigo codigo) {
        super(codigo.name());
        this.codigo = codigo;
    }

    public Codigo codigo() { return codigo; }
}
