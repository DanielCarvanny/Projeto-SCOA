# Execução por módulo, sem reunião de refinamento

Esta organização é vigente; o planejamento anterior está arquivado em documentos/historico/BACKLOG_ENTREGA_SUBSTITUIDO.md e não orienta a execução atual. Mantém todos os requisitos, com estimativas de referência atribuídas pelo PO. Como há cinco módulos para quatro pessoas, a proposta agrupa Almoxarifado e Administrativo no mesmo responsável, preservando sua separação técnica. Os nomes dos quatro responsáveis podem ser preenchidos diretamente nos pacotes.

## Pontos e execução

3 SP: incremento localizado; 5 SP: múltiplas regras ou integração. Os antigos cartões de 8 foram divididos. Pontos medem tamanho/incerteza, não horas nem quantidade de agentes. Um integrante pode registrar uma alteração de estimativa com motivo sem convocar reunião. O uso de IA não elimina revisão humana, testes ou integração. O escopo completo continua arriscado até 30/11; a disponibilidade informada não permite garantir a entrega.

No Trello: uma etiqueta por módulo e um membro responsável por cartão. Listas: A fazer, Em implementação, Em revisão/teste, Bloqueado, Concluído. Limite de um incremento principal em implementação por integrante. Agentes trabalham nas subtarefas desse incremento. Ao bloquear, registrar motivo, responsável pela dependência e próxima ação. Atualização assíncrona diária: concluído com evidência / próximo incremento / impedimento. Não exige reunião.

## Base técnica e propriedade

Preservar monólito modular, Clean Architecture e MySQL definidos no documento. Um único repositório e uma única aplicação integrável. D entrega BAS-01/02/03; B entrega INT-01. Stack ainda não confirmada: D registra linguagem, framework, versões e comando de execução em uma decisão escrita até 08/11; os outros seguem essa decisão antes de gerar código. Este é um bloqueio técnico explícito, não uma escolha que cada agente pode fazer isoladamente.

Cada integrante possui seus diretórios, migrações e testes. Alteração no núcleo compartilhado deve passar pelo responsável D. B mantém contratos compartilhados com participação assíncrona dos provedores. Agentes usam branches/worktrees separados e não editam simultaneamente o mesmo arquivo. O humano revisa e integra cada incremento. Alterações incompatíveis de contrato precisam identificar consumidores e atualizar os respectivos testes antes do merge.

## Contratos mínimos propostos

São interfaces internas do monólito, não novos microsserviços. B deve materializá-las no código na stack escolhida e publicar exemplos válidos/inválidos. IDs são estáveis e opacos para consumidores; dinheiro usa decimal e moeda; datas seguem formato único acordado; toda escrita recebe autor e identificador de operação quando reprocessável.

| Provedor → consumidor | Operação e dados mínimos | Garantia |
|---|---|---|
| Identidade → todos | Consultar pessoa(id): identificação, situação; verificar permissão(usuario, ação, âmbito) | Mesmo indivíduo mantém pessoaId comum; rejeitar acesso sem permissão. |
| Acadêmico → Financeiro | Consultar inscrições válidas(matriculaId, periodoId): inscricaoId, disciplinaId, cargaHoraria, situação, versão | Vínculo e período explícitos; não cobrar inscrição inválida. |
| Financeiro → Acadêmico | Consultar impedimentos(pessoaId): origemId, situação, motivo, atualizadoEm | Falha de consulta não equivale a ausência de impedimento. |
| Biblioteca → Acadêmico | Consultar impedimentos(pessoaId): origemId, situação, motivo, atualizadoEm | Regularizar um registro preserva outros impedimentos. |
| Biblioteca → Financeiro | Registrar multa(multaId, pessoaId, valor, motivo, data, operacaoId) → cobrancaId | Repetição da mesma multa retorna vínculo existente sem novo débito; admite pessoa não aluna. |
| Financeiro → Biblioteca | Consultar situação(cobrancaId): saldo, situação, atualizadoEm | Quitação não registra devolução física. |
| Administrativo → Almoxarifado | Consultar setor/centro(id): identificação e situação | Destinação exige referência válida; propriedade do cadastro permanece no Administrativo. |

Erros comuns: não encontrado, entrada inválida, não autorizado, conflito e indisponibilidade. Não usar texto livre como contrato de controle. Simulações locais devem ter os mesmos contratos e testes; comprovar integração real antes da entrega. Não escrever diretamente nas tabelas de outro módulo.

## Decisões pendentes, com responsável

A: médias, exame final, frequência e calendário. B: fórmula de mensalidade, benefícios, parcial e ajustes. C: tarifas de multas, renovação e avisos. D: folha, limites de autorização e código de barras. Cada um registra os parâmetros adotados e marca os não definidos no documento como hipótese de demonstração sujeita à confirmação do responsável acadêmico. Nenhum agente deve apresentar hipótese como requisito aprovado. Serviços externos demonstrativos devem estar identificados na interface.

## Testes integrados e responsabilidade

A+B executam QA-01; B+C executam QA-02; D executa QA-03 com revisão de C. Cada integrante testa acesso e auditoria de seu módulo (QA-04); todos fornecem regressão e medições de suas operações (QA-05). C consolida evidências, A coordena aceite do fluxo acadêmico e cada responsável aceita seu módulo (QA-06). D mantém instalação reproduzível (REL-01); C monta roteiro/pacote com contribuição dos quatro (REL-02). IDs e critérios detalhados estão em documentos/PLANO_TESTES_ENTREGA.md. Essas atividades têm capacidade reservada e não desaparecem com a divisão por módulo.

## Datas de controle assíncrono

- Até 08/11: stack, aplicação-base, identidade e contratos publicados; responsáveis preenchidos.
- Até 12/11: um fluxo persistido e demonstrável por pacote; integração inicial executada. Falha deste marco exige comunicar risco e ajustar plano por escrito.
- Até 15/11: matrícula–cobrança e empréstimo–multa conectados; materiais e recursos administrativos demonstráveis.
- Até 22/11: concluir funcionalidades planejadas; registrar explicitamente qualquer RF restante. Congelamento não autoriza omissão silenciosa de requisito.
- 23–26/11: testes sistêmicos, instalação limpa, correções e regressão.
- 27–29/11: homologação e ensaio com dados preparados.
- 30/11: entrega com versão identificada, evidências e limitações registradas.

## Instrução inicial para a equipe de agentes de cada integrante

“Leia GOVERNANCA.md, meu pacote e as seções correspondentes do documento do SCOA. Implemente um incremento por vez na ordem das dependências. Respeite a stack, os contratos e a propriedade dos arquivos compartilhados. Antes de codificar, reporte lacunas que afetem o aceite; não invente regra institucional. Entregue código integrado, migração, interface, testes executados e roteiro reproduzível. Declare o que não foi testado. Não marque um cartão concluído com dados simulados onde se exige integração real. O responsável humano revisará e aceitará a entrega.”
