package br.edu.scoa.academico.aplicacao.turmas;

import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.curriculo.PeriodoLetivoTest;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.dominio.turmas.*;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GestaoTurmasTest {

    private CenarioTurmas cenario;
    private PeriodoLetivo periodo;
    private ItemCatalogo disciplina;

    @BeforeEach
    void setUp() {
        cenario = new CenarioTurmas();
        periodo = cenario.cenarioPeriodos.salvar(PeriodoLetivoTest.novo(), UUID.randomUUID(), 7, cenario.cenarioCatalogo.relogio.instant());
        disciplina = cenario.cenarioCatalogo.salvar(
                new ItemCatalogo(0, TipoCatalogo.DISCIPLINA, "MAT101", "Cálculo I", "Limites e Derivadas", 60, true, 0),
                7, cenario.cenarioCatalogo.relogio.instant()
        );
    }

    private HorarioTurma horario(DiaSemana dia, int inicio, int fim, Long salaId) {
        return new HorarioTurma(0, dia, LocalTime.of(inicio, 0), LocalTime.of(fim, 0), salaId, 0);
    }

    @Test
    void coordenadorSalvaEConsultaSala() {
        var sala = cenario.gestao.salvarSala(
                new Sala(0, "LAB-1", "Bloco A", 40, "30 PCs", SituacaoSala.DISPONIVEL, 0),
                UUID.randomUUID()
        );
        assertTrue(sala.id() > 0);
        assertEquals(1, sala.revisao());

        var buscadas = cenario.gestao.pesquisarSalas("LAB");
        assertEquals(1, buscadas.size());

        var consultada = cenario.gestao.consultarSala(sala.id());
        assertEquals("LAB-1", consultada.identificacao());

        // Edição
        var editada = cenario.gestao.salvarSala(
                new Sala(sala.id(), "LAB-01", "Bloco A - Térreo", 45, "40 PCs", SituacaoSala.DISPONIVEL, sala.revisao()),
                UUID.randomUUID()
        );
        assertEquals(2, editada.revisao());
        assertEquals("LAB-01", editada.identificacao());
    }

    @Test
    void salaDuplicadaOuConflitoEdicaoEhRecusado() {
        var s1 = cenario.gestao.salvarSala(new Sala(0, "SALA-A", "Bloco 1", 30, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarSala(new Sala(0, "SALA-A", "Bloco 2", 30, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID()));

        // Versão antiga de edição
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarSala(new Sala(s1.id(), "SALA-A2", "Bloco 1", 30, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID()));
    }

    @Test
    void coordenadorSalvaEConsultaTurma() {
        var turma = new Turma(0, periodo.id(), disciplina.id(), 100L, "T01", 30, SituacaoTurma.PLANEJADA,
                List.of(horario(DiaSemana.SEGUNDA, 8, 10, null)), 0);

        var salva = cenario.gestao.salvarTurma(turma, UUID.randomUUID());
        assertTrue(salva.id() > 0);
        assertEquals(1, salva.revisao());

        var listadas = cenario.gestao.pesquisarTurmas(periodo.id());
        assertEquals(1, listadas.size());
        assertEquals("T01", listadas.getFirst().codigo());

        var consultada = cenario.gestao.consultarTurma(salva.id());
        assertEquals(salva.id(), consultada.id());
    }

    @Test
    void validacoesAoSalvarTurma() {
        // Período encerrado não permite turma
        var pEncerrado = cenario.cenarioPeriodos.transicionar(periodo.id(), periodo.revisao(), SituacaoPeriodoLetivo.ABERTO, 7, cenario.cenarioCatalogo.relogio.instant());
        pEncerrado = cenario.cenarioPeriodos.transicionar(pEncerrado.id(), pEncerrado.revisao(), SituacaoPeriodoLetivo.ENCERRADO, 7, cenario.cenarioCatalogo.relogio.instant());
        final var finalPEncerrado = pEncerrado;

        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarTurma(
                new Turma(0, finalPEncerrado.id(), disciplina.id(), 100L, "T02", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.TERCA, 8, 10, null)), 0),
                UUID.randomUUID()
        ));

        // Disciplina inativa
        var discInativa = cenario.cenarioCatalogo.salvar(
                new ItemCatalogo(0, TipoCatalogo.DISCIPLINA, "MAT999", "Inativa", "", 60, false, 0),
                7, cenario.cenarioCatalogo.relogio.instant()
        );
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), discInativa.id(), 100L, "T03", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.TERCA, 8, 10, null)), 0),
                UUID.randomUUID()
        ));

        // Professor inexistente
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 9999L, "T04", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.TERCA, 8, 10, null)), 0),
                UUID.randomUUID()
        ));
    }

    @Test
    void conflitosDeProfessorESalaEmTurmasDistintas() {
        var sala = cenario.gestao.salvarSala(new Sala(0, "S10", "Bloco X", 40, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());

        // Primeira turma: prof 100, segunda 08-10, sala S10
        cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 100L, "T_A", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.SEGUNDA, 8, 10, sala.id())), 0),
                UUID.randomUUID()
        );

        // Outra turma com mesmo professor no mesmo horário -> conflito
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 100L, "T_B", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.SEGUNDA, 9, 11, null)), 0),
                UUID.randomUUID()
        ));

        // Outra turma com professor diferente mas mesma sala no mesmo horário -> conflito
        assertThrows(FalhaTurma.class, () -> cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 200L, "T_C", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.SEGUNDA, 8, 10, sala.id())), 0),
                UUID.randomUUID()
        ));
    }

    @Test
    void transicoesDeSituacaoDaTurma() {
        var turma = cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 100L, "T_TR", 30, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.QUINTA, 14, 16, null)), 0),
                UUID.randomUUID()
        );

        // PLANEJADA -> ABERTA
        var aberta = cenario.gestao.transicionar(turma, SituacaoTurma.ABERTA, UUID.randomUUID());
        assertEquals(SituacaoTurma.ABERTA, aberta.situacao());

        // ABERTA -> EM_ANDAMENTO
        var emAndamento = cenario.gestao.transicionar(aberta, SituacaoTurma.EM_ANDAMENTO, UUID.randomUUID());
        assertEquals(SituacaoTurma.EM_ANDAMENTO, emAndamento.situacao());

        // EM_ANDAMENTO -> ENCERRADA
        var encerrada = cenario.gestao.transicionar(emAndamento, SituacaoTurma.ENCERRADA, UUID.randomUUID());
        assertEquals(SituacaoTurma.ENCERRADA, encerrada.situacao());

        // ENCERRADA -> qualquer transição inválida
        assertThrows(FalhaTurma.class, () -> cenario.gestao.transicionar(encerrada, SituacaoTurma.ABERTA, UUID.randomUUID()));
    }

    @Test
    void alocacaoEDesalocacaoDeSalaPosterior() {
        var salaPequena = cenario.gestao.salvarSala(new Sala(0, "PEQ", "B1", 20, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        var salaAdequada = cenario.gestao.salvarSala(new Sala(0, "ADEQ", "B1", 50, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        var salaIndisp = cenario.gestao.salvarSala(new Sala(0, "MANUT", "B1", 50, "", SituacaoSala.INDISPONIVEL, 0), UUID.randomUUID());

        var turma = cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), disciplina.id(), 100L, "T_ALOC", 35, SituacaoTurma.PLANEJADA,
                        List.of(horario(DiaSemana.SEXTA, 10, 12, null)), 0),
                UUID.randomUUID()
        );
        var h = turma.horarios().getFirst();

        // Sala com capacidade insuficiente
        assertThrows(FalhaTurma.class, () -> cenario.gestao.alocarSala(turma, h, salaPequena.id(), UUID.randomUUID()));

        // Sala indisponível
        assertThrows(FalhaTurma.class, () -> cenario.gestao.alocarSala(turma, h, salaIndisp.id(), UUID.randomUUID()));

        // Alocação com sucesso
        var alocada = cenario.gestao.alocarSala(turma, h, salaAdequada.id(), UUID.randomUUID());
        assertEquals(salaAdequada.id(), alocada.horarios().getFirst().salaId());

        // Desalocação passando null
        var hAlocado = alocada.horarios().getFirst();
        var desalocada = cenario.gestao.alocarSala(alocada, hAlocado, null, UUID.randomUUID());
        assertNull(desalocada.horarios().getFirst().salaId());
    }

    @Test
    void alunoEProfessorNaoAdministram() {
        for (var perfil : new Perfil[]{Perfil.ALUNO, Perfil.PROFESSOR}) {
            var c = new CenarioTurmas();
            c.cenarioCatalogo.conta = new Conta(7, 3, "operador", perfil, true, 1);
            assertThrows(FalhaIdentidade.class, () -> c.gestao.salvarSala(new Sala(0, "S1", "L1", 20, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID()));
            assertThrows(FalhaIdentidade.class, () -> c.gestao.pesquisarSalas(""));
            assertThrows(FalhaIdentidade.class, () -> c.gestao.consultarSala(1));
            assertThrows(FalhaIdentidade.class, () -> c.gestao.salvarTurma(new Turma(0, 1, 1, 1, "C", 20, SituacaoTurma.PLANEJADA, List.of(horario(DiaSemana.SEGUNDA, 8, 10, null)), 0), UUID.randomUUID()));
            assertThrows(FalhaIdentidade.class, () -> c.gestao.pesquisarTurmas(1));
            assertThrows(FalhaIdentidade.class, () -> c.gestao.consultarTurma(1));
        }
    }
}
