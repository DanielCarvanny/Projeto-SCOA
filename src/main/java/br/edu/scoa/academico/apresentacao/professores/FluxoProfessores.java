package br.edu.scoa.academico.apresentacao.professores;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.apresentacao.alunos.ControladorAlunos;
import br.edu.scoa.academico.dominio.professores.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

/** Cadastro guiado e edição somente após localizar o docente. */
public final class FluxoProfessores extends JPanel {
    private final GestaoProfessores gestao;
    private final DialogosAluno dialogos;
    private final CardLayout navegacao=new CardLayout();
    private final JPanel paginas=new JPanel(navegacao);
    private final JLabel estado=new JLabel("Pronto."), orientacao=new JLabel(), identidadeEdicao=new JLabel(), docenteEdicao=new JLabel();
    private final JTextField cpf=new JTextField(24), nome=new JTextField(24), email=new JTextField(24), telefone=new JTextField(24), id=new JTextField(24);
    private final JTextField novoNome=new JTextField(24), novoEmail=new JTextField(24), novoTelefone=new JTextField(24);
    private final JComboBox<SituacaoFuncional> situacao=new JComboBox<>(SituacaoFuncional.values()), novaSituacao=new JComboBox<>(SituacaoFuncional.values());
    private final JTextArea revisao=texto("textoRevisao"), detalhes=texto("textoDetalhes"), resultado=texto("textoResultado");
    private final JComboBox<String> titulacao=opcoesTitulos(), novoTitulo=opcoesTitulos();
    private final JTextField cpfBusca=new JTextField(24);
    private final JComboBox<String> tipoBusca=new JComboBox<>(new String[]{"ID interno","CPF"});

    private final JLabel tituloAnterior=new JLabel();
    private static JComboBox<String> opcoesTitulos() {
        var opcoes=new JComboBox<String>(); opcoes.addItem("Selecione uma titulação");
        for(var titulo:Titulacao.values()) opcoes.addItem(titulo.toString()); return opcoes;
    }
    private String cpfConsultado;
    private boolean ocupado;
    private FichaProfessor selecionada;
    public FluxoProfessores(GestaoProfessores gestao,DialogosAluno dialogos,String contexto) {
        this.gestao=gestao;this.dialogos=dialogos;
        setLayout(new BorderLayout(16,16));setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        add(new JLabel(contexto),BorderLayout.NORTH);add(paginas,BorderLayout.CENTER);estado.setName("estado");add(estado,BorderLayout.SOUTH);
        var menu=pagina("menu","Professores","Escolha o que deseja fazer.");
        botao(menu,"menuCadastrar","Cadastrar professor",()->{limparCadastro();mostrar("identificar");});
        botao(menu,"menuConsultar","Consultar professor",this::novaBusca);
        var identificar=pagina("identificar","Localizar pessoa cadastrada","Informe o CPF para localizar a pessoa institucional.");
        campo(identificar,"CPF — XXXXXXXXXXX ou XXX.XXX.XXX-XX",cpf,"cpf");
        identificar.add(new JLabel("Exemplo: 52998224725 ou 529.982.247-25."));
        botao(identificar,"buscarCpf","Buscar CPF e continuar",()->{
            String documento=cpf.getText();limparDados();
            executar(()->gestao.buscarPessoa(documento),pessoa->{
                if(pessoa.isEmpty()){dialogos.erro(this,"Pessoa não cadastrada. Cadastre-a em Pessoas antes de criar este vínculo.");return;}
                cpfConsultado=documento;
                pessoa.ifPresent(p->{nome.setText(p.nome());email.setText(p.email());telefone.setText(p.telefone());});
                nome.setEditable(false);email.setEditable(false);telefone.setEditable(false);
                orientacao.setText("Pessoa encontrada: para editar seus dados pessoais, use Pessoas.");mostrar("dados");
            });
        });
        botao(identificar,"voltarMenu","Voltar ao menu",this::menu);
        var dados=pagina("dados","Cadastrar professor · 2 de 3","Confira os dados e informe a maior titulação registrada.");dados.add(orientacao);
        campo(dados,"Nome completo *",nome,"nome");campo(dados,"E-mail *",email,"email");campo(dados,"Telefone (opcional)",telefone,"telefone");
        campo(dados,"Maior titulação concluída *",titulacao,"titulacao");campo(dados,"Situação funcional *",situacao,"situacao");
        botao(dados,"revisar","Continuar para revisão",()->{
            try { GestaoProfessores.validarDados(((String)titulacao.getSelectedItem()),(SituacaoFuncional)situacao.getSelectedItem()); }
            catch(FalhaProfessor falha) { erro(falha);return; }
            revisao.setText("CPF: "+cpfConsultado+"\nNome: "+nome.getText()+"\nE-mail: "+email.getText()+"\nTelefone: "+telefone.getText()
                    +"\nTitulação: "+((String)titulacao.getSelectedItem())+"\nSituação funcional: "+situacao.getSelectedItem()+"\n\n"+"Será criado apenas o vínculo docente da pessoa encontrada.");mostrar("revisao");
        });
        botao(dados,"voltarCpf","Voltar à identificação",()->{limparDados();mostrar("identificar");});botao(dados,"cancelarCadastro","Cancelar cadastro",this::menu);
        var confirmar=pagina("revisao","Cadastrar professor · 3 de 3","Revise os dados. Nada foi gravado até este momento.");confirmar.add(revisao);
        botao(confirmar,"confirmarCadastro","Confirmar cadastro",()->{
            if(cpfConsultado==null)return;
            String documento=cpfConsultado,n=nome.getText(),e=email.getText(),t=telefone.getText(),titulo=((String)titulacao.getSelectedItem());
            var valor=(SituacaoFuncional)situacao.getSelectedItem();
            executar(()->gestao.cadastrar(documento,n,e,t,titulo,valor,false),professor->{
                limparCadastro();resultado.setText("Professor cadastrado com sucesso.\n\nID do professor: "+professor.id()+"\nTitulação: "+professor.titulacao()+"\nSituação: "+professor.situacaoFuncional()+"\n\nConsulte o professor pelo CPF ou pelo ID interno.");mostrar("resultado");
            });
        });
        botao(confirmar,"voltarDados","Voltar e revisar dados",()->mostrar("dados"));botao(confirmar,"cancelarRevisao","Cancelar cadastro",this::menu);
        var sucesso=pagina("resultado","Cadastro concluído","O vínculo docente foi criado.");sucesso.add(resultado);botao(sucesso,"resultadoMenu","Voltar ao menu",this::menu);
        var busca=pagina("consulta","Consultar professor","Escolha a forma de busca e informe o dado correspondente.");
        campo(busca,"Buscar por",tipoBusca,"tipoBusca");
        var entradaId=new JPanel();entradaId.setLayout(new BoxLayout(entradaId,BoxLayout.Y_AXIS));
        var entradaCpf=new JPanel();entradaCpf.setLayout(new BoxLayout(entradaCpf,BoxLayout.Y_AXIS));
        campo(entradaId,"ID interno do professor",id,"professorId");
        campo(entradaCpf,"CPF — XXXXXXXXXXX ou XXX.XXX.XXX-XX",cpfBusca,"cpfBusca");
        busca.add(entradaId);busca.add(entradaCpf);entradaCpf.setVisible(false);
        tipoBusca.addActionListener(e->{entradaId.setVisible(tipoBusca.getSelectedIndex()==0);entradaCpf.setVisible(tipoBusca.getSelectedIndex()==1);});
        botao(busca,"buscarProfessor","Buscar professor",()->{
            selecionada=null;detalhes.setText("");String valor=tipoBusca.getSelectedIndex()==1?cpfBusca.getText():id.getText();boolean porCpf=tipoBusca.getSelectedIndex()==1;
            executar(()->porCpf?gestao.consultarPorCpf(valor):gestao.consultar(valor),this::exibir);
        });
        botao(busca,"consultaMenu","Voltar ao menu",this::menu);
        var ficha=pagina("detalhes","Detalhes do professor","Confira o docente antes de solicitar uma alteração.");ficha.add(detalhes);
        botao(ficha,"editarDocente","Editar titulação e situação",()->{
            if(selecionada==null)return;var p=selecionada.professor();
            docenteEdicao.setText("Professor "+p.id()+" · "+selecionada.pessoa().nome());novoTitulo.setSelectedIndex(0); if(Titulacao.aceita(p.titulacao())) novoTitulo.setSelectedItem(p.titulacao()); tituloAnterior.setText("Titulação registrada: "+p.titulacao());novaSituacao.setSelectedItem(p.situacaoFuncional());mostrar("edicaoDocente");
        });
        botao(ficha,"editarPessoa","Editar dados pessoais",()->{
            if(selecionada==null)return;var p=selecionada.pessoa();
            identidadeEdicao.setText("Professor "+selecionada.professor().id()+" · CPF: "+p.cpf()+" (não editável)");
            novoNome.setText(p.nome());novoEmail.setText(p.email());novoTelefone.setText(p.telefone());mostrar("edicaoPessoa");
        });
        botao(ficha,"novaBusca","Voltar à busca",this::novaBusca);botao(ficha,"detalhesMenu","Voltar ao menu",this::menu);
        var editar=pagina("edicaoDocente","Editar dados docentes","A atualização preserva o histórico da titulação e da situação.");editar.add(docenteEdicao); editar.add(tituloAnterior);
        campo(editar,"Maior titulação concluída *",novoTitulo,"novoTitulo");campo(editar,"Situação funcional *",novaSituacao,"novaSituacao");
        botao(editar,"salvarDocente","Confirmar alteração",()->{
            if(selecionada==null)return;var esperada=selecionada;String titulo=((String)novoTitulo.getSelectedItem());var valor=(SituacaoFuncional)novaSituacao.getSelectedItem();
            if(!dialogos.confirmar(this,"Atualizar titulação e situação do professor "+esperada.professor().id()+"?"))return;
            executar(()->gestao.atualizar(esperada.professor(),titulo,valor),p->exibir(new FichaProfessor(p,esperada.pessoa())));
        });botao(editar,"cancelarDocente","Voltar sem alterar",()->mostrar("detalhes"));
        var pessoal=pagina("edicaoPessoa","Editar dados pessoais","<html>Nome e contato são compartilhados entre os vínculos desta pessoa.<br>Alterações também aparecerão em seu cadastro de aluno, se houver.</html>");pessoal.add(identidadeEdicao);
        campo(pessoal,"Nome completo *",novoNome,"novoNome");campo(pessoal,"E-mail *",novoEmail,"novoEmail");campo(pessoal,"Telefone (opcional)",novoTelefone,"novoTelefone");
        botao(pessoal,"salvarPessoa","Salvar dados pessoais",()->{
            if(selecionada==null)return;var esperada=selecionada;String n=novoNome.getText(),e=novoEmail.getText(),t=novoTelefone.getText();
            if(dialogos.confirmar(this,"Salvar dados pessoais compartilhados? A alteração aparecerá em todos os vínculos desta pessoa."))executar(()->gestao.atualizarPessoa(esperada,n,e,t),this::exibir);
        });botao(pessoal,"cancelarPessoa","Voltar sem salvar",()->mostrar("detalhes"));mostrar("menu");
    }
    private JPanel pagina(String nome,String titulo,String ajuda) {
        JPanel painel=new JPanel();painel.setName(nome);painel.setLayout(new BoxLayout(painel,BoxLayout.Y_AXIS));
        JLabel cabecalho=new JLabel(titulo);cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD,23f));
        painel.add(cabecalho);painel.add(Box.createVerticalStrut(12));painel.add(new JLabel(ajuda));painel.add(Box.createVerticalStrut(16));paginas.add(painel,nome);return painel;
    }
    private void campo(JPanel painel,String titulo,JComponent campo,String nome) {
        campo.setName(nome);JLabel label=new JLabel(titulo);label.setLabelFor(campo);painel.add(label);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));painel.add(campo);painel.add(Box.createVerticalStrut(8));
    }
    private void botao(JPanel painel,String nome,String titulo,Runnable acao) {
        JButton botao=new JButton(titulo);botao.setName(nome);botao.addActionListener(e->{if(!ocupado)acao.run();});painel.add(botao);painel.add(Box.createVerticalStrut(8));
    }
    private static JTextArea texto(String nome) {
        JTextArea area=new JTextArea(10,48);area.setName(nome);area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);
        area.setFont(UIManager.getFont("Label.font"));area.setBackground(UIManager.getColor("Panel.background"));return area;
    }
    private void mostrar(String nome) {
        for(Component pagina:paginas.getComponents()) for(Component filho:((Container)pagina).getComponents()) if(filho instanceof JComponent visual)visual.setAlignmentX(Component.LEFT_ALIGNMENT);
        navegacao.show(paginas,nome);
    }
    private void limparDados(){cpfConsultado=null;nome.setText("");email.setText("");telefone.setText("");titulacao.setSelectedIndex(0);situacao.setSelectedIndex(0);revisao.setText("");}
    private void limparCadastro(){cpf.setText("");limparDados();}
    private void menu(){limparCadastro();selecionada=null;detalhes.setText("");resultado.setText("");mostrar("menu");}
    private void novaBusca(){selecionada=null;detalhes.setText("");id.setText("");cpfBusca.setText("");mostrar("consulta");}
    private void exibir(FichaProfessor ficha){selecionada=ficha;var p=ficha.professor();var pessoa=ficha.pessoa();detalhes.setText("DADOS PESSOAIS COMPARTILHADOS\nNome: "+pessoa.nome()+"\nCPF: "+pessoa.cpf()+"\nE-mail: "+pessoa.email()+"\nTelefone: "+(pessoa.telefone()==null?"":pessoa.telefone())+"\n\nDADOS DO PROFESSOR\nID do professor: "+p.id()+"\nTitulação: "+p.titulacao()+"\nSituação funcional: "+p.situacaoFuncional());mostrar("detalhes");}
    private void habilitar(Container pai,boolean valor){for(Component filho:pai.getComponents()){filho.setEnabled(valor);if(filho instanceof Container grupo)habilitar(grupo,valor);}}
    private <T> void executar(Callable<T> tarefa,Consumer<T> sucesso){
        ocupado=true;habilitar(paginas,false);estado.setText("Processando…");
        new SwingWorker<T,Void>(){
            protected T doInBackground() throws Exception{return tarefa.call();}
            protected void done(){try{sucesso.accept(get());}catch(InterruptedException e){Thread.currentThread().interrupt();erro(e);}catch(ExecutionException e){erro(e.getCause());}finally{ocupado=false;habilitar(paginas,true);estado.setText("Pronto.");}}
        }.execute();
    }
    private void erro(Throwable erro){
        String mensagem=erro instanceof FalhaProfessor falha?switch(falha.codigo()){
            case CPF_INVALIDO->"CPF inválido. Informe 11 números válidos, com ou sem pontuação.";
            case TITULACAO_INVALIDA->"Selecione uma das seis titulações disponíveis.";
            case PESSOA_JA_CADASTRADA->"Este CPF já possui cadastro. Volte e escolha Usar uma pessoa já cadastrada.";
            case ENTRADA_INVALIDA->"Confira o identificador e a situação funcional.";
            case NAO_AUTORIZADO->"Você não tem permissão para esta operação.";
            case PESSOA_NAO_ENCONTRADA->"Pessoa não cadastrada. Cadastre-a em Pessoas antes de criar este vínculo.";
            case PROFESSOR_NAO_ENCONTRADO->"Cadastro de professor não encontrado. Na busca por CPF, a pessoa existe, mas não possui vínculo de professor.";
            case PROFESSOR_JA_CADASTRADO->"Esta pessoa já possui cadastro de professor.";
            case CONFLITO_EDICAO->"Os dados foram alterados por outra operação. Volte à busca e consulte novamente.";
            case RESPOSTA_INCONSISTENTE->"Não foi possível confirmar a identidade institucional.";
        }:ControladorAlunos.mensagemErro(erro);dialogos.erro(this,mensagem);
    }
}
