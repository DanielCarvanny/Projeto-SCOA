package br.edu.scoa.academico.aplicacao.identidade;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.identidade.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static br.edu.scoa.academico.aplicacao.identidade.FalhaIdentidade.Codigo.*;
public final class ServicoIdentidade {
    private final RepositorioIdentidade repositorio;
    private final Senhas senhas;
    private final Clock relogio;
    private final Set<Sessao> sessoes=ConcurrentHashMap.newKeySet();
    public ServicoIdentidade(RepositorioIdentidade repositorio,Senhas senhas,Clock relogio){this.repositorio=repositorio;this.senhas=senhas;this.relogio=relogio;}
    public Sessao entrar(String login,char[] senha){
        String normalizado;
        try{normalizado=login(login);}catch(FalhaIdentidade e){throw new FalhaIdentidade(CREDENCIAIS_INVALIDAS);}
        if(senha==null||senha.length>128)throw new FalhaIdentidade(CREDENCIAIS_INVALIDAS);
        var conta=repositorio.autenticar(normalizado,senha,relogio.instant());
        sessoes.removeIf(s->!relogio.instant().isBefore(s.expiraEm()));
        var sessao=new Sessao(conta,relogio.instant().plus(Duration.ofMinutes(30)));sessoes.add(sessao);return sessao;
    }
    public Conta verificar(Sessao sessao){
        if(sessao==null||!sessoes.contains(sessao)||!relogio.instant().isBefore(sessao.expiraEm())){if(sessao!=null)sessoes.remove(sessao);throw new FalhaIdentidade(SESSAO_INVALIDA);}
        var atual=repositorio.conta(sessao.usuarioId()).orElseThrow(()->new FalhaIdentidade(SESSAO_INVALIDA));
        if(!atual.estaAtiva()||atual.versao()!=sessao.conta().versao()){sessoes.remove(sessao);throw new FalhaIdentidade(SESSAO_INVALIDA);}
        return atual;
    }
    public void sair(Sessao sessao){if(sessao!=null&&sessoes.remove(sessao))repositorio.registrarSaida(sessao.usuarioId());}
    public boolean coordenador(Sessao sessao){return verificar(sessao).perfil()==Perfil.COORDENADOR;}
    private Conta exigirCoordenador(Sessao sessao){var c=verificar(sessao);if(c.perfil()!=Perfil.COORDENADOR)throw new FalhaIdentidade(NAO_AUTORIZADO);return c;}
    public Optional<PessoaParaCadastro> consultarPessoa(Sessao sessao,String cpf){
        String documento=ValidacaoPessoa.cpf(cpf);var c=verificar(sessao);
        if(c.perfil()!=Perfil.COORDENADOR){var propria=repositorio.pessoaPorId(c.pessoaId());if(propria.isEmpty()||!propria.get().cpf().equals(documento))throw new FalhaIdentidade(NAO_AUTORIZADO);return propria;}
        return repositorio.pessoaPorCpf(documento);
    }
    public Optional<PessoaParaCadastro> consultarPessoa(Sessao sessao,long id){var c=verificar(sessao);if(c.perfil()!=Perfil.COORDENADOR&&c.pessoaId()!=id)throw new FalhaIdentidade(NAO_AUTORIZADO);return repositorio.pessoaPorId(id);}
    public PessoaParaCadastro criarPessoa(Sessao sessao,String cpf,String nome,String email,String telefone){
        var c=exigirCoordenador(sessao);String documento=ValidacaoPessoa.cpf(cpf);ValidacaoPessoa.dados(nome,email,telefone);
        return repositorio.criarPessoa(documento,nome.trim(),email.trim(),ValidacaoPessoa.limpar(telefone),c.id());
    }
    public PessoaParaCadastro atualizarPessoa(Sessao sessao,PessoaParaCadastro esperada,String nome,String email,String telefone){
        if(esperada==null)throw new FalhaIdentidade(PESSOA_INCOMPLETA);
        var c=verificar(sessao);if(c.perfil()!=Perfil.COORDENADOR&&c.pessoaId()!=esperada.pessoaId())throw new FalhaIdentidade(NAO_AUTORIZADO);
        if(c.perfil()!=Perfil.COORDENADOR&&!Objects.equals(nome,esperada.nome()))throw new FalhaIdentidade(NAO_AUTORIZADO);
        ValidacaoPessoa.dados(nome,email,telefone);
        return repositorio.atualizarPessoa(esperada,nome.trim(),email.trim(),ValidacaoPessoa.limpar(telefone),c.id());
    }
    public Conta criarConta(Sessao sessao,long pessoaId,String login,char[] senha,Perfil perfil){
        var c=exigirCoordenador(sessao);if(perfil==null)throw new FalhaIdentidade(NAO_AUTORIZADO);
        if(repositorio.pessoaPorId(pessoaId).isEmpty())throw new FalhaIdentidade(PESSOA_INCOMPLETA);
        repositorio.vinculos(pessoaId).exigirPerfil(perfil);
        return repositorio.criarConta(pessoaId,login(login),senhas.gerar(senha),perfil,c.id());
    }
    public Optional<Conta> consultarConta(Sessao sessao,long pessoaId){exigirCoordenador(sessao);return repositorio.contaPorPessoa(pessoaId);}
    public Conta atualizarConta(Sessao sessao,Conta esperada,Perfil perfil,boolean ativa){
        var c=exigirCoordenador(sessao);if(esperada==null||perfil==null)throw new FalhaIdentidade(CONTA_NAO_ENCONTRADA);
        if(perfil!=esperada.perfil()) repositorio.vinculos(esperada.pessoaId()).exigirPerfil(perfil);
        return repositorio.atualizarConta(esperada,perfil,ativa,c.id());
    }
    public FichaPessoa consultarFicha(Sessao sessao,String cpf) {
        exigirCoordenador(sessao);
        var pessoa=consultarPessoa(sessao,cpf).orElseThrow(()->new CadastroAlunoException(ErroCadastroAluno.PESSOA_NAO_ENCONTRADA));
        return new FichaPessoa(pessoa,repositorio.contaPorPessoa(pessoa.pessoaId()),repositorio.vinculos(pessoa.pessoaId()));
    }
    public static String login(String entrada){String valor=entrada==null?"":entrada.trim().toLowerCase(Locale.ROOT);if(!valor.matches("[a-z0-9][a-z0-9._@-]{2,99}"))throw new FalhaIdentidade(LOGIN_INVALIDO);return valor;}
}
