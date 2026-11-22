package br.edu.scoa.academico.dominio.curriculo;

import java.time.LocalDate;
import java.util.*;
import static br.edu.scoa.academico.dominio.curriculo.FalhaMatriz.Codigo.*;

public record MatrizCurricular(long id, long cursoId, String codigo, String versao,
        LocalDate inicioVigencia, LocalDate fimVigencia, SituacaoMatriz situacao, long revisao,
        List<ComponenteCurricular> componentes, List<PreRequisito> preRequisitos, Long matrizOrigemId) {
    public MatrizCurricular(long id,long cursoId,String codigo,String versao,LocalDate inicioVigencia,
            LocalDate fimVigencia,SituacaoMatriz situacao,long revisao,List<ComponenteCurricular> componentes,List<PreRequisito> preRequisitos){
        this(id,cursoId,codigo,versao,inicioVigencia,fimVigencia,situacao,revisao,componentes,preRequisitos,null);
    }
    public MatrizCurricular(long id,long cursoId,String codigo,String versao,LocalDate inicioVigencia,
            LocalDate fimVigencia,SituacaoMatriz situacao,long revisao,List<ComponenteCurricular> componentes){
        this(id,cursoId,codigo,versao,inicioVigencia,fimVigencia,situacao,revisao,componentes,List.of());
    }
    public MatrizCurricular {
        codigo=codigo==null?"":codigo.strip().toUpperCase(Locale.ROOT);
        versao=versao==null?"":versao.strip();
        if(id<0 || revisao<0 || (id==0)!=(revisao==0) || situacao==null)
            throw new FalhaMatriz(IDENTIFICACAO_INVALIDA);
        validarCabecalho(cursoId,codigo,versao,inicioVigencia,fimVigencia);
        if(componentes==null || componentes.isEmpty()) throw new FalhaMatriz(MATRIZ_VAZIA);
        var ids=new HashSet<Long>();
        for(var componente:componentes) {
            if(componente==null) throw new FalhaMatriz(COMPONENTE_INVALIDO);
            if(!ids.add(componente.disciplinaId())) throw new FalhaMatriz(DISCIPLINA_REPETIDA);
        }
        componentes=List.copyOf(componentes);
        preRequisitos=List.copyOf(preRequisitos);
        ValidadorPreRequisitos.validar(ids,preRequisitos);
    }
    public static void validarCabecalho(long cursoId,String codigo,String versao,LocalDate inicio,LocalDate fim){
        codigo=codigo==null?"":codigo.strip().toUpperCase(Locale.ROOT);
        versao=versao==null?"":versao.strip();
        if(cursoId<=0 || codigo.isBlank() || codigo.codePointCount(0,codigo.length())>40
                || versao.isBlank() || versao.codePointCount(0,versao.length())>255)
            throw new FalhaMatriz(IDENTIFICACAO_INVALIDA);
        // Limites técnicos de DATE no MySQL; não impõe regra institucional de calendário.
        if(inicio==null || inicio.getYear()<1000 || inicio.getYear()>9999
                || (fim!=null && (fim.isBefore(inicio) || fim.getYear()>9999)))
            throw new FalhaMatriz(DATAS_INVALIDAS);
    }
    public void exigirEditavel() {
        if(situacao!=SituacaoMatriz.EM_ELABORACAO) throw new FalhaMatriz(MATRIZ_NAO_EDITAVEL);
    }
    public MatrizCurricular remover(long disciplinaId) {
        exigirEditavel();
        if(preRequisitos.stream().anyMatch(p->p.disciplinaId()==disciplinaId||p.disciplinaExigidaId()==disciplinaId))
            throw new FalhaPreRequisito(FalhaPreRequisito.Codigo.COMPONENTE_UTILIZADO);
        return new MatrizCurricular(id,cursoId,codigo,versao,inicioVigencia,fimVigencia,situacao,revisao,
                componentes.stream().filter(c->c.disciplinaId()!=disciplinaId).toList(),preRequisitos,matrizOrigemId);
    }
    public long cargaObrigatoria() {
        return componentes.stream().filter(c->c.tipo()==TipoComponente.OBRIGATORIO)
            .mapToLong(ComponenteCurricular::cargaHorariaConsiderada).sum();
    }
    public MatrizCurricular identificada(long novoId,long novaRevisao) {
        return new MatrizCurricular(novoId,cursoId,codigo,versao,inicioVigencia,fimVigencia,situacao,novaRevisao,componentes,preRequisitos,matrizOrigemId);
    }
    public MatrizCurricular comPreRequisitos(List<PreRequisito> relacoes){
        exigirEditavel();
        return new MatrizCurricular(id,cursoId,codigo,versao,inicioVigencia,fimVigencia,situacao,revisao,componentes,relacoes,matrizOrigemId);
    }
    public MatrizCurricular publicar(){
        exigirEditavel();
        if(id<=0)throw new FalhaMatriz(NAO_ENCONTRADA);
        return new MatrizCurricular(id,cursoId,codigo,versao,inicioVigencia,fimVigencia,SituacaoMatriz.VIGENTE,revisao+1,componentes,preRequisitos,matrizOrigemId);
    }
    /** Situação publicada e intervalo inclusivo; publicar não antecipa a data inicial. */
    public boolean estaVigente(LocalDate data){
        return data!=null && situacao==SituacaoMatriz.VIGENTE && !data.isBefore(inicioVigencia)
            && (fimVigencia==null||!data.isAfter(fimVigencia));
    }
    public MatrizCurricular novaVersao(String codigoNovo,String versaoNova,LocalDate inicio,LocalDate fim){
        if(situacao!=SituacaoMatriz.VIGENTE||id<=0)throw new FalhaMatriz(MATRIZ_NAO_EDITAVEL);
        var copia=new MatrizCurricular(0,cursoId,codigoNovo,versaoNova,inicio,fim,SituacaoMatriz.EM_ELABORACAO,0,componentes,preRequisitos,id);
        if(codigo.equals(copia.codigo())&&versao.equals(copia.versao()))throw new FalhaMatriz(MATRIZ_DUPLICADA);
        return copia;
    }
    @Override public String toString() { return codigo+" · versão "+versao+" · "+situacao; }
}

