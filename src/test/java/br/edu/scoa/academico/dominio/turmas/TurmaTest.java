package br.edu.scoa.academico.dominio.turmas;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TurmaTest {

    private HorarioTurma horario(DiaSemana dia, int inicio, int fim) {
        return new HorarioTurma(0, dia, LocalTime.of(inicio, 0), LocalTime.of(fim, 0), null, 0);
    }

    private HorarioTurma horario(int inicio, int fim) {
        return horario(DiaSemana.SEGUNDA, inicio, fim);
    }

    @Test
    void turmaValidaEhCriadaComSucesso() {
        var turma = new Turma(0, 10, 20, 30, "TURMA-A", 35, SituacaoTurma.PLANEJADA,
                List.of(horario(DiaSemana.SEGUNDA, 8, 10), horario(DiaSemana.QUARTA, 8, 10)), 0);
        assertEquals(10, turma.periodoId());
        assertEquals(20, turma.disciplinaId());
        assertEquals(30, turma.professorId());
        assertEquals("TURMA-A", turma.codigo());
        assertEquals(35, turma.capacidade());
        assertEquals(SituacaoTurma.PLANEJADA, turma.situacao());
        assertEquals(2, turma.horarios().size());
    }

    @Test
    void horarioSobrepostoNaMesmaTurmaEhRecusado() {
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "A", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10), horario(9, 11)), 0));
    }

    @Test
    void horariosConsecutivosNoMesmoDiaSaoPermitidos() {
        var turma = new Turma(0, 1, 2, 3, "A", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10), horario(10, 12)), 0);
        assertEquals(2, turma.horarios().size());
    }

    @Test
    void horariosIguaisEmDiasDiferentesSaoPermitidos() {
        var turma = new Turma(0, 1, 2, 3, "A", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(DiaSemana.TERCA, 8, 10), horario(DiaSemana.QUINTA, 8, 10)), 0);
        assertEquals(2, turma.horarios().size());
    }

    @Test
    void horarioComFimIgualAoInicioEhRecusado() {
        assertThrows(FalhaTurma.class, () -> horario(8, 8));
    }

    @Test
    void horarioComFimAnteriorAoInicioEhRecusado() {
        assertThrows(FalhaTurma.class, () -> horario(10, 8));
    }

    @Test
    void horarioSemDiaSemanaEhRecusado() {
        assertThrows(FalhaTurma.class, () -> new HorarioTurma(0, null, LocalTime.of(8, 0), LocalTime.of(10, 0), null, 0));
    }

    @Test
    void salaIndisponivelMantemEstadoExplicito() {
        var sala = new Sala(0, "LAB-01", "Bloco A", 30, "Projetor", SituacaoSala.INDISPONIVEL, 0);
        assertEquals(SituacaoSala.INDISPONIVEL, sala.situacao());
    }

    @Test
    void turmaExigePeloMenosUmHorario() {
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "A", 30, SituacaoTurma.PLANEJADA, List.of(), 0));
    }

    @Test
    void codigoTurmaEmBrancoEhRecusado() {
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10)), 0));
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "   ", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10)), 0));
    }

    @Test
    void capacidadeTurmaMenorOuIgualAZeroEhRecusada() {
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "A", 0, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10)), 0));
        assertThrows(FalhaTurma.class, () -> new Turma(0, 1, 2, 3, "A", -10, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10)), 0));
    }

    @Test
    void identificadorAtualizaIdERevisaoEHorarios() {
        var turma = new Turma(0, 1, 2, 3, "A", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(8, 10)), 0);
        var novoHorario = new HorarioTurma(100, DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0), 50L, 1);
        var salva = turma.identificado(10, 1, List.of(novoHorario));
        assertEquals(10, salva.id());
        assertEquals(1, salva.revisao());
        assertEquals(1, salva.horarios().size());
        assertEquals(50L, salva.horarios().getFirst().salaId());
    }
}
