package br.edu.scoa.academico.apresentacao.alunos;

import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.alunos.*;

/** Adapta o formulário aos casos de uso; não conhece Swing nem JDBC. */
public final class ControladorAlunos {
    private final long usuarioId;
    private final CadastrarAluno cadastrar;
    private final ConsultarAluno consultar;
    private final AtualizarAluno atualizar;

    public ControladorAlunos(long usuarioId, CadastrarAluno cadastrar,
                             ConsultarAluno consultar, AtualizarAluno atualizar) {
        if (usuarioId <= 0) throw new IllegalArgumentException("Operador autenticado obrigatório.");
        this.usuarioId = usuarioId;
        this.cadastrar = cadastrar;
        this.consultar = consultar;
        this.atualizar = atualizar;
    }
    public Aluno cadastrar(String pessoaId, SituacaoCadastral situacao) {
        return cadastrar.executar(usuarioId, identificador(pessoaId), situacao);
    }
    public Aluno consultar(String alunoId) {
        return consultar.executar(usuarioId, identificador(alunoId));
    }
    public Aluno atualizar(String alunoId, SituacaoCadastral situacao) {
        return atualizar.executar(usuarioId, identificador(alunoId), situacao);
    }
    public static String mensagemErro(Throwable erro) {
        if (erro instanceof br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade falha)
            return br.edu.scoa.academico.apresentacao.identidade.MensagensIdentidade.erro(falha);
        if (erro instanceof FalhaDadosPessoais falha) {
            return switch (falha.codigo()) {
                case CONFLITO_EDICAO -> "Os dados foram alterados por outra operação. Volte à busca e consulte novamente antes de editar.";
                case RESPOSTA_INCONSISTENTE -> "Não foi possível confirmar a identidade institucional. Consulte o responsável.";
            };
        }
        if (erro instanceof CadastroAlunoException falha) {
            return switch (falha.codigo()) {
                case CPF_INVALIDO -> "CPF inválido. Informe 11 números válidos, com ou sem pontuação.";
                case NOME_INVALIDO -> "Informe o nome completo, com até 200 caracteres.";
                case EMAIL_INVALIDO -> "Informe um e-mail válido, com até 254 caracteres.";
                case TELEFONE_INVALIDO -> "O telefone deve ter até 30 caracteres.";
                case PESSOA_JA_CADASTRADA -> "Este CPF já possui cadastro. Volte e escolha Usar uma pessoa já cadastrada.";
                case ENTRADA_INVALIDA -> "Informe identificadores positivos e uma situação cadastral.";
                case NAO_AUTORIZADO -> "Você não tem permissão para esta operação.";
                case PESSOA_NAO_ENCONTRADA -> "Pessoa não cadastrada. Cadastre-a em Pessoas antes de criar este vínculo.";
                case ALUNO_JA_CADASTRADO -> "Esta pessoa já possui cadastro de aluno.";
                case ALUNO_NAO_ENCONTRADO -> "Aluno não encontrado. A pessoa pode ainda não possuir vínculo de aluno.";
            };
        }
        return "Não foi possível concluir a operação. Tente novamente ou contate o responsável.";
    }
    private static long identificador(String entrada) {
        try {
            long valor = Long.parseLong(entrada == null ? "" : entrada.trim());
            if (valor > 0) return valor;
        } catch (NumberFormatException ignorada) {
            // Formato inválido vira erro público estável, sem detalhes técnicos.
        }
        throw new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA);
    }
}
