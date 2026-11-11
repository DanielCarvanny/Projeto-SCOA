package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.CadastroAlunoException;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DadosDemonstracaoAlunosTest {
    @Test void iniciaComDoisAlunosConsultaveisERecusaDuplicidade() {
        var cenario = DadosDemonstracaoAlunos.criar();
        assertEquals(42, cenario.gestao().consultar("1").pessoaId());
        assertEquals(SituacaoCadastral.ATIVO, cenario.gestao().consultar("1").situacaoCadastral());
        assertEquals(43, cenario.gestao().consultar("2").pessoaId());
        assertEquals(SituacaoCadastral.INATIVO, cenario.gestao().consultar("2").situacaoCadastral());
        assertThrows(CadastroAlunoException.class, () -> cenario.cadastro().cadastrar("52998224725", "", "", "", SituacaoCadastral.ATIVO));
    }
    @Test void permiteAlterarENovoCadastroSemCompartilharEstadoEntreExecucoes() {
        var cenario = DadosDemonstracaoAlunos.criar();
        cenario.gestao().atualizar("1", SituacaoCadastral.INATIVO);
        assertEquals(SituacaoCadastral.INATIVO, cenario.gestao().consultar("1").situacaoCadastral());
        assertEquals(3, cenario.cadastro().cadastrar("12345678909", "Nova Pessoa Teste", "nova@example.invalid", "", SituacaoCadastral.ATIVO).id());
        var reiniciado = DadosDemonstracaoAlunos.criar();
        assertEquals(SituacaoCadastral.ATIVO, reiniciado.gestao().consultar("1").situacaoCadastral());
        assertThrows(CadastroAlunoException.class, () -> reiniciado.gestao().consultar("3"));
    }
}
