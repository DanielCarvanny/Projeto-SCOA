package br.edu.scoa.academico.infraestrutura.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/** Credenciais não aparecem em toString nem em mensagens de validação. */
public final class ConfiguracaoBanco {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConfiguracaoBanco(String url, String usuario, String senha) {
        if (url == null || !url.startsWith("jdbc:mysql://") || usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Configure URL MySQL, usuário e senha.");
        }
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public static ConfiguracaoBanco doAmbiente(Map<String, String> ambiente, String prefixo) {
        return new ConfiguracaoBanco(ambiente.get(prefixo + "URL"),
                ambiente.get(prefixo + "USER"), ambiente.get(prefixo + "PASSWORD"));
    }

    public Connection abrir() throws SQLException {
        var propriedades = new Properties();
        propriedades.setProperty("user", usuario);
        propriedades.setProperty("password", senha);
        propriedades.setProperty("connectTimeout", "5000");
        propriedades.setProperty("socketTimeout", "10000");
        propriedades.setProperty("connectionTimeZone", "UTC");
        propriedades.setProperty("forceConnectionTimeZoneToSession", "true");
        propriedades.setProperty("sslMode", "REQUIRED");
        return DriverManager.getConnection(url, propriedades);
    }

    String url() { return url; }
    String usuario() { return usuario; }
    String senha() { return senha; }
}
