package br.edu.scoa.almoxarifado.dominio;

public record Localizacao(long id, String nome, String descricao, boolean ativa) {
    public Localizacao {
        if (id < 0 || nome == null || nome.isBlank() || nome.trim().length() > 120
                || descricao == null || descricao.length() > 500) {
            throw new IllegalArgumentException("Localização inválida.");
        }
        nome = nome.trim();
        descricao = descricao.trim();
    }

    public static Localizacao nova(String nome, String descricao) {
        return new Localizacao(0, nome, descricao == null ? "" : descricao, true);
    }
}
