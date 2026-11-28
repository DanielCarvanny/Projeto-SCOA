package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoFlyway;

import org.flywaydb.core.Flyway;

public final class MigracoesAcademico {
    private MigracoesAcademico() {}

    public static Flyway configurar(ConfiguracaoBanco banco) {
        return ConfiguracaoFlyway.configurar(banco, "classpath:db/academico",
                "flyway_academico_schema_history");
    }
}
