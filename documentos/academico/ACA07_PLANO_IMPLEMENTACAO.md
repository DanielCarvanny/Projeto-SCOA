# ACA-07 — Plano de execução

**Responsável:** Dan (A, B, C e D)
**Incremento:** ACA-07 — Avaliações, notas e resultados
**Estimativa no backlog:** 5 SP
**Dependência:** ACA-04 unificado (matrícula, inscrição, aulas e frequência)
**Stack:** Java 21, Maven, Swing, JDBC/MySQL 8, Flyway e JUnit 5
**Branch de trabalho:** `codex/aca-07-avaliacoes`

## Resultado esperado

O professor responsável pela turma e o coordenador podem cadastrar avaliações e lançar/corrigir notas. O aluno pode consultar apenas as próprias notas. O sistema calcula a média aritmética com pesos iguais, arredonda HALF_UP a uma casa decimal, mantém resultado pendente enquanto faltar nota prevista e indica elegibilidade ao exame final pela média mínima 5 nas provas. Após o exame, calcula `MF = (MP + EF) / 2` e aprova com `MF >= 5,0`. Avaliações devem ser cadastradas ou ter a data alterada com antecedência mínima de sete dias corridos. Nenhuma nota ausente vira zero.


## Estado atual

**Progresso ACA-07: implementação e testes automatizados concluídos; aceite integrado acompanhado pelo ACA-04.** As duas validações finais (roteiro Swing em execução e uso de turma/inscrições originadas pelo fluxo ACA-04) foram transferidas para os critérios integrados do ACA-04, para execução conjunta nesse incremento. Não são tarefas independentes a reabrir no ACA-07.

- Implementados: domínio de avaliação/nota, faixa `0,0–10,0` e precisão de uma casa; média aritmética com pesos iguais e arredondamento HALF_UP, pendência para nota ausente e elegibilidade ao exame pela média das provas `>= 5,0`.
- Implementado: antecedência mínima de sete dias corridos para cadastrar avaliação ou alterar sua data, calculada com a data civil de `America/Sao_Paulo`. Alterar título/descrição/tipo sem mudar a data não exige novo prazo.
- Implementado: resultado pós-exame pela média aritmética entre a média das provas e a nota do exame; aprovação quando a média final arredondada HALF_UP for `>= 5,0`. O lançamento do exame é recusado se faltar nota de prova ou se a média das provas ficar abaixo de `5,0`.
- Implementados: casos de uso com sessão institucional existente, autorização do professor responsável e coordenador para escrita, consulta exclusiva do próprio aluno, persistência MySQL, auditoria de alterações, controle de revisão, idempotência por operação e cálculo/classificação pós-exame.
- Implementados: migração V014 para avaliação, nota e auditoria; teste de integração existente atualizado para instalar o histórico desde V001 e conferir V014; menu Swing integrado para professor, coordenação e aluno, com operações fora da EDT.
- Verificação mais recente: `mvn test` passou com 34 testes; `mvn -Pintegracao verify` passou contra MySQL 8.4 descartável, com 5 testes de integração aprovados. A execução instala V001–V010 desde banco vazio, atualiza até V014 e exercita operações reais de avaliações/notas, permissões, auditoria, idempotência, elegibilidade e resultado de exame. Flyway avisou que MySQL 8.4 é mais novo que a versão máxima oficialmente verificada (8.1), sem impedir a execução.
- A validação interativa da interface Swing em execução não foi feita nesta etapa; está registrada como critério de aceite integrado no cartão ACA-04.
- O IT usa repositórios reais de turmas e inscrições com dados sintéticos. A demonstração com dados originados no fluxo ACA-04 também está registrada no cartão ACA-04.

## Plano de execução das pendências para concluir o ACA-07

**Objetivo:** obter evidência integrada de que as regras, a persistência e os fluxos principais funcionam no ambiente MySQL e na interface da aplicação. O incremento não deve ser marcado como concluído antes dos gates abaixo, mas decisões acadêmicas já não são bloqueio.

| Ordem | Ação | Como executar | Critério de saída |
|---|---|---|---|
| 1. Preparar ambiente descartável | **Concluída.** MySQL 8.4 descartável na porta 3308; URL e confirmação foram verificadas pelo IT. | Compose de teste e credenciais locais descartáveis; sem conexão ao banco da aplicação. | Banco isolado confirmado. |
| 2. Exercitar V001–V014 e o repositório de notas | **Concluída.** Histórico instalado desde V001, atualização V011/V012/V013 e chegada a V014. Operações ACA-07 executadas com entidades acadêmicas sintéticas. | `mvn -Pintegracao verify`; 5 testes Failsafe aprovados contra MySQL. | Histórico completo e persistência validados. |
| 3. Verificar autorização e regra do exame | **Concluída no nível de aplicação/integração.** Cobertos professor responsável/coordenador, recusas, consulta própria, pendência, elegibilidade, correção auditada, idempotência e fronteiras do cálculo final. | Casos de uso e repositórios MySQL reais com IDs sintéticos. | Sem falhas nos testes; concorrência não está coberta por este IT. |
| 4. Executar roteiro Swing de ponta a ponta | **Acompanhada pelo ACA-04.** Transferida ao critério de aceite integrado desse cartão para executar uma vez com o fluxo real. | Ver roteiro ACA-07 na seção ACA-04 de `entrega_modular/01_ACADEMICO.md`. | Registrar evidência no aceite integrado ACA-04. |
| 5. Fechar integração ACA-04 e aceite | **Acompanhada pelo ACA-04.** A validação de origem real dos dados e o roteiro visual foram unificados no mesmo cartão. | Executar com turma/inscrições criadas pelo fluxo ACA-04. | Evidência única no ACA-04, sem reabrir pendência de teste no ACA-07. |

**Estado da execução:** `mvn test` passou (34 testes) e `mvn -Pintegracao verify` passou (34 unitários e 5 Failsafe, sem falhas) em MySQL 8.4 descartável instalado desde V001. O aviso de suporte do Flyway para MySQL 8.4 foi observado. O roteiro Swing e a origem real dos dados ainda precisam ser demonstrados uma única vez como aceite integrado ACA-04.

### Pontos de escopo a registrar durante a demonstração

- A política do responsável agora está fechada: `HALF_UP`, sete dias corridos, elegibilidade ao EF com média das provas `>= 5,0` e fórmula/corte final adotados. Não há decisão acadêmica pendente para a fórmula.
- O resultado final só é calculado quando há uma única avaliação `EXAME_FINAL` na turma e todas as notas requeridas estão presentes. Se a instituição permitir mais de uma aplicação/recuperação de EF por turma, decidir a regra de seleção ou agregação antes de ampliar esse comportamento.
- A auditoria já grava autor, instante, origem, valores anterior/novo e operação. Exigir justificativa textual ou aviso ao aluno para uma correção continua sendo política opcional a confirmar; não impede o fluxo básico descrito no backlog.

## Decisões/gates antes de fechar o escopo

| Item | Estado e ação necessária |
|---|---|
| Escala | Definida: 0,0 a 10,0, no máximo uma casa decimal. |
| Pesos e média | Definidos: peso igual por avaliação e média aritmética, uma casa decimal. |
| Modo de arredondamento | Definido: `HALF_UP`, a uma casa decimal. Uma média de `4,95` vira `5,0` e atinge elegibilidade para exame. |
| Exame final | Definido: estudante só pode fazer o exame se a média das provas for `>= 5,0`; `MF = (MP + EF) / 2`, arredondada HALF_UP a uma casa; aprovado se `MF >= 5,0`. |
| Antecedência | Definida: sete dias corridos. O cadastro e a alteração da data exigem data de aplicação pelo menos sete dias civis após hoje, no fuso institucional. |
| Correção publicada | Auditoria é obrigatória pelo padrão do projeto. Confirmar se a correção exige justificativa ou aviso ao aluno; na falta dessa regra, registrar ao menos autor, instante, origem, valor anterior/novo e operação. |
| ACA-04 | O teste MySQL utiliza repositórios reais de turma e inscrição com dados sintéticos. Demonstração end-to-end com dados originados pelo fluxo ACA-04 e validação visual Swing foram transferidas para os critérios de aceite do ACA-04; executar uma vez nesse cartão. |

## Execução realizada

| Etapa | Resultado |
|---|---|
| Regras já informadas pelo responsável | Aplicadas: escala, pesos iguais, médias, HALF_UP, limiar de elegibilidade, fórmula e corte pós-exame, e antecedência de sete dias corridos. |
| Contratos e autorização | `AutorizacaoAvaliacoes<Sessao>` reutiliza identidade institucional e resolve professor/aluno via `pessoaId`; operações validam perfil e vínculo com turma no servidor. |
| Persistência | V014 cria avaliações, notas em `DECIMAL(3,1)`, chaves, unicidade, revisão otimista e tabelas de auditoria com autor, instante UTC, origem, valores anterior/novo e operação. Gravações são transacionais e idempotentes. |
| Aplicação e interface | Cadastro/edição de avaliação, lançamento/correção de nota, diário de turma e consulta do aluno integrados ao menu Swing existente. |
| Testes | `mvn test`: 34 aprovados. `mvn -Pintegracao verify`: 5 testes Failsafe aprovados contra MySQL 8.4 descartável; o IT instala V001–V010 desde banco vazio, atualiza V011–V014 e verifica persistência e regras ACA-07. |

## Sequência executável

| Etapa | Trabalho | Saída verificável / condição para avançar |
|---|---|---|
| 1. Fechar regras | Concluída: escala, pesos, média, HALF_UP, limiar de elegibilidade, fórmula/corte pós-exame e antecedência mínima de sete dias corridos aplicados. | Nenhuma decisão acadêmica desta etapa pendente. |
| 2. Fechar contratos | Implementado contra os contratos/repos existentes no Acadêmico; autenticação reutiliza sessão institucional. | O fluxo end-to-end de ACA-04 com aulas/frequência ainda não foi demonstrado. |
| 3. Persistência | V014 implementada com `DECIMAL`, FKs, unicidade, revisões e auditoria. | Concluído: IT MySQL V001–V014 aprovado. |
| 4. Aplicação e autorização | Cadastro/edição, lançamento/correção, diário, consulta própria, médias e regras de exame aplicados no servidor. | Concluído para cenários exercitados no IT; concorrência não coberta. |
| 5. Interface Swing | Integrada ao menu; operações de I/O executam fora da EDT. | Homologação visual e navegação ponta a ponta ficam no aceite integrado ACA-04. |
| 6. Testes ACA-07 | Testes unitários e integração MySQL cobrindo regras, autorização, persistência e idempotência. | Concluído para os cenários cobertos; acompanhar cobertura descrita nas limitações. |
| 7. Integração ACA-04 | Consultas usam repositórios reais de turma/inscrição. | Demonstração conjunta registrada e executada uma vez como critério de aceite ACA-04. |
| 8. Revisão e aceite | Implementação e limitações registradas neste plano; testes automatizados executados. | Revisão do ACA-07 acompanha o resultado do aceite integrado ACA-04; sem roteiro duplicado. |

## Casos de teste essenciais

1. Aceitar notas `0,0`, `10,0` e valores de uma casa; recusar `-0,1`, `10,1` e valores como `7,01`.
2. Média com pesos iguais, uma casa decimal e `HALF_UP`; caso-limite `4,95` para elegibilidade ao exame.
3. Uma nota ainda não lançada mantém média/elegibilidade como pendente, sem converter ausência em zero.
4. Média das provas abaixo de `5,0` não habilita exame; média no/acima do limite habilita, aplicando HALF_UP antes da comparação. Média pós-exame `>= 5,0` aprova; abaixo reprova.
5. Professor vinculado e coordenador podem cadastrar/corrigir; outros perfis recebem recusa no servidor.
6. Aluno consulta somente o próprio diário/notas; tentativa de consultar outro aluno é recusada.
7. Correção conserva auditoria; repetição da mesma operação não duplica registros.
8. Persistência MySQL verifica chaves, unicidade, histórico e concorrência em banco descartável.
9. Cadastro permite data de aplicação a partir do sétimo dia corrido; recusa datas com seis dias ou menos. Alterar data aplica a mesma regra; alteração de outros dados mantém a data existente.
10. Fluxo Swing valida navegação real entre login, diário e retorno ao menu após o ACA-04 fornecer o contrato integrado.

## Fora do escopo confirmado

- Não há dependência do Financeiro para ACA-07.
- A frequência automática por controle de acesso à sala é parte do escopo descrito pelo cliente para o módulo Acadêmico, mas deve ser implementada/integrada pelo ACA-04; ACA-07 consome apenas dados expostos pelo contrato Acadêmico se uma regra de resultado aprovada exigir frequência.
- A tela do aluno apresenta o resultado pós-exame quando existe exatamente uma avaliação do tipo exame final e as notas necessárias estão completas; mais de uma avaliação desse tipo sinaliza resultado sem cálculo para revisão.

## Política adotada para o exame final

O estudo de caso não define a fórmula pós-exame. O responsável acadêmico adotou `MF = (MP + EF) / 2`, com aprovação quando `MF >= 5,0`; `MF` é arredondada HALF_UP a uma casa decimal. O lançamento de EF exige média das provas `MP >= 5,0` e todas as notas das provas preenchidas. O modelo aritmético também aparece no guia da Prograd/UFT.

Uma alternativa observada em regulamento institucional é a média ponderada 60/40 (`MF = 0,6 × MP + 0,4 × EF`). O IFRS apresenta essa ponderação junto a critérios próprios para acesso ao exame; ela não foi adotada pelo SCOA.

`MP` é a média aritmética das provas antes do exame e `EF` é a nota do exame, ambas entre `0,0` e `10,0`. O resultado final é arredondado a uma casa decimal antes de comparar com o corte.

Referências: [Guia da Prograd/UFT, cálculo após exame final](https://docs.uft.edu.br/share/proxy/alfresco-noauth/api/internal/shared/node/EEeJ73QgQZmVfvpQTPFfBw/content/Guia%20do%20Coordenador%20de%20Curso%20Prograd%20-%202017.pdf); [página do curso no SIGAA/IFRS, avaliação e exame final](https://sig.ifrs.edu.br/sigaa/public/curso/ppp.jsf?id=85364&lc=pt_BR).
