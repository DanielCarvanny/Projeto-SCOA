package br.edu.scoa.academico.dominio.diarios;

import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.turmas.Turma;

import java.util.List;

public record ResumoAuditoriaDiario(Turma turma, PautaFinal pauta, List<PendenciaPauta> pendencias,
                                    long versaoDiarioAtual, List<RegistroNota> diario, List<EventoAuditoriaDiario> historicoDados,
                                    List<EventoPautaFinal> historicoPauta) {
    public ResumoAuditoriaDiario {
        if (turma == null || pauta == null || turma.id() != pauta.turmaId()
                || versaoDiarioAtual < 0 || pendencias == null || diario == null || historicoDados == null || historicoPauta == null) {
            throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.ENTRADA_INVALIDA);
        }
        pendencias = List.copyOf(pendencias);
        diario = List.copyOf(diario);
        historicoDados = List.copyOf(historicoDados);
        historicoPauta = List.copyOf(historicoPauta);
    }
}
