package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.professores.*;
import br.edu.scoa.academico.apresentacao.professores.FluxoProfessores;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegressaoUxTest {
    @Test void cadastroDeAlunoComecaComCpfSemCriacaoDePessoa() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var c = new CenarioCpf();
            var painel = new FluxoAlunos(c.cadastro(), c.gestao(), new PainelAlunosTest.DialogosTeste(), "Teste");
            ((JButton)PainelAlunosTest.componente(painel,"menuCadastrar")).doClick();
            assertNull(PainelAlunosTest.componente(painel,"novaPessoa"));
            assertTrue(PainelAlunosTest.componente(painel,"identificar").isVisible());
            assertNull(PainelAlunosTest.componente(painel,"cpfNovo"));
        });
    }
    @Test void cadastroDeProfessorComecaComCpfSemCriacaoDePessoa() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var c = new CenarioProfessores();
            var painel = new FluxoProfessores(c.gestao(), new PainelAlunosTest.DialogosTeste(), "Teste");
            ((JButton)PainelAlunosTest.componente(painel,"menuCadastrar")).doClick();
            assertNull(PainelAlunosTest.componente(painel,"novaPessoa"));
            assertTrue(PainelAlunosTest.componente(painel,"identificar").isVisible());
        });
    }
    @Test void textoArbitrarioNaoEhTitulacao() {
        var c = new CenarioProfessores();
        assertThrows(FalhaProfessor.class, () -> c.gestao().cadastrar("52998224725","","","","Qualquer texto",SituacaoFuncional.ATIVO));
        assertEquals(0,c.escritas);
    }
}
