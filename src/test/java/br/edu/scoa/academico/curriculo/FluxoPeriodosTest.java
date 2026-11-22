package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoPeriodos;
import javax.swing.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.curriculo.FluxoCatalogoTest.*;
class FluxoPeriodosTest {
    @Test void horarioInexistenteNoFusoNaoEhCorrigidoSilenciosamente()throws Exception{
        var c=new CenarioPeriodos();var d=new Dialogos();SwingUtilities.invokeAndWait(()->{
            var p=new FluxoPeriodos(c.gestao,d);preencherCadastro(p);clicar(p,"corrigirPeriodo");clicar(p,"continuarMatricula");
            preencher(p,"inicioMatricula","04/11/2018 00:30");preencher(p,"fimMatricula","04/11/2018 03:00");clicar(p,"continuarInscricao");
            assertNotNull(d.erro);assertTrue(d.erro.contains("America/Sao_Paulo"));assertEquals(0,c.gravacoes);
        });
    }
    @Test void consultaEdicaoEAberturaEncerramentoEmTelasSeparadas()throws Exception{
        var c=new CenarioPeriodos();var salvo=c.gestao.salvar(PeriodoLetivoTest.novo(),java.util.UUID.randomUUID());
        var p=new FluxoPeriodos[1];c.acessouNaEdt=false;
        SwingUtilities.invokeAndWait(()->{p[0]=new FluxoPeriodos(c.gestao,new Dialogos());clicar(p[0],"consultarPeriodos");clicar(p[0],"pesquisarPeriodos");});aguardar(p[0]);
        SwingUtilities.invokeAndWait(()->{((JList<?>)encontrar(p[0],"resultadosPeriodo")).setSelectedIndex(0);clicar(p[0],"abrirFichaPeriodo");});aguardar(p[0]);
        SwingUtilities.invokeAndWait(()->{clicar(p[0],"editarPeriodo");preencher(p[0],"descricaoPeriodo","Alterado");clicar(p[0],"continuarMatricula");clicar(p[0],"continuarInscricao");clicar(p[0],"revisarPeriodo");clicar(p[0],"salvarPeriodo");});aguardar(p[0]);
        assertEquals("Alterado",c.gestao.consultar(salvo.id()).descricao());
        SwingUtilities.invokeAndWait(()->{clicar(p[0],"abrirPeriodo");clicar(p[0],"cancelarSituacaoPeriodo");});assertEquals(2,c.gravacoes);
        SwingUtilities.invokeAndWait(()->{clicar(p[0],"abrirPeriodo");clicar(p[0],"confirmarSituacaoPeriodo");clicar(p[0],"confirmarSituacaoPeriodo");});aguardar(p[0]);
        assertEquals(br.edu.scoa.academico.dominio.curriculo.SituacaoPeriodoLetivo.ABERTO,c.gestao.consultar(salvo.id()).situacao());
        SwingUtilities.invokeAndWait(()->{clicar(p[0],"encerrarPeriodo");clicar(p[0],"confirmarSituacaoPeriodo");});aguardar(p[0]);
        assertEquals(4,c.gravacoes);assertFalse(c.acessouNaEdt);
        SwingUtilities.invokeAndWait(()->{assertFalse(encontrar(p[0],"editarPeriodo").isEnabled());assertFalse(encontrar(p[0],"abrirPeriodo").isEnabled());assertFalse(encontrar(p[0],"encerrarPeriodo").isEnabled());});
    }
    @Test void mensagensDiferenciamMatriculaDeInscricao()throws Exception{
        var c=new CenarioPeriodos();var d=new Dialogos();SwingUtilities.invokeAndWait(()->{
            var p=new FluxoPeriodos(c.gestao,d);preencherCadastro(p);clicar(p,"corrigirPeriodo");clicar(p,"continuarMatricula");
            preencher(p,"fimMatricula","31/12/2026 18:00");clicar(p,"continuarInscricao");assertTrue(d.erro.contains("matrícula"));
            preencher(p,"fimMatricula","31/01/2027 18:00");clicar(p,"continuarInscricao");preencher(p,"fimInscricao","31/02/2027 18:00");clicar(p,"revisarPeriodo");assertTrue(d.erro.contains("inscrição"));assertEquals(0,c.gravacoes);
        });
    }
    static void preencherCadastro(FluxoPeriodos p){
        clicar(p,"novoPeriodo");preencher(p,"descricaoPeriodo","2027.1");preencher(p,"inicioAtividades","01/02/2027");preencher(p,"fimAtividades","30/06/2027");clicar(p,"continuarMatricula");
        preencher(p,"inicioMatricula","01/01/2027 09:00");preencher(p,"fimMatricula","31/01/2027 18:00");clicar(p,"continuarInscricao");
        preencher(p,"inicioInscricao","15/01/2027 09:00");preencher(p,"fimInscricao","10/02/2027 18:00");clicar(p,"revisarPeriodo");
    }
    @Test void revisaoECancelamentoNaoGravam()throws Exception{
        var c=new CenarioPeriodos();SwingUtilities.invokeAndWait(()->{
            var p=new FluxoPeriodos(c.gestao,new Dialogos());preencherCadastro(p);assertEquals(0,c.gravacoes);clicar(p,"cancelarRevisaoPeriodo");assertEquals(0,c.gravacoes);
        });
    }
    @Test void cadastroSoGravaUmaVezForaDaEdt()throws Exception{
        var c=new CenarioPeriodos();var p=new FluxoPeriodos[1];
        SwingUtilities.invokeAndWait(()->{p[0]=new FluxoPeriodos(c.gestao,new Dialogos());preencherCadastro(p[0]);clicar(p[0],"salvarPeriodo");clicar(p[0],"salvarPeriodo");});
        aguardar(p[0]);assertEquals(1,c.gravacoes);assertFalse(c.acessouNaEdt);
        var salvo=c.gestao.pesquisar("",null).getFirst();assertEquals(java.time.Instant.parse("2027-01-01T12:00:00Z"),salvo.inicioMatricula());
        SwingUtilities.invokeAndWait(()->{
            p[0].setSize(800,650);distribuir(p[0]);
            try{var img=new java.awt.image.BufferedImage(800,650,java.awt.image.BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();p[0].printAll(g);g.dispose();javax.imageio.ImageIO.write(img,"png",new java.io.File("target/aca02-periodo.png"));}catch(java.io.IOException e){throw new RuntimeException(e);}
        });
    }
    @Test void dataInvalidaNaoAvanca()throws Exception{
        var c=new CenarioPeriodos();var d=new Dialogos();SwingUtilities.invokeAndWait(()->{
            var p=new FluxoPeriodos(c.gestao,d);clicar(p,"novoPeriodo");preencher(p,"descricaoPeriodo","2027");preencher(p,"inicioAtividades","31/02/2027");preencher(p,"fimAtividades","30/06/2027");clicar(p,"continuarMatricula");assertNotNull(d.erro);assertEquals(0,c.gravacoes);
        });
    }
    static void aguardar(FluxoPeriodos p)throws Exception{
        long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        while(System.nanoTime()<limite){var pronto=new boolean[1];SwingUtilities.invokeAndWait(()->pronto[0]=((JLabel)encontrar(p,"estadoPeriodo")).getText().equals("Pronto."));if(pronto[0])return;Thread.sleep(20);}fail("Operação não terminou");
    }
}
