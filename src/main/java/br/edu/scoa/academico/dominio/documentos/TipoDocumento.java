package br.edu.scoa.academico.dominio.documentos;
public record TipoDocumento(long id,String nome,boolean obrigatorio,boolean ativo,long revisao){public TipoDocumento{nome=nome==null?"":nome.strip();if(nome.isBlank()||id<0||revisao<0||(id==0)!=(revisao==0))throw new IllegalArgumentException("Tipo de documento inválido");}}
