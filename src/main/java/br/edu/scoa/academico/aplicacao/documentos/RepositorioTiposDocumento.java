package br.edu.scoa.academico.aplicacao.documentos;
import br.edu.scoa.academico.dominio.documentos.*;import java.util.*;
public interface RepositorioTiposDocumento{TipoDocumento salvar(TipoDocumento tipo);List<TipoDocumento> listar();}
