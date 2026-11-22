package br.edu.scoa.academico.dominio.curriculo;
import java.util.*;
import static br.edu.scoa.academico.dominio.curriculo.FalhaPreRequisito.Codigo.*;
public final class ValidadorPreRequisitos {
    private ValidadorPreRequisitos(){}
    public static void validar(Set<Long> disciplinas,List<PreRequisito> relacoes){
        var unicas=new HashSet<PreRequisito>();
        var adjacencias=new HashMap<Long,List<Long>>();
        var graus=new HashMap<Long,Integer>();
        for(long id:disciplinas){adjacencias.put(id,new ArrayList<>());graus.put(id,0);}
        for(var relacao:relacoes){
            if(relacao==null||!disciplinas.contains(relacao.disciplinaId())||!disciplinas.contains(relacao.disciplinaExigidaId()))
                throw new FalhaPreRequisito(FORA_DA_MATRIZ);
            if(!unicas.add(relacao))throw new FalhaPreRequisito(DUPLICADO);
            adjacencias.get(relacao.disciplinaId()).add(relacao.disciplinaExigidaId());
            graus.compute(relacao.disciplinaExigidaId(),(id,grau)->grau+1);
        }
        var fila=new ArrayDeque<Long>();graus.forEach((id,grau)->{if(grau==0)fila.add(id);});
        int visitados=0;
        while(!fila.isEmpty()){
            long id=fila.remove();visitados++;
            for(long proxima:adjacencias.get(id))if(graus.compute(proxima,(chave,grau)->grau-1)==0)fila.add(proxima);
        }
        if(visitados!=disciplinas.size())throw new FalhaPreRequisito(CICLO);
    }
}

