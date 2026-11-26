package br.edu.scoa.academico.dominio.avaliacoes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.NOTA_INVALIDA;

public record Nota(BigDecimal valor) {
    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;

    public Nota {
        if (valor == null || valor.compareTo(NOTA_MINIMA) < 0 || valor.compareTo(NOTA_MAXIMA) > 0) {
            throw new FalhaAvaliacao(NOTA_INVALIDA);
        }
        try {
            valor = valor.setScale(1, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException erro) {
            throw new FalhaAvaliacao(NOTA_INVALIDA);
        }
    }
}
