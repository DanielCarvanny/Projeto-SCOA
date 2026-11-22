package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.identidade.PainelIdentidade;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoPeriodos;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.curriculo.FluxoCatalogoTest.*;
class MenuPeriodosTest {
    @Test void menuAbrePeriodosComSessaoAutenticada()throws Exception{
        var c=new CenarioPeriodos();var painel=new PainelIdentidade[1];var dialogos=new Dialogos();
        SwingUtilities.invokeAndWait(()->{
            painel[0]=new PainelIdentidade(c.identidade.identidade,dialogos,(s,a)->new JPanel(),null,null,
                sessao->new FluxoPeriodos(new br.edu.scoa.academico.aplicacao.curriculo.GestaoPeriodos(c.identidade.identidade,sessao,c,c.identidade.relogio),dialogos));
            preencher(painel[0],"loginEntrada","coord");clicar(painel[0],"entrar");
        });
        try{
            esperar(painel[0],"periodos");SwingUtilities.invokeAndWait(()->clicar(painel[0],"periodos"));esperar(painel[0],"novoPeriodo");
        }finally{SwingUtilities.invokeAndWait(painel[0]::encerrar);}
    }
    private static void esperar(PainelIdentidade painel,String nome)throws Exception{
        long limite=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while(System.nanoTime()<limite){var pronto=new boolean[1];SwingUtilities.invokeAndWait(()->{var c=encontrar(painel,nome);pronto[0]=c!=null&&c.isEnabled();});if(pronto[0])return;Thread.sleep(20);}fail("Controle não disponível: "+nome);
    }
}
