package br.edu.scoa.academico.aplicacao.avaliacoes;

import br.edu.scoa.academico.aplicacao.turmas.RepositorioTurmas;
import br.edu.scoa.academico.dominio.avaliacoes.AvaliacaoAcademica;
import br.edu.scoa.academico.dominio.avaliacoes.CalculoNotas;
import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.Nota;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.avaliacoes.RegraAntecedenciaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.ResultadoExameFinal;
import br.edu.scoa.academico.dominio.avaliacoes.ResultadoFinalAcademico;
import br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao;
import br.edu.scoa.academico.dominio.turmas.Turma;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.NAO_AUTORIZADO;
import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.NAO_ENCONTRADA;

public final class GestaoAvaliacoes<S> {
    private final AutorizacaoAvaliacoes<S> autorizacao;
    private final RepositorioAvaliacoes avaliacoes;
    private final RepositorioTurmas turmas;
    private final Clock relogio;
    private static final ZoneId FUSO_INSTITUCIONAL = ZoneId.of("America/Sao_Paulo");

    public GestaoAvaliacoes(AutorizacaoAvaliacoes<S> autorizacao, RepositorioAvaliacoes avaliacoes,
                            RepositorioTurmas turmas, Clock relogio) {
        this.autorizacao = Objects.requireNonNull(autorizacao);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.turmas = Objects.requireNonNull(turmas);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public boolean ehAluno(S sessao) { return autorizacao.ehAluno(sessao); }
    public boolean ehProfessor(S sessao) { return autorizacao.ehProfessor(sessao); }

    public List<Turma> turmasDisponiveis(S sessao) {
        exigirSessao(sessao);
        if (autorizacao.ehCoordenador(sessao)) return turmas.todas();
        return turmas.porProfessor(autorizacao.professorId(sessao));
    }

    public AvaliacaoAcademica cadastrar(S sessao, AvaliacaoAcademica avaliacao, UUID operacao) {
        if (avaliacao == null) throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        long autor = autorizacao.usuarioId(sessao);
        exigirResponsavelOuCoordenador(sessao, avaliacao.turmaId());
        validarAntecedencia(avaliacao.dataAplicacao());
        return avaliacoes.salvar(avaliacao, operacao, autor, relogio.instant());
    }

    public AvaliacaoAcademica atualizar(S sessao, AvaliacaoAcademica avaliacao, UUID operacao) {
        if (avaliacao == null) throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        long autor = autorizacao.usuarioId(sessao);
        var atual = avaliacoes.consultar(avaliacao.id())
                .orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
        exigirResponsavelOuCoordenador(sessao, atual.turmaId());
        if (!atual.dataAplicacao().equals(avaliacao.dataAplicacao())) validarAntecedencia(avaliacao.dataAplicacao());
        return avaliacoes.atualizar(avaliacao, operacao, autor, relogio.instant());
    }

    public List<AvaliacaoAcademica> porTurma(S sessao, long turmaId) {
        exigirResponsavelOuCoordenador(sessao, turmaId);
        return avaliacoes.porTurma(turmaId);
    }

    public List<RegistroNota> diario(S sessao, long turmaId) {
        exigirResponsavelOuCoordenador(sessao, turmaId);
        return avaliacoes.diarioTurma(turmaId);
    }

    public RegistroNota lancarNota(S sessao, long avaliacaoId, long inscricaoId, Nota nota,
                                   long revisaoEsperada, UUID operacao) {
        long autor = autorizacao.usuarioId(sessao);
        var avaliacao = avaliacoes.consultar(avaliacaoId)
                .orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
        exigirResponsavelOuCoordenador(sessao, avaliacao.turmaId());
        return avaliacoes.lancarNota(avaliacaoId, inscricaoId, nota, revisaoEsperada,
                operacao, autor, relogio.instant());
    }

    public List<RegistroNota> minhasNotas(S sessao) {
        return avaliacoes.notasAluno(autorizacao.alunoId(sessao));
    }

    public Optional<BigDecimal> mediaDasProvas(List<RegistroNota> registros) {
        var provas = registros.stream()
                .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.PROVA)
                .map(RegistroNota::nota)
                .toList();
        if (provas.isEmpty()) return Optional.empty();
        return CalculoNotas.calcularMediaAritmetica(provas);
    }

    public Optional<Boolean> elegivelParaExame(List<RegistroNota> registros) {
        var provas = registros.stream()
                .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.PROVA)
                .map(RegistroNota::nota)
                .toList();
        if (provas.isEmpty()) return Optional.empty();
        return CalculoNotas.elegivelParaExameFinal(provas);
    }

    public Optional<ResultadoExameFinal> resultadoAposExame(List<RegistroNota> registros) {
        var exames = registros.stream()
                .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.EXAME_FINAL)
                .toList();
        if (exames.isEmpty()) return Optional.empty();
        if (exames.size() > 1) throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.EXAME_FINAL_MULTIPLO);
        var mediaProvas = mediaDasProvas(registros);
        var notaExame = exames.getFirst().nota();
        if (mediaProvas.isEmpty() || notaExame.isEmpty()) return Optional.empty();
        return Optional.of(CalculoNotas.calcularResultadoAposExame(mediaProvas.orElseThrow(), notaExame.orElseThrow()));
    }

    public ResultadoFinalAcademico resultadoFinal(List<RegistroNota> registros) {
        if (registros == null || registros.isEmpty()) {
            return CalculoNotas.calcularResultadoFinal(List.of(), Optional.empty());
        }
        var inscricaoId = registros.getFirst().inscricaoId();
        if (registros.stream().anyMatch(registro -> registro.inscricaoId() != inscricaoId)) {
            throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.ENTRADA_INVALIDA);
        }
        var provas = registros.stream()
                .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.PROVA)
                .map(RegistroNota::nota)
                .toList();
        var exames = registros.stream()
                .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.EXAME_FINAL)
                .toList();
        if (exames.size() > 1) throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.EXAME_FINAL_MULTIPLO);
        Optional<Nota> notaExame = exames.isEmpty() ? Optional.empty() : exames.getFirst().nota();
        return CalculoNotas.calcularResultadoFinal(provas, notaExame);
    }

    private void validarAntecedencia(LocalDate dataAplicacao) {
        RegraAntecedenciaAvaliacao.validar(dataAplicacao,
                LocalDate.now(relogio.withZone(FUSO_INSTITUCIONAL)));
    }

    private void exigirResponsavelOuCoordenador(S sessao, long turmaId) {
        exigirSessao(sessao);
        var turma = turmas.consultarTurma(turmaId).orElseThrow(() -> new FalhaAvaliacao(NAO_ENCONTRADA));
        if (autorizacao.ehCoordenador(sessao)) return;
        if (autorizacao.professorId(sessao) != turma.professorId()) throw new FalhaAvaliacao(NAO_AUTORIZADO);
    }

    private void exigirSessao(S sessao) {
        autorizacao.usuarioId(sessao);
    }
}
