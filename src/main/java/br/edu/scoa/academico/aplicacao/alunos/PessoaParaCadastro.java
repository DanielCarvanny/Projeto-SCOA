package br.edu.scoa.academico.aplicacao.alunos;

/** DTO de leitura da identidade; não é uma segunda entidade Pessoa. */
public record PessoaParaCadastro(long pessoaId, String cpf, String nome, String email, String telefone) {
    @Override public String toString() { return "PessoaParaCadastro[pessoaId=" + pessoaId + "]"; }
}
