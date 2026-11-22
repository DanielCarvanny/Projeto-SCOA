package br.edu.scoa.academico.aplicacao.curriculo;
/** Retrato imutável de uma revisão; versao é técnica, não versão de matriz curricular. */
public record ItemCatalogo(long id, TipoCatalogo tipo, String codigo, String nome,
        String ementa, int cargaHoraria, boolean estaAtivo, long versao) {
    @Override public String toString() { return codigo + " — " + nome + (estaAtivo ? "" : " (inativo)"); }
}

