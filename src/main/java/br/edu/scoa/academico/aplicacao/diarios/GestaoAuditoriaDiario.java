package br.edu.scoa.academico.aplicacao.diarios;

import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.avaliacoes.RepositorioAvaliacoes;
import br.edu.scoa.academico.aplicacao.turmas.RepositorioTurmas;
import br.edu.scoa.academico.dominio.avaliacoes.SituacaoResultadoFinal;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.diarios.*;
import br.edu.scoa.academico.dominio.turmas.Turma;

import java.time.Clock;
import java.util.*;

import static br.edu.scoa.academico.dominio.diarios.CodigoPendenciaPauta.*;
import static br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario.Codigo.*;

public final class GestaoAuditoriaDiario<S> {
    private final AutorizacaoAuditoriaDiario<S> autorizacao;
    private final RepositorioPautasFinais pautas;
    private final RepositorioAvaliacoes avaliacoes;
    private final RepositorioTurmas turmas;
    private final ConsultaDiarioAulas diarioAulas;
    private final Clock relogio;
    private final GestaoAvaliacoes<S> gestaoAvaliacoes;

    public GestaoAuditoriaDiario(AutorizacaoAuditoriaDiario<S> autorizacao, RepositorioPautasFinais pautas,
                                RepositorioAvaliacoes avaliacoes, RepositorioTurmas turmas,
                                ConsultaDiarioAulas diarioAulas, Clock relogio,
                                GestaoAvaliacoes<S> gestaoAvaliacoes) {
        this.autorizacao = Objects.requireNonNull(autorizacao);
        this.pautas = Objects.requireNonNull(pautas);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.turmas = Objects.requireNonNull(turmas);
        this.diarioAulas = Objects.requireNonNull(diarioAulas);
        this.relogio = Objects.requireNonNull(relogio);
        this.gestaoAvaliacoes = Objects.requireNonNull(gestaoAvaliacoes);
    }

    public List<Turma> turmas(S sessao) {
        exigirCoordenador(sessao);
        return turmas.todas();
    }

    public ResumoAuditoriaDiario consultar(S sessao, long turmaId) {
        exigirCoordenador(sessao);
        Turma turma = turmas.consultarTurma(turmaId)
                .orElseThrow(() -> new FalhaAuditoriaDiario(TURMA_NAO_ENCONTRADA));
        PautaFinal pauta = pautas.consultar(turmaId);
        long versaoDiarioAtual = pautas.versaoDiario(turmaId);
        List<RegistroNota> diario = avaliacoes.diarioTurma(turmaId);
        var pendencias = calcularPendencias(turmaId, versaoDiarioAtual, diario);
        return new ResumoAuditoriaDiario(turma, pauta, pendencias, versaoDiarioAtual, diario,
                avaliacoes.historicoTurma(turmaId), pautas.historico(turmaId));
    }

    public PautaFinal registrarAnalise(S sessao, long turmaId, String observacao, UUID operacaoId) {
        long autor = exigirCoordenador(sessao);
        PautaFinal atual = pautas.consultar(turmaId);
        return pautas.registrarAnalise(turmaId, atual.revisao(), normalizar(observacao), autor,
                relogio.instant(), operacaoId);
    }

    public PautaFinal homologar(S sessao, long turmaId, long revisaoEsperada, UUID operacaoId) {
        long autor = exigirCoordenador(sessao);
        ResumoAuditoriaDiario resumo = consultar(sessao, turmaId);
        if (!resumo.pendencias().isEmpty()) throw new FalhaAuditoriaDiario(PENDENCIAS_PARA_HOMOLOGAR);
        return pautas.homologar(turmaId, revisaoEsperada, resumo.versaoDiarioAtual(), autor,
                relogio.instant(), operacaoId);
    }

    public PautaFinal reabrir(S sessao, long turmaId, long revisaoEsperada, String motivo, UUID operacaoId) {
        long autor = exigirCoordenador(sessao);
        return pautas.reabrir(turmaId, revisaoEsperada, normalizar(motivo), autor, relogio.instant(), operacaoId);
    }

    private List<PendenciaPauta> calcularPendencias(long turmaId, long versaoDiarioAtual, List<RegistroNota> diario) {
        var pendencias = new ArrayList<PendenciaPauta>();
        var todasAvaliacoes = avaliacoes.porTurma(turmaId);
        if (todasAvaliacoes.isEmpty()) {
            pendencias.add(new PendenciaPauta(AVALIACOES_NAO_CADASTRADAS,
                    "Nenhuma avaliação foi cadastrada para a turma."));
        }
        if (todasAvaliacoes.stream().noneMatch(avaliacao -> avaliacao.tipo() == br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao.PROVA)) {
            pendencias.add(new PendenciaPauta(PROVAS_NAO_CADASTRADAS,
                    "A turma não possui avaliação do tipo prova."));
        }
        Map<Long, List<br.edu.scoa.academico.dominio.avaliacoes.RegistroNota>> porInscricao = diario.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        br.edu.scoa.academico.dominio.avaliacoes.RegistroNota::inscricaoId));
        for (var entrada : porInscricao.entrySet()) {
            var registros = entrada.getValue();
            boolean faltaProva = registros.stream()
                    .filter(registro -> registro.avaliacao().tipo() == br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao.PROVA)
                    .anyMatch(registro -> registro.nota().isEmpty());
            if (faltaProva) {
                pendencias.add(new PendenciaPauta(NOTAS_PROVAS_PENDENTES,
                        "Há nota de prova não lançada para uma inscrição ativa.", entrada.getKey()));
                continue;
            }
            try {
                var resultado = gestaoAvaliacoes.resultadoFinal(registros);
                if (resultado.situacao() == SituacaoResultadoFinal.PENDENTE_EXAME) {
                    pendencias.add(new PendenciaPauta(EXAME_FINAL_PENDENTE,
                            "Aluno elegível ao exame final ainda não possui nota do exame.", entrada.getKey()));
                    continue;
                }
                if (resultado.situacao() == SituacaoResultadoFinal.PENDENTE_PROVAS) {
                    pendencias.add(new PendenciaPauta(NOTAS_PROVAS_PENDENTES,
                            "Não foi possível apurar a média das provas.", entrada.getKey()));
                }
            } catch (br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao falha) {
                if (falha.codigo() == br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.EXAME_FINAL_MULTIPLO) {
                    pendencias.add(new PendenciaPauta(EXAME_FINAL_MULTIPLO,
                            "Há mais de uma avaliação de exame final para a turma.", entrada.getKey()));
                } else throw falha;
            }
        }
        todasAvaliacoes.stream()
                .filter(avaliacao -> avaliacao.tipo() == br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao.EXAME_FINAL)
                .findAny().ifPresentOrElse(exame -> { }, () -> {
                    for (var entrada : porInscricao.entrySet()) {
                        var registros = entrada.getValue();
                        if (gestaoAvaliacoes.resultadoFinal(registros).situacao() == SituacaoResultadoFinal.PENDENTE_EXAME) {
                            pendencias.add(new PendenciaPauta(EXAME_FINAL_NAO_CADASTRADO,
                                    "Existe aluno elegível, mas não foi cadastrada avaliação de exame final.", entrada.getKey()));
                        }
                    }
                });
        try {
            var resumoAulas = diarioAulas.consultarPendenciasObrigatorias(turmaId);
            if (resumoAulas.turmaId() != turmaId || resumoAulas.versaoDiario() != versaoDiarioAtual) {
                pendencias.add(new PendenciaPauta(CodigoPendenciaPauta.INTEGRACAO_ACA04_INDISPONIVEL,
                        "O diário de aulas mudou durante a consulta; atualize a pauta e tente novamente."));
            } else {
                resumoAulas.pendenciasObrigatorias().forEach(pendencia -> pendencias.add(
                        new PendenciaPauta(DIARIO_AULAS_PENDENTE, pendencia.descricao())));
            }
        } catch (FalhaAuditoriaDiario falha) {
            if (falha.codigo() != FalhaAuditoriaDiario.Codigo.INTEGRACAO_ACA04_INDISPONIVEL) throw falha;
            pendencias.add(new PendenciaPauta(CodigoPendenciaPauta.INTEGRACAO_ACA04_INDISPONIVEL,
                    "A consulta de conteúdo e frequência obrigatórios do ACA-04 está indisponível."));
        }
        return List.copyOf(pendencias);
    }

    private long exigirCoordenador(S sessao) {
        long usuario = autorizacao.usuarioId(sessao);
        if (!autorizacao.ehCoordenador(sessao)) throw new FalhaAuditoriaDiario(NAO_AUTORIZADO);
        return usuario;
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.strip();
    }
}
