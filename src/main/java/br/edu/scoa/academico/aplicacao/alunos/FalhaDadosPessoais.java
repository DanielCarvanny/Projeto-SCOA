package br.edu.scoa.academico.aplicacao.alunos;

/** Códigos locais propostos para falhas na fronteira institucional. */
public final class FalhaDadosPessoais extends IllegalStateException {
    public enum Codigo { CONFLITO_EDICAO, RESPOSTA_INCONSISTENTE }
    private final Codigo codigo;
    public FalhaDadosPessoais(Codigo codigo) { super(codigo.name()); this.codigo = codigo; }
    public Codigo codigo() { return codigo; }
}
