package br.edu.scoa.academico.apresentacao.curriculo;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import br.edu.scoa.academico.dominio.curriculo.FalhaCurriculo;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

/** Menu, formulário, revisão, consulta e ficha separados; grava somente na confirmação. */
public final class FluxoCatalogo extends JPanel {
    private final GestaoCatalogo gestao;
    private final TipoCatalogo tipo;
    private final DialogosAluno dialogos;
    private final CardLayout navegacao = new CardLayout();
    private final JPanel paginas = new JPanel(navegacao);
    private final JTextField codigo = new JTextField(30), nome = new JTextField(30), carga = new JTextField(10), termo = new JTextField(30);
    private final JTextArea ementa = area(), revisao = area(), ficha = area(), textoHistorico = area();
    private final DefaultListModel<ItemCatalogo> resultados = new DefaultListModel<>();
    private final JList<ItemCatalogo> lista = new JList<>(resultados);
    private final JLabel estado = new JLabel("Pronto.");
    private ItemCatalogo selecionado, revisado;
    private boolean ocupado;
    private final JButton inativar;

    public FluxoCatalogo(GestaoCatalogo gestao, TipoCatalogo tipo, DialogosAluno dialogos) {
        this.gestao = gestao; this.tipo = tipo; this.dialogos = dialogos;
        setLayout(new BorderLayout(12, 12));
        add(paginas, BorderLayout.CENTER); add(estado, BorderLayout.SOUTH);
        estado.setName("estadoCatalogo");
        String entidade = tipo == TipoCatalogo.CURSO ? "Curso" : "Disciplina";
        var menu = pagina("menu", entidade + "s", "Escolha a funcionalidade. Inativar preserva os dados e o histórico.");
        botao(menu, "cadastrar", "Cadastrar " + entidade.toLowerCase(), () -> {
            selecionado = null; revisado = null; codigo.setText(""); nome.setText(""); carga.setText(""); ementa.setText(""); mostrar("dados");
        });
        botao(menu, "consultar", "Consultar, editar ou inativar", () -> { resultados.clear(); termo.setText(""); mostrar("consulta"); });

        var dados = pagina("dados", "Dados de " + entidade.toLowerCase(), "Campos obrigatórios. Código será salvo em maiúsculas, sem espaços nas extremidades.");
        campo(dados, "Código (até 40 caracteres)", codigo, "codigo");
        campo(dados, "Nome (até 200 caracteres)", nome, "nome");
        campo(dados, tipo == TipoCatalogo.CURSO ? "Carga horária total (horas inteiras, maior que zero)" : "Carga horária (horas inteiras, maior que zero)", carga, "carga");
        if (tipo == TipoCatalogo.DISCIPLINA) {
            ementa.setName("ementa"); dados.add(new JLabel("Ementa — conteúdo da disciplina")); dados.add(new JScrollPane(ementa));
        }
        botao(dados, "revisar", "Revisar dados", () -> {
            try {
                int horas;
                try { horas = Integer.parseInt(carga.getText().strip()); }
                catch (NumberFormatException erro) { throw new FalhaCurriculo(FalhaCurriculo.Codigo.CARGA_HORARIA_INVALIDA); }
                revisado = GestaoCatalogo.validar(tipo, selecionado, codigo.getText(), nome.getText(), ementa.getText(), horas);
                revisao.setText(descrever(revisado) + "\n\nConfira antes de confirmar. Cada alteração salva gera uma revisão no histórico.");
                mostrar("revisao");
            } catch (RuntimeException erro) { erro(erro); }
        });
        botao(dados, "cancelarDados", "Cancelar sem salvar", () -> mostrar("menu"));

        var conferir = pagina("revisao", "Revisar " + entidade.toLowerCase(), "Os dados ainda não foram gravados.");
        revisao.setEditable(false); conferir.add(new JScrollPane(revisao));
        botao(conferir, "confirmar", "Confirmar e salvar", () -> {
            var entrada = revisado; var anterior = selecionado;
            executar(() -> gestao.salvar(tipo, anterior, entrada.codigo(), entrada.nome(), entrada.ementa(), entrada.cargaHoraria()),
                salvo -> { mostrarFicha(salvo); dialogos.informar(this, "Cadastro salvo. Revisão " + salvo.versao() + " registrada."); });
        });
        botao(conferir, "corrigir", "Voltar e corrigir", () -> mostrar("dados"));
        botao(conferir, "cancelarRevisao", "Cancelar sem salvar", () -> mostrar("menu"));

        var consulta = pagina("consulta", "Consultar " + entidade.toLowerCase(), "Pesquise por código ou parte do nome. Deixe vazio para listar todos, inclusive inativos.");
        campo(consulta, "Código ou nome", termo, "termo"); lista.setName("resultados"); lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        botao(consulta, "pesquisar", "Pesquisar", () -> {
            String busca = termo.getText();
            executar(() -> gestao.pesquisar(tipo, busca), itens -> {
                resultados.clear(); itens.forEach(resultados::addElement);
                if (itens.isEmpty()) dialogos.informar(this, "Nenhum cadastro encontrado.");
            });
        });
        consulta.add(new JScrollPane(lista));
        botao(consulta, "abrirFicha", "Abrir cadastro selecionado", () -> {
            if (lista.getSelectedValue() == null) { dialogos.informar(this, "Selecione um cadastro da lista."); return; }
            mostrarFicha(lista.getSelectedValue());
        });
        botao(consulta, "voltarConsulta", "Voltar ao menu", () -> mostrar("menu"));

        var detalhes = pagina("ficha", "Ficha de " + entidade.toLowerCase(), "Edição e inativação preservam as revisões anteriores.");
        ficha.setEditable(false); ficha.setName("fichaCatalogo"); detalhes.add(new JScrollPane(ficha));
        botao(detalhes, "editar", "Editar dados", () -> {
            codigo.setText(selecionado.codigo()); nome.setText(selecionado.nome());
            carga.setText(Integer.toString(selecionado.cargaHoraria())); ementa.setText(selecionado.ementa()); mostrar("dados");
        });
        inativar = botao(detalhes, "inativar", "Inativar cadastro", () -> {
            if (!dialogos.confirmar(this, "Inativar " + selecionado.codigo() + "? Os dados e o histórico serão preservados.")) return;
            var anterior = selecionado; executar(() -> gestao.inativar(anterior), salvo -> {
                mostrarFicha(salvo); dialogos.informar(this, "Cadastro inativado.");
            });
        });
        botao(detalhes, "historico", "Consultar histórico", () -> {
            var item = selecionado;
            executar(() -> gestao.historico(item), historico -> {
                var texto = new StringBuilder();
                var formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/Sao_Paulo"));
                for (var registro : historico) texto.append("REVISÃO ").append(registro.item().versao())
                    .append(" · ").append(formato.format(registro.instante())).append(" (São Paulo)")
                    .append(" · autor ID ").append(registro.autorId()).append("\n")
                    .append(descrever(registro.item())).append("\n\n");
                textoHistorico.setText(texto.toString()); textoHistorico.setCaretPosition(0); mostrar("historico");
            });
        });
        botao(detalhes, "voltarFicha", "Voltar à consulta", () -> { resultados.clear(); mostrar("consulta"); });
        var historico = pagina("historico", "Histórico de alterações", "Cada revisão guarda os dados daquele momento. Não é a versão de uma matriz curricular.");
        textoHistorico.setEditable(false); textoHistorico.setName("textoHistorico"); historico.add(new JScrollPane(textoHistorico));
        botao(historico, "voltarHistorico", "Voltar à ficha", () -> mostrar("ficha"));
        mostrar("menu");
    }
    private static JTextArea area() {
        var campo = new JTextArea(6, 45); campo.setLineWrap(true); campo.setWrapStyleWord(true); return campo;
    }
    private void mostrarFicha(ItemCatalogo item) {
        selecionado = item; ficha.setText(descrever(item)); ficha.setCaretPosition(0); inativar.setEnabled(item.estaAtivo()); mostrar("ficha");
    }
    private String descrever(ItemCatalogo item) {
        return "Código: " + item.codigo() + "\nNome: " + item.nome() + "\nCarga horária: " + item.cargaHoraria()
            + " horas\nSituação: " + (item.estaAtivo() ? "Ativo(a)" : "Inativo(a)") + "\nRevisão: " + item.versao()
            + (tipo == TipoCatalogo.DISCIPLINA ? "\n\nEmenta:\n" + item.ementa() : "");
    }
    private JPanel pagina(String id, String titulo, String ajuda) {
        var painel = new JPanel(); painel.setName(id); painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(new JLabel(titulo)); painel.add(Box.createVerticalStrut(10));
        var orientacao = new JTextArea(ajuda); orientacao.setEditable(false); orientacao.setLineWrap(true); orientacao.setWrapStyleWord(true);
        orientacao.setFont(UIManager.getFont("Label.font")); orientacao.setBackground(UIManager.getColor("Panel.background"));
        orientacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); painel.add(orientacao);
        paginas.add(painel, id); return painel;
    }
    private void campo(JPanel painel, String texto, JTextField campo, String id) {
        var rotulo = new JLabel(texto); rotulo.setLabelFor(campo); painel.add(rotulo); campo.setName(id);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); painel.add(campo); painel.add(Box.createVerticalStrut(8));
    }
    private JButton botao(JPanel painel, String id, String texto, Runnable acao) {
        var botao = new JButton(texto); botao.setName(id); botao.addActionListener(evento -> { if (!ocupado) acao.run(); });
        painel.add(botao); painel.add(Box.createVerticalStrut(8)); return botao;
    }
    private void mostrar(String pagina) { navegacao.show(paginas, pagina); revalidate(); repaint(); }
    private void habilitar(Container raiz, boolean habilitado) {
        for (var componente : raiz.getComponents()) { componente.setEnabled(habilitado); if (componente instanceof Container grupo) habilitar(grupo, habilitado); }
    }
    private <T> void executar(Callable<T> trabalho, Consumer<T> sucesso) {
        ocupado = true; habilitar(paginas, false); estado.setText("Processando…");
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return trabalho.call(); }
            protected void done() {
                ocupado = false; habilitar(paginas, true); estado.setText("Pronto.");
                try { sucesso.accept(get()); }
                catch (InterruptedException falha) { Thread.currentThread().interrupt(); erro(falha); }
                catch (ExecutionException falha) { erro(falha.getCause()); }
                inativar.setEnabled(selecionado != null && selecionado.estaAtivo());
            }
        }.execute();
    }
    private void erro(Throwable falha) {
        String mensagem = falha instanceof FalhaCurriculo erro ? switch (erro.codigo()) {
            case CODIGO_INVALIDO -> "Informe um código com 1 a 40 caracteres.";
            case NOME_INVALIDO -> "Informe um nome com 1 a 200 caracteres.";
            case EMENTA_INVALIDA -> "Informe a ementa. O conteúdo deve ocupar até 65.535 bytes em UTF-8.";
            case CARGA_HORARIA_INVALIDA -> "Informe a carga horária como número inteiro maior que zero.";
            case CODIGO_DUPLICADO -> "Este código já está cadastrado. Consulte o registro existente ou use outro código.";
            case NAO_ENCONTRADO -> "Cadastro não encontrado.";
            case CONFLITO_EDICAO -> "O cadastro mudou desde a consulta. Volte à busca e consulte novamente antes de editar.";
            case INDISPONIVEL -> "Não foi possível acessar o catálogo. Confira a conexão e tente novamente.";
        } : ControladorAlunos.mensagemErro(falha);
        dialogos.erro(this, mensagem);
    }
}

