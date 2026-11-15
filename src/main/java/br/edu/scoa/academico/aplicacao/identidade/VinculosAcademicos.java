package br.edu.scoa.academico.aplicacao.identidade;

import br.edu.scoa.academico.dominio.identidade.Perfil;

/** Existência do vínculo é distinta de sua completude e do perfil da conta. */
public record VinculosAcademicos(boolean possuiAluno, boolean possuiProfessor, boolean professorCompleto) {
    public void exigirPerfil(Perfil perfil) {
        if (perfil == Perfil.ALUNO && !possuiAluno)
            throw new FalhaIdentidade(FalhaIdentidade.Codigo.VINCULO_ALUNO_NECESSARIO);
        if (perfil == Perfil.PROFESSOR && (!possuiProfessor || !professorCompleto))
            throw new FalhaIdentidade(FalhaIdentidade.Codigo.VINCULO_PROFESSOR_NECESSARIO);
    }
    public String descricao() {
        if (possuiAluno && possuiProfessor) return "Aluno e Professor";
        if (possuiAluno) return "Aluno";
        if (possuiProfessor) return "Professor";
        return "Nenhum";
    }
}
