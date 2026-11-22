package br.edu.scoa.academico.dominio.curriculo;
public final class FalhaPreRequisito extends RuntimeException {
    public enum Codigo { AUTO_REFERENCIA, DUPLICADO, FORA_DA_MATRIZ, CICLO, COMPONENTE_UTILIZADO }
    private final Codigo codigo;
    public FalhaPreRequisito(Codigo codigo){super(codigo.name());this.codigo=codigo;}
    public Codigo codigo(){return codigo;}
}

