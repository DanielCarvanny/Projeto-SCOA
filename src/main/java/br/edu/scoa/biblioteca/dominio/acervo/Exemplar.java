package br.edu.scoa.biblioteca.dominio.acervo;
public record Exemplar(long id, String numeroTombo, boolean disponivel, long obraId) {}