package br.edu.scoa.academico.infraestrutura.persistencia;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoBancoTest {
    @Test void exigeConfiguracaoSemExporSegredo() {
        var erro = assertThrows(IllegalArgumentException.class,
                () -> ConfiguracaoBanco.doAmbiente(Map.of("SCOA_DB_PASSWORD", "segredo"), "SCOA_DB_"));
        assertFalse(erro.getMessage().contains("segredo"));
    }
    @Test void naoAceitaOutroBancoNemSenhaVazia() {
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoBanco("jdbc:h2:test", "usuario", "senha"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoBanco("jdbc:mysql://localhost/scoa", "usuario", ""));
    }
    @Test void representacaoNaoExibeCredenciais() {
        var config = ConfiguracaoBanco.doAmbiente(Map.of("SCOA_DB_URL", "jdbc:mysql://localhost/scoa",
                "SCOA_DB_USER", "operador", "SCOA_DB_PASSWORD", "segredo"), "SCOA_DB_");
        assertFalse(config.toString().contains("segredo"));
        assertFalse(config.toString().contains("operador"));
    }
}
