package br.edu.scoa.academico.aplicacao.matriculas;
import br.edu.scoa.academico.dominio.matriculas.*;import java.time.*;import java.util.*;
public interface RepositorioMatriculas { MatriculaCurso salvar(MatriculaCurso matricula,UUID operacao,long autor,Instant instante); Optional<MatriculaCurso> porNumero(String numero); Optional<MatriculaCurso> consultar(long id); List<MatriculaCurso> porAluno(long alunoId); }
