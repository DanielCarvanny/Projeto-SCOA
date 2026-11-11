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
    public Aluno cadastrar(String cpf, String nome, String email, String telefone, SituacaoCadastral situacao) {
        autorizar();
        String documento = normalizarCpf(cpf);
        if (situacao == null) throw invalida();
        var existente = pessoas.consultar(usuarioId, documento);
        PessoaParaCadastro pessoa;
        if (existente.isPresent()) {
            pessoa = existente.get();
        } else {
            String nomeLimpo = limpar(nome), emailLimpo = limpar(email), telefoneLimpo = limpar(telefone);
            if (nomeLimpo.isEmpty() || nomeLimpo.length() > 200 || emailLimpo.length() > 254
                    || !emailLimpo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || telefoneLimpo.length() > 30) throw invalida();
            pessoa = pessoas.cadastrarOuObter(usuarioId, documento, nomeLimpo, emailLimpo, telefoneLimpo);
        }
        if (pessoa.pessoaId() <= 0 || !documento.equals(pessoa.cpf())) throw new IllegalStateException("Resposta institucional inconsistente");
        return cadastro.executar(usuarioId, pessoa.pessoaId(), situacao);
    }
    private void autorizar() {
        if (usuarioId <= 0 || !identidade.podeCadastrarAluno(usuarioId))
            throw new CadastroAlunoException(ErroCadastroAluno.NAO_AUTORIZADO);
    }
    private static String limpar(String valor) { return valor == null ? "" : valor.trim(); }
    private static CadastroAlunoException invalida() { return new CadastroAlunoException(ErroCadastroAluno.ENTRADA_INVALIDA); }
    private static String normalizarCpf(String entrada) {
        String valor = limpar(entrada);
        if (!valor.matches("[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}")) throw invalida();
        String cpf = valor.replace(".", "").replace("-", "");
        if (cpf.chars().distinct().count() == 1) throw invalida();
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            int digito = 11 - soma % 11;
            if (digito >= 10) digito = 0;
            if (digito != cpf.charAt(tamanho) - '0') throw invalida();
        }
        return cpf;
    }
}
