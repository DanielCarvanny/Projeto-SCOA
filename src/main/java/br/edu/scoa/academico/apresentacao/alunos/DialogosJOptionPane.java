package br.edu.scoa.academico.apresentacao.alunos;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class DialogosJOptionPane implements DialogosAluno {
    public void informar(Component origem, String mensagem) {
        JOptionPane.showMessageDialog(origem, mensagem, "Alunos", JOptionPane.INFORMATION_MESSAGE);
    }
    public void erro(Component origem, String mensagem) {
        JOptionPane.showMessageDialog(origem, mensagem, "Operação não concluída", JOptionPane.ERROR_MESSAGE);
    }
    public boolean confirmar(Component origem, String mensagem) {
        return JOptionPane.showConfirmDialog(origem, mensagem, "Confirmar alteração",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
