package br.edu.scoa.administrativo.infraestrutura.persistencia;

import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoFlyway;
import org.flywaydb.core.Flyway;

public final class MigracoesAdministrativo {
    private MigracoesAdministrativo() {}

    public static Flyway configurar(ConfiguracaoBanco banco) {
        return ConfiguracaoFlyway.configurar(banco, "classpath:db/administrativo",
                "flyway_administrativo_schema_history");
    }
}
