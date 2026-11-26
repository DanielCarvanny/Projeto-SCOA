package br.edu.scoa.academico.aplicacao.boletins;

import java.util.List;

public record ResumoBoletimAluno(List<MatriculaBoletimAluno> matriculas, List<HorarioSemanalAluno> horarios) {
    public ResumoBoletimAluno {
        if (matriculas == null || horarios == null) {
            throw new FalhaBoletimAluno(FalhaBoletimAluno.Codigo.DADOS_ACADEMICOS_INCONSISTENTES);
        }
        matriculas = List.copyOf(matriculas);
        horarios = List.copyOf(horarios);
    }
}
