package br.edu.scoa.academico.dominio.diarios;

public final class FalhaAuditoriaDiario extends RuntimeException {
    public enum Codigo {
        ENTRADA_INVALIDA,
        NAO_AUTORIZADO,
        TURMA_NAO_ENCONTRADA,
        PAUTA_HOMOLOGADA,
        PAUTA_NAO_ANALISADA,
        PENDENCIAS_PARA_HOMOLOGAR,
        CONFLITO_REVISAO,
        CONFLITO_DIARIO,
        OPERACAO_REUTILIZADA,
        INTEGRACAO_ACA04_INDISPONIVEL,
        PERSISTENCIA_INDISPONIVEL
    }

    private final Codigo codigo;

    public FalhaAuditoriaDiario(Codigo codigo) {
        super(codigo.name());
        this.codigo = codigo;
    }

    public Codigo codigo() { return codigo; }
}
