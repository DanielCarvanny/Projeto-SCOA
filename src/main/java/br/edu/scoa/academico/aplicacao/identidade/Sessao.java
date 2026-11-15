package br.edu.scoa.academico.aplicacao.identidade;
import br.edu.scoa.academico.dominio.identidade.Conta;
import java.time.Instant;
/** Capacidade opaca: somente o serviço cria sessões e valida sua presença no registro. */
public final class Sessao {
    private final Conta conta;
    private final Instant expiraEm;
    Sessao(Conta conta,Instant expiraEm){this.conta=conta;this.expiraEm=expiraEm;}
    public long usuarioId(){return conta.id();}
    public long pessoaId(){return conta.pessoaId();}
    Conta conta(){return conta;}
    Instant expiraEm(){return expiraEm;}
    @Override public String toString(){return "Sessao[protegida]";}
}
