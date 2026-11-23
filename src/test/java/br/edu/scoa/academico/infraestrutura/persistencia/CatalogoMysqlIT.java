package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.Instant;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioCatalogoMysql repositorio;
    static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");
    @BeforeAll static void preparar() {
        var ambiente = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(ambiente.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(ambiente.get("SCOA_TEST_USER")))
            throw new IllegalStateException("Exige banco exclusivo de teste.");
        banco = ConfiguracaoBanco.doAmbiente(ambiente, "SCOA_TEST_");
        MigracoesIdentidade.executar(banco);
        var migracoes = MigracoesAcademico.configurar(banco);
        if (migracoes.info().applied().length == 0) MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        migracoes.migrate();
    }
    @BeforeEach void iniciar() throws Exception { limpar(); repositorio = new RepositorioCatalogoMysql(banco); }
    @AfterAll static void limpar() throws Exception {
        if (banco == null) return;
        try (var conexao = banco.abrir(); var comando = conexao.createStatement()) {
            comando.execute("DROP TRIGGER IF EXISTS teste_catalogo_historico");
            for (String tabela : new String[]{"horario_turma", "turma_auditoria", "turma", "disciplina_historico", "curso_historico", "disciplina", "curso"})
                comando.executeUpdate("DELETE FROM " + tabela);
        }
    }
    static ItemCatalogo novo(TipoCatalogo tipo, String codigo) {
        return GestaoCatalogo.validar(tipo, null, codigo, "Nome sintético", "Ementa inicial", 80);
    }
    @Test void persisteReabrePesquisaEGuardaConteudoAnterior() {
        for (var tipo : TipoCatalogo.values()) {
            var salvo = repositorio.salvar(novo(tipo, "CAT"), 7, AGORA);
            var alterado = GestaoCatalogo.validar(tipo, salvo, "CAT", "Novo nome", "Nova ementa", 100);
            var atualizado = repositorio.salvar(alterado, 8, AGORA.plusSeconds(1));
            assertEquals(2, atualizado.versao());
            var reaberto = new RepositorioCatalogoMysql(banco);
            assertEquals(atualizado, reaberto.pesquisar(tipo, "Novo").getFirst());
            var historico = reaberto.historico(tipo, salvo.id());
            assertEquals(2, historico.size());
            assertEquals("Nome sintético", historico.getFirst().item().nome());
            assertEquals(tipo == TipoCatalogo.CURSO ? "" : "Ementa inicial", historico.getFirst().item().ementa());
            assertEquals(7, historico.getFirst().autorId());
            assertEquals(AGORA, historico.getFirst().instante());
            assertNotEquals(historico.getFirst().operacaoId(), historico.getLast().operacaoId());
            var inativo = new ItemCatalogo(atualizado.id(), tipo, atualizado.codigo(), atualizado.nome(), atualizado.ementa(), 100, false, atualizado.versao());
            assertFalse(repositorio.salvar(inativo, 7, AGORA).estaAtivo());
            assertEquals(3, reaberto.historico(tipo, salvo.id()).size());
        }
    }
    @Test void duplicidadeNormalizadaNaoGravaHistorico() {
        var salvo = repositorio.salvar(novo(TipoCatalogo.CURSO, "ADS"), 7, AGORA);
        assertEquals(FalhaCurriculo.Codigo.CODIGO_DUPLICADO,
            assertThrows(FalhaCurriculo.class, () -> repositorio.salvar(novo(TipoCatalogo.CURSO, " ads "), 7, AGORA)).codigo());
        assertEquals(1, repositorio.historico(TipoCatalogo.CURSO, salvo.id()).size());
    }
    @Test void falhaNoHistoricoDesfazCadastro() throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement()) {
            s.execute("CREATE TRIGGER teste_catalogo_historico BEFORE INSERT ON disciplina_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");
        }
        assertThrows(FalhaCurriculo.class, () -> repositorio.salvar(novo(TipoCatalogo.DISCIPLINA, "POO"), 7, AGORA));
        assertTrue(repositorio.pesquisar(TipoCatalogo.DISCIPLINA, "").isEmpty());
    }
    @Test void duasEdicoesDaMesmaRevisaoNaoSobrescrevemSilenciosamente() throws Exception {
        var salvo = repositorio.salvar(novo(TipoCatalogo.DISCIPLINA, "POO"), 7, AGORA);
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> editar = () -> {
                largada.await();
                try { repositorio.salvar(GestaoCatalogo.validar(salvo.tipo(), salvo, "POO", "Outra ementa", "Classes", 90), 7, AGORA); return true; }
                catch (FalhaCurriculo erro) { assertEquals(FalhaCurriculo.Codigo.CONFLITO_EDICAO, erro.codigo()); return false; }
            };
            var primeiro = executor.submit(editar); var segundo = executor.submit(editar); largada.countDown();
            assertNotEquals(primeiro.get(10, TimeUnit.SECONDS), segundo.get(10, TimeUnit.SECONDS));
        }
        assertEquals(2, repositorio.historico(salvo.tipo(), salvo.id()).size());
    }
    @Test void pesquisaTrataCaracteresSqlComoTexto() {
        repositorio.salvar(novo(TipoCatalogo.CURSO, "ADS"), 7, AGORA);
        assertTrue(repositorio.pesquisar(TipoCatalogo.CURSO, "' OR 1=1 --").isEmpty());
        assertTrue(repositorio.pesquisar(TipoCatalogo.CURSO, "%").isEmpty());
    }
    @Test void cadastrosConcorrentesNaoDuplicamCodigo() throws Exception {
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> cadastrar = () -> {
                largada.await();
                try { repositorio.salvar(novo(TipoCatalogo.CURSO, "ADS"), 7, AGORA); return true; }
                catch (FalhaCurriculo erro) { assertEquals(FalhaCurriculo.Codigo.CODIGO_DUPLICADO, erro.codigo()); return false; }
            };
            var primeiro = executor.submit(cadastrar); var segundo = executor.submit(cadastrar); largada.countDown();
            assertNotEquals(primeiro.get(10, TimeUnit.SECONDS), segundo.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1, repositorio.pesquisar(TipoCatalogo.CURSO, "ADS").size());
    }
    @Test void bancoRecusaCargaInvalidaEExclusaoComHistorico() throws Exception {
        var salvo = repositorio.salvar(novo(TipoCatalogo.CURSO, "ADS"), 7, AGORA);
        try (var conexao = banco.abrir(); var comando = conexao.createStatement()) {
            assertThrows(java.sql.SQLException.class, () -> comando.executeUpdate("UPDATE curso SET carga_horaria_total=0 WHERE codigo='ADS'"));
            assertThrows(java.sql.SQLException.class, () -> comando.executeUpdate("DELETE FROM curso WHERE codigo='ADS'"));
        }
        assertEquals(80, repositorio.pesquisar(TipoCatalogo.CURSO, "ADS").getFirst().cargaHoraria());
        assertEquals(1, repositorio.historico(TipoCatalogo.CURSO, salvo.id()).size());
    }
}
