package br.edu.scoa.academico.aplicacao.alunos;
public final class CadastroAlunoException extends RuntimeException {
    private final ErroCadastroAluno codigo;
    public CadastroAlunoException(ErroCadastroAluno codigo) {
        super(codigo.name());
        this.codigo = codigo;
    }
    public ErroCadastroAluno codigo() { return codigo; }
}
