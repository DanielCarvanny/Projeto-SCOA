package br.edu.scoa.academico.aplicacao.boletins;

import br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.avaliacoes.ResultadoFinalAcademico;
import br.edu.scoa.academico.dominio.matriculas.InscricaoTurma;
import br.edu.scoa.academico.dominio.turmas.Turma;

import java.util.List;
import java.util.Optional;

public record DisciplinaBoletimAluno(long matriculaId, InscricaoTurma inscricao, Turma turma,
                                     String periodo, ItemCatalogo disciplina,
                                     List<RegistroNota> lancamentos,
                                     Optional<ResultadoFinalAcademico> resultadoFinal,
                                     boolean possuiExamesFinaisMultiplos,
                                     Optional<ComponentePlanoAluno> componentePlano) {
    public DisciplinaBoletimAluno {
        periodo = periodo == null ? "" : periodo.strip();
        if (matriculaId <= 0 || inscricao == null || turma == null || disciplina == null
                || inscricao.matriculaId() != matriculaId || inscricao.turmaId() != turma.id()
                || turma.disciplinaId() != disciplina.id() || lancamentos == null || resultadoFinal == null
                || componentePlano == null || componentePlano.filter(componente -> componente.disciplina().id() != disciplina.id()).isPresent()
                || (possuiExamesFinaisMultiplos && resultadoFinal.isPresent())) {
            throw new FalhaBoletimAluno(FalhaBoletimAluno.Codigo.DADOS_ACADEMICOS_INCONSISTENTES);
        }
        lancamentos = List.copyOf(lancamentos);
    }
}
