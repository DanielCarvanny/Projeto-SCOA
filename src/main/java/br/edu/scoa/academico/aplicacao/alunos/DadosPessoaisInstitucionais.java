package br.edu.scoa.academico.aplicacao.alunos;

import java.util.Optional;

/** Porta consumidora proposta à Fundação (B/D), sem acesso às suas tabelas.
 * O provedor autoriza por usuário/pessoa, audita e atualiza atomicamente.
 * Deve rejeitar edição se os dados atuais diferirem de esperada (concorrência).
 * CPF e identidade permanecem imutáveis nesta operação.
 */
public interface DadosPessoaisInstitucionais {
    Optional<PessoaParaCadastro> consultarPorId(long usuarioId, long pessoaId);
    PessoaParaCadastro atualizar(long usuarioId, PessoaParaCadastro esperada, String nome, String email, String telefone);
}
