package br.edu.scoa.academico.apresentacao.alunos;
import java.awt.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluxoAlunosTest {
    final CenarioCpf cenario = new CenarioCpf();
    final PainelAlunosTest.DialogosTeste dialogos = new PainelAlunosTest.DialogosTeste();
    FluxoAlunos painel;
    void criar() throws Exception { SwingUtilities.invokeAndWait(() -> painel = new FluxoAlunos(cenario.cadastro(), cenario.gestao(), dialogos, "DEMONSTRAÇÃO EM MEMÓRIA")); }
    Component item(String nome) { return PainelAlunosTest.componente(painel, nome); }
    void clicar(String nome) throws Exception { SwingUtilities.invokeAndWait(() -> { assertNotNull(item(nome), nome); ((JButton)item(nome)).doClick(); }); }
    void preencher(String nome, String valor) throws Exception { SwingUtilities.invokeAndWait(() -> ((JTextField)item(nome)).setText(valor)); }
    void tela(String nome) throws Exception { SwingUtilities.invokeAndWait(() -> assertTrue(item(nome).isVisible(), nome)); }
    void aguardar() throws Exception {
        CountDownLatch fim = new CountDownLatch(1);
        Timer timer = new Timer(10, evento -> { if ("Pronto.".equals(((JLabel)item("estado")).getText())) fim.countDown(); });
        SwingUtilities.invokeAndWait(timer::start);
        try { assertTrue(fim.await(5, TimeUnit.SECONDS)); } finally { SwingUtilities.invokeAndWait(timer::stop); }
    }
    void buscar() throws Exception { clicar("menuCadastrar"); preencher("cpf", "52998224725"); clicar("buscarCpf"); aguardar(); }
    @Test void menuMostraSomenteEscolhas() throws Exception { criar(); tela("menu"); SwingUtilities.invokeAndWait(() -> assertFalse(item("identificar").isVisible())); }
    @Test void cadastroSoGravaNaConfirmacaoFinal() throws Exception {
        criar(); buscar(); tela("dados"); clicar("revisar"); tela("revisao"); assertEquals(0, cenario.alunos.escritas);
        clicar("confirmarCadastro"); aguardar(); tela("resultado"); assertEquals(1, cenario.alunos.escritas);
    }
    @Test void voltarIdentificacaoExigeNovaBusca() throws Exception {
        criar(); buscar(); clicar("voltarCpf"); tela("identificar"); preencher("cpf", "11144477735"); clicar("buscarCpf"); aguardar();
        SwingUtilities.invokeAndWait(() -> assertEquals("Pessoa Exemplo B", ((JTextField)item("nome")).getText()));
        clicar("cancelarCadastro"); tela("menu"); assertEquals(0, cenario.alunos.escritas);
    }
    @Test void consultaEAlteracaoPartemDoAlunoSelecionado() throws Exception {
        cenario.cadastro().cadastrar("52998224725", "", "", "", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        criar(); clicar("menuConsultar"); preencher("alunoId", "1"); clicar("buscarAluno"); aguardar(); tela("detalhes");
        clicar("editarSituacao"); tela("alteracao");
        SwingUtilities.invokeAndWait(() -> ((JComboBox<?>)item("novaSituacao")).setSelectedItem(br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.INATIVO));
        clicar("salvarSituacao"); tela("alteracao"); assertEquals(1, cenario.alunos.escritas);
        dialogos.confirmar = true; clicar("salvarSituacao"); aguardar(); tela("detalhes"); assertEquals(2, cenario.alunos.escritas);
        clicar("novaBusca"); preencher("alunoId", "999"); clicar("buscarAluno"); aguardar(); tela("consulta");
        SwingUtilities.invokeAndWait(() -> assertEquals("", ((JTextArea)item("textoDetalhes")).getText()));
    }
    @Test void erroDeCpfMantemIdentificacaoSemGravar() throws Exception { criar(); clicar("menuCadastrar"); preencher("cpf", "abc"); clicar("buscarCpf"); aguardar(); tela("identificar"); assertEquals(0, cenario.alunos.escritas); assertNotNull(dialogos.falha); }
    @Test void dadosPessoaisSaoSomenteConferenciaECancelamentoNaoGrava() throws Exception {
        criar(); buscar();
        SwingUtilities.invokeAndWait(()->{assertFalse(((JTextField)item("nome")).isEditable());assertFalse(((JTextField)item("email")).isEditable());assertFalse(((JTextField)item("telefone")).isEditable());});
        clicar("revisar");clicar("cancelarRevisao");tela("menu");
        assertEquals(0,cenario.alunos.escritas);assertEquals(2,cenario.pessoas.size());
    }
    @Test void gravaForaDaThreadGraficaEBloqueiaRepeticaoENavegacao() throws Exception {
        CountDownLatch iniciou = new CountDownLatch(1), liberar = new CountDownLatch(1);
        cenario.alunos.antesDeGravar = () -> {
            assertFalse(SwingUtilities.isEventDispatchThread()); iniciou.countDown();
            try { if (!liberar.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Tempo de teste excedido"); }
            catch (InterruptedException falha) { Thread.currentThread().interrupt(); throw new IllegalStateException(falha); }
        };
        criar(); buscar(); clicar("revisar");
        try {
            clicar("confirmarCadastro"); assertTrue(iniciou.await(5, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> { assertFalse(item("confirmarCadastro").isEnabled()); assertFalse(item("cancelarRevisao").isEnabled()); });
            clicar("confirmarCadastro");
        } finally { liberar.countDown(); }
        aguardar(); assertEquals(1, cenario.alunos.escritas);
    }
    @Test void duplicidadeNaoSaiDaRevisaoNemDuplicaAluno() throws Exception {
        cenario.cadastro().cadastrar("52998224725", "", "", "", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        criar(); buscar(); clicar("revisar"); clicar("confirmarCadastro"); aguardar(); tela("revisao");
        assertEquals(1, cenario.alunos.escritas); assertNotNull(dialogos.falha);
    }
    @Test void geraPreviasDeTodasAsEtapas() throws Exception {
        criar(); previa("menu"); clicar("menuCadastrar"); previa("identificar"); preencher("cpf", "52998224725"); clicar("buscarCpf"); aguardar();
        previa("dados"); clicar("revisar"); previa("revisao"); clicar("confirmarCadastro"); aguardar(); previa("resultado");
        clicar("resultadoMenu"); clicar("menuConsultar"); previa("consulta"); preencher("alunoId", "1"); clicar("buscarAluno"); aguardar();
        previa("detalhes"); clicar("editarSituacao"); previa("alteracao");
    }
    @Test void pessoaExistenteAusenteNaoAbreFormularioNovo() throws Exception {
        criar(); clicar("menuCadastrar"); preencher("cpf","12345678909"); clicar("buscarCpf"); aguardar();
        tela("identificar"); assertTrue(dialogos.falha.contains("Cadastre-a em Pessoas")); assertEquals(2,cenario.pessoas.size());
    }
    @Test void consultaCpfMostraFichaEUsaSomenteCampoSelecionado() throws Exception {
        cenario.cadastro().cadastrar("52998224725","","","",br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        SwingUtilities.invokeAndWait(() -> painel=new FluxoAlunos(cenario.cadastro(),cenario.gestao(),dialogos,"Teste",cenario.ficha()));
        clicar("menuConsultar");
        SwingUtilities.invokeAndWait(() -> ((JComboBox<?>)item("tipoBusca")).setSelectedItem("CPF"));
        preencher("cpfBusca","529.982.247-25"); clicar("buscarAluno"); aguardar(); tela("detalhes");
        SwingUtilities.invokeAndWait(() -> assertTrue(((JTextArea)item("textoDetalhes")).getText().contains("Pessoa Exemplo A")));
        clicar("editarPessoa"); preencher("editarNome","Não salvar"); clicar("cancelarPessoa");
        assertEquals("Pessoa Exemplo A",cenario.pessoas.get("52998224725").nome());
    }
    @Test void erroDeCpfNaoMencionaNomeOuEmail() throws Exception {
        criar(); clicar("menuCadastrar"); previa("escolha"); preencher("cpf","5299822472"); clicar("buscarCpf"); aguardar();
        assertTrue(dialogos.falha.contains("CPF inválido")); assertFalse(dialogos.falha.contains("nome")); assertFalse(dialogos.falha.contains("e-mail"));
        clicar("voltarMenu"); tela("menu");
    }
    void previa(String nome) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            painel.setSize(painel.getPreferredSize()); PainelAlunosTest.organizar(painel);
            var imagem = new java.awt.image.BufferedImage(painel.getWidth(), painel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D pincel = imagem.createGraphics();
            try { painel.printAll(pincel); javax.imageio.ImageIO.write(imagem, "png", java.nio.file.Path.of("target", "tela-" + nome + ".png").toFile()); }
            catch (java.io.IOException falha) { throw new IllegalStateException(falha); }
            finally { pincel.dispose(); }
        });
    }
}
