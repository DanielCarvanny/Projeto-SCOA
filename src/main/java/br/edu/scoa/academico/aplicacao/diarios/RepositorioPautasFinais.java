package br.edu.scoa.academico.aplicacao.diarios;

import br.edu.scoa.academico.dominio.diarios.EventoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.PautaFinal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RepositorioPautasFinais {
    PautaFinal consultar(long turmaId);
    List<EventoPautaFinal> historico(long turmaId);
    long versaoDiario(long turmaId);
    PautaFinal registrarAnalise(long turmaId, long revisaoEsperada, String observacao,
                                long autorId, Instant instante, UUID operacaoId);
    PautaFinal homologar(long turmaId, long revisaoEsperada, long versaoDiarioEsperada,
                         long autorId, Instant instante, UUID operacaoId);
    PautaFinal reabrir(long turmaId, long revisaoEsperada, String motivo,
                       long autorId, Instant instante, UUID operacaoId);
}
