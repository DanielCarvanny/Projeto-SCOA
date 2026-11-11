package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GestaoFichaAlunoTest {
    final CenarioCpf cenario = DadosDemonstracaoAlunos.criar();
    PessoaParaCadastro pessoa = cenario.pessoas.get("52998224725");
    int leituras, escritas;
    boolean indisponivel;
    final DadosPessoaisInstitucionais porta = new DadosPessoaisInstitucionais() {
        public Optional<PessoaParaCadastro> consultarPorId(long usuario, long id) {
            leituras++; assertEquals(7, usuario); assertEquals(42, id);
            if (indisponivel) throw new IllegalStateException("Indisponível");
            return Optional.ofNullable(pessoa);
        }
        public PessoaParaCadastro atualizar(long usuario, PessoaParaCadastro esperada, String nome, String email, String telefone) {
            if (!esperada.equals(pessoa)) throw new IllegalStateException("Conflito");
            escritas++; pessoa = new PessoaParaCadastro(esperada.pessoaId(), esperada.cpf(), nome, email, telefone); return pessoa;
        }
    };
    final GestaoFichaAluno gestao = new GestaoFichaAluno(7, new ConsultarAluno(cenario.alunos, cenario.alunos), cenario.alunos, porta);
    @Test void consultaDadosPeloPessoaIdDoVinculo() {
        var ficha = gestao.consultar("1"); assertEquals(1, ficha.aluno().id()); assertEquals(42, ficha.pessoa().pessoaId());
        assertEquals("Pessoa Exemplo A", ficha.pessoa().nome());
    }
    @Test void atualizaDadosPessoaisSemTrocarCpfNemVinculo() {
        var ficha = gestao.consultar("1");
        var nova = gestao.atualizar(ficha, " Nome Novo ", "novo@example.invalid", "");
        assertEquals("Nome Novo", nova.pessoa().nome()); assertEquals(ficha.pessoa().cpf(), nova.pessoa().cpf());
        assertEquals(ficha.aluno(), nova.aluno()); assertEquals(1, escritas);
    }
    @Test void acessoNegadoNaoExibeNemAlteraPessoa() {
        var ficha = gestao.consultar("1"); cenario.alunos.autorizado = false; int antes = leituras;
        assertThrows(CadastroAlunoException.class, () -> gestao.consultar("1"));
        assertThrows(CadastroAlunoException.class, () -> gestao.atualizar(ficha, "Novo", "novo@example.invalid", ""));
        assertEquals(antes, leituras); assertEquals(0, escritas);
    }
    @Test void validaEntradaAntesDeGravar() {
        var ficha = gestao.consultar("1");
        assertThrows(CadastroAlunoException.class, () -> gestao.atualizar(ficha, "", "novo@example.invalid", ""));
        assertThrows(CadastroAlunoException.class, () -> gestao.atualizar(ficha, "Novo", "invalido", ""));
        assertThrows(CadastroAlunoException.class, () -> gestao.consultar("abc")); assertEquals(0, escritas);
    }
    @Test void ausenciaEIndisponibilidadeNaoRetornamFichaIncompleta() {
        pessoa = null; assertThrows(CadastroAlunoException.class, () -> gestao.consultar("1"));
        indisponivel = true; assertThrows(IllegalStateException.class, () -> gestao.consultar("1"));
    }
    @Test void rejeitaRespostaInstitucionalDeOutraPessoa() {
        pessoa = new PessoaParaCadastro(43, "11144477735", "Outra", "outra@example.invalid", "");
        assertThrows(IllegalStateException.class, () -> gestao.consultar("1"));
    }
    @Test void edicaoConcorrenteNaoSobrescreveAtualizacaoAnterior() {
        var ficha = gestao.consultar("1"); gestao.atualizar(ficha, "Primeira", "primeira@example.invalid", "");
        assertThrows(IllegalStateException.class, () -> gestao.atualizar(ficha, "Segunda", "segunda@example.invalid", ""));
        assertEquals("Primeira", pessoa.nome()); assertEquals(1, escritas);
    }
    @Test void novaTentativaReutilizaPessoaAposFalhaDoVinculo() {
        var novo = new CenarioCpf();
        var falhar = new java.util.concurrent.atomic.AtomicBoolean(true);
        RepositorioAlunos repositorio = (id, situacao, autor) -> {
            if (falhar.getAndSet(false)) throw new IllegalStateException("Falha simulada de gravação");
            return novo.alunos.cadastrar(id, situacao, autor);
        };
        var cadastro = new CadastroAlunoPorCpf(7, novo, novo, new CadastrarAluno(novo, repositorio));
        assertThrows(IllegalStateException.class, () -> cadastro.cadastrar("12345678909", "Pessoa Teste", "teste@example.invalid", "", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO));
        assertEquals(3, novo.pessoas.size()); assertTrue(novo.alunos.alunos.isEmpty());
        var aluno = cadastro.cadastrar("12345678909", "Pessoa Teste", "teste@example.invalid", "", br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        assertEquals(novo.pessoas.get("12345678909").pessoaId(), aluno.pessoaId()); assertEquals(3, novo.pessoas.size());
    }
}
