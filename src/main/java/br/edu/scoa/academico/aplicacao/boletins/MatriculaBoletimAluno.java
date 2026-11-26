package br.edu.scoa.academico.aplicacao.boletins;

import br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo;
import br.edu.scoa.academico.dominio.curriculo.MatrizCurricular;
import br.edu.scoa.academico.dominio.matriculas.MatriculaCurso;

import java.util.List;

public record MatriculaBoletimAluno(MatriculaCurso matricula, ItemCatalogo curso, MatrizCurricular matriz,
                                    List<ComponentePlanoAluno> componentesPlano,
                                    List<DisciplinaBoletimAluno> historicoDisciplinas) {
    public MatriculaBoletimAluno {
        if (matricula == null || curso == null || matriz == null || componentesPlano == null || historicoDisciplinas == null
                || matricula.cursoId() != curso.id() || matricula.matrizId() != matriz.id()
                || matriz.cursoId() != curso.id() || componentesPlano.size() != matriz.componentes().size()) {
            throw new FalhaBoletimAluno(FalhaBoletimAluno.Codigo.DADOS_ACADEMICOS_INCONSISTENTES);
        }
        componentesPlano = List.copyOf(componentesPlano);
        historicoDisciplinas = List.copyOf(historicoDisciplinas);
    }
}
