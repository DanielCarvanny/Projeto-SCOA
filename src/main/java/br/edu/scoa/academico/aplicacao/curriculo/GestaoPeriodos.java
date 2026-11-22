package br.edu.scoa.academico.aplicacao.curriculo;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.*;
import java.util.*;
public final class GestaoPeriodos {
    private final ServicoIdentidade identidade;private final Sessao sessao;
    private final RepositorioPeriodos repositorio;private final Clock relogio;
    public GestaoPeriodos(ServicoIdentidade identidade,Sessao sessao,RepositorioPeriodos repositorio,Clock relogio){
        this.identidade=identidade;this.sessao=sessao;this.repositorio=repositorio;this.relogio=relogio;
    }
    private long autor(){if(!identidade.coordenador(sessao))throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);return sessao.usuarioId();}
    public PeriodoLetivo salvar(PeriodoLetivo p,UUID operacao){long autor=autor();p.exigirEditavel();return repositorio.salvar(p,operacao,autor,relogio.instant());}
    public PeriodoLetivo consultar(long id){autor();return repositorio.consultar(id);}
    public List<PeriodoLetivo> pesquisar(String descricao,SituacaoPeriodoLetivo situacao){autor();return repositorio.pesquisar(descricao==null?"":descricao.strip(),situacao);}
    public PeriodoLetivo abrir(PeriodoLetivo p){long autor=autor();p.abrir();return repositorio.transicionar(p.id(),p.revisao(),SituacaoPeriodoLetivo.ABERTO,autor,relogio.instant());}
    public PeriodoLetivo encerrar(PeriodoLetivo p){long autor=autor();p.encerrar();return repositorio.transicionar(p.id(),p.revisao(),SituacaoPeriodoLetivo.ENCERRADO,autor,relogio.instant());}
}
