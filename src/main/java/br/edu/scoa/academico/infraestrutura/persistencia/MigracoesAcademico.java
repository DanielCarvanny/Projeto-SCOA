package br.edu.scoa.academico.infraestrutura.persistencia;

import org.flywaydb.core.Flyway;

public final class MigracoesAcademico {
    private MigracoesAcademico() {}

    public static Flyway configurar(ConfiguracaoBanco banco) {
        return Flyway.configure()
                .dataSource(banco.url(), banco.usuario(), banco.senha())
                .locations("classpath:db/academico")
                .table("flyway_academico_schema_history")
                .cleanDisabled(true)
                .load();
    }
}
