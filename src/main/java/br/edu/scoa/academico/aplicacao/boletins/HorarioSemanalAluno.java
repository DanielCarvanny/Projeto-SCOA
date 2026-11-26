package br.edu.scoa.academico.aplicacao.boletins;

import br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo;
import br.edu.scoa.academico.dominio.curriculo.PeriodoLetivo;
import br.edu.scoa.academico.dominio.turmas.HorarioTurma;
import br.edu.scoa.academico.dominio.turmas.Sala;
import br.edu.scoa.academico.dominio.turmas.Turma;

public record HorarioSemanalAluno(long matriculaId, ItemCatalogo curso, PeriodoLetivo periodo,
                                  ItemCatalogo disciplina, Turma turma, HorarioTurma horario, Sala sala) {
    public HorarioSemanalAluno {
        if (matriculaId <= 0 || curso == null || periodo == null || disciplina == null || turma == null || horario == null
                || curso.tipo() != br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.CURSO
                || disciplina.tipo() != br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.DISCIPLINA
                || turma.disciplinaId() != disciplina.id() || turma.periodoId() != periodo.id()
                || (horario.salaId() == null) != (sala == null)
                || (sala != null && sala.id() != horario.salaId())) {
            throw new FalhaBoletimAluno(FalhaBoletimAluno.Codigo.DADOS_ACADEMICOS_INCONSISTENTES);
        }
    }
}
