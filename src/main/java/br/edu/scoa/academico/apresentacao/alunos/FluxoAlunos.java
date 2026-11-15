package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;

/** Uma tarefa por tela; estado de seleção não é fornecido por campos editáveis. */
public final class FluxoAlunos extends JPanel {
    private final CardLayout navegacao = new CardLayout();
    private final JPanel paginas = new JPanel(navegacao);
    private final JLabel estado = new JLabel("Pronto.");
    private final JTextField cpf = new JTextField(24), nome = new JTextField(24), email = new JTextField(24), telefone = new JTextField(24), alunoId = new JTextField(24);
    private final JComboBox<SituacaoCadastral> situacao = new JComboBox<>(SituacaoCadastral.values()), novaSituacao = new JComboBox<>(SituacaoCadastral.values());
    private final JTextArea revisao = texto("textoRevisao"), detalhes = texto("textoDetalhes"), resultado = texto("textoResultado"), alteracao = texto("textoAlteracao");
    private final JLabel orientacaoDados = new JLabel();
    private final CadastroAlunoPorCpf cadastro;
    private final ControladorAlunos gestao;
    private final DialogosAluno dialogos;
    private final JTextField cpfBusca = new JTextField(24);
    private final JComboBox<String> tipoBusca = new JComboBox<>(new String[]{"ID interno", "CPF"});
    private final JPanel entradaId = new JPanel(), entradaCpf = new JPanel();
    private String cpfConsultado;
    private Aluno selecionado;
    private boolean ocupado;
    private final GestaoFichaAluno fichas;
    private FichaAluno fichaSelecionada;
    private final JTextField editarNome = new JTextField(24), editarEmail = new JTextField(24), editarTelefone = new JTextField(24);
    private final JLabel pessoaEmEdicao = new JLabel();

    public FluxoAlunos(CadastroAlunoPorCpf cadastro, ControladorAlunos gestao, DialogosAluno dialogos, String contexto) {
        this(cadastro, gestao, dialogos, contexto, null);
    }
    public FluxoAlunos(CadastroAlunoPorCpf cadastro, ControladorAlunos gestao, DialogosAluno dialogos, String contexto, GestaoFichaAluno fichas) {
        this.fichas = fichas;
        this.cadastro = cadastro; this.gestao = gestao; this.dialogos = dialogos;
        setLayout(new BorderLayout(16, 16)); setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        add(new JLabel(contexto), BorderLayout.NORTH); add(paginas, BorderLayout.CENTER);
        estado.setName("estado"); add(estado, BorderLayout.SOUTH);
        JPanel menu = pagina("menu", "Alunos", "Escolha o que deseja fazer.");
        botao(menu, "menuCadastrar", "Cadastrar aluno", () -> { limparCadastro(); mostrar("identificar"); });
        botao(menu, "menuConsultar", "Consultar aluno", this::novaBusca);

        JPanel identificar = pagina("identificar", "Localizar pessoa cadastrada", "Informe o CPF para localizar a pessoa institucional.");
        campo(identificar, "CPF — XXXXXXXXXXX ou XXX.XXX.XXX-XX", cpf, "cpf");
        identificar.add(new JLabel("Exemplo: 52998224725 ou 529.982.247-25."));
        botao(identificar, "buscarCpf", "Buscar CPF e continuar", () -> {
            String documento = cpf.getText(); limparDados();
            executar(() -> cadastro.consultar(documento), pessoa -> {
                if (pessoa.isEmpty()) { dialogos.erro(this,"Pessoa não cadastrada. Cadastre-a em Pessoas antes de criar este vínculo."); return; }
                cpfConsultado = documento;
                pessoa.ifPresent(p -> { nome.setText(p.nome()); email.setText(p.email()); telefone.setText(p.telefone()); });
                nome.setEditable(false); email.setEditable(false); telefone.setEditable(false);
                orientacaoDados.setText("Pessoa encontrada: confira os dados. Para editá-los, use Pessoas.");
                mostrar("dados");
            });
        });
        botao(identificar, "voltarMenu", "Voltar ao menu", this::menu);

        JPanel dados = pagina("dados", "Cadastrar aluno · 2 de 3", "Confira a pessoa e informe os dados do vínculo.");
        dados.add(orientacaoDados);
        campo(dados, "Nome completo *", nome, "nome"); campo(dados, "E-mail *", email, "email"); campo(dados, "Telefone (opcional)", telefone, "telefone");
        campo(dados, "Situação cadastral *", situacao, "situacao");
        botao(dados, "revisar", "Continuar para revisão", () -> {
            revisao.setText("CPF: " + cpfConsultado + "\nNome: " + nome.getText() + "\nE-mail: " + email.getText()
                    + "\nTelefone: " + telefone.getText() + "\nSituação: " + situacao.getSelectedItem()
                    + "\n\n" + "Será criado apenas o vínculo de aluno para a pessoa encontrada.");
            mostrar("revisao");
        });
        botao(dados, "voltarCpf", "Voltar à identificação", () -> { limparDados(); mostrar("identificar"); });
        botao(dados, "cancelarCadastro", "Cancelar cadastro", this::menu);

        JPanel confirmar = pagina("revisao", "Cadastrar aluno · 3 de 3", "Revise os dados. Nada foi gravado até este momento.");
        confirmar.add(revisao);
        botao(confirmar, "confirmarCadastro", "Confirmar cadastro", () -> {
            if (cpfConsultado == null) return;
            String documento = cpfConsultado, nomeInformado = nome.getText(), emailInformado = email.getText(), telefoneInformado = telefone.getText();
            SituacaoCadastral valor = (SituacaoCadastral)situacao.getSelectedItem();
            executar(() -> cadastro.cadastrar(documento, nomeInformado, emailInformado, telefoneInformado, valor, false), aluno -> {
                limparCadastro(); resultado.setText("Aluno cadastrado com sucesso.\n\nID do aluno: " + aluno.id() + "\nSituação: " + aluno.situacaoCadastral() + "\n\nConsulte o aluno pelo CPF ou pelo ID interno."); mostrar("resultado");
            });
        });
        botao(confirmar, "voltarDados", "Voltar e revisar dados", () -> mostrar("dados"));
        botao(confirmar, "cancelarRevisao", "Cancelar cadastro", this::menu);
        JPanel sucesso = pagina("resultado", "Cadastro concluído", "O vínculo de aluno foi criado."); sucesso.add(resultado);
        botao(sucesso, "resultadoMenu", "Voltar ao menu", this::menu);

        JPanel consulta = pagina("consulta", "Consultar aluno", "Escolha a forma de busca e informe o dado correspondente.");
        campo(consulta,"Buscar por",tipoBusca,"tipoBusca");
        entradaId.setLayout(new BoxLayout(entradaId,BoxLayout.Y_AXIS));
        entradaCpf.setLayout(new BoxLayout(entradaCpf,BoxLayout.Y_AXIS));
        campo(entradaId,"ID interno do aluno",alunoId,"alunoId");
        campo(entradaCpf,"CPF — XXXXXXXXXXX ou XXX.XXX.XXX-XX",cpfBusca,"cpfBusca");
        entradaCpf.add(new JLabel("Exemplo: 52998224725 ou 529.982.247-25."));
        consulta.add(entradaId); consulta.add(entradaCpf); entradaCpf.setVisible(false);
        if (fichas == null) tipoBusca.removeItem("CPF");
        tipoBusca.addActionListener(e -> { entradaId.setVisible(tipoBusca.getSelectedIndex()==0); entradaCpf.setVisible(tipoBusca.getSelectedIndex()==1); });
        botao(consulta, "buscarAluno", "Buscar aluno", () -> {
            selecionado = null; fichaSelecionada = null; detalhes.setText(""); String id = alunoId.getText();
            if (tipoBusca.getSelectedIndex()==1) { String documento=cpfBusca.getText(); executar(() -> cadastro.consultarAluno(documento,fichas),this::exibirFicha); }
            else if (fichas == null) executar(() -> gestao.consultar(id), this::exibirDetalhes);
            else executar(() -> fichas.consultar(id), this::exibirFicha);
        });
        botao(consulta, "consultaMenu", "Voltar ao menu", this::menu);
        JPanel ficha = pagina("detalhes", "Detalhes do aluno", "Confira o aluno localizado antes de solicitar uma alteração."); ficha.add(detalhes);
        if (fichas != null) botao(ficha, "editarPessoa", "Editar dados pessoais", () -> {
            if (fichaSelecionada == null) return;
            var pessoa = fichaSelecionada.pessoa();
            pessoaEmEdicao.setText("Aluno " + fichaSelecionada.aluno().id() + " · CPF: " + pessoa.cpf() + " (não editável)");
            editarNome.setText(pessoa.nome()); editarEmail.setText(pessoa.email()); editarTelefone.setText(pessoa.telefone()); mostrar("edicaoPessoa");
        });
        botao(ficha, "editarSituacao", "Alterar situação", () -> {
            if (selecionado == null) return;
            alteracao.setText("Aluno: " + selecionado.id() + "\nSituação atual: " + selecionado.situacaoCadastral());
            novaSituacao.setSelectedItem(selecionado.situacaoCadastral()); mostrar("alteracao");
        });
        botao(ficha, "novaBusca", "Voltar à busca", this::novaBusca);
        botao(ficha, "detalhesMenu", "Voltar ao menu", this::menu);
        JPanel editar = pagina("alteracao", "Alterar situação do aluno", "A alteração será aplicada somente ao aluno identificado abaixo."); editar.add(alteracao);
        campo(editar, "Nova situação", novaSituacao, "novaSituacao");
        botao(editar, "salvarSituacao", "Confirmar alteração", () -> {
            if (selecionado == null) return;
            long id = selecionado.id(); SituacaoCadastral valor = (SituacaoCadastral)novaSituacao.getSelectedItem();
            if (valor == selecionado.situacaoCadastral()) { dialogos.informar(this, "Nenhuma alteração: a situação permanece igual."); return; }
            if (dialogos.confirmar(this, "Alterar o aluno " + id + " de " + selecionado.situacaoCadastral() + " para " + valor + "?"))
                executar(() -> gestao.atualizar(Long.toString(id), valor), this::exibirDetalhes);
        });
        botao(editar, "cancelarAlteracao", "Voltar sem alterar", () -> mostrar("detalhes"));
        JPanel dadosPessoais = pagina("edicaoPessoa", "Editar dados pessoais", "<html>Nome e contato são compartilhados entre os vínculos desta pessoa.<br>Alterações também aparecerão em seu cadastro de professor, se houver.</html>");
        dadosPessoais.add(pessoaEmEdicao);
        campo(dadosPessoais, "Nome completo *", editarNome, "editarNome");
        campo(dadosPessoais, "E-mail *", editarEmail, "editarEmail");
        campo(dadosPessoais, "Telefone (opcional)", editarTelefone, "editarTelefone");
        botao(dadosPessoais, "salvarPessoa", "Salvar dados pessoais", () -> {
            if (fichas == null || fichaSelecionada == null) return;
            var esperada = fichaSelecionada;
            String novoNome = editarNome.getText(), novoEmail = editarEmail.getText(), novoTelefone = editarTelefone.getText();
            if (dialogos.confirmar(this, "Salvar dados pessoais compartilhados? A alteração aparecerá em todos os vínculos desta pessoa."))
                executar(() -> fichas.atualizar(esperada, novoNome, novoEmail, novoTelefone), this::exibirFicha);
        });
        botao(dadosPessoais, "cancelarPessoa", "Voltar sem salvar", () -> mostrar("detalhes"));
        mostrar("menu");
    }
    private JPanel pagina(String nome, String titulo, String orientacao) {
        JPanel painel = new JPanel(); painel.setName(nome); painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        JLabel cabecalho = new JLabel(titulo); cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD, 23f));
        painel.add(cabecalho); painel.add(Box.createVerticalStrut(12)); painel.add(new JLabel(orientacao)); painel.add(Box.createVerticalStrut(16));
        paginas.add(painel, nome); return painel;
    }
    private void campo(JPanel painel, String rotulo, JComponent campo, String nome) {
        campo.setName(nome); JLabel label = new JLabel(rotulo); label.setLabelFor(campo); painel.add(label);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); painel.add(campo); painel.add(Box.createVerticalStrut(10));
    }
    private void botao(JPanel painel, String nome, String titulo, Runnable acao) {
        JButton botao = new JButton(titulo); botao.setName(nome);
        botao.addActionListener(evento -> { if (!ocupado) acao.run(); }); painel.add(botao); painel.add(Box.createVerticalStrut(8));
    }
    private static JTextArea texto(String nome) {
        JTextArea area = new JTextArea(9, 48); area.setName(nome); area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setFont(UIManager.getFont("Label.font")); area.setBackground(UIManager.getColor("Panel.background")); return area;
    }
    private void mostrar(String tela) {
        for (Component pagina : paginas.getComponents()) {
            for (Component componente : ((Container)pagina).getComponents()) {
                if (componente instanceof JComponent visual) visual.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }
        navegacao.show(paginas, tela);
    }
    private void limparDados() { cpfConsultado = null; nome.setText(""); email.setText(""); telefone.setText(""); revisao.setText(""); situacao.setSelectedIndex(0); }
    private void limparCadastro() { cpf.setText(""); limparDados(); }
    private void menu() { limparCadastro(); fichaSelecionada = null; selecionado = null; detalhes.setText(""); alteracao.setText(""); resultado.setText(""); mostrar("menu"); }
    private void novaBusca() { fichaSelecionada = null; selecionado = null; detalhes.setText(""); alteracao.setText(""); alunoId.setText(""); cpfBusca.setText(""); mostrar("consulta"); }
    private void exibirFicha(FichaAluno ficha) { fichaSelecionada = ficha; exibirDetalhes(ficha.aluno()); }
    private void exibirDetalhes(Aluno aluno) {
        selecionado = aluno;
        String pessoais = "\n\nNome e contato ainda não estão disponíveis nesta consulta.";
        if (fichaSelecionada != null && fichaSelecionada.aluno().id() == aluno.id()) {
            fichaSelecionada = new FichaAluno(aluno, fichaSelecionada.pessoa());
            var pessoa = fichaSelecionada.pessoa();
            pessoais = "\n\nDADOS PESSOAIS COMPARTILHADOS\nNome: " + pessoa.nome() + "\nCPF: " + pessoa.cpf() + "\nE-mail: " + pessoa.email() + "\nTelefone: " + (pessoa.telefone() == null ? "" : pessoa.telefone());
        }
        detalhes.setText("DADOS DO ALUNO\nID do aluno: " + aluno.id() + "\nSituação cadastral: " + aluno.situacaoCadastral() + pessoais); mostrar("detalhes");
    }
    private void habilitar(Container pai, boolean valor) {
        for (Component filho : pai.getComponents()) { filho.setEnabled(valor); if (filho instanceof Container grupo) habilitar(grupo, valor); }
    }
    private <T> void executar(Callable<T> tarefa, Consumer<T> sucesso) {
        ocupado = true; habilitar(paginas, false); estado.setText("Processando…");
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return tarefa.call(); }
            protected void done() {
                try { sucesso.accept(get()); }
                catch (InterruptedException falha) { Thread.currentThread().interrupt(); erro(falha); }
                catch (ExecutionException falha) { erro(falha.getCause()); }
                finally { ocupado = false; habilitar(paginas, true); estado.setText("Pronto."); }
            }
        }.execute();
    }
    private void erro(Throwable falha) {
        String mensagem = ControladorAlunos.mensagemErro(falha);
        dialogos.erro(this, mensagem);
    }
}
