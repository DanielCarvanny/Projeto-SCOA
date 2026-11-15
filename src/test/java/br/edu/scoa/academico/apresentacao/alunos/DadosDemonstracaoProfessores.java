package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.dominio.professores.SituacaoFuncional;
final class DadosDemonstracaoProfessores {
    private DadosDemonstracaoProfessores() { }
    static CenarioProfessores criar(CenarioCpf identidade) {
        var cenario=new CenarioProfessores(identidade);
        cenario.gestao().cadastrar("52998224725","","","","Mestre",SituacaoFuncional.ATIVO);
        cenario.gestao().cadastrar("11144477735","","","","Doutor",SituacaoFuncional.INATIVO);
        return cenario;
    }
}
