package br.edu.scoa.academico.apresentacao.curriculo;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

public final class FluxoMatrizes extends JPanel {
    private static final DateTimeFormatter DATA=DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final GestaoMatrizes gestao;
    private final DialogosAluno dialogos;
    private final CardLayout navegacao=new CardLayout();
    private final JPanel paginas=new JPanel(navegacao);
    private final JLabel estado=new JLabel("Pronto.");
    private final JComboBox<ItemCatalogo> cursos=new JComboBox<>(), filtroCurso=new JComboBox<>(), disciplinas=new JComboBox<>();
    private final JComboBox<TipoComponente> tipo=new JComboBox<>(TipoComponente.values());
    private final JTextField codigo=new JTextField(),versao=new JTextField(),inicio=new JTextField(),fim=new JTextField(),
        periodo=new JTextField(),carga=new JTextField(),busca=new JTextField();
    private final JTextArea resumo=area(),ficha=area(),conteudo=area();
    private final DefaultListModel<ComponenteCurricular> componentes=new DefaultListModel<>();
    private final JList<ComponenteCurricular> lista=new JList<>(componentes), listaFicha=new JList<>(componentes);
    private final DefaultListModel<MatrizCurricular> resultados=new DefaultListModel<>();
    private final JList<MatrizCurricular> matrizes=new JList<>(resultados);
    private final Map<String,ItemCatalogo> referencias=new HashMap<>();
    private final List<JButton> botoesEdicao=new ArrayList<>();
    private ItemCatalogo curso;
    private MatrizCurricular anterior,revisada;
    private ComponenteCurricular editando;
    private boolean ocupado;
    private JButton publicar,copiar;
    private String retornoEditor="componentes";

    public FluxoMatrizes(GestaoMatrizes gestao,DialogosAluno dialogos){
        this.gestao=gestao;this.dialogos=dialogos;
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(paginas);estado.setName("estadoMatriz");add(estado,BorderLayout.SOUTH);
        var menu=pagina("menu","Matrizes curriculares","Cada matriz reúne as disciplinas de um curso. Crie, consulte, publique ou prepare uma nova versão.");
        botao(menu,"novaMatriz","Criar matriz",()->executar(()->gestao.catalogo(TipoCatalogo.CURSO),itens->{
            anterior=null;revisada=null;curso=null;componentes.clear();referencias.clear();codigo.setText("");versao.setText("");inicio.setText("");fim.setText("");
            preencher(cursos,itens);mostrar("curso");
        }));
        botao(menu,"consultarMatrizes","Consultar matriz",()->executar(()->gestao.catalogo(TipoCatalogo.CURSO),itens->{
            filtroCurso.removeAllItems();filtroCurso.addItem(null);itens.forEach(filtroCurso::addItem);resultados.clear();busca.setText("");mostrar("consulta");
        }));
        var escolher=pagina("curso","1. Selecionar curso","Escolha o curso ao qual a matriz pertence. Cursos e disciplinas devem estar cadastrados.");
        campo(escolher,"Curso",cursos,"curso");
        botao(escolher,"selecionarCurso","Continuar",()->{
            curso=(ItemCatalogo)cursos.getSelectedItem();
            if(curso==null){dialogos.informar(this,"Cadastre e selecione um curso antes de continuar.");return;}
            mostrar("dados");
        });
        botao(escolher,"cancelarCurso","Cancelar",()->mostrar("menu"));

        var dados=pagina("dados","2. Identificação e vigência","A versão curricular identifica esta composição. O curso não muda após a criação.");
        campo(dados,"Código da matriz (até 40 caracteres)",codigo,"codigoMatriz");
        campo(dados,"Versão curricular (até 255 caracteres; exemplo: 2027.1)",versao,"versaoMatriz");
        campo(dados,"Início de vigência — DD/MM/AAAA",inicio,"inicioVigencia");
        campo(dados,"Fim de vigência — DD/MM/AAAA (opcional)",fim,"fimVigencia");
        botao(dados,"montarComponentes","Continuar para componentes",()->{
            try{validarCabecalho();mostrar("componentes");}catch(RuntimeException erro){erro(erro);}
        });
        botao(dados,"cancelarDadosMatriz","Cancelar alterações",this::cancelar);

        var montar=pagina("componentes","3. Montar componentes","As mudanças permanecem na tela até Revisar e Confirmar. É obrigatório manter ao menos uma disciplina.");
        lista.setName("componentes");lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaFicha.setName("componentesFicha");listaFicha.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        var renderizador=new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> l,Object valor,int indice,boolean selecionado,boolean foco){
                super.getListCellRendererComponent(l,valor,indice,selecionado,foco);
                if(valor instanceof ComponenteCurricular componente)setText(descrever(componente));
                return this;
            }
        };
        lista.setCellRenderer(renderizador);listaFicha.setCellRenderer(renderizador);montar.add(new JScrollPane(lista));
        botao(montar,"adicionarComponente","Adicionar componente",()->abrirEditor(null,"componentes"));
        botao(montar,"editarComponente","Editar componente selecionado",()->editarSelecionado(lista,"componentes"));
        botao(montar,"removerComponente","Remover componente selecionado",()->remover(lista,false));
        botao(montar,"revisarMatriz","Revisar matriz inteira",this::revisar);
        botao(montar,"voltarCabecalho","Voltar aos dados da matriz",()->mostrar("dados"));
        botao(montar,"cancelarMontagem","Cancelar alterações",this::cancelar);

        var editor=pagina("editor","Dados do componente","Escolha disciplina, tipo, período ideal e carga considerada nesta matriz.");
        campo(editor,"Disciplina",disciplinas,"disciplina");
        disciplinas.addActionListener(e->{
            var item=(ItemCatalogo)disciplinas.getSelectedItem();
            if(item!=null)conteudo.setText("Revisão de conteúdo: "+item.versao()+"\nEmenta: "+item.ementa());
        });
        conteudo.setEditable(false);editor.add(new JScrollPane(conteudo));
        campo(editor,"Tipo",tipo,"tipoComponente");campo(editor,"Período ideal (inteiro positivo)",periodo,"periodoIdeal");
        campo(editor,"Carga horária considerada (horas inteiras positivas)",carga,"cargaComponente");
        botao(editor,"atualizarRevisao","Usar revisão atual da disciplina",()->{
            var escolhida=(ItemCatalogo)disciplinas.getSelectedItem();if(escolhida==null)return;
            executar(()->gestao.catalogo(TipoCatalogo.DISCIPLINA),itens->{
                var atual=itens.stream().filter(i->i.id()==escolhida.id()).findFirst().orElseThrow();
                if(atual.versao()==escolhida.versao()){dialogos.informar(this,"Este componente já usa a revisão atual.");return;}
                if(dialogos.confirmar(this,"Substituir explicitamente a revisão "+escolhida.versao()+" pela "+atual.versao()+"?\nO conteúdo novo aparecerá antes da confirmação da matriz.")){
                    disciplinas.removeAllItems();disciplinas.addItem(atual);referencias.put(chave(atual.id(),atual.versao()),atual);
                }
            });
        });
        botao(editor,"aplicarComponente","Aplicar à montagem",()->{
            try{
                var escolhida=(ItemCatalogo)disciplinas.getSelectedItem();
                if(escolhida==null)throw new FalhaMatriz(FalhaMatriz.Codigo.COMPONENTE_INVALIDO);
                var componente=new ComponenteCurricular(escolhida.id(),escolhida.versao(),(TipoComponente)tipo.getSelectedItem(),
                    inteiro(periodo.getText()),inteiro(carga.getText()));
                for(int i=0;i<componentes.size();i++)
                    if(componentes.get(i).disciplinaId()==componente.disciplinaId() && (editando==null || editando.disciplinaId()!=componente.disciplinaId()))
                        throw new FalhaMatriz(FalhaMatriz.Codigo.DISCIPLINA_REPETIDA);
                if(editando==null)componentes.addElement(componente);else componentes.set(componentes.indexOf(editando),componente);
                referencias.put(chave(escolhida.id(),escolhida.versao()),escolhida);mostrar("componentes");
            }catch(RuntimeException erro){erro(erro);}
        });
        botao(editor,"cancelarComponente","Cancelar edição do componente",()->mostrar(retornoEditor));

        var confirmar=pagina("revisao","4. Revisar e confirmar","A confirmação salva cabeçalho, componentes e auditoria juntos. Situação: em elaboração.");
        resumo.setEditable(false);resumo.setName("resumoMatriz");confirmar.add(new JScrollPane(resumo));
        botao(confirmar,"confirmarMatriz","Confirmar e salvar",()->{
            var entrada=revisada;var cursoSalvo=curso;var referenciasSalvas=List.copyOf(referencias.values());
            executar(()->gestao.salvar(entrada),salva->{
                apresentar(new Carregamento(salva,cursoSalvo,referenciasSalvas));
                dialogos.informar(this,"Matriz salva em elaboração. Revisão "+anterior.revisao()+".");
            });
        });
        botao(confirmar,"corrigirMatriz","Voltar à montagem",()->mostrar("componentes"));
        botao(confirmar,"cancelarRevisaoMatriz","Cancelar sem salvar",this::cancelar);

        var consultar=pagina("consulta","Consultar matrizes","Filtre pelo curso e por código ou versão curricular. Curso vazio pesquisa todos.");
        filtroCurso.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){
                return super.getListCellRendererComponent(l,v==null?"Todos os cursos":v,i,s,f);
            }
        });
        campo(consultar,"Curso",filtroCurso,"filtroCurso");campo(consultar,"Código ou versão",busca,"buscaMatriz");
        botao(consultar,"pesquisarMatriz","Pesquisar",()->{
            var filtro=(ItemCatalogo)filtroCurso.getSelectedItem();long cursoId=filtro==null?0:filtro.id();String termo=busca.getText();
            executar(()->gestao.pesquisar(cursoId,termo),itens->{resultados.clear();itens.forEach(resultados::addElement);if(itens.isEmpty())dialogos.informar(this,"Nenhuma matriz encontrada.");});
        });
        matrizes.setName("resultadosMatriz");matrizes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);consultar.add(new JScrollPane(matrizes));
        botao(consultar,"abrirMatriz","Abrir matriz selecionada",()->{
            var selecionada=matrizes.getSelectedValue();if(selecionada==null){dialogos.informar(this,"Selecione uma matriz.");return;}
            executar(()->carregar(gestao.consultar(selecionada.id())),this::apresentar);
        });
        botao(consultar,"voltarConsultaMatriz","Voltar ao menu",()->mostrar("menu"));

        var detalhes=pagina("ficha","Ficha da matriz","Edições só serão gravadas depois de revisar e confirmar a matriz.");
        ficha.setEditable(false);ficha.setName("fichaMatriz");detalhes.add(new JScrollPane(ficha));detalhes.add(new JScrollPane(listaFicha));
        botoesEdicao.add(botao(detalhes,"editarDadosMatriz","Editar identificação e vigência",()->mostrar("dados")));
        botoesEdicao.add(botao(detalhes,"adicionarNaFicha","Adicionar componente",()->abrirEditor(null,"ficha")));
        botoesEdicao.add(botao(detalhes,"editarNaFicha","Editar componente selecionado",()->editarSelecionado(listaFicha,"ficha")));
        botoesEdicao.add(botao(detalhes,"removerNaFicha","Remover componente selecionado",()->remover(listaFicha,true)));
        botoesEdicao.add(botao(detalhes,"gerenciarPreRequisitos","Gerenciar pré-requisitos",()->{
            var matriz=anterior;
            var itens=matriz.componentes().stream().map(c->referencias.get(chave(c.disciplinaId(),c.disciplinaRevisao()))).toList();
            for(var pagina:paginas.getComponents())if("pagina_preRequisitos".equals(pagina.getName()))paginas.remove(pagina);
            var painel=new PainelPreRequisitos(gestao,matriz,itens,dialogos,
                salva->apresentar(new Carregamento(salva,curso,itens)),()->mostrar("ficha"));
            painel.setName("pagina_preRequisitos");paginas.add(painel,"preRequisitos");mostrar("preRequisitos");
        }));
        publicar=botao(detalhes,"publicarMatriz","Publicar matriz",()->abrirPublicacao(false));
        copiar=botao(detalhes,"novaVersaoMatriz","Criar nova versão",()->abrirPublicacao(true));
        botao(detalhes,"voltarFichaMatriz","Voltar à consulta",()->{resultados.clear();mostrar("consulta");});
        mostrar("menu");
    }
    private static int inteiro(String valor){
        try{return Integer.parseInt(valor.strip());}catch(NumberFormatException erro){throw new FalhaMatriz(FalhaMatriz.Codigo.COMPONENTE_INVALIDO);}
    }
    private LocalDate data(String valor,boolean opcional){
        if(opcional&&valor.isBlank())return null;
        try{return LocalDate.parse(valor.strip(),DATA);}catch(DateTimeParseException erro){throw new FalhaMatriz(FalhaMatriz.Codigo.DATAS_INVALIDAS);}
    }
    private void validarCabecalho(){
        MatrizCurricular.validarCabecalho(curso==null?0:curso.id(),codigo.getText(),versao.getText(),
            data(inicio.getText(),false),data(fim.getText(),true));
    }
    private MatrizCurricular construir(List<ComponenteCurricular> itens){
        return new MatrizCurricular(anterior==null?0:anterior.id(),curso==null?0:curso.id(),codigo.getText(),versao.getText(),
            data(inicio.getText(),false),data(fim.getText(),true),SituacaoMatriz.EM_ELABORACAO,anterior==null?0:anterior.revisao(),itens,
            anterior==null?List.of():anterior.preRequisitos(),anterior==null?null:anterior.matrizOrigemId());
    }
    private void revisar(){
        try{revisada=construir(Collections.list(componentes.elements()));resumo.setText(descrever(revisada));resumo.setCaretPosition(0);mostrar("revisao");}
        catch(RuntimeException erro){erro(erro);}
    }
    private void editarSelecionado(JList<ComponenteCurricular> origem,String retorno){
        var item=origem.getSelectedValue();if(item==null){dialogos.informar(this,"Selecione um componente.");return;}abrirEditor(item,retorno);
    }
    private void abrirEditor(ComponenteCurricular item,String retorno){
        editando=item;retornoEditor=retorno;
        executar(()->item==null?gestao.catalogo(TipoCatalogo.DISCIPLINA):List.of(gestao.disciplina(item.disciplinaId(),item.disciplinaRevisao())),itens->{
            preencher(disciplinas,itens);if(item!=null)disciplinas.setSelectedIndex(0);disciplinas.setEnabled(item==null);
            tipo.setSelectedItem(item==null?null:item.tipo());periodo.setText(item==null?"":Integer.toString(item.periodoIdeal()));
            carga.setText(item==null?"":Integer.toString(item.cargaHorariaConsiderada()));mostrar("editor");
        });
    }
    private void remover(JList<ComponenteCurricular> origem,boolean veioDaFicha){
        var item=origem.getSelectedValue();if(item==null){dialogos.informar(this,"Selecione um componente.");return;}
        if(anterior!=null){
            var envolvidas=anterior.preRequisitos().stream().filter(p->p.disciplinaId()==item.disciplinaId()||p.disciplinaExigidaId()==item.disciplinaId()).toList();
            if(!envolvidas.isEmpty()){
                String detalhe=envolvidas.stream().map(p->nomeDisciplina(p.disciplinaId())+" exige "+nomeDisciplina(p.disciplinaExigidaId())).collect(java.util.stream.Collectors.joining("\n"));
                dialogos.erro(this,"Remova e salve os pré-requisitos antes de remover o componente:\n"+detalhe);return;
            }
        }
        if(componentes.size()==1){erro(new FalhaMatriz(FalhaMatriz.Codigo.MATRIZ_VAZIA));return;}
        if(dialogos.confirmar(this,"Remover "+descrever(item)+" da montagem? A gravação ocorrerá só após confirmar a matriz.")){
            componentes.removeElement(item);if(veioDaFicha)mostrar("componentes");
        }
    }
    private record Carregamento(MatrizCurricular matriz,ItemCatalogo curso,List<ItemCatalogo> disciplinas){}
    private Carregamento carregar(MatrizCurricular matriz){
        var escolhido=gestao.catalogo(TipoCatalogo.CURSO).stream().filter(c->c.id()==matriz.cursoId()).findFirst().orElseThrow();
        var disciplinas=new ArrayList<ItemCatalogo>();for(var c:matriz.componentes())disciplinas.add(gestao.disciplina(c.disciplinaId(),c.disciplinaRevisao()));
        return new Carregamento(matriz,escolhido,disciplinas);
    }
    private void apresentar(Carregamento carregamento){
        anterior=carregamento.matriz();curso=carregamento.curso();referencias.clear();
        for(var item:carregamento.disciplinas())referencias.put(chave(item.id(),item.versao()),item);
        codigo.setText(anterior.codigo());versao.setText(anterior.versao());inicio.setText(DATA.format(anterior.inicioVigencia()));
        fim.setText(anterior.fimVigencia()==null?"":DATA.format(anterior.fimVigencia()));
        componentes.clear();anterior.componentes().forEach(componentes::addElement);
        ficha.setText(descrever(anterior));ficha.setCaretPosition(0);atualizarBotoes();mostrar("ficha");
    }
    private void cancelar(){
        if(anterior==null){componentes.clear();mostrar("menu");}
        else {long id=anterior.id();executar(()->carregar(gestao.consultar(id)),this::apresentar);}
    }
    private static String chave(long id,long revisao){return id+":"+revisao;}
    private String nomeDisciplina(long id){
        return referencias.values().stream().filter(d->d.id()==id).findFirst().map(d->d.codigo()+" — "+d.nome()).orElse("Disciplina "+id);
    }
    private String descrever(ComponenteCurricular c){
        var disciplina=referencias.get(chave(c.disciplinaId(),c.disciplinaRevisao()));
        return (disciplina==null?"Disciplina "+c.disciplinaId():disciplina.codigo()+" — "+disciplina.nome())
            +" · revisão "+c.disciplinaRevisao()+" · "+c.tipo()+" · período "+c.periodoIdeal()+" · "+c.cargaHorariaConsiderada()+" h";
    }
    private String descrever(MatrizCurricular matriz){
        var texto=new StringBuilder("Curso: ").append(curso).append("\nCódigo: ").append(matriz.codigo())
            .append("\nVersão curricular: ").append(matriz.versao()).append("\nVigência: ").append(DATA.format(matriz.inicioVigencia()))
            .append(" a ").append(matriz.fimVigencia()==null?"sem fim definido":DATA.format(matriz.fimVigencia()))
            .append("\nMatriz de origem: ").append(matriz.matrizOrigemId()==null?"sem origem (criação inicial)":matriz.matrizOrigemId()).append("\nSituação: ").append(matriz.situacao()==SituacaoMatriz.VIGENTE?"Publicada — aplicável somente no intervalo de vigência":matriz.situacao()).append("\nCarga obrigatória: ").append(matriz.cargaObrigatoria())
            .append(" h (não altera a carga total do curso)\n\nCOMPONENTES\n");
        for(var c:matriz.componentes()){
            texto.append(descrever(c)).append("\n");var d=referencias.get(chave(c.disciplinaId(),c.disciplinaRevisao()));
            if(d!=null)texto.append("Ementa desta revisão: ").append(d.ementa()).append("\n\n");
        }
        texto.append("\nPRÉ-REQUISITOS\n");
        if(matriz.preRequisitos().isEmpty())texto.append("Nenhum configurado.\n");
        for(var p:matriz.preRequisitos())texto.append(nomeDisciplina(p.disciplinaId())).append(" exige aprovação prévia em ").append(nomeDisciplina(p.disciplinaExigidaId())).append("\n");
        return texto.toString();
    }
    private void abrirPublicacao(boolean novaVersao){
        for(var pagina:paginas.getComponents())if("pagina_publicacao".equals(pagina.getName()))paginas.remove(pagina);
        var itens=List.copyOf(referencias.values());var cursoAtual=curso;
        var painel=new PainelPublicacaoMatriz(gestao,anterior,descrever(anterior),novaVersao,dialogos,
            salva->apresentar(new Carregamento(salva,cursoAtual,itens)),()->mostrar("ficha"));
        painel.setName("pagina_publicacao");paginas.add(painel,"publicacao");mostrar("publicacao");
    }
    private void atualizarBotoes(){
        boolean editavel=!ocupado&&anterior!=null&&anterior.situacao()==SituacaoMatriz.EM_ELABORACAO;
        for(var b:botoesEdicao)b.setEnabled(editavel);
        publicar.setEnabled(editavel);copiar.setEnabled(!ocupado&&anterior!=null&&anterior.situacao()==SituacaoMatriz.VIGENTE);
    }
    private static JTextArea area(){var a=new JTextArea(5,40);a.setLineWrap(true);a.setWrapStyleWord(true);return a;}
    private static void preencher(JComboBox<ItemCatalogo> combo,List<ItemCatalogo> itens){combo.removeAllItems();itens.forEach(combo::addItem);combo.setSelectedIndex(-1);}
    private JPanel pagina(String id,String titulo,String orientacao){
        var p=new JPanel();p.setName("pagina_"+id);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(new JLabel(titulo));
        var ajuda=area();ajuda.setText(orientacao);ajuda.setEditable(false);ajuda.setBackground(UIManager.getColor("Panel.background"));
        ajuda.setFont(UIManager.getFont("Label.font"));ajuda.setMaximumSize(new Dimension(Integer.MAX_VALUE,55));p.add(ajuda);paginas.add(p,id);return p;
    }
    private void campo(JPanel p,String titulo,JComponent campo,String id){
        var rotulo=new JLabel(titulo);rotulo.setLabelFor(campo);p.add(rotulo);campo.setName(id);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));p.add(campo);p.add(Box.createVerticalStrut(8));
    }
    private JButton botao(JPanel p,String id,String texto,Runnable acao){
        var b=new JButton(texto);b.setName(id);b.addActionListener(e->{if(!ocupado)acao.run();});p.add(b);p.add(Box.createVerticalStrut(6));return b;
    }
    private void mostrar(String pagina){navegacao.show(paginas,pagina);revalidate();repaint();}
    private void habilitar(Container raiz,boolean valor){for(var c:raiz.getComponents()){c.setEnabled(valor);if(c instanceof Container grupo)habilitar(grupo,valor);}}
    private <T>void executar(Callable<T> trabalho,Consumer<T> sucesso){
        ocupado=true;habilitar(paginas,false);estado.setText("Processando…");
        new SwingWorker<T,Void>(){
            protected T doInBackground()throws Exception{return trabalho.call();}
            protected void done(){
                ocupado=false;habilitar(paginas,true);estado.setText("Pronto.");
                try{sucesso.accept(get());}catch(InterruptedException erro){Thread.currentThread().interrupt();erro(erro);}
                catch(ExecutionException erro){erro(erro.getCause());}
                atualizarBotoes();if(editando!=null)disciplinas.setEnabled(false);
            }
        }.execute();
    }
    private void erro(Throwable erro){
        if(erro instanceof FalhaPreRequisito falha){dialogos.erro(this,PainelPreRequisitos.mensagem(falha));return;}
        String mensagem=erro instanceof FalhaMatriz f?switch(f.codigo()){
            case MATRIZ_VAZIA -> "A matriz precisa ter ao menos um componente. Não é permitido remover o último.";
            case DISCIPLINA_REPETIDA -> "Esta disciplina já está na matriz. Edite o componente existente.";
            case COMPONENTE_INVALIDO -> "Selecione disciplina e tipo. Período ideal e carga devem ser números inteiros maiores que zero.";
            case IDENTIFICACAO_INVALIDA -> "Selecione um curso e informe código (até 40 caracteres) e versão curricular (até 255).";
            case DATAS_INVALIDAS -> "Informe datas válidas em DD/MM/AAAA. O fim não pode ser anterior ao início.";
            case MATRIZ_NAO_EDITAVEL -> "Somente matrizes em elaboração podem ser alteradas.";
            case MATRIZ_DUPLICADA -> "Já existe uma matriz com este curso, código e versão curricular.";
            case CONFLITO_EDICAO -> "A matriz foi alterada em outra tela. Cancele a edição e consulte novamente.";
            case REFERENCIA_INEXISTENTE -> "O curso ou a revisão de disciplina não existe. Confira os cadastros e consulte novamente.";
            case CURSO_IMUTAVEL -> "O curso de uma matriz existente não pode ser trocado.";
            case NAO_ENCONTRADA -> "Matriz não encontrada.";
            case INDISPONIVEL -> "Não foi possível concluir a operação da matriz. Confira a conexão e tente novamente.";
        }:ControladorAlunos.mensagemErro(erro);
        dialogos.erro(this,mensagem);
    }
}


