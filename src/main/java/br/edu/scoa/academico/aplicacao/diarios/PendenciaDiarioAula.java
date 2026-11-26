package br.edu.scoa.academico.aplicacao.diarios;

import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;

public record PendenciaDiarioAula(String codigo, String descricao) {
    public PendenciaDiarioAula {
        codigo = codigo == null ? "" : codigo.strip();
        descricao = descricao == null ? "" : descricao.strip();
        if (codigo.isBlank() || descricao.isBlank()) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
    }
}
