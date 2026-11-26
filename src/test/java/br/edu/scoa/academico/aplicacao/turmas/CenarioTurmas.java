package br.edu.scoa.academico.aplicacao.turmas;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.professores.*;
import br.edu.scoa.academico.dominio.turmas.*;
import java.time.*;
import java.util.*;
import static br.edu.scoa.academico.dominio.turmas.FalhaTurma.Codigo.*;

public final class CenarioTurmas implements RepositorioTurmas {
    public final CenarioPeriodos cenarioPeriodos = new CenarioPeriodos();
    public final CenarioCatalogo cenarioCatalogo = cenarioPeriodos.identidade;
    public final RepositorioProfessoresMemoria repositorioProfessores = new RepositorioProfessoresMemoria();
    public final GestaoTurmas gestao;

    public final Map<Long, Sala> salas = new LinkedHashMap<>();
    public final Map<Long, Turma> turmas = new LinkedHashMap<>();
    private final Map<UUID, Object> operacoes = new HashMap<>();

    public int gravacoes;
    public boolean acessouNaEdt;

    public CenarioTurmas() {
        this.gestao = new GestaoTurmas(cenarioCatalogo.identidade, cenarioCatalogo.sessao, this,
                cenarioPeriodos, cenarioCatalogo, repositorioProfessores, cenarioCatalogo.relogio);
    }

    public static class RepositorioProfessoresMemoria implements RepositorioProfessores {
        private final Map<Long, Professor> professores = new HashMap<>();
        public RepositorioProfessoresMemoria() {
            professores.put(100L, new Professor(100L, 1000L, "Doutor", SituacaoFuncional.ATIVO));
            professores.put(200L, new Professor(200L, 2000L, "Mestre", SituacaoFuncional.ATIVO));
        }
        public void cadastrar(Professor p) { professores.put(p.id(), p); }
        @Override public Professor cadastrar(long pessoaId, String titulacao, SituacaoFuncional situacao, long usuarioId) {
            var p = new Professor(professores.size() + 1, pessoaId, titulacao, situacao);
            professores.put(p.id(), p);
            return p;
        }
        @Override public Optional<Professor> consultar(long professorId) { return Optional.ofNullable(professores.get(professorId)); }
        @Override public Optional<Professor> consultarPorPessoa(long pessoaId) {
            return professores.values().stream().filter(p -> p.pessoaId() == pessoaId).findFirst();
        }
        @Override public Professor atualizar(Professor esperado, String titulacao, SituacaoFuncional situacao, long usuarioId) {
            var atualizado = new Professor(esperado.id(), esperado.pessoaId(), titulacao, situacao);
            professores.put(atualizado.id(), atualizado);
            return atualizado;
        }
    }

    @Override
    public synchronized Sala salvarSala(Sala sala, UUID operacao, long autor, Instant instante) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        if (operacao == null) throw new FalhaTurma(OPERACAO_REUTILIZADA);
        if (salas.values().stream().anyMatch(s -> s.id() != sala.id() && s.identificacao().equalsIgnoreCase(sala.identificacao()))) {
            throw new FalhaTurma(SALA_INVALIDA);
        }
        if (sala.id() != 0) {
            var atual = consultarSala(sala.id()).orElseThrow(() -> new FalhaTurma(NAO_ENCONTRADO));
            if (atual.revisao() != sala.revisao()) throw new FalhaTurma(CONFLITO_EDICAO);
        }
        long id = sala.id() == 0 ? (salas.size() + 1) : sala.id();
        long rev = sala.revisao() + 1;
        var salva = sala.identificado(id, rev);
        salas.put(id, salva);
        operacoes.put(operacao, salva);
        gravacoes++;
        return salva;
    }

    @Override
    public synchronized Optional<Sala> consultarSala(long id) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        return Optional.ofNullable(salas.get(id));
    }

    @Override
    public synchronized List<Sala> pesquisarSalas(String termo) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        String t = termo == null ? "" : termo.strip().toLowerCase(Locale.ROOT);
        return salas.values().stream()
                .filter(s -> s.identificacao().toLowerCase(Locale.ROOT).contains(t) || s.localizacao().toLowerCase(Locale.ROOT).contains(t))
                .sorted(Comparator.comparing(Sala::identificacao))
                .toList();
    }

    @Override
    public synchronized Turma salvarTurma(Turma turma, UUID operacao, long autor, Instant instante) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        if (operacao == null) throw new FalhaTurma(OPERACAO_REUTILIZADA);
        if (turmas.values().stream().anyMatch(t -> t.id() != turma.id() && t.periodoId() == turma.periodoId() && t.codigo().equalsIgnoreCase(turma.codigo()))) {
            throw new FalhaTurma(TURMA_INVALIDA);
        }
        // Conflito de professor no mesmo periodo
        for (var tExistente : turmas.values()) {
            if (tExistente.id() != turma.id() && tExistente.periodoId() == turma.periodoId() && tExistente.professorId() == turma.professorId()) {
                for (var h1 : turma.horarios()) {
                    for (var h2 : tExistente.horarios()) {
                        if (h1.sobrepoe(h2)) throw new FalhaTurma(CONFLITO_PROFESSOR);
                    }
                }
            }
        }
        // Conflito de sala no mesmo periodo
        for (var tExistente : turmas.values()) {
            if (tExistente.id() != turma.id() && tExistente.periodoId() == turma.periodoId()) {
                for (var h1 : turma.horarios()) {
                    if (h1.salaId() != null) {
                        for (var h2 : tExistente.horarios()) {
                            if (h1.salaId().equals(h2.salaId()) && h1.sobrepoe(h2)) throw new FalhaTurma(CONFLITO_SALA);
                        }
                    }
                }
            }
        }
        long id = turma.id() == 0 ? (turmas.size() + 1) : turma.id();
        long rev = turma.revisao() + 1;
        long hId = 1;
        var novosHorarios = new ArrayList<HorarioTurma>();
        for (var h : turma.horarios()) {
            novosHorarios.add(h.identificado(h.id() == 0 ? hId++ : h.id(), h.revisao() == 0 ? 1 : h.revisao() + 1));
        }
        var salva = turma.identificado(id, rev, novosHorarios);
        turmas.put(id, salva);
        operacoes.put(operacao, salva);
        gravacoes++;
        return salva;
    }

    @Override
    public synchronized Optional<Turma> consultarTurma(long id) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        return Optional.ofNullable(turmas.get(id));
    }

    @Override
    public synchronized List<Turma> pesquisarTurmas(long periodoId) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        return turmas.values().stream()
                .filter(t -> t.periodoId() == periodoId)
                .sorted(Comparator.comparing(Turma::codigo))
                .toList();
    }

    @Override
    public synchronized Turma transicionarTurma(long id, long revisao, SituacaoTurma destino, UUID operacao, long autor, Instant instante) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        if (operacao == null) throw new FalhaTurma(OPERACAO_REUTILIZADA);
        var atual = consultarTurma(id).orElseThrow(() -> new FalhaTurma(NAO_ENCONTRADO));
        if (atual.revisao() != revisao) throw new FalhaTurma(CONFLITO_EDICAO);
        var salva = new Turma(id, atual.periodoId(), atual.disciplinaId(), atual.professorId(), atual.codigo(),
                atual.capacidade(), destino, atual.horarios(), revisao + 1);
        turmas.put(id, salva);
        gravacoes++;
        return salva;
    }

    @Override
    public synchronized Turma alocarSala(long turmaId, long horarioId, long revisaoTurma, long revisaoHorario, Long salaId, UUID operacao, long autor, Instant instante) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        if (operacao == null) throw new FalhaTurma(OPERACAO_REUTILIZADA);
        var turma = consultarTurma(turmaId).orElseThrow(() -> new FalhaTurma(NAO_ENCONTRADO));
        if (turma.revisao() != revisaoTurma) throw new FalhaTurma(CONFLITO_EDICAO);
        var horario = turma.horarios().stream().filter(h -> h.id() == horarioId).findFirst().orElseThrow(() -> new FalhaTurma(NAO_ENCONTRADO));
        if (horario.revisao() != revisaoHorario) throw new FalhaTurma(CONFLITO_EDICAO);

        if (salaId != null) {
            var sala = consultarSala(salaId).orElseThrow(() -> new FalhaTurma(NAO_ENCONTRADO));
            if (sala.situacao() != SituacaoSala.DISPONIVEL) throw new FalhaTurma(SALA_INDISPONIVEL);
            if (sala.capacidade() < turma.capacidade()) throw new FalhaTurma(CAPACIDADE_INSUFICIENTE);

            for (var t : turmas.values()) {
                if (t.periodoId() == turma.periodoId()) {
                    for (var h : t.horarios()) {
                        if (h.id() != horarioId && salaId.equals(h.salaId()) && horario.sobrepoe(h)) {
                            throw new FalhaTurma(CONFLITO_SALA);
                        }
                    }
                }
            }
        }

        var novosHorarios = new ArrayList<HorarioTurma>();
        for (var h : turma.horarios()) {
            if (h.id() == horarioId) {
                novosHorarios.add(new HorarioTurma(h.id(), h.diaSemana(), h.horaInicio(), h.horaFim(), salaId, h.revisao() + 1));
            } else {
                novosHorarios.add(h);
            }
        }
        var salva = new Turma(turmaId, turma.periodoId(), turma.disciplinaId(), turma.professorId(), turma.codigo(),
                turma.capacidade(), turma.situacao(), novosHorarios, revisaoTurma + 1);
        turmas.put(turmaId, salva);
        gravacoes++;
        return salva;
    }
    @Override public synchronized List<Turma> porProfessor(long professorId) {
        return turmas.values().stream().filter(turma -> turma.professorId() == professorId).toList();
    }
    @Override public synchronized List<Turma> todas() {
        return List.copyOf(turmas.values());
    }
}
