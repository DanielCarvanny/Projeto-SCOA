package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.dominio.alunos.*;
import br.edu.scoa.academico.dominio.professores.*;
import br.edu.scoa.academico.infraestrutura.identidade.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.Codigo.*;

class IdentidadeMysqlIT {
    static ConfiguracaoBanco banco;
    static final SenhasPbkdf2 senhas=new SenhasPbkdf2();
    RepositorioIdentidadeMysql repo;
    ServicoIdentidade servico;
    Sessao coordenador;
    Relogio relogio;
    static class Relogio extends Clock {
        Instant agora=Instant.parse("2026-11-12T12:00:00Z");
        public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId z){return this;}public Instant instant(){return agora;}
    }
    @BeforeAll static void preparar()throws Exception{
        var e=System.getenv();
        if(!"CONFIRMO_BANCO_DESCARTAVEL".equals(e.get("SCOA_TEST_CONFIRM"))||!"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(e.get("SCOA_TEST_URL"))||!"scoa_test".equals(e.get("SCOA_TEST_USER")))throw new IllegalStateException("Somente banco de testes exclusivo");
        banco=ConfiguracaoBanco.doAmbiente(e,"SCOA_TEST_");
        MigracoesIdentidade.executar(banco);
        var f=MigracoesAcademico.configurar(banco);
        if(f.info().applied().length==0)org.flywaydb.core.Flyway.configure().configuration(f.getConfiguration()).baselineVersion("0").load().baseline();
        f.migrate();
    }
    @BeforeEach void iniciar()throws Exception{
        limpar();relogio=new Relogio();repo=new RepositorioIdentidadeMysql(banco,senhas);servico=new ServicoIdentidade(repo,senhas,relogio);
        repo.inicializar("12345678901","Coord Teste","coord@example.invalid","coord",senhas.gerar("Teste123!".toCharArray()));
        coordenador=servico.entrar("COORD","Teste123!".toCharArray());
    }
    @AfterAll static void limpar()throws Exception{
        if(banco==null)return;
        try(var c=banco.abrir();var s=c.createStatement()){
            s.execute("DROP TRIGGER IF EXISTS teste_identidade_auditoria");
            s.executeUpdate("DELETE FROM identidade_auditoria");s.executeUpdate("DELETE FROM usuario");
            s.executeUpdate("DELETE FROM professor_auditoria");s.executeUpdate("DELETE FROM professor");
            s.executeUpdate("DELETE FROM aluno_auditoria");s.executeUpdate("DELETE FROM aluno");s.executeUpdate("DELETE FROM pessoa");
        }
    }
    @Test void identidadePersistidaLigaAlunoProfessorEContato() {
        var alunos=new RepositorioAlunosMysql(banco);var professores=new RepositorioProfessoresMysql(banco);
        var acesso=new AcessoAcademicoAutenticado(servico,coordenador,alunos,professores);
        var cadastro=new CadastroAlunoPorCpf(coordenador.usuarioId(),acesso,acesso,new CadastrarAluno(acesso,alunos));
        servico.criarPessoa(coordenador,"52998224725","Pessoa Teste","pessoa@example.invalid","");
        var aluno=cadastro.cadastrar("52998224725","Pessoa Teste","pessoa@example.invalid","",SituacaoCadastral.ATIVO,true);
        var docentes=new GestaoProfessores(coordenador.usuarioId(),acesso,acesso,acesso,professores);
        var professor=docentes.cadastrar("529.982.247-25","","","","Mestre",SituacaoFuncional.ATIVO,false);
        assertEquals(aluno.pessoaId(),professor.pessoaId());
        var ficha=docentes.consultar(""+professor.id());docentes.atualizarPessoa(ficha,"Pessoa Teste","novo@example.invalid","");
        var reaberto=new RepositorioIdentidadeMysql(banco,senhas);
        assertEquals("novo@example.invalid",reaberto.pessoaPorId(aluno.pessoaId()).orElseThrow().email());
        assertEquals(aluno,new RepositorioAlunosMysql(banco).consultar(aluno.id()).orElseThrow());
        assertEquals(professor,new RepositorioProfessoresMysql(banco).consultar(professor.id()).orElseThrow());
    }
    @Test void sessaoExpiraSaiEContaInativaRevoga(){
        relogio.agora=relogio.agora.plusSeconds(1800);
        assertEquals(SESSAO_INVALIDA,assertThrows(FalhaIdentidade.class,()->servico.verificar(coordenador)).codigo());
        var nova=servico.entrar("coord","Teste123!".toCharArray());servico.sair(nova);assertThrows(FalhaIdentidade.class,()->servico.verificar(nova));
    }
    @Test void bloqueioPersisteMesmoRecriandoServico(){
        for(int i=0;i<5;i++)assertEquals(CREDENCIAIS_INVALIDAS,assertThrows(FalhaIdentidade.class,()->servico.entrar("coord","errada".toCharArray())).codigo());
        var outro=new ServicoIdentidade(new RepositorioIdentidadeMysql(banco,senhas),senhas,relogio);
        assertThrows(FalhaIdentidade.class,()->outro.entrar("coord","Teste123!".toCharArray()));
        relogio.agora=relogio.agora.plusSeconds(300);assertNotNull(outro.entrar("coord","Teste123!".toCharArray()));
    }
    @Test void alunoSoEditaSeuContatoNaoNomePerfilOuSituacao(){
        var p=servico.criarPessoa(coordenador,"52998224725","Aluno Teste","aluno@example.invalid","");
        var alunos=new RepositorioAlunosMysql(banco);var aluno=alunos.cadastrar(p.pessoaId(),SituacaoCadastral.ATIVO,coordenador.usuarioId());
        var conta=servico.criarConta(coordenador,p.pessoaId(),"aluno","Teste123!".toCharArray(),Perfil.ALUNO);
        var sessao=servico.entrar("aluno","Teste123!".toCharArray());
        assertThrows(FalhaIdentidade.class,()->servico.criarPessoa(sessao,"11144477735","X","x@example.invalid",""));
        assertThrows(FalhaIdentidade.class,()->servico.consultarPessoa(sessao,coordenador.pessoaId()));
        assertThrows(FalhaIdentidade.class,()->servico.atualizarPessoa(sessao,p,"Novo nome",p.email(),""));
        assertThrows(FalhaIdentidade.class,()->servico.atualizarConta(sessao,conta,Perfil.COORDENADOR,true));
        assertEquals("novo@example.invalid",servico.atualizarPessoa(sessao,p,p.nome(),"novo@example.invalid","").email());
        var acesso=new AcessoAcademicoAutenticado(servico,sessao,alunos,new RepositorioProfessoresMysql(banco));
        assertTrue(acesso.podeConsultarAluno(conta.id(),aluno.id()));assertFalse(acesso.podeAtualizarAluno(conta.id(),aluno.id()));
        assertThrows(FalhaIdentidade.class,()->acesso.podeConsultarAluno(coordenador.usuarioId(),aluno.id()));
        servico.atualizarConta(coordenador,conta,Perfil.ALUNO,false);
        assertThrows(FalhaIdentidade.class,()->servico.verificar(sessao));
        assertThrows(FalhaIdentidade.class,()->servico.entrar("aluno","Teste123!".toCharArray()));
    }
    @Test void conflitoContatoEDuplicidadeConta(){
        var p=servico.criarPessoa(coordenador,"52998224725","Pessoa","p@example.invalid","");
        servico.atualizarPessoa(coordenador,p,"Novo","p@example.invalid","");
        assertThrows(FalhaDadosPessoais.class,()->servico.atualizarPessoa(coordenador,p,"Outro","p@example.invalid",""));
        new RepositorioProfessoresMysql(banco).cadastrar(p.pessoaId(),"Mestre",SituacaoFuncional.ATIVO,coordenador.usuarioId());
        new RepositorioAlunosMysql(banco).cadastrar(p.pessoaId(),SituacaoCadastral.ATIVO,coordenador.usuarioId());
        servico.criarConta(coordenador,p.pessoaId(),"teste","Teste123!".toCharArray(),Perfil.PROFESSOR);
        assertEquals(CONTA_DUPLICADA,assertThrows(FalhaIdentidade.class,()->servico.criarConta(coordenador,p.pessoaId(),"outra","Teste123!".toCharArray(),Perfil.ALUNO)).codigo());
    }
    @Test void naoRepeteBootstrapNemRemoveUltimoCoordenador(){
        assertEquals(INICIALIZACAO_JA_REALIZADA,assertThrows(FalhaIdentidade.class,()->repo.inicializar("11144477735","Outro","o@example.invalid","outro",senhas.gerar("Teste123!".toCharArray()))).codigo());
        var c=servico.verificar(coordenador);assertEquals(ULTIMO_COORDENADOR,assertThrows(FalhaIdentidade.class,()->servico.atualizarConta(coordenador,c,Perfil.COORDENADOR,false)).codigo());
    }
    @Test void concorrenciaPessoaReutilizaSemSobrescrever()throws Exception{
        var inicio=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)){
            Callable<PessoaParaCadastro> criar=()->{inicio.await();return servico.criarPessoa(coordenador,"52998224725","Concorrente","c@example.invalid","");};
            var a=executor.submit(criar);var b=executor.submit(criar);inicio.countDown();assertEquals(a.get(15,TimeUnit.SECONDS).pessoaId(),b.get(15,TimeUnit.SECONDS).pessoaId());
        }
        assertEquals("Concorrente",servico.criarPessoa(coordenador,"52998224725","Não sobrescrever","n@example.invalid","").nome());
    }
    @Test void falhaAuditoriaRevertePessoa()throws Exception{
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TRIGGER teste_identidade_auditoria BEFORE INSERT ON identidade_auditoria FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Teste' ");}
        assertThrows(PersistenciaAcademicaException.class,()->servico.criarPessoa(coordenador,"52998224725","Nova","n@example.invalid",""));
        assertTrue(repo.pessoaPorCpf("52998224725").isEmpty());
    }
    @Test void reaplicarMigracoesPreservaIdentidade(){
        MigracoesIdentidade.executar(banco);MigracoesAcademico.configurar(banco).migrate();
        assertEquals(coordenador.pessoaId(),repo.pessoaPorCpf("12345678901").orElseThrow().pessoaId());
    }
    @Test void contaExigeVinculoCompletoETrocaDePerfilRevogaSessao() {
        var p=servico.criarPessoa(coordenador,"52998224725","Vínculos","v@example.invalid","");
        assertEquals(VINCULO_ALUNO_NECESSARIO,assertThrows(FalhaIdentidade.class,()->servico.criarConta(coordenador,p.pessoaId(),"vinculo","Teste123!".toCharArray(),Perfil.ALUNO)).codigo());
        assertEquals(VINCULO_PROFESSOR_NECESSARIO,assertThrows(FalhaIdentidade.class,()->repo.criarConta(p.pessoaId(),"direto",senhas.gerar("Teste123!".toCharArray()),Perfil.PROFESSOR,coordenador.usuarioId())).codigo());
        assertTrue(repo.contaPorPessoa(p.pessoaId()).isEmpty());
        var alunos=new RepositorioAlunosMysql(banco);alunos.cadastrar(p.pessoaId(),SituacaoCadastral.ATIVO,coordenador.usuarioId());
        var conta=servico.criarConta(coordenador,p.pessoaId(),"vinculo","Teste123!".toCharArray(),Perfil.ALUNO);
        var sessao=servico.entrar("vinculo","Teste123!".toCharArray());
        assertEquals(VINCULO_PROFESSOR_NECESSARIO,assertThrows(FalhaIdentidade.class,()->servico.atualizarConta(coordenador,conta,Perfil.PROFESSOR,true)).codigo());
        var professores=new RepositorioProfessoresMysql(banco);var professor=professores.cadastrar(p.pessoaId(),"Mestre",SituacaoFuncional.ATIVO,coordenador.usuarioId());
        var acesso=new AcessoAcademicoAutenticado(servico,coordenador,alunos,professores);
        var gestao=new GestaoProfessores(coordenador.usuarioId(),acesso,acesso,acesso,professores);
        assertEquals(professor,gestao.consultarPorCpf("529.982.247-25").professor());
        assertNotEquals(p.pessoaId(),professor.id());
        assertEquals("Aluno e Professor",servico.consultarFicha(coordenador,p.cpf()).vinculos().descricao());
        assertEquals(Perfil.PROFESSOR,servico.atualizarConta(coordenador,conta,Perfil.PROFESSOR,true).perfil());
        assertEquals(SESSAO_INVALIDA,assertThrows(FalhaIdentidade.class,()->servico.verificar(sessao)).codigo());
    }
    @Test void tituloLegadoIncompletoNaoConcedePerfilProfessor()throws Exception {
        var p=servico.criarPessoa(coordenador,"52998224725","Legado","l@example.invalid","");
        new RepositorioProfessoresMysql(banco).cadastrar(p.pessoaId(),"Mestre",SituacaoFuncional.ATIVO,coordenador.usuarioId());
        try(var c=banco.abrir();var s=c.prepareStatement("UPDATE professor SET titulacao='Título legado' WHERE pessoa_id=?")){s.setLong(1,p.pessoaId());s.executeUpdate();}
        assertTrue(repo.vinculos(p.pessoaId()).possuiProfessor());assertFalse(repo.vinculos(p.pessoaId()).professorCompleto());
        assertEquals(VINCULO_PROFESSOR_NECESSARIO,assertThrows(FalhaIdentidade.class,()->servico.criarConta(coordenador,p.pessoaId(),"legado","Teste123!".toCharArray(),Perfil.PROFESSOR)).codigo());
    }
    @Test void telaRealAutenticaCriaPessoaEContaEReabreDados()throws Exception{
        var painel=new br.edu.scoa.academico.apresentacao.identidade.PainelIdentidade[1];
        var falha=new java.util.concurrent.atomic.AtomicReference<String>();
        javax.swing.SwingUtilities.invokeAndWait(()->painel[0]=new br.edu.scoa.academico.apresentacao.identidade.PainelIdentidade(servico,new br.edu.scoa.academico.apresentacao.alunos.DialogosAluno(){
            public void informar(java.awt.Component origem,String mensagem){}
            public void erro(java.awt.Component origem,String mensagem){falha.set(mensagem);}
            public boolean confirmar(java.awt.Component origem,String mensagem){return true;}
        },(s,a)->new javax.swing.JPanel()));
        try{
            preencher(painel[0],"loginEntrada","coord");preencher(painel[0],"senha","Teste123!");clicar(painel[0],"entrar");esperarTela(painel[0],"menu");
            clicar(painel[0],"pessoas");clicar(painel[0],"novaPessoa");preencher(painel[0],"cpf","52998224725");preencher(painel[0],"nome","Pessoa da tela");preencher(painel[0],"email","tela@example.invalid");
            clicar(painel[0],"revisarPessoa");assertTrue(repo.pessoaPorCpf("52998224725").isEmpty());clicar(painel[0],"salvarPessoa");esperarTela(painel[0],"menu");
            clicar(painel[0],"pessoas");clicar(painel[0],"buscarPessoa");preencher(painel[0],"cpfBusca","529.982.247-25");clicar(painel[0],"consultarPessoa");esperarTela(painel[0],"fichaPessoa");
            new RepositorioAlunosMysql(banco).cadastrar(repo.pessoaPorCpf("52998224725").orElseThrow().pessoaId(),SituacaoCadastral.ATIVO,coordenador.usuarioId());
            clicar(painel[0],"gerenciarConta");esperarTela(painel[0],"conta");preencher(painel[0],"novoLogin","pessoa.tela");preencher(painel[0],"novaSenha","Teste123!");clicar(painel[0],"salvarConta");esperarTela(painel[0],"menu");assertNull(falha.get());
            clicar(painel[0],"sair");esperarTela(painel[0],"login");
            var reaberto=new RepositorioIdentidadeMysql(banco,senhas);var p=reaberto.pessoaPorCpf("52998224725").orElseThrow();
            assertEquals("Pessoa da tela",p.nome());assertEquals("pessoa.tela",reaberto.contaPorPessoa(p.pessoaId()).orElseThrow().login());
            assertNotNull(new ServicoIdentidade(reaberto,senhas,relogio).entrar("pessoa.tela","Teste123!".toCharArray()));
        }finally{javax.swing.SwingUtilities.invokeAndWait(painel[0]::encerrar);esperarTela(painel[0],"login");}
    }
    private java.awt.Component componente(java.awt.Container pai,String nome){
        for(var f:pai.getComponents()){if(nome.equals(f.getName()))return f;if(f instanceof java.awt.Container c){var encontrado=componente(c,nome);if(encontrado!=null)return encontrado;}}return null;
    }
    private void preencher(java.awt.Container p,String campo,String texto)throws Exception{javax.swing.SwingUtilities.invokeAndWait(()->((javax.swing.JTextField)componente(p,campo)).setText(texto));}
    private void clicar(java.awt.Container p,String botao)throws Exception{javax.swing.SwingUtilities.invokeAndWait(()->((javax.swing.JButton)componente(p,botao)).doClick());}
    private void esperarTela(java.awt.Container p,String tela)throws Exception{
        var pronto=new CountDownLatch(1);var timer=new javax.swing.Timer(15,e->{var pagina=componente(p,tela);if(pagina!=null&&pagina.isVisible()&&"Pronto.".equals(((javax.swing.JLabel)componente(p,"estado")).getText()))pronto.countDown();});
        javax.swing.SwingUtilities.invokeAndWait(timer::start);try{assertTrue(pronto.await(15,TimeUnit.SECONDS),tela);}finally{javax.swing.SwingUtilities.invokeAndWait(timer::stop);}
    }
}
