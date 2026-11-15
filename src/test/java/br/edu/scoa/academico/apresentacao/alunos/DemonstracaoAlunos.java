package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.apresentacao.professores.FluxoProfessores;
import javax.swing.*;

/** Demonstração integrada do Acadêmico; identidade simulada, dados em memória. */
public final class DemonstracaoAlunos {
    public static void main(String[] argumentos) {
        SwingUtilities.invokeLater(()->{
            CenarioCpf alunos=DadosDemonstracaoAlunos.criar();
            CenarioProfessores professores=DadosDemonstracaoProfessores.criar(alunos);
            alunos.cadastrarOuObter(7,"12345678901","Pessoa sem vínculo","nova@example.invalid","");
            JTabbedPane modulos=new JTabbedPane();
            modulos.addTab("Alunos",new FluxoAlunos(alunos.cadastro(),alunos.gestao(),new DialogosJOptionPane(),
                    "MEMÓRIA · Pessoa disponível para novo vínculo: CPF 12345678901 · sem persistência",alunos.ficha()));
            modulos.addTab("Professores",new FluxoProfessores(professores.gestao(),new DialogosJOptionPane(),
                    "MEMÓRIA · Pessoa disponível para novo vínculo: CPF 12345678901 · sem persistência"));
            JFrame janela=new JFrame("SCOA — Acadêmico");janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            janela.setContentPane(modulos);janela.pack();janela.setMinimumSize(janela.getSize());janela.setLocationRelativeTo(null);janela.setVisible(true);
        });
    }
}
