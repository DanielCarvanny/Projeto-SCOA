package br.edu.scoa.almoxarifado.infraestrutura.persistencia;

import br.edu.scoa.almoxarifado.aplicacao.GestaoAlmoxarifado;
import br.edu.scoa.almoxarifado.dominio.TipoProduto;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioAlmoxarifadoMysqlIT {
    private static ConfiguracaoBanco banco;
    private GestaoAlmoxarifado gestao;

    @BeforeAll static void configurar() {
        var ambiente = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(ambiente.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(ambiente.get("SCOA_TEST_USER"))) throw new IllegalStateException("Exige banco exclusivo de teste");
        banco = ConfiguracaoBanco.doAmbiente(ambiente, "SCOA_TEST_");
        MigracoesAlmoxarifado.configurar(banco).migrate();
    }

    @BeforeEach void limpar() throws Exception {
        try (var conexao = banco.abrir(); var comando = conexao.createStatement()) {
            assertEquals("scoa_test", conexao.getCatalog());
            comando.executeUpdate("DELETE FROM produto");
            comando.executeUpdate("DELETE FROM localizacao");
        }
        gestao = new GestaoAlmoxarifado(new RepositorioAlmoxarifadoMysql(banco));
    }

    @Test void cadastraEConsultaProdutoELocalizacaoPersistidos() {
        var local = gestao.cadastrarLocalizacao("Depósito A", "Prateleira azul");
        var produto = gestao.cadastrarProduto("Papel A4", TipoProduto.MATERIAL_CONSUMO,
                "un", "PAPELA401", local.id(), 2, 30);
        var novoGestao = new GestaoAlmoxarifado(new RepositorioAlmoxarifadoMysql(banco));
        assertEquals(local, novoGestao.consultarLocalizacao(local.id()));
        assertEquals(produto, novoGestao.consultarProduto(produto.id()));
        assertEquals(1, novoGestao.consultarProdutos().size());
    }

    @Test void codigoEhOpcionalMasNaoPodeDuplicarEntreAtivos() {
        var local = gestao.cadastrarLocalizacao("Depósito", "");
        gestao.cadastrarProduto("Produto 1", TipoProduto.MATERIAL_CONSUMO, "UN", null, local.id(), 0, 0);
        gestao.cadastrarProduto("Produto 2", TipoProduto.MATERIAL_PERMANENTE, "PC", null, local.id(), 0, 0);
        gestao.cadastrarProduto("Produto 3", TipoProduto.MATERIAL_CONSUMO, "UN", " abc1 ", local.id(), 0, 0);
        assertThrows(IllegalStateException.class, () -> gestao.cadastrarProduto("Produto 4",
                TipoProduto.MATERIAL_CONSUMO, "UN", "ABC1", local.id(), 0, 0));
    }

    @Test void rejeitaLocalizacaoAusenteEIdentificadorInexistente() {
        assertThrows(IllegalArgumentException.class, () -> gestao.cadastrarProduto("Item",
                TipoProduto.MATERIAL_CONSUMO, "UN", null, 999999, 0, 0));
        assertTrue(gestao.consultarProdutos().isEmpty());
    }
}
