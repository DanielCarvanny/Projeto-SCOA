package br.edu.scoa.academico.apresentacao.curriculo;

import br.edu.scoa.academico.aplicacao.curriculo.GestaoMatrizes;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

public final class PainelPublicacaoMatriz extends JPanel {
    private static final DateTimeFormatter DATA=DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final DialogosAluno dialogos;
    private boolean ocupado;
    private MatrizCurricular revisada;

    public PainelPublicacaoMatriz(GestaoMatrizes gestao,MatrizCurricular origem,String resumo,boolean copiar,
                                 DialogosAluno dialogos,Consumer<MatrizCurricular> salvo,Runnable cancelar){
        this.dialogos=dialogos;setLayout(new BorderLayout(8,8));
        var navegacao=new CardLayout();var paginas=new JPanel(navegacao);add(paginas);
        var revisao=new JPanel(new BorderLayout(8,8));paginas.add(revisao,"revisao");
        revisao.add(new JLabel(copiar?"Revisar nova versão em elaboração":"Confirmar publicação da matriz"),BorderLayout.NORTH);
        var texto=new JTextArea();texto.setEditable(false);texto.setLineWrap(true);texto.setWrapStyleWord(true);
        texto.setText(resumo+"\n\nPublicar congela os dados, componentes e pré-requisitos. A aplicação da matriz depende do intervalo de vigência. Outras versões permanecem inalteradas.");
        revisao.add(new JScrollPane(texto));var acoes=new JPanel();revisao.add(acoes,BorderLayout.SOUTH);
        if(copiar){
            var formulario=new JPanel();formulario.setLayout(new BoxLayout(formulario,BoxLayout.Y_AXIS));paginas.add(formulario,"dados");
            formulario.add(new JLabel("Criar nova versão a partir da matriz publicada"));
            var codigo=campo(formulario,"Código","novoCodigo",origem.codigo());
            var versao=campo(formulario,"Nova versão curricular","novaVersao","");
            var inicio=campo(formulario,"Início de vigência — DD/MM/AAAA","novoInicio","");
            var fim=campo(formulario,"Fim de vigência — DD/MM/AAAA (opcional)","novoFim","");
            botao(formulario,"revisarCopia","Revisar nova versão",()->{
                try{
                    revisada=origem.novaVersao(codigo.getText(),versao.getText(),data(inicio.getText(),false),data(fim.getText(),true));
                    texto.setText("NOVA VERSÃO: "+revisada.codigo()+" / "+revisada.versao()+"\nVigência: "+DATA.format(revisada.inicioVigencia())+" a "+(revisada.fimVigencia()==null?"sem fim definido":DATA.format(revisada.fimVigencia()))
                        +"\nSerá salva em elaboração. A composição e as revisões históricas abaixo serão copiadas; a origem será preservada.\n\nMATRIZ DE ORIGEM\n"+resumo);
                    texto.setCaretPosition(0);navegacao.show(paginas,"revisao");
                }catch(RuntimeException erro){erro(erro);}
            });
            botao(formulario,"cancelarNovaVersao","Cancelar",cancelar);
            botao(acoes,"confirmarCopia","Confirmar e criar nova versão",()->{
                if(revisada==null)return;
                var entrada=revisada;
                executar(()->gestao.copiar(origem,entrada.codigo(),entrada.versao(),entrada.inicioVigencia(),entrada.fimVigencia()),salvo);
            });
            botao(acoes,"voltarCopia","Voltar aos dados",()->navegacao.show(paginas,"dados"));
            navegacao.show(paginas,"dados");
        }else botao(acoes,"confirmarPublicacao","Confirmar publicação",()->executar(()->gestao.publicar(origem),salvo));
        botao(acoes,"cancelarPublicacao","Cancelar",cancelar);
    }
    private JTextField campo(JPanel painel,String rotulo,String nome,String valor){
        var campo=new JTextField(valor);campo.setName(nome);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        var label=new JLabel(rotulo);label.setLabelFor(campo);painel.add(label);painel.add(campo);painel.add(Box.createVerticalStrut(10));return campo;
    }
    private void botao(JPanel painel,String nome,String texto,Runnable acao){
        var botao=new JButton(texto);botao.setName(nome);botao.addActionListener(e->{if(!ocupado)acao.run();});painel.add(botao);
    }
    private static LocalDate data(String valor,boolean opcional){
        if(opcional&&valor.isBlank())return null;
        try{return LocalDate.parse(valor.strip(),DATA);}catch(DateTimeParseException erro){throw new FalhaMatriz(FalhaMatriz.Codigo.DATAS_INVALIDAS);}
    }
    private void habilitar(Container raiz,boolean habilitado){
        for(var componente:raiz.getComponents()){componente.setEnabled(habilitado);if(componente instanceof Container grupo)habilitar(grupo,habilitado);}
    }
    private void executar(Callable<MatrizCurricular> trabalho,Consumer<MatrizCurricular> salvo){
        ocupado=true;habilitar(this,false);
        new SwingWorker<MatrizCurricular,Void>(){
            protected MatrizCurricular doInBackground()throws Exception{return trabalho.call();}
            protected void done(){
                try{var resultado=get();salvo.accept(resultado);}
                catch(InterruptedException falha){Thread.currentThread().interrupt();ocupado=false;habilitar(PainelPublicacaoMatriz.this,true);erro(falha);}
                catch(ExecutionException falha){ocupado=false;habilitar(PainelPublicacaoMatriz.this,true);erro(falha.getCause());}
            }
        }.execute();
    }
    private void erro(Throwable falha){
        String mensagem= falha instanceof FalhaMatriz matriz?switch(matriz.codigo()){
            case DATAS_INVALIDAS -> "Informe datas válidas em DD/MM/AAAA; o fim não pode ser anterior ao início.";
            case IDENTIFICACAO_INVALIDA -> "Informe código (até 40 caracteres) e versão curricular (até 255).";
            case MATRIZ_DUPLICADA -> "Escolha outra versão: já existe uma matriz com este curso, código e versão.";
            case CONFLITO_EDICAO -> "A matriz mudou desde a consulta. Cancele e consulte novamente.";
            case MATRIZ_NAO_EDITAVEL -> "Publique apenas matrizes em elaboração. Para criar outra versão, selecione uma matriz publicada.";
            case MATRIZ_VAZIA -> "A matriz precisa de ao menos um componente antes da publicação.";
            default -> "Não foi possível concluir a operação. Confira a matriz e a conexão e consulte novamente.";
        }:falha instanceof FalhaPreRequisito pre?PainelPreRequisitos.mensagem(pre):ControladorAlunos.mensagemErro(falha);
        dialogos.erro(this,mensagem);
    }
}
