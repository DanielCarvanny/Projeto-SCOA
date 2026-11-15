package br.edu.scoa.academico.apresentacao.identidade;
import br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade;
public final class MensagensIdentidade {
    private MensagensIdentidade(){}
    public static String erro(FalhaIdentidade falha){return switch(falha.codigo()){
        case CREDENCIAIS_INVALIDAS -> "Não foi possível entrar. Confira as credenciais; após tentativas repetidas, aguarde 5 minutos.";
        case SESSAO_INVALIDA -> "Sua sessão terminou. Entre novamente.";
        case NAO_AUTORIZADO -> "Você não tem permissão para esta operação.";
        case LOGIN_INVALIDO -> "Login: use de 3 a 100 caracteres, com letras sem acento, números, ponto, @, hífen ou sublinhado.";
        case SENHA_INVALIDA -> "A senha deve ter entre 8 e 128 caracteres.";
        case CONTA_DUPLICADA -> "Esta pessoa ou este login já possui uma conta.";
        case PESSOA_INCOMPLETA -> "Pessoa ausente ou cadastro antigo incompleto. Solicite regularização ao responsável.";
        case CONTA_NAO_ENCONTRADA -> "Conta não encontrada.";
        case INICIALIZACAO_JA_REALIZADA -> "A conta inicial já foi provisionada. Utilize um coordenador existente.";
        case ULTIMO_COORDENADOR -> "Mantenha pelo menos uma conta de coordenador ativa.";
        case CONFLITO -> "Este registro mudou. Consulte novamente antes de editar.";
        case VINCULO_ALUNO_NECESSARIO -> "Cadastre o vínculo completo em Alunos antes de atribuir o perfil Aluno.";
        case VINCULO_PROFESSOR_NECESSARIO -> "Cadastre ou complete o vínculo em Professores, incluindo a titulação, antes de atribuir o perfil Professor.";
        case INDISPONIVEL -> "Serviço indisponível. Tente novamente.";
    };}
}
