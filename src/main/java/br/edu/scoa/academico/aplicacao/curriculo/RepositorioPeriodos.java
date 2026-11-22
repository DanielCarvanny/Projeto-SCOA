package br.edu.scoa.academico.aplicacao.curriculo;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.Instant;
import java.util.*;
public interface RepositorioPeriodos {
    PeriodoLetivo salvar(PeriodoLetivo periodo,UUID operacao,long autor,Instant instante);
    PeriodoLetivo transicionar(long id,long revisao,SituacaoPeriodoLetivo destino,long autor,Instant instante);
    PeriodoLetivo consultar(long id);
    List<PeriodoLetivo> pesquisar(String descricao,SituacaoPeriodoLetivo situacao);
}
