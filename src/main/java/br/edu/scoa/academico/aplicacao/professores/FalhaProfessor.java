package br.edu.scoa.academico.aplicacao.professores;
public final class FalhaProfessor extends RuntimeException {
    public enum Codigo { CPF_INVALIDO, TITULACAO_INVALIDA, PESSOA_JA_CADASTRADA, ENTRADA_INVALIDA, NAO_AUTORIZADO, PESSOA_NAO_ENCONTRADA, PROFESSOR_NAO_ENCONTRADO, PROFESSOR_JA_CADASTRADO, CONFLITO_EDICAO, RESPOSTA_INCONSISTENTE }
    private final Codigo codigo;
    public FalhaProfessor(Codigo codigo) { super(codigo.name()); this.codigo = codigo; }
    public Codigo codigo() { return codigo; }
}
