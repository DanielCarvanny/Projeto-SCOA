package br.edu.scoa.academico.aplicacao.identidade;
import br.edu.scoa.academico.aplicacao.alunos.PessoaParaCadastro;
import br.edu.scoa.academico.dominio.identidade.*;
import java.time.Instant;
import java.util.Optional;
public interface RepositorioIdentidade {
    Optional<PessoaParaCadastro> pessoaPorCpf(String cpf);
    Optional<PessoaParaCadastro> pessoaPorId(long id);
    PessoaParaCadastro criarPessoa(String cpf,String nome,String email,String telefone,long autor);
    PessoaParaCadastro atualizarPessoa(PessoaParaCadastro esperada,String nome,String email,String telefone,long autor);
    Conta criarConta(long pessoaId,String login,String hash,Perfil perfil,long autor);
    VinculosAcademicos vinculos(long pessoaId);
    Optional<Conta> conta(long id);
    Optional<Conta> contaPorPessoa(long pessoaId);
    Conta atualizarConta(Conta esperada,Perfil perfil,boolean ativa,long autor);
    /** Bloqueio persistente e comparação de senha dentro da transação. */
    Conta autenticar(String login,char[] senha,Instant agora);
    void registrarSaida(long autor);
    /** Apenas ferramenta local explícita; rejeita se qualquer conta já existe. */
    Conta inicializar(String cpf,String nome,String email,String login,String hash);
}
