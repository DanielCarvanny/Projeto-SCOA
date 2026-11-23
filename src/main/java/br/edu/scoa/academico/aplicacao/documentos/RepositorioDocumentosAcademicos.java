package br.edu.scoa.academico.aplicacao.documentos;
import br.edu.scoa.academico.dominio.documentos.*;import java.util.*;
public interface RepositorioDocumentosAcademicos{DocumentoAcademico salvar(DocumentoAcademico documento);List<DocumentoAcademico> porAluno(long alunoId);}
