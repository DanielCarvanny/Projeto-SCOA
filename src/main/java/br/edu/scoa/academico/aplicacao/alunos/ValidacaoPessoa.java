package br.edu.scoa.academico.aplicacao.alunos;

/** Validação local anterior a qualquer chamada institucional. */
public final class ValidacaoPessoa {
    private ValidacaoPessoa() { }
    public static String cpf(String entrada) {
        String valor = limpar(entrada);
        if (!valor.matches("[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}")) throw falha(ErroCadastroAluno.CPF_INVALIDO);
        String cpf = valor.replace(".", "").replace("-", "");
        if (cpf.chars().distinct().count() == 1) throw falha(ErroCadastroAluno.CPF_INVALIDO);
        return cpf;
    }
    public static void dados(String nome, String email, String telefone) {
        if (limpar(nome).isEmpty() || limpar(nome).length() > 200) throw falha(ErroCadastroAluno.NOME_INVALIDO);
        if (limpar(email).length() > 254 || !limpar(email).matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw falha(ErroCadastroAluno.EMAIL_INVALIDO);
        if (limpar(telefone).length() > 30) throw falha(ErroCadastroAluno.TELEFONE_INVALIDO);
    }
    public static String limpar(String valor) { return valor == null ? "" : valor.trim(); }
    private static CadastroAlunoException falha(ErroCadastroAluno codigo) { return new CadastroAlunoException(codigo); }
}
