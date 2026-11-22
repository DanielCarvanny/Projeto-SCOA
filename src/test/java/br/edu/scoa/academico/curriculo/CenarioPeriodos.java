package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.*;
import java.util.*;
public final class CenarioPeriodos implements RepositorioPeriodos {
    public final CenarioCatalogo identidade=new CenarioCatalogo();
    public final GestaoPeriodos gestao=new GestaoPeriodos(identidade.identidade,identidade.sessao,this,identidade.relogio);
    private final Map<Long,PeriodoLetivo> periodos=new LinkedHashMap<>();
    private final Map<UUID,PeriodoLetivo> operacoes=new HashMap<>();
    public int gravacoes;public boolean acessouNaEdt;
    public synchronized PeriodoLetivo salvar(PeriodoLetivo p,UUID operacao,long autor,Instant instante){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();p.exigirEditavel();
        if(p.id()==0&&operacoes.containsKey(operacao))return operacoes.get(operacao);
        if(p.id()!=0){var atual=consultar(p.id());if(atual.revisao()!=p.revisao())throw new FalhaPeriodo(FalhaPeriodo.Codigo.CONFLITO_EDICAO);}
        var salvo=p.identificado(p.id()==0?periodos.size()+1:p.id(),p.revisao()+1);periodos.put(salvo.id(),salvo);operacoes.put(operacao,salvo);gravacoes++;return salvo;
    }
    public synchronized PeriodoLetivo transicionar(long id,long revisao,SituacaoPeriodoLetivo destino,long autor,Instant instante){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();var p=consultar(id);
        if(p.revisao()!=revisao)throw new FalhaPeriodo(FalhaPeriodo.Codigo.CONFLITO_EDICAO);
        var salvo=(destino==SituacaoPeriodoLetivo.ABERTO?p.abrir():p.encerrar()).identificado(id,revisao+1);periodos.put(id,salvo);gravacoes++;return salvo;
    }
    public synchronized PeriodoLetivo consultar(long id){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();var p=periodos.get(id);if(p==null)throw new FalhaPeriodo(FalhaPeriodo.Codigo.NAO_ENCONTRADO);return p;
    }
    public synchronized List<PeriodoLetivo> pesquisar(String descricao,SituacaoPeriodoLetivo situacao){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();return periodos.values().stream().filter(p->p.descricao().contains(descricao)&&(situacao==null||p.situacao()==situacao)).toList();
    }
}
