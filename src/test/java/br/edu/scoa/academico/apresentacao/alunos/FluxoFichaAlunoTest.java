package br.edu.scoa.academico.apresentacao.alunos;

import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluxoFichaAlunoTest {
    FluxoAlunosTest preparar() throws Exception {
        var teste = new FluxoAlunosTest();
        teste.cenario.cadastro().cadastrar("52998224725", "", "", "", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        SwingUtilities.invokeAndWait(() -> teste.painel = new FluxoAlunos(teste.cenario.cadastro(), teste.cenario.gestao(), teste.dialogos,
                "DEMONSTRAÇÃO EM MEMÓRIA · identidade simulada", teste.cenario.ficha()));
        teste.clicar("menuConsultar"); teste.preencher("alunoId", "1"); teste.clicar("buscarAluno"); teste.aguardar();
        return teste;
    }
    @Test void mostraDadosPessoaisECancelaOuSalvaEdicaoDoAlunoSelecionado() throws Exception {
        var teste = preparar();
        SwingUtilities.invokeAndWait(() -> assertTrue(((JTextArea)teste.item("textoDetalhes")).getText().contains("Pessoa Exemplo A")));
        teste.previa("ficha-completa"); teste.clicar("editarPessoa"); teste.tela("edicaoPessoa"); teste.previa("edicao-pessoa");
        teste.preencher("editarNome", "Nome alterado"); teste.clicar("cancelarPessoa");
        assertEquals("Pessoa Exemplo A", teste.cenario.pessoas.get("52998224725").nome());
        teste.clicar("editarPessoa"); teste.preencher("editarNome", "Nome alterado");
        teste.dialogos.confirmar = true; teste.clicar("salvarPessoa"); teste.aguardar(); teste.tela("detalhes");
        assertEquals("Nome alterado", teste.cenario.pessoas.get("52998224725").nome());
        SwingUtilities.invokeAndWait(() -> assertTrue(((JTextArea)teste.item("textoDetalhes")).getText().contains("Nome alterado")));
    }
    @Test void erroPreservaEdicaoESemPermissaoNaoGrava() throws Exception {
        var teste = preparar(); teste.clicar("editarPessoa"); teste.preencher("editarEmail", "errado"); teste.dialogos.confirmar = true;
        teste.clicar("salvarPessoa"); teste.aguardar(); teste.tela("edicaoPessoa");
        assertEquals("pessoa.a@example.invalid", teste.cenario.pessoas.get("52998224725").email());
        teste.preencher("editarEmail", "novo@example.invalid"); teste.cenario.alunos.autorizado = false;
        teste.clicar("salvarPessoa"); teste.aguardar(); teste.tela("edicaoPessoa");
        assertEquals("pessoa.a@example.invalid", teste.cenario.pessoas.get("52998224725").email());
    }
}
