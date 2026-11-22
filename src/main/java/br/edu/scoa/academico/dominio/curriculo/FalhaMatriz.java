package br.edu.scoa.academico.dominio.curriculo;
public final class FalhaMatriz extends RuntimeException {
    public enum Codigo { MATRIZ_VAZIA, DISCIPLINA_REPETIDA, COMPONENTE_INVALIDO, IDENTIFICACAO_INVALIDA, DATAS_INVALIDAS,
        MATRIZ_NAO_EDITAVEL, MATRIZ_DUPLICADA, CONFLITO_EDICAO, REFERENCIA_INEXISTENTE, CURSO_IMUTAVEL, NAO_ENCONTRADA, INDISPONIVEL }
    private final Codigo codigo;
    public FalhaMatriz(Codigo codigo) { super(codigo.name()); this.codigo=codigo; }
    public Codigo codigo() { return codigo; }
}

