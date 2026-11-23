package br.edu.scoa.academico.dominio.turmas;

public final class FalhaTurma extends RuntimeException {
    public enum Codigo { SALA_INVALIDA, SALA_INDISPONIVEL, CAPACIDADE_INSUFICIENTE, TURMA_INVALIDA, HORARIO_INVALIDO,
        CONFLITO_PROFESSOR, CONFLITO_SALA, PERIODO_NAO_EDITAVEL, NAO_ENCONTRADO, CONFLITO_EDICAO, OPERACAO_REUTILIZADA, INDISPONIVEL }
    private final Codigo codigo;
    public FalhaTurma(Codigo codigo) { super(codigo.name()); this.codigo=codigo; }
    public Codigo codigo(){ return codigo; }
}
