package br.edu.scoa.academico.aplicacao.alunos;

import java.util.Optional;

/** Porta consumidora proposta, pendente de acordo B/D. Não implementa a Fundação.
 * O provedor verifica permissão/âmbito antes de expor dados ou escrever.
 * CPF único; cadastrarOuObter reutiliza identidade em repetição/concorrência,
 * sem sobrescrever dados existentes. Falha de consulta lança erro, nunca Optional.empty().
 */
public interface PessoasPorCpf {
    Optional<PessoaParaCadastro> consultar(long usuarioId, String cpf);
    PessoaParaCadastro cadastrarOuObter(long usuarioId, String cpf, String nome, String email, String telefone);
}
