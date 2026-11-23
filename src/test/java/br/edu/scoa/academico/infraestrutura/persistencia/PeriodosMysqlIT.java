package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.curriculo.PeriodoLetivoTest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class PeriodosMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioPeriodosMysql repo;
    static final Instant AGORA=Instant.parse("2026-11-20T12:00:00Z");
    @BeforeAll static void preparar(){
        var e=System.getenv();
        if(!"CONFIRMO_BANCO_DESCARTAVEL".equals(e.get("SCOA_TEST_CONFIRM"))||!"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(e.get("SCOA_TEST_URL"))||!"scoa_test".equals(e.get("SCOA_TEST_USER")))throw new IllegalStateException("Banco exclusivo de testes obrigatório.");
        banco=ConfiguracaoBanco.doAmbiente(e,"SCOA_TEST_");MigracoesIdentidade.executar(banco);
        var f=MigracoesAcademico.configurar(banco);if(f.info().applied().length==0)MigracoesAplicacao.inicializarHistoricoAcademico(banco);f.migrate();
    }
    @BeforeEach void iniciar()throws Exception{limpar();repo=new RepositorioPeriodosMysql(banco);}
    @AfterAll static void limpar()throws Exception{
        if(banco==null)return;try(var c=banco.abrir();var s=c.createStatement()){
            s.execute("DROP TRIGGER IF EXISTS teste_periodo_auditoria");
            s.executeUpdate("DELETE FROM horario_turma");s.executeUpdate("DELETE FROM turma_auditoria");s.executeUpdate("DELETE FROM turma");
            s.executeUpdate("DELETE FROM periodo_historico");s.executeUpdate("DELETE FROM periodo_letivo");
        }
    }
    PeriodoLetivo salvar(PeriodoLetivo p){return repo.salvar(p,UUID.randomUUID(),7,AGORA);}
    @Test void persisteConsultaFiltraEAuditaComUtc()throws Exception{
        var p=salvar(PeriodoLetivoTest.novo());assertEquals(p,new RepositorioPeriodosMysql(banco).consultar(p.id()));
        assertEquals(1,repo.pesquisar("primeiro",SituacaoPeriodoLetivo.PLANEJADO).size());assertTrue(repo.pesquisar("",SituacaoPeriodoLetivo.ABERTO).isEmpty());
        var aberto=repo.transicionar(p.id(),p.revisao(),SituacaoPeriodoLetivo.ABERTO,7,AGORA);
        assertTrue(aberto.matriculaEstaAberta(aberto.inicioMatricula()));
        var encerrado=repo.transicionar(aberto.id(),aberto.revisao(),SituacaoPeriodoLetivo.ENCERRADO,7,AGORA);
        assertEquals(SituacaoPeriodoLetivo.ENCERRADO,repo.consultar(p.id()).situacao());assertThrows(FalhaPeriodo.class,()->salvar(encerrado));
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*),MIN(autor_id),MIN(instante) FROM periodo_historico")){
            r.next();assertEquals(3,r.getInt(1));assertEquals(7,r.getLong(2));assertEquals(AGORA,r.getObject(3,LocalDateTime.class).toInstant(ZoneOffset.UTC));
        }
    }
    @Test void repeticaoDeCriacaoNaoDuplicaMasDescricaoPodeRepetir(){
        var chave=UUID.randomUUID();var p=PeriodoLetivoTest.novo();var salvo=repo.salvar(p,chave,7,AGORA);
        assertEquals(salvo,repo.salvar(p,chave,7,AGORA));assertEquals(1,repo.pesquisar("",null).size());
        salvar(p);assertEquals(2,repo.pesquisar("",null).size());
        var outro=new PeriodoLetivo(0,"Outro",p.dataInicio(),p.dataFim(),p.inicioMatricula(),p.fimMatricula(),p.inicioInscricao(),p.fimInscricao(),p.situacao(),0);
        assertEquals(FalhaPeriodo.Codigo.OPERACAO_REUTILIZADA,assertThrows(FalhaPeriodo.class,()->repo.salvar(outro,chave,7,AGORA)).codigo());
    }
    @Test void concorrenciaNaoSobrescreve()throws Exception{
        var p=salvar(PeriodoLetivoTest.novo());var largada=new CountDownLatch(1);
        Callable<Boolean> editar=()->{largada.await();try{salvar(p);return true;}catch(FalhaPeriodo erro){assertEquals(FalhaPeriodo.Codigo.CONFLITO_EDICAO,erro.codigo());return false;}};
        try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(editar);var b=pool.submit(editar);largada.countDown();assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));}
        assertEquals(2,repo.consultar(p.id()).revisao());
    }
    @Test void criacaoConcorrenteComMesmaChaveRetornaMesmoPeriodo()throws Exception{
        var chave=UUID.randomUUID();var largada=new CountDownLatch(1);
        Callable<PeriodoLetivo> criar=()->{largada.await();return repo.salvar(PeriodoLetivoTest.novo(),chave,7,AGORA);};
        try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(criar);var b=pool.submit(criar);largada.countDown();assertEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));}
        assertEquals(1,repo.pesquisar("",null).size());
    }
    @Test void auditoriaFalhaReverteCadastroETransicao()throws Exception{
        var p=salvar(PeriodoLetivoTest.novo());
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TRIGGER teste_periodo_auditoria BEFORE INSERT ON periodo_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");}
        assertThrows(FalhaPeriodo.class,()->salvar(PeriodoLetivoTest.novo()));
        assertThrows(FalhaPeriodo.class,()->repo.transicionar(p.id(),p.revisao(),SituacaoPeriodoLetivo.ABERTO,7,AGORA));
        assertEquals(p,repo.consultar(p.id()));assertEquals(1,repo.pesquisar("",null).size());
    }
    @Test void salvarNaoPodeBurlarTransicaoNemEncerramento(){
        var p=salvar(PeriodoLetivoTest.novo());assertThrows(FalhaPeriodo.class,()->salvar(p.abrir()));
        var aberto=repo.transicionar(p.id(),p.revisao(),SituacaoPeriodoLetivo.ABERTO,7,AGORA);
        repo.transicionar(aberto.id(),aberto.revisao(),SituacaoPeriodoLetivo.ENCERRADO,7,AGORA);
        assertThrows(FalhaPeriodo.class,()->salvar(aberto));
    }
}
