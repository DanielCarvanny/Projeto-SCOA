package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.alunos.*;
import br.edu.scoa.academico.dominio.professores.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CadastroSomenteVinculoTest {
    @Test void cadastrarAlunoOuProfessorNaoCriaPessoaImplicitamente() {
        var pessoas=new CenarioCpf(); var professores=new CenarioProfessores(pessoas);
        assertEquals(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA,assertThrows(CadastroAlunoException.class,
            ()->pessoas.cadastro().cadastrar("12345678901","Nova","nova@example.invalid","",SituacaoCadastral.ATIVO)).codigo());
        assertEquals(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA,assertThrows(FalhaProfessor.class,
            ()->professores.gestao().cadastrar("12345678901","Nova","nova@example.invalid","","Mestre",SituacaoFuncional.ATIVO)).codigo());
        assertEquals(2,pessoas.pessoas.size());assertEquals(0,pessoas.alunos.escritas);assertEquals(0,professores.escritas);
    }
}
