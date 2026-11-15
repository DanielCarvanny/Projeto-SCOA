package br.edu.scoa.academico.aplicacao.alunos;

import br.edu.scoa.academico.dominio.alunos.*;
import java.util.Optional;

public final class CadastroAlunoPorCpf {
    private final long usuarioId;
    private final IdentidadeInstitucional identidade;
    private final PessoasPorCpf pessoas;
    private final CadastrarAluno cadastro;
    public CadastroAlunoPorCpf(long usuarioId, IdentidadeInstitucional identidade, PessoasPorCpf pessoas, CadastrarAluno cadastro) {
        this.usuarioId = usuarioId; this.identidade = identidade; this.pessoas = pessoas; this.cadastro = cadastro;
    }
    public Optional<PessoaParaCadastro> consultar(String cpf) {
        autorizar();
        return pessoas.consultar(usuarioId, normalizarCpf(cpf));
    }
    /** Compatibilidade: dados pessoais não são mais criados ou modificados por este fluxo. */
    public Aluno cadastrar(String cpf, String nome, String email, String telefone, SituacaoCadastral situacao) {
        return cadastrar(cpf, nome, email, telefone, situacao, null);
    }
    /** Compatibilidade: dados pessoais não são mais criados ou modificados por este fluxo. */
    public Aluno cadastrar(String cpf, String nome, String email, String telefone, SituacaoCadastral situacao, Boolean pessoaNova) {
        autorizar();
        String documento = normalizarCpf(cpf);
        if (situacao == null) throw invalida();
        var existente = pessoas.consultar(usuarioId, documento);
        var pessoa = existente.orElseThrow(() -> new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA));
        if (pessoa.pessoaId() <= 0 || !documento.equals(pessoa.cpf())) throw new IllegalStateException("Resposta institucional inconsistente");
        return cadastro.executar(usuarioId, pessoa.pessoaId(), situacao);
    }
    public FichaAluno consultarAluno(String cpf, GestaoFichaAluno fichas) {
        return fichas.consultarPorCpf(cpf, pessoas);
    }
    private void autorizar() {
        if (usuarioId <= 0 || !identidade.podeCadastrarAluno(usuarioId))
            throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
    }
    private static String limpar(String valor) { return valor == null ? "" : valor.trim(); }
    private static CadastroAlunoException invalida() { return new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA); }
    private static String normalizarCpf(String entrada) { return ValidacaoPessoa.cpf(entrada); }
}
