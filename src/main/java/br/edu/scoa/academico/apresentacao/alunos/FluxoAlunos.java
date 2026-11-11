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
    private String cpfConsultado;
    private boolean pessoaNova;
    private Aluno selecionado;
    private boolean ocupado;

    public FluxoAlunos(CadastroAlunoPorCpf cadastro, ControladorAlunos gestao, DialogosAluno dialogos, String contexto) {
        this.cadastro = cadastro; this.gestao = gestao; this.dialogos = dialogos;
        setLayout(new BorderLayout(16, 16)); setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        add(new JLabel(contexto), BorderLayout.NORTH); add(paginas, BorderLayout.CENTER);
        estado.setName("estado"); add(estado, BorderLayout.SOUTH);
        JPanel menu = pagina("menu", "Alunos", "Escolha o que deseja fazer.");
        botao(menu, "menuCadastrar", "Cadastrar aluno", () -> { limparCadastro(); mostrar("identificar"); });
        botao(menu, "menuConsultar", "Consultar aluno", this::novaBusca);

        JPanel identificar = pagina("identificar", "Cadastrar aluno · 1 de 3", "Informe o CPF para localizar a pessoa institucional.");
        campo(identificar, "CPF", cpf, "cpf");
        botao(identificar, "buscarCpf", "Buscar CPF e continuar", () -> {
            String documento = cpf.getText(); limparDados();
            executar(() -> cadastro.consultar(documento), pessoa -> {
                cpfConsultado = documento; pessoaNova = pessoa.isEmpty();
                pessoa.ifPresent(p -> { nome.setText(p.nome()); email.setText(p.email()); telefone.setText(p.telefone()); });
                nome.setEditable(pessoaNova); email.setEditable(pessoaNova); telefone.setEditable(pessoaNova);
                orientacaoDados.setText(pessoaNova ? "Pessoa nova: preencha os campos obrigatórios (*)." : "Pessoa encontrada: confira os dados. Eles não serão alterados.");
                mostrar("dados");
            });
        });
        botao(identificar, "voltarMenu", "Voltar ao menu", this::menu);

        JPanel dados = pagina("dados", "Cadastrar aluno · 2 de 3", "Confira ou complete os dados antes de continuar.");
        dados.add(orientacaoDados);
        campo(dados, "Nome completo *", nome, "nome"); campo(dados, "E-mail *", email, "email"); campo(dados, "Telefone (opcional)", telefone, "telefone");
        campo(dados, "Situação cadastral *", situacao, "situacao");
        botao(dados, "revisar", "Continuar para revisão", () -> {
            if (pessoaNova && (nome.getText().isBlank() || nome.getText().trim().length() > 200
                    || !email.getText().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || email.getText().trim().length() > 254 || telefone.getText().trim().length() > 30)) {
                dialogos.erro(this, "Confira nome e e-mail obrigatórios e o tamanho dos campos."); return;
            }
            revisao.setText("CPF: " + cpfConsultado + "\nNome: " + nome.getText() + "\nE-mail: " + email.getText()
                    + "\nTelefone: " + telefone.getText() + "\nSituação: " + situacao.getSelectedItem()
                    + "\n\n" + (pessoaNova ? "Será criada uma pessoa e seu vínculo de aluno." : "Será criado o vínculo de aluno para a pessoa encontrada."));
            mostrar("revisao");
        });
        botao(dados, "voltarCpf", "Voltar ao CPF", () -> { limparDados(); mostrar("identificar"); });
        botao(dados, "cancelarCadastro", "Cancelar cadastro", this::menu);

        JPanel confirmar = pagina("revisao", "Cadastrar aluno · 3 de 3", "Revise os dados. Nada foi gravado até este momento.");
        confirmar.add(revisao);
        botao(confirmar, "confirmarCadastro", "Confirmar cadastro", () -> {
            if (cpfConsultado == null) return;
            String documento = cpfConsultado, nomeInformado = nome.getText(), emailInformado = email.getText(), telefoneInformado = telefone.getText();
            SituacaoCadastral valor = (SituacaoCadastral)situacao.getSelectedItem();
            executar(() -> cadastro.cadastrar(documento, nomeInformado, emailInformado, telefoneInformado, valor), aluno -> {
                limparCadastro(); resultado.setText("Aluno cadastrado com sucesso.\n\nID do aluno: " + aluno.id() + "\nSituação: " + aluno.situacaoCadastral() + "\n\nAnote o ID para consultar o aluno."); mostrar("resultado");
            });
        });
        botao(confirmar, "voltarDados", "Voltar e revisar dados", () -> mostrar("dados"));
        botao(confirmar, "cancelarRevisao", "Cancelar cadastro", this::menu);
        JPanel sucesso = pagina("resultado", "Cadastro concluído", "O vínculo de aluno foi criado."); sucesso.add(resultado);
        botao(sucesso, "resultadoMenu", "Voltar ao menu", this::menu);

        JPanel consulta = pagina("consulta", "Consultar aluno", "Informe o ID recebido ao concluir o cadastro.");
        campo(consulta, "ID do aluno", alunoId, "alunoId");
        botao(consulta, "buscarAluno", "Buscar aluno", () -> {
            selecionado = null; detalhes.setText(""); String id = alunoId.getText();
            executar(() -> gestao.consultar(id), this::exibirDetalhes);
        });
        botao(consulta, "consultaMenu", "Voltar ao menu", this::menu);
        JPanel ficha = pagina("detalhes", "Detalhes do aluno", "Confira o aluno localizado antes de solicitar uma alteração."); ficha.add(detalhes);
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
    private void menu() { limparCadastro(); selecionado = null; detalhes.setText(""); alteracao.setText(""); resultado.setText(""); mostrar("menu"); }
    private void novaBusca() { selecionado = null; detalhes.setText(""); alteracao.setText(""); alunoId.setText(""); mostrar("consulta"); }
    private void exibirDetalhes(Aluno aluno) {
        selecionado = aluno;
        detalhes.setText("ID do aluno: " + aluno.id() + "\nSituação cadastral: " + aluno.situacaoCadastral()
                + "\n\nNome e contato ainda não estão disponíveis nesta consulta."); mostrar("detalhes");
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
        String mensagem = falha instanceof CadastroAlunoException e && e.codigo() == ErroCadastroAluno.ENTRADA_INVALIDA
                ? "Confira os dados informados. CPF deve ter 11 dígitos válidos; nome e e-mail são obrigatórios para pessoa nova."
                : ControladorAlunos.mensagemErro(falha);
        dialogos.erro(this, mensagem);
    }
}
