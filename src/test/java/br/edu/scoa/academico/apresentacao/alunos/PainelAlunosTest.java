package br.edu.scoa.academico.apresentacao.alunos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelAlunosTest {
    final CenarioAlunos cenario = new CenarioAlunos();
    final DialogosTeste dialogos = new DialogosTeste();
    PainelAlunos painel;

    void criar() throws Exception {
        SwingUtilities.invokeAndWait(() -> painel = new PainelAlunos(cenario.controlador(), dialogos,
                "DEMONSTRAÇÃO EM MEMÓRIA · operador 7 · pessoas 42 e 43"));
    }
    @Test void cadastraEExibeIdentificadorGerado() throws Exception {
        criar();
        SwingUtilities.invokeAndWait(() -> { texto("pessoaId").setText("42"); botao("cadastrar").doClick(); });
        concluir();
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("1", texto("alunoId").getText());
            assertEquals("42", texto("pessoaId").getText());
            assertTrue(botao("cadastrar").isEnabled());
        });
        assertNull(dialogos.falha);
        assertEquals(1, cenario.escritas);
    }
    @Test void erroMantemEntradaELiberaFormulario() throws Exception {
        criar();
        SwingUtilities.invokeAndWait(() -> { texto("pessoaId").setText("abc"); botao("cadastrar").doClick(); });
        concluir();
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("abc", texto("pessoaId").getText());
            assertTrue(botao("cadastrar").isEnabled());
        });
        assertNotNull(dialogos.falha);
        assertEquals(0, cenario.escritas);
    }
    @Test void cancelarAlteracaoNaoGrava() throws Exception {
        criar();
        SwingUtilities.invokeAndWait(() -> { texto("alunoId").setText("1"); botao("atualizar").doClick(); });
        assertEquals(1, dialogos.confirmacoes);
        assertEquals(0, cenario.escritas);
    }
    @Test void consultaEAtualizaSemAlterarPessoa() throws Exception {
        cenario.controlador().cadastrar("42", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        criar();
        SwingUtilities.invokeAndWait(() -> { texto("alunoId").setText("1"); botao("consultar").doClick(); });
        concluir();
        dialogos.concluido = new CountDownLatch(1);
        dialogos.confirmar = true;
        SwingUtilities.invokeAndWait(() -> {
            ((JComboBox<?>) componente(painel, "situacao")).setSelectedItem(
                    br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.INATIVO);
            botao("atualizar").doClick();
        });
        concluir();
        assertEquals(br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.INATIVO,
                cenario.consultar(1).orElseThrow().situacaoCadastral());
        assertEquals(42, cenario.consultar(1).orElseThrow().pessoaId());
    }
    @Test void operacaoNaoBloqueiaThreadGraficaEImpedeDuploClique() throws Exception {
        CountDownLatch iniciou = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        cenario.antesDeGravar = () -> {
            assertFalse(SwingUtilities.isEventDispatchThread());
            iniciou.countDown();
            try { if (!liberar.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Teste expirou"); }
            catch (InterruptedException falha) { Thread.currentThread().interrupt(); throw new IllegalStateException(falha); }
        };
        criar();
        try {
            SwingUtilities.invokeAndWait(() -> { texto("pessoaId").setText("42"); botao("cadastrar").doClick(); });
            assertTrue(iniciou.await(5, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(botao("cadastrar").isEnabled());
                botao("cadastrar").doClick();
                assertFalse(texto("pessoaId").isEnabled());
            });
        } finally { liberar.countDown(); }
        concluir();
        assertEquals(1, cenario.escritas);
    }
    @Test void geraPreviaDaTela() throws Exception {
        criar();
        SwingUtilities.invokeAndWait(() -> {
            painel.setSize(painel.getPreferredSize());
            organizar(painel);
            BufferedImage previa = new BufferedImage(painel.getWidth(), painel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D pincel = previa.createGraphics();
            try {
                painel.printAll(pincel);
                Path destino = Path.of("target", "previa-alunos.png");
                Files.createDirectories(destino.getParent());
                ImageIO.write(previa, "png", destino.toFile());
            } catch (java.io.IOException falha) { throw new IllegalStateException(falha); }
            finally { pincel.dispose(); }
        });
    }
    void concluir() throws Exception {
        assertTrue(dialogos.concluido.await(5, TimeUnit.SECONDS), "Resposta da interface não recebida");
        SwingUtilities.invokeAndWait(() -> { });
    }
    JTextField texto(String nome) { return (JTextField) componente(painel, nome); }
    JButton botao(String nome) { return (JButton) componente(painel, nome); }
    static Component componente(Container pai, String nome) {
        for (Component filho : pai.getComponents()) {
            if (nome.equals(filho.getName())) return filho;
            if (filho instanceof Container grupo) {
                Component encontrado = componente(grupo, nome);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    static void organizar(Container pai) {
        pai.doLayout();
        for (Component filho : pai.getComponents()) if (filho instanceof Container grupo) organizar(grupo);
    }
    static class DialogosTeste implements DialogosAluno {
        CountDownLatch concluido = new CountDownLatch(1);
        String falha;
        boolean confirmar;
        int confirmacoes;
        public void informar(Component origem, String mensagem) {
            assertTrue(SwingUtilities.isEventDispatchThread()); concluido.countDown();
        }
        public void erro(Component origem, String mensagem) {
            assertTrue(SwingUtilities.isEventDispatchThread()); falha = mensagem; concluido.countDown();
        }
        public boolean confirmar(Component origem, String mensagem) { confirmacoes++; return confirmar; }
    }
}
