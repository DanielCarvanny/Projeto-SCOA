package br.edu.scoa.academico.aplicacao.alunos;

/** Porta consumidora proposta pelo Acadêmico; não implementa a fundação.
 * Indisponibilidade deve propagar erro, nunca retornar autorização implícita.
 */
public interface IdentidadeInstitucional {
    boolean podeCadastrarAluno(long usuarioId);
    boolean pessoaExiste(long pessoaId);
}
