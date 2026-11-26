package br.edu.scoa.academico.infraestrutura.identidade;

import br.edu.scoa.academico.aplicacao.diarios.AutorizacaoAuditoriaDiario;
import br.edu.scoa.academico.aplicacao.identidade.ServicoIdentidade;
import br.edu.scoa.academico.aplicacao.identidade.Sessao;
import br.edu.scoa.academico.dominio.identidade.Perfil;
import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;

import java.util.Objects;

public final class AutorizacaoAuditoriaDiarioAcademica implements AutorizacaoAuditoriaDiario<Sessao> {
    private final ServicoIdentidade identidade;

    public AutorizacaoAuditoriaDiarioAcademica(ServicoIdentidade identidade) {
        this.identidade = Objects.requireNonNull(identidade);
    }

    @Override public long usuarioId(Sessao sessao) {
        try { return identidade.verificar(sessao).id(); }
        catch (RuntimeException erro) { throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.NAO_AUTORIZADO); }
    }

    @Override public boolean ehCoordenador(Sessao sessao) {
        try { return identidade.verificar(sessao).perfil() == Perfil.COORDENADOR; }
        catch (RuntimeException erro) { return false; }
    }
}
