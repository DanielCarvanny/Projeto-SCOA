package br.edu.scoa.almoxarifado.infraestrutura.persistencia;

import br.edu.scoa.almoxarifado.aplicacao.*;
import br.edu.scoa.almoxarifado.dominio.*;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import org.junit.jupiter.api.*;
import java.time.Clock;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class MovimentacoesEstoqueMysqlIT {
    private static ConfiguracaoBanco banco;
    private GestaoEstoque gestao;
    private long produtoId;

    @BeforeAll static void configurar() {
        var env = System.getenv();
        if (!"CONFIRMO_BANCO_DESCARTAVEL".equals(env.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(env.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(env.get("SCOA_TEST_USER"))) throw new IllegalStateException("Exige banco exclusivo de teste");
        banco = ConfiguracaoBanco.doAmbiente(env, "SCOA_TEST_");
        MigracoesAlmoxarifado.configurar(banco).migrate();
    }

    @BeforeEach void preparar() throws Exception {
        try (var c=banco.abrir(); var s=c.createStatement()) {
            assertEquals("scoa_test", c.getCatalog());
            s.executeUpdate("DELETE FROM movimentacao_estoque"); s.executeUpdate("DELETE FROM produto"); s.executeUpdate("DELETE FROM localizacao");
        }
        var cadastros = new GestaoAlmoxarifado(new RepositorioAlmoxarifadoMysql(banco));
        var local = cadastros.cadastrarLocalizacao("Local", "");
        var produto = cadastros.cadastrarProduto("Item", TipoProduto.MATERIAL_CONSUMO, "UN", null, local.id(), 2, 5);
        produtoId = produto.id();
        gestao = new GestaoEstoque(s -> 77, new RepositorioMovimentacoesEstoqueMysql(banco),
        Clock.tick(Clock.systemUTC(), java.time.Duration.ofNanos(1000)));
    }

    @Test void entradaSaidaIdempotenciaAuditoriaESaldo() throws Exception {
        var entrada=gestao.entrada(new Object(), produtoId, 5, "recebimento", "op-entrada");
        assertEquals(5, entrada.saldoResultante());
        assertEquals(entrada, gestao.entrada(new Object(), produtoId, 5, "recebimento", "op-entrada"));
        var saida=gestao.saida(new Object(), produtoId, 2, "consumo", "op-saida");
        assertEquals(3, saida.saldoResultante());
        assertEquals(AlertaEstoque.NORMAL, gestao.consultarSituacao(produtoId).alerta());
        assertThrows(IllegalStateException.class, () -> gestao.entrada(new Object(), produtoId, 4, "outro", "op-entrada"));
        try (var c=banco.abrir(); var s=c.createStatement(); var r=s.executeQuery("SELECT usuario_autor_id, origem, instante_utc FROM movimentacao_estoque WHERE operacao_id='op-entrada'")) {
            assertTrue(r.next()); assertEquals(77,r.getLong(1)); assertEquals("recebimento",r.getString(2)); assertNotNull(r.getTimestamp(3));
        }
    }

    @Test void duasSaidasConcorrentesNaoGeramSaldoNegativo() throws Exception {
        gestao.entrada(new Object(), produtoId, 5, "recebimento", "conc-entrada");
        var inicio = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a=pool.submit(()->saidaConcorrente(inicio,"conc-saida-a"));
            var b=pool.submit(()->saidaConcorrente(inicio,"conc-saida-b"));
            inicio.countDown();
            int sucesso=(a.get()+b.get());
            assertEquals(1,sucesso);
            assertEquals(1,gestao.consultarSituacao(produtoId).saldo());
            try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT saldo FROM produto WHERE id="+produtoId)) { r.next(); assertEquals(1,r.getInt(1)); }
        } finally { pool.shutdownNow(); }
    }

    private int saidaConcorrente(CountDownLatch inicio,String operacao) {
        try { inicio.await(); gestao.saida(new Object(),produtoId,4,"consumo",operacao); return 1; }
        catch (IllegalStateException e) { return 0; }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }
}
