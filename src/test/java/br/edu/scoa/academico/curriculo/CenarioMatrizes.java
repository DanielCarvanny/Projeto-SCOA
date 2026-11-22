package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.Instant;
import java.util.*;
public final class CenarioMatrizes implements RepositorioMatrizes {
    public final CenarioCatalogo catalogo=new CenarioCatalogo();
    public final GestaoMatrizes gestao=new GestaoMatrizes(catalogo.identidade,catalogo.sessao,this,catalogo,catalogo.relogio);
    public final Map<Long,MatrizCurricular> matrizes=new LinkedHashMap<>();
    public final ItemCatalogo curso=catalogo.gestao.salvar(TipoCatalogo.CURSO,null,"ADS-DEMO","Sistemas — demonstração","",2400);
    public final ItemCatalogo disciplina=catalogo.gestao.salvar(TipoCatalogo.DISCIPLINA,null,"POO-DEMO","Objetos","Classes e objetos",80);
    public int gravacoes;
    public boolean acessouNaEdt;
    public synchronized MatrizCurricular salvar(MatrizCurricular matriz,long autor,Instant instante){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();matriz.exigirEditavel();
        var atual=matrizes.get(matriz.id());
        if(matriz.id()!=0){
            if(atual==null)throw new FalhaMatriz(FalhaMatriz.Codigo.NAO_ENCONTRADA);
            atual.exigirEditavel();
            if(atual.revisao()!=matriz.revisao())throw new FalhaMatriz(FalhaMatriz.Codigo.CONFLITO_EDICAO);
            var ids=new HashSet<Long>();for(var componente:matriz.componentes())ids.add(componente.disciplinaId());
            if(atual.preRequisitos().stream().anyMatch(p->!ids.contains(p.disciplinaId())||!ids.contains(p.disciplinaExigidaId())))
                throw new FalhaPreRequisito(FalhaPreRequisito.Codigo.COMPONENTE_UTILIZADO);
        }
        if(matrizes.values().stream().anyMatch(m->m.id()!=matriz.id()&&m.cursoId()==matriz.cursoId()&&m.codigo().equals(matriz.codigo())&&m.versao().equals(matriz.versao())))
            throw new FalhaMatriz(FalhaMatriz.Codigo.MATRIZ_DUPLICADA);
        var salva=matriz.identificada(matriz.id()==0?matrizes.size()+1:matriz.id(),matriz.revisao()+1);
        matrizes.put(salva.id(),salva);gravacoes++;return salva;
    }
    public synchronized List<MatrizCurricular> pesquisar(long cursoId,String termo){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();
        return matrizes.values().stream().filter(m->(cursoId==0||m.cursoId()==cursoId)&&(m.codigo().contains(termo)||m.versao().contains(termo))).toList();
    }
    public synchronized MatrizCurricular consultar(long id){
        var matriz=matrizes.get(id);if(matriz==null)throw new FalhaMatriz(FalhaMatriz.Codigo.NAO_ENCONTRADA);return matriz;
    }
    public synchronized MatrizCurricular publicar(long id,long revisao,long autor,Instant instante){
        acessouNaEdt|=javax.swing.SwingUtilities.isEventDispatchThread();
        var atual=consultar(id);if(atual.revisao()!=revisao)throw new FalhaMatriz(FalhaMatriz.Codigo.CONFLITO_EDICAO);
        var publicada=atual.publicar();matrizes.put(id,publicada);gravacoes++;return publicada;
    }
    public synchronized MatrizCurricular copiar(long id,long revisao,String codigo,String versao,java.time.LocalDate inicio,java.time.LocalDate fim,long autor,Instant instante){
        var atual=consultar(id);if(atual.revisao()!=revisao)throw new FalhaMatriz(FalhaMatriz.Codigo.CONFLITO_EDICAO);
        return salvar(atual.novaVersao(codigo,versao,inicio,fim),autor,instante);
    }
}

