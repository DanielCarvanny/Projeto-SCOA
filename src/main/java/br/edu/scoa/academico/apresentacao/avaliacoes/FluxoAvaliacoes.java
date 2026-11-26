package br.edu.scoa.academico.apresentacao.avaliacoes;

import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.dominio.avaliacoes.*;
import br.edu.scoa.academico.dominio.turmas.Turma;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class FluxoAvaliacoes<S> extends JPanel {
    private final S sessao;
    private final GestaoAvaliacoes<S> gestao;
    private final DialogosAluno dialogos;
    private final JComboBox<Turma> turmas = new JComboBox<>();
    private final DefaultListModel<AvaliacaoAcademica> modeloAvaliacoes = new DefaultListModel<>();
    private final JList<AvaliacaoAcademica> listaAvaliacoes = new JList<>(modeloAvaliacoes);
    private final DefaultTableModel modeloNotas = new DefaultTableModel(new Object[]{"Inscrição", "Aluno", "Avaliação", "Nota"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final JTable tabelaNotas = new JTable(modeloNotas);
    private final DefaultTableModel modeloProprio = new DefaultTableModel(new Object[]{"Período", "Disciplina", "Turma", "Avaliação", "Data", "Nota"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final JTable tabelaPropria = new JTable(modeloProprio);
    private List<RegistroNota> registrosDiario = List.of();
    private List<RegistroNota> registrosProprios = List.of();
    private boolean ocupado;

    public FluxoAvaliacoes(S sessao, GestaoAvaliacoes<S> gestao, DialogosAluno dialogos) {
        this.sessao = Objects.requireNonNull(sessao);
        this.gestao = Objects.requireNonNull(gestao);
        this.dialogos = Objects.requireNonNull(dialogos);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        if (gestao.ehAluno(sessao)) montarConsultaAluno(); else montarDiario();
    }

    private void montarDiario() {
        add(new JLabel("Avaliações e notas — professor responsável ou coordenação"), BorderLayout.NORTH);
        var controles = new JPanel(new FlowLayout(FlowLayout.LEADING));
        turmas.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof Turma turma) setText(turma.codigo() + " · disciplina " + turma.disciplinaId() + " · turma " + turma.id());
                return this;
            }
        });
        controles.add(new JLabel("Turma:"));
        controles.add(turmas);
        controles.add(botao("Atualizar", this::atualizarTurmas));
        controles.add(botao("Cadastrar avaliação", this::cadastrarAvaliacao));
        controles.add(botao("Editar avaliação", this::editarAvaliacao));
        controles.add(botao("Lançar/alterar nota", this::lancarNotaSelecionada));
        add(controles, BorderLayout.PAGE_START);

        listaAvaliacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAvaliacoes.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof AvaliacaoAcademica avaliacao)
                    setText(avaliacao.dataAplicacao() + " · " + avaliacao.tipo() + " · " + avaliacao.titulo());
                return this;
            }
        });
        listaAvaliacoes.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) mostrarNotasDaAvaliacao(); });
        var divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(listaAvaliacoes), new JScrollPane(tabelaNotas));
        divisao.setResizeWeight(0.35);
        add(divisao, BorderLayout.CENTER);
        turmas.addActionListener(e -> atualizarAvaliacoes());
        atualizarTurmas();
    }

    private void montarConsultaAluno() {
        add(new JLabel("Minhas avaliações e notas"), BorderLayout.NORTH);
        add(new JScrollPane(tabelaPropria), BorderLayout.CENTER);
        var resumo = new JTextArea(4, 60);
        resumo.setEditable(false);
        resumo.setLineWrap(true);
        resumo.setWrapStyleWord(true);
        add(new JScrollPane(resumo), BorderLayout.SOUTH);
        executar(() -> gestao.minhasNotas(sessao), registros -> {
            registrosProprios = registros;
            modeloProprio.setRowCount(0);
            for (var registro : registros) {
                modeloProprio.addRow(new Object[]{registro.periodo(), registro.disciplina(), registro.turma(),
                        registro.avaliacao().titulo(), registro.avaliacao().dataAplicacao(),
                        registro.nota().map(Nota::valor).orElse(null) == null ? "Pendente" : registro.nota().orElseThrow().valor().toPlainString()});
            }
            var texto = new StringBuilder();
            var porTurma = registros.stream().collect(java.util.stream.Collectors.groupingBy(r -> r.avaliacao().turmaId()));
            porTurma.forEach((turmaId, itens) -> {
                var media = gestao.mediaDasProvas(itens);
                var elegibilidade = gestao.elegivelParaExame(itens);
                texto.append("Turma ").append(turmaId).append(" — média das provas: ")
                        .append(media.map(BigDecimal::toPlainString).orElse("pendente"))
                        .append("; exame final: ")
                        .append(elegibilidade.map(valor -> valor ? "elegível" : "não elegível").orElse("pendente"));
                try {
                    var resultado = gestao.resultadoFinal(itens);
                    switch (resultado.situacao()) {
                        case PENDENTE_PROVAS -> texto.append("; resultado final: pendente (notas de prova)");
                        case PENDENTE_EXAME -> texto.append("; resultado final: pendente (exame final)");
                        case APROVADO -> texto.append("; média final: ")
                                .append(resultado.mediaFinal().orElseThrow().valor().toPlainString()).append(" — aprovado após exame");
                        case REPROVADO -> {
                            if (resultado.notaExame().isPresent()) texto.append("; média final: ")
                                    .append(resultado.mediaFinal().orElseThrow().valor().toPlainString()).append(" — reprovado após exame");
                            else texto.append("; resultado final: reprovado pela média das provas");
                        }
                    }
                } catch (FalhaAvaliacao falha) {
                    if (falha.codigo() == FalhaAvaliacao.Codigo.EXAME_FINAL_MULTIPLO)
                        texto.append("; média final: sem cálculo (mais de uma avaliação do tipo exame final)");
                    else throw falha;
                }
                texto.append('\n');
            });
            resumo.setText(texto.isEmpty() ? "Nenhuma avaliação registrada." : texto.toString());
        });
    }

    private void atualizarTurmas() {
        executar(() -> gestao.turmasDisponiveis(sessao), itens -> {
            turmas.removeAllItems();
            itens.forEach(turmas::addItem);
            if (itens.isEmpty()) dialogos.informar(this, "Não há turmas disponíveis para este perfil.");
        });
    }

    private void atualizarAvaliacoes() {
        Turma turma = (Turma) turmas.getSelectedItem();
        if (turma == null) { limparDiario(); return; }
        executar(() -> new DadosDiario(gestao.porTurma(sessao, turma.id()), gestao.diario(sessao, turma.id())), dados -> {
            modeloAvaliacoes.clear();
            dados.avaliacoes().forEach(modeloAvaliacoes::addElement);
            registrosDiario = dados.registros();
            if (!modeloAvaliacoes.isEmpty()) listaAvaliacoes.setSelectedIndex(0); else preencherTabela(List.of());
        });
    }

    private void mostrarNotasDaAvaliacao() {
        AvaliacaoAcademica avaliacao = listaAvaliacoes.getSelectedValue();
        preencherTabela(avaliacao == null ? List.of() : registrosDiario.stream()
                .filter(registro -> registro.avaliacao().id() == avaliacao.id()).toList());
    }

    private void preencherTabela(List<RegistroNota> itens) {
        modeloNotas.setRowCount(0);
        registrosDiario = List.copyOf(registrosDiario);
        for (var registro : itens) modeloNotas.addRow(new Object[]{registro.inscricaoId(), registro.nomeAluno(),
                registro.avaliacao().titulo(), registro.nota().map(n -> n.valor().toPlainString()).orElse("Pendente")});
    }

    private void cadastrarAvaliacao() {
        Turma turma = (Turma) turmas.getSelectedItem();
        if (turma == null) { dialogos.erro(this, "Selecione uma turma."); return; }
        var dados = formulario(null);
        if (dados == null) return;
        var nova = new AvaliacaoAcademica(0, turma.id(), dados.titulo(), dados.descricao(), dados.data(), dados.tipo(), 0);
        if (!dialogos.confirmar(this, "Cadastrar avaliação?\n" + nova.titulo() + " · " + nova.dataAplicacao() + " · " + nova.tipo())) return;
        executar(() -> gestao.cadastrar(sessao, nova, UUID.randomUUID()), salvo -> {
            dialogos.informar(this, "Avaliação cadastrada.");
            atualizarAvaliacoes();
        });
    }

    private void editarAvaliacao() {
        AvaliacaoAcademica atual = listaAvaliacoes.getSelectedValue();
        if (atual == null) { dialogos.erro(this, "Selecione uma avaliação."); return; }
        var dados = formulario(atual);
        if (dados == null) return;
        var alterada = new AvaliacaoAcademica(atual.id(), atual.turmaId(), dados.titulo(), dados.descricao(),
                dados.data(), dados.tipo(), atual.revisao());
        if (!dialogos.confirmar(this, "Alterar avaliação? As alterações serão registradas na auditoria.\n" + alterada.titulo())) return;
        executar(() -> gestao.atualizar(sessao, alterada, UUID.randomUUID()), salvo -> {
            dialogos.informar(this, "Avaliação alterada e auditada.");
            atualizarAvaliacoes();
        });
    }

    private Formulario formulario(AvaliacaoAcademica atual) {
        var titulo = new JTextField(atual == null ? "" : atual.titulo(), 24);
        var descricao = new JTextField(atual == null ? "" : atual.descricao(), 24);
        var data = new JTextField(atual == null ? "" : atual.dataAplicacao().toString(), 24);
        var tipo = new JComboBox<>(TipoAvaliacao.values());
        if (atual != null) tipo.setSelectedItem(atual.tipo());
        var painel = new JPanel(new GridLayout(0, 1, 4, 4));
        painel.add(new JLabel("Título")); painel.add(titulo);
        painel.add(new JLabel("Descrição (opcional)")); painel.add(descricao);
        painel.add(new JLabel("Data (AAAA-MM-DD)")); painel.add(data);
        painel.add(new JLabel("Tipo")); painel.add(tipo);
        if (JOptionPane.showConfirmDialog(this, painel, atual == null ? "Nova avaliação" : "Editar avaliação",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
        try {
            return new Formulario(titulo.getText(), descricao.getText(), LocalDate.parse(data.getText().strip()),
                    (TipoAvaliacao) tipo.getSelectedItem());
        } catch (RuntimeException erro) {
            dialogos.erro(this, "Informe título e data válida no formato AAAA-MM-DD.");
            return null;
        }
    }

    private void lancarNotaSelecionada() {
        int linha = tabelaNotas.getSelectedRow();
        if (linha < 0) { dialogos.erro(this, "Selecione um aluno no diário."); return; }
        AvaliacaoAcademica avaliacao = listaAvaliacoes.getSelectedValue();
        long inscricaoId = ((Number) modeloNotas.getValueAt(linha, 0)).longValue();
        var registro = registrosDiario.stream().filter(r -> r.avaliacao().id() == avaliacao.id() && r.inscricaoId() == inscricaoId)
                .findFirst().orElse(null);
        if (registro == null) { dialogos.erro(this, "Atualize o diário e tente novamente."); return; }
        String entrada = JOptionPane.showInputDialog(this, "Nota de 0,0 a 10,0 (uma casa decimal):",
                registro.nota().map(n -> n.valor().toPlainString()).orElse(""));
        if (entrada == null) return;
        try {
            var nota = new Nota(new BigDecimal(entrada.strip().replace(',', '.')));
            String resumo = registro.nomeAluno() + " · " + avaliacao.titulo() + " · " + nota.valor().toPlainString();
            if (!dialogos.confirmar(this, "Confirmar lançamento?\n" + resumo)) return;
            executar(() -> gestao.lancarNota(sessao, avaliacao.id(), inscricaoId, nota,
                    registro.revisaoNota(), UUID.randomUUID()), salvo -> {
                dialogos.informar(this, "Nota salva e auditada.");
                atualizarAvaliacoes();
            });
        } catch (NumberFormatException | FalhaAvaliacao erro) {
            dialogos.erro(this, "Nota inválida. Use um valor entre 0,0 e 10,0 com até uma casa decimal.");
        }
    }

    private void limparDiario() {
        modeloAvaliacoes.clear();
        registrosDiario = List.of();
        preencherTabela(List.of());
    }

    private JButton botao(String texto, Runnable acao) {
        var botao = new JButton(texto);
        botao.addActionListener(evento -> { if (!ocupado) acao.run(); });
        return botao;
    }

    private <T> void executar(java.util.concurrent.Callable<T> trabalho, Consumer<T> sucesso) {
        if (ocupado) return;
        ocupado = true;
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return trabalho.call(); }
            @Override protected void done() {
                ocupado = false;
                try { sucesso.accept(get()); }
                catch (InterruptedException erro) { Thread.currentThread().interrupt(); dialogos.erro(FluxoAvaliacoes.this, "Operação interrompida."); }
                catch (ExecutionException erro) { mostrarErro(erro.getCause()); }
            }
        }.execute();
    }

    private void mostrarErro(Throwable erro) {
        if (erro instanceof FalhaAvaliacao falha) dialogos.erro(this, "Não foi possível concluir: " + falha.codigo() + ".");
        else dialogos.erro(this, "Não foi possível acessar as avaliações. Verifique a conexão e tente novamente.");
    }

    private record Formulario(String titulo, String descricao, LocalDate data, TipoAvaliacao tipo) { }
    private record DadosDiario(List<AvaliacaoAcademica> avaliacoes, List<RegistroNota> registros) { }
}
