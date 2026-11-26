package br.edu.scoa.academico.dominio.avaliacoes;

import java.time.LocalDate;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.ENTRADA_INVALIDA;

public record AvaliacaoAcademica(long id, long turmaId, String titulo, String descricao,
                                LocalDate dataAplicacao, TipoAvaliacao tipo, long revisao) {
    public AvaliacaoAcademica {
        titulo = titulo == null ? "" : titulo.strip();
        descricao = descricao == null ? "" : descricao.strip();
        if (id < 0 || turmaId <= 0 || titulo.isBlank() || titulo.length() > 120
                || descricao.length() > 1000 || dataAplicacao == null || tipo == null
                || revisao < 0 || (id == 0) != (revisao == 0)) {
            throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        }
    }

    public AvaliacaoAcademica identificada(long novoId, long novaRevisao) {
        return new AvaliacaoAcademica(novoId, turmaId, titulo, descricao, dataAplicacao, tipo, novaRevisao);
    }
}
