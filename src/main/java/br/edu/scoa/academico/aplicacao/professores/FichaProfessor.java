package br.edu.scoa.academico.aplicacao.professores;
import br.edu.scoa.academico.aplicacao.alunos.PessoaParaCadastro;
import br.edu.scoa.academico.dominio.professores.Professor;
public record FichaProfessor(Professor professor, PessoaParaCadastro pessoa) { }
