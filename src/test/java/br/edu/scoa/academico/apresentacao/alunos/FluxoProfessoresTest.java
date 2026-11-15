package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.apresentacao.professores.FluxoProfessores;
import br.edu.scoa.academico.dominio.professores.SituacaoFuncional;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluxoProfessoresTest {
    final CenarioProfessores cenario=new CenarioProfessores();
    final PainelAlunosTest.DialogosTeste dialogos=new PainelAlunosTest.DialogosTeste();
    FluxoProfessores painel;
    void criar() throws Exception {SwingUtilities.invokeAndWait(()->painel=new FluxoProfessores(cenario.gestao(),dialogos,"DEMONSTRAÇÃO EM MEMÓRIA · identidade simulada"));}
    Component item(String nome){return PainelAlunosTest.componente(painel,nome);}
    void clicar(String nome)throws Exception{SwingUtilities.invokeAndWait(()->((JButton)item(nome)).doClick());}
    void preencher(String nome,String texto)throws Exception{SwingUtilities.invokeAndWait(()->{ if(item(nome) instanceof JComboBox<?> combo) combo.setSelectedItem(texto); else ((JTextField)item(nome)).setText(texto); });}
    void tela(String nome)throws Exception{SwingUtilities.invokeAndWait(()->assertTrue(item(nome).isVisible(),nome));}
    void aguardar()throws Exception{
        CountDownLatch terminou=new CountDownLatch(1);
        Timer timer=new Timer(10,e->{if("Pronto.".equals(((JLabel)item("estado")).getText()))terminou.countDown();});
        SwingUtilities.invokeAndWait(timer::start);try{assertTrue(terminou.await(5,TimeUnit.SECONDS));}finally{SwingUtilities.invokeAndWait(timer::stop);}
    }
    void buscarPessoa()throws Exception{clicar("menuCadastrar");preencher("cpf","52998224725");clicar("buscarCpf");aguardar();}
    @Test void cadastroGuiadoExigeTitulacaoEGravaSoNaConfirmacao()throws Exception{
        criar();tela("menu");buscarPessoa();tela("dados");clicar("revisar");tela("dados");assertNotNull(dialogos.falha);
        preencher("titulacao","Mestre");clicar("revisar");tela("revisao");assertEquals(0,cenario.escritas);
        clicar("voltarDados");preencher("titulacao","Doutor");clicar("revisar");clicar("confirmarCadastro");aguardar();tela("resultado");
        assertEquals("Doutor",cenario.professores.get(1L).titulacao());assertEquals(1,cenario.escritas);
    }
    @Test void voltarCpfInvalidaDadosECancelarNaoGrava()throws Exception{
        criar();buscarPessoa();clicar("voltarCpf");preencher("cpf","11144477735");clicar("buscarCpf");aguardar();
        SwingUtilities.invokeAndWait(()->{assertEquals("Pessoa Exemplo B",((JTextField)item("nome")).getText());assertEquals(0,((JComboBox<?>)item("titulacao")).getSelectedIndex());});
        clicar("cancelarCadastro");tela("menu");assertEquals(0,cenario.escritas);
    }
    @Test void consultaEdicaoEContatoSaoSeparados()throws Exception{
        cenario.gestao().cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO);
        criar();clicar("menuConsultar");preencher("professorId","1");clicar("buscarProfessor");aguardar();tela("detalhes");
        clicar("editarDocente");preencher("novoTitulo","Doutor");clicar("salvarDocente");assertEquals("Mestre",cenario.professores.get(1L).titulacao());
        dialogos.confirmar=true;clicar("salvarDocente");aguardar();tela("detalhes");assertEquals("Doutor",cenario.professores.get(1L).titulacao());
        clicar("editarPessoa");preencher("novoEmail","novo@example.invalid");clicar("salvarPessoa");aguardar();tela("detalhes");
        assertEquals("novo@example.invalid",cenario.identidade.pessoas.get("52998224725").email());
        clicar("novaBusca");preencher("professorId","999");clicar("buscarProfessor");aguardar();tela("consulta");
        SwingUtilities.invokeAndWait(()->assertEquals("",((JTextArea)item("textoDetalhes")).getText()));
    }
    @Test void duplicidadeEPermissaoNegadaMantemTelaSemGravar()throws Exception{
        cenario.gestao().cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO);
        criar();buscarPessoa();preencher("titulacao","Mestre");clicar("revisar");clicar("confirmarCadastro");aguardar();tela("revisao");assertEquals(1,cenario.escritas);
        clicar("cancelarRevisao");cenario.autorizado=false;clicar("menuConsultar");preencher("professorId","1");clicar("buscarProfessor");aguardar();tela("consulta");
    }
    @Test void previasDeTodasAsTelas()throws Exception{
        criar();previa("menu");clicar("menuCadastrar");previa("identificar");preencher("cpf","52998224725");clicar("buscarCpf");aguardar();preencher("titulacao","Mestre");previa("dados");
        clicar("revisar");previa("revisao");clicar("confirmarCadastro");aguardar();previa("resultado");clicar("resultadoMenu");clicar("menuConsultar");previa("consulta");
        preencher("professorId","1");clicar("buscarProfessor");aguardar();previa("detalhes");clicar("editarDocente");previa("edicaoDocente");clicar("cancelarDocente");clicar("editarPessoa");previa("edicaoPessoa");
    }
    @Test void gravacaoForaDaThreadGraficaImpedeDuploCliqueENavegacao()throws Exception{
        var iniciou=new CountDownLatch(1);var liberar=new CountDownLatch(1);
        cenario.antesDeGravar=()->{
            assertFalse(SwingUtilities.isEventDispatchThread());iniciou.countDown();
            try{if(!liberar.await(5,TimeUnit.SECONDS))throw new IllegalStateException("Tempo excedido");}
            catch(InterruptedException erro){Thread.currentThread().interrupt();throw new IllegalStateException(erro);}
        };
        criar();buscarPessoa();preencher("titulacao","Mestre");clicar("revisar");
        try{
            clicar("confirmarCadastro");assertTrue(iniciou.await(5,TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(()->{assertFalse(item("confirmarCadastro").isEnabled());assertFalse(item("cancelarRevisao").isEnabled());});clicar("confirmarCadastro");
        }finally{liberar.countDown();}
        aguardar();assertEquals(1,cenario.escritas);
    }
    @Test void consultaPorCpfExibeFichaEDiferenciaPessoaSemVinculo()throws Exception{
        cenario.gestao().cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO);
        criar();clicar("menuConsultar");preencher("tipoBusca","CPF");
        SwingUtilities.invokeAndWait(()->{assertFalse(item("professorId").getParent().isVisible());assertTrue(item("cpfBusca").getParent().isVisible());});
        preencher("cpfBusca","529.982.247-25");clicar("buscarProfessor");aguardar();tela("detalhes");
        SwingUtilities.invokeAndWait(()->assertTrue(((JTextArea)item("textoDetalhes")).getText().contains("Pessoa Exemplo A")));previa("consulta-cpf-ficha");
        clicar("novaBusca");preencher("cpfBusca","11144477735");clicar("buscarProfessor");aguardar();tela("consulta");assertTrue(dialogos.falha.contains("não possui vínculo"));
    }
    @Test void pessoaAusenteEInvalidaPermanecemNaIdentificacao()throws Exception{
        criar();clicar("menuCadastrar");preencher("cpf","4299822472");clicar("buscarCpf");aguardar();
        tela("identificar");assertTrue(dialogos.falha.contains("CPF inválido"));assertFalse(dialogos.falha.contains("titulação"));
        preencher("cpf","12345678909");clicar("buscarCpf");aguardar();tela("identificar");assertTrue(dialogos.falha.contains("Cadastre-a em Pessoas"));assertEquals(0,cenario.escritas);
    }
    @Test void edicaoDeTituloLegadoExigeEscolhaSemApagarOriginal()throws Exception{
        cenario.cadastrar(42,"Título antigo",SituacaoFuncional.ATIVO,7);criar();clicar("menuConsultar");preencher("professorId","1");clicar("buscarProfessor");aguardar();clicar("editarDocente");
        SwingUtilities.invokeAndWait(()->assertEquals(0,((JComboBox<?>)item("novoTitulo")).getSelectedIndex()));
        dialogos.confirmar=true;clicar("salvarDocente");aguardar();tela("edicaoDocente");assertEquals("Título antigo",cenario.professores.get(1L).titulacao());
        clicar("cancelarDocente");assertEquals("Título antigo",cenario.professores.get(1L).titulacao());
    }
    void previa(String nome)throws Exception{
        SwingUtilities.invokeAndWait(()->{
            painel.setSize(painel.getPreferredSize());PainelAlunosTest.organizar(painel);
            var imagem=new java.awt.image.BufferedImage(painel.getWidth(),painel.getHeight(),java.awt.image.BufferedImage.TYPE_INT_RGB);var g=imagem.createGraphics();
            try{painel.printAll(g);javax.imageio.ImageIO.write(imagem,"png",java.nio.file.Path.of("target","professor-"+nome+".png").toFile());}
            catch(java.io.IOException e){throw new IllegalStateException(e);}finally{g.dispose();}
        });
    }
}
