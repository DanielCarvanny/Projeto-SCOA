package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.professores.*;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioProfessoresMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioProfessoresMysql repositorio;
    @BeforeAll static void preparar() throws Exception {
        var ambiente=System.getenv();
        if(!"CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(ambiente.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(ambiente.get("SCOA_TEST_USER"))) throw new IllegalStateException("Exige banco descartável scoa_test na porta 3308");
        banco=ConfiguracaoBanco.doAmbiente(ambiente,"SCOA_TEST_");
        try(var c=banco.abrir();var s=c.createStatement()) { s.execute("CREATE TABLE IF NOT EXISTS pessoa(id INT PRIMARY KEY) ENGINE=InnoDB"); }
        var flyway=MigracoesAcademico.configurar(banco);
        if(flyway.info().applied().length==0) {
            org.flywaydb.core.Flyway.configure().dataSource(banco.url(),banco.usuario(),banco.senha())
                .table("flyway_academico_schema_history").baselineVersion("0").load().baseline();
        }
        flyway.migrate();
    }
    @BeforeEach void limpar() throws Exception {
        limparFixtures();
        try(var c=banco.abrir();var s=c.createStatement()) { s.executeUpdate("INSERT INTO pessoa(id) VALUES(910001),(910002)"); }
        repositorio=new RepositorioProfessoresMysql(banco);
    }
    @AfterAll static void limparFixtures() throws Exception {
        if(banco==null) return;
        try(var c=banco.abrir();var s=c.createStatement()) {
            s.execute("DROP TRIGGER IF EXISTS teste_falha_professor");
            s.executeUpdate("DELETE h FROM professor_auditoria h JOIN professor p ON p.id=h.professor_id WHERE p.pessoa_id IN(910001,910002)");
            s.executeUpdate("DELETE FROM professor WHERE pessoa_id IN(910001,910002)");
            s.executeUpdate("DELETE FROM pessoa WHERE id IN(910001,910002)");
        }
    }
    @Test void tituloLegadoPermaneceLegivelEConversaoExplicitaEhAuditada() throws Exception {
        // Representa dado antigo; não modifica migração compartilhada nem converte em lote.
        try(var c=banco.abrir();var s=c.createStatement()) {
            s.executeUpdate("INSERT INTO professor(pessoa_id,titulacao,situacao_funcional) VALUES(910001,'Mestrado','ATIVO')");
        }
        long id;
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT id FROM professor WHERE pessoa_id=910001")) { r.next();id=r.getLong(1); }
        var anterior=repositorio.consultar(id).orElseThrow(); assertEquals("Mestrado",anterior.titulacao());
        repositorio.atualizar(anterior,"Mestre",SituacaoFuncional.ATIVO,7);
        assertEquals("Mestre",new RepositorioProfessoresMysql(banco).consultar(id).orElseThrow().titulacao());
        assertEquals(1,contar("SELECT COUNT(*) FROM professor_auditoria WHERE professor_id="+id+" AND titulacao_anterior='Mestrado' AND titulacao_nova='Mestre'"));
    }
    @Test void cadastroConsultaAtualizacaoEAuditoriaEmNovaConexao() throws Exception {
        var p=repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7);
        assertEquals(p,new RepositorioProfessoresMysql(banco).consultar(p.id()).orElseThrow());
        var novo=repositorio.atualizar(p,"Doutor",SituacaoFuncional.INATIVO,8);
        assertEquals(novo,repositorio.consultar(p.id()).orElseThrow());
        assertEquals(1,contar("SELECT COUNT(*) FROM professor_auditoria WHERE professor_id="+p.id()+" AND usuario_id=8 AND titulacao_anterior='Mestre' AND titulacao_nova='Doutor' AND situacao_anterior='ATIVO' AND situacao_nova='INATIVO'"));
    }
    @Test void unicidadeEFkReais() {
        repositorio.cadastrar(910001,"Bacharel",SituacaoFuncional.ATIVO,7);
        assertEquals(FalhaProfessor.Codigo.PROFESSOR_JA_CADASTRADO,assertThrows(FalhaProfessor.class,()->repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7)).codigo());
        assertEquals(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA,assertThrows(FalhaProfessor.class,()->repositorio.cadastrar(919999,"Mestre",SituacaoFuncional.ATIVO,7)).codigo());
    }
    @Test void consultaEAtualizacaoAusentes() {
        assertTrue(repositorio.consultar(919999).isEmpty());
        assertEquals(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO,assertThrows(FalhaProfessor.class,()->repositorio.atualizar(new Professor(919999,910001,"Mestre",SituacaoFuncional.ATIVO),"Doutor",SituacaoFuncional.ATIVO,7)).codigo());
    }
    @Test void falhaAuditoriaReverteCadastroEAtualizacao() throws Exception {
        var p=repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7);
        try(var c=banco.abrir();var s=c.createStatement()) { s.execute("CREATE TRIGGER teste_falha_professor BEFORE INSERT ON professor_auditoria FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Falha de teste'"); }
        assertThrows(PersistenciaAcademicaException.class,()->repositorio.atualizar(p,"Doutor",SituacaoFuncional.INATIVO,7));
        assertEquals(p,repositorio.consultar(p.id()).orElseThrow());
        assertThrows(PersistenciaAcademicaException.class,()->repositorio.cadastrar(910002,"Mestre",SituacaoFuncional.ATIVO,7));
        assertEquals(0,contar("SELECT COUNT(*) FROM professor WHERE pessoa_id=910002"));
    }
    @Test void recusaAtualizacaoDesatualizada() {
        var p=repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7);
        var novo=repositorio.atualizar(p,"Doutor",SituacaoFuncional.ATIVO,7);
        assertEquals(FalhaProfessor.Codigo.CONFLITO_EDICAO,assertThrows(FalhaProfessor.class,()->repositorio.atualizar(p,"Bacharel",SituacaoFuncional.INATIVO,7)).codigo());
        assertEquals(novo,repositorio.consultar(p.id()).orElseThrow());
    }
    @Test void concorrenciaCriaUmVinculo() throws Exception {
        CountDownLatch inicio=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            Callable<String> tarefa=()->{ inicio.await(); try { repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7); return "CRIADO"; } catch(FalhaProfessor e) { return e.codigo().name(); } };
            var a=executor.submit(tarefa);var b=executor.submit(tarefa);inicio.countDown();
            var respostas=java.util.List.of(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
            assertTrue(respostas.contains("CRIADO"));assertTrue(respostas.contains("PROFESSOR_JA_CADASTRADO"));
        }
        assertEquals(1,contar("SELECT COUNT(*) FROM professor WHERE pessoa_id=910001"));
    }
    @Test void bancoRecusaTitulacaoVaziaESituacaoInvalida() throws Exception {
        try(var c=banco.abrir();var s=c.createStatement()) {
            assertThrows(java.sql.SQLException.class,()->s.executeUpdate("INSERT INTO professor(pessoa_id,titulacao,situacao_funcional) VALUES(910001,'','ATIVO')"));
            assertThrows(java.sql.SQLException.class,()->s.executeUpdate("INSERT INTO professor(pessoa_id,titulacao,situacao_funcional) VALUES(910001,'Mestre','OUTRA')"));
        }
    }
    @Test void reaplicarMigracaoPreservaRegistro() {
        var p=repositorio.cadastrar(910001,"Mestre",SituacaoFuncional.ATIVO,7);
        assertEquals(0,MigracoesAcademico.configurar(banco).migrate().migrationsExecuted);
        assertEquals(p,repositorio.consultar(p.id()).orElseThrow());
    }
    private long contar(String sql) throws Exception { try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery(sql)) { r.next();return r.getLong(1); } }
}
