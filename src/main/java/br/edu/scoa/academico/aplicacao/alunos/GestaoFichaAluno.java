package br.edu.scoa.academico.aplicacao.alunos;

public final class GestaoFichaAluno {
    private final long usuarioId;
    private final ConsultarAluno consulta;
    private final AcessoAlunos acesso;
    private final DadosPessoaisInstitucionais pessoas;
    public GestaoFichaAluno(long usuarioId, ConsultarAluno consulta, AcessoAlunos acesso, DadosPessoaisInstitucionais pessoas) {
        this.usuarioId = usuarioId; this.consulta = consulta; this.acesso = acesso; this.pessoas = pessoas;
    }
    public FichaAluno consultar(String alunoId) {
        long id;
        try { id = Long.parseLong(alunoId == null ? "" : alunoId.trim()); }
        catch (NumberFormatException falha) { throw invalida(); }
        var aluno = consulta.executar(usuarioId, id);
        var pessoa = pessoas.consultarPorId(usuarioId, aluno.pessoaId()).orElseThrow(
                () -> new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA));
        conferir(aluno.pessoaId(), pessoa);
        return new FichaAluno(aluno, pessoa);
    }
    public FichaAluno consultarPorCpf(String cpf, PessoasPorCpf identidade) {
        String documento = ValidacaoPessoa.cpf(cpf);
        if (usuarioId <= 0) throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
        // O provedor autoriza a consulta institucional; a autorização por aluno ocorre abaixo.
        var pessoa = identidade.consultar(usuarioId, documento).orElseThrow(
                () -> new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA));
        if (pessoa.pessoaId() <= 0 || !documento.equals(pessoa.cpf()))
            throw new FalhaDadosPessoais(FalhaDadosPessoais.Codigo.RESPOSTA_INCONSISTENTE);
        var aluno = consulta.porPessoa(usuarioId, pessoa.pessoaId());
        return new FichaAluno(aluno, pessoa);
    }
    public FichaAluno atualizar(FichaAluno esperada, String nome, String email, String telefone) {
        if (esperada == null || esperada.aluno() == null || esperada.pessoa() == null) throw invalida();
        var aluno = consulta.executar(usuarioId, esperada.aluno().id());
        if (!acesso.podeAtualizarDadosPessoais(usuarioId, aluno.id())) throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
        conferir(aluno.pessoaId(), esperada.pessoa());
        String nomeLimpo = limpar(nome), emailLimpo = limpar(email), telefoneLimpo = limpar(telefone);
        ValidacaoPessoa.dados(nomeLimpo, emailLimpo, telefoneLimpo);
        var pessoa = pessoas.atualizar(usuarioId, esperada.pessoa(), nomeLimpo, emailLimpo, telefoneLimpo);
        conferir(aluno.pessoaId(), pessoa);
        if (!esperada.pessoa().cpf().equals(pessoa.cpf())) throw new FalhaDadosPessoais(FalhaDadosPessoais.Codigo.RESPOSTA_INCONSISTENTE);
        return new FichaAluno(aluno, pessoa);
    }
    private static void conferir(long id, PessoaParaCadastro pessoa) {
        if (pessoa == null || pessoa.pessoaId() != id) throw new FalhaDadosPessoais(FalhaDadosPessoais.Codigo.RESPOSTA_INCONSISTENTE);
    }
    private static String limpar(String valor) { return valor == null ? "" : valor.trim(); }
    private static CadastroAlunoException invalida() { return new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA); }
}
