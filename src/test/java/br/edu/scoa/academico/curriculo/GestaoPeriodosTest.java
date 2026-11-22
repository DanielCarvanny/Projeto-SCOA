package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.identidade.*;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GestaoPeriodosTest {
    @Test void sessaoExpiraAntesDaConfirmacao(){
        var agora=new java.util.concurrent.atomic.AtomicReference<>(Instant.parse("2026-11-20T12:00:00Z"));
        var relogio=new Clock(){public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zona){return this;}public Instant instant(){return agora.get();}};
        var conta=new Conta(7,3,"coord",Perfil.COORDENADOR,true,1);
        var repo=(RepositorioIdentidade)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{RepositorioIdentidade.class},
            (objeto,metodo,args)->switch(metodo.getName()){case "autenticar"->conta;case "conta"->java.util.Optional.of(conta);default->throw new UnsupportedOperationException();});
        var identidade=new ServicoIdentidade(repo,null,relogio);var sessao=identidade.entrar("coord",new char[0]);
        var memoria=new CenarioPeriodos();var gestao=new GestaoPeriodos(identidade,sessao,memoria,relogio);
        agora.set(agora.get().plusSeconds(1800));
        assertEquals(FalhaIdentidade.Codigo.SESSAO_INVALIDA,assertThrows(FalhaIdentidade.class,()->gestao.salvar(PeriodoLetivoTest.novo(),UUID.randomUUID())).codigo());assertEquals(0,memoria.gravacoes);
    }
    @Test void coordenadorGerenciaCiclo(){
        var c=new CenarioPeriodos();var p=c.gestao.salvar(PeriodoLetivoTest.novo(),UUID.randomUUID());
        assertEquals(1,c.gestao.pesquisar("primeiro",null).size());
        var aberto=c.gestao.abrir(p);assertEquals(SituacaoPeriodoLetivo.ABERTO,aberto.situacao());
        assertEquals(SituacaoPeriodoLetivo.ENCERRADO,c.gestao.encerrar(aberto).situacao());
    }
    @Test void alunoEProfessorNaoAdministram(){
        for(var perfil:new Perfil[]{Perfil.ALUNO,Perfil.PROFESSOR}){
            var c=new CenarioPeriodos();c.identidade.conta=new Conta(7,3,"coord",perfil,true,1);
            assertThrows(FalhaIdentidade.class,()->c.gestao.salvar(PeriodoLetivoTest.novo(),UUID.randomUUID()));
            assertThrows(FalhaIdentidade.class,()->c.gestao.pesquisar("",null));
            assertThrows(FalhaIdentidade.class,()->c.gestao.consultar(1));
            assertThrows(FalhaIdentidade.class,()->c.gestao.abrir(PeriodoLetivoTest.novo()));
            assertThrows(FalhaIdentidade.class,()->c.gestao.encerrar(PeriodoLetivoTest.novo()));assertEquals(0,c.gravacoes);
        }
    }
    @Test void sessaoEncerradaNaoGrava(){
        var c=new CenarioPeriodos();c.identidade.identidade.sair(c.identidade.sessao);
        assertThrows(FalhaIdentidade.class,()->c.gestao.salvar(PeriodoLetivoTest.novo(),UUID.randomUUID()));assertEquals(0,c.gravacoes);
    }
}
