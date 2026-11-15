package br.edu.scoa.academico.identidade;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.infraestrutura.identidade.*;
import br.edu.scoa.academico.apresentacao.identidade.*;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ServicoETelasIdentidadeTest {
    static class Memoria implements RepositorioIdentidade {
        Conta conta=new Conta(7,42,"coord",Perfil.COORDENADOR,true,1);
        Map<Long,PessoaParaCadastro> pessoas=new HashMap<>();int escritas;
        Memoria(){pessoas.put(42L,new PessoaParaCadastro(42,"12345678901","Pessoa Teste","p@example.invalid",""));}
        public Optional<PessoaParaCadastro> pessoaPorCpf(String cpf){return pessoas.values().stream().filter(p->p.cpf().equals(cpf)).findFirst();}
        public Optional<PessoaParaCadastro> pessoaPorId(long id){return Optional.ofNullable(pessoas.get(id));}
        public PessoaParaCadastro criarPessoa(String cpf,String n,String e,String t,long u){escritas++;var p=new PessoaParaCadastro(43,cpf,n,e,t);pessoas.put(43L,p);return p;}
        public PessoaParaCadastro atualizarPessoa(PessoaParaCadastro p,String n,String e,String t,long u){escritas++;var novo=new PessoaParaCadastro(p.pessoaId(),p.cpf(),n,e,t);pessoas.put(p.pessoaId(),novo);return novo;}
        public Conta criarConta(long p,String l,String h,Perfil f,long u){throw new UnsupportedOperationException();}
        VinculosAcademicos vinculos=new VinculosAcademicos(false,false,false);
        public VinculosAcademicos vinculos(long id){return vinculos;}
        public Optional<Conta> conta(long id){return id==conta.id()?Optional.of(conta):Optional.empty();}
        public Optional<Conta> contaPorPessoa(long p){return p==conta.pessoaId()?Optional.of(conta):Optional.empty();}
        public Conta atualizarConta(Conta c,Perfil p,boolean a,long u){throw new UnsupportedOperationException();}
        public Conta autenticar(String l,char[] s,Instant agora){if(!l.equals("coord")||!Arrays.equals(s,"senha123".toCharArray()))throw new FalhaIdentidade(FalhaIdentidade.Codigo.CREDENCIAIS_INVALIDAS);return conta;}
        public void registrarSaida(long u){}
        public Conta inicializar(String c,String n,String e,String l,String h){throw new UnsupportedOperationException();}
    }
    static class Relogio extends Clock {Instant agora=Instant.parse("2026-11-12T00:00:00Z");public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId z){return this;}public Instant instant(){return agora;}}
    final Memoria repo=new Memoria();final Relogio relogio=new Relogio();
    final ServicoIdentidade servico=new ServicoIdentidade(repo,new SenhasPbkdf2(),relogio);
    PainelIdentidade painel;String erro;
    void criar()throws Exception{SwingUtilities.invokeAndWait(()->painel=new PainelIdentidade(servico,new DialogosAluno(){public void informar(Component c,String m){}public void erro(Component c,String m){erro=m;}public boolean confirmar(Component c,String m){return true;}},(s,a)->new JPanel()));}
    Component item(Container c,String id){for(Component f:c.getComponents()){if(id.equals(f.getName()))return f;if(f instanceof Container p){var achado=item(p,id);if(achado!=null)return achado;}}return null;}
    void clicar(String id)throws Exception{SwingUtilities.invokeAndWait(()->((JButton)item(painel,id)).doClick());}
    void preencher(String id,String t)throws Exception{SwingUtilities.invokeAndWait(()->((JTextField)item(painel,id)).setText(t));}
    void esperar(String tela)throws Exception{
        CountDownLatch pronto=new CountDownLatch(1);var timer=new javax.swing.Timer(15,e->{var p=item(painel,tela);if(p!=null&&p.isVisible()&&"Pronto.".equals(((JLabel)item(painel,"estado")).getText()))pronto.countDown();});
        SwingUtilities.invokeAndWait(timer::start);try{assertTrue(pronto.await(5,TimeUnit.SECONDS),tela);}finally{SwingUtilities.invokeAndWait(timer::stop);}
    }
    void entrar()throws Exception{preencher("loginEntrada","coord");preencher("senha","senha123");clicar("entrar");esperar("menu");}
    @Test void expiracaoELimiteExatoESaidaInvalidamSessao(){
        var s=servico.entrar("COORD","senha123".toCharArray());relogio.agora=relogio.agora.plusSeconds(1799);assertEquals(7,servico.verificar(s).id());
        relogio.agora=relogio.agora.plusSeconds(1);assertThrows(FalhaIdentidade.class,()->servico.verificar(s));
        var nova=servico.entrar("coord","senha123".toCharArray());servico.sair(nova);assertThrows(FalhaIdentidade.class,()->servico.verificar(nova));
    }
    @Test void contaAlteradaRevogaSessao(){var s=servico.entrar("coord","senha123".toCharArray());repo.conta=new Conta(7,42,"coord",Perfil.ALUNO,true,2);assertThrows(FalhaIdentidade.class,()->servico.verificar(s));}
    @Test void fichaExibeDadosContaEVinculosSemConfundirAusencia()throws Exception {
        criar();entrar();clicar("pessoas");clicar("buscarPessoa");preencher("cpfBusca","12345678901");
        for(var vinculo:new VinculosAcademicos[]{new VinculosAcademicos(false,false,false),new VinculosAcademicos(true,false,false),new VinculosAcademicos(false,true,true),new VinculosAcademicos(true,true,true)}) {
            repo.vinculos=vinculo;clicar("consultarPessoa");esperar("fichaPessoa");
            SwingUtilities.invokeAndWait(()->{
                String texto=((JTextArea)item(painel,"resumoPessoa")).getText();
                assertTrue(texto.contains("12345678901"));assertTrue(texto.contains("p@example.invalid"));
                assertTrue(texto.contains("COORDENADOR"));assertTrue(texto.contains(vinculo.descricao()));
            });
            previa("ficha");clicar("fichaVoltar");
        }
        repo.criarPessoa("52998224725","Sem conta","sem@example.invalid","",7);
        preencher("cpfBusca","52998224725");clicar("consultarPessoa");esperar("fichaPessoa");
        SwingUtilities.invokeAndWait(()->assertTrue(((JTextArea)item(painel,"resumoPessoa")).getText().contains("Sem conta")));
        clicar("fichaVoltar");clicar("cancelarBusca");clicar("voltarMenu");esperar("menu");clicar("sair");esperar("login");
    }
    @Test void fichaFalhaNaoSeConverteEmAusenciaENaoAutorizaAluno() {
        var falho=new Memoria(){public VinculosAcademicos vinculos(long id){throw new FalhaIdentidade(FalhaIdentidade.Codigo.INDISPONIVEL);}};
        var s=new ServicoIdentidade(falho,new SenhasPbkdf2(),relogio);
        var sessao=s.entrar("coord","senha123".toCharArray());
        assertEquals(FalhaIdentidade.Codigo.INDISPONIVEL,assertThrows(FalhaIdentidade.class,()->s.consultarFicha(sessao,"12345678901")).codigo());
        repo.conta=new Conta(7,42,"coord",Perfil.ALUNO,true,1);
        var aluno=servico.entrar("coord","senha123".toCharArray());
        assertEquals(FalhaIdentidade.Codigo.NAO_AUTORIZADO,assertThrows(FalhaIdentidade.class,()->servico.consultarFicha(aluno,"12345678901")).codigo());
    }
    @Test void loginERoteiroNovaPessoaSoGravaNaConfirmacao()throws Exception{
        criar();preencher("loginEntrada","coord");preencher("senha","errada");clicar("entrar");esperar("login");assertNotNull(erro);
        entrar();previa("menu");clicar("pessoas");clicar("novaPessoa");preencher("cpf","52998224725");preencher("nome","Nova Pessoa");preencher("email","nova@example.invalid");previa("pessoa");clicar("revisarPessoa");assertEquals(0,repo.escritas);previa("revisao");clicar("salvarPessoa");esperar("menu");assertEquals(1,repo.escritas);clicar("sair");esperar("login");previa("login");
    }
    @Test void alunoNaoVeAdministracaoEContatoNaoPermiteNome()throws Exception{
        repo.conta=new Conta(7,42,"coord",Perfil.ALUNO,true,1);criar();entrar();
        SwingUtilities.invokeAndWait(()->assertNull(item((Container)item(painel,"menu"),"pessoas")));
        clicar("meuContato");esperar("dadosPessoa");SwingUtilities.invokeAndWait(()->assertFalse(((JTextField)item(painel,"nome")).isEditable()));
        preencher("email","novo@example.invalid");clicar("cancelarPessoa");esperar("menu");assertEquals(0,repo.escritas);clicar("sair");esperar("login");
    }
    void previa(String nome)throws Exception{SwingUtilities.invokeAndWait(()->{painel.setSize(860,780);organizar(painel);var img=new java.awt.image.BufferedImage(860,780,java.awt.image.BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();try{painel.printAll(g);javax.imageio.ImageIO.write(img,"png",java.nio.file.Path.of("target","identidade-"+nome+".png").toFile());}catch(java.io.IOException e){throw new IllegalStateException(e);}finally{g.dispose();}});}
    void organizar(Container c){c.doLayout();for(Component f:c.getComponents())if(f instanceof Container p)organizar(p);}
}
