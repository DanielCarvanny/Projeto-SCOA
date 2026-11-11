package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.*;
import java.util.*;

/** Apoio exclusivamente de teste/demonstração. Nunca incluído no artefato de produção. */
final class CenarioAlunos implements RepositorioGestaoAlunos, IdentidadeInstitucional, AcessoAlunos {
    final Map<Long, Aluno> alunos = new HashMap<>();
    boolean autorizado = true;
    long autor;
    int escritas;
    Runnable antesDeGravar = () -> {};

    ControladorAlunos controlador() { return controlador(this); }
    ControladorAlunos controlador(RepositorioGestaoAlunos repositorio) {
        return new ControladorAlunos(7, new CadastrarAluno(this, repositorio),
                new ConsultarAluno(this, repositorio), new AtualizarAluno(this, repositorio));
    }
    public boolean podeCadastrarAluno(long usuario) { return autorizado && usuario == 7; }
    public boolean pessoaExiste(long pessoa) { return pessoa == 42 || pessoa == 43; }
    public boolean podeConsultarAluno(long usuario, long aluno) { return autorizado && usuario == 7; }
    public boolean podeAtualizarAluno(long usuario, long aluno) { return autorizado && usuario == 7; }
    public Aluno cadastrar(long pessoa, SituacaoCadastral situacao, long usuario) {
        antesDeGravar.run();
        if (alunos.values().stream().anyMatch(a -> a.pessoaId() == pessoa))
            throw new CadastroAlunoException(ErroCadastroAluno.ALUNO_JA_CADASTRADO);
        var aluno = new Aluno(alunos.size() + 1, pessoa, situacao);
        alunos.put(aluno.id(), aluno); autor = usuario; escritas++;
        return aluno;
    }
    public Optional<Aluno> consultar(long id) { return Optional.ofNullable(alunos.get(id)); }
    public Aluno atualizarSituacao(long id, SituacaoCadastral situacao, long usuario) {
        var anterior = consultar(id).orElseThrow(() -> new CadastroAlunoException(ErroCadastroAluno.ALUNO_NAO_ENCONTRADO));
        var aluno = new Aluno(id, anterior.pessoaId(), situacao);
        alunos.put(id, aluno); autor = usuario; escritas++;
        return aluno;
    }
}
