package br.edu.scoa.academico.avaliacoes;

import br.edu.scoa.academico.dominio.avaliacoes.CalculoNotas;
import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.Nota;
import br.edu.scoa.academico.dominio.avaliacoes.SituacaoResultadoFinal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CalculoNotasTest {
    @Test void notaAceitaIntervaloDeZeroATezComUmaCasaDecimal() {
        assertEquals(new BigDecimal("0.0"), new Nota(new BigDecimal("0.0")).valor());
        assertEquals(new BigDecimal("10.0"), new Nota(new BigDecimal("10.0")).valor());
        assertEquals(new BigDecimal("7.0"), new Nota(new BigDecimal("7.00")).valor());
    }

    @Test void notaRecusaValorForaDoIntervaloOuPrecisaoMaiorQueUmaCasa() {
        assertThrows(FalhaAvaliacao.class, () -> new Nota(new BigDecimal("-0.1")));
        assertThrows(FalhaAvaliacao.class, () -> new Nota(new BigDecimal("10.1")));
        assertThrows(FalhaAvaliacao.class, () -> new Nota(new BigDecimal("7.01")));
    }

    @Test void mediaAritmeticaUsaPesosIguaisEArredondamentoExplicito() {
        var notas = List.of(Optional.of(new Nota(new BigDecimal("7.0"))),
                Optional.of(new Nota(new BigDecimal("8.0"))));

        assertEquals(new BigDecimal("7.5"), CalculoNotas.calcularMediaAritmetica(notas).orElseThrow());
    }

    @Test void mediaFicaPendenteEnquantoHouverNotaAusente() {
        var notas = List.of(Optional.of(new Nota(new BigDecimal("8.0"))), Optional.<Nota>empty());

        assertTrue(CalculoNotas.calcularMediaAritmetica(notas).isEmpty());
        assertTrue(CalculoNotas.elegivelParaExameFinal(notas).isEmpty());
    }

    @Test void elegibilidadeExigeMediaCincoNasProvas() {
        var elegivel = List.of(Optional.of(new Nota(new BigDecimal("4.0"))),
                Optional.of(new Nota(new BigDecimal("6.0"))));
        var naoElegivel = List.of(Optional.of(new Nota(new BigDecimal("4.8"))),
                Optional.of(new Nota(new BigDecimal("5.0"))));
        var limiteArredondado = List.of(Optional.of(new Nota(new BigDecimal("4.9"))),
                Optional.of(new Nota(new BigDecimal("5.0"))));

        assertEquals(Optional.of(true), CalculoNotas.elegivelParaExameFinal(elegivel));
        assertEquals(Optional.of(false), CalculoNotas.elegivelParaExameFinal(naoElegivel));
        assertEquals(Optional.of(true), CalculoNotas.elegivelParaExameFinal(limiteArredondado),
                "A média 4,95 é arredondada para 5,0 por HALF_UP.");
        assertEquals(new BigDecimal("5.0"), CalculoNotas.calcularMediaAritmetica(limiteArredondado).orElseThrow());
    }

    @Test void mediaExigeListaNaoVazia() {
        assertThrows(FalhaAvaliacao.class, () -> CalculoNotas.calcularMediaAritmetica(List.of()));
    }

    @Test void calculaMediaFinalComoMediaAritmeticaDaMediaDasProvasComExame() {
        var aprovado = CalculoNotas.calcularResultadoAposExame(new BigDecimal("6.0"), new Nota(new BigDecimal("4.0")));
        var arredondadoNaFronteira = CalculoNotas.calcularResultadoAposExame(
                new BigDecimal("5.0"), new Nota(new BigDecimal("4.9")));
        var reprovado = CalculoNotas.calcularResultadoAposExame(new BigDecimal("5.0"), new Nota(new BigDecimal("4.8")));

        assertEquals(new BigDecimal("5.0"), aprovado.mediaFinal().valor());
        assertTrue(aprovado.aprovado());
        assertEquals(new BigDecimal("5.0"), arredondadoNaFronteira.mediaFinal().valor(), "4,95 é arredondado a 5,0 em HALF_UP.");
        assertTrue(arredondadoNaFronteira.aprovado());
        assertEquals(new BigDecimal("4.9"), reprovado.mediaFinal().valor());
        assertFalse(reprovado.aprovado());
    }

    @Test void resultadoSemExameReprovaPelaMediaDasProvasQuandoAlunoNaoElegivel() {
        var resultado = CalculoNotas.calcularResultadoFinal(List.of(
                Optional.of(new Nota(new BigDecimal("4.0"))),
                Optional.of(new Nota(new BigDecimal("4.8")))), Optional.empty());

        assertEquals(SituacaoResultadoFinal.REPROVADO, resultado.situacao());
        assertEquals(new BigDecimal("4.4"), resultado.mediaProvas().orElseThrow().valor());
        assertTrue(resultado.mediaFinal().isEmpty(), "Não se cria uma fórmula de média final sem exame.");
    }

    @Test void resultadoAguardaExameParaAlunoElegivel() {
        var resultado = CalculoNotas.calcularResultadoFinal(List.of(
                Optional.of(new Nota(new BigDecimal("5.0"))),
                Optional.of(new Nota(new BigDecimal("5.0")))), Optional.empty());

        assertEquals(SituacaoResultadoFinal.PENDENTE_EXAME, resultado.situacao());
        assertTrue(resultado.mediaFinal().isEmpty());
    }

    @Test void resultadoAguardaNotasDeProvaSeHouverAlgumaAusente() {
        var resultado = CalculoNotas.calcularResultadoFinal(List.of(
                Optional.of(new Nota(new BigDecimal("7.0"))), Optional.empty()), Optional.empty());

        assertEquals(SituacaoResultadoFinal.PENDENTE_PROVAS, resultado.situacao());
        assertTrue(resultado.mediaProvas().isEmpty());
    }
}
