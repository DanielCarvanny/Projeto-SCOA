package br.edu.scoa.fundacao.persistencia;

import br.edu.scoa.academico.infraestrutura.persistencia.MigracoesAcademico;
import br.edu.scoa.academico.infraestrutura.persistencia.MigracoesAplicacao;
import br.edu.scoa.academico.infraestrutura.persistencia.MigracoesIdentidade;
import br.edu.scoa.administrativo.infraestrutura.persistencia.MigracoesAdministrativo;
import br.edu.scoa.almoxarifado.infraestrutura.persistencia.MigracoesAlmoxarifado;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MigracoesDosModulosDMySqlIT {
    static ConfiguracaoBanco banco;

    @BeforeAll
    static void configurar() {
        var ambiente = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(ambiente.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(ambiente.get("SCOA_TEST_USER"))) {
            throw new IllegalStateException("Exige banco exclusivo de teste");
        }
        banco = ConfiguracaoBanco.doAmbiente(ambiente, "SCOA_TEST_");
    }

    @BeforeEach
    void limparBancoDescartavel() throws Exception {
        try (var conexao = banco.abrir(); var comando = conexao.createStatement()) {
            assertEquals("scoa_test", conexao.getCatalog());
            comando.execute("SET FOREIGN_KEY_CHECKS = 0");
            var tabelas = new ArrayList<String>();
            try (ResultSet resultado = comando.executeQuery("SELECT table_name FROM information_schema.tables "
                    + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'")) {
                while (resultado.next()) tabelas.add(resultado.getString(1));
            }
            for (String tabela : tabelas) comando.execute("DROP TABLE `" + tabela.replace("`", "``") + "`");
            comando.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    @Test
    void instalaEmBancoLimpoComHistoricosIndependentesEReexecucaoIdempotente() throws Exception {
        MigracoesAlmoxarifado.configurar(banco).migrate();
        MigracoesAdministrativo.configurar(banco).migrate();

        assertEquals(2, MigracoesAlmoxarifado.configurar(banco).info().applied().length);
        assertEquals(2, MigracoesAdministrativo.configurar(banco).info().applied().length);
        assertEquals(0, MigracoesAlmoxarifado.configurar(banco).migrate().migrationsExecuted);
        assertEquals(0, MigracoesAdministrativo.configurar(banco).migrate().migrationsExecuted);
        try (var conexao = banco.abrir(); var comando = conexao.createStatement();
             var resultado = comando.executeQuery("SELECT COUNT(*) FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE() AND table_name IN "
                     + "('funcionario', 'setor', 'centro_custo')")) {
            resultado.next();
            assertEquals(0, resultado.getInt(1));
        }
    }

    @Test
    void instalaEmBancoExistenteSemInterferirNoHistoricoAcademico() {
        MigracoesIdentidade.executar(banco);
        var academico = MigracoesAcademico.configurar(banco);
        if (academico.info().applied().length == 0) {
            MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        }
        academico.migrate();
        var migracoesAcademicasAntes = academico.info().applied().length;
        MigracoesAlmoxarifado.configurar(banco).migrate();
        MigracoesAdministrativo.configurar(banco).migrate();

        assertEquals(migracoesAcademicasAntes, academico.info().applied().length);
        assertEquals(0, academico.migrate().migrationsExecuted);
        assertEquals(0, MigracoesAlmoxarifado.configurar(banco).migrate().migrationsExecuted);
        assertEquals(0, MigracoesAdministrativo.configurar(banco).migrate().migrationsExecuted);
        try (var conexao = banco.abrir(); var comando = conexao.createStatement();
             var resultado = comando.executeQuery("SELECT COUNT(*) FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE() AND table_name IN "
                     + "('flyway_academico_schema_history', 'flyway_almoxarifado_schema_history', "
                     + "'flyway_administrativo_schema_history')")) {
            resultado.next();
            assertEquals(3, resultado.getInt(1));
        } catch (Exception e) {
            throw new AssertionError("Não foi possível verificar os históricos Flyway independentes", e);
        }
    }
}
