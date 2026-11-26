package br.edu.scoa.academico.dominio.diarios;

public record PendenciaPauta(CodigoPendenciaPauta codigo, String descricao, Long inscricaoId) {
    public PendenciaPauta {
        descricao = descricao == null ? "" : descricao.strip();
        if (codigo == null || descricao.isBlank() || (inscricaoId != null && inscricaoId <= 0)) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
    }

    public PendenciaPauta(CodigoPendenciaPauta codigo, String descricao) {
        this(codigo, descricao, null);
    }
}
