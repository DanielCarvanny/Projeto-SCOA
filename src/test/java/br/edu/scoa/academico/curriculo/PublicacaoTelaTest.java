package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.PainelPublicacaoMatriz;
import br.edu.scoa.academico.dominio.curriculo.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.curriculo.FluxoCatalogoTest.*;
class PublicacaoTelaTest {
    @Test void cancelarNaoPublica()throws Exception{
        var c=new CenarioMatrizes();var matriz=c.gestao.salvar(GestaoMatrizesTest.nova(c));
        SwingUtilities.invokeAndWait(()->{
            var painel=new PainelPublicacaoMatriz(c.gestao,matriz,"Composição de teste",false,new Dialogos(),m->{},()->{});
            clicar(painel,"cancelarPublicacao");
        });
        assertEquals(1,c.gravacoes);assertEquals(SituacaoMatriz.EM_ELABORACAO,c.gestao.consultar(matriz.id()).situacao());
    }
    @Test void publicaUmaVezForaDaEdt()throws Exception{
        var c=new CenarioMatrizes();var matriz=c.gestao.salvar(GestaoMatrizesTest.nova(c));
        var concluido=new java.util.concurrent.CountDownLatch(1);c.acessouNaEdt=false;
        SwingUtilities.invokeAndWait(()->{
            var painel=new PainelPublicacaoMatriz(c.gestao,matriz,"Curso: ADS\nComponente: POO, 80 h\nSem pré-requisitos",false,new Dialogos(),m->concluido.countDown(),()->{});
            painel.setSize(800,650);distribuir(painel);
            try{var imagem=new java.awt.image.BufferedImage(800,650,java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g=imagem.createGraphics();painel.printAll(g);g.dispose();
                javax.imageio.ImageIO.write(imagem,"png",new java.io.File("target/aca02-publicacao.png"));
            }catch(java.io.IOException erro){throw new RuntimeException(erro);}
            clicar(painel,"confirmarPublicacao");clicar(painel,"confirmarPublicacao");
        });
        assertTrue(concluido.await(5,java.util.concurrent.TimeUnit.SECONDS));assertEquals(2,c.gravacoes);assertFalse(c.acessouNaEdt);
    }
    @Test void copiaExigeRevisaoEDataValidaAntesDeGravar()throws Exception{
        var c=new CenarioMatrizes();var matriz=c.gestao.publicar(c.gestao.salvar(GestaoMatrizesTest.nova(c)));
        var d=new Dialogos();var concluido=new java.util.concurrent.CountDownLatch(1);
        SwingUtilities.invokeAndWait(()->{
            var p=new PainelPublicacaoMatriz(c.gestao,matriz,"POO 80 h",true,d,m->concluido.countDown(),()->{});
            preencher(p,"novaVersao","2028");preencher(p,"novoInicio","31/02/2028");clicar(p,"revisarCopia");
            assertNotNull(d.erro);assertEquals(2,c.gravacoes);
            preencher(p,"novoInicio","01/01/2028");clicar(p,"revisarCopia");assertEquals(2,c.gravacoes);
            clicar(p,"confirmarCopia");
        });
        assertTrue(concluido.await(5,java.util.concurrent.TimeUnit.SECONDS));assertEquals(3,c.gravacoes);
        assertEquals(SituacaoMatriz.VIGENTE,c.gestao.consultar(matriz.id()).situacao());
    }
    @Test void alunoNaoPublicaNemCopia(){
        var c=new CenarioMatrizes();var matriz=c.gestao.salvar(GestaoMatrizesTest.nova(c));
        c.catalogo.conta=new br.edu.scoa.academico.dominio.identidade.Conta(7,3,"coord",br.edu.scoa.academico.dominio.identidade.Perfil.ALUNO,true,1);
        assertThrows(br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.class,()->c.gestao.publicar(matriz));
        assertThrows(br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.class,()->c.gestao.copiar(matriz,"ADS","2028",java.time.LocalDate.of(2028,1,1),null));
    }
}
