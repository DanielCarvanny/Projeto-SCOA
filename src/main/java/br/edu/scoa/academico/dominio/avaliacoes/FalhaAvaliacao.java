package br.edu.scoa.academico.dominio.avaliacoes;

public final class FalhaAvaliacao extends RuntimeException {
    public enum Codigo {
        NOTA_INVALIDA,
        ENTRADA_INVALIDA,
        PRAZO_MINIMO_SETE_DIAS,
        NOTAS_PROVAS_PENDENTES,
        ALUNO_NAO_ELEGIVEL_EXAME,
        EXAME_FINAL_MULTIPLO,
        TIPO_AVALIACAO_COM_NOTAS,
        NAO_ENCONTRADA,
        NAO_AUTORIZADO,
        CONFLITO_EDICAO,
        INSCRICAO_NAO_ATIVA,
        OPERACAO_REUTILIZADA,
        PAUTA_HOMOLOGADA,
        PERSISTENCIA_INDISPONIVEL
    }

    private final Codigo codigo;

    public FalhaAvaliacao(Codigo codigo) {
        super(codigo.name());
        this.codigo = codigo;
    }

    public Codigo codigo() {
        return codigo;
    }
}
