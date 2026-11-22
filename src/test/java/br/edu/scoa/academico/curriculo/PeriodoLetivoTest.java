package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PeriodoLetivoTest {
    public static PeriodoLetivo novo(){return new PeriodoLetivo(0,"2027 — primeiro semestre",LocalDate.of(2027,2,1),LocalDate.of(2027,6,30),
        Instant.parse("2027-01-01T12:00:00Z"),Instant.parse("2027-01-31T21:00:00Z"),
        Instant.parse("2027-01-15T12:00:00Z"),Instant.parse("2027-02-10T21:00:00Z"),SituacaoPeriodoLetivo.PLANEJADO,0);}
    @Test void janelasInclusivasExigemSituacaoAberta(){
        var p=novo();assertFalse(p.matriculaEstaAberta(p.inicioMatricula()));
        p=p.identificado(1,1).abrir();
        assertTrue(p.matriculaEstaAberta(p.inicioMatricula()));assertTrue(p.matriculaEstaAberta(p.fimMatricula()));
        assertFalse(p.matriculaEstaAberta(p.inicioMatricula().minusNanos(1)));assertFalse(p.matriculaEstaAberta(p.fimMatricula().plusNanos(1)));
        assertTrue(p.inscricaoEstaAberta(p.inicioInscricao()));assertTrue(p.inscricaoEstaAberta(p.fimInscricao()));
        assertFalse(p.inscricaoEstaAberta(p.inicioInscricao().minusNanos(1)));assertFalse(p.inscricaoEstaAberta(p.fimInscricao().plusNanos(1)));
        assertFalse(p.encerrar().matriculaEstaAberta(p.inicioMatricula()));assertFalse(p.inscricaoEstaAberta(null));
    }
    @Test void transicoesInvalidasSaoRecusadas(){
        var p=novo().identificado(1,1);assertThrows(FalhaPeriodo.class,p::encerrar);
        var aberto=p.abrir();assertThrows(FalhaPeriodo.class,aberto::abrir);
        var encerrado=aberto.encerrar();assertThrows(FalhaPeriodo.class,encerrado::abrir);assertThrows(FalhaPeriodo.class,encerrado::exigirEditavel);
    }
    @Test void intervalosObrigatoriosEOrdenados(){
        var p=novo();
        assertEquals(FalhaPeriodo.Codigo.CALENDARIO_INVALIDO,assertThrows(FalhaPeriodo.class,()->new PeriodoLetivo(0,p.descricao(),p.dataFim(),p.dataInicio(),p.inicioMatricula(),p.fimMatricula(),p.inicioInscricao(),p.fimInscricao(),p.situacao(),0)).codigo());
        assertEquals(FalhaPeriodo.Codigo.MATRICULA_INVALIDA,assertThrows(FalhaPeriodo.class,()->new PeriodoLetivo(0,p.descricao(),p.dataInicio(),p.dataFim(),p.fimMatricula(),p.inicioMatricula(),p.inicioInscricao(),p.fimInscricao(),p.situacao(),0)).codigo());
        assertEquals(FalhaPeriodo.Codigo.INSCRICAO_INVALIDA,assertThrows(FalhaPeriodo.class,()->new PeriodoLetivo(0,p.descricao(),p.dataInicio(),p.dataFim(),p.inicioMatricula(),p.fimMatricula(),null,p.fimInscricao(),p.situacao(),0)).codigo());
        assertThrows(FalhaPeriodo.class,()->new PeriodoLetivo(0," ",p.dataInicio(),p.dataFim(),p.inicioMatricula(),p.fimMatricula(),p.inicioInscricao(),p.fimInscricao(),p.situacao(),0));
    }
    @Test void aceitaMesmoInstanteEJanelasAntesDasAulas(){
        var p=novo();var igual=new PeriodoLetivo(0,p.descricao(),p.dataInicio(),p.dataInicio(),p.inicioMatricula(),p.inicioMatricula(),p.inicioInscricao(),p.inicioInscricao(),p.situacao(),0);
        assertTrue(igual.identificado(1,1).abrir().matriculaEstaAberta(p.inicioMatricula()));
    }
}
