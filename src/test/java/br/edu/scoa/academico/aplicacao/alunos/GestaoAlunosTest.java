package br.edu.scoa.academico.aplicacao.alunos;

import br.edu.scoa.academico.dominio.alunos.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.aplicacao.alunos.ErroCadastroAluno.*;

class GestaoAlunosTest {
    final RepositorioFake repositorio = new RepositorioFake();
    boolean autorizado = true;
    RuntimeException falhaAcesso;
    final AcessoAlunos acesso = new AcessoAlunos() {
        public boolean podeConsultarAluno(long usuarioId, long alunoId) {
            assertEquals(7, usuarioId); assertEquals(10, alunoId);
            if (falhaAcesso != null) throw falhaAcesso;
            return autorizado;
        }
        public boolean podeAtualizarAluno(long usuarioId, long alunoId) {
            return podeConsultarAluno(usuarioId, alunoId);
        }
    };
    final ConsultarAluno consultar = new ConsultarAluno(acesso, repositorio);
    final AtualizarAluno atualizar = new AtualizarAluno(acesso, repositorio);

    @Test void consultaAlunoPermitido() { assertEquals(repositorio.aluno, consultar.executar(7, 10)); }
    @Test void consultaAusenteRetornaErro() {
        repositorio.aluno = null;
        erro(ALUNO_NAO_ENCONTRADO, () -> consultar.executar(7, 10));
    }
    @Test void consultaSemPermissaoNaoAcessaBanco() {
        autorizado = false;
        erro(NAO_AUTORIZADO, () -> consultar.executar(7, 10));
        assertEquals(0, repositorio.leituras);
    }
    @Test void atualizaSituacaoPreservandoIdentidadeEAutor() {
        var aluno = atualizar.executar(7, 10, SituacaoCadastral.INATIVO);
        assertEquals(new Aluno(10, 42, SituacaoCadastral.INATIVO), aluno);
        assertEquals(7, repositorio.autor);
    }
    @Test void atualizacaoSemPermissaoNaoGrava() {
        autorizado = false;
        erro(NAO_AUTORIZADO, () -> atualizar.executar(7, 10, SituacaoCadastral.INATIVO));
        assertEquals(0, repositorio.escritas);
    }
    @Test void atualizacaoAusentePreservaErroDoRepositorio() {
        repositorio.aluno = null;
        erro(ALUNO_NAO_ENCONTRADO, () -> atualizar.executar(7, 10, SituacaoCadastral.INATIVO));
    }
    @Test void situacaoNulaNaoGrava() {
        erro(ENTRADA_INVALIDA, () -> atualizar.executar(7, 10, null));
        assertEquals(0, repositorio.escritas);
    }
    @ParameterizedTest @CsvSource({"0,10", "-1,10", "7,0", "7,-1"})
    void idsInvalidosNaoAcessamBanco(long usuario, long aluno) {
        erro(ENTRADA_INVALIDA, () -> consultar.executar(usuario, aluno));
        erro(ENTRADA_INVALIDA, () -> atualizar.executar(usuario, aluno, SituacaoCadastral.ATIVO));
        assertEquals(0, repositorio.leituras + repositorio.escritas);
    }
    @Test void falhaDeAutorizacaoNaoPermiteLeituraNemEscrita() {
        falhaAcesso = new IllegalStateException("Autorização indisponível");
        assertSame(falhaAcesso, assertThrows(IllegalStateException.class, () -> consultar.executar(7,10)));
        assertSame(falhaAcesso, assertThrows(IllegalStateException.class, () -> atualizar.executar(7,10,SituacaoCadastral.ATIVO)));
        assertEquals(0, repositorio.leituras + repositorio.escritas);
    }
    private void erro(ErroCadastroAluno codigo, Runnable operacao) {
        assertEquals(codigo, assertThrows(CadastroAlunoException.class, operacao::run).codigo());
    }
    static final class RepositorioFake implements RepositorioGestaoAlunos {
        Aluno aluno = new Aluno(10,42,SituacaoCadastral.ATIVO);
        int leituras, escritas;
        long autor;
        public Aluno cadastrar(long pessoa, SituacaoCadastral situacao, long usuario) {
            throw new AssertionError("Consulta/atualização não devem cadastrar.");
        }
        public Optional<Aluno> consultar(long id) { leituras++; return Optional.ofNullable(aluno); }
        public Aluno atualizarSituacao(long id, SituacaoCadastral situacao, long usuario) {
            if (aluno == null) throw new CadastroAlunoException(ALUNO_NAO_ENCONTRADO);
            escritas++; autor = usuario;
            aluno = new Aluno(id, aluno.pessoaId(), situacao);
            return aluno;
        }
    }
}
