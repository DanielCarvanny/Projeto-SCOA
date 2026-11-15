package br.edu.scoa.academico.apresentacao.alunos;

/** Carga exclusiva da demonstração; não muda o cenário vazio dos testes. */
final class DadosDemonstracaoAlunos {
    private DadosDemonstracaoAlunos() { }
    static CenarioCpf criar() {
        var cenario = new CenarioCpf();
        cenario.cadastro().cadastrar("52998224725", "", "", "",
                br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.ATIVO);
        cenario.cadastro().cadastrar("11144477735", "", "", "",
                br.edu.scoa.academico.dominio.alunos.SituacaoCadastral.INATIVO);
        return cenario;
    }
}
