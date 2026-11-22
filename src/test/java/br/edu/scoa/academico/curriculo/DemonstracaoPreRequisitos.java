package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoMatrizes;
import br.edu.scoa.academico.apresentacao.alunos.DialogosJOptionPane;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
public final class DemonstracaoPreRequisitos {
    private DemonstracaoPreRequisitos(){}
    public static void main(String[] args){
        var c=new CenarioMatrizes();
        var p2=c.catalogo.gestao.salvar(TipoCatalogo.DISCIPLINA,null,"P2-DEMO","Programação II","Herança e interfaces",80);
        var p3=c.catalogo.gestao.salvar(TipoCatalogo.DISCIPLINA,null,"P3-DEMO","Programação III","Projetos",80);
        c.gestao.salvar(new MatrizCurricular(0,c.curso.id(),"ADS-PRE","2027.1",LocalDate.of(2027,1,1),null,SituacaoMatriz.EM_ELABORACAO,0,
            List.of(new ComponenteCurricular(c.disciplina.id(),1,TipoComponente.OBRIGATORIO,1,80),
                new ComponenteCurricular(p2.id(),1,TipoComponente.OBRIGATORIO,2,80),new ComponenteCurricular(p3.id(),1,TipoComponente.OBRIGATORIO,3,80)),
            List.of(new PreRequisito(p2.id(),c.disciplina.id()),new PreRequisito(p3.id(),p2.id()))));
        SwingUtilities.invokeLater(()->{
            var janela=new JFrame("SCOA — Pré-requisitos — demonstração em memória");
            janela.setContentPane(new FluxoMatrizes(c.gestao,new DialogosJOptionPane()));janela.setSize(860,780);
            janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);janela.setLocationRelativeTo(null);janela.setVisible(true);
        });
    }
}
