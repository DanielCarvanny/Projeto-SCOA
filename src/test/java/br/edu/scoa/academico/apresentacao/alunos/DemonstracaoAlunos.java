package br.edu.scoa.academico.apresentacao.alunos;

import javax.swing.*;

/** Demonstração: CPF e dados sintéticos, identidade simulada, sem MySQL. */
public final class DemonstracaoAlunos {
    public static void main(String[] argumentos) {
        SwingUtilities.invokeLater(() -> {
            CenarioCpf cenario = DadosDemonstracaoAlunos.criar();
            JFrame janela = new JFrame("SCOA — Acadêmico");
            janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            janela.setContentPane(new FluxoAlunos(cenario.cadastro(), cenario.gestao(), new DialogosJOptionPane(),
                    "DEMONSTRAÇÃO EM MEMÓRIA · alunos 1 e 2 pré-cadastrados · dados não persistidos"));
            janela.pack(); janela.setMinimumSize(janela.getSize()); janela.setLocationRelativeTo(null); janela.setVisible(true);
        });
    }
}
