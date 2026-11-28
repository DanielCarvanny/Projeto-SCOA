package br.edu.scoa.fundacao.persistencia;

import org.flywaydb.core.Flyway;

/** Configuração comum de Flyway; cada módulo mantém localização e histórico próprios. */
public final class ConfiguracaoFlyway {
    private ConfiguracaoFlyway() {}

    public static Flyway configurar(ConfiguracaoBanco banco, String localizacao, String tabelaHistorico) {
        return banco.configurarFlyway(localizacao, tabelaHistorico);
    }
}
