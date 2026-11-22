package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.PainelPreRequisitos;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.util.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.curriculo.FluxoCatalogoTest.*;
class PreRequisitosTelaTest {
    @Test void salvaUmaVezForaDaThreadGraficaEGeraPrevia()throws Exception{
        var c=new CenarioMatrizes();var outra=c.catalogo.gestao.salvar(TipoCatalogo.DISCIPLINA,null,"P2","Programação II","Objetos",80);
        var base=GestaoMatrizesTest.nova(c);
        var salva=c.gestao.salvar(new MatrizCurricular(0,base.cursoId(),base.codigo(),base.versao(),base.inicioVigencia(),null,base.situacao(),0,
            List.of(base.componentes().getFirst(),new ComponenteCurricular(outra.id(),1,TipoComponente.OBRIGATORIO,2,80))));
        c.acessouNaEdt=false;
        var concluido=new java.util.concurrent.CountDownLatch(1);var d=new Dialogos();
        SwingUtilities.invokeAndWait(()->{
            var p=new PainelPreRequisitos(c.gestao,salva,List.of(c.disciplina,outra),d,m->concluido.countDown(),()->{});
            ((JComboBox<?>)encontrar(p,"disciplinaCursar")).setSelectedIndex(1);
            ((JComboBox<?>)encontrar(p,"disciplinaExigida")).setSelectedIndex(0);
            clicar(p,"adicionarExigencia");clicar(p,"revisarExigencias");
            try{
                p.setSize(800,650);distribuir(p);
                var imagem=new java.awt.image.BufferedImage(800,650,java.awt.image.BufferedImage.TYPE_INT_RGB);
                var desenho=imagem.createGraphics();p.printAll(desenho);desenho.dispose();
                javax.imageio.ImageIO.write(imagem,"png",new java.io.File("target/aca02-pre-requisitos.png"));
            }catch(java.io.IOException erro){throw new RuntimeException(erro);}
            clicar(p,"salvarExigencias");clicar(p,"salvarExigencias");
        });
        assertTrue(concluido.await(5,java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(2,c.gravacoes);assertFalse(c.acessouNaEdt);
        assertEquals(1,c.gestao.consultar(salva.id()).preRequisitos().size());assertNull(d.erro);
    }
    @Test void servicoRecusaPerfilSemPermissaoMesmoComRelacoesValidas(){
        var c=new CenarioMatrizes();
        c.catalogo.conta=new br.edu.scoa.academico.dominio.identidade.Conta(7,3,"coord",
            br.edu.scoa.academico.dominio.identidade.Perfil.ALUNO,true,1);
        assertThrows(br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.class,()->c.gestao.salvar(GestaoMatrizesTest.nova(c)));
        assertEquals(0,c.gravacoes);
    }
    @Test void cancelarNaoGravaEFiltraPropriaDisciplina()throws Exception{
        var c=new CenarioMatrizes();var outra=c.catalogo.gestao.salvar(TipoCatalogo.DISCIPLINA,null,"P2","Programação II","Objetos",80);
        var base=GestaoMatrizesTest.nova(c);
        var matriz=new MatrizCurricular(base.id(),base.cursoId(),base.codigo(),base.versao(),base.inicioVigencia(),null,base.situacao(),0,
            List.of(base.componentes().getFirst(),new ComponenteCurricular(outra.id(),1,TipoComponente.OBRIGATORIO,2,80)));
        var salva=c.gestao.salvar(matriz);
        SwingUtilities.invokeAndWait(()->{
            var painel=new PainelPreRequisitos(c.gestao,salva,List.of(c.disciplina,outra),new Dialogos(),m->{},()->{});
            ((JComboBox<?>)encontrar(painel,"disciplinaCursar")).setSelectedIndex(1);
            var exigidas=(JComboBox<?>)encontrar(painel,"disciplinaExigida");
            assertEquals(1,exigidas.getItemCount());exigidas.setSelectedIndex(0);
            clicar(painel,"adicionarExigencia");clicar(painel,"revisarExigencias");clicar(painel,"cancelarExigencias");
        });
        assertEquals(1,c.gravacoes);assertTrue(c.gestao.consultar(salva.id()).preRequisitos().isEmpty());
    }
}
