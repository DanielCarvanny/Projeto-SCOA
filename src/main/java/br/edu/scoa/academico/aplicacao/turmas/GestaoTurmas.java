package br.edu.scoa.academico.aplicacao.turmas;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.turmas.*;
import java.time.*;
import java.util.*;
import static br.edu.scoa.academico.dominio.turmas.FalhaTurma.Codigo.*;

public final class GestaoTurmas {
    private final ServicoIdentidade identidade; private final Sessao sessao; private final RepositorioTurmas repositorio;
    private final RepositorioPeriodos periodos; private final RepositorioCatalogo catalogo; private final RepositorioProfessores professores; private final Clock relogio;
    public GestaoTurmas(ServicoIdentidade identidade,Sessao sessao,RepositorioTurmas repositorio,RepositorioPeriodos periodos,RepositorioCatalogo catalogo,RepositorioProfessores professores,Clock relogio){
        this.identidade=identidade;this.sessao=sessao;this.repositorio=repositorio;this.periodos=periodos;this.catalogo=catalogo;this.professores=professores;this.relogio=relogio;
    }
    private long autor(){if(!identidade.coordenador(sessao))throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);return sessao.usuarioId();}
    public Sala salvarSala(Sala sala,UUID operacao){return repositorio.salvarSala(sala,operacao,autor(),relogio.instant());}
    public List<Sala> pesquisarSalas(String termo){autor();return repositorio.pesquisarSalas(termo==null?"":termo.strip());}
    public Sala consultarSala(long id){autor();return repositorio.consultarSala(id).orElseThrow(()->new FalhaTurma(NAO_ENCONTRADO));}
    public List<PeriodoLetivo> listarPeriodos(){autor();return periodos.pesquisar("",null);}
    public List<ItemCatalogo> listarDisciplinas(){autor();return catalogo.pesquisar(TipoCatalogo.DISCIPLINA,"").stream().filter(ItemCatalogo::estaAtivo).toList();}
    public Optional<br.edu.scoa.academico.dominio.professores.Professor> consultarProfessor(long id){autor();return professores.consultar(id);}
    public Turma salvarTurma(Turma turma,UUID operacao){
        long autor=autor(); var periodo=periodos.consultar(turma.periodoId());
        if(periodo.situacao()==SituacaoPeriodoLetivo.ENCERRADO)throw new FalhaTurma(PERIODO_NAO_EDITAVEL);
        var disciplina=catalogo.pesquisar(TipoCatalogo.DISCIPLINA,"").stream().filter(i->i.id()==turma.disciplinaId()&&i.estaAtivo()).findFirst().orElseThrow(()->new FalhaTurma(TURMA_INVALIDA));
        if(disciplina.tipo()!=TipoCatalogo.DISCIPLINA||professores.consultar(turma.professorId()).isEmpty())throw new FalhaTurma(TURMA_INVALIDA);
        for(var horario:turma.horarios())if(horario.salaId()!=null){var sala=consultarSala(horario.salaId());validarSala(sala,turma);}
        return repositorio.salvarTurma(turma,operacao,autor,relogio.instant());
    }
    private void validarSala(Sala sala,Turma turma){if(sala.situacao()!=SituacaoSala.DISPONIVEL)throw new FalhaTurma(SALA_INDISPONIVEL);if(sala.capacidade()<turma.capacidade())throw new FalhaTurma(CAPACIDADE_INSUFICIENTE);}
    public Turma consultarTurma(long id){autor();return repositorio.consultarTurma(id).orElseThrow(()->new FalhaTurma(NAO_ENCONTRADO));}
    public List<Turma> pesquisarTurmas(long periodoId){autor();return repositorio.pesquisarTurmas(periodoId);}
    public Turma transicionar(Turma turma,SituacaoTurma destino,UUID operacao){
        long autor=autor(); if(turma==null||destino==null||!transicaoValida(turma.situacao(),destino))throw new FalhaTurma(TURMA_INVALIDA);
        return repositorio.transicionarTurma(turma.id(),turma.revisao(),destino,operacao,autor,relogio.instant());
    }
    public Turma alocarSala(Turma turma,HorarioTurma horario,Long salaId,UUID operacao){
        long autor=autor(); if(turma==null||horario==null||turma.situacao()==SituacaoTurma.ENCERRADA||turma.situacao()==SituacaoTurma.CANCELADA)throw new FalhaTurma(TURMA_INVALIDA);
        if(salaId!=null)validarSala(consultarSala(salaId),turma);
        return repositorio.alocarSala(turma.id(),horario.id(),turma.revisao(),horario.revisao(),salaId,operacao,autor,relogio.instant());
    }
    private static boolean transicaoValida(SituacaoTurma origem,SituacaoTurma destino){return (origem==SituacaoTurma.PLANEJADA&&(destino==SituacaoTurma.ABERTA||destino==SituacaoTurma.CANCELADA))||(origem==SituacaoTurma.ABERTA&&(destino==SituacaoTurma.EM_ANDAMENTO||destino==SituacaoTurma.CANCELADA))||(origem==SituacaoTurma.EM_ANDAMENTO&&destino==SituacaoTurma.ENCERRADA);}
}
