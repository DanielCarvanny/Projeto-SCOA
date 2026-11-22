package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.identidade.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GestaoCatalogoTest {
    @Test void coordenadorCadastraEditaEInativaComAutorEHistorico() {
        var c = new CenarioCatalogo();
        var salvo = c.gestao.salvar(TipoCatalogo.DISCIPLINA, null, " poo ", "Objetos", "Classes", 80);
        var editado = c.gestao.salvar(salvo.tipo(), salvo, "POO", "Objetos", "Classes e interfaces", 100);
        assertFalse(c.gestao.inativar(editado).estaAtivo());
        var historico = c.gestao.historico(salvo);
        assertEquals(3, historico.size()); assertEquals("Classes", historico.getFirst().item().ementa());
        assertEquals(c.sessao.usuarioId(), historico.getFirst().autorId());
    }
    @Test void alunoEProfessorNaoAdministramNemConsultamCatalogo() {
        for (var perfil : new Perfil[]{Perfil.ALUNO, Perfil.PROFESSOR}) {
            var c = new CenarioCatalogo();
            c.conta = new Conta(7, 3, "coord", perfil, true, 1);
            assertEquals(FalhaIdentidade.Codigo.NAO_AUTORIZADO,
                assertThrows(FalhaIdentidade.class, () -> c.gestao.salvar(TipoCatalogo.CURSO, null, "ADS", "Sistemas", "", 2400)).codigo());
            assertThrows(FalhaIdentidade.class, () -> c.gestao.pesquisar(TipoCatalogo.CURSO, ""));
            assertEquals(0, c.gravacoes);
        }
    }
    @Test void sessaoEncerradaImpedeGravacao() {
        var c = new CenarioCatalogo(); c.identidade.sair(c.sessao);
        assertEquals(FalhaIdentidade.Codigo.SESSAO_INVALIDA,
            assertThrows(FalhaIdentidade.class, () -> c.gestao.salvar(TipoCatalogo.CURSO, null, "ADS", "Sistemas", "", 2400)).codigo());
        assertEquals(0, c.gravacoes);
    }
}

