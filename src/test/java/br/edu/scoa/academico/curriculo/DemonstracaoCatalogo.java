package br.edu.scoa.academico.curriculo;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.apresentacao.curriculo.FluxoCatalogo;
import br.edu.scoa.academico.apresentacao.alunos.DialogosJOptionPane;
import javax.swing.*;

/** Ambiente de teste: nenhuma escrita no MySQL e nenhuma credencial real. */
public final class DemonstracaoCatalogo {
    private DemonstracaoCatalogo() {}
    public static void main(String[] argumentos) {
        var cenario = new CenarioCatalogo();
        cenario.gestao.salvar(TipoCatalogo.CURSO, null, "ADS-DEMO", "Análise e Desenvolvimento de Sistemas — demonstração", "", 2400);
        var disciplina = cenario.gestao.salvar(TipoCatalogo.DISCIPLINA, null, "POO-DEMO", "Programação Orientada a Objetos", "Classes, objetos e encapsulamento.", 80);
        cenario.gestao.salvar(TipoCatalogo.DISCIPLINA, disciplina, "POO-DEMO", "Programação Orientada a Objetos", "Classes, objetos, encapsulamento e interfaces.", 80);
        SwingUtilities.invokeLater(() -> {
            var abas = new JTabbedPane();
            for (var tipo : TipoCatalogo.values()) abas.addTab(tipo == TipoCatalogo.CURSO ? "Cursos" : "Disciplinas",
                    new FluxoCatalogo(cenario.gestao, tipo, new DialogosJOptionPane()));
            var janela = new JFrame("SCOA — DEMONSTRAÇÃO EM MEMÓRIA — dados descartados ao fechar");
            janela.setContentPane(abas); janela.setSize(860, 780); janela.setLocationRelativeTo(null);
            janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); janela.setVisible(true);
        });
    }
}
