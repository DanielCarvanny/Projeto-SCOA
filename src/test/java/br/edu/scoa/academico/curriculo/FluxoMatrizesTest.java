package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoMatrizes;
import javax.swing.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.curriculo.FluxoCatalogoTest.*;

class FluxoMatrizesTest {
    static FluxoMatrizes abrirFicha(CenarioMatrizes c,Dialogos d)throws Exception{
        var painel=new FluxoMatrizes[1];
        SwingUtilities.invokeAndWait(()->{painel[0]=new FluxoMatrizes(c.gestao,d);clicar(painel[0],"consultarMatrizes");});
        aguardar(painel[0]);
        SwingUtilities.invokeAndWait(()->clicar(painel[0],"pesquisarMatriz"));aguardar(painel[0]);
        SwingUtilities.invokeAndWait(()->{((JList<?>)encontrar(painel[0],"resultadosMatriz")).setSelectedIndex(0);clicar(painel[0],"abrirMatriz");});
        aguardar(painel[0]);return painel[0];
    }
    @Test void recusaRemoverUltimoNaFichaECancelamentoDescartaEdicao()throws Exception{
        var c=new CenarioMatrizes();var salva=c.gestao.salvar(GestaoMatrizesTest.nova(c));var d=new Dialogos();var p=abrirFicha(c,d);
        SwingUtilities.invokeAndWait(()->{
            ((JList<?>)encontrar(p,"componentesFicha")).setSelectedIndex(0);clicar(p,"removerNaFicha");assertNotNull(d.erro);
            clicar(p,"editarDadosMatriz");preencher(p,"codigoMatriz","ALTERADO");clicar(p,"cancelarDadosMatriz");
        });aguardar(p);
        assertEquals(salva,c.gestao.consultar(salva.id()));assertEquals(1,c.gravacoes);
    }
    @Test void atualizarConteudoExigeAcaoExplicitaEPreservaCargaConsiderada()throws Exception{
        var c=new CenarioMatrizes();var salva=c.gestao.salvar(GestaoMatrizesTest.nova(c));
        c.catalogo.gestao.salvar(br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.DISCIPLINA,c.disciplina,
            "POO-DEMO","Objetos","Classes e interfaces",100);
        var d=new Dialogos();var p=abrirFicha(c,d);
        SwingUtilities.invokeAndWait(()->{((JList<?>)encontrar(p,"componentesFicha")).setSelectedIndex(0);clicar(p,"editarNaFicha");});aguardar(p);
        SwingUtilities.invokeAndWait(()->{
            var atual=(br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo)((JComboBox<?>)encontrar(p,"disciplina")).getSelectedItem();
            assertEquals(1,atual.versao());assertEquals("80",((JTextField)encontrar(p,"cargaComponente")).getText());
            clicar(p,"atualizarRevisao");
        });aguardar(p);
        SwingUtilities.invokeAndWait(()->{
            var atual=(br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo)((JComboBox<?>)encontrar(p,"disciplina")).getSelectedItem();
            assertEquals(2,atual.versao());clicar(p,"aplicarComponente");clicar(p,"revisarMatriz");clicar(p,"confirmarMatriz");
        });aguardar(p);
        var componente=c.gestao.consultar(salva.id()).componentes().getFirst();
        assertEquals(2,componente.disciplinaRevisao());assertEquals(80,componente.cargaHorariaConsiderada());assertNull(d.erro);
    }
    @Test void capturaFichaParaConferenciaVisual()throws Exception{
        var c=new CenarioMatrizes();c.gestao.salvar(GestaoMatrizesTest.nova(c));var p=abrirFicha(c,new Dialogos());
        SwingUtilities.invokeAndWait(()->{
            try{
                p.setSize(800,650);FluxoCatalogoTest.distribuir(p);
                var imagem=new java.awt.image.BufferedImage(800,650,java.awt.image.BufferedImage.TYPE_INT_RGB);
                var desenho=imagem.createGraphics();p.printAll(desenho);desenho.dispose();
                javax.imageio.ImageIO.write(imagem,"png",new java.io.File("target/aca02-matriz.png"));
                assertTrue(encontrar(p,"editarDadosMatriz").getHeight()>0);
            }catch(java.io.IOException erro){throw new RuntimeException(erro);}
        });
    }
    static void aguardar(FluxoMatrizes painel)throws Exception{
        long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);var pronto=new boolean[1];
        while(!pronto[0]&&System.nanoTime()<limite){
            SwingUtilities.invokeAndWait(()->pronto[0]=((JLabel)encontrar(painel,"estadoMatriz")).getText().equals("Pronto."));
            Thread.sleep(20);
        }
        assertTrue(pronto[0]);
    }
    static FluxoMatrizes iniciar(CenarioMatrizes c,Dialogos d)throws Exception{
        var ref=new FluxoMatrizes[1];
        SwingUtilities.invokeAndWait(()->{ref[0]=new FluxoMatrizes(c.gestao,d);clicar(ref[0],"novaMatriz");});
        aguardar(ref[0]);
        SwingUtilities.invokeAndWait(()->{
            ((JComboBox<?>)encontrar(ref[0],"curso")).setSelectedIndex(0);clicar(ref[0],"selecionarCurso");
            preencher(ref[0],"codigoMatriz","M-ADS");preencher(ref[0],"versaoMatriz","2027.1");
            preencher(ref[0],"inicioVigencia","01/01/2027");clicar(ref[0],"montarComponentes");
        });
        return ref[0];
    }
    @Test void naoConfirmaMatrizVaziaENaoGravaAoCancelar()throws Exception{
        var c=new CenarioMatrizes();var d=new Dialogos();var p=iniciar(c,d);
        SwingUtilities.invokeAndWait(()->{
            clicar(p,"revisarMatriz");assertNotNull(d.erro);assertEquals(0,c.gravacoes);clicar(p,"cancelarMontagem");
        });
    }
    @Test void criaSomenteNaConfirmacaoComUmComponente()throws Exception{
        var c=new CenarioMatrizes();var d=new Dialogos();var p=iniciar(c,d);
        SwingUtilities.invokeAndWait(()->clicar(p,"adicionarComponente"));aguardar(p);
        SwingUtilities.invokeAndWait(()->{
            ((JComboBox<?>)encontrar(p,"disciplina")).setSelectedIndex(0);
            ((JComboBox<?>)encontrar(p,"tipoComponente")).setSelectedIndex(0);
            preencher(p,"periodoIdeal","1");preencher(p,"cargaComponente","80");
            clicar(p,"aplicarComponente");clicar(p,"revisarMatriz");assertEquals(0,c.gravacoes);
            clicar(p,"confirmarMatriz");clicar(p,"confirmarMatriz");
        });aguardar(p);
        assertEquals(1,c.gravacoes);assertFalse(c.acessouNaEdt);assertNull(d.erro);
    }
    @Test void datasInvalidasNaoAvancam()throws Exception{
        var c=new CenarioMatrizes();var d=new Dialogos();var p=iniciar(c,d);
        SwingUtilities.invokeAndWait(()->{
            clicar(p,"voltarCabecalho");preencher(p,"inicioVigencia","31/02/2027");clicar(p,"montarComponentes");
            assertNotNull(d.erro);assertEquals(0,c.gravacoes);
        });
    }
}

