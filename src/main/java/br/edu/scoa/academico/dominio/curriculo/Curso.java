package br.edu.scoa.academico.dominio.curriculo;

public record Curso(String codigo, String nome, int cargaHorariaTotal) {
    public Curso {
        codigo = ValidacaoCatalogo.codigo(codigo);
        nome = ValidacaoCatalogo.nome(nome);
        cargaHorariaTotal = ValidacaoCatalogo.carga(cargaHorariaTotal);
    }
}

