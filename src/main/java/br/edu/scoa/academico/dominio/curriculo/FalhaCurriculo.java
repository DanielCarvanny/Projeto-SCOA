package br.edu.scoa.academico.dominio.curriculo;

public final class FalhaCurriculo extends RuntimeException {
    public enum Codigo {
        CODIGO_INVALIDO, NOME_INVALIDO, EMENTA_INVALIDA, CARGA_HORARIA_INVALIDA,
        CODIGO_DUPLICADO, NAO_ENCONTRADO, CONFLITO_EDICAO, INDISPONIVEL
    }
    private final Codigo codigo;
    public FalhaCurriculo(Codigo codigo) { super(codigo.name()); this.codigo = codigo; }
    public Codigo codigo() { return codigo; }
}

