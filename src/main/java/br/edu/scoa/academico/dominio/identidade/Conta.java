package br.edu.scoa.academico.dominio.identidade;
public record Conta(long id, long pessoaId, String login, Perfil perfil, boolean estaAtiva, long versao) { }
