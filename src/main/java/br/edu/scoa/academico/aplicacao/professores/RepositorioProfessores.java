package br.edu.scoa.academico.aplicacao.professores;
import br.edu.scoa.academico.dominio.professores.*;
import java.util.Optional;
public interface RepositorioProfessores {
    Professor cadastrar(long pessoaId, String titulacao, SituacaoFuncional situacao, long usuarioId);
    Optional<Professor> consultar(long professorId);
    Optional<Professor> consultarPorPessoa(long pessoaId);
    Professor atualizar(Professor esperado, String titulacao, SituacaoFuncional situacao, long usuarioId);
}
