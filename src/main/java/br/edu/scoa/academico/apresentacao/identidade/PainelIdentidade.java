package br.edu.scoa.academico.apresentacao.identidade;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.*;
import javax.swing.*;

/** Uma função por tela. Escritas só ocorrem após confirmação; JDBC nunca roda na EDT. */
public final class PainelIdentidade extends JPanel {
    private final ServicoIdentidade servico;
    private final DialogosAluno dialogos;
    private final BiFunction<Sessao,Boolean,JComponent> abrirAcademico;
    private final BiFunction<Sessao,br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo,JComponent> abrirCatalogo;
    private final Function<Sessao,JComponent> abrirMatrizes,abrirPeriodos;
    private final CardLayout navegacao=new CardLayout();
    private final JPanel paginas=new JPanel(navegacao);
    private final JLabel estado=new JLabel("Pronto."), resumoConta=new JLabel();
    private final JTextField login=new JTextField(24), cpf=new JTextField(24), nome=new JTextField(24), email=new JTextField(24), telefone=new JTextField(24), novoLogin=new JTextField(24);
    private final JPasswordField senha=new JPasswordField(24), novaSenha=new JPasswordField(24);
    private final JComboBox<Perfil> perfil=new JComboBox<>(Perfil.values());
    private final JCheckBox ativa=new JCheckBox("Conta ativa",true);
    private final JTextArea resumoPessoa=new JTextArea(15,45);
    private final JTextArea revisao=new JTextArea(8,40);
    private Sessao sessao;
    private PessoaParaCadastro pessoa;
    private Conta conta;
    private boolean ocupado,pessoaNova,proprioContato;
    private int geracao;
    private String cpfRevisado;
    private final Timer expiracao;
    private final JPanel menu;
    public PainelIdentidade(ServicoIdentidade servico,DialogosAluno dialogos,BiFunction<Sessao,Boolean,JComponent> abrirAcademico){
        this(servico,dialogos,abrirAcademico,null);
    }
    public PainelIdentidade(ServicoIdentidade servico,DialogosAluno dialogos,BiFunction<Sessao,Boolean,JComponent> abrirAcademico,
            BiFunction<Sessao,br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo,JComponent> abrirCatalogo){
        this(servico,dialogos,abrirAcademico,abrirCatalogo,null);
    }
    public PainelIdentidade(ServicoIdentidade servico,DialogosAluno dialogos,BiFunction<Sessao,Boolean,JComponent> abrirAcademico,
            BiFunction<Sessao,br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo,JComponent> abrirCatalogo,Function<Sessao,JComponent> abrirMatrizes){
        this(servico,dialogos,abrirAcademico,abrirCatalogo,abrirMatrizes,null);
    }
    public PainelIdentidade(ServicoIdentidade servico,DialogosAluno dialogos,BiFunction<Sessao,Boolean,JComponent> abrirAcademico,
            BiFunction<Sessao,br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo,JComponent> abrirCatalogo,
            Function<Sessao,JComponent> abrirMatrizes,Function<Sessao,JComponent> abrirPeriodos){
        this.servico=servico;this.dialogos=dialogos;this.abrirAcademico=abrirAcademico;this.abrirCatalogo=abrirCatalogo;this.abrirMatrizes=abrirMatrizes;this.abrirPeriodos=abrirPeriodos;
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        add(new JLabel("SCOA · Acadêmico · dados persistidos no MySQL"),BorderLayout.NORTH);add(paginas,BorderLayout.CENTER);estado.setName("estado");add(estado,BorderLayout.SOUTH);
        expiracao=new Timer(30*60*1000,e->encerrar());expiracao.setRepeats(false);
        var entrar=pagina("login","Entrar no SCOA","Informe as credenciais da sua conta institucional.");
        campo(entrar,"Login",login,"loginEntrada");campo(entrar,"Senha",senha,"senha");
        botao(entrar,"entrar","Entrar",()->{
            String usuario=login.getText();char[] segredo=senha.getPassword();senha.setText("");
            executar(()->{try{return servico.entrar(usuario,segredo);}finally{Arrays.fill(segredo,'\0');}},s->{sessao=s;expiracao.restart();abrirMenu();});
        });
        menu=pagina("menu","Menu acadêmico","Escolha uma funcionalidade.");
        var escolher=pagina("escolhaPessoa","Pessoas","O cadastro pessoal é compartilhado entre os vínculos.");
        botao(escolher,"novaPessoa","Cadastrar nova pessoa",()->{limparPessoa();pessoaNova=true;cpf.setEditable(true);nome.setEditable(true);mostrar("dadosPessoa");});
        botao(escolher,"buscarPessoa","Consultar pessoa existente",()->{limparPessoa();mostrar("buscarPessoa");});
        botao(escolher,"voltarMenu","Voltar ao menu",this::abrirMenu);
        var buscar=pagina("buscarPessoa","Consultar pessoa","Informe CPF com ou sem pontuação.");
        var cpfBusca=new JTextField(24);campo(buscar,"CPF — XXXXXXXXXXX ou XXX.XXX.XXX-XX",cpfBusca,"cpfBusca");
        botao(buscar,"consultarPessoa","Consultar",()->{String valor=cpfBusca.getText();executar(()->servico.consultarFicha(sessao,valor),f->{pessoa=f.pessoa();mostrarPessoa(f);mostrar("fichaPessoa");});});
        botao(buscar,"cancelarBusca","Voltar",()->mostrar("escolhaPessoa"));
        var ficha=pagina("fichaPessoa","Pessoa institucional","Nome e contato são compartilhados entre aluno e professor.");resumoPessoa.setName("resumoPessoa");resumoPessoa.setEditable(false);resumoPessoa.setLineWrap(true);resumoPessoa.setWrapStyleWord(true);resumoPessoa.setFont(UIManager.getFont("Label.font"));resumoPessoa.setBackground(UIManager.getColor("Panel.background"));ficha.add(new JScrollPane(resumoPessoa));
        botao(ficha,"editarPessoa","Editar dados pessoais",()->{pessoaNova=false;proprioContato=false;preencherPessoa();nome.setEditable(true);mostrar("dadosPessoa");});
        botao(ficha,"gerenciarConta","Gerenciar conta desta pessoa",()->{
            long id=pessoa.pessoaId();executar(()->servico.consultarConta(sessao,id),c->{conta=c.orElse(null);novoLogin.setText(conta==null?"":conta.login());novoLogin.setEditable(conta==null);novaSenha.setText("");novaSenha.setEnabled(conta==null);perfil.setSelectedItem(conta==null?Perfil.ALUNO:conta.perfil());ativa.setSelected(conta==null||conta.estaAtiva());ativa.setVisible(conta!=null);resumoConta.setText(conta==null?"Criar conta para "+pessoa.nome():"Conta de "+pessoa.nome()+" · login "+conta.login());mostrar("conta");});
        });
        botao(ficha,"fichaVoltar","Voltar à busca",()->mostrar("buscarPessoa"));
        var dados=pagina("dadosPessoa","Dados pessoais","Confira os dados. CPF não muda após o cadastro.");
        campo(dados,"CPF — com ou sem pontuação",cpf,"cpf");campo(dados,"Nome completo",nome,"nome");campo(dados,"E-mail",email,"email");campo(dados,"Telefone (opcional)",telefone,"telefone");
        botao(dados,"revisarPessoa","Revisar",()->{
            try{cpfRevisado=ValidacaoPessoa.cpf(cpf.getText());ValidacaoPessoa.dados(nome.getText(),email.getText(),telefone.getText());}
            catch(RuntimeException e){erro(e);return;}
            revisao.setText("Nome: "+nome.getText()+"\nCPF: "+cpfRevisado+"\nE-mail: "+email.getText()+"\nTelefone: "+telefone.getText()+"\n\n"+(pessoaNova?"Cadastrar nova pessoa.":"Salvar dados compartilhados em todos os vínculos desta pessoa."));mostrar("revisaoPessoa");
        });
        botao(dados,"cancelarPessoa","Cancelar sem salvar",this::abrirMenu);
        var revisar=pagina("revisaoPessoa","Confirmar dados pessoais","Nada foi gravado nesta edição até agora.");revisao.setEditable(false);revisao.setLineWrap(true);revisao.setWrapStyleWord(true);revisar.add(revisao);
        botao(revisar,"salvarPessoa","Confirmar",()->{
            String n=nome.getText(),e=email.getText(),t=telefone.getText(),documento=cpfRevisado;var esperada=pessoa;boolean nova=pessoaNova;
            executar(()->{
                if(nova){if(servico.consultarPessoa(sessao,documento).isPresent())throw new CadastroAlunoException(ErroCadastroAluno.PESSOA_JA_CADASTRADA);return servico.criarPessoa(sessao,documento,n,e,t);}
                return servico.atualizarPessoa(sessao,esperada,n,e,t);
            },p->{pessoa=p;dialogos.informar(this,"Dados pessoais salvos.");abrirMenu();});
        });
        botao(revisar,"voltarDados","Voltar e revisar",()->mostrar("dadosPessoa"));
        var dadosConta=pagina("conta","Conta de acesso","<html>Aluno exige vínculo em Alunos; Professor exige vínculo completo em Professores.<br>Coordenador é atribuído por autorização administrativa.</html>");dadosConta.add(resumoConta);
        campo(dadosConta,"Login (3 a 100 caracteres)",novoLogin,"novoLogin");campo(dadosConta,"Senha inicial (8 a 128 caracteres; usada apenas na criação)",novaSenha,"novaSenha");campo(dadosConta,"Perfil",perfil,"perfil");dadosConta.add(ativa);
        botao(dadosConta,"salvarConta","Confirmar conta",()->{
            if(!dialogos.confirmar(this,"Confirmar perfil e situação da conta desta pessoa?"))return;
            var escolhido=(Perfil)perfil.getSelectedItem();boolean habilitada=ativa.isSelected();String usuario=novoLogin.getText();char[] segredo=novaSenha.getPassword();novaSenha.setText("");var anterior=conta;long pessoaId=pessoa.pessoaId();
            executar(()->{try{return anterior==null?servico.criarConta(sessao,pessoaId,usuario,segredo,escolhido):servico.atualizarConta(sessao,anterior,escolhido,habilitada);}finally{Arrays.fill(segredo,'\0');}},c->{conta=c;dialogos.informar(this,"Conta salva. Alterações de perfil/situação invalidam as sessões anteriores.");abrirMenu();});
        });
        botao(dadosConta,"cancelarConta","Voltar sem salvar",()->{novaSenha.setText("");mostrar("fichaPessoa");});
        mostrar("login");
    }
    private void abrirMenu(){
        executar(()->servico.verificar(sessao),c->{
            menu.removeAll();menu.add(new JLabel("Menu acadêmico · "+c.login()+" · "+c.perfil()));
            if(c.perfil()==Perfil.COORDENADOR){
                botao(menu,"pessoas","Pessoas e contas",()->{limparPessoa();mostrar("escolhaPessoa");});
                botao(menu,"alunos","Alunos",()->abrirVinculo(true));botao(menu,"professores","Professores",()->abrirVinculo(false));
                if(abrirMatrizes!=null)botao(menu,"matrizes","Matrizes curriculares",this::abrirMatrizes);
                if(abrirPeriodos!=null)botao(menu,"periodos","Períodos letivos",this::abrirPeriodos);
                if(abrirCatalogo!=null){
                    botao(menu,"cursos","Cursos",()->abrirCatalogo(br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.CURSO));
                    botao(menu,"disciplinas","Disciplinas",()->abrirCatalogo(br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.DISCIPLINA));
                }
            }
            botao(menu,"meuContato","Meu contato",()->executar(()->servico.consultarPessoa(sessao,sessao.pessoaId()).orElseThrow(()->new FalhaIdentidade(FalhaIdentidade.Codigo.PESSOA_INCOMPLETA)),p->{pessoa=p;pessoaNova=false;proprioContato=true;preencherPessoa();nome.setEditable(c.perfil()==Perfil.COORDENADOR);mostrar("dadosPessoa");}));
            botao(menu,"sair","Sair da conta",this::encerrar);mostrar("menu");
        });
    }
    private void abrirVinculo(boolean aluno){
        executar(()->{servico.verificar(sessao);return sessao;},s->{
            Component antigo=null;for(Component p:paginas.getComponents())if("vinculo".equals(p.getName()))antigo=p;if(antigo!=null)paginas.remove(antigo);
            var painel=new JPanel(new BorderLayout());painel.setName("vinculo");painel.add(abrirAcademico.apply(s,aluno));
            var voltar=new JButton("Voltar ao menu acadêmico");voltar.addActionListener(e->abrirMenu());painel.add(voltar,BorderLayout.SOUTH);paginas.add(painel,"vinculo");mostrar("vinculo");
        });
    }
    private void abrirCatalogo(br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo tipo){
        executar(()->{if(!servico.coordenador(sessao))throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);return sessao;},s->{
            for(Component p:paginas.getComponents())if("vinculo".equals(p.getName()))paginas.remove(p);
            var painel=new JPanel(new BorderLayout());painel.setName("vinculo");painel.add(abrirCatalogo.apply(s,tipo));
            var voltar=new JButton("Voltar ao menu acadêmico");voltar.addActionListener(e->abrirMenu());painel.add(voltar,BorderLayout.SOUTH);
            paginas.add(painel,"vinculo");mostrar("vinculo");
        });
    }
    private void abrirMatrizes(){
        executar(()->{if(!servico.coordenador(sessao))throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);return sessao;},s->{
            for(Component p:paginas.getComponents())if("vinculo".equals(p.getName()))paginas.remove(p);
            var painel=new JPanel(new BorderLayout());painel.setName("vinculo");painel.add(abrirMatrizes.apply(s));
            var voltar=new JButton("Voltar ao menu acadêmico");voltar.addActionListener(e->abrirMenu());painel.add(voltar,BorderLayout.SOUTH);
            paginas.add(painel,"vinculo");mostrar("vinculo");
        });
    }
    private void abrirPeriodos(){
        executar(()->{if(!servico.coordenador(sessao))throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);return sessao;},s->{
            for(Component p:paginas.getComponents())if("vinculo".equals(p.getName()))paginas.remove(p);
            var painel=new JPanel(new BorderLayout());painel.setName("vinculo");painel.add(abrirPeriodos.apply(s));
            var voltar=new JButton("Voltar ao menu acadêmico");voltar.addActionListener(e->abrirMenu());painel.add(voltar,BorderLayout.SOUTH);
            paginas.add(painel,"vinculo");mostrar("vinculo");
        });
    }
    public void encerrar(){
        geracao++;
        expiracao.stop();Sessao anterior=sessao;sessao=null;limparPessoa();senha.setText("");novaSenha.setText("");login.setText("");
        for(Component p:paginas.getComponents())if("vinculo".equals(p.getName()))paginas.remove(p);
        executar(()->{servico.sair(anterior);return true;},x->mostrar("login"));mostrar("login");
    }
    private void preencherPessoa(){cpf.setText(pessoa.cpf());cpf.setEditable(false);nome.setText(pessoa.nome());email.setText(pessoa.email());telefone.setText(pessoa.telefone());}
    private void mostrarPessoa(FichaPessoa ficha){
        var p=ficha.pessoa();var c=ficha.conta();
        String contaTexto=c.map(a->"Login: "+a.login()+"\nPerfil de acesso: "+a.perfil()+"\nSituação: "+(a.estaAtiva()?"Ativa":"Inativa")).orElse("Sem conta");
        String pendencia="";
        if(c.isPresent())try{ficha.vinculos().exigirPerfil(c.get().perfil());}catch(FalhaIdentidade e){pendencia="\n\nRegularização pendente: "+MensagensIdentidade.erro(e);}
        resumoPessoa.setText("DADOS PESSOAIS\nNome: "+p.nome()+"\nCPF: "+p.cpf()+"\nE-mail: "+p.email()+"\nTelefone: "+(p.telefone()==null||p.telefone().isBlank()?"Não informado":p.telefone())
            +"\n\nCONTA DE ACESSO\n"+contaTexto+"\n\nVÍNCULOS ACADÊMICOS\n"+ficha.vinculos().descricao()+pendencia);
        resumoPessoa.setCaretPosition(0);
    }
    private void limparPessoa(){pessoa=null;conta=null;resumoPessoa.setText("");proprioContato=false;cpf.setText("");nome.setText("");email.setText("");telefone.setText("");revisao.setText("");}
    private JPanel pagina(String id,String titulo,String ajuda){var p=new JPanel();p.setName(id);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(new JLabel(titulo));p.add(Box.createVerticalStrut(12));p.add(new JLabel(ajuda));p.add(Box.createVerticalStrut(12));paginas.add(p,id);return p;}
    private void campo(JPanel p,String titulo,JComponent campo,String id){var label=new JLabel(titulo);label.setLabelFor(campo);p.add(label);campo.setName(id);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));p.add(campo);p.add(Box.createVerticalStrut(8));}
    private void botao(JPanel p,String id,String titulo,Runnable acao){var b=new JButton(titulo);b.setName(id);b.addActionListener(e->{if(!ocupado)acao.run();});p.add(b);p.add(Box.createVerticalStrut(8));}
    private void mostrar(String tela){navegacao.show(paginas,tela);revalidate();repaint();}
    private void habilitar(Container c,boolean valor){for(Component f:c.getComponents()){f.setEnabled(valor);if(f instanceof Container grupo)habilitar(grupo,valor);}}
    private <T>void executar(Callable<T> trabalho,Consumer<T> sucesso){
        int origem=geracao;
        ocupado=true;habilitar(paginas,false);estado.setText("Processando…");
        new SwingWorker<T,Void>(){
            protected T doInBackground()throws Exception{return trabalho.call();}
            protected void done(){
                if(origem!=geracao)return;
                // Libera antes do callback: ele pode iniciar o carregamento da próxima tela.
                ocupado=false;habilitar(paginas,true);estado.setText("Pronto.");
                try{sucesso.accept(get());}catch(InterruptedException e){Thread.currentThread().interrupt();erro(e);}catch(ExecutionException e){erro(e.getCause());}
                if(conta!=null)novaSenha.setEnabled(false);
            }
        }.execute();
    }
    private void erro(Throwable e){dialogos.erro(this,ControladorAlunos.mensagemErro(e));if(e instanceof FalhaIdentidade f&&f.codigo()==FalhaIdentidade.Codigo.SESSAO_INVALIDA){expiracao.stop();sessao=null;mostrar("login");}}
}
