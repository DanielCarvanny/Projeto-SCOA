package br.edu.scoa.almoxarifado.aplicacao;

import br.edu.scoa.almoxarifado.dominio.*;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class GestaoEstoqueTest {
    @Test void exigeIdentidadeAntesDeRegistrarEUsaAutorRetornado() {
        var repo = new RepoFake();
        var acesso = (IdentidadeParaMovimentacao) sessao -> { assertSame("sessao", sessao); return 42; };
        var gestao = new GestaoEstoque(acesso, repo, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        var mov = gestao.entrada("sessao", 5, 2, "recebimento", "op-1");
        assertEquals(42, mov.usuarioAutorId());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), mov.instanteUtc());
        assertEquals("recebimento", mov.origem());
    }

    @Test void recusaQuantidadeInvalidaAntesDoRepositorio() {
        var repo = new RepoFake();
        var gestao = new GestaoEstoque(s -> 42, repo, Clock.systemUTC());
        assertThrows(IllegalArgumentException.class, () -> gestao.saida(new Object(), 1, 0, "teste", "op"));
        assertNull(repo.tipo);
    }

    @Test void normalizaEspacosLateraisDoIdentificadorEOrigem() {
        var repo = new RepoFake();
        var gestao = new GestaoEstoque(s -> 42, repo, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var comEspacos = gestao.entrada(new Object(), 1, 1, " recebimento ", " operacao-1 ");
        var normalizada = gestao.entrada(new Object(), 1, 1, "recebimento", "operacao-1");
        assertEquals("recebimento", comEspacos.origem());
        assertEquals("operacao-1", comEspacos.operacaoId());
        assertEquals(comEspacos.origem(), normalizada.origem());
        assertEquals(comEspacos.operacaoId(), normalizada.operacaoId());
    }

    private static final class RepoFake implements RepositorioMovimentacoesEstoque {
        TipoMovimentacao tipo;
        public MovimentacaoEstoque movimentar(long p, TipoMovimentacao t, int q, long a, Instant i, String o, String id) {
            tipo = t; return new MovimentacaoEstoque(1,p,t,q,a,i,o,id,q);
        }
        public SituacaoEstoque consultarSituacao(long p) { return new SituacaoEstoque(p,0,0,0,AlertaEstoque.NORMAL); }
    }
}
