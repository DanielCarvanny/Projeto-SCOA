package br.edu.scoa.academico.apresentacao.alunos;

import java.awt.Component;

public interface DialogosAluno {
    void informar(Component origem, String mensagem);
    void erro(Component origem, String mensagem);
    boolean confirmar(Component origem, String mensagem);
}
