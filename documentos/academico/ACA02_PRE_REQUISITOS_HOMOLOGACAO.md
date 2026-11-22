# ACA-02 — pré-requisitos: entrega e homologação

Data: 16/11/2026. Responsável: A/Dan — Acadêmico. Status: pronto para homologação após validação técnica, sem aceite final do ACA-02.

## O que mudou

A ficha da matriz apresenta as exigências e oferece **Gerenciar pré-requisitos** para matrizes em elaboração. O Coordenador seleciona a disciplina que será cursada e a disciplina em que é necessária aprovação prévia. As opções mostram código e nome.

Exemplo: Programação II exige aprovação prévia em Programação I. Isso configura uma regra curricular; não verifica ainda o histórico de um aluno. A verificação efetiva será integrada à inscrição no ACA-04.

Fluxo: consultar matriz → abrir ficha → Gerenciar pré-requisitos → adicionar/remover exigências → Continuar para salvar → revisar → Salvar pré-requisitos.

Adicionar e remover modificam apenas a configuração na tela. Cancelar descarta essas mudanças. Somente Salvar pré-requisitos grava, com auditoria e incremento da revisão técnica da matriz.

## Regras

- Somente Coordenador administra. Sessão e permissão são verificadas pelo serviço.
- Somente matriz em elaboração pode ser alterada; o banco é conferido durante a transação.
- As duas disciplinas pertencem à mesma matriz.
- A própria disciplina e exigências já cadastradas não aparecem nas opções disponíveis.
- Não são permitidas autorreferência, duplicidade nem ciclos diretos ou indiretos.
- Editar carga, período ideal ou revisão histórica da disciplina preserva as relações.
- Para remover componente envolvido em relações, remova e salve essas relações primeiro. A tela informa quais disciplinas estão envolvidas.
- Edições concorrentes usam a revisão da matriz; a segunda gravação desatualizada é recusada, inclusive quando duas telas tentam criar relações opostas.
- A matriz continua obrigatoriamente não vazia.

Um ciclo seria: A exige B, B exige C e C exige A. Nenhuma dessas disciplinas poderia iniciar a sequência. O sistema recusa a relação que fecharia esse ciclo.

## Como testar com MySQL

Na pasta C:\Users\Dan\SCOA:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

Entre como Coordenador. Use uma matriz em elaboração com ao menos três disciplinas para testar todos os cenários.

A migração nova é V005__pre_requisitos.sql. Ela cria pre_requisito e pre_requisito_historico sem alterar migrações anteriores. Chaves compostas garantem que as duas pontas pertencem à matriz; tipo disponível: APROVACAO_PREVIA.

Mapeamento do modelo: as relações usam matriz_id + disciplina_id para identificar o componente de origem, em vez de duplicar seu ID técnico. O par é único em componente_curricular. Cada histórico de relações está associado à revisão de matriz_historico, que contém autor, instante UTC, origem e operação. Nenhuma relação é removida por cascata.

## Demonstração em memória

Classe: br.edu.scoa.academico.curriculo.DemonstracaoPreRequisitos. Execute pela IDE com classpath de testes ou:

```powershell
.\executar-academico.ps1 -Acao Preparar
$javaPreRequisitos = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $javaPreRequisitos -cp "target\test-classes;target\classes" br.edu.scoa.academico.curriculo.DemonstracaoPreRequisitos
```

Pré-carrega a matriz ADS-PRE / 2027.1 com POO-DEMO, P2-DEMO e P3-DEMO; P2 exige POO e P3 exige P2. Consulte a matriz e abra a ficha. Os dados são sintéticos, não chegam ao MySQL e desaparecem ao fechar. A identidade é simulada apenas nessa demonstração.

## Roteiro de homologação

| Teste | Ação | Resultado esperado |
|---|---|---|
| Consulta | Abrir a ficha da matriz de demonstração | Exibe P2 exige POO e P3 exige P2 |
| Inclusão válida | Adicionar P3 exige POO, revisar e salvar | Relação adicional persistida; duas exigências para P3 |
| Cancelamento | Adicionar/remover uma relação e cancelar | Ficha conserva a configuração anterior |
| Autorreferência | Selecionar P2 como disciplina a cursar | P2 não aparece como exigência disponível |
| Duplicidade | Selecionar P2, que já exige POO | POO não aparece novamente nas opções |
| Ciclo direto | Tentar configurar POO exige P2 | Mensagem de ciclo; relação não é adicionada |
| Ciclo indireto | Tentar configurar POO exige P3 | Mensagem de ciclo; cadeia original preservada |
| Remoção de relação | Selecionar P3 exige P2, remover, revisar e salvar | Relação deixa a configuração atual; histórico preservado |
| Remoção de componente | Tentar remover POO enquanto P2 o exige | Recusa, informando a relação envolvida |
| Remoção permitida | Remover e salvar relações do componente; depois remover componente e confirmar matriz | Operação permitida se restar ao menos um componente |
| Edição de componente | Alterar carga/período ideal e salvar matriz | Exigências preservadas |
| Conteúdo histórico | Atualizar explicitamente a revisão da disciplina e salvar matriz | Relações permanecem vinculadas à mesma disciplina |
| Persistência | Fechar/reabrir a aplicação MySQL | Relações mantidas |
| Concorrência | Consultar a mesma matriz em duas instâncias e salvar em ambas | Segunda recebe conflito e deve cancelar/consultar novamente |
| Permissão | Entrar como Aluno ou Professor | Administração de matrizes indisponível |

## Evidências e limites

TDD: testes de domínio e tela foram escritos antes das respectivas classes e falharam por ausência delas. A primeira execução MySQL encontrou o banco desligado; essa falha foi ambiental e não foi tratada como evidência funcional. Após iniciar o banco descartável, a integração foi executada com sucesso.

Testes abrangem ciclos, referências externas, duplicidade, independência de matrizes, autorização, cancelamento, confirmação única fora da thread gráfica, persistência, snapshots, rollback, remoções e concorrência. Instalação limpa e upgrade V004 → V005 preservando componentes foram verificados.

Logs locais: tmp/tdd-pre-red.log, tmp/tdd-pre-tela-red.log, tmp/tdd-pre-integracao.log e tmp/tdd-pre-final.log (falha de concorrência preexistente em Alunos) e tmp/tdd-pre-final-corrigido.log na pasta de trabalho do agente. A captura target/aca02-pre-requisitos.png é gerada pelo teste de interface.

Não houve aplicação da V005 ao banco padrão, commit, push ou aceite automático. A revisão manual permanece necessária. Publicação/cópia de versões, períodos letivos e os refinamentos futuros de UX continuam pendentes.

Correção adicional de regressão: o cadastro de Aluno repetiu de forma limitada (até três tentativas) a transação revertida pelo MySQL em caso de deadlock 1213. Outros erros não são repetidos. Unicidade e auditoria permanecem transacionais. A falha original foi observada no teste duasConexoesConcorrentesCriamUmUnicoAluno e foi preservada no log anterior.

Resultado final em 16/11/2026: **190 testes unitários/de interface e 66 de integração MySQL aprovados**, zero falhas/erros/ignorados. Log: tmp/tdd-pre-final-corrigido.log. A captura da revisão foi inspecionada visualmente; homologação humana permanece pendente.
