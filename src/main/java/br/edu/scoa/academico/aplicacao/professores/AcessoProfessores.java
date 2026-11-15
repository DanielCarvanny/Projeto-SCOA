package br.edu.scoa.academico.aplicacao.professores;
/** Porta consumidora local. A Fundação deverá validar autorização e âmbito. */
public interface AcessoProfessores {
    boolean podeCadastrar(long usuarioId);
    boolean podeConsultar(long usuarioId, long professorId);
    boolean podeAtualizar(long usuarioId, long professorId);
    default boolean podeAtualizarContato(long usuarioId,long professorId) { return podeAtualizar(usuarioId,professorId); }
}
