package br.edu.scoa.academico.aplicacao.alunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;

public final class ConsultarAluno {
    private final AcessoAlunos acesso;
    private final RepositorioGestaoAlunos repositorio;
    public ConsultarAluno(AcessoAlunos acesso, RepositorioGestaoAlunos repositorio) {
        this.acesso = acesso; this.repositorio = repositorio;
    }
    public Aluno executar(long usuarioId, long alunoId) {
        throw new UnsupportedOperationException("RED: consulta não implementada");
    }
}
