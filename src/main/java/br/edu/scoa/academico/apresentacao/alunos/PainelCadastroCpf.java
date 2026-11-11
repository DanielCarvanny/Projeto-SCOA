package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.*;

/** Busca por CPF antes de confirmar. Nenhum acesso direto à tabela pessoa. */
public final class PainelCadastroCpf extends JPanel {
    private final JTextField cpf = new JTextField(20);
    private final JTextField nome = new JTextField(20);
    private final JTextField email = new JTextField(20);
    private final JTextField telefone = new JTextField(20);
    private final JTextField alunoId = new JTextField(20);
    private final JComboBox<SituacaoCadastral> situacao = new JComboBox<>(SituacaoCadastral.values());
    private final JButton buscar = new JButton("Buscar CPF");
    private final JButton cadastrar = new JButton("Cadastrar aluno");
    private final JButton consultar = new JButton("Consultar aluno");
    private final JButton atualizar = new JButton("Atualizar situação");
    private final JButton limpar = new JButton("Limpar");
    private final JLabel estado = new JLabel("Informe o CPF e clique em Buscar CPF.");
    private final CadastroAlunoPorCpf cadastro;
    private final ControladorAlunos gestao;
    private final DialogosAluno dialogos;
    private boolean consultado;
    private boolean pessoaNova;
    private boolean ocupado;

    public PainelCadastroCpf(CadastroAlunoPorCpf cadastro, ControladorAlunos gestao, DialogosAluno dialogos, String contexto) {
        this.cadastro = cadastro; this.gestao = gestao; this.dialogos = dialogos;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel topo = new JPanel(new GridLayout(0, 1, 0, 8));
        JLabel titulo = new JLabel("SCOA · Cadastro de alunos por CPF");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        topo.add(titulo); topo.add(new JLabel(contexto));
        topo.add(new JLabel("Busque o CPF, confira os dados e confirme o cadastro de aluno."));
        add(topo, BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(0, 2, 12, 10));
        campo(campos, "CPF", cpf, "cpf"); campo(campos, "Nome completo *", nome, "nome");
        campo(campos, "E-mail *", email, "email"); campo(campos, "Telefone (opcional)", telefone, "telefone");
        campo(campos, "Situação cadastral", situacao, "situacao");
        campo(campos, "ID do aluno (consulta/alteração)", alunoId, "alunoId");
        add(campos, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new GridLayout(0, 1, 0, 8));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton[] acoes = {buscar, cadastrar, consultar, atualizar, limpar};
        String[] nomes = {"buscar", "cadastrar", "consultar", "atualizar", "limpar"};
        for (int i = 0; i < acoes.length; i++) { acoes[i].setName(nomes[i]); botoes.add(acoes[i]); }
        rodape.add(botoes); estado.setName("estado"); rodape.add(estado); add(rodape, BorderLayout.SOUTH);
        cpf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evento) { invalidar(); }
            public void removeUpdate(DocumentEvent evento) { invalidar(); }
            public void changedUpdate(DocumentEvent evento) { invalidar(); }
        });
        buscar.addActionListener(evento -> {
            String documento = cpf.getText(); invalidar();
            executar(() -> cadastro.consultar(documento), encontrada -> {
                consultado = true; pessoaNova = encontrada.isEmpty();
                encontrada.ifPresent(pessoa -> { nome.setText(pessoa.nome()); email.setText(pessoa.email()); telefone.setText(pessoa.telefone()); });
                estado.setText(pessoaNova ? "Pessoa não encontrada. Preencha os dados obrigatórios para cadastrar." : "Pessoa encontrada. Confira os dados antes de cadastrar o aluno.");
            });
        });
        cadastrar.addActionListener(evento -> {
            if (!consultado || ocupado) return;
            String documento = cpf.getText(), nomeInformado = nome.getText(), emailInformado = email.getText(), telefoneInformado = telefone.getText();
            SituacaoCadastral valor = (SituacaoCadastral) situacao.getSelectedItem();
            if (!dialogos.confirmar(this, pessoaNova ? "Cadastrar a pessoa informada e seu vínculo de aluno?" : "Criar o vínculo de aluno para " + nomeInformado + "?")) return;
            executar(() -> cadastro.cadastrar(documento, nomeInformado, emailInformado, telefoneInformado, valor), aluno -> {
                alunoId.setText(Long.toString(aluno.id())); consultado = false;
                estado.setText("Aluno cadastrado. ID: " + aluno.id()); dialogos.informar(this, "Aluno cadastrado. ID: " + aluno.id());
            });
        });
        consultar.addActionListener(evento -> {
            String id = alunoId.getText();
            invalidar(); cpf.setText("");
            executar(() -> gestao.consultar(id), aluno -> {
                alunoId.setText(Long.toString(aluno.id())); situacao.setSelectedItem(aluno.situacaoCadastral());
                estado.setText("Aluno consultado. Dados pessoais são consultados separadamente pelo CPF.");
            });
        });
        atualizar.addActionListener(evento -> {
            String id = alunoId.getText(); SituacaoCadastral valor = (SituacaoCadastral) situacao.getSelectedItem();
            if (!dialogos.confirmar(this, "Alterar o aluno " + id + " para " + valor + "?")) return;
            executar(() -> gestao.atualizar(id, valor), aluno -> {
                situacao.setSelectedItem(aluno.situacaoCadastral()); estado.setText("Situação atualizada.");
                dialogos.informar(this, "Situação atualizada.");
            });
        });
        limpar.addActionListener(evento -> { cpf.setText(""); invalidar(); situacao.setSelectedIndex(0); });
        atualizarControles();
    }
    private void invalidar() {
        consultado = false; pessoaNova = false;
        nome.setText(""); email.setText(""); telefone.setText(""); alunoId.setText("");
        estado.setText("Informe o CPF e clique em Buscar CPF."); atualizarControles();
    }
    private void atualizarControles() {
        for (JComponent componente : new JComponent[]{cpf, buscar, consultar, atualizar, limpar, alunoId, situacao}) componente.setEnabled(!ocupado);
        cadastrar.setEnabled(!ocupado && consultado);
        for (JTextField campo : new JTextField[]{nome, email, telefone}) campo.setEditable(!ocupado && consultado && pessoaNova);
    }
    private static void campo(JPanel painel, String texto, JComponent campo, String nome) {
        JLabel rotulo = new JLabel(texto); rotulo.setLabelFor(campo); campo.setName(nome); painel.add(rotulo); painel.add(campo);
    }
    private <T> void executar(Callable<T> operacao, Consumer<T> sucesso) {
        ocupado = true; atualizarControles(); estado.setText("Processando…");
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return operacao.call(); }
            protected void done() {
                try { sucesso.accept(get()); }
                catch (InterruptedException falha) { Thread.currentThread().interrupt(); erro(falha); }
                catch (ExecutionException falha) { erro(falha.getCause()); }
                finally { ocupado = false; atualizarControles(); }
            }
        }.execute();
    }
    private void erro(Throwable falha) {
        estado.setText("Operação não concluída.");
        String mensagem = falha instanceof CadastroAlunoException excecao && excecao.codigo() == ErroCadastroAluno.ENTRADA_INVALIDA
                ? "Confira o CPF (incluindo os dígitos verificadores), nome, e-mail e situação cadastral."
                : ControladorAlunos.mensagemErro(falha);
        dialogos.erro(this, mensagem);
    }
}
