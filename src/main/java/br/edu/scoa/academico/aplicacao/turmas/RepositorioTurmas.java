package br.edu.scoa.academico.aplicacao.turmas;

import br.edu.scoa.academico.dominio.turmas.*;
import java.time.Instant;
import java.util.*;

public interface RepositorioTurmas {
    Sala salvarSala(Sala sala, UUID operacao, long autor, Instant instante);
    Optional<Sala> consultarSala(long id);
    List<Sala> pesquisarSalas(String termo);
    Turma salvarTurma(Turma turma, UUID operacao, long autor, Instant instante);
    Turma transicionarTurma(long id, long revisao, SituacaoTurma destino, UUID operacao, long autor, Instant instante);
    Turma alocarSala(long turmaId, long horarioId, long revisaoTurma, long revisaoHorario, Long salaId, UUID operacao, long autor, Instant instante);
    Optional<Turma> consultarTurma(long id);
    List<Turma> pesquisarTurmas(long periodoId);
}
