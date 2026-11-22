package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoPeriodos;
import br.edu.scoa.academico.apresentacao.alunos.DialogosJOptionPane;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.*;
import java.util.UUID;
import javax.swing.*;
public final class DemonstracaoPeriodos {
    private DemonstracaoPeriodos(){}
    public static void main(String[] args){
        var c=new CenarioPeriodos();
        for(int i=0;i<3;i++){
            var periodo=new PeriodoLetivo(0,new String[]{"DEMO — Planejado","DEMO — Aberto","DEMO — Encerrado"}[i],LocalDate.of(2027,2,1),LocalDate.of(2027,6,30),
                Instant.parse("2027-01-01T12:00:00Z"),Instant.parse("2027-01-31T21:00:00Z"),Instant.parse("2027-01-15T12:00:00Z"),Instant.parse("2027-02-10T21:00:00Z"),SituacaoPeriodoLetivo.PLANEJADO,0);
            periodo=c.gestao.salvar(periodo,UUID.randomUUID());if(i>0)periodo=c.gestao.abrir(periodo);if(i==2)c.gestao.encerrar(periodo);
        }
        SwingUtilities.invokeLater(()->{var janela=new JFrame("SCOA — Períodos — demonstração em memória");janela.setContentPane(new FluxoPeriodos(c.gestao,new DialogosJOptionPane()));janela.setSize(860,780);janela.setMinimumSize(new java.awt.Dimension(800,720));janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);janela.setLocationRelativeTo(null);janela.setVisible(true);});
    }
}
