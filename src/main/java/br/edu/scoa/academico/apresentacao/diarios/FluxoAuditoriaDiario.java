package br.edu.scoa.academico.apresentacao.diarios;

import br.edu.scoa.academico.aplicacao.diarios.GestaoAuditoriaDiario;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.dominio.diarios.EstadoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.EventoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;
import br.edu.scoa.academico.dominio.diarios.ResumoAuditoriaDiario;
import br.edu.scoa.academico.dominio.diarios.TipoEventoPauta;
import br.edu.scoa.academico.dominio.turmas.Turma;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class FluxoAuditoriaDiario<S> extends JPanel {
    private final S sessao;
    private final GestaoAuditoriaDiario<S> gestao;
    private final DialogosAluno dialogos;
    private final JComboBox<Turma> turmas = new JComboBox<>();
    private final DefaultTableModel modeloDiario = new DefaultTableModel(
            new Object[]{"Inscrição", "Aluno", "Avaliação", "Data", "Nota"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final JTable tabelaDiario = new JTable(modeloDiario);
    private final JTextArea resumo = new JTextArea(12, 80);
    private final JButton analisar = botao("Registrar análise", this::registrarAnalise);
    private final JButton homologar = botao("Homologar e fechar pauta", this::homologar);
    private final JButton reabrir = botao("Reabrir pauta", this::reabrir);
    private ResumoAuditoriaDiario atual;
    private boolean ocupado;

    public FluxoAuditoriaDiario(S sessao, GestaoAuditoriaDiario<S> gestao, DialogosAluno dialogos) {
        this.sessao = java.util.Objects.requireNonNull(sessao);
        this.gestao = java.util.Objects.requireNonNull(gestao);
        this.dialogos = java.util.Objects.requireNonNull(dialogos);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(new JLabel("Auditoria de diários e homologação da pauta final"), BorderLayout.NORTH);

        var controles = new JPanel(new FlowLayout(FlowLayout.LEADING));
        turmas.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof Turma turma) setText(turma.codigo() + " · turma " + turma.id());
                return this;
            }
        });
        controles.add(new JLabel("Turma:"));
        controles.add(turmas);
        controles.add(botao("Atualizar", this::carregarTurmas));
        controles.add(analisar);
        controles.add(homologar);
        controles.add(reabrir);
        add(controles, BorderLayout.PAGE_START);

        resumo.setEditable(false);
        resumo.setLineWrap(true);
        resumo.setWrapStyleWord(true);
        var divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabelaDiario), new JScrollPane(resumo));
        divisao.setResizeWeight(0.62);
        add(divisao, BorderLayout.CENTER);
        turmas.addActionListener(evento -> atualizarResumo());
        atualizarAcoes();
        carregarTurmas();
    }

    private void carregarTurmas() {
        executar(() -> gestao.turmas(sessao), itens -> {
            var selecionada = (Turma) turmas.getSelectedItem();
            turmas.removeAllItems();
            itens.forEach(turmas::addItem);
            if (selecionada != null) {
                for (int indice = 0; indice < turmas.getItemCount(); indice++) {
                    if (turmas.getItemAt(indice).id() == selecionada.id()) turmas.setSelectedIndex(indice);
                }
            }
            if (turmas.getItemCount() == 0) limpar("Nenhuma turma disponível para auditoria.");
            else atualizarResumo();
        });
    }

    private void atualizarResumo() {
        Turma turma = (Turma) turmas.getSelectedItem();
        if (turma == null) { limpar("Selecione uma turma."); return; }
        executar(() -> gestao.consultar(sessao, turma.id()), this::mostrarResumo);
    }

    private void mostrarResumo(ResumoAuditoriaDiario dados) {
        atual = dados;
        modeloDiario.setRowCount(0);
        for (var registro : dados.diario()) {
            modeloDiario.addRow(new Object[]{registro.inscricaoId(), registro.nomeAluno(), registro.avaliacao().titulo(),
                    registro.avaliacao().dataAplicacao(), registro.nota().map(nota -> nota.valor().toPlainString()).orElse("Pendente")});
        }
        var texto = new StringBuilder();
        texto.append("Estado da pauta: ").append(dados.pauta().estado())
                .append(" · revisão ").append(dados.pauta().revisao()).append('\n');
        if (dados.pauta().homologadaEm() != null) {
            texto.append("Homologada por usuário ").append(dados.pauta().homologadaPor())
                    .append(" em ").append(dados.pauta().homologadaEm()).append('\n');
        }
        texto.append("\nPENDÊNCIAS\n");
        if (dados.pendencias().isEmpty()) texto.append("Nenhuma pendência impeditiva encontrada.\n");
        else dados.pendencias().forEach(pendencia -> texto.append("• ").append(pendencia.codigo())
                .append(": ").append(pendencia.descricao())
                .append(pendencia.inscricaoId() == null ? "" : " (inscrição " + pendencia.inscricaoId() + ")")
                .append('\n'));
        texto.append("\nHISTÓRICO DE ALTERAÇÕES\n");
        if (dados.historicoDados().isEmpty()) texto.append("Sem alterações registradas.\n");
        else dados.historicoDados().forEach(evento -> texto.append(evento.registradoEm()).append(" · ")
                .append(evento.categoria()).append(" · autor ").append(evento.autorId()).append(" · ")
                .append(evento.detalhes()).append('\n'));
        texto.append("\nDECISÕES DE REVISÃO\n");
        if (dados.historicoPauta().isEmpty()) texto.append("Nenhuma análise registrada.\n");
        else dados.historicoPauta().forEach(evento -> texto.append(evento.registradoEm()).append(" · ")
                .append(evento.evento()).append(" · autor ").append(evento.autorId())
                .append(evento.observacao().isBlank() ? "" : " · " + evento.observacao()).append('\n'));
        resumo.setText(texto.toString());
        resumo.setCaretPosition(0);
        atualizarAcoes();
    }

    private void registrarAnalise() {
        if (atual == null || atual.pauta().estado() != EstadoPautaFinal.ABERTA) return;
        String observacao = JOptionPane.showInputDialog(this, "Observação da análise (opcional):", "");
        if (observacao == null) return;
        long turmaId = atual.turma().id();
        executar(() -> gestao.registrarAnalise(sessao, turmaId, observacao, UUID.randomUUID()), pauta -> atualizarResumo());
    }

    private void homologar() {
        if (atual == null || atual.pauta().estado() != EstadoPautaFinal.ABERTA) return;
        if (!atual.pendencias().isEmpty()) {
            dialogos.erro(this, "A pauta não pode ser homologada enquanto houver pendências.");
            return;
        }
        if (atual.historicoPauta().stream().noneMatch(evento -> evento.evento() == TipoEventoPauta.ANALISE_REGISTRADA)) {
            dialogos.erro(this, "Registre a análise do coordenador antes de homologar.");
            return;
        }
        if (!dialogos.confirmar(this, "Homologar e fechar a pauta desta turma?")) return;
        long turmaId = atual.turma().id();
        long revisao = atual.pauta().revisao();
        executar(() -> gestao.homologar(sessao, turmaId, revisao, UUID.randomUUID()), pauta -> atualizarResumo());
    }

    private void reabrir() {
        if (atual == null || atual.pauta().estado() != EstadoPautaFinal.HOMOLOGADA) return;
        String motivo = JOptionPane.showInputDialog(this, "Motivo para reabrir (opcional):", "");
        if (motivo == null) return;
        long turmaId = atual.turma().id();
        long revisao = atual.pauta().revisao();
        executar(() -> gestao.reabrir(sessao, turmaId, revisao, motivo, UUID.randomUUID()), pauta -> atualizarResumo());
    }

    private void atualizarAcoes() {
        boolean aberta = atual != null && atual.pauta().estado() == EstadoPautaFinal.ABERTA;
        analisar.setEnabled(!ocupado && aberta);
        homologar.setEnabled(!ocupado && aberta && atual != null && atual.pendencias().isEmpty()
                && atual.historicoPauta().stream().anyMatch(evento -> evento.evento() == TipoEventoPauta.ANALISE_REGISTRADA));
        reabrir.setEnabled(!ocupado && atual != null && atual.pauta().estado() == EstadoPautaFinal.HOMOLOGADA);
    }

    private void limpar(String mensagem) {
        atual = null;
        modeloDiario.setRowCount(0);
        resumo.setText(mensagem);
        atualizarAcoes();
    }

    private JButton botao(String texto, Runnable acao) {
        var botao = new JButton(texto);
        botao.addActionListener(evento -> { if (!ocupado) acao.run(); });
        return botao;
    }

    private <T> void executar(java.util.concurrent.Callable<T> trabalho, java.util.function.Consumer<T> sucesso) {
        if (ocupado) return;
        ocupado = true;
        atualizarAcoes();
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return trabalho.call(); }
            @Override protected void done() {
                ocupado = false;
                atualizarAcoes();
                try { sucesso.accept(get()); }
                catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    dialogos.erro(FluxoAuditoriaDiario.this, "Operação interrompida.");
                } catch (ExecutionException erro) { mostrarErro(erro.getCause()); }
            }
        }.execute();
    }

    private void mostrarErro(Throwable erro) {
        if (erro instanceof FalhaAuditoriaDiario falha) {
            dialogos.erro(this, switch (falha.codigo()) {
                case PENDENCIAS_PARA_HOMOLOGAR -> "A pauta contém pendências e não pode ser fechada.";
                case INTEGRACAO_ACA04_INDISPONIVEL -> "A integração de aulas e frequência do ACA-04 ainda não está disponível.";
                case CONFLITO_REVISAO, CONFLITO_DIARIO -> "Os dados mudaram durante a revisão. Atualize a pauta e tente novamente.";
                case PAUTA_HOMOLOGADA -> "A pauta está homologada. Reabra-a antes de alterar dados.";
                case PAUTA_NAO_ANALISADA -> "Registre ou atualize a análise antes desta ação.";
                case NAO_AUTORIZADO -> "Apenas um coordenador pode auditar e homologar pautas.";
                default -> "Não foi possível concluir a ação: " + falha.codigo() + ".";
            });
        } else dialogos.erro(this, "Não foi possível carregar o diário. Confira a conexão e tente novamente.");
    }
}
