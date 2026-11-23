package br.edu.scoa.academico.dominio.matriculas;
import java.time.Instant;
public record InscricaoTurma(long id,long matriculaId,long turmaId,SituacaoInscricao situacao,Instant inscritaEm,long revisao){public InscricaoTurma{if(id<0||revisao<0||(id==0)!=(revisao==0)||matriculaId<=0||turmaId<=0||situacao==null||inscritaEm==null)throw new FalhaMatricula(FalhaMatricula.Codigo.INSCRICAO_INVALIDA);}public InscricaoTurma identificado(long novoId,long novaRevisao){return new InscricaoTurma(novoId,matriculaId,turmaId,situacao,inscritaEm,novaRevisao);}}
