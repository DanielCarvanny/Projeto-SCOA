package br.edu.scoa.academico.aplicacao.avaliacoes;

public interface AutorizacaoAvaliacoes<S> {
    long usuarioId(S sessao);
    boolean ehCoordenador(S sessao);
    boolean ehProfessor(S sessao);
    boolean ehAluno(S sessao);
    long professorId(S sessao);
    long alunoId(S sessao);
}
