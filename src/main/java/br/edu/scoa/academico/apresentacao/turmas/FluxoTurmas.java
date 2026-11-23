package br.edu.scoa.academico.apresentacao.turmas;

import br.edu.scoa.academico.aplicacao.turmas.*;
import br.edu.scoa.academico.dominio.turmas.*;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * Interface gráfica de Salas e Turmas.
 * Segue o padrão do Acadêmico com navegação guiada por CardLayout,
 * isolamento total de regras no domínio e execução assíncrona fora da EDT.
 */
public final class FluxoTurmas extends JPanel {
    private final GestaoTurmas gestao;
    private final DialogosAluno dialogos;
    private final CardLayout cards = new CardLayout();
    private final JPanel paginas = new JPanel(cards);
    private final JLabel estado = new JLabel("Pronto.");

    // Campos de Sala
    private final JTextField identificacaoSala = new JTextField(), localizacaoSala = new JTextField(),
            capacidadeSala = new JTextField(), recursosSala = new JTextField(), buscaSala = new JTextField();
    private final DefaultListModel<Sala> resultadosSalasModel = new DefaultListModel<>();
    private final JList<Sala> listaSalas = new JList<>(resultadosSalasModel);

    // Campos de Turma
    private final JTextField periodoTurma = new JTextField(), disciplinaTurma = new JTextField(),
            professorTurma = new JTextField(), codigoTurma = new JTextField(), capacidadeTurma = new JTextField(),
            diaTurma = new JTextField(), inicioTurma = new JTextField(), fimTurma = new JTextField(),
            salaTurma = new JTextField(), buscaPeriodoTurma = new JTextField(), idSalaAlocacao = new JTextField();
    private final DefaultListModel<Turma> resultadosTurmasModel = new DefaultListModel<>();
    private final JList<Turma> listaTurmas = new JList<>(resultadosTurmasModel);

    // Text areas
    private final JTextArea resumoSala = area(), fichaAreaSala = area(), resumoTurma = area(), fichaAreaTurma = area();

    // Estado da seleção
    private Sala salaAtual, salaRevisada;
    private Turma turmaAtual, turmaRevisada;
    private boolean ocupado;

    public FluxoTurmas(GestaoTurmas gestao, DialogosAluno dialogos) {
        this.gestao = gestao;
        this.dialogos = dialogos;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(paginas);
        estado.setName("estadoTurmas");
        add(estado, BorderLayout.SOUTH);

        configurarMenu();
        configurarSalas();
        configurarTurmas();

        mostrar("menu");
    }

    private void configurarMenu() {
        var menu = pagina("menu", "Turmas e salas", "Selecione a operação desejada.");
        botao(menu, "novaSala", "Cadastrar sala", () -> {
            limparSala();
            salaAtual = null;
            mostrar("sala");
        });
        botao(menu, "consultarSalas", "Consultar salas", () -> {
            resultadosSalasModel.clear();
            buscaSala.setText("");
            mostrar("consultaSalas");
        });
        botao(menu, "novaTurma", "Cadastrar turma", () -> {
            limparTurma();
            turmaAtual = null;
            mostrar("turma");
        });
        botao(menu, "consultarTurmas", "Consultar turmas", () -> {
            resultadosTurmasModel.clear();
            buscaPeriodoTurma.setText("");
            mostrar("consultaTurmas");
        });
    }

    private void configurarSalas() {
        // 1. Cadastrar/Editar Sala
        var telaSala = pagina("sala", "Dados da sala", "Informe os atributos da sala. A capacidade deve ser maior que zero.");
        campo(telaSala, "Identificação da sala (única)", identificacaoSala, "identificacaoSala");
        campo(telaSala, "Localização (bloco, andar)", localizacaoSala, "localizacaoSala");
        campo(telaSala, "Capacidade máxima de alunos", capacidadeSala, "capacidadeSala");
        campo(telaSala, "Recursos disponíveis (opcional)", recursosSala, "recursosSala");
        botao(telaSala, "revisarSala", "Revisar dados da sala", () -> {
            try {
                salaRevisada = montarSala();
                resumoSala.setText("IDENTIFICAÇÃO: " + salaRevisada.identificacao() + "\n"
                        + "LOCALIZAÇÃO: " + salaRevisada.localizacao() + "\n"
                        + "CAPACIDADE: " + salaRevisada.capacidade() + "\n"
                        + "RECURSOS: " + (salaRevisada.recursos().isBlank() ? "(nenhum)" : salaRevisada.recursos()) + "\n"
                        + "SITUAÇÃO: " + salaRevisada.situacao());
                mostrar("revisaoSala");
            } catch (RuntimeException e) {
                erro(e);
            }
        });
        botao(telaSala, "cancelarSala", "Cancelar", () -> mostrar(salaAtual == null ? "menu" : "fichaSala"));

        // 2. Revisão da Sala
        var revSala = pagina("revisaoSala", "Confirmar dados da sala", "Confira antes de gravar no sistema.");
        revSala.add(new JScrollPane(resumoSala));
        botao(revSala, "salvarSala", "Confirmar e salvar", () -> {
            var s = salaRevisada;
            executar(() -> gestao.salvarSala(s, UUID.randomUUID()), salva -> {
                dialogos.informar(this, "Sala salva com sucesso: " + salva.identificacao());
                abrirFichaSala(salva);
            });
        });
        botao(revSala, "corrigirSala", "Voltar e corrigir", () -> mostrar("sala"));
        botao(revSala, "cancelarRevisaoSala", "Cancelar sem salvar", () -> mostrar("menu"));

        // 3. Consulta de Salas
        var conSala = pagina("consultaSalas", "Consultar salas", "Pesquise salas por identificação ou localização.");
        campo(conSala, "Termo de busca", buscaSala, "buscaSala");
        listaSalas.setName("resultadosSalas");
        listaSalas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conSala.add(new JScrollPane(listaSalas));
        botao(conSala, "pesquisarSalas", "Pesquisar", () -> {
            String termo = buscaSala.getText();
            executar(() -> gestao.pesquisarSalas(termo), itens -> {
                resultadosSalasModel.clear();
                itens.forEach(resultadosSalasModel::addElement);
            });
        });
        botao(conSala, "verFichaSala", "Abrir ficha da sala", () -> {
            var sel = listaSalas.getSelectedValue();
            if (sel != null) abrirFichaSala(sel);
            else dialogos.erro(this, "Selecione uma sala na lista.");
        });
        botao(conSala, "voltarMenuSalas", "Voltar ao menu", () -> mostrar("menu"));

        // 4. Ficha da Sala
        var fichaSala = pagina("fichaSala", "Ficha da sala", "Detalhes e manutenção.");
        fichaSala.add(new JScrollPane(fichaAreaSala));
        botao(fichaSala, "editarSala", "Editar dados da sala", () -> {
            if (salaAtual != null) {
                identificacaoSala.setText(salaAtual.identificacao());
                localizacaoSala.setText(salaAtual.localizacao());
                capacidadeSala.setText(String.valueOf(salaAtual.capacidade()));
                recursosSala.setText(salaAtual.recursos());
                mostrar("sala");
            }
        });
        botao(fichaSala, "alternarSituacaoSala", "Alternar situação (Disponível/Indisponível)", () -> {
            if (salaAtual != null) {
                var novaSituacao = salaAtual.situacao() == SituacaoSala.DISPONIVEL ? SituacaoSala.INDISPONIVEL : SituacaoSala.DISPONIVEL;
                var novaSala = new Sala(salaAtual.id(), salaAtual.identificacao(), salaAtual.localizacao(),
                        salaAtual.capacidade(), salaAtual.recursos(), novaSituacao, salaAtual.revisao());
                executar(() -> gestao.salvarSala(novaSala, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Situação atualizada para: " + salva.situacao());
                    abrirFichaSala(salva);
                });
            }
        });
        botao(fichaSala, "voltarConsultaSalas", "Voltar à pesquisa", () -> mostrar("consultaSalas"));
    }

    private void configurarTurmas() {
        // 1. Cadastrar Turma
        var telaTurma = pagina("turma", "Cadastrar turma", "Informe período, disciplina, professor e horários.");
        campo(telaTurma, "ID do período letivo", periodoTurma, "periodoTurma");
        campo(telaTurma, "ID da disciplina ativa", disciplinaTurma, "disciplinaTurma");
        campo(telaTurma, "ID do professor", professorTurma, "professorTurma");
        campo(telaTurma, "Código da turma (ex: T01)", codigoTurma, "codigoTurma");
        campo(telaTurma, "Capacidade da turma", capacidadeTurma, "capacidadeTurma");
        campo(telaTurma, "Dia da semana (SEGUNDA a DOMINGO)", diaTurma, "diaTurma");
        campo(telaTurma, "Hora de início (HH:mm)", inicioTurma, "inicioTurma");
        campo(telaTurma, "Hora de término (HH:mm)", fimTurma, "fimTurma");
        campo(telaTurma, "ID da sala alocada (opcional)", salaTurma, "salaTurma");
        botao(telaTurma, "revisarTurma", "Revisar turma", () -> {
            try {
                turmaRevisada = montarTurma();
                var h = turmaRevisada.horarios().getFirst();
                resumoTurma.setText("TURMA: " + turmaRevisada.codigo() + "\n"
                        + "PERÍODO ID: " + turmaRevisada.periodoId() + "\n"
                        + "DISCIPLINA ID: " + turmaRevisada.disciplinaId() + "\n"
                        + "PROFESSOR ID: " + turmaRevisada.professorId() + "\n"
                        + "CAPACIDADE: " + turmaRevisada.capacidade() + "\n"
                        + "HORÁRIO: " + h.diaSemana() + " das " + h.horaInicio() + " às " + h.horaFim()
                        + (h.salaId() != null ? " (Sala ID: " + h.salaId() + ")" : " (Sem sala)"));
                mostrar("revisaoTurma");
            } catch (RuntimeException e) {
                erro(e);
            }
        });
        botao(telaTurma, "cancelarTurma", "Cancelar", () -> mostrar("menu"));

        // 2. Revisão da Turma
        var revTurma = pagina("revisaoTurma", "Confirmar dados da turma", "A turma será gravada como PLANEJADA.");
        revTurma.add(new JScrollPane(resumoTurma));
        botao(revTurma, "salvarTurma", "Confirmar e salvar", () -> {
            var t = turmaRevisada;
            executar(() -> gestao.salvarTurma(t, UUID.randomUUID()), salva -> {
                dialogos.informar(this, "Turma salva com sucesso: " + salva.codigo());
                abrirFichaTurma(salva);
            });
        });
        botao(revTurma, "corrigirTurma", "Voltar e corrigir", () -> mostrar("turma"));
        botao(revTurma, "cancelarRevisaoTurma", "Cancelar sem salvar", () -> mostrar("menu"));

        // 3. Consulta de Turmas
        var conTurma = pagina("consultaTurmas", "Consultar turmas", "Informe o ID do período letivo para listar as ofertas.");
        campo(conTurma, "ID do período", buscaPeriodoTurma, "buscaPeriodoTurma");
        listaTurmas.setName("resultadosTurmas");
        listaTurmas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conTurma.add(new JScrollPane(listaTurmas));
        botao(conTurma, "pesquisarTurmas", "Buscar turmas", () -> {
            try {
                long pId = Long.parseLong(buscaPeriodoTurma.getText().strip());
                executar(() -> gestao.pesquisarTurmas(pId), itens -> {
                    resultadosTurmasModel.clear();
                    itens.forEach(resultadosTurmasModel::addElement);
                });
            } catch (NumberFormatException e) {
                dialogos.erro(this, "Informe um ID numérico de período.");
            }
        });
        botao(conTurma, "verFichaTurma", "Abrir ficha da turma", () -> {
            var sel = listaTurmas.getSelectedValue();
            if (sel != null) abrirFichaTurma(sel);
            else dialogos.erro(this, "Selecione uma turma na lista.");
        });
        botao(conTurma, "voltarMenuTurmas", "Voltar ao menu", () -> mostrar("menu"));

        // 4. Ficha da Turma
        var fichaTurma = pagina("fichaTurma", "Ficha da turma", "Visualização, alocação de salas e transições de situação.");
        fichaTurma.add(new JScrollPane(fichaAreaTurma));

        // Transições de situação
        botao(fichaTurma, "abrirTurma", "Abrir turma", () -> {
            if (turmaAtual != null) {
                executar(() -> gestao.transicionar(turmaAtual, SituacaoTurma.ABERTA, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Turma aberta com sucesso.");
                    abrirFichaTurma(salva);
                });
            }
        });
        botao(fichaTurma, "iniciarAulasTurma", "Iniciar aulas (Em andamento)", () -> {
            if (turmaAtual != null) {
                executar(() -> gestao.transicionar(turmaAtual, SituacaoTurma.EM_ANDAMENTO, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Turma colocada em andamento.");
                    abrirFichaTurma(salva);
                });
            }
        });
        botao(fichaTurma, "encerrarTurma", "Encerrar turma", () -> {
            if (turmaAtual != null) {
                executar(() -> gestao.transicionar(turmaAtual, SituacaoTurma.ENCERRADA, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Turma encerrada com sucesso.");
                    abrirFichaTurma(salva);
                });
            }
        });
        botao(fichaTurma, "cancelarTurmaSituacao", "Cancelar turma", () -> {
            if (turmaAtual != null) {
                executar(() -> gestao.transicionar(turmaAtual, SituacaoTurma.CANCELADA, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Turma cancelada com sucesso.");
                    abrirFichaTurma(salva);
                });
            }
        });

        // Alocação de sala
        campo(fichaTurma, "ID da sala para alocar ao 1º horário", idSalaAlocacao, "idSalaAlocacao");
        botao(fichaTurma, "alocarSalaTurma", "Alocar sala ao horário", () -> {
            if (turmaAtual != null && !turmaAtual.horarios().isEmpty()) {
                try {
                    long salaId = Long.parseLong(idSalaAlocacao.getText().strip());
                    var horario = turmaAtual.horarios().getFirst();
                    executar(() -> gestao.alocarSala(turmaAtual, horario, salaId, UUID.randomUUID()), salva -> {
                        dialogos.informar(this, "Sala " + salaId + " alocada com sucesso.");
                        abrirFichaTurma(salva);
                    });
                } catch (NumberFormatException e) {
                    dialogos.erro(this, "Informe um ID numérico de sala válido.");
                }
            }
        });
        botao(fichaTurma, "desalocarSalaTurma", "Desalocar sala do horário", () -> {
            if (turmaAtual != null && !turmaAtual.horarios().isEmpty()) {
                var horario = turmaAtual.horarios().getFirst();
                executar(() -> gestao.alocarSala(turmaAtual, horario, null, UUID.randomUUID()), salva -> {
                    dialogos.informar(this, "Sala desalocada com sucesso.");
                    abrirFichaTurma(salva);
                });
            }
        });
        botao(fichaTurma, "voltarConsultaTurmas", "Voltar à pesquisa", () -> mostrar("consultaTurmas"));
    }

    private void abrirFichaSala(Sala sala) {
        this.salaAtual = sala;
        fichaAreaSala.setText("FICHA DA SALA\n"
                + "ID: " + sala.id() + "\n"
                + "IDENTIFICAÇÃO: " + sala.identificacao() + "\n"
                + "LOCALIZAÇÃO: " + sala.localizacao() + "\n"
                + "CAPACIDADE: " + sala.capacidade() + "\n"
                + "RECURSOS: " + (sala.recursos().isBlank() ? "(nenhum)" : sala.recursos()) + "\n"
                + "SITUAÇÃO: " + sala.situacao() + "\n"
                + "REVISÃO: " + sala.revisao());
        mostrar("fichaSala");
    }

    private void abrirFichaTurma(Turma turma) {
        this.turmaAtual = turma;
        var sb = new StringBuilder();
        sb.append("FICHA DA TURMA\n")
                .append("ID: ").append(turma.id()).append("\n")
                .append("CÓDIGO: ").append(turma.codigo()).append("\n")
                .append("SITUAÇÃO: ").append(turma.situacao()).append("\n")
                .append("PERÍODO ID: ").append(turma.periodoId()).append("\n")
                .append("DISCIPLINA ID: ").append(turma.disciplinaId()).append("\n")
                .append("PROFESSOR ID: ").append(turma.professorId()).append("\n")
                .append("CAPACIDADE: ").append(turma.capacidade()).append("\n")
                .append("REVISÃO: ").append(turma.revisao()).append("\n")
                .append("HORÁRIOS:\n");
        for (var h : turma.horarios()) {
            sb.append(" - ").append(h.diaSemana()).append(" ").append(h.horaInicio()).append(" às ").append(h.horaFim())
                    .append(h.salaId() != null ? " [Sala ID: " + h.salaId() + "]" : " [Sem sala]")
                    .append(" (Rev: ").append(h.revisao()).append(")\n");
        }
        fichaAreaTurma.setText(sb.toString());
        mostrar("fichaTurma");
    }

    private Sala montarSala() {
        long id = salaAtual != null ? salaAtual.id() : 0;
        long rev = salaAtual != null ? salaAtual.revisao() : 0;
        SituacaoSala sit = salaAtual != null ? salaAtual.situacao() : SituacaoSala.DISPONIVEL;
        return new Sala(id, identificacaoSala.getText(), localizacaoSala.getText(),
                numero(capacidadeSala), recursosSala.getText(), sit, rev);
    }

    private Turma montarTurma() {
        Long salaId = salaTurma.getText().strip().isEmpty() ? null : Long.valueOf(salaTurma.getText().strip());
        var h = new HorarioTurma(0, DiaSemana.valueOf(diaTurma.getText().strip().toUpperCase(Locale.ROOT)),
                LocalTime.parse(inicioTurma.getText().strip()), LocalTime.parse(fimTurma.getText().strip()), salaId, 0);
        long id = turmaAtual != null ? turmaAtual.id() : 0;
        long rev = turmaAtual != null ? turmaAtual.revisao() : 0;
        SituacaoTurma sit = turmaAtual != null ? turmaAtual.situacao() : SituacaoTurma.PLANEJADA;
        return new Turma(id, numeroLongo(periodoTurma), numeroLongo(disciplinaTurma), numeroLongo(professorTurma),
                codigoTurma.getText(), numero(capacidadeTurma), sit, List.of(h), rev);
    }

    private int numero(JTextField f) { return Integer.parseInt(f.getText().strip()); }
    private long numeroLongo(JTextField f) { return Long.parseLong(f.getText().strip()); }

    private void limparSala() {
        for (var f : new JTextField[]{identificacaoSala, localizacaoSala, capacidadeSala, recursosSala}) f.setText("");
    }

    private void limparTurma() {
        for (var f : new JTextField[]{periodoTurma, disciplinaTurma, professorTurma, codigoTurma, capacidadeTurma, diaTurma, inicioTurma, fimTurma, salaTurma}) f.setText("");
    }

    private JPanel pagina(String id, String titulo, String ajuda) {
        var p = new JPanel();
        p.setName(id);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        var lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        p.add(lblTitulo);
        p.add(new JLabel(ajuda));
        p.add(Box.createVerticalStrut(8));
        paginas.add(p, id);
        return p;
    }

    private void campo(JPanel p, String titulo, JTextField f, String nome) {
        p.add(new JLabel(titulo));
        f.setName(nome);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.add(f);
        p.add(Box.createVerticalStrut(4));
    }

    private void botao(JPanel p, String id, String titulo, Runnable r) {
        var b = new JButton(titulo);
        b.setName(id);
        b.addActionListener(e -> {
            if (!ocupado) r.run();
        });
        p.add(b);
        p.add(Box.createVerticalStrut(4));
    }

    private static JTextArea area() {
        var a = new JTextArea(8, 45);
        a.setEditable(false);
        return a;
    }

    private void mostrar(String id) { cards.show(paginas, id); }

    private <T> void executar(Callable<T> tarefa, Consumer<T> aoConcluir) {
        ocupado = true;
        estado.setText("Processando...");
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return tarefa.call();
            }
            protected void done() {
                ocupado = false;
                estado.setText("Pronto.");
                try {
                    aoConcluir.accept(get());
                } catch (Exception e) {
                    erro(e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e);
                }
            }
        }.execute();
    }

    private void erro(Throwable e) {
        String msg = e instanceof FalhaTurma f
                ? "Não foi possível concluir: " + f.codigo() + ". Revise os dados, horários e disponibilidade."
                : (e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Confira os dados informados.");
        dialogos.erro(this, msg);
    }
}
