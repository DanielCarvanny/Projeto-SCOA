package br.edu.scoa.academico.aplicacao.professores;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.dominio.professores.*;
import java.util.Optional;
public final class GestaoProfessores {
    private final long usuarioId;
    private final AcessoProfessores acesso;
    private final PessoasPorCpf pessoas;
    private final DadosPessoaisInstitucionais dadosPessoais;
    private final RepositorioProfessores repositorio;
    public GestaoProfessores(long usuarioId, AcessoProfessores acesso, PessoasPorCpf pessoas,
            DadosPessoaisInstitucionais dadosPessoais, RepositorioProfessores repositorio) {
        this.usuarioId=usuarioId; this.acesso=acesso; this.pessoas=pessoas; this.dadosPessoais=dadosPessoais; this.repositorio=repositorio;
    }
    public Optional<PessoaParaCadastro> buscarPessoa(String cpf) {
        autorizar(acesso.podeCadastrar(usuarioId));
        return pessoas.consultar(usuarioId, cpf(cpf));
    }
    public Professor cadastrar(String cpf, String nome, String email, String telefone, String titulacao, SituacaoFuncional situacao) {
        return cadastrar(cpf,nome,email,telefone,titulacao,situacao,null);
    }
    public Professor cadastrar(String cpf, String nome, String email, String telefone, String titulacao, SituacaoFuncional situacao, Boolean pessoaNova) {
        autorizar(acesso.podeCadastrar(usuarioId));
        String documento=cpf(cpf), titulo=validarDados(titulacao,situacao);
        var existente=pessoas.consultar(usuarioId,documento);
        var pessoa=existente.orElseThrow(()->falha(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA));
        if (pessoa == null || pessoa.pessoaId() <= 0 || !documento.equals(pessoa.cpf())) throw falha(FalhaProfessor.Codigo.RESPOSTA_INCONSISTENTE);
        return repositorio.cadastrar(pessoa.pessoaId(),titulo,situacao,usuarioId);
    }
    public FichaProfessor consultarPorCpf(String entrada) {
        String documento=cpf(entrada);
        autorizar(usuarioId > 0);
        // O contrato institucional autoriza a leitura da Pessoa; o vínculo é autorizado abaixo.
        var pessoa=pessoas.consultar(usuarioId,documento).orElseThrow(()->falha(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA));
        if(pessoa.pessoaId()<=0 || !documento.equals(pessoa.cpf())) throw falha(FalhaProfessor.Codigo.RESPOSTA_INCONSISTENTE);
        var professor=repositorio.consultarPorPessoa(pessoa.pessoaId()).orElseThrow(()->falha(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO));
        autorizar(acesso.podeConsultar(usuarioId,professor.id()));
        conferir(professor,pessoa);
        return new FichaProfessor(professor,pessoa);
    }
    public FichaProfessor consultar(String id) {
        long professorId=id(id); autorizar(acesso.podeConsultar(usuarioId,professorId));
        var professor=repositorio.consultar(professorId).orElseThrow(()->falha(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO));
        var pessoa=dadosPessoais.consultarPorId(usuarioId,professor.pessoaId()).orElseThrow(()->falha(FalhaProfessor.Codigo.PESSOA_NAO_ENCONTRADA));
        conferir(professor,pessoa);
        return new FichaProfessor(professor,pessoa);
    }
    public Professor atualizar(Professor esperado, String titulacao, SituacaoFuncional situacao) {
        if (esperado == null || esperado.id() <= 0) throw falha(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        autorizar(acesso.podeAtualizar(usuarioId,esperado.id()));
        return repositorio.atualizar(esperado,validarDados(titulacao,situacao),situacao,usuarioId);
    }
    public FichaProfessor atualizarPessoa(FichaProfessor esperada, String nome, String email, String telefone) {
        if (esperada == null || esperada.professor() == null || esperada.pessoa() == null) throw falha(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        long id=esperada.professor().id();
        autorizar(acesso.podeAtualizarContato(usuarioId,id)); autorizar(acesso.podeConsultar(usuarioId,id));
        var professor=repositorio.consultar(id).orElseThrow(()->falha(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO));
        conferir(professor,esperada.pessoa()); validarPessoa(nome,email,telefone);
        var pessoa=dadosPessoais.atualizar(usuarioId,esperada.pessoa(),limpar(nome),limpar(email),limpar(telefone));
        conferir(professor,pessoa);
        if (!esperada.pessoa().cpf().equals(pessoa.cpf())) throw falha(FalhaProfessor.Codigo.RESPOSTA_INCONSISTENTE);
        return new FichaProfessor(professor,pessoa);
    }
    private static void conferir(Professor professor,PessoaParaCadastro pessoa) {
        if (pessoa == null || professor.pessoaId() != pessoa.pessoaId()) throw falha(FalhaProfessor.Codigo.RESPOSTA_INCONSISTENTE);
    }
    private void autorizar(boolean permitido) { if (usuarioId <= 0 || !permitido) throw falha(FalhaProfessor.Codigo.NAO_AUTORIZADO); }
    private static long id(String entrada) {
        try { long valor=Long.parseLong(limpar(entrada)); if (valor > 0) return valor; }
        catch (NumberFormatException ignorada) { }
        throw falha(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
    }
    public static String validarDados(String titulacao,SituacaoFuncional situacao) {
        String titulo=limpar(titulacao);
        if (!Titulacao.aceita(titulo)) throw falha(FalhaProfessor.Codigo.TITULACAO_INVALIDA);
        if (situacao == null) throw falha(FalhaProfessor.Codigo.ENTRADA_INVALIDA);
        return titulo;
    }
    private static void validarPessoa(String nome,String email,String telefone) {
        ValidacaoPessoa.dados(nome,email,telefone);
    }
    private static String limpar(String valor) { return valor == null ? "" : valor.trim(); }
    private static FalhaProfessor falha(FalhaProfessor.Codigo codigo) { return new FalhaProfessor(codigo); }
    private static String cpf(String entrada) {
        try { return ValidacaoPessoa.cpf(entrada); }
        catch (CadastroAlunoException falha) { throw falha(FalhaProfessor.Codigo.CPF_INVALIDO); }
    }
}