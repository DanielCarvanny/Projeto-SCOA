package br.edu.scoa.academico.aplicacao.boletins;

import br.edu.scoa.academico.aplicacao.avaliacoes.AutorizacaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.avaliacoes.RepositorioAvaliacoes;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.matriculas.RepositorioInscricoes;
import br.edu.scoa.academico.aplicacao.matriculas.RepositorioMatriculas;
import br.edu.scoa.academico.aplicacao.turmas.RepositorioTurmas;
import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao;
import br.edu.scoa.academico.dominio.curriculo.ComponenteCurricular;
import br.edu.scoa.academico.dominio.curriculo.MatrizCurricular;
import br.edu.scoa.academico.dominio.matriculas.MatriculaCurso;
import br.edu.scoa.academico.dominio.matriculas.SituacaoInscricao;
import br.edu.scoa.academico.dominio.matriculas.SituacaoMatricula;
import br.edu.scoa.academico.dominio.turmas.SituacaoTurma;
import br.edu.scoa.academico.dominio.turmas.Turma;

import java.util.*;
import java.util.stream.Collectors;

import static br.edu.scoa.academico.aplicacao.boletins.FalhaBoletimAluno.Codigo.*;

/** Consultas do titular autenticado; não aceita ids de aluno fornecidos pela apresentação. */
public final class GestaoBoletimAluno<S> {
    private final AutorizacaoAvaliacoes<S> autorizacao;
    private final GestaoAvaliacoes<S> gestaoAvaliacoes;
    private final RepositorioAvaliacoes avaliacoes;
    private final RepositorioMatriculas matriculas;
    private final RepositorioInscricoes inscricoes;
    private final RepositorioTurmas turmas;
    private final RepositorioMatrizes matrizes;
    private final RepositorioCatalogo catalogo;
    private final RepositorioPeriodos periodos;

    public GestaoBoletimAluno(AutorizacaoAvaliacoes<S> autorizacao, GestaoAvaliacoes<S> gestaoAvaliacoes,
            RepositorioAvaliacoes avaliacoes, RepositorioMatriculas matriculas,
            RepositorioInscricoes inscricoes, RepositorioTurmas turmas,
            RepositorioMatrizes matrizes, RepositorioCatalogo catalogo, RepositorioPeriodos periodos) {
        this.autorizacao = Objects.requireNonNull(autorizacao);
        this.gestaoAvaliacoes = Objects.requireNonNull(gestaoAvaliacoes);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.matriculas = Objects.requireNonNull(matriculas);
        this.inscricoes = Objects.requireNonNull(inscricoes);
        this.turmas = Objects.requireNonNull(turmas);
        this.matrizes = Objects.requireNonNull(matrizes);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.periodos = Objects.requireNonNull(periodos);
    }

    public ResumoBoletimAluno consultar(S sessao) {
        long alunoId = exigirAluno(sessao);
        List<RegistroNota> lancamentos = gestaoAvaliacoes.minhasNotas(sessao);
        if (lancamentos.stream().anyMatch(registro -> registro.alunoId() != alunoId)) {
            throw new FalhaBoletimAluno(DADOS_ACADEMICOS_INCONSISTENTES);
        }
        Map<Long, List<RegistroNota>> lancamentosPorInscricao = lancamentos.stream()
                .collect(Collectors.groupingBy(RegistroNota::inscricaoId));
        Map<Long, Turma> turmasConsultadas = new HashMap<>();
        Map<Long, br.edu.scoa.academico.dominio.curriculo.PeriodoLetivo> periodosConsultados = new HashMap<>();
        Map<Long, ItemCatalogo> disciplinasPlano = new HashMap<>();
        Map<Long, ItemCatalogo> disciplinasConsultadas = new HashMap<>();
        Map<Long, ItemCatalogo> cursosConsultados = new HashMap<>();
        Map<Long, br.edu.scoa.academico.dominio.turmas.Sala> salasConsultadas = new HashMap<>();
        var matriculasDoAluno = matriculas.porAluno(alunoId);
        if (matriculasDoAluno.isEmpty() && !lancamentos.isEmpty()) {
            throw new FalhaBoletimAluno(DADOS_ACADEMICOS_INCONSISTENTES);
        }
        var resumosMatricula = new ArrayList<MatriculaBoletimAluno>();
        var horariosDoAluno = new ArrayList<HorarioSemanalAluno>();

        for (MatriculaCurso matricula : matriculasDoAluno) {
            if (matricula.alunoId() != alunoId) throw new FalhaBoletimAluno(DADOS_ACADEMICOS_INCONSISTENTES);
            ItemCatalogo curso = consultarCatalogo(cursosConsultados, TipoCatalogo.CURSO, matricula.cursoId(), CURSO_NAO_ENCONTRADO);
            MatrizCurricular matriz = consultarMatriz(matricula);
            List<ComponentePlanoAluno> componentes = componentesPlano(matriz, disciplinasPlano);
            Map<Long, ComponentePlanoAluno> componentesPorDisciplina = componentes.stream()
                    .collect(Collectors.toMap(componente -> componente.disciplina().id(), componente -> componente));
            var disciplinasHistorico = new ArrayList<DisciplinaBoletimAluno>();

            for (var inscricao : inscricoes.porMatricula(matricula.id())) {
                if (inscricao.matriculaId() != matricula.id()) throw new FalhaBoletimAluno(DADOS_ACADEMICOS_INCONSISTENTES);
                Turma turma = consultarTurma(turmasConsultadas, inscricao.turmaId());
                ItemCatalogo disciplina = consultarCatalogo(disciplinasConsultadas, TipoCatalogo.DISCIPLINA,
                        turma.disciplinaId(), DISCIPLINA_NAO_ENCONTRADA);
                var periodo = periodosConsultados.computeIfAbsent(turma.periodoId(), this::consultarPeriodo);
                List<RegistroNota> notasDaInscricao = lancamentosPorInscricao.getOrDefault(inscricao.id(), List.of())
                        .stream().filter(registro -> registro.avaliacao().turmaId() == turma.id()).toList();
                Optional<br.edu.scoa.academico.dominio.avaliacoes.ResultadoFinalAcademico> resultado = Optional.empty();
                boolean examesMultiplos = notasDaInscricao.stream()
                        .filter(registro -> registro.avaliacao().tipo() == TipoAvaliacao.EXAME_FINAL).count() > 1;
                if (!notasDaInscricao.isEmpty() && !examesMultiplos) {
                    try { resultado = Optional.of(gestaoAvaliacoes.resultadoFinal(notasDaInscricao)); }
                    catch (FalhaAvaliacao falha) {
                        if (falha.codigo() != FalhaAvaliacao.Codigo.EXAME_FINAL_MULTIPLO) throw falha;
                        examesMultiplos = true;
                    }
                }
                disciplinasHistorico.add(new DisciplinaBoletimAluno(matricula.id(), inscricao, turma,
                        periodo.descricao(), disciplina, notasDaInscricao, resultado, examesMultiplos,
                        Optional.ofNullable(componentesPorDisciplina.get(disciplina.id()))));

                if (matricula.situacao() == SituacaoMatricula.ATIVA
                        && inscricao.situacao() == SituacaoInscricao.ATIVA
                        && (turma.situacao() == SituacaoTurma.ABERTA || turma.situacao() == SituacaoTurma.EM_ANDAMENTO)) {
                    for (var horario : turma.horarios()) {
                        var sala = horario.salaId() == null ? null : salasConsultadas.computeIfAbsent(
                                horario.salaId(), id -> turmas.consultarSala(id)
                                        .orElseThrow(() -> new FalhaBoletimAluno(SALA_NAO_ENCONTRADA)));
                        horariosDoAluno.add(new HorarioSemanalAluno(matricula.id(), curso, periodo,
                                disciplina, turma, horario, sala));
                    }
                }
            }
            resumosMatricula.add(new MatriculaBoletimAluno(matricula, curso, matriz, componentes, disciplinasHistorico));
        }
        horariosDoAluno.sort(Comparator.comparing((HorarioSemanalAluno horario) -> horario.horario().diaSemana())
                .thenComparing(horario -> horario.horario().horaInicio())
                .thenComparing(horario -> horario.turma().codigo()));
        return new ResumoBoletimAluno(resumosMatricula, horariosDoAluno);
    }

    private long exigirAluno(S sessao) {
        try {
            if (sessao == null || !autorizacao.ehAluno(sessao)) throw new FalhaBoletimAluno(NAO_AUTORIZADO);
            long alunoId = autorizacao.alunoId(sessao);
            if (alunoId <= 0) throw new FalhaBoletimAluno(ALUNO_NAO_ENCONTRADO);
            return alunoId;
        } catch (FalhaBoletimAluno falha) {
            throw falha;
        } catch (RuntimeException falha) {
            throw new FalhaBoletimAluno(CONSULTA_INDISPONIVEL);
        }
    }

    private MatrizCurricular consultarMatriz(MatriculaCurso matricula) {
        try {
            MatrizCurricular matriz = matrizes.consultar(matricula.matrizId());
            if (matriz.cursoId() != matricula.cursoId()) throw new FalhaBoletimAluno(DADOS_ACADEMICOS_INCONSISTENTES);
            return matriz;
        } catch (FalhaBoletimAluno falha) {
            throw falha;
        } catch (RuntimeException falha) {
            throw new FalhaBoletimAluno(CONSULTA_INDISPONIVEL);
        }
    }

    private List<ComponentePlanoAluno> componentesPlano(MatrizCurricular matriz, Map<Long, ItemCatalogo> cache) {
        return matriz.componentes().stream()
                .sorted(Comparator.comparingInt(ComponenteCurricular::periodoIdeal)
                        .thenComparingLong(ComponenteCurricular::disciplinaId))
                .map(componente -> {
                    ItemCatalogo disciplina = catalogo.historico(TipoCatalogo.DISCIPLINA, componente.disciplinaId()).stream()
                            .map(RevisaoCatalogo::item)
                            .filter(item -> item.versao() == componente.disciplinaRevisao())
                            .findFirst()
                            .orElseThrow(() -> new FalhaBoletimAluno(DISCIPLINA_NAO_ENCONTRADA));
                    cache.put(componente.disciplinaId(), disciplina);
                    return new ComponentePlanoAluno(disciplina, componente);
                }).toList();
    }

    private ItemCatalogo consultarCatalogo(Map<Long, ItemCatalogo> cache, TipoCatalogo tipo, long id,
                                            FalhaBoletimAluno.Codigo codigoFalha) {
        return cache.computeIfAbsent(id, chave -> catalogo.consultar(tipo, chave)
                .orElseThrow(() -> new FalhaBoletimAluno(codigoFalha)));
    }

    private Turma consultarTurma(Map<Long, Turma> cache, long turmaId) {
        return cache.computeIfAbsent(turmaId, id -> turmas.consultarTurma(id)
                .orElseThrow(() -> new FalhaBoletimAluno(TURMA_NAO_ENCONTRADA)));
    }

    private br.edu.scoa.academico.dominio.curriculo.PeriodoLetivo consultarPeriodo(long periodoId) {
        try {
            return periodos.consultar(periodoId);
        } catch (br.edu.scoa.academico.dominio.curriculo.FalhaPeriodo falha) {
            if (falha.codigo() == br.edu.scoa.academico.dominio.curriculo.FalhaPeriodo.Codigo.NAO_ENCONTRADO) {
                throw new FalhaBoletimAluno(PERIODO_NAO_ENCONTRADO);
            }
            throw new FalhaBoletimAluno(CONSULTA_INDISPONIVEL);
        }
    }
}
