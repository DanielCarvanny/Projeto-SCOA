package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.alunos.*;
import br.edu.scoa.academico.dominio.professores.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class FluxosCadastroExplicitoTest {
    final CenarioCpf pessoas = new CenarioCpf();
    final CenarioProfessores docentes = new CenarioProfessores(pessoas);
    @Test void parametrosLegadosNaoSobrescrevemPessoa() {
        pessoas.cadastro().cadastrar("52998224725","Outro","outro@example.invalid","",SituacaoCadastral.ATIVO,true);
        docentes.gestao().cadastrar("52998224725","Outro","outro@example.invalid","","Mestre",SituacaoFuncional.ATIVO,true);
        assertEquals("Pessoa Exemplo A",pessoas.pessoas.get("52998224725").nome());assertEquals(2,pessoas.pessoas.size());
    }
    @Test void caminhoExistenteNuncaCriaPessoaAusente() {
        assertEquals(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA,assertThrows(CadastroAlunoException.class,()->pessoas.cadastro().cadastrar("12345678909","Nova","nova@example.invalid","",SituacaoCadastral.ATIVO,false)).codigo());
        assertEquals(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA,assertThrows(FalhaProfessor.class,()->docentes.gestao().cadastrar("12345678909","Nova","nova@example.invalid","","Mestre",SituacaoFuncional.ATIVO,false)).codigo());
        assertEquals(2,pessoas.pessoas.size());assertEquals(0,pessoas.alunos.escritas);assertEquals(0,docentes.escritas);
    }
    @Test void parametroLegadoPessoaNovaTambemNaoCriaPessoa() {
        assertThrows(CadastroAlunoException.class,()->pessoas.cadastro().cadastrar("12345678909","Nova","nova@example.invalid","",SituacaoCadastral.ATIVO,true));
        assertThrows(FalhaProfessor.class,()->docentes.gestao().cadastrar("12345678909","Nova","nova@example.invalid","","Mestre",SituacaoFuncional.ATIVO,true));
        assertEquals(2,pessoas.pessoas.size());
    }
    @ParameterizedTest @ValueSource(strings={"52998224725","529.982.247-25"})
    void consultaPorCpfUsaPessoaIdDiferenteDeAlunoId(String cpf) {
        var aluno=pessoas.cadastro().cadastrar(cpf,"","","",SituacaoCadastral.ATIVO,false);
        var ficha=pessoas.cadastro().consultarAluno(cpf,pessoas.ficha());
        assertEquals(aluno,ficha.aluno());assertEquals(42,ficha.pessoa().pessoaId());assertNotEquals(aluno.id(),aluno.pessoaId());
    }
    @Test void consultaDistinguePessoaAusenteDeVinculoAusente() {
        assertEquals(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA,assertThrows(CadastroAlunoException.class,()->pessoas.cadastro().consultarAluno("12345678909",pessoas.ficha())).codigo());
        assertEquals(ErroCadastroAluno.ALUNO_NAO_ENCONTRADO,assertThrows(CadastroAlunoException.class,()->pessoas.cadastro().consultarAluno("52998224725",pessoas.ficha())).codigo());
    }
    @Test void consultaRespeitaAutorizacaoDoAlunoSemExporFicha() {
        pessoas.cadastro().cadastrar("52998224725","","","",SituacaoCadastral.ATIVO);
        AcessoAlunos negado=new AcessoAlunos(){public boolean podeConsultarAluno(long u,long a){return false;}public boolean podeAtualizarAluno(long u,long a){return false;}};
        var fichas=new GestaoFichaAluno(7,new ConsultarAluno(negado,pessoas.alunos),negado,pessoas);
        assertEquals(ErroCadastroAluno.NAO_AUTORIZADO,assertThrows(CadastroAlunoException.class,()->fichas.consultarPorCpf("52998224725",pessoas)).codigo());
    }
    @ParameterizedTest @ValueSource(strings={"123","abc","11111111111","529.982247-25"})
    void cpfInvalidoNaoConsultaInstituicaoEmNenhumFluxo(String cpf) {
        PessoasPorCpf proibida=new PessoasPorCpf(){
            public Optional<PessoaParaCadastro> consultar(long u,String c){fail("Não consultar CPF inválido");return Optional.empty();}
            public PessoaParaCadastro cadastrarOuObter(long u,String c,String n,String e,String t){throw new AssertionError();}
        };
        var aluno=new CadastroAlunoPorCpf(7,pessoas,proibida,new CadastrarAluno(pessoas,pessoas.alunos));
        var professor=new GestaoProfessores(7,docentes,proibida,pessoas,docentes);
        assertEquals(ErroCadastroAluno.CPF_INVALIDO,assertThrows(CadastroAlunoException.class,()->aluno.consultar(cpf)).codigo());
        assertEquals(FalhaProfessor.Codigo.CPF_INVALIDO,assertThrows(FalhaProfessor.class,()->professor.buscarPessoa(cpf)).codigo());
        assertEquals(FalhaProfessor.Codigo.CPF_INVALIDO,assertThrows(FalhaProfessor.class,()->professor.consultarPorCpf(cpf)).codigo());
        assertEquals(ErroCadastroAluno.CPF_INVALIDO,assertThrows(CadastroAlunoException.class,()->pessoas.ficha().consultarPorCpf(cpf,proibida)).codigo());
    }
    @ParameterizedTest @ValueSource(strings={"Tecnólogo","Bacharel","Licenciado","Especialista","Mestre","Doutor"})
    void aceitaCadaTituloDoCatalogo(String titulo) {
        assertEquals(titulo,docentes.gestao().cadastrar("52998224725","","","",titulo,SituacaoFuncional.ATIVO,false).titulacao());
    }
    @Test void titulosAntigosSaoLidosSemConversaoEAtualizadosExplicitamente() {
        var antigo=docentes.cadastrar(42,"Mestrado",SituacaoFuncional.ATIVO,7);
        assertEquals("Mestrado",docentes.gestao().consultar("1").professor().titulacao());
        assertEquals("Mestre",docentes.gestao().atualizar(antigo,"Mestre",SituacaoFuncional.ATIVO).titulacao());
    }
    @Test void alteracaoEspecificaNaoAfetaOutroVinculo() {
        pessoas.cadastro().cadastrar("52998224725","","","",SituacaoCadastral.ATIVO);
        var professor=docentes.gestao().cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO);
        pessoas.gestao().atualizar("1",SituacaoCadastral.INATIVO);
        assertEquals(SituacaoFuncional.ATIVO,docentes.gestao().consultar("1").professor().situacaoFuncional());
        docentes.gestao().atualizar(professor,"Doutor",SituacaoFuncional.INATIVO);
        assertEquals(SituacaoCadastral.INATIVO,pessoas.ficha().consultar("1").aluno().situacaoCadastral());
    }
    @Test void indisponibilidadeNaoViraPessoaAusente() {
        PessoasPorCpf indisponivel=new PessoasPorCpf(){
            public Optional<PessoaParaCadastro> consultar(long u,String c){throw new IllegalStateException("Falha sintética");}
            public PessoaParaCadastro cadastrarOuObter(long u,String c,String n,String e,String t){throw new AssertionError();}
        };
        assertThrows(IllegalStateException.class,()->pessoas.ficha().consultarPorCpf("52998224725",indisponivel));
    }
}
