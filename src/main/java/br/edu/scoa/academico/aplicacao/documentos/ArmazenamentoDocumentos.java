package br.edu.scoa.academico.aplicacao.documentos;
import java.io.*;import java.nio.file.*;import java.util.*;
public interface ArmazenamentoDocumentos { String guardar(String nome,String mime,long tamanho,InputStream conteudo)throws IOException; void remover(String chave)throws IOException; }
