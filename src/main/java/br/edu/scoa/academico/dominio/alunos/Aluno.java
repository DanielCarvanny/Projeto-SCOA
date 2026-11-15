package br.edu.scoa.academico.dominio.alunos;

/** Vínculo cadastral acadêmico; não representa conta de acesso ou matrícula em curso. */
public record Aluno(long id, long pessoaId, SituacaoCadastral situacaoCadastral) {}
