package br.edu.scoa.academico.aplicacao.alunos;
import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;

public final class CadastrarAluno {
    private final IdentidadeInstitucional identidade;
    private final RepositorioAlunos repositorio;
    public CadastrarAluno(IdentidadeInstitucional identidade, RepositorioAlunos repositorio) {
        this.identidade = identidade;
        this.repositorio = repositorio;
    }
    public Aluno executar(long usuarioId, long pessoaId, SituacaoCadastral situacao) {
        if (usuarioId <= 0 || pessoaId <= 0 || situacao == null) {
            throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
        }
        if (!identidade.podeCadastrarAluno(usuarioId)) {
            throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
        }
        if (!identidade.pessoaExiste(pessoaId)) {
            throw new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA);
        }
        // A unicidade é garantida na escrita, sem consulta prévia vulnerável à concorrência.
        return repositorio.cadastrar(pessoaId, situacao, usuarioId);
    }
}
