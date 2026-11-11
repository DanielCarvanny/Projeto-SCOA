package br.edu.scoa.academico.apresentacao.alunos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelCadastroCpfTest {
    final CenarioCpf cenario = new CenarioCpf();
    final PainelAlunosTest.DialogosTeste dialogos = new PainelAlunosTest.DialogosTeste();
    PainelCadastroCpf painel;
    void criar() throws Exception {
        SwingUtilities.invokeAndWait(() -> painel = new PainelCadastroCpf(cenario.cadastro(), cenario.gestao(), dialogos,
                "DEMONSTRAÇÃO EM MEMÓRIA · identidade simulada · dados não persistidos"));
    }
    JTextField texto(String nome) { return (JTextField) PainelAlunosTest.componente(painel, nome); }
    JButton botao(String nome) { return (JButton) PainelAlunosTest.componente(painel, nome); }
    void buscar(String cpf) throws Exception {
        SwingUtilities.invokeAndWait(() -> { texto("cpf").setText(cpf); botao("buscar").doClick(); });
        aguardar();
    }
    void aguardar() throws Exception {
        CountDownLatch terminou = new CountDownLatch(1);
        AtomicReference<Timer> relogio = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            Timer timer = new Timer(10, evento -> { if (botao("buscar").isEnabled()) terminou.countDown(); });
            relogio.set(timer); timer.start();
        });
        try { assertTrue(terminou.await(5, TimeUnit.SECONDS)); }
        finally { SwingUtilities.invokeAndWait(() -> relogio.get().stop()); }
    }
    @Test void encontraPessoaEInvalidaSelecaoQuandoCpfMuda() throws Exception {
        criar(); buscar("529.982.247-25");
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("Pessoa Exemplo A", texto("nome").getText());
            assertFalse(texto("nome").isEditable()); assertTrue(botao("cadastrar").isEnabled());
            texto("cpf").setText("11144477735");
            assertEquals("", texto("nome").getText()); assertFalse(botao("cadastrar").isEnabled());
        });
    }
    @Test void confirmaCadastroExistenteSemPedirIdInterno() throws Exception {
        criar(); buscar("52998224725"); dialogos.confirmar = true;
        SwingUtilities.invokeAndWait(() -> botao("cadastrar").doClick()); aguardar();
        SwingUtilities.invokeAndWait(() -> assertEquals("1", texto("alunoId").getText()));
        assertEquals(42, cenario.alunos.consultar(1).orElseThrow().pessoaId());
    }
    @Test void permitePessoaNovaECancelamentoNaoGrava() throws Exception {
        criar(); buscar("12345678909");
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(texto("nome").isEditable()); texto("nome").setText("Pessoa Nova Teste");
            texto("email").setText("nova@example.invalid"); botao("cadastrar").doClick();
        });
        assertEquals(2, cenario.pessoas.size()); assertEquals(0, cenario.alunos.escritas);
        dialogos.confirmar = true;
        SwingUtilities.invokeAndWait(() -> botao("cadastrar").doClick()); aguardar();
        assertEquals(3, cenario.pessoas.size()); assertEquals(1, cenario.alunos.escritas);
    }
    @Test void cpfInvalidoNaoLiberaCadastro() throws Exception {
        criar(); buscar("11111111111");
        SwingUtilities.invokeAndWait(() -> assertFalse(botao("cadastrar").isEnabled()));
        assertNotNull(dialogos.falha);
    }
    @Test void geraPrevia() throws Exception {
        criar(); buscar("52998224725");
        SwingUtilities.invokeAndWait(() -> {
            painel.setSize(painel.getPreferredSize()); PainelAlunosTest.organizar(painel);
            BufferedImage previa = new BufferedImage(painel.getWidth(), painel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D pincel = previa.createGraphics();
            try { painel.printAll(pincel); ImageIO.write(previa, "png", Path.of("target", "previa-cpf.png").toFile()); }
            catch (java.io.IOException falha) { throw new IllegalStateException(falha); }
            finally { pincel.dispose(); }
        });
    }
}
