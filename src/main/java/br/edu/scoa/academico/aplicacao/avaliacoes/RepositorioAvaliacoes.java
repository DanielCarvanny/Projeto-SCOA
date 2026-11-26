package br.edu.scoa.academico.aplicacao.avaliacoes;

import br.edu.scoa.academico.dominio.avaliacoes.AvaliacaoAcademica;
import br.edu.scoa.academico.dominio.avaliacoes.Nota;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.diarios.EventoAuditoriaDiario;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioAvaliacoes {
    AvaliacaoAcademica salvar(AvaliacaoAcademica avaliacao, UUID operacao, long autorId, Instant instante);
    AvaliacaoAcademica atualizar(AvaliacaoAcademica avaliacao, UUID operacao, long autorId, Instant instante);
    Optional<AvaliacaoAcademica> consultar(long avaliacaoId);
    List<AvaliacaoAcademica> porTurma(long turmaId);
    List<RegistroNota> diarioTurma(long turmaId);
    List<EventoAuditoriaDiario> historicoTurma(long turmaId);
    long contarInscricoesAtivas(long turmaId);
    List<RegistroNota> notasAluno(long alunoId);
    RegistroNota lancarNota(long avaliacaoId, long inscricaoId, Nota nota, long revisaoEsperada,
                            UUID operacao, long autorId, Instant instante);
}
