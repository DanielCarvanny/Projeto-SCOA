package br.edu.scoa.academico.aplicacao.matriculas;
import br.edu.scoa.academico.dominio.matriculas.*;import java.time.*;import java.util.*;
public interface RepositorioInscricoes { InscricaoTurma inscrever(InscricaoTurma inscricao,UUID operacao,long autor,Instant instante); InscricaoTurma cancelar(InscricaoTurma inscricao,UUID operacao,long autor,Instant instante); List<InscricaoTurma> ativasPorMatricula(long matriculaId); }
