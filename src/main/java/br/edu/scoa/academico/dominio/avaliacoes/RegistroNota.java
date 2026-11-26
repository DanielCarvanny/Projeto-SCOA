package br.edu.scoa.academico.dominio.avaliacoes;

import java.util.Optional;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.ENTRADA_INVALIDA;

public record RegistroNota(AvaliacaoAcademica avaliacao, long inscricaoId, long alunoId,
                           String nomeAluno, Optional<Nota> nota, long revisaoNota,
                           String disciplina, String turma, String periodo) {
    public RegistroNota {
        nomeAluno = nomeAluno == null ? "" : nomeAluno.strip();
        disciplina = disciplina == null ? "" : disciplina.strip();
        turma = turma == null ? "" : turma.strip();
        periodo = periodo == null ? "" : periodo.strip();
        if (avaliacao == null || inscricaoId <= 0 || alunoId <= 0 || nomeAluno.isBlank()
                || nota == null || revisaoNota < 0 || (nota.isEmpty() && revisaoNota != 0)) {
            throw new FalhaAvaliacao(ENTRADA_INVALIDA);
        }
    }
}
