package br.edu.scoa.academico.apresentacao.boletins;

import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.boletins.GestaoBoletimAluno;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.aplicacao.identidade.ServicoIdentidade;
import br.edu.scoa.academico.aplicacao.professores.RepositorioProfessores;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.infraestrutura.identidade.AutorizacaoAvaliacoesAcademica;
import br.edu.scoa.academico.infraestrutura.persistencia.*;

import java.time.Clock;
import java.time.ZoneId;

public final class ModuloBoletimMysql {
    private ModuloBoletimMysql() { }

    public static FluxoBoletimAluno abrir(ConfiguracaoBanco banco, ServicoIdentidade identidade, Sessao sessao,
            RepositorioAlunosMysql alunos, RepositorioProfessores professores, DialogosAluno dialogos) {
        var autorizacao = new AutorizacaoAvaliacoesAcademica(identidade, alunos, professores);
        var repositorioAvaliacoes = new RepositorioAvaliacoesMysql(banco);
        var repositorioTurmas = new RepositorioTurmasMysql(banco);
        var gestaoAvaliacoes = new GestaoAvaliacoes<>(autorizacao, repositorioAvaliacoes,
                repositorioTurmas, Clock.system(ZoneId.of("America/Sao_Paulo")));
        var gestaoBoletim = new GestaoBoletimAluno<>(autorizacao, gestaoAvaliacoes, repositorioAvaliacoes,
                new RepositorioMatriculasMysql(banco), new RepositorioInscricoesMysql(banco),
                repositorioTurmas, new RepositorioMatrizesMysql(banco),
                new RepositorioCatalogoMysql(banco), new RepositorioPeriodosMysql(banco));
        return new FluxoBoletimAluno(sessao, gestaoBoletim, dialogos);
    }
}
