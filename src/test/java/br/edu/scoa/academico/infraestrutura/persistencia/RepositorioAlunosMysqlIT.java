package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.alunos.CadastroAlunoException;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Fixture Pessoa só existe no banco sintético. Não representa a fundação real. */
class RepositorioAlunosMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioAlunosMysql repositorio;

    @BeforeAll static void prepararEsquema() throws Exception {
        var ambiente = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(ambiente.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(ambiente.get("SCOA_TEST_USER"))) {
            throw new IllegalStateException("Integração requer banco descartável local exclusivo na porta 3308.");
        }
        banco = ConfiguracaoBanco.doAmbiente(ambiente, "SCOA_TEST_");
        try (var c = banco.abrir(); var s = c.createStatement()) {
            assertEquals("scoa_test", c.getCatalog());
            s.execute("CREATE TABLE IF NOT EXISTS pessoa (id INT PRIMARY KEY) ENGINE=InnoDB");
        }
        var flyway = MigracoesAcademico.configurar(banco);
        // Baseline explícito somente na fixture de testes; não adota esquema de produção.
        if (flyway.info().all().length == 1 && flyway.info().applied().length == 0) {
            org.flywaydb.core.Flyway.configure().dataSource(banco.url(), banco.usuario(), banco.senha())
                .table("flyway_academico_schema_history").baselineVersion("0").load().baseline();
        }
        flyway.migrate();
    }

    @BeforeEach void limparDadosSinteticos() throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement()) {
            s.execute("DROP TRIGGER IF EXISTS teste_falha_auditoria");
            s.executeUpdate("DELETE FROM aluno_auditoria");
            s.executeUpdate("DELETE FROM aluno");
            s.executeUpdate("DELETE FROM pessoa");
            s.executeUpdate("INSERT INTO pessoa (id) VALUES (42), (43)");
        }
        repositorio = new RepositorioAlunosMysql(banco);
    }

    @Test void gravaEConsultaEmNovaConexaoComAuditoria() throws Exception {
        var aluno = repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7);
        assertTrue(aluno.id() > 0);
        assertEquals(aluno, new RepositorioAlunosMysql(banco).consultar(aluno.id()).orElseThrow());
        assertEquals(1, contar("SELECT COUNT(*) FROM aluno_auditoria WHERE usuario_id=7 AND operacao='CADASTRO'"));
    }
    @Test void recusaDuplicidadePreservandoRegistro() throws Exception {
        var aluno = repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7);
        var erro = assertThrows(CadastroAlunoException.class,
                () -> repositorio.cadastrar(42, SituacaoCadastral.INATIVO, 7));
        assertEquals("ALUNO_JA_CADASTRADO", erro.codigo().name());
        assertEquals(aluno, repositorio.consultar(aluno.id()).orElseThrow());
        assertEquals(1, contar("SELECT COUNT(*) FROM aluno_auditoria"));
    }
    @Test void recusaPessoaInexistente() throws Exception {
        var erro = assertThrows(CadastroAlunoException.class,
                () -> repositorio.cadastrar(999, SituacaoCadastral.ATIVO, 7));
        assertEquals("PESSOA_NAO_ENCONTRADA", erro.codigo().name());
        assertEquals(0, contar("SELECT COUNT(*) FROM aluno"));
    }
    @Test void consultaAusenteRetornaVazio() { assertTrue(repositorio.consultar(999).isEmpty()); }
    @Test void atualizaSemMudarPessoaComHistorico() throws Exception {
        var aluno = repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7);
        var atualizado = repositorio.atualizarSituacao(aluno.id(), SituacaoCadastral.INATIVO, 8);
        assertEquals(42, atualizado.pessoaId());
        assertEquals(SituacaoCadastral.INATIVO, repositorio.consultar(aluno.id()).orElseThrow().situacaoCadastral());
        assertEquals(1, contar("SELECT COUNT(*) FROM aluno_auditoria WHERE usuario_id=8 AND situacao_anterior='ATIVO' AND situacao_nova='INATIVO'"));
    }
    @Test void atualizacaoAusenteRetornaErroEstavel() {
        var erro = assertThrows(CadastroAlunoException.class,
                () -> repositorio.atualizarSituacao(999, SituacaoCadastral.INATIVO, 7));
        assertEquals("ALUNO_NAO_ENCONTRADO", erro.codigo().name());
    }
    @Test void falhaNaAuditoriaReverteCadastro() throws Exception {
        bloquearAuditoria();
        assertThrows(PersistenciaAcademicaException.class, () -> repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7));
        assertEquals(0, contar("SELECT COUNT(*) FROM aluno"));
    }
    @Test void falhaNaAuditoriaReverteAtualizacao() throws Exception {
        var aluno = repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7);
        bloquearAuditoria();
        assertThrows(PersistenciaAcademicaException.class,
                () -> repositorio.atualizarSituacao(aluno.id(), SituacaoCadastral.INATIVO, 8));
        assertEquals(aluno, repositorio.consultar(aluno.id()).orElseThrow());
    }
    @Test void duasConexoesConcorrentesCriamUmUnicoAluno() throws Exception {
        var inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<String> operacao = () -> {
                inicio.await();
                try { repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7); return "CRIADO"; }
                catch (CadastroAlunoException e) { return e.codigo().name(); }
            };
            var a = executor.submit(operacao); var b = executor.submit(operacao);
            inicio.countDown();
            var resultados = java.util.List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
            assertTrue(resultados.contains("CRIADO"));
            assertTrue(resultados.contains("ALUNO_JA_CADASTRADO"));
        }
        assertEquals(1, contar("SELECT COUNT(*) FROM aluno"));
        assertEquals(1, contar("SELECT COUNT(*) FROM aluno_auditoria"));
    }
    @Test void bancoRejeitaSituacaoInvalida() throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement()) {
            assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO aluno(pessoa_id,situacao_cadastral) VALUES(42,'OUTRA')"));
        }
    }
    @Test void migracaoReexecutadaNaoAlteraDados() {
        var aluno = repositorio.cadastrar(42, SituacaoCadastral.ATIVO, 7);
        assertEquals(0, MigracoesAcademico.configurar(banco).migrate().migrationsExecuted);
        assertEquals(aluno, repositorio.consultar(aluno.id()).orElseThrow());
    }
    @Test void credencialInvalidaNaoConecta() {
        var invalida = new ConfiguracaoBanco(banco.url(), "scoa_test", "senha-incorreta");
        assertThrows(SQLException.class, invalida::abrir);
    }
    private void bloquearAuditoria() throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement()) {
            s.execute("CREATE TRIGGER teste_falha_auditoria BEFORE INSERT ON aluno_auditoria FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Falha de teste'");
        }
    }
    private long contar(String sql) throws Exception {
        try (var c = banco.abrir(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            r.next(); return r.getLong(1);
        }
    }
}
