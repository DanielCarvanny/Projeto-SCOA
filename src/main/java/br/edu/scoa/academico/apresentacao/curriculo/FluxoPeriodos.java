package br.edu.scoa.academico.apresentacao.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.GestaoPeriodos;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

public final class FluxoPeriodos extends JPanel {
    private static final ZoneId FUSO=ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA=DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORARIO=new DateTimeFormatterBuilder().appendPattern("dd/MM/uuuu HH:mm")
        .optionalStart().appendPattern(":ss").optionalStart().appendFraction(ChronoField.NANO_OF_SECOND,0,6,true).optionalEnd().optionalEnd().toFormatter().withResolverStyle(ResolverStyle.STRICT);
    private final GestaoPeriodos gestao;private final DialogosAluno dialogos;
    private final CardLayout navegacao=new CardLayout();private final JPanel paginas=new JPanel(navegacao);
    private final JLabel estado=new JLabel("Pronto.");
    private final JTextField descricao=new JTextField(),inicio=new JTextField(),fim=new JTextField(),inicioMatricula=new JTextField(),fimMatricula=new JTextField(),inicioInscricao=new JTextField(),fimInscricao=new JTextField(),busca=new JTextField();
    private final JComboBox<SituacaoPeriodoLetivo> filtro=new JComboBox<>();
    private final DefaultListModel<PeriodoLetivo> resultados=new DefaultListModel<>();private final JList<PeriodoLetivo> lista=new JList<>(resultados);
    private final JTextArea resumo=area(),ficha=area(),resumoTransicao=area();
    private JButton editar,abrir,encerrar;private PeriodoLetivo anterior,revisado;private UUID operacao;
    private boolean ocupado,abrindo;
    public FluxoPeriodos(GestaoPeriodos gestao,DialogosAluno dialogos){
        this.gestao=gestao;this.dialogos=dialogos;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(paginas);estado.setName("estadoPeriodo");add(estado,BorderLayout.SOUTH);
        var menu=pagina("menu","Períodos letivos","Escolha cadastrar ou consultar. Abertura e encerramento são feitos pela ficha.");
        botao(menu,"novoPeriodo","Cadastrar período",()->{anterior=null;operacao=UUID.randomUUID();for(var campo:new JTextField[]{descricao,inicio,fim,inicioMatricula,fimMatricula,inicioInscricao,fimInscricao})campo.setText("");mostrar("calendario");});
        botao(menu,"consultarPeriodos","Consultar períodos",()->{resultados.clear();busca.setText("");filtro.setSelectedIndex(0);mostrar("consulta");});
        var calendario=pagina("calendario","1. Identificação e calendário","Datas das atividades. O período novo será salvo como Planejado.");
        campo(calendario,"Descrição",descricao,"descricaoPeriodo");campo(calendario,"Início das atividades — DD/MM/AAAA",inicio,"inicioAtividades");campo(calendario,"Fim das atividades — DD/MM/AAAA",fim,"fimAtividades");
        botao(calendario,"continuarMatricula","Continuar para matrícula",()->{try{PeriodoLetivo.validarCalendario(descricao.getText(),data(inicio.getText()),data(fim.getText()));mostrar("matricula");}catch(RuntimeException erro){erro(erro);}});botao(calendario,"cancelarCalendario","Cancelar sem salvar",this::cancelar);
        var matricula=pagina("matricula","2. Janela de matrícula","Horário de America/Sao_Paulo. Pode anteceder o início das aulas.");
        campo(matricula,"Início — DD/MM/AAAA HH:mm (segundos opcionais)",inicioMatricula,"inicioMatricula");campo(matricula,"Fim — DD/MM/AAAA HH:mm (segundos opcionais)",fimMatricula,"fimMatricula");
        botao(matricula,"continuarInscricao","Continuar para inscrição",()->{try{PeriodoLetivo.validarMatricula(horario(inicioMatricula.getText(),FalhaPeriodo.Codigo.MATRICULA_INVALIDA),horario(fimMatricula.getText(),FalhaPeriodo.Codigo.MATRICULA_INVALIDA));mostrar("inscricao");}catch(RuntimeException erro){erro(erro);}});
        botao(matricula,"voltarCalendario","Voltar ao calendário",()->mostrar("calendario"));botao(matricula,"cancelarMatricula","Cancelar sem salvar",this::cancelar);
        var inscricao=pagina("inscricao","3. Janela de inscrição em turmas","Horário de America/Sao_Paulo. Esta janela é independente da matrícula.");
        campo(inscricao,"Início — DD/MM/AAAA HH:mm (segundos opcionais)",inicioInscricao,"inicioInscricao");campo(inscricao,"Fim — DD/MM/AAAA HH:mm (segundos opcionais)",fimInscricao,"fimInscricao");
        botao(inscricao,"revisarPeriodo","Revisar antes de salvar",()->{try{revisado=montar();resumo.setText(descrever(revisado));mostrar("revisao");}catch(RuntimeException erro){erro(erro);}});
        botao(inscricao,"voltarMatricula","Voltar à matrícula",()->mostrar("matricula"));botao(inscricao,"cancelarInscricao","Cancelar sem salvar",this::cancelar);
        var revisao=pagina("revisao","4. Confirmar período","Confira os três intervalos. Nada foi gravado nesta edição.");revisao.add(new JScrollPane(resumo));
        botao(revisao,"salvarPeriodo","Confirmar e salvar período",()->{var entrada=revisado;var chave=operacao;executar(()->gestao.salvar(entrada,chave),this::apresentar);});
        botao(revisao,"corrigirPeriodo","Voltar e corrigir",()->mostrar("calendario"));botao(revisao,"cancelarRevisaoPeriodo","Cancelar sem salvar",this::cancelar);
        var consulta=pagina("consulta","Consultar períodos","Pesquise pela descrição e, se desejar, pela situação.");campo(consulta,"Descrição contém",busca,"buscaPeriodo");
        filtro.addItem(null);for(var situacao:SituacaoPeriodoLetivo.values())filtro.addItem(situacao);
        filtro.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> l,Object valor,int i,boolean s,boolean f){return super.getListCellRendererComponent(l,valor==null?"Todas as situações":valor,i,s,f);}});
        campo(consulta,"Situação",filtro,"situacaoPeriodo");botao(consulta,"pesquisarPeriodos","Pesquisar",()->{
            String termo=busca.getText();var situacao=(SituacaoPeriodoLetivo)filtro.getSelectedItem();executar(()->gestao.pesquisar(termo,situacao),itens->{resultados.clear();itens.forEach(resultados::addElement);if(itens.isEmpty())dialogos.informar(this,"Nenhum período encontrado.");});
        });
        lista.setName("resultadosPeriodo");lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);consulta.add(new JScrollPane(lista));
        botao(consulta,"abrirFichaPeriodo","Abrir ficha selecionada",()->{var p=lista.getSelectedValue();if(p==null){dialogos.informar(this,"Selecione um período.");return;}executar(()->gestao.consultar(p.id()),this::apresentar);});
        botao(consulta,"voltarMenuPeriodos","Voltar ao menu",()->mostrar("menu"));
        var detalhes=pagina("ficha","Ficha do período","Abrir habilita as janelas conforme as datas. Encerrar bloqueia novas operações e edições.");ficha.setName("fichaPeriodo");detalhes.add(new JScrollPane(ficha));
        editar=botao(detalhes,"editarPeriodo","Editar período",()->{preencher();operacao=UUID.randomUUID();mostrar("calendario");});
        abrir=botao(detalhes,"abrirPeriodo","Abrir período",()->prepararTransicao(true));encerrar=botao(detalhes,"encerrarPeriodo","Encerrar período",()->prepararTransicao(false));
        botao(detalhes,"reconsultarPeriodo","Atualizar ficha",()->executar(()->gestao.consultar(anterior.id()),this::apresentar));
        botao(detalhes,"voltarConsultaPeriodos","Voltar à consulta",()->{resultados.clear();mostrar("consulta");});
        var transicao=pagina("transicao","Confirmar mudança de situação","Esta ação será registrada no histórico do período.");transicao.add(new JScrollPane(resumoTransicao));
        botao(transicao,"confirmarSituacaoPeriodo","Confirmar mudança",()->{var p=anterior;boolean abrirAgora=abrindo;executar(()->abrirAgora?gestao.abrir(p):gestao.encerrar(p),this::apresentar);});
        botao(transicao,"cancelarSituacaoPeriodo","Cancelar",()->mostrar("ficha"));mostrar("menu");
    }
    private PeriodoLetivo montar(){
        var im=horario(inicioMatricula.getText(),FalhaPeriodo.Codigo.MATRICULA_INVALIDA);
        var fm=horario(fimMatricula.getText(),FalhaPeriodo.Codigo.MATRICULA_INVALIDA);
        var ii=horario(inicioInscricao.getText(),FalhaPeriodo.Codigo.INSCRICAO_INVALIDA);
        var fi=horario(fimInscricao.getText(),FalhaPeriodo.Codigo.INSCRICAO_INVALIDA);
        return new PeriodoLetivo(anterior==null?0:anterior.id(),descricao.getText(),data(inicio.getText()),data(fim.getText()),im,fm,ii,fi,
            anterior==null?SituacaoPeriodoLetivo.PLANEJADO:anterior.situacao(),anterior==null?0:anterior.revisao());
    }
    private static LocalDate data(String texto){try{return LocalDate.parse(texto.strip(),DATA);}catch(DateTimeException erro){throw new FalhaPeriodo(FalhaPeriodo.Codigo.CALENDARIO_INVALIDO);}}
    static Instant horario(String texto,FalhaPeriodo.Codigo codigo){
        try{var local=LocalDateTime.parse(texto.strip(),HORARIO);var offsets=FUSO.getRules().getValidOffsets(local);
            if(offsets.size()!=1)throw new FalhaPeriodo(codigo);return local.toInstant(offsets.getFirst());
        }catch(DateTimeException erro){throw new FalhaPeriodo(codigo);}
    }
    private static String formatar(Instant instante){return HORARIO.format(instante.atZone(FUSO));}
    private void preencher(){descricao.setText(anterior.descricao());inicio.setText(DATA.format(anterior.dataInicio()));fim.setText(DATA.format(anterior.dataFim()));
        inicioMatricula.setText(formatar(anterior.inicioMatricula()));fimMatricula.setText(formatar(anterior.fimMatricula()));inicioInscricao.setText(formatar(anterior.inicioInscricao()));fimInscricao.setText(formatar(anterior.fimInscricao()));}
    private void apresentar(PeriodoLetivo p){anterior=p;ficha.setText(descrever(p));ficha.setCaretPosition(0);mostrar("ficha");atualizarBotoes();}
    private void cancelar(){if(anterior==null)mostrar("menu");else apresentar(anterior);}
    private void prepararTransicao(boolean abertura){abrindo=abertura;resumoTransicao.setText(descrever(anterior)+"\n\n"+(abertura?"ABRIR: operações só estarão disponíveis dentro das respectivas janelas.":"ENCERRAR: bloqueará matrícula, inscrição e edição do período. Não haverá reabertura nesta versão. Esta ação não homologa pautas nem encerra diários."));mostrar("transicao");}
    private static String descrever(PeriodoLetivo p){return "PERÍODO: "+p.descricao()+"\nSituação: "+p.situacao()+"\n\nCALENDÁRIO DE ATIVIDADES\n"+DATA.format(p.dataInicio())+" a "+DATA.format(p.dataFim())
        +"\n\nMATRÍCULA\n"+formatar(p.inicioMatricula())+" a "+formatar(p.fimMatricula())+"\n\nINSCRIÇÃO EM TURMAS\n"+formatar(p.inicioInscricao())+" a "+formatar(p.fimInscricao())
        +"\n\nHorários: America/Sao_Paulo. Limites inclusivos.\nJanelas somente disponíveis com situação ABERTO.";}
    private static JTextArea area(){var t=new JTextArea(12,45);t.setEditable(false);t.setLineWrap(true);t.setWrapStyleWord(true);return t;}
    private JPanel pagina(String id,String titulo,String ajuda){var p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setName("pagina_"+id);p.add(new JLabel(titulo));
        var texto=area();texto.setText(ajuda);texto.setBackground(UIManager.getColor("Panel.background"));texto.setFont(UIManager.getFont("Label.font"));texto.setMinimumSize(new Dimension(0,55));texto.setPreferredSize(new Dimension(600,55));texto.setMaximumSize(new Dimension(Integer.MAX_VALUE,55));p.add(texto);paginas.add(p,id);return p;}
    private void campo(JPanel p,String titulo,JComponent campo,String nome){var label=new JLabel(titulo);label.setLabelFor(campo);p.add(label);campo.setName(nome);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));p.add(campo);p.add(Box.createVerticalStrut(8));}
    private JButton botao(JPanel p,String nome,String titulo,Runnable acao){var b=new JButton(titulo);b.setName(nome);b.addActionListener(e->{if(!ocupado)acao.run();});p.add(b);p.add(Box.createVerticalStrut(8));return b;}
    private void mostrar(String pagina){navegacao.show(paginas,pagina);revalidate();repaint();}
    private void habilitar(Container raiz,boolean valor){for(var c:raiz.getComponents()){c.setEnabled(valor);if(c instanceof Container grupo)habilitar(grupo,valor);}}
    private void atualizarBotoes(){editar.setEnabled(!ocupado&&anterior!=null&&anterior.situacao()!=SituacaoPeriodoLetivo.ENCERRADO);abrir.setEnabled(!ocupado&&anterior!=null&&anterior.situacao()==SituacaoPeriodoLetivo.PLANEJADO);encerrar.setEnabled(!ocupado&&anterior!=null&&anterior.situacao()==SituacaoPeriodoLetivo.ABERTO);}
    private <T>void executar(Callable<T> trabalho,Consumer<T> sucesso){
        ocupado=true;habilitar(paginas,false);estado.setText("Processando…");
        new SwingWorker<T,Void>(){
            protected T doInBackground()throws Exception{return trabalho.call();}
            protected void done(){ocupado=false;habilitar(paginas,true);
                try{sucesso.accept(get());}catch(InterruptedException e){Thread.currentThread().interrupt();erro(e);}catch(ExecutionException e){erro(e.getCause());}
                atualizarBotoes();estado.setText("Pronto.");
            }
        }.execute();
    }
    private void erro(Throwable erro){
        String mensagem=erro instanceof FalhaPeriodo p?switch(p.codigo()){
            case DESCRICAO_INVALIDA -> "Informe uma descrição para o período. Se o texto for extenso, reduza-o.";
            case CALENDARIO_INVALIDO -> "Confira as datas das atividades: use DD/MM/AAAA, com fim igual ou posterior ao início.";
            case MATRICULA_INVALIDA -> "Confira a janela de matrícula: use DD/MM/AAAA HH:mm, com fim igual ou posterior ao início e horário válido em America/Sao_Paulo.";
            case INSCRICAO_INVALIDA -> "Confira a janela de inscrição: use DD/MM/AAAA HH:mm, com fim igual ou posterior ao início e horário válido em America/Sao_Paulo.";
            case TRANSICAO_INVALIDA -> "Somente períodos planejados podem ser abertos; somente períodos abertos podem ser encerrados.";
            case PERIODO_ENCERRADO -> "Este período está encerrado e não pode ser editado.";
            case CONFLITO_EDICAO -> "O período mudou em outra tela. Cancele e atualize a ficha antes de tentar novamente.";
            case NAO_ENCONTRADO -> "Período não encontrado. Consulte novamente.";
            case OPERACAO_REUTILIZADA -> "Esta confirmação já foi utilizada. Consulte o período salvo antes de iniciar outro cadastro.";
            case INDISPONIVEL -> "Não foi possível concluir a operação. Confira a conexão. Antes de iniciar outro cadastro, consulte se o período foi salvo.";
        }:ControladorAlunos.mensagemErro(erro);dialogos.erro(this,mensagem);
    }
}
