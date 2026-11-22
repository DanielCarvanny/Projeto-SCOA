package br.edu.scoa.academico.dominio.curriculo;
import java.time.*;
import java.nio.charset.StandardCharsets;
import static br.edu.scoa.academico.dominio.curriculo.FalhaPeriodo.Codigo.*;

public record PeriodoLetivo(long id,String descricao,LocalDate dataInicio,LocalDate dataFim,
        Instant inicioMatricula,Instant fimMatricula,Instant inicioInscricao,Instant fimInscricao,
        SituacaoPeriodoLetivo situacao,long revisao){
    public PeriodoLetivo {
        descricao=descricao==null?"":descricao.strip();
        validarCalendario(descricao,dataInicio,dataFim);
        validarMatricula(inicioMatricula,fimMatricula);
        validarInscricao(inicioInscricao,fimInscricao);
        if(id<0||revisao<0||(id==0)!=(revisao==0)||situacao==null)throw new FalhaPeriodo(CONFLITO_EDICAO);
    }
    public static void validarCalendario(String descricao,LocalDate dataInicio,LocalDate dataFim){
        descricao=descricao==null?"":descricao.strip();
        if(descricao.isBlank()||descricao.getBytes(StandardCharsets.UTF_8).length>65535)throw new FalhaPeriodo(DESCRICAO_INVALIDA);
        if(!dataValida(dataInicio)||!dataValida(dataFim)||dataFim.isBefore(dataInicio))throw new FalhaPeriodo(CALENDARIO_INVALIDO);
    }
    public static void validarMatricula(Instant inicioMatricula,Instant fimMatricula){
        if(!intervaloValido(inicioMatricula,fimMatricula))throw new FalhaPeriodo(MATRICULA_INVALIDA);
    }
    public static void validarInscricao(Instant inicioInscricao,Instant fimInscricao){
        if(!intervaloValido(inicioInscricao,fimInscricao))throw new FalhaPeriodo(INSCRICAO_INVALIDA);
    }
    private static boolean dataValida(LocalDate data){return data!=null&&data.getYear()>=1000&&data.getYear()<=9999;}
    private static boolean instanteValido(Instant instante){return instante!=null&&instante.compareTo(Instant.parse("1000-01-01T00:00:00Z"))>=0
        &&instante.compareTo(Instant.parse("9999-12-31T23:59:59.999999Z"))<=0&&instante.getNano()%1000==0;}
    private static boolean intervaloValido(Instant inicio,Instant fim){return instanteValido(inicio)&&instanteValido(fim)&&!fim.isBefore(inicio);}
    public boolean matriculaEstaAberta(Instant data){return estaAberta(data,inicioMatricula,fimMatricula);}
    public boolean inscricaoEstaAberta(Instant data){return estaAberta(data,inicioInscricao,fimInscricao);}
    private boolean estaAberta(Instant data,Instant inicio,Instant fim){return situacao==SituacaoPeriodoLetivo.ABERTO&&data!=null&&!data.isBefore(inicio)&&!data.isAfter(fim);}
    public void exigirEditavel(){if(situacao==SituacaoPeriodoLetivo.ENCERRADO)throw new FalhaPeriodo(PERIODO_ENCERRADO);}
    public PeriodoLetivo abrir(){
        if(id==0||situacao!=SituacaoPeriodoLetivo.PLANEJADO)throw new FalhaPeriodo(TRANSICAO_INVALIDA);
        return comSituacao(SituacaoPeriodoLetivo.ABERTO);
    }
    public PeriodoLetivo encerrar(){
        if(id==0||situacao!=SituacaoPeriodoLetivo.ABERTO)throw new FalhaPeriodo(TRANSICAO_INVALIDA);
        return comSituacao(SituacaoPeriodoLetivo.ENCERRADO);
    }
    private PeriodoLetivo comSituacao(SituacaoPeriodoLetivo nova){return new PeriodoLetivo(id,descricao,dataInicio,dataFim,inicioMatricula,fimMatricula,inicioInscricao,fimInscricao,nova,revisao);}
    public PeriodoLetivo identificado(long novoId,long novaRevisao){return new PeriodoLetivo(novoId,descricao,dataInicio,dataFim,inicioMatricula,fimMatricula,inicioInscricao,fimInscricao,situacao,novaRevisao);}
    @Override public String toString(){return descricao+" · "+situacao+" · "+dataInicio+" a "+dataFim;}
}
