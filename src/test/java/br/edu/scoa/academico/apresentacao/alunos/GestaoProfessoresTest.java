package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.professores.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
class GestaoProfessoresTest {
    final CenarioProfessores cenario = new CenarioProfessores();
    final GestaoProfessores gestao = cenario.gestao();
    Professor cadastrar() { return gestao.cadastrar("529.982.247-25", "", "", "", " Mestre ", SituacaoFuncional.ATIVO); }
    @Test void cadastroConsultaAtualizacaoPreservamPessoaETitulacao() {
        var professor=cadastrar(); assertEquals(42, professor.pessoaId()); assertEquals("Mestre", professor.titulacao());
        var ficha=gestao.consultar("1"); assertEquals("Pessoa Exemplo A", ficha.pessoa().nome());
        var novo=gestao.atualizar(professor,"Doutor",SituacaoFuncional.INATIVO);
        assertEquals(42,novo.pessoaId()); assertEquals("Doutor",novo.titulacao());
    }
    @Test void recusaVinculoDuplicado() { cadastrar(); assertEquals(FalhaProfessor.Codigo.PROFESSOR_JA_CADASTRADO, assertThrows(FalhaProfessor.class,this::cadastrar).codigo()); }
    @Test void buscaCpfDistinguePessoaAusenteVinculoAusenteEAcessoNegado() {
        assertEquals(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA,assertThrows(FalhaProfessor.class,()->gestao.consultarPorCpf("12345678901")).codigo());
        assertEquals(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO,assertThrows(FalhaProfessor.class,()->gestao.consultarPorCpf("52998224725")).codigo());
        var professor=cadastrar();
        assertEquals(professor,gestao.consultarPorCpf("529.982.247-25").professor());
        assertNotEquals(professor.id(),professor.pessoaId());
        cenario.autorizado=false;
        assertEquals(FalhaProfessor.Codigo.NAO_AUTORIZADO,assertThrows(FalhaProfessor.class,()->gestao.consultarPorCpf("52998224725")).codigo());
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={" ", "11111111111","abc"})
    void cpfInvalidoNaoCriaPessoaOuProfessor(String cpf) {
        assertThrows(FalhaProfessor.class,()->gestao.cadastrar(cpf,"Nova","nova@example.invalid","","Mestre",SituacaoFuncional.ATIVO));
        assertEquals(0,cenario.escritas); assertEquals(2,cenario.identidade.pessoas.size());
    }
    @Test void dadosDocentesObrigatoriosAntesDeCriarPessoa() {
        assertThrows(FalhaProfessor.class,()->gestao.cadastrar("12345678909","Nova","nova@example.invalid","","",SituacaoFuncional.ATIVO));
        assertThrows(FalhaProfessor.class,()->gestao.cadastrar("12345678909","Nova","nova@example.invalid","","X".repeat(256),SituacaoFuncional.ATIVO));
        assertThrows(FalhaProfessor.class,()->gestao.cadastrar("12345678909","Nova","nova@example.invalid","","Mestre",null));
        assertEquals(2,cenario.identidade.pessoas.size());
    }
    @Test void acessoNegadoNaoConsultaNemGrava() {
        var professor=cadastrar(); cenario.autorizado=false;
        assertThrows(FalhaProfessor.class,()->gestao.buscarPessoa("52998224725"));
        assertThrows(FalhaProfessor.class,()->gestao.consultar("1"));
        assertThrows(FalhaProfessor.class,()->gestao.atualizar(professor,"Doutor",SituacaoFuncional.ATIVO));
        assertThrows(FalhaProfessor.class,this::cadastrar); assertEquals(1,cenario.escritas);
    }
    @Test void recusaEdicaoDesatualizada() { var p=cadastrar(); gestao.atualizar(p,"Doutor",SituacaoFuncional.ATIVO); assertThrows(FalhaProfessor.class,()->gestao.atualizar(p,"Especialista",SituacaoFuncional.INATIVO)); }
    @Test void vinculaPessoaExistenteEEditaContatoSemTrocarCpf() {
        cenario.identidade.cadastrarOuObter(7,"12345678909","Nova","nova@example.invalid","");
        var p=gestao.cadastrar("12345678909","Nova","nova@example.invalid","","Bacharel",SituacaoFuncional.ATIVO);
        var ficha=gestao.consultar(""+p.id());
        var nova=gestao.atualizarPessoa(ficha,"Novo nome","novo@example.invalid","");
        assertEquals("Novo nome",nova.pessoa().nome()); assertEquals(ficha.pessoa().cpf(),nova.pessoa().cpf());
    }
    @Test void pessoaAusenteOuFalhaNaoRetornaFichaIncompleta() {
        cadastrar(); cenario.identidade.pessoas.clear(); assertThrows(RuntimeException.class,()->gestao.consultar("1"));
    }
    @Test void falhaDeIdentidadeNaoCriaProfessor() {
        var porta=new br.edu.scoa.academico.aplicacao.alunos.PessoasPorCpf() {
            public java.util.Optional<br.edu.scoa.academico.aplicacao.alunos.PessoaParaCadastro> consultar(long usuario,String cpf){throw new IllegalStateException("Indisponível");}
            public br.edu.scoa.academico.aplicacao.alunos.PessoaParaCadastro cadastrarOuObter(long usuario,String cpf,String nome,String email,String telefone){throw new AssertionError("Não deve criar pessoa");}
        };
        var servico=new GestaoProfessores(7,cenario,porta,cenario.identidade,cenario);
        assertThrows(IllegalStateException.class,()->servico.cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO));
        assertEquals(0,cenario.escritas);
    }
    @Test void repetirAposFalhaDoVinculoReutilizaPessoa() {
        cenario.identidade.cadastrarOuObter(7,"12345678909","Nova","nova@example.invalid","");
        cenario.antesDeGravar=()->{throw new IllegalStateException("Falha sintética");};
        assertThrows(IllegalStateException.class,()->gestao.cadastrar("12345678909","Nova","nova@example.invalid","","Bacharel",SituacaoFuncional.ATIVO));
        assertEquals(3,cenario.identidade.pessoas.size());cenario.antesDeGravar=()->{};
        var professor=gestao.cadastrar("12345678909","Nova","nova@example.invalid","","Bacharel",SituacaoFuncional.ATIVO);
        assertEquals(3,cenario.identidade.pessoas.size());assertEquals(cenario.identidade.pessoas.get("12345678909").pessoaId(),professor.pessoaId());
    }
    @Test void alunoEProfessorCompartilhamIdentidadeSemDuplicarPessoa() {
        var alunos=DadosDemonstracaoAlunos.criar();var docentes=DadosDemonstracaoProfessores.criar(alunos);
        var ficha=docentes.gestao().consultar("1");
        docentes.gestao().atualizarPessoa(ficha,"Nome compartilhado","comum@example.invalid","");
        assertEquals("Nome compartilhado",alunos.ficha().consultar("1").pessoa().nome());assertEquals(2,alunos.pessoas.size());
    }
}
