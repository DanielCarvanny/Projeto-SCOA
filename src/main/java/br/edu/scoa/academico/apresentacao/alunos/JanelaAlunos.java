package br.edu.scoa.academico.apresentacao.alunos;

import javax.swing.*;

public final class JanelaAlunos {
    private JanelaAlunos() { }
    public static void abrir(ControladorAlunos controlador, String contexto) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> abrir(controlador, contexto));
            return;
        }
        JFrame janela = new JFrame("SCOA — Acadêmico");
        janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        janela.setContentPane(new PainelAlunos(controlador, new DialogosJOptionPane(), contexto));
        janela.pack();
        janela.setMinimumSize(janela.getSize());
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }
}
