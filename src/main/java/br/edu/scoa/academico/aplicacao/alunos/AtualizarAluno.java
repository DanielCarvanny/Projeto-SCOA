package br.edu.scoa.academico.aplicacao.alunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;

/** Atualiza somente a situação acadêmica cadastral; não altera pessoa nem matrícula. */
public final class AtualizarAluno {
    private final AcessoAlunos acesso;
    private final RepositorioGestaoAlunos repositorio;
    public AtualizarAluno(AcessoAlunos acesso, RepositorioGestaoAlunos repositorio) {
        this.acesso = acesso; this.repositorio = repositorio;
    }
    public Aluno executar(long usuarioId, long alunoId, SituacaoCadastral situacao) {
        if (usuarioId <= 0 || alunoId <= 0 || situacao == null) {
            throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
        }
        if (!acesso.podeAtualizarAluno(usuarioId, alunoId)) {
            throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
        }
        return repositorio.atualizarSituacao(alunoId, situacao, usuarioId);
    }
}
