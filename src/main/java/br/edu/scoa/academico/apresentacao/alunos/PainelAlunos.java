package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/** Formulário simples. Os casos de uso continuam responsáveis por autorização e regras. */
public final class PainelAlunos extends JPanel {
    private final JTextField pessoaId = new JTextField(20);
    private final JTextField alunoId = new JTextField(20);
    private final JComboBox<SituacaoCadastral> situacao = new JComboBox<>(SituacaoCadastral.values());
    private final JButton cadastrar = new JButton("Cadastrar");
    private final JButton consultar = new JButton("Consultar");
    private final JButton atualizar = new JButton("Atualizar situação");
    private final JButton limpar = new JButton("Limpar");
    private final JLabel estado = new JLabel("Pronto.");
    private final ControladorAlunos controlador;
    private final DialogosAluno dialogos;

    public PainelAlunos(ControladorAlunos controlador, DialogosAluno dialogos, String contexto) {
        this.controlador = controlador;
        this.dialogos = dialogos;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel cabecalho = new JPanel(new GridLayout(0, 1, 0, 6));
        JLabel titulo = new JLabel("SCOA · Cadastro de alunos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        cabecalho.add(titulo);
        cabecalho.add(new JLabel(contexto));
        cabecalho.add(new JLabel("Cadastre pelo ID da pessoa. Consulte ou atualize pelo ID do aluno."));
        add(cabecalho, BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(0, 2, 12, 12));
        campo(campos, "ID da pessoa institucional", pessoaId, "pessoaId");
        campo(campos, "ID do aluno", alunoId, "alunoId");
        campo(campos, "Situação cadastral", situacao, "situacao");
        add(campos, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton[] acoes = {cadastrar, consultar, atualizar, limpar};
        String[] nomes = {"cadastrar", "consultar", "atualizar", "limpar"};
        for (int i = 0; i < acoes.length; i++) { acoes[i].setName(nomes[i]); botoes.add(acoes[i]); }
        estado.setName("estado");
        rodape.add(botoes, BorderLayout.NORTH);
        rodape.add(estado, BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);
        cadastrar.addActionListener(evento -> {
            String id = pessoaId.getText();
            SituacaoCadastral valor = (SituacaoCadastral) situacao.getSelectedItem();
            executar(() -> controlador.cadastrar(id, valor), "Aluno cadastrado.");
        });
        consultar.addActionListener(evento -> {
            String id = alunoId.getText();
            executar(() -> controlador.consultar(id), "Aluno consultado.");
        });
        atualizar.addActionListener(evento -> {
            String id = alunoId.getText();
            SituacaoCadastral valor = (SituacaoCadastral) situacao.getSelectedItem();
            if (dialogos.confirmar(this, "Alterar a situação do aluno " + id + " para " + valor + "?")) {
                executar(() -> controlador.atualizar(id, valor), "Situação atualizada.");
            }
        });
        limpar.addActionListener(evento -> {
            pessoaId.setText(""); alunoId.setText(""); situacao.setSelectedIndex(0);
            estado.setText("Pronto."); pessoaId.requestFocusInWindow();
        });
    }
    private void campo(JPanel painel, String texto, JComponent componente, String nome) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setLabelFor(componente); componente.setName(nome);
        painel.add(rotulo); painel.add(componente);
    }
    private void habilitar(boolean valor) {
        for (JComponent componente : new JComponent[]{pessoaId, alunoId, situacao, cadastrar, consultar, atualizar, limpar}) {
            componente.setEnabled(valor);
        }
    }
    private void executar(Callable<Aluno> operacao, String sucesso) {
        habilitar(false);
        estado.setText("Processando…");
        new SwingWorker<Aluno, Void>() {
            protected Aluno doInBackground() throws Exception { return operacao.call(); }
            protected void done() {
                try {
                    Aluno resultado = get();
                    pessoaId.setText(Long.toString(resultado.pessoaId()));
                    alunoId.setText(Long.toString(resultado.id()));
                    situacao.setSelectedItem(resultado.situacaoCadastral());
                    estado.setText(sucesso);
                    dialogos.informar(PainelAlunos.this, sucesso);
                } catch (InterruptedException falha) {
                    Thread.currentThread().interrupt();
                    mostrarErro(falha);
                } catch (ExecutionException falha) {
                    mostrarErro(falha.getCause());
                } finally { habilitar(true); }
            }
        }.execute();
    }
    private void mostrarErro(Throwable falha) {
        estado.setText("Operação não concluída.");
        dialogos.erro(this, ControladorAlunos.mensagemErro(falha));
    }
}
