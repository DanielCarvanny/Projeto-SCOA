package br.edu.scoa.almoxarifado.infraestrutura.persistencia;

import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoFlyway;
import org.flywaydb.core.Flyway;


public final class MigracoesAlmoxarifado {
    private MigracoesAlmoxarifado() {}

    public static Flyway configurar(ConfiguracaoBanco banco) {
        return ConfiguracaoFlyway.configurar(banco, "classpath:db/almoxarifado",
                "flyway_almoxarifado_schema_history");
    }
}
