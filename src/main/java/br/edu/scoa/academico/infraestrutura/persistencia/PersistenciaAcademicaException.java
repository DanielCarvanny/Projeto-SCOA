package br.edu.scoa.academico.infraestrutura.persistencia;

/** Mensagem pública sem SQL ou credenciais; causa disponível para diagnóstico controlado. */
public final class PersistenciaAcademicaException extends RuntimeException {
    public PersistenciaAcademicaException(Throwable causa) {
        super("Não foi possível concluir a operação acadêmica no banco.", causa);
    }
}
