package br.edu.scoa.academico.aplicacao.identidade;
public interface Senhas { String gerar(char[] senha); boolean confere(char[] senha,String hash); }
