package br.edu.scoa.academico.apresentacao.boletins;

import br.edu.scoa.academico.aplicacao.boletins.*;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.dominio.avaliacoes.ResultadoFinalAcademico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class FluxoBoletimAluno extends JPanel {
    private final Sessao sessao;
    private final GestaoBoletimAluno<Sessao> gestao;
    private final DialogosAluno dialogos;
    private final JButton atualizar = new JButton("Atualizar consultas");
    private final DefaultTableModel modeloBoletim = modelo("Curso", "Período", "Disciplina", "Turma",
            "Média provas", "Nota exame", "Média final", "Resultado");
    private final DefaultTableModel modeloLancamentos = modelo("Avaliação", "Tipo", "Data", "Nota");
    private final DefaultTableModel modeloHistorico = modelo("Matrícula", "Curso", "Matriz", "Disciplina", "Relação com o plano", "Turma", "Inscrição", "Resultado");
    private final DefaultTableModel modeloPlano = modelo("Curso", "Matriz", "Período ideal", "Disciplina", "Tipo", "Carga horária");
    private final DefaultTableModel modeloHorarios = modelo("Dia", "Início", "Fim", "Curso", "Período", "Disciplina", "Turma", "Sala");
    private final JTable tabelaBoletim = new JTable(modeloBoletim);
    private final JTable tabelaLancamentos = new JTable(modeloLancamentos);
    private final List<DisciplinaBoletimAluno> linhasBoletim = new ArrayList<>();
    private boolean ocupado;

    public FluxoBoletimAluno(Sessao sessao, GestaoBoletimAluno<Sessao> gestao, DialogosAluno dialogos) {
        this.sessao = java.util.Objects.requireNonNull(sessao);
        this.gestao = java.util.Objects.requireNonNull(gestao);
        this.dialogos = java.util.Objects.requireNonNull(dialogos);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(new JLabel("Boletim, histórico acadêmico e horários"), BorderLayout.NORTH);
        var conteudo = new JTabbedPane();
        conteudo.addTab("Boletim", painelBoletim());
        conteudo.addTab("Histórico e plano curricular", painelHistorico());
        conteudo.addTab("Horários", new JScrollPane(new JTable(modeloHorarios)));
        add(conteudo, BorderLayout.CENTER);
        atualizar.addActionListener(evento -> carregar());
        var acoes = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        acoes.add(atualizar);
        add(acoes, BorderLayout.SOUTH);
        carregar();
    }

    private JComponent painelBoletim() {
        tabelaBoletim.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaBoletim.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) mostrarLancamentos(tabelaBoletim.getSelectedRow());
        });
        var divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabelaBoletim), new JScrollPane(tabelaLancamentos));
        divisao.setResizeWeight(0.68);
        return divisao;
    }

    private JComponent painelHistorico() {
        var plano = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(new JTable(modeloHistorico)), new JScrollPane(new JTable(modeloPlano)));
        plano.setResizeWeight(0.58);
        return plano;
    }

    private void carregar() {
        executar(() -> gestao.consultar(sessao), this::mostrar);
    }

    private void mostrar(ResumoBoletimAluno dados) {
        linhasBoletim.clear();
        modeloBoletim.setRowCount(0);
        modeloHistorico.setRowCount(0);
        modeloPlano.setRowCount(0);
        modeloHorarios.setRowCount(0);

        for (var matricula : dados.matriculas()) {
            for (var componente : matricula.componentesPlano()) {
                modeloPlano.addRow(new Object[]{matricula.curso().toString(), matricula.matriz().codigo() + " · " + matricula.matriz().versao(),
                        componente.componente().periodoIdeal(), componente.disciplina().toString(),
                        componente.componente().tipo(), componente.componente().cargaHorariaConsiderada()});
            }
            for (var disciplina : matricula.historicoDisciplinas()) {
                String resultado = textoResultado(disciplina);
                modeloHistorico.addRow(new Object[]{matricula.matricula().numeroMatricula(), matricula.curso().toString(),
                        matricula.matriz().codigo() + " · " + matricula.matriz().versao(), disciplina.disciplina().toString(),
                        disciplina.componentePlano().map(componente -> "Previsto · período " + componente.componente().periodoIdeal())
                                .orElse("Não listado na matriz"),
                        disciplina.turma().codigo() + " · " + disciplina.periodo(), disciplina.inscricao().situacao(), resultado});
                modeloBoletim.addRow(new Object[]{matricula.curso().toString(), disciplina.periodo(), disciplina.disciplina().toString(),
                        disciplina.turma().codigo(), mediaProvas(disciplina.resultadoFinal()), notaExame(disciplina.resultadoFinal()),
                        mediaFinal(disciplina.resultadoFinal()), resultado});
                linhasBoletim.add(disciplina);
            }
        }
        for (var horario : dados.horarios()) {
            modeloHorarios.addRow(new Object[]{horario.horario().diaSemana(), horario.horario().horaInicio(),
                    horario.horario().horaFim(), horario.curso().toString(), horario.periodo().descricao(),
                    horario.disciplina().toString(), horario.turma().codigo(),
                    horario.sala() == null ? "Sala não alocada" : horario.sala().identificacao() + " · " + horario.sala().localizacao()});
        }
        if (modeloBoletim.getRowCount() > 0) tabelaBoletim.setRowSelectionInterval(0, 0);
        else mostrarLancamentos(-1);
        if (modeloHistorico.getRowCount() == 0 && modeloPlano.getRowCount() == 0) {
            modeloHistorico.addRow(new Object[]{"Sem matrícula acadêmica registrada", "", "", "", "", "", "", ""});
        }
        if (modeloHorarios.getRowCount() == 0) modeloHorarios.addRow(new Object[]{"Sem horários de turmas ativas", "", "", "", "", "", "", ""});
        if (modeloBoletim.getRowCount() == 0) modeloBoletim.addRow(new Object[]{"Sem avaliações lançadas", "", "", "", "", "", "", ""});
    }

    private void mostrarLancamentos(int linha) {
        modeloLancamentos.setRowCount(0);
        if (linha < 0 || linha >= linhasBoletim.size()) {
            modeloLancamentos.addRow(new Object[]{"Selecione uma disciplina para ver os lançamentos", "", "", ""});
            return;
        }
        for (var registro : linhasBoletim.get(linha).lancamentos()) {
            modeloLancamentos.addRow(new Object[]{registro.avaliacao().titulo(), registro.avaliacao().tipo(),
                    registro.avaliacao().dataAplicacao(), registro.nota().map(nota -> nota.valor().toPlainString().replace('.', ','))
                    .orElse("Pendente")});
        }
        if (modeloLancamentos.getRowCount() == 0) modeloLancamentos.addRow(new Object[]{"Nenhuma avaliação cadastrada", "", "", ""});
    }

    private static String textoResultado(DisciplinaBoletimAluno disciplina) {
        if (disciplina.possuiExamesFinaisMultiplos()) return "Sem cálculo: mais de um exame final";
        if (disciplina.resultadoFinal().isEmpty()) return disciplina.lancamentos().isEmpty()
                ? "Sem avaliações lançadas" : "Resultado não disponível";
        ResultadoFinalAcademico resultado = disciplina.resultadoFinal().orElseThrow();
        return switch (resultado.situacao()) {
            case PENDENTE_PROVAS -> "Pendente: notas das provas";
            case PENDENTE_EXAME -> "Pendente: exame final";
            case APROVADO -> "Aprovado após exame";
            case REPROVADO -> resultado.notaExame().isPresent()
                    ? "Reprovado após exame" : "Reprovado pela média das provas";
        };
    }

    private static String mediaProvas(java.util.Optional<ResultadoFinalAcademico> resultado) {
        return resultado.flatMap(ResultadoFinalAcademico::mediaProvas).map(nota -> nota.valor().toPlainString().replace('.', ',')).orElse("Pendente");
    }

    private static String notaExame(java.util.Optional<ResultadoFinalAcademico> resultado) {
        return resultado.flatMap(ResultadoFinalAcademico::notaExame).map(nota -> nota.valor().toPlainString().replace('.', ',')).orElse("—");
    }

    private static String mediaFinal(java.util.Optional<ResultadoFinalAcademico> resultado) {
        return resultado.flatMap(ResultadoFinalAcademico::mediaFinal).map(nota -> nota.valor().toPlainString().replace('.', ',')).orElse("—");
    }

    private static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
    }

    private <T> void executar(java.util.concurrent.Callable<T> trabalho, java.util.function.Consumer<T> sucesso) {
        if (ocupado) return;
        ocupado = true;
        atualizar.setEnabled(false);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return trabalho.call(); }
            @Override protected void done() {
                ocupado = false;
                atualizar.setEnabled(true);
                try { sucesso.accept(get()); }
                catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    dialogos.erro(FluxoBoletimAluno.this, "Consulta interrompida.");
                } catch (ExecutionException erro) { mostrarErro(erro.getCause()); }
            }
        }.execute();
    }

    private void mostrarErro(Throwable erro) {
        if (erro instanceof FalhaBoletimAluno falha) {
            String mensagem = switch (falha.codigo()) {
                case NAO_AUTORIZADO -> "Esta consulta está disponível somente para o aluno autenticado.";
                case ALUNO_NAO_ENCONTRADO -> "Não há vínculo de aluno para esta conta.";
                case CURSO_NAO_ENCONTRADO, MATRIZ_NAO_ENCONTRADA, DISCIPLINA_NAO_ENCONTRADA,
                     TURMA_NAO_ENCONTRADA, PERIODO_NAO_ENCONTRADO, SALA_NAO_ENCONTRADA,
                     DADOS_ACADEMICOS_INCONSISTENTES -> "Os dados acadêmicos associados à sua matrícula estão incompletos. Procure a coordenação.";
                case CONSULTA_INDISPONIVEL -> "Não foi possível consultar os dados acadêmicos agora. Tente novamente.";
            };
            dialogos.erro(this, mensagem);
        } else dialogos.erro(this, "Não foi possível carregar o boletim. Verifique a conexão e tente novamente.");
    }
}
