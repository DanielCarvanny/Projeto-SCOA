package br.edu.scoa.academico.apresentacao.diarios;

import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.diarios.GestaoAuditoriaDiario;
import br.edu.scoa.academico.aplicacao.identidade.ServicoIdentidade;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.aplicacao.professores.RepositorioProfessores;
import br.edu.scoa.academico.infraestrutura.diarios.ConsultaDiarioAulasIndisponivel;
import br.edu.scoa.academico.infraestrutura.identidade.AutorizacaoAuditoriaDiarioAcademica;
import br.edu.scoa.academico.infraestrutura.identidade.AutorizacaoAvaliacoesAcademica;
import br.edu.scoa.academico.infraestrutura.persistencia.ConfiguracaoBanco;
import br.edu.scoa.academico.infraestrutura.persistencia.RepositorioAlunosMysql;
import br.edu.scoa.academico.infraestrutura.persistencia.RepositorioAvaliacoesMysql;
import br.edu.scoa.academico.infraestrutura.persistencia.RepositorioPautasFinaisMysql;
import br.edu.scoa.academico.infraestrutura.persistencia.RepositorioTurmasMysql;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;

import java.time.Clock;
import java.time.ZoneId;

public final class ModuloAuditoriaDiarioMysql {
    private ModuloAuditoriaDiarioMysql() { }

    public static FluxoAuditoriaDiario<Sessao> abrir(ConfiguracaoBanco banco, ServicoIdentidade identidade,
            Sessao sessao, RepositorioAlunosMysql alunos, RepositorioProfessores professores,
            DialogosAluno dialogos) {
        var repositorioAvaliacoes = new RepositorioAvaliacoesMysql(banco);
        var gestaoAvaliacoes = new GestaoAvaliacoes<>(
                new AutorizacaoAvaliacoesAcademica(identidade, alunos, professores), repositorioAvaliacoes,
                new RepositorioTurmasMysql(banco), Clock.system(ZoneId.of("America/Sao_Paulo")));
        var gestao = new GestaoAuditoriaDiario<>(new AutorizacaoAuditoriaDiarioAcademica(identidade),
                new RepositorioPautasFinaisMysql(banco), repositorioAvaliacoes,
                new RepositorioTurmasMysql(banco), new ConsultaDiarioAulasIndisponivel(), Clock.systemUTC(),
                gestaoAvaliacoes);
        return new FluxoAuditoriaDiario<>(sessao, gestao, dialogos);
    }
}
