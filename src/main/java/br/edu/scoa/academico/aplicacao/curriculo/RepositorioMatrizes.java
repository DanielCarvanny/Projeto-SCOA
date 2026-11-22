package br.edu.scoa.academico.aplicacao.curriculo;
import br.edu.scoa.academico.dominio.curriculo.MatrizCurricular;
import java.time.Instant;
import java.util.List;
public interface RepositorioMatrizes {
    /** Cabeçalho, componentes e auditoria na mesma transação, com bloqueio e revisão esperada. */
    MatrizCurricular salvar(MatrizCurricular matriz,long autor,Instant instante);
    List<MatrizCurricular> pesquisar(long cursoId,String termo);
    MatrizCurricular consultar(long id);
    MatrizCurricular publicar(long id,long revisao,long autor,Instant instante);
    MatrizCurricular copiar(long origemId,long revisao,String codigo,String versao,
        java.time.LocalDate inicio,java.time.LocalDate fim,long autor,Instant instante);
}

