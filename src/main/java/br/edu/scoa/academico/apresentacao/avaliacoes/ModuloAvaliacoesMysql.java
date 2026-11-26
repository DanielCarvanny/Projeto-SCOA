package br.edu.scoa.academico.apresentacao.avaliacoes;

import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.identidade.ServicoIdentidade;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.aplicacao.professores.RepositorioProfessores;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.infraestrutura.identidade.AutorizacaoAvaliacoesAcademica;
import br.edu.scoa.academico.infraestrutura.persistencia.*;
import java.time.Clock;
import java.time.ZoneId;

public final class ModuloAvaliacoesMysql {
    private ModuloAvaliacoesMysql() { }

    public static FluxoAvaliacoes<Sessao> abrir(ConfiguracaoBanco banco, ServicoIdentidade identidade,
            Sessao sessao, RepositorioAlunosMysql alunos, RepositorioProfessores professores,
            DialogosAluno dialogos) {
        var autorizacao = new AutorizacaoAvaliacoesAcademica(identidade, alunos, professores);
        var gestao = new GestaoAvaliacoes<>(autorizacao, new RepositorioAvaliacoesMysql(banco),
                new RepositorioTurmasMysql(banco), Clock.system(ZoneId.of("America/Sao_Paulo")));
        return new FluxoAvaliacoes<>(sessao, gestao, dialogos);
    }
}
