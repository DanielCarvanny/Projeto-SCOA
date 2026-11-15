package br.edu.scoa.academico.aplicacao.alunos;

import br.edu.scoa.academico.dominio.alunos.Aluno;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static br.edu.scoa.academico.aplicacao.alunos.ErroCadastroAluno.*;

class CadastrarAlunoTest {
    private final IdentidadeFake identidade = new IdentidadeFake();
    private final RepositorioFake repositorio = new RepositorioFake();
    private final CadastrarAluno cadastrar = new CadastrarAluno(identidade, repositorio);

    @Test void cadastraAlunoPreservandoPessoaSituacaoEAutor() {
        var aluno = cadastrar.executar(7, 42, SituacaoCadastral.ATIVO);
        assertAll(() -> assertEquals(1, aluno.id()),
            () -> assertEquals(42, aluno.pessoaId()),
            () -> assertEquals(SituacaoCadastral.ATIVO, aluno.situacaoCadastral()),
            () -> assertEquals(aluno, repositorio.alunos.get(42L)),
            () -> assertEquals(7, repositorio.autor));
    }
    @Test void preservaSituacaoInativaInformada() {
        assertEquals(SituacaoCadastral.INATIVO,
            cadastrar.executar(7, 42, SituacaoCadastral.INATIVO).situacaoCadastral());
    }
    @Test void recusaDuplicidadeSemAlterarCadastroAnterior() {
        var original = cadastrar.executar(7, 42, SituacaoCadastral.ATIVO);
        assertErro(ALUNO_JA_CADASTRADO, () -> cadastrar.executar(7, 42, SituacaoCadastral.INATIVO));
        assertEquals(Map.of(42L, original), repositorio.alunos);
    }
    @Test void impedeCadastroSemPermissaoAntesDeConsultarPessoa() {
        identidade.autorizado = false;
        assertErro(NAO_AUTORIZADO, () -> cadastrar.executar(7, 42, SituacaoCadastral.ATIVO));
        assertEquals(0, identidade.consultasPessoa);
        assertEquals(0, repositorio.chamadas);
    }
    @Test void recusaPessoaInexistenteSemGravar() {
        identidade.existe = false;
        assertErro(PESSOA_NAO_ENCONTRADA, () -> cadastrar.executar(7, 42, SituacaoCadastral.ATIVO));
        assertEquals(0, repositorio.chamadas);
    }
    @ParameterizedTest @ValueSource(longs = {0, -1})
    void recusaPessoaIdInvalido(long id) {
        assertErro(ENTRADA_INVALIDA, () -> cadastrar.executar(7, id, SituacaoCadastral.ATIVO));
        assertEquals(0, repositorio.chamadas);
    }
    @ParameterizedTest @ValueSource(longs = {0, -1})
    void recusaUsuarioIdInvalido(long id) {
        assertErro(ENTRADA_INVALIDA, () -> cadastrar.executar(id, 42, SituacaoCadastral.ATIVO));
        assertEquals(0, repositorio.chamadas);
    }
    @Test void exigeSituacaoExplicitaSemInventarValorPadrao() {
        assertErro(ENTRADA_INVALIDA, () -> cadastrar.executar(7, 42, null));
        assertEquals(0, repositorio.chamadas);
    }
    @Test void falhaDeIdentidadeNaoAutorizaGravacao() {
        identidade.falha = new IllegalStateException("Identidade indisponível");
        assertSame(identidade.falha, assertThrows(IllegalStateException.class,
            () -> cadastrar.executar(7, 42, SituacaoCadastral.ATIVO)));
        assertEquals(0, repositorio.chamadas);
    }
    @Test void falhaDePersistenciaNaoRetornaSucesso() {
        var falha = new IllegalStateException("Persistência indisponível");
        RepositorioAlunos indisponivel = (pessoa, situacao, autor) -> { throw falha; };
        var caso = new CadastrarAluno(identidade, indisponivel);
        assertSame(falha, assertThrows(IllegalStateException.class,
            () -> caso.executar(7, 42, SituacaoCadastral.ATIVO)));
    }
    private void assertErro(ErroCadastroAluno esperado, Runnable acao) {
        assertEquals(esperado, assertThrows(CadastroAlunoException.class, acao::run).codigo());
    }
    private static final class IdentidadeFake implements IdentidadeInstitucional {
        boolean autorizado = true;
        boolean existe = true;
        int consultasPessoa;
        RuntimeException falha;
        public boolean podeCadastrarAluno(long usuarioId) {
            if (falha != null) throw falha;
            return autorizado;
        }
        public boolean pessoaExiste(long pessoaId) { consultasPessoa++; return existe; }
    }
    /** Dublê apenas para testes unitários; não comprova MySQL nem concorrência real. */
    private static final class RepositorioFake implements RepositorioAlunos {
        final Map<Long, Aluno> alunos = new HashMap<>();
        int chamadas;
        long autor;
        public Aluno cadastrar(long pessoaId, SituacaoCadastral situacao, long usuarioId) {
            chamadas++;
            if (alunos.containsKey(pessoaId)) throw new CadastroAlunoException(ALUNO_JA_CADASTRADO);
            var aluno = new Aluno(alunos.size() + 1, pessoaId, situacao);
            alunos.put(pessoaId, aluno);
            autor = usuarioId;
            return aluno;
        }
    }
}
