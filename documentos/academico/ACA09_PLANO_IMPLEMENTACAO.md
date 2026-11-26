# ACA-09 — Plano de implementação

**Responsável:** Dan (A, B, C e D)
**Incremento:** ACA-09 — Boletim, histórico e horários
**Estimativa vigente:** 5 SP
**Dependência registrada:** ACA-07
**Stack:** Java 21, Maven, Swing, JDBC/MySQL 8, Flyway e JUnit 5
**Branch planejada:** `codex/aca-09-boletim-historico-horarios`

## Objetivo e critérios de aceite

Permitir ao aluno consultar o próprio boletim, histórico acadêmico e horários. Os resultados exibidos precisam corresponder aos lançamentos do ACA-07 e ao plano curricular aplicável. Consultas não podem aceitar um `alunoId` arbitrário do usuário nem revelar dados de outro aluno.

O backlog não define uma fórmula adicional para aprovação, uma regra de integralização, nem um formato oficial de boletim. O ACA-09 deve reaproveitar a situação e as médias calculadas pelo ACA-07, sem converter nota ausente em zero ou deduzir conclusão curricular com regra própria.

## Base existente e lacunas verificadas

- O ACA-07 possui `GestaoAvaliacoes.minhasNotas(sessao)`, que deriva o aluno da sessão institucional, retorna lançamentos e permite consultar médias e resultado final. O resultado final já distingue pendência, aprovação e reprovação.
- O registro de nota oferece período, disciplina, turma, avaliação, data e nota. A interface atual do ACA-07 já apresenta ao aluno médias das provas, elegibilidade ao exame e situação pós-exame.
- `Turma` contém horários semanais (`HorarioTurma`: dia, início, fim e sala); `RepositorioTurmas` consulta turmas por id, professor ou período, mas não oferece a consulta das turmas inscritas por aluno.
- `RepositorioMatriculas.porAluno` e `RepositorioInscricoes.ativasPorMatricula` existem, mas a segunda consulta retorna apenas inscrições ativas. Não há ainda um caso de uso que exponha ao aluno seu histórico de inscrições e os respectivos horários.
- `MatrizCurricular` contém componentes e período ideal. A apresentação precisa correlacionar a disciplina/turma cursada ao componente da matriz vinculada à matrícula, usando contratos públicos de aplicação.
- O ACA-04 unificado ainda é dependência para evidência de ponta a ponta com matrícula/inscrições geradas pela interface. A implementação do ACA-09 pode avançar usando os contratos e dados disponíveis; fixtures não bastam para o aceite integrado final.

## Escopo proposto

1. **Boletim:** listar avaliações/notas por período, disciplina e turma; apresentar média de provas, elegibilidade ao exame e resultado final diretamente das regras ACA-07. Valores ausentes permanecem como pendentes.
2. **Histórico acadêmico:** mostrar matrícula/curso/matriz e componentes curriculares relacionados às disciplinas cursadas, incluindo período/turma e o resultado fornecido pelo ACA-07 quando houver lançamentos suficientes. A tela não declara conclusão ou integralização sem regra aprovada.
3. **Horários:** listar dia da semana, intervalo de horário, disciplina, turma e sala das inscrições ativas do aluno. Não inferir calendário de aulas ou presença a partir do horário semanal.
4. **Autorização:** obter a identidade do aluno somente pela sessão institucional existente (`Sessao`/`pessoaId`); professor/coordenador não usam este fluxo de consulta do aluno e nenhum parâmetro recebido pode trocar o titular da consulta.
5. **Interface:** incluir acesso do perfil aluno pelo menu, atualizar consultas fora da EDT e retornar ao menu acadêmico sem perder a sessão.
6. **Persistência:** priorizar leitura dos dados já persistidos; não criar tabelas ou migração sem necessidade demonstrada. Caso a consulta de histórico exija estado que não exista, registrar a lacuna antes de propor alteração destrutiva ou regra nova.

## Decisões preservadas

- O cálculo é responsabilidade do ACA-07: escala `0,0–10,0`, média das provas com pesos iguais, arredondamento `HALF_UP`, regra de exame e classificação já adotadas.
- `Nota ausente` continua distinta de `0,0`.
- A consulta é sempre do próprio aluno, resolvido pela identidade institucional; não será criado usuário ou cadastro paralelo.
- O plano curricular é evidência de componentes previstos, não autorização para inventar critérios de aprovação, equivalência, trancamento ou integralização.
- A referência de horário é o horário semanal cadastrado para a turma. O ACA-09 não marca presença, não valida reposição e não transforma horário em data civil.

## Sequência de execução

| Etapa | Trabalho | Critério de saída |
|---|---|---|
| 1. Confirmar contratos | Mapear APIs públicas de avaliação, matrícula, inscrição, turma/horários e matriz. Definir uma consulta de aplicação para histórico/horários próprios sem expor repositórios de infraestrutura. | Cada informação possui fonte oficial e vínculo claro com pessoa/aluno/matrícula/turma; lacunas de ACA-04 registradas como dependência. |
| 2. Modelar consultas | Criar DTOs de leitura para boletim, linha histórica e horário semanal. Reutilizar `ResultadoFinalAcademico`/notas do ACA-07 em vez de recalcular em DTO ou apresentação. | Ausência de nota e ausência de lançamento continuam explícitas; DTO não incorpora fórmula nova. |
| 3. Implementar autorização e aplicação | Construir consultas para o titular autenticado. Resolver aluno pelo `pessoaId` validado e recuperar suas matrículas/inscrições por contratos públicos; negar consulta de outra pessoa. | Chamadas sem sessão, perfil inadequado ou titular diferente falham no servidor; nenhum `alunoId` livre é aceito na fronteira da tela. |
| 4. Implementar persistência de leitura | Adaptar repositórios existentes ou criar porta de consulta de histórico do ACA-04, respeitando as fronteiras públicas. Consultar horários por turma inscrita e componentes pela matriz da matrícula. | Consultas parametrizadas retornam apenas dados associados ao aluno; sem leitura de tabelas privadas de outros módulos. Migração só se necessária e versionada. |
| 5. Integrar interface Swing | Adicionar entrada no menu do aluno e telas/abas de Boletim, Histórico e Horários, estados vazios e atualização assíncrona. Oferecer retorno ao menu. | Fluxo legível para aluno sem notas, sem inscrição ativa ou sem horário cadastrado; janela permanece responsiva. |
| 6. Testar regras e fronteiras | Cobrir aluno titular, tentativa de acesso cruzado, notas pendentes, cálculo ACA-07, matriz/plano e ordenação/conflitos de horários conforme os dados existentes. | Testes unitários e de contrato passam com fixtures sintéticas; nenhuma regra de negócio é duplicada. |
| 7. Validar MySQL e Swing integrado | Aplicar histórico MySQL completo até a versão corrente, consultar dados gravados e executar login de aluno, boletim, histórico, horários e retorno ao menu. Quando disponível, gerar inscrições pelo fluxo ACA-04. | Evidência MySQL real e roteiro Swing reprodutível; dados reais/sintéticos identificados; nenhuma fixture apresentada como integração final. |
| 8. Documentar resultado | Registrar arquivos, migrações (ou ausência delas), testes, comando de reprodução e dependências ainda abertas. | Status do ACA-09 permanece sujeito à integração real do ACA-04 e à revisão/aceite humano. |

## Matriz mínima de testes

1. Aluno autenticado vê somente suas matrículas, inscrições, notas e horários.
2. Tentativa de fornecer ou adulterar o id de outro aluno é recusada ou ignorada porque a aplicação deriva o titular da sessão.
3. Notas ausentes aparecem como pendentes, nunca como zero; média e situação exibidas correspondem ao ACA-07.
4. Resultado com exame pendente, aprovado e reprovado confere com lançamentos e cálculo ACA-07, inclusive uma casa decimal e `HALF_UP`.
5. Histórico relaciona a matrícula e sua matriz aos componentes cursados sem afirmar integralização não prevista.
6. Horários correspondem apenas às inscrições ativas e mostram dia, início, fim e sala; ausência de horário não é transformada em falta.
7. Falha/indisponibilidade de consulta de histórico não produz lista vazia como se o aluno não tivesse vínculo.
8. Teste MySQL verifica junções, titularidade, ordenação, dados pendentes e instalação/upgrade das migrações aplicáveis.
9. Roteiro Swing valida login, acesso ao menu do aluno, consulta das três áreas e retorno ao menu.

## Arquivos previstos

- `src/main/java/br/edu/scoa/academico/dominio/boletins/` — DTOs de resultado/consulta, se os tipos existentes não bastarem.
- `src/main/java/br/edu/scoa/academico/aplicacao/boletins/` — consultas de boletim, histórico e horário, com autorização da sessão.
- `src/main/java/br/edu/scoa/academico/infraestrutura/persistencia/` — adaptadores de leitura, somente se as portas existentes não cobrirem as consultas.
- `src/main/java/br/edu/scoa/academico/apresentacao/boletins/` — painel Swing e composição.
- `src/main/java/br/edu/scoa/academico/apresentacao/identidade/PainelIdentidade.java` e composição — ligação do menu do aluno, preservando o fluxo e a identidade atuais.
- `src/test/java/br/edu/scoa/academico/` — testes unitários, contrato e MySQL com dados sintéticos.
- `documentos/academico/ACA09_PLANO_IMPLEMENTACAO.md` — este plano e registro de execução.

Os caminhos são previsões baseadas na estrutura atual. Reutilizar pacotes existentes se forem mais adequados; evitar refatoração ampla ou mudanças em migrações aplicadas. A atividade permanece no módulo Acadêmico, sob responsabilidade de Dan (A, B, C e D).

## Riscos e dependências

- O ACA-07 já fornece as notas/resultados necessários ao boletim; a integração completa do ACA-07 com turma/inscrições produzidas pelo ACA-04 continua registrada no aceite do ACA-04.
- A consulta pública de todas as inscrições do histórico do próprio aluno ainda não está exposta no contrato atual. Implementá-la como leitura do ACA-04 é necessário para um histórico completo, mas não deve impedir o desenvolvimento do boletim e do horário com as portas existentes.
- O backlog ACA-09 não autoriza definir aproveitamento de estudos, percentuais de frequência para aprovação, situação de integralização ou política para horário de aula cancelada/reposição.
- O checkout atual contém alterações ACA-07/ACA-08 não commitadas na branch `codex/aca-07-avaliacoes`. Antes de iniciar a implementação, separar o ACA-09 em branch/worktree própria sem perder essas alterações.
- Migração ou acesso a tabelas pertencentes a outro módulo exige contrato público e concordância do provedor afetado; não copiar nem contornar repositórios privados.

## Estado do plano

### Implementação realizada

- Criados os casos de uso e projeções de leitura do boletim, plano curricular, histórico de inscrições e horários semanais. A consulta aceita apenas a sessão; o aluno é resolvido por `pessoaId` através do adaptador de autorização já usado pelo ACA-07.
- O boletim reutiliza `GestaoAvaliacoes` e `ResultadoFinalAcademico`; notas ausentes seguem pendentes e resultado com mais de um exame não é recalculado.
- Adicionado `RepositorioInscricoes.porMatricula` para leitura do histórico ativo/cancelado e `RepositorioCatalogo.consultar` para buscar dados do curso/disciplina. Ambos são contratos de aplicação implementados pelos repositórios acadêmicos existentes.
- A grade semanal lista apenas inscrições e matrículas ativas em turmas abertas ou em andamento; o histórico preserva inscrições canceladas e notas anteriores.
- A tela Swing tem abas de Boletim, Histórico/plano curricular e Horários, carrega dados fora da EDT e retorna ao menu acadêmico pelo fluxo institucional.
- Nenhuma migração foi necessária: o incremento consulta entidades e horários já persistidos.
- Criado teste MySQL no IT acadêmico cobrindo titularidade, correlação da matriz, resultado de nota e preservação do histórico após cancelamento.

### Verificação nesta execução

- `mvn -o -Dmaven.repo.local=tmp/maven-repository -Pintegracao verify`: 37 testes unitários e 7 testes MySQL passaram.
- O MySQL 8.4 descartável iniciou em `127.0.0.1:3308` com armazenamento `tmpfs`; o IT aplicou a instalação limpa de identidade e o histórico Acadêmico V001–V015, inclusive o caminho de atualização de V010 para V011.
- A primeira execução revelou que a limpeza da fixture ACA-08 não removia a nota do exame final antes da inscrição. O teste passou a incluir o ID do exame na limpeza; a repetição em uma base limpa passou integralmente.
- Validação visual executada com a conta sintética `aluno.teste`: login, opção ACA-09 no menu, carregamento das abas Boletim, Histórico/plano curricular e Horários, estados vazios esperados e retorno ao menu passaram.
- `git diff --check`: sem erros de whitespace; permanecem apenas avisos LF/CRLF em arquivos já modificados no checkout.
- A conta Swing sintética não tinha matrícula; assim, as telas com dados reais gerados pela interface ACA-04 não foram demonstradas. O aceite integrado com inscrições do fluxo ACA-04 ainda depende desse provedor.

### Estado e dependências

Implementação e validações locais ACA-09 concluídas para revisão. O aceite integrado com matrícula/inscrição geradas pelo fluxo ACA-04 permanece pendente; o teste MySQL e a demonstração Swing desta execução usaram fixtures sintéticas. A implementação foi feita no checkout que continua na branch `codex/aca-07-avaliacoes`, junto de alterações anteriores não commitadas; separar o incremento antes de criar commit/PR.

## Integração no checkout oficial em 27/11/2026

A branch codex/aca-09-integracao-oficial parte de origin/codex/aca-05-documentos. A composição Swing liga avaliações, auditoria de pauta e boletim ao menu da base oficial. O backlog mantém as notas já registradas nessa base. A V011 publicada foi preservada. A instalação descartável aplicou V001 a V011 e a atualização aplicou V014 e V015.

A suíte unitária passou com 258 testes. FluxosAcademicosMysqlIT passou com três cenários em MySQL 8.4, com dados sintéticos. A navegação Swing com matrículas e inscrições produzidas pelo fluxo real do ACA-04 permanece pendente. A interface de documentos e as migrações de retenção do checkout anterior não foram transferidas nesta integração.
