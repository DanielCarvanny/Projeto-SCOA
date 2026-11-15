package br.edu.scoa.academico.dominio.professores;

/** Catálogo aprovado pelo responsável do Acadêmico em 11/11/2026. */
public enum Titulacao {
    TECNOLOGO("Tecnólogo"), BACHAREL("Bacharel"), LICENCIADO("Licenciado"),
    ESPECIALISTA("Especialista"), MESTRE("Mestre"), DOUTOR("Doutor");
    private final String descricao;
    Titulacao(String descricao) { this.descricao = descricao; }
    public String toString() { return descricao; }
    public static boolean aceita(String valor) {
        for (var titulo : values()) if (titulo.descricao.equals(valor)) return true;
        return false;
    }
}
