package br.edu.scoa.academico.aplicacao.identidade;

import br.edu.scoa.academico.aplicacao.alunos.PessoaParaCadastro;
import br.edu.scoa.academico.dominio.identidade.Conta;
import java.util.Optional;

public record FichaPessoa(PessoaParaCadastro pessoa, Optional<Conta> conta, VinculosAcademicos vinculos) {
    @Override public String toString() { return "FichaPessoa[pessoaId=" + pessoa.pessoaId() + "]"; }
}
