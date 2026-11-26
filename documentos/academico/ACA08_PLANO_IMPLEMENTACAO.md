# ACA-08 — Plano de implementação

**Responsável:** Dan (A, B, C e D)
**Incremento:** ACA-08 — Auditoria do diário e pauta final
**Estimativa vigente:** 5 SP
**Dependência do cartão:** ACA-07
**Dependência de integração:** ACA-04 unificado, que fornecerá pendências de conteúdo das aulas e registro de frequência
**Stack:** Java 21, Maven, Swing, JDBC/MySQL 8, Flyway e JUnit 5

## Objetivo

Permitir que a coordenação analise e registre a homologação do diário acadêmico e da pauta final, preservando evidências de revisão. O sistema deve impedir o fechamento enquanto existirem pendências, conforme o critério já registrado no backlog. A fórmula e os resultados são responsabilidade do ACA-07; o ACA-08 os consulta e não recalcula notas.

## Base documental e situação existente

- O estudo de caso descreve auditoria, pela coordenação, dos diários dos professores sob sua coordenação; consulta de avaliações por turma/disciplina; monitoramento de resultados e análise curricular. Também cita registro de avaliações, notas, faltas e conteúdo de aulas. O documento não detalha estados do processo de homologação nem quais pendências bloqueiam o fechamento.
- O backlog do ACA-08 define que o coordenador registra análise/homologação e que pendências impedem o fechamento.
- O ACA-07 já persiste histórico de alterações de avaliações e notas em V014, com autor, instante, origem, valores anterior/novo e identificador da operação. Expõe diário e resultados pela aplicação, mas não possui workflow de revisão/homologação nem fechamento de pauta.
- O ACA-04 unificado reúne aulas, conteúdo e frequência. Para auditar o diário de classe completo, o ACA-08 deve consultar os contratos públicos do ACA-04; não deve ler suas tabelas diretamente.

## Escopo proposto

1. Coordenador lista turmas e pautas no âmbito autorizado e consulta o diário por turma/disciplina, com avaliações, notas/resultados e histórico de alterações disponível.
2. Coordenador registra análise e decisão de revisão, com autor, instante UTC, observação/justificativa quando aplicável e revisão da pauta analisada.
3. O sistema calcula e apresenta pendências antes do fechamento e recusa a homologação/fechamento se alguma pendência impeditiva definida estiver presente.
4. A decisão, reanálise, devolução, homologação e eventual reabertura ficam em trilha imutável de auditoria.
5. A pauta homologada apresenta os resultados produzidos pelo ACA-07 e os metadados da decisão, sem recalcular média ou duplicar os históricos de nota.

**Fora do escopo sem requisito adicional:** alterar fórmulas/notas do ACA-07, criar regra mínima de frequência, impor formato de exportação PDF/CSV, ou estabelecer prazos de guarda da pauta.

## Decisões de regra registradas pelo responsável

As decisões abaixo foram informadas pelo responsável e passam a orientar esta implementação:

- **Âmbito:** coordenadores, pelo perfil institucional atual, podem auditar todas as turmas.
- **Bloqueios:** impedem homologação notas das provas ausentes, exame final pendente para aluno elegível e pendências de conteúdo/frequência que o ACA-04 informar como obrigatórias. Não criar percentual mínimo de presença.
- **Sem exame:** aluno sem elegibilidade ao exame é reprovado pela média das provas; o ACA-07 não deve inventar outra fórmula de média final.
- **Pós-homologação:** bloquear alterações de avaliações e notas até que o coordenador reabra a pauta. Reabertura deve ser auditada.
- **Fluxo mínimo:** coordenador pode registrar análise e homologar; não há etapa de submissão pelo professor neste incremento. A interface mantém o registro/histórico no sistema; emissão/assinatura externa não está no escopo aprovado.

O provedor ACA-04 ainda precisa implementar o contrato de pendências do diário de aulas/frequência. Enquanto não estiver disponível, a falha de integração bloqueia homologação.

## Sequência de implementação

| Etapa | Trabalho | Critério de saída |
|---|---|---|
| 1. Refinar e mapear | Decisões acima registradas; mapear contratos do ACA-07 para diário, resultados e histórico de alterações, e criar o contrato público ACA-04 para conteúdo/frequência. | Critérios verificáveis; integração real ACA-04 permanece explicitamente bloqueante para homologação enquanto o provedor não existir. |
| 2. Modelar o domínio | Criar tipos para revisão da pauta, decisão/estado, pendências, autor e evento de auditoria. Modelar transições permitidas e revisão otimista sem acoplar domínio a Swing ou MySQL. | Transições e recusas cobertas por testes de domínio; nenhum estado ou regra institucional foi inventado. |
| 3. Criar casos de uso e autorização | Implementar consulta do diário, análise, devolução, homologação e eventual reabertura. Reutilizar `Sessao` e identidade existente; verificar perfil de coordenador e seu âmbito no servidor. Tratar falha de consulta ao ACA-04 como erro, nunca como diário sem pendência. | Usuários não autorizados não consultam escopo protegido nem decidem; conflitos de revisão não sobrescrevem análise concorrente. |
| 4. Persistir V015 | Criar migração nova, sem editar V014. Persistir estado atual da revisão da pauta e histórico de eventos, com FKs, índices, autor, instante UTC, operação idempotente e revisão. Usar transação para validar pendências e efetivar decisão; se homologação congelar notas, integrar essa trava ao contrato de escrita do ACA-07. | Instalação desde V001 e atualização de V014 para V015 aprovadas em MySQL de teste; eventos não podem ser apagados/alterados pelo fluxo comum. |
| 5. Integrar interfaces públicas | Adicionar apenas operações necessárias aos contratos públicos do ACA-07 e ACA-04. Reaproveitar o diário e o histórico do ACA-07; consumir dados de aula/frequência por interface acordada. Não consultar diretamente tabelas de outros componentes. | Fronteiras testadas; contratos preservam os consumidores existentes. |
| 6. Construir interface Swing | Disponibilizar à coordenação uma tela de seleção/filtro de turma, diário/pauta, pendências, histórico e análise/decisão. Mostrar ação e motivo de bloqueio com mensagens claras. Executar I/O fora da EDT e não confiar na ocultação de controles como autorização. | Coordenador consegue analisar e registrar decisão; demais perfis recebem recusa no serviço; navegação retorna ao menu e não congela a janela. |
| 7. Testar e demonstrar | Cobrir domínio, autorização, persistência MySQL e um roteiro Swing com dados sintéticos. Na demonstração integrada, usar turmas, aulas e inscrições reais criadas pelo ACA-04 e avaliações/notas do ACA-07; fixtures são aceitáveis nos testes, não como prova de integração. | Testes unitários e MySQL desde V001/V014 passam; evidência reproduzível mostra fechamento permitido e bloqueado pelas pendências definidas. |

## Arquivos previstos

- `src/main/java/br/edu/scoa/academico/dominio/diarios/` — modelo da revisão, estados/eventos e regras de transição.
- `src/main/java/br/edu/scoa/academico/aplicacao/diarios/` — casos de uso e contratos de leitura/registro.
- `src/main/java/br/edu/scoa/academico/infraestrutura/persistencia/RepositorioAuditoriaDiarioMysql.java` — persistência MySQL do processo de revisão.
- `src/main/java/br/edu/scoa/academico/infraestrutura/identidade/AutorizacaoAuditoriaDiarioAcademica.java` — adaptador à identidade e aos vínculos existentes.
- `src/main/java/br/edu/scoa/academico/apresentacao/diarios/FluxoAuditoriaDiario.java` e módulo de composição MySQL — interface Swing e integração ao menu institucional.
- `src/main/resources/db/academico/V015__revisao_diario_e_pauta.sql` — nova migração, se V015 continuar sendo a próxima versão livre.
- `src/test/java/br/edu/scoa/academico/diarios/` — testes unitários; teste de integração MySQL deve acompanhar o IT acadêmico existente ou ser separado sem competir pelo mesmo banco.
- `documentos/academico/ACA08_PLANO_IMPLEMENTACAO.md` — este plano; atualizar para registrar decisões e execução.

Os nomes são previsões alinhadas à estrutura atual. Rever antes de criar arquivos e não editar arquivos fora da propriedade Acadêmica. Alterações necessárias ao ACA-07 devem manter a mesma regra de propriedade e ser documentadas como impacto do ACA-08.

## Matriz mínima de testes

1. Coordenador autorizado consulta qualquer turma; professor/aluno não acessam a função de revisão.
2. Professor/aluno não registra decisão de homologação; a recusa não altera estado nem auditoria.
3. Pauta sem pendência impeditiva pode avançar conforme transição aprovada; com cada pendência bloqueadora, fechamento é recusado com código estável e pendência identificável.
4. Notas, exames e pendências vêm de consultas ACA-07; falha/indisponibilidade da consulta impede homologar.
5. Dados de aula/frequência vêm do contrato ACA-04 quando forem parte dos critérios; ausência de integração não equivale a frequência completa.
6. Devolução/reanálise/homologação persistem autor, instante UTC, motivo exigido pela regra e revisão da pauta; histórico antigo permanece.
7. Repetição da operação com mesmo identificador é idempotente; identificador repetido com outro conteúdo é recusado.
8. Alteração concorrente ou dado modificado após o início da revisão causa conflito, sem aceitar snapshot desatualizado silenciosamente.
9. Gravações de avaliação ou nota são recusadas após homologação e só voltam a ser permitidas após reabertura pelo coordenador.
10. Instalação MySQL desde V001 e upgrade V014→V015 preservam todo o histórico anterior.

## Limitações e gates

- A lista de bloqueios foi definida pelo responsável: provas/exame elegível pendentes e pendências obrigatórias de aula/frequência retornadas pelo ACA-04.
- O estudo de caso não descreve assinatura digital, formato de exportação ou período de guarda da pauta; não foram incluídos.
- A integração completa com o diário de aulas do ACA-04 precisa usar contrato público e dados reais antes do aceite integrado; consultar o provedor ausente deve bloquear a homologação.
- ACA-07 usa um único exame final por turma e expõe resultado apenas quando há notas suficientes. O ACA-08 deve consumir essa saída atual; qualquer ampliação requer decisão no ACA-07.
- Não alterar estimativa de 5 SP, RF, responsável ou status final do backlog por este plano.

## Estado do plano

### Entrega implementada

- V015 acrescenta versão do diário por turma, estado atual da pauta e histórico imutável das análises, homologações e reaberturas.
- O caso de uso de auditoria autoriza o perfil coordenador, lista todas as turmas, consulta avaliações/notas e seus históricos, calcula pendências de prova/exame e exige o contrato público do ACA-04.
- A homologação valida a versão do diário sob bloqueio transacional. Avaliações e notas ficam bloqueadas enquanto a pauta estiver homologada; somente uma reabertura auditada pelo coordenador as libera.
- O fluxo Swing está conectado ao menu acadêmico e retorna ao menu. O ponto de composição fornece deliberadamente um adaptador ACA-04 indisponível: a integração ausente aparece como pendência e não permite fechamento.
- A regra ACA-07 foi ajustada para resultado final sem exame: média das provas abaixo de 5,0 reprova; média igual ou superior a 5,0 mantém o exame como pendência até lançamento da nota.
- O teste MySQL no arquivo IT cobre bloqueio por notas/frequência, exame pendente, homologação, bloqueio de edições, reabertura e auditoria, além da migração completa até V015.

### Validação nesta execução

- `mvn -o -Dmaven.repo.local=tmp/maven-repository test`: 37 testes unitários aprovados.
- `git diff --check`: aprovado; apenas avisos de conversão LF/CRLF em arquivos previamente modificados.
- Integração MySQL V001–V015: não executada. Docker Desktop não está disponível e a porta local `127.0.0.1:3308` não está aceitando conexões.
- Teste manual Swing conectado a MySQL: não executado pela mesma ausência do banco local.

### Estado

Implementação do fluxo ACA-08 entregue para revisão. O aceite integrado permanece incompleto até o ACA-04 implementar o contrato `ConsultaDiarioAulas`, conectar seus dados reais e os testes de migração/integração e a demonstração Swing serem executados contra MySQL descartável. Na configuração atual, homologação falha em modo seguro devido ao adaptador indisponível.
