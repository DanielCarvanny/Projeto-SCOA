package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.dominio.turmas.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validação dos invariantes de Turmas, Salas e Horários no banco MySQL real descartável.
 */
class RepositorioTurmasMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioTurmasMysql repositorio;

    @BeforeAll
    static void banco() {
        var e = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(e.get("SCOA_TEST_CONFIRM"))) throw new IllegalStateException();
        banco = ConfiguracaoBanco.doAmbiente(e, "SCOA_TEST_");
        MigracoesIdentidade.executar(banco);
        var f = MigracoesAcademico.configurar(banco);
        if (f.info().applied().length == 0) MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        f.migrate();
    }

    @BeforeEach
    void preparar() throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement()) {
            s.executeUpdate("DELETE FROM sala_auditoria");
            s.executeUpdate("DELETE FROM turma_auditoria");
            s.executeUpdate("DELETE FROM horario_turma");
            s.executeUpdate("DELETE FROM turma");
            s.executeUpdate("DELETE FROM sala");
            s.executeUpdate("DELETE FROM periodo_historico");
            s.executeUpdate("DELETE FROM periodo_letivo");
            s.executeUpdate("DELETE FROM professor_auditoria");
            s.executeUpdate("DELETE FROM professor");
            s.executeUpdate("DELETE FROM disciplina_historico");
            s.executeUpdate("DELETE FROM disciplina");
            s.executeUpdate("DELETE FROM aluno_auditoria");
            s.executeUpdate("DELETE FROM aluno");
            s.executeUpdate("DELETE FROM pessoa");

            s.executeUpdate("INSERT INTO pessoa(id,cpf,nome,email) VALUES(801,'11111111111','Professor Um','p1@teste.invalid'),(802,'22222222222','Professor Dois','p2@teste.invalid')");
            s.executeUpdate("INSERT INTO professor(id,pessoa_id,titulacao,situacao_funcional) VALUES(801,801,'Mestre','ATIVO'),(802,802,'Mestre','ATIVO')");
            s.executeUpdate("INSERT INTO disciplina(id,codigo,nome,ementa,carga_horaria,situacao,versao_registro) VALUES(801,'DISC','Disciplina','Conteúdo',60,'ATIVA',1)");
            s.executeUpdate("INSERT INTO periodo_letivo(id,descricao,data_inicio,data_fim,inicio_matricula,fim_matricula,inicio_inscricao,fim_inscricao,situacao,versao_registro,operacao_criacao) VALUES(801,'2026.1','2026-01-01','2026-12-01','2025-12-01','2025-12-02','2025-12-03','2025-12-04','PLANEJADO',1,UUID())");
        }
        repositorio = new RepositorioTurmasMysql(banco);
    }

    private Turma turma(String codigo, long professor, LocalTime inicio, LocalTime fim, Long salaId) {
        return new Turma(0, 801, 801, professor, codigo, 20, SituacaoTurma.PLANEJADA,
                List.of(new HorarioTurma(0, DiaSemana.SEGUNDA, inicio, fim, salaId, 0)), 0);
    }

    private Turma turma(String codigo, long professor) {
        return turma(codigo, professor, LocalTime.of(8, 0), LocalTime.of(10, 0), null);
    }

    @Test
    void impedeAlocarMesmaSalaEmHorariosSobrepostos() {
        var sala = repositorio.salvarSala(new Sala(0, "A1", "Bloco A", 30, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID(), 7, Instant.now());
        var primeira = repositorio.salvarTurma(turma("A", 801), UUID.randomUUID(), 7, Instant.now());
        var alocada = repositorio.alocarSala(primeira.id(), primeira.horarios().getFirst().id(), primeira.revisao(), primeira.horarios().getFirst().revisao(), sala.id(), UUID.randomUUID(), 7, Instant.now());
        var segunda = repositorio.salvarTurma(turma("B", 802), UUID.randomUUID(), 7, Instant.now());

        assertEquals(FalhaTurma.Codigo.CONFLITO_SALA, assertThrows(FalhaTurma.class,
                () -> repositorio.alocarSala(segunda.id(), segunda.horarios().getFirst().id(), segunda.revisao(), segunda.horarios().getFirst().revisao(), sala.id(), UUID.randomUUID(), 7, Instant.now())).codigo());
        assertEquals(sala.id(), alocada.horarios().getFirst().salaId());
    }

    @Test
    void impedeProfessorComHorariosSobrepostosNoMesmoPeriodo() {
        repositorio.salvarTurma(turma("T01", 801, LocalTime.of(8, 0), LocalTime.of(10, 0), null), UUID.randomUUID(), 7, Instant.now());

        // Segunda turma com mesmo professor no horário 09:00 - 11:00 (sobrepõe 08:00 - 10:00)
        assertThrows(FalhaTurma.class, () -> repositorio.salvarTurma(
                turma("T02", 801, LocalTime.of(9, 0), LocalTime.of(11, 0), null),
                UUID.randomUUID(), 7, Instant.now()
        ));
    }

    @Test
    void permiteProfessorEmHorariosNaoSobrepostos() {
        var t1 = repositorio.salvarTurma(turma("T01", 801, LocalTime.of(8, 0), LocalTime.of(10, 0), null), UUID.randomUUID(), 7, Instant.now());
        var t2 = repositorio.salvarTurma(turma("T02", 801, LocalTime.of(10, 0), LocalTime.of(12, 0), null), UUID.randomUUID(), 7, Instant.now());

        assertTrue(t1.id() > 0);
        assertTrue(t2.id() > 0);
        assertNotEquals(t1.id(), t2.id());
    }

    @Test
    void alocarEDesalocarSalaPersisteNoBancoEAudita() throws Exception {
        var sala = repositorio.salvarSala(new Sala(0, "LAB-1", "Bloco A", 40, "PCs", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID(), 7, Instant.now());
        var turmaSalva = repositorio.salvarTurma(turma("TURMA-ALOC", 801), UUID.randomUUID(), 7, Instant.now());
        var h = turmaSalva.horarios().getFirst();

        // Aloca
        var alocada = repositorio.alocarSala(turmaSalva.id(), h.id(), turmaSalva.revisao(), h.revisao(), sala.id(), UUID.randomUUID(), 7, Instant.now());
        assertEquals(sala.id(), alocada.horarios().getFirst().salaId());
        assertEquals(2, alocada.revisao());

        // Consulta do banco para confirmar persistência real
        var consultada = repositorio.consultarTurma(turmaSalva.id()).orElseThrow();
        assertEquals(sala.id(), consultada.horarios().getFirst().salaId());

        // Desaloca
        var hAloc = alocada.horarios().getFirst();
        var desalocada = repositorio.alocarSala(alocada.id(), hAloc.id(), alocada.revisao(), hAloc.revisao(), null, UUID.randomUUID(), 7, Instant.now());
        assertNull(desalocada.horarios().getFirst().salaId());

        // Confere auditoria no banco
        try (var c = banco.abrir(); var s = c.createStatement(); var r = s.executeQuery("SELECT evento FROM turma_auditoria WHERE turma_id=" + turmaSalva.id() + " ORDER BY id")) {
            assertTrue(r.next());
            assertEquals("CADASTRO", r.getString("evento"));
            assertTrue(r.next());
            assertEquals("ALOCACAO", r.getString("evento"));
            assertTrue(r.next());
            assertEquals("DESALOCACAO", r.getString("evento"));
        }
    }

    @Test
    void impedeAlocarSalaComCapacidadeInsuficienteOuIndisponivel() {
        var salaPequena = repositorio.salvarSala(new Sala(0, "S-PEQ", "B1", 10, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID(), 7, Instant.now());
        var salaIndisp = repositorio.salvarSala(new Sala(0, "S-IND", "B1", 50, "", SituacaoSala.INDISPONIVEL, 0), UUID.randomUUID(), 7, Instant.now());
        var t = repositorio.salvarTurma(turma("TURMA-VAL", 801), UUID.randomUUID(), 7, Instant.now()); // capacidade 20
        var h = t.horarios().getFirst();

        assertEquals(FalhaTurma.Codigo.CAPACIDADE_INSUFICIENTE, assertThrows(FalhaTurma.class,
                () -> repositorio.alocarSala(t.id(), h.id(), t.revisao(), h.revisao(), salaPequena.id(), UUID.randomUUID(), 7, Instant.now())).codigo());

        assertEquals(FalhaTurma.Codigo.SALA_INDISPONIVEL, assertThrows(FalhaTurma.class,
                () -> repositorio.alocarSala(t.id(), h.id(), t.revisao(), h.revisao(), salaIndisp.id(), UUID.randomUUID(), 7, Instant.now())).codigo());
    }

    @Test
    void transicionarTurmaAtualizaSituacaoEAudita() throws Exception {
        var t = repositorio.salvarTurma(turma("T-SIT", 801), UUID.randomUUID(), 7, Instant.now());
        assertEquals(SituacaoTurma.PLANEJADA, t.situacao());

        var aberta = repositorio.transicionarTurma(t.id(), t.revisao(), SituacaoTurma.ABERTA, UUID.randomUUID(), 7, Instant.now());
        assertEquals(SituacaoTurma.ABERTA, aberta.situacao());
        assertEquals(2, aberta.revisao());

        try (var c = banco.abrir(); var s = c.createStatement(); var r = s.executeQuery("SELECT evento FROM turma_auditoria WHERE turma_id=" + t.id() + " AND evento='TRANSICAO_ABERTA'")) {
            assertTrue(r.next());
        }
    }

    @Test
    void concorrenciaTurmaESalaRecusaVersaoDesatualizada() {
        var sala = repositorio.salvarSala(new Sala(0, "SALA-C", "B1", 30, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID(), 7, Instant.now());
        // Edita com versão 1 -> vai para versão 2
        var sEditada = repositorio.salvarSala(new Sala(sala.id(), "SALA-C2", "B1", 35, "", SituacaoSala.DISPONIVEL, sala.revisao()), UUID.randomUUID(), 7, Instant.now());
        assertEquals(2, sEditada.revisao());

        // Outro cliente tenta editar usando versão 1 desatualizada -> CONFLITO_EDICAO
        assertThrows(FalhaTurma.class, () -> repositorio.salvarSala(
                new Sala(sala.id(), "SALA-C3", "B1", 40, "", SituacaoSala.DISPONIVEL, 1),
                UUID.randomUUID(), 7, Instant.now()
        ));

        // Concorrência na Turma
        var t = repositorio.salvarTurma(turma("T-CONC", 801), UUID.randomUUID(), 7, Instant.now());
        var tAberta = repositorio.transicionarTurma(t.id(), t.revisao(), SituacaoTurma.ABERTA, UUID.randomUUID(), 7, Instant.now());
        assertEquals(2, tAberta.revisao());

        // Outra transição com revisão antiga (1) -> CONFLITO_EDICAO
        assertThrows(FalhaTurma.class, () -> repositorio.transicionarTurma(
                t.id(), 1, SituacaoTurma.CANCELADA, UUID.randomUUID(), 7, Instant.now()
        ));
    }
}
