package br.edu.scoa.academico.aplicacao.identidade;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.identidade.Perfil;
import java.util.Optional;
/** Adaptador de portas do mesmo módulo. Nunca aceita outro autor além da sessão vinculada. */
public final class AcessoAcademicoAutenticado implements IdentidadeInstitucional,AcessoAlunos,AcessoProfessores,PessoasPorCpf,DadosPessoaisInstitucionais {
    private final ServicoIdentidade identidade;
    private final Sessao sessao;
    private final RepositorioGestaoAlunos alunos;
    private final RepositorioProfessores professores;
    public AcessoAcademicoAutenticado(ServicoIdentidade identidade,Sessao sessao,RepositorioGestaoAlunos alunos,RepositorioProfessores professores){this.identidade=identidade;this.sessao=sessao;this.alunos=alunos;this.professores=professores;}
    private boolean coordenador(long autor){conferir(autor);return identidade.coordenador(sessao);}
    private void conferir(long autor){identidade.verificar(sessao);if(autor!=sessao.usuarioId())throw new FalhaIdentidade(FalhaIdentidade.Codigo.NAO_AUTORIZADO);}
    public boolean podeCadastrarAluno(long autor){return coordenador(autor);}
    public boolean pessoaExiste(long pessoaId){return identidade.consultarPessoa(sessao,pessoaId).isPresent();}
    public boolean podeConsultarAluno(long autor,long alunoId){
        if(coordenador(autor))return true;
        return identidade.verificar(sessao).perfil()==Perfil.ALUNO&&alunos.consultar(alunoId).map(a->a.pessoaId()==sessao.pessoaId()).orElse(false);
    }
    public boolean podeAtualizarAluno(long autor,long alunoId){return coordenador(autor);}
    public boolean podeAtualizarDadosPessoais(long autor,long alunoId){return podeConsultarAluno(autor,alunoId);}
    public boolean podeCadastrar(long autor){return coordenador(autor);}
    public boolean podeConsultar(long autor,long professorId){
        if(coordenador(autor))return true;
        return identidade.verificar(sessao).perfil()==Perfil.PROFESSOR&&professores.consultar(professorId).map(p->p.pessoaId()==sessao.pessoaId()).orElse(false);
    }
    public boolean podeAtualizar(long autor,long professorId){return coordenador(autor);}
    public boolean podeAtualizarContato(long autor,long professorId){return podeConsultar(autor,professorId);}
    public Optional<PessoaParaCadastro> consultar(long autor,String cpf){conferir(autor);return identidade.consultarPessoa(sessao,cpf);}
    public Optional<PessoaParaCadastro> consultarPorId(long autor,long id){conferir(autor);return identidade.consultarPessoa(sessao,id);}
    public PessoaParaCadastro cadastrarOuObter(long autor,String cpf,String nome,String email,String telefone){conferir(autor);return identidade.criarPessoa(sessao,cpf,nome,email,telefone);}
    public PessoaParaCadastro atualizar(long autor,PessoaParaCadastro esperada,String nome,String email,String telefone){conferir(autor);return identidade.atualizarPessoa(sessao,esperada,nome,email,telefone);}
}
