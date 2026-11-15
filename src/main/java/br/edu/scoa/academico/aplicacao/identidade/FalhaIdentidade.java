package br.edu.scoa.academico.aplicacao.identidade;
public final class FalhaIdentidade extends RuntimeException {
    public enum Codigo { CREDENCIAIS_INVALIDAS, SESSAO_INVALIDA, NAO_AUTORIZADO, LOGIN_INVALIDO, SENHA_INVALIDA, CONTA_DUPLICADA, PESSOA_INCOMPLETA, CONTA_NAO_ENCONTRADA, INICIALIZACAO_JA_REALIZADA, ULTIMO_COORDENADOR, CONFLITO, INDISPONIVEL, VINCULO_ALUNO_NECESSARIO, VINCULO_PROFESSOR_NECESSARIO }
    private final Codigo codigo;
    public FalhaIdentidade(Codigo codigo) { super(codigo.name());this.codigo=codigo; }
    public Codigo codigo(){return codigo;}
}
