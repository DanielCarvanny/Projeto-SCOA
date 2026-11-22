package br.edu.scoa.academico.apresentacao.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;

/** Edição local das exigências; somente a confirmação grava a matriz. */
public final class PainelPreRequisitos extends JPanel {
    private final MatrizCurricular matriz;
    private final List<ItemCatalogo> disciplinas;
    private final DefaultListModel<PreRequisito> relacoes=new DefaultListModel<>();
    private final JComboBox<ItemCatalogo> cursar=new JComboBox<>(),exigida=new JComboBox<>();
    private final JList<PreRequisito> lista=new JList<>(relacoes);
    private final CardLayout navegacao=new CardLayout();
    private final JPanel paginas=new JPanel(navegacao);
    private final JTextArea revisao=new JTextArea(12,50);
    private final JLabel estado=new JLabel("Pronto.");
    private boolean ocupado;
    public PainelPreRequisitos(GestaoMatrizes gestao,MatrizCurricular matriz,List<ItemCatalogo> disciplinas,
            DialogosAluno dialogos,Consumer<MatrizCurricular> salvo,Runnable cancelar){
        this.matriz=matriz;this.disciplinas=List.copyOf(disciplinas);
        matriz.exigirEditavel();matriz.preRequisitos().forEach(relacoes::addElement);
        setLayout(new BorderLayout(10,10));add(paginas);estado.setName("estadoPreRequisitos");add(estado,BorderLayout.SOUTH);
        var editar=new JPanel();editar.setLayout(new BoxLayout(editar,BoxLayout.Y_AXIS));paginas.add(editar,"editar");
        editar.add(new JLabel("Pré-requisitos — aprovação prévia — "+matriz.codigo()+" / "+matriz.versao()));
        editar.add(new JLabel("Alterações ainda não salvas. Selecione as disciplinas e revise antes de confirmar."));
        editar.add(new JLabel("Disciplina que será cursada"));cursar.setName("disciplinaCursar");editar.add(cursar);
        editar.add(new JLabel("Disciplina exigida antes (da mesma matriz)"));exigida.setName("disciplinaExigida");editar.add(exigida);
        for(var item:disciplinas)cursar.addItem(item);
        cursar.addActionListener(e->filtrar());filtrar();
        botao(editar,"adicionarExigencia","Adicionar exigência",()->{
            var a=(ItemCatalogo)cursar.getSelectedItem();var b=(ItemCatalogo)exigida.getSelectedItem();
            if(a==null||b==null){dialogos.informar(this,"Selecione as duas disciplinas. Pode não haver outra disciplina disponível.");return;}
            var novas=Collections.list(relacoes.elements());novas.add(new PreRequisito(a.id(),b.id()));
            try{matriz.comPreRequisitos(novas);relacoes.addElement(novas.getLast());filtrar();}
            catch(FalhaPreRequisito erro){dialogos.erro(this,mensagem(erro));}
        });
        lista.setName("listaExigencias");lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){
                return super.getListCellRendererComponent(l,v instanceof PreRequisito p?descricao(p):v,i,s,f);
            }
        });editar.add(new JScrollPane(lista));
        botao(editar,"removerExigencia","Remover exigência selecionada",()->{
            var selecionada=lista.getSelectedValue();
            if(selecionada==null){dialogos.informar(this,"Selecione uma exigência.");return;}
            if(dialogos.confirmar(this,"Remover da configuração: "+descricao(selecionada)+"?")){
                relacoes.removeElement(selecionada);filtrar();
            }
        });
        botao(editar,"revisarExigencias","Continuar para salvar",()->{
            var texto=new StringBuilder("Matriz: ").append(matriz.codigo()).append(" / ").append(matriz.versao()).append("\n\n");
            if(relacoes.isEmpty())texto.append("Nenhum pré-requisito será configurado.");
            for(var p:Collections.list(relacoes.elements()))texto.append(descricao(p)).append("\n");
            revisao.setText(texto.toString());revisao.setCaretPosition(0);navegacao.show(paginas,"revisao");
        });
        botao(editar,"cancelarConfiguracao","Cancelar sem salvar",cancelar);
        var confirmar=new JPanel(new BorderLayout(10,10));paginas.add(confirmar,"revisao");
        confirmar.add(new JLabel("Confira as exigências antes de salvar"),BorderLayout.NORTH);
        revisao.setEditable(false);revisao.setLineWrap(true);revisao.setWrapStyleWord(true);confirmar.add(new JScrollPane(revisao));
        var acoes=new JPanel();confirmar.add(acoes,BorderLayout.SOUTH);
        botao(acoes,"voltarExigencias","Voltar",()->navegacao.show(paginas,"editar"));
        botao(acoes,"salvarExigencias","Salvar pré-requisitos",()->{
            var entrada=matriz.comPreRequisitos(Collections.list(relacoes.elements()));
            ocupado=true;habilitar(this,false);estado.setText("Salvando…");
            new SwingWorker<MatrizCurricular,Void>(){
                protected MatrizCurricular doInBackground(){return gestao.salvar(entrada);}
                protected void done(){
                    ocupado=false;habilitar(PainelPreRequisitos.this,true);estado.setText("Pronto.");
                    try{var resultado=get();salvo.accept(resultado);dialogos.informar(PainelPreRequisitos.this,"Pré-requisitos salvos.");}
                    catch(InterruptedException erro){Thread.currentThread().interrupt();dialogos.erro(PainelPreRequisitos.this,"Operação interrompida. Consulte a matriz antes de tentar novamente.");}
                    catch(ExecutionException erro){
                        var causa=erro.getCause();
                        String texto=causa instanceof FalhaPreRequisito p?mensagem(p):
                            causa instanceof FalhaMatriz m?(m.codigo()==FalhaMatriz.Codigo.CONFLITO_EDICAO?
                                "A matriz mudou em outra tela. Cancele e consulte novamente.":m.codigo()==FalhaMatriz.Codigo.MATRIZ_NAO_EDITAVEL?
                                "A matriz não está mais em elaboração. Cancele e consulte novamente.":"Não foi possível salvar. Consulte a matriz e tente novamente."):
                            ControladorAlunos.mensagemErro(causa);
                        dialogos.erro(PainelPreRequisitos.this,texto);
                    }
                }
            }.execute();
        });
        botao(acoes,"cancelarExigencias","Cancelar sem salvar",cancelar);
    }
    private void filtrar(){
        exigida.removeAllItems();var selecionada=(ItemCatalogo)cursar.getSelectedItem();if(selecionada==null)return;
        for(var item:disciplinas)if(item.id()!=selecionada.id()&&Collections.list(relacoes.elements()).stream()
            .noneMatch(p->p.disciplinaId()==selecionada.id()&&p.disciplinaExigidaId()==item.id()))exigida.addItem(item);
        exigida.setSelectedIndex(-1);
    }
    private String nome(long id){return disciplinas.stream().filter(d->d.id()==id).findFirst().map(d->d.codigo()+" — "+d.nome()).orElse("Disciplina "+id);}
    private String descricao(PreRequisito p){return nome(p.disciplinaId())+" exige aprovação prévia em "+nome(p.disciplinaExigidaId());}
    public static String mensagem(FalhaPreRequisito erro){
        return switch(erro.codigo()){
            case AUTO_REFERENCIA -> "Uma disciplina não pode exigir aprovação nela mesma.";
            case DUPLICADO -> "Essa exigência já está cadastrada.";
            case FORA_DA_MATRIZ -> "As duas disciplinas devem pertencer à mesma matriz.";
            case CICLO -> "Essa exigência cria um ciclo: uma disciplina passaria a depender dela mesma por meio de outras. Revise a sequência.";
            case COMPONENTE_UTILIZADO -> "O componente possui pré-requisitos ou é exigido por outro. Remova e salve essas relações antes de remover o componente.";
        };
    }
    private void botao(JPanel painel,String id,String texto,Runnable acao){var b=new JButton(texto);b.setName(id);b.addActionListener(e->{if(!ocupado)acao.run();});painel.add(b);}
    private void habilitar(Container raiz,boolean valor){for(var c:raiz.getComponents()){c.setEnabled(valor);if(c instanceof Container grupo)habilitar(grupo,valor);}}
}

