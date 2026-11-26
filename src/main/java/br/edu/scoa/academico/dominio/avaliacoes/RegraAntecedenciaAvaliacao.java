package br.edu.scoa.academico.dominio.avaliacoes;

import java.time.LocalDate;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.PRAZO_MINIMO_SETE_DIAS;

public final class RegraAntecedenciaAvaliacao {
    public static final int DIAS_CORRIDOS = 7;

    private RegraAntecedenciaAvaliacao() { }

    public static void validar(LocalDate dataAplicacao, LocalDate dataReferencia) {
        if (dataAplicacao == null || dataReferencia == null
                || dataAplicacao.isBefore(dataReferencia.plusDays(DIAS_CORRIDOS))) {
            throw new FalhaAvaliacao(PRAZO_MINIMO_SETE_DIAS);
        }
    }
}
