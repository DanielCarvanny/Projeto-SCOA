package br.edu.scoa.academico.aplicacao.matriculas;
/** Adaptador para integrações ainda externas ao Acadêmico. Erro deve bloquear a inscrição. */
public interface ConsultaElegibilidade { Resultado verificar(long alunoId,long matrizId,long turmaId); record Resultado(boolean possuiImpedimento,boolean preRequisitosAtendidos,boolean documentosAprovados,boolean disponivel){ public static Resultado apto(){return new Resultado(false,true,true,true);} } }
