package br.edu.scoa.academico.dominio.curriculo;

public record Disciplina(String codigo, String nome, String ementa, int cargaHoraria) {
    public Disciplina {
        codigo = ValidacaoCatalogo.codigo(codigo);
        nome = ValidacaoCatalogo.nome(nome);
        ementa = ValidacaoCatalogo.ementa(ementa);
        cargaHoraria = ValidacaoCatalogo.carga(cargaHoraria);
    }
}

