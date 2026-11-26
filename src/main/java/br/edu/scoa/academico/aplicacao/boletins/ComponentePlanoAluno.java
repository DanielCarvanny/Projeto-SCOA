package br.edu.scoa.academico.aplicacao.boletins;

import br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo;
import br.edu.scoa.academico.dominio.curriculo.ComponenteCurricular;

public record ComponentePlanoAluno(ItemCatalogo disciplina, ComponenteCurricular componente) {
    public ComponentePlanoAluno {
        if (disciplina == null || componente == null || disciplina.id() != componente.disciplinaId()) {
            throw new FalhaBoletimAluno(FalhaBoletimAluno.Codigo.DADOS_ACADEMICOS_INCONSISTENTES);
        }
    }
}
