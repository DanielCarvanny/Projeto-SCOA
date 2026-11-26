package br.edu.scoa.academico.curriculo;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.identidade.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.lang.reflect.Proxy;
import java.time.*;
import java.util.*;

public final class CenarioCatalogo implements RepositorioCatalogo {
    public final Clock relogio = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC);
    public Conta conta = new Conta(7, 3, "coord", Perfil.COORDENADOR, true, 1);
    public final ServicoIdentidade identidade;
    public final Sessao sessao;
    public final GestaoCatalogo gestao;
    public int gravacoes;
    public boolean acessouNaEdt;
    private final List<ItemCatalogo> itens = new ArrayList<>();
    private final List<RevisaoCatalogo> revisoes = new ArrayList<>();
    public CenarioCatalogo() {
        var repo = (RepositorioIdentidade) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{RepositorioIdentidade.class},
            (objeto, metodo, argumentos) -> switch (metodo.getName()) {
                case "autenticar" -> conta;
                case "conta" -> Optional.of(conta);
                case "registrarSaida" -> null;
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        identidade = new ServicoIdentidade(repo, null, relogio);
        sessao = identidade.entrar("coord", new char[0]);
        gestao = new GestaoCatalogo(identidade, sessao, this, relogio);
    }
    public synchronized ItemCatalogo salvar(ItemCatalogo item, long autor, Instant instante) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        if (itens.stream().anyMatch(i -> i.tipo() == item.tipo() && i.id() != item.id() && i.codigo().equals(item.codigo())))
            throw new FalhaCurriculo(FalhaCurriculo.Codigo.CODIGO_DUPLICADO);
        if (item.id() != 0 && itens.stream().noneMatch(i -> i.id() == item.id() && i.versao() == item.versao()))
            throw new FalhaCurriculo(FalhaCurriculo.Codigo.CONFLITO_EDICAO);
        var salvo = new ItemCatalogo(item.id() == 0 ? itens.size() + 1 : item.id(), item.tipo(), item.codigo(), item.nome(), item.ementa(), item.cargaHoraria(), item.estaAtivo(), item.versao() + 1);
        itens.removeIf(i -> i.id() == salvo.id()); itens.add(salvo);
        revisoes.add(new RevisaoCatalogo(salvo, autor, instante, UUID.randomUUID().toString())); gravacoes++;
        return salvo;
    }
    public synchronized List<ItemCatalogo> pesquisar(TipoCatalogo tipo, String termo) {
        acessouNaEdt |= javax.swing.SwingUtilities.isEventDispatchThread();
        return itens.stream().filter(i -> i.tipo() == tipo && (i.codigo().contains(termo.toUpperCase(Locale.ROOT)) || i.nome().contains(termo))).toList();
    }
    public synchronized List<RevisaoCatalogo> historico(TipoCatalogo tipo, long id) {
        return revisoes.stream().filter(r -> r.item().tipo() == tipo && r.item().id() == id).toList();
    }
    @Override public Optional<ItemCatalogo> consultar(TipoCatalogo tipo, long id) {
        return itens.stream().filter(item -> item.tipo() == tipo && item.id() == id).findFirst();
    }
}

