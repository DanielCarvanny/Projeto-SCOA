package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.aplicacao.identidade.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GestaoMatrizesTest {
    static MatrizCurricular nova(CenarioMatrizes c){
        return new MatrizCurricular(0,c.curso.id(),"ADS","2027",LocalDate.of(2027,1,1),null,SituacaoMatriz.EM_ELABORACAO,0,
            List.of(new ComponenteCurricular(c.disciplina.id(),1,TipoComponente.OBRIGATORIO,1,80)));
    }
    @Test void criaConsultaERecuperaConteudoHistorico(){
        var c=new CenarioMatrizes();var salva=c.gestao.salvar(nova(c));
        assertEquals(salva,c.gestao.consultar(salva.id()));
        assertEquals("Classes e objetos",c.gestao.disciplina(c.disciplina.id(),1).ementa());
    }
    @Test void alunoProfessorESessaoEncerradaNaoAdministram(){
        for(var perfil:List.of(Perfil.ALUNO,Perfil.PROFESSOR)){
            var c=new CenarioMatrizes();c.catalogo.conta=new Conta(7,3,"coord",perfil,true,1);
            assertEquals(FalhaIdentidade.Codigo.NAO_AUTORIZADO,assertThrows(FalhaIdentidade.class,()->c.gestao.salvar(nova(c))).codigo());
            assertThrows(FalhaIdentidade.class,()->c.gestao.pesquisar(0,""));assertEquals(0,c.gravacoes);
        }
        var c=new CenarioMatrizes();c.catalogo.identidade.sair(c.catalogo.sessao);
        assertThrows(FalhaIdentidade.class,()->c.gestao.salvar(nova(c)));
    }
    @Test void revisaoInexistenteTemErroEspecifico(){
        var c=new CenarioMatrizes();
        assertEquals(FalhaMatriz.Codigo.REFERENCIA_INEXISTENTE,assertThrows(FalhaMatriz.class,()->c.gestao.disciplina(c.disciplina.id(),99)).codigo());
    }
}

