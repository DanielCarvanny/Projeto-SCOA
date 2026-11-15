# Plano de testes e entrega — SCOA

Fonte vigente de QA/REL, extraída do backlog em 09/11/2026. Prazo: 30/11/2026. Todos os itens começam pendentes; nenhum teste foi executado nesta reorganização. P0 indica ordem de execução, sem excluir os demais requisitos dos pacotes.



| ID | Entrega | Prioridade | SP | Dependência/aceite |
|---|---|---|---:|---|
| QA-01 | Fluxo acadêmico-financeiro integrado | P0 | 5 | Após ACA-04 e FIN-03: matrícula → inscrição → cobrança → recebimento; negativos e totais documentados. |
| QA-02 | Fluxo biblioteca-financeiro integrado | P0 | 5 | Após FIN-04: empréstimo → devolução/multa → cobrança → pagamento; sem dupla cobrança nem quitação implícita. |
| QA-03 | Estoque e autorização administrativa | P0 | 3 | Após ALM-02 e ADM-02: concorrência de saídas, limites e autorizações; nenhuma perda de saldo/histórico. |
| QA-04 | Segurança, validação e auditoria | P0 | 5 | Após fluxos P0: acesso entre usuários/papéis, alteração indevida e entradas inválidas; evidências de recusa. |
| QA-05 | Regressão, interface e desempenho | P0 | 5 | Versão candidata: fluxos do compromisso, navegadores acordados; consultas/cadastros selecionados abaixo de 3 s na carga registrada. |
| QA-06 | Homologação da equipe | P0 | 3 | Após QA-01 a 05: matriz de aceite com resultado e evidência; falhas impedidoras corrigidas e retestadas. |
| REL-01 | Instalação, dados e manual de execução | P0 | 3 | Colega instala versão limpa; restaura dados; executa roteiro sem apoio do autor. |
| REL-02 | Demonstração e pacote final | P0 | 2 | Após QA-06/REL-01: versão identificada, repositório, instruções, evidências e limitações; ensaio dos cinco módulos. |

Falhas descobertas geram cartões BUG vinculados ao teste: passos, esperado/obtido, gravidade e evidência. Bloqueador = impede instalação/fluxo de aceite; crítico = perda/inconsistência de dados ou acesso indevido. Ambos devem estar zerados na entrega. Defeitos menores exigem registro e decisão explícita. Não inventar SP de bugs antes de diagnóstico; usar a reserva de capacidade e replanejar quando consumida.


## Responsabilidade e abrangência

A+B: QA-01. B+C: QA-02. D com revisão de C: QA-03. Todos executam QA-04/05 sobre seus módulos; C consolida evidências. QA-06 exige aceite de cada responsável. D mantém REL-01; C consolida REL-02 com os quatro integrantes.

Os fluxos QA-01/02/03 são cenários centrais, não cobertura completa dos 72 RF. QA-05/06 devem verificar também todos os incrementos entregues dos quatro pacotes, incluindo bolsas, financiamentos, folha, patrimônio, relatórios e demais funcionalidades. Requisitos não implementados devem ser explicitamente registrados, sem serem considerados aprovados.

## Registro de evidência

Para cada cenário: ID QA/REL e RF/cartão relacionado, versão/commit, ambiente e dados sintéticos, pré-condições, passos, resultado esperado, resultado obtido, data, executor, evidência e bug associado. Status: pendente, aprovado, reprovado ou bloqueado. Teste não executado nunca equivale a aprovado.

Testes locais integram os SP dos incrementos. Estes 31 SP são referência adicional para integração e entrega, não horas; defeitos usam a reserva de capacidade. Carga de desempenho, navegadores e operações selecionadas precisam ser registrados antes da medição.

## Critério de liberação

Versão instalada do zero por colega, migrações verificadas, fluxos reais integrados, permissões e integridade validadas, sem defeitos bloqueadores/críticos. Limitações e requisitos restantes explicitados e submetidos ao responsável pelo aceite. Evidências vinculadas ao commit entregue. Não declarar integração concluída usando apenas mocks.

Marcos: funcionalidades até 22/11; testes sistêmicos/correções de 23 a 26/11; homologação/ensaio de 27 a 29/11; entrega em 30/11. Consultar entrega_modular/GOVERNANCA.md para a coordenação assíncrona.
