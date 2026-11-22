package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoMatrizes;
import br.edu.scoa.academico.apresentacao.alunos.DialogosJOptionPane;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

/** Demonstração exclusivamente em memória, sem acesso ao MySQL. */
public final class DemonstracaoMatrizes {
    private DemonstracaoMatrizes(){}
    public static void main(String[] argumentos){
        var cenario=new CenarioMatrizes();
        cenario.gestao.salvar(new MatrizCurricular(0,cenario.curso.id(),"ADS-DEMO","2027.1",LocalDate.of(2027,1,1),null,
            SituacaoMatriz.EM_ELABORACAO,0,List.of(new ComponenteCurricular(cenario.disciplina.id(),1,TipoComponente.OBRIGATORIO,1,80))));
        SwingUtilities.invokeLater(()->{
            var janela=new JFrame("SCOA — Matrizes — DEMONSTRAÇÃO EM MEMÓRIA");
            janela.setContentPane(new FluxoMatrizes(cenario.gestao,new DialogosJOptionPane()));
            janela.setSize(860,780);janela.setLocationRelativeTo(null);janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);janela.setVisible(true);
        });
    }
}
