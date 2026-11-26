package br.edu.scoa.academico.dominio.avaliacoes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.ENTRADA_INVALIDA;

public final class CalculoNotas {
    private static final BigDecimal MEDIA_MINIMA_EXAME_FINAL = new BigDecimal("5.0");
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private CalculoNotas() {}

    public static Optional<BigDecimal> calcularMediaAritmetica(List<Optional<Nota>> notas) {
        validarEntrada(notas);
        if (notas.stream().anyMatch(Optional::isEmpty)) return Optional.empty();

        BigDecimal soma = notas.stream()
                .map(Optional::orElseThrow)
                .map(Nota::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Optional.of(soma.divide(BigDecimal.valueOf(notas.size()), 1, ARREDONDAMENTO));
    }

    public static Optional<Boolean> elegivelParaExameFinal(List<Optional<Nota>> notasDasProvas) {
        return calcularMediaAritmetica(notasDasProvas)
                .map(media -> media.compareTo(MEDIA_MINIMA_EXAME_FINAL) >= 0);
    }

    public static ResultadoExameFinal calcularResultadoAposExame(BigDecimal mediaDasProvas, Nota notaExame) {
        Nota mediaProvasValidada = new Nota(mediaDasProvas);
        if (notaExame == null) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        BigDecimal mediaFinal = mediaProvasValidada.valor().add(notaExame.valor())
                .divide(BigDecimal.valueOf(2), 1, ARREDONDAMENTO);
        Nota notaFinal = new Nota(mediaFinal);
        return new ResultadoExameFinal(notaFinal, notaFinal.valor().compareTo(MEDIA_MINIMA_EXAME_FINAL) >= 0);
    }

    public static ResultadoFinalAcademico calcularResultadoFinal(List<Optional<Nota>> notasDasProvas,
                                                                   Optional<Nota> notaExame) {
        if (notasDasProvas == null || notaExame == null) throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        if (notasDasProvas.isEmpty()) {
            return new ResultadoFinalAcademico(Optional.empty(), Optional.empty(), Optional.empty(),
                    SituacaoResultadoFinal.PENDENTE_PROVAS);
        }
        var mediaProvas = calcularMediaAritmetica(notasDasProvas);
        if (mediaProvas.isEmpty()) {
            return new ResultadoFinalAcademico(Optional.empty(), Optional.empty(), Optional.empty(),
                    SituacaoResultadoFinal.PENDENTE_PROVAS);
        }
        var notaMediaProvas = new Nota(mediaProvas.orElseThrow());
        if (notaMediaProvas.valor().compareTo(MEDIA_MINIMA_EXAME_FINAL) < 0) {
            return new ResultadoFinalAcademico(Optional.of(notaMediaProvas), Optional.empty(), Optional.empty(),
                    SituacaoResultadoFinal.REPROVADO);
        }
        if (notaExame.isEmpty()) {
            return new ResultadoFinalAcademico(Optional.of(notaMediaProvas), Optional.empty(), Optional.empty(),
                    SituacaoResultadoFinal.PENDENTE_EXAME);
        }
        var resultadoExame = calcularResultadoAposExame(mediaProvas.orElseThrow(), notaExame.orElseThrow());
        return new ResultadoFinalAcademico(Optional.of(notaMediaProvas), notaExame,
                Optional.of(resultadoExame.mediaFinal()), resultadoExame.aprovado()
                ? SituacaoResultadoFinal.APROVADO : SituacaoResultadoFinal.REPROVADO);
    }

    private static void validarEntrada(List<Optional<Nota>> notas) {
        if (notas == null || notas.isEmpty() || notas.stream().anyMatch(nota -> nota == null)) {
            throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        }
    }
}
