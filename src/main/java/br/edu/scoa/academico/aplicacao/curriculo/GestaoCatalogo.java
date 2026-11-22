package br.edu.scoa.academico.aplicacao.curriculo;

import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.Clock;
import java.util.List;

public final class GestaoCatalogo {
    private final ServicoIdentidade identidade;
    private final Sessao sessao;
    private final RepositorioCatalogo repositorio;
    private final Clock relogio;
    public GestaoCatalogo(ServicoIdentidade identidade, Sessao sessao, RepositorioCatalogo repositorio, Clock relogio) {
        this.identidade = identidade; this.sessao = sessao; this.repositorio = repositorio; this.relogio = relogio;
    }
    private long autor() {
        if (!identidade.coordenador(sessao)) throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);
        return sessao.usuarioId();
    }
    public ItemCatalogo salvar(TipoCatalogo tipo, ItemCatalogo anterior, String codigo, String nome, String ementa, int carga) {
        long autor = autor();
        if (tipo == null || (anterior != null && anterior.tipo() != tipo)) throw new IllegalArgumentException("Tipo de catálogo incompatível.");
        var item = validar(tipo, anterior, codigo, nome, ementa, carga);
        return repositorio.salvar(item, autor, relogio.instant());
    }
    public static ItemCatalogo validar(TipoCatalogo tipo, ItemCatalogo anterior, String codigo, String nome, String ementa, int carga) {
        if (tipo == TipoCatalogo.CURSO) {
            var curso = new Curso(codigo, nome, carga);
            codigo = curso.codigo(); nome = curso.nome(); ementa = "";
        } else {
            var disciplina = new Disciplina(codigo, nome, ementa, carga);
            codigo = disciplina.codigo(); nome = disciplina.nome(); ementa = disciplina.ementa();
        }
        return new ItemCatalogo(anterior == null ? 0 : anterior.id(), tipo, codigo, nome, ementa, carga,
                anterior == null || anterior.estaAtivo(), anterior == null ? 0 : anterior.versao());
    }
    public ItemCatalogo inativar(ItemCatalogo anterior) {
        long autor = autor();
        if (anterior == null) throw new FalhaCurriculo(FalhaCurriculo.Codigo.NAO_ENCONTRADO);
        return repositorio.salvar(new ItemCatalogo(anterior.id(), anterior.tipo(), anterior.codigo(), anterior.nome(),
                anterior.ementa(), anterior.cargaHoraria(), false, anterior.versao()), autor, relogio.instant());
    }
    public List<ItemCatalogo> pesquisar(TipoCatalogo tipo, String termo) {
        autor(); return repositorio.pesquisar(tipo, termo == null ? "" : termo.strip());
    }
    public List<RevisaoCatalogo> historico(ItemCatalogo item) {
        autor(); return repositorio.historico(item.tipo(), item.id());
    }
}

