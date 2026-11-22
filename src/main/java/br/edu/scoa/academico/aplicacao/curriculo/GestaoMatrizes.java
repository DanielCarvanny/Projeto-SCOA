package br.edu.scoa.academico.aplicacao.curriculo;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.Clock;
import java.util.List;

public final class GestaoMatrizes {
    private final ServicoIdentidade identidade;
    private final Sessao sessao;
    private final RepositorioMatrizes matrizes;
    private final RepositorioCatalogo catalogo;
    private final Clock relogio;
    public GestaoMatrizes(ServicoIdentidade identidade,Sessao sessao,RepositorioMatrizes matrizes,RepositorioCatalogo catalogo,Clock relogio) {
        this.identidade=identidade;this.sessao=sessao;this.matrizes=matrizes;this.catalogo=catalogo;this.relogio=relogio;
    }
    private long autor() {
        if(!identidade.coordenador(sessao)) throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);
        return sessao.usuarioId();
    }
    public MatrizCurricular salvar(MatrizCurricular matriz) {
        long autor=autor();matriz.exigirEditavel();
        return matrizes.salvar(matriz,autor,relogio.instant());
    }
    public List<MatrizCurricular> pesquisar(long cursoId,String termo) {
        autor();return matrizes.pesquisar(cursoId,termo==null?"":termo.strip());
    }
    public MatrizCurricular consultar(long id) { autor();return matrizes.consultar(id); }
    public MatrizCurricular publicar(MatrizCurricular matriz){
        long operador=autor();matriz.exigirEditavel();
        return matrizes.publicar(matriz.id(),matriz.revisao(),operador,relogio.instant());
    }
    public MatrizCurricular copiar(MatrizCurricular origem,String codigo,String versao,java.time.LocalDate inicio,java.time.LocalDate fim){
        long operador=autor();origem.novaVersao(codigo,versao,inicio,fim);
        return matrizes.copiar(origem.id(),origem.revisao(),codigo,versao,inicio,fim,operador,relogio.instant());
    }
    public List<ItemCatalogo> catalogo(TipoCatalogo tipo) { autor();return catalogo.pesquisar(tipo,""); }
    public ItemCatalogo disciplina(long id,long revisao) {
        autor();
        return catalogo.historico(TipoCatalogo.DISCIPLINA,id).stream().map(RevisaoCatalogo::item)
            .filter(i->i.versao()==revisao).findFirst().orElseThrow(()->new FalhaMatriz(FalhaMatriz.Codigo.REFERENCIA_INEXISTENTE));
    }
}

