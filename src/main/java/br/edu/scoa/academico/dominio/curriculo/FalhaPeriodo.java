package br.edu.scoa.academico.dominio.curriculo;
public final class FalhaPeriodo extends RuntimeException {
    public enum Codigo { DESCRICAO_INVALIDA,CALENDARIO_INVALIDO,MATRICULA_INVALIDA,INSCRICAO_INVALIDA,
        TRANSICAO_INVALIDA,PERIODO_ENCERRADO,CONFLITO_EDICAO,NAO_ENCONTRADO,OPERACAO_REUTILIZADA,INDISPONIVEL }
    private final Codigo codigo;
    public FalhaPeriodo(Codigo codigo){super(codigo.name());this.codigo=codigo;}
    public Codigo codigo(){return codigo;}
}
