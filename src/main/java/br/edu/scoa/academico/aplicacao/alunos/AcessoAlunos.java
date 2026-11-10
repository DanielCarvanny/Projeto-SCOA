package br.edu.scoa.academico.aplicacao.alunos;

/** Porta consumidora proposta: permissão inclui o âmbito do aluno solicitado.
 * O adaptador institucional deverá receber o usuário do contexto autenticado.
 */
public interface AcessoAlunos {
    boolean podeConsultarAluno(long usuarioId, long alunoId);
    boolean podeAtualizarAluno(long usuarioId, long alunoId);
}
