package br.edu.scoa.academico.aplicacao.alunos;

import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import java.util.Optional;

/** Extensão da porta de cadastro, sem obrigar seus consumidores a conhecer novas operações. */
public interface RepositorioGestaoAlunos extends RepositorioAlunos {
    Optional<Aluno> consultar(long alunoId);
    Aluno atualizarSituacao(long alunoId, SituacaoCadastral situacao, long usuarioId);
}
