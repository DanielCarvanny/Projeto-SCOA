package br.edu.scoa.academico.aplicacao.alunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;

public interface RepositorioAlunos {
    /** Insere atomicamente ou lança ALUNO_JA_CADASTRADO, inclusive sob concorrência.
     * O adaptador MySQL deverá garantir UNIQUE(pessoa_id) e mapear sua violação.
     * usuarioId identifica o autor para auditoria transacional do adaptador.
     */
    Aluno cadastrar(long pessoaId, SituacaoCadastral situacao, long usuarioId);
}
