package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class CadastroAlunoPorCpfTest {
    final CenarioAlunos alunos = new CenarioAlunos();
    final Map<String, PessoaParaCadastro> pessoas = new HashMap<>();
    int consultas;
    int criacoes;
    boolean indisponivel;
    final PessoasPorCpf porta = new PessoasPorCpf() {
        public Optional<PessoaParaCadastro> consultar(long usuario, String cpf) {
            consultas++;
            if (indisponivel) throw new IllegalStateException("Indisponível");
            assertEquals(7, usuario);
            return Optional.ofNullable(pessoas.get(cpf));
        }
        public PessoaParaCadastro cadastrarOuObter(long usuario, String cpf, String nome, String email, String telefone) {
            criacoes++;
            return pessoas.computeIfAbsent(cpf, chave -> new PessoaParaCadastro(42, cpf, nome, email, telefone));
        }
    };
    final CadastroAlunoPorCpf fluxo = new CadastroAlunoPorCpf(7, alunos, porta, new CadastrarAluno(alunos, alunos));

    @Test void localizaPessoaComCpfFormatadoECadastraPeloIdInterno() {
        pessoas.put("52998224725", new PessoaParaCadastro(42, "52998224725", "Pessoa Teste", "teste@example.invalid", ""));
        assertEquals(42, fluxo.consultar("529.982.247-25").orElseThrow().pessoaId());
        assertEquals(42, fluxo.cadastrar("52998224725", "", "", "", SituacaoCadastral.ATIVO).pessoaId());
        assertEquals(0, criacoes);
    }
    @Test void solicitaCriacaoPelaPortaERecusaAlunoDuplicado() {
        assertTrue(fluxo.consultar("52998224725").isEmpty());
        fluxo.cadastrar("52998224725", "Pessoa Teste", "teste@example.invalid", "", SituacaoCadastral.ATIVO);
        assertEquals(1, criacoes);
        assertThrows(CadastroAlunoException.class, () -> fluxo.cadastrar("52998224725", "Outro", "", "", SituacaoCadastral.ATIVO));
        assertEquals("Pessoa Teste", pessoas.get("52998224725").nome());
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"42", "11111111111", "52998224724", "abc52998224725", "529.982247-25"})
    void recusaCpfInvalidoAntesDeConsultar(String cpf) {
        assertThrows(CadastroAlunoException.class, () -> fluxo.consultar(cpf));
        assertEquals(0, consultas);
    }
    @Test void recusaAcessoAntesDeExporPessoa() {
        alunos.autorizado = false;
        assertThrows(CadastroAlunoException.class, () -> fluxo.consultar("52998224725"));
        assertThrows(CadastroAlunoException.class, () -> fluxo.cadastrar("52998224725", "Teste", "teste@example.invalid", "", SituacaoCadastral.ATIVO));
        assertEquals(0, consultas);
        assertEquals(0, criacoes);
    }
    @Test void falhaNaoViraPessoaAusenteNemCriaCadastro() {
        indisponivel = true;
        assertThrows(IllegalStateException.class, () -> fluxo.cadastrar("52998224725", "Teste", "teste@example.invalid", "", SituacaoCadastral.ATIVO));
        assertEquals(0, criacoes);
        assertEquals(0, alunos.escritas);
    }
    @Test void validaDadosESituacaoAntesDeCriarPessoa() {
        assertThrows(CadastroAlunoException.class, () -> fluxo.cadastrar("52998224725", "", "teste@example.invalid", "", SituacaoCadastral.ATIVO));
        assertThrows(CadastroAlunoException.class, () -> fluxo.cadastrar("52998224725", "Teste", "sem-email", "", SituacaoCadastral.ATIVO));
        assertThrows(CadastroAlunoException.class, () -> fluxo.cadastrar("52998224725", "Teste", "teste@example.invalid", "", null));
        assertEquals(0, criacoes);
    }
}
