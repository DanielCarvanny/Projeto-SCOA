package br.edu.scoa.academico.infraestrutura.identidade;

import br.edu.scoa.academico.aplicacao.alunos.RepositorioGestaoAlunos;
import br.edu.scoa.academico.aplicacao.avaliacoes.AutorizacaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.identidade.ServicoIdentidade;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.aplicacao.professores.RepositorioProfessores;
import br.edu.scoa.academico.dominio.identidade.Perfil;
import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;

import static br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao.Codigo.NAO_AUTORIZADO;

public final class AutorizacaoAvaliacoesAcademica implements AutorizacaoAvaliacoes<Sessao> {
    private final ServicoIdentidade identidade;
    private final RepositorioGestaoAlunos alunos;
    private final RepositorioProfessores professores;

    public AutorizacaoAvaliacoesAcademica(ServicoIdentidade identidade, RepositorioGestaoAlunos alunos,
                                         RepositorioProfessores professores) {
        this.identidade = identidade;
        this.alunos = alunos;
        this.professores = professores;
    }

    @Override public long usuarioId(Sessao sessao) { return identidade.verificar(sessao).id(); }

    @Override public boolean ehCoordenador(Sessao sessao) {
        return identidade.verificar(sessao).perfil() == Perfil.COORDENADOR;
    }

    @Override public boolean ehProfessor(Sessao sessao) {
        return identidade.verificar(sessao).perfil() == Perfil.PROFESSOR;
    }

    @Override public boolean ehAluno(Sessao sessao) {
        return identidade.verificar(sessao).perfil() == Perfil.ALUNO;
    }

    @Override public long professorId(Sessao sessao) {
        var conta = identidade.verificar(sessao);
        if (conta.perfil() != Perfil.PROFESSOR) throw new FalhaAvaliacao(NAO_AUTORIZADO);
        return professores.consultarPorPessoa(conta.pessoaId())
                .map(professor -> (long) professor.id())
                .orElseThrow(() -> new FalhaAvaliacao(NAO_AUTORIZADO));
    }

    @Override public long alunoId(Sessao sessao) {
        var conta = identidade.verificar(sessao);
        if (conta.perfil() != Perfil.ALUNO) throw new FalhaAvaliacao(NAO_AUTORIZADO);
        return alunos.consultarPorPessoa(conta.pessoaId())
                .map(aluno -> (long) aluno.id())
                .orElseThrow(() -> new FalhaAvaliacao(NAO_AUTORIZADO));
    }
}
