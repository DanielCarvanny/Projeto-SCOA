package br.edu.scoa.academico.identidade;

import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.identidade.*;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContaExigeVinculoTest {
    @Test void alunoEProfessorSemVinculoNaoRecebemContaNemTrocaDePerfil() {
        var repo=new ServicoETelasIdentidadeTest.Memoria(){
            public Conta criarConta(long p,String l,String h,Perfil f,long u){return new Conta(9,p,l,f,true,1);}
            public Conta atualizarConta(Conta c,Perfil p,boolean a,long u){return new Conta(c.id(),c.pessoaId(),c.login(),p,a,c.versao()+1);}
        };
        var senhas=new Senhas(){public String gerar(char[] s){return "hash-teste";}public boolean confere(char[] s,String h){return true;}};
        var servico=new ServicoIdentidade(repo,senhas,Clock.systemUTC());
        var sessao=servico.entrar("coord","senha123".toCharArray());
        for(var perfil:new Perfil[]{Perfil.ALUNO,Perfil.PROFESSOR}) {
            assertThrows(FalhaIdentidade.class,()->servico.criarConta(sessao,42,"teste","senha123".toCharArray(),perfil));
            assertThrows(FalhaIdentidade.class,()->servico.atualizarConta(sessao,new Conta(9,42,"teste",Perfil.COORDENADOR,true,1),perfil,true));
        }
        assertEquals(Perfil.COORDENADOR,servico.criarConta(sessao,42,"outro","senha123".toCharArray(),Perfil.COORDENADOR).perfil());
    }
}
