package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class ControladorAlunosTest {
    final CenarioAlunos cenario = new CenarioAlunos();
    final ControladorAlunos controlador = cenario.controlador();

    @Test void cadastraConsultaEAtualizaComOperadorDaSessao() {
        var aluno = controlador.cadastrar(" 42 ", SituacaoCadastral.ATIVO);
        assertEquals(aluno, controlador.consultar(" " + aluno.id() + " "));
        var alterado = controlador.atualizar("" + aluno.id(), SituacaoCadastral.INATIVO);
        assertEquals(42, alterado.pessoaId());
        assertEquals(SituacaoCadastral.INATIVO, alterado.situacaoCadastral());
        assertEquals(7, cenario.autor);
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {" ", "abc", "0", "-1", "1.5", "9223372036854775808"})
    void recusaIdentificadoresInvalidosSemGravar(String id) {
        assertErro(() -> controlador.cadastrar(id, SituacaoCadastral.ATIVO), ErroCadastroAluno.ENTRADA_INVALIDA);
        assertErro(() -> controlador.consultar(id), ErroCadastroAluno.ENTRADA_INVALIDA);
        assertErro(() -> controlador.atualizar(id, SituacaoCadastral.INATIVO), ErroCadastroAluno.ENTRADA_INVALIDA);
        assertEquals(0, cenario.escritas);
    }
    @Test void preservaRecusaDePermissaoDoCasoDeUso() {
        cenario.autorizado = false;
        assertErro(() -> controlador.cadastrar("42", SituacaoCadastral.ATIVO), ErroCadastroAluno.NAO_AUTORIZADO);
        assertEquals(0, cenario.escritas);
    }
    @Test void preservaDuplicidadeEPessoaInexistente() {
        controlador.cadastrar("42", SituacaoCadastral.ATIVO);
        assertErro(() -> controlador.cadastrar("42", SituacaoCadastral.ATIVO), ErroCadastroAluno.ALUNO_JA_CADASTRADO);
        assertErro(() -> controlador.cadastrar("99", SituacaoCadastral.ATIVO), ErroCadastroAluno.PESSOA_NAO_ENCONTRADA);
    }
    @ParameterizedTest @EnumSource(ErroCadastroAluno.class)
    void traduzTodosOsCodigosParaMensagemLegivel(ErroCadastroAluno codigo) {
        var mensagem = ControladorAlunos.mensagemErro(new CadastroAlunoException(codigo));
        assertFalse(mensagem.isBlank());
        assertNotEquals(codigo.name(), mensagem);
    }
    @Test void naoExibeDetalhesTecnicosOuCredenciais() {
        var mensagem = ControladorAlunos.mensagemErro(new IllegalStateException("password=segredo SQL SELECT"));
        assertFalse(mensagem.contains("segredo"));
        assertFalse(mensagem.contains("SELECT"));
        assertTrue(mensagem.contains("Não foi possível"));
    }
    private void assertErro(Runnable acao, ErroCadastroAluno esperado) {
        assertEquals(esperado, assertThrows(CadastroAlunoException.class, acao::run).codigo());
    }
}
