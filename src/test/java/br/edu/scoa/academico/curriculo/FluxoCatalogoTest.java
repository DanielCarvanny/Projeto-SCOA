package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoCatalogo;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import java.awt.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluxoCatalogoTest {
    @Test void formularioMantemCamposVisiveisNaJanelaInicial() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                var painel = new FluxoCatalogo(new CenarioCatalogo().gestao, TipoCatalogo.DISCIPLINA, new Dialogos());
                painel.setSize(800, 650); clicar(painel, "cadastrar");
                distribuir(painel);
                for (String campo : new String[]{"codigo", "nome", "carga", "ementa", "revisar"}) {
                    var componente = encontrar(painel, campo);
                    assertTrue(componente.getWidth() > 0 && componente.getHeight() > 0, campo);
                }
                var imagem = new java.awt.image.BufferedImage(800, 650, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var desenho = imagem.createGraphics(); painel.printAll(desenho); desenho.dispose();
                javax.imageio.ImageIO.write(imagem, "png", new java.io.File("target/aca02-disciplina.png"));
            } catch (java.io.IOException erro) { throw new RuntimeException(erro); }
        });
    }
    static void distribuir(Container raiz) {
        raiz.doLayout();
        for (var filho : raiz.getComponents()) if (filho instanceof Container grupo) distribuir(grupo);
    }
    static final class Dialogos implements DialogosAluno {
        String erro; int informacoes;
        public void informar(Component origem, String mensagem) { informacoes++; }
        public void erro(Component origem, String mensagem) { erro = mensagem; }
        public boolean confirmar(Component origem, String mensagem) { return true; }
    }
    static Component encontrar(Container raiz, String nome) {
        for (var filho : raiz.getComponents()) {
            if (nome.equals(filho.getName())) return filho;
            if (filho instanceof Container grupo) { var achado = encontrar(grupo, nome); if (achado != null) return achado; }
        }
        return null;
    }
    static void clicar(Container raiz, String nome) { ((JButton) encontrar(raiz, nome)).doClick(); }
    static void preencher(Container raiz, String nome, String valor) { ((javax.swing.text.JTextComponent) encontrar(raiz, nome)).setText(valor); }
    @Test void cancelarRevisaoNaoGrava() throws Exception {
        var c = new CenarioCatalogo(); var d = new Dialogos();
        SwingUtilities.invokeAndWait(() -> {
            var painel = new FluxoCatalogo(c.gestao, TipoCatalogo.CURSO, d);
            clicar(painel, "cadastrar"); preencher(painel, "codigo", "ADS"); preencher(painel, "nome", "Sistemas"); preencher(painel, "carga", "2400");
            clicar(painel, "revisar"); clicar(painel, "cancelarRevisao");
            assertEquals(0, c.gravacoes);
        });
    }
    @Test void cargaInvalidaNaoConsultaBanco() throws Exception {
        var c = new CenarioCatalogo(); var d = new Dialogos();
        SwingUtilities.invokeAndWait(() -> {
            var painel = new FluxoCatalogo(c.gestao, TipoCatalogo.DISCIPLINA, d);
            clicar(painel, "cadastrar"); preencher(painel, "codigo", "POO"); preencher(painel, "nome", "Objetos");
            preencher(painel, "ementa", "Classes"); preencher(painel, "carga", "abc"); clicar(painel, "revisar");
            assertNotNull(d.erro); assertTrue(d.erro.contains("carga")); assertEquals(0, c.gravacoes);
        });
    }
    @Test void confirmaUmaVezForaDaEdt() throws Exception {
        var c = new CenarioCatalogo(); var d = new Dialogos(); var painel = new FluxoCatalogo[1];
        SwingUtilities.invokeAndWait(() -> {
            painel[0] = new FluxoCatalogo(c.gestao, TipoCatalogo.DISCIPLINA, d);
            clicar(painel[0], "cadastrar"); preencher(painel[0], "codigo", "POO"); preencher(painel[0], "nome", "Objetos");
            preencher(painel[0], "ementa", "Classes"); preencher(painel[0], "carga", "80");
            clicar(painel[0], "revisar"); clicar(painel[0], "confirmar"); clicar(painel[0], "confirmar");
        });
        long limite = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        var terminou = new boolean[1];
        while (!terminou[0] && System.nanoTime() < limite) {
            SwingUtilities.invokeAndWait(() -> terminou[0] = d.informacoes == 1); Thread.sleep(20);
        }
        assertTrue(terminou[0]); assertEquals(1, c.gravacoes); assertFalse(c.acessouNaEdt);
    }
}

