package br.edu.scoa.academico.apresentacao.alunos;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.dominio.professores.*;
import java.util.*;
final class CenarioProfessores implements RepositorioProfessores, AcessoProfessores {
    final CenarioCpf identidade;
    CenarioProfessores() { this(new CenarioCpf()); }
    CenarioProfessores(CenarioCpf identidade) { this.identidade=identidade; }
    final Map<Long, Professor> professores = new HashMap<>();
    boolean autorizado = true;
    int escritas;
    Runnable antesDeGravar=()->{};
    GestaoProfessores gestao() { return new GestaoProfessores(7, this, identidade, identidade, this); }
    public boolean podeCadastrar(long usuario) { return autorizado && usuario == 7; }
    public boolean podeConsultar(long usuario, long id) { return podeCadastrar(usuario); }
    public boolean podeAtualizar(long usuario, long id) { return podeCadastrar(usuario); }
    public synchronized Professor cadastrar(long pessoaId, String titulacao, SituacaoFuncional situacao, long autor) {
        antesDeGravar.run();
        if (professores.values().stream().anyMatch(p -> p.pessoaId() == pessoaId)) throw new FalhaProfessor(FalhaProfessor.Codigo.PROFESSOR_JA_CADASTRADO);
        var professor = new Professor(professores.size()+1, pessoaId, titulacao, situacao);
        professores.put(professor.id(), professor); escritas++; return professor;
    }
    public Optional<Professor> consultarPorPessoa(long id) { return professores.values().stream().filter(p->p.pessoaId()==id).findFirst(); }
    public Optional<Professor> consultar(long id) { return Optional.ofNullable(professores.get(id)); }
    public synchronized Professor atualizar(Professor esperado, String titulacao, SituacaoFuncional situacao, long autor) {
        var atual = consultar(esperado.id()).orElseThrow(() -> new FalhaProfessor(FalhaProfessor.Codigo.PROFESSOR_NAO_ENCONTRADO));
        if (!atual.equals(esperado)) throw new FalhaProfessor(FalhaProfessor.Codigo.CONFLITO_EDICAO);
        var novo = new Professor(atual.id(), atual.pessoaId(), titulacao, situacao); professores.put(novo.id(), novo); escritas++; return novo;
    }
}
