# ACA-04 — Matrícula e inscrição em turma

Data: 23/11/2026. Responsável: A/Dan — Acadêmico. Status: implementação iniciada.

## Escopo

`MatriculaCurso` é o vínculo de um Aluno a um Curso e a uma Matriz Curricular publicada. Possui número próprio, distinto de `alunoId`. `InscricaoTurma` liga uma matrícula a uma Turma; não cria nem substitui a matrícula.

## Regras implementáveis localmente

- Coordenador cria, consulta e cancela matrículas/inscrições.
- Matrícula exige aluno ativo, curso e matriz vigente do mesmo curso.
- Número da matrícula é único; um aluno pode ter múltiplas matrículas.
- Inscrição exige matrícula ativa e turma aberta.
- Uma matrícula não possui duas inscrições ativas na mesma turma.
- Horários de inscrições ativas não podem se sobrepor.
- Vaga é consumida na mesma transação da inscrição; concorrência não ultrapassa a capacidade.
- Cancelamento preserva o registro e libera a vaga.

## Limites de integração

Pré-requisito aprovado, documentos obrigatórios (ACA-05), Financeiro e Biblioteca são portas de integração. Até seus provedores existirem, adaptadores de demonstração poderão ser usados apenas em testes, declarados como simulação. Falha ou indisponibilidade de qualquer consulta bloqueia a confirmação; não equivale a ausência de impedimento.

## Decisões propostas, pendentes de validação institucional

- Situação de matrícula: `ATIVA`, `TRANCADA`, `CONCLUIDA`, `CANCELADA`.
- Situação de inscrição: `ATIVA`, `CANCELADA`.
- A matriz é escolhida na criação da matrícula e preservada como histórico.
- Cancelar inscrição libera vaga; não remove registro.
