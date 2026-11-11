package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import java.util.*;

/** Fundação fictícia somente para testes e demonstração em memória. */
final class CenarioCpf implements IdentidadeInstitucional, PessoasPorCpf {
    final CenarioAlunos alunos = new CenarioAlunos();
    final Map<String, PessoaParaCadastro> pessoas = new HashMap<>();
    long proximoId = 44;
    CenarioCpf() {
        pessoas.put("52998224725", new PessoaParaCadastro(42, "52998224725", "Pessoa Exemplo A", "pessoa.a@example.invalid", ""));
        pessoas.put("11144477735", new PessoaParaCadastro(43, "11144477735", "Pessoa Exemplo B", "pessoa.b@example.invalid", ""));
    }
    public boolean podeCadastrarAluno(long usuario) { return alunos.podeCadastrarAluno(usuario); }
    public boolean pessoaExiste(long id) { return pessoas.values().stream().anyMatch(pessoa -> pessoa.pessoaId() == id); }
    public Optional<PessoaParaCadastro> consultar(long usuario, String cpf) {
        autorizar(usuario); return Optional.ofNullable(pessoas.get(cpf));
    }
    public synchronized PessoaParaCadastro cadastrarOuObter(long usuario, String cpf, String nome, String email, String telefone) {
        autorizar(usuario);
        return pessoas.computeIfAbsent(cpf, chave -> new PessoaParaCadastro(proximoId++, cpf, nome, email, telefone));
    }
    private void autorizar(long usuario) {
        if (!podeCadastrarAluno(usuario)) throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
    }
    CadastroAlunoPorCpf cadastro() { return new CadastroAlunoPorCpf(7, this, this, new CadastrarAluno(this, alunos)); }
    ControladorAlunos gestao() {
        return new ControladorAlunos(7, new CadastrarAluno(this, alunos), new ConsultarAluno(alunos, alunos), new AtualizarAluno(alunos, alunos));
    }
}
