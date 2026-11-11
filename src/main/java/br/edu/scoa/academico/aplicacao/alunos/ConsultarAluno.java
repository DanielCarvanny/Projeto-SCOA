package br.edu.scoa.academico.aplicacao.alunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;

public final class ConsultarAluno {
    private final AcessoAlunos acesso;
    private final RepositorioGestaoAlunos repositorio;
    public ConsultarAluno(AcessoAlunos acesso, RepositorioGestaoAlunos repositorio) {
        this.acesso = acesso; this.repositorio = repositorio;
    }
    public Aluno executar(long usuarioId, long alunoId) {
        if (usuarioId <= 0 || alunoId <= 0) {
            throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
        }
        if (!acesso.podeConsultarAluno(usuarioId, alunoId)) {
            throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
        }
        return repositorio.consultar(alunoId).orElseThrow(
                () -> new CadastroAlunoException(ErroCadastroAluno.ALUNO_NAO_ENCONTRADO));
    }
}
