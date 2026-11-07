# Pacote 02_FINANCEIRO — Integrante B

Responsável nominal: a preencher. Prazo final: 30/11/2026.

Contratos, cobrança, benefícios, pagamentos e repasses. Manter os contratos de integração versionados e executar o fluxo acadêmico-financeiro com A.

11 incrementos; 43 SP de referência. Pontos incluem interface, aplicação, persistência e testes locais. Não equivalem a horas nem asseguram cabimento no prazo. Todos os RF listados permanecem no escopo; P0/P1/P2 do arquivo anterior passam a indicar apenas ordem de construção.

Leia primeiro GOVERNANCA.md. As dependências identificam interfaces/dados necessários; usar implementações simuladas locais até o provedor estar disponível e depois executar obrigatoriamente os testes reais.

## [FIN-01][3 SP] Contratos e consulta de cobranças

- Referência: FIN-01. Ordem: P0. Dependências: ACA-01,BAS-02.
- Resultado e aceite: Modalidade/vigência registradas; aluno consulta apenas suas cobranças; devedor de multa pode não ser aluno.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-02][5 SP] Mensalidades e documento de cobrança

- Referência: FIN-02,03,04. Ordem: P0. Dependências: FIN-01,ACA-04.
- Resultado e aceite: Fórmula e proporcionalidade acordadas; memória de cálculo; reexecução sem duplicidade; natureza demonstrativa identificada quando aplicável.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-03][5 SP] Pagamentos e saldo

- Referência: FIN-08. Ordem: P0. Dependências: FIN-02.
- Resultado e aceite: Comprovante não duplica pagamento; parcial mantém saldo; quitação apenas após recebimento registrado.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-04][3 SP] Multa integrada ao financeiro

- Referência: FIN-10. Ordem: P0. Dependências: FIN-03,BIB-04.
- Resultado e aceite: Mesma multa gera um débito; quitação/cancelamento retorna à biblioteca; atende todos os devedores elegíveis.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-05][5 SP] Ajuste por alteração acadêmica

- Referência: FIN-09. Ordem: P1. Dependências: FIN-02,03.
- Resultado e aceite: Alteração de carga recalcula conforme contrato; cobrança paga preserva pagamento e histórico.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-06][5 SP] Bolsas e sua aplicação

- Referência: FIN-05. Ordem: P2. Dependências: FIN-02.
- Resultado e aceite: Vigência/cobertura/limites respeitados; benefício não equivale a recebimento.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-07A][3 SP] Financiamentos públicos

- Referência: FIN-06. Ordem: P2. Dependências: FIN-06.
- Resultado e aceite: Registrar contrato, vigência, cobertura e programa; concessão não registra recebimento.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-07B][3 SP] Financiamentos privados

- Referência: FIN-07. Ordem: P2. Dependências: FIN-06.
- Resultado e aceite: Registrar agente, vigência e cobertura; renovação/liquidação condicionadas à confirmação exigida.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-07C][5 SP] Repasses vinculados às cobranças

- Referência: FIN-06,07. Ordem: P2. Dependências: FIN-07A,07B.
- Resultado e aceite: Distinguir previsto, recebido e cancelado; recebimento atualiza saldo uma única vez e preserva origem.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [FIN-08][3 SP] Relatórios financeiros

- Referência: FIN-11. Ordem: P1. Dependências: FIN-03.
- Resultado e aceite: Totais de saldo/recebimento conferem com dados e filtros; acesso restrito.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [INT-01][3 SP] Contratos de integração e identidade

- Referência: ADM-04. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Interfaces e IDs comuns acordados antes do trabalho paralelo; integrações executáveis verificadas em QA-01/02.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## Encerramento obrigatório do pacote

Executar os RF do pacote de ponta a ponta; testar permissões e transações; instalar migrações em banco limpo e existente; entregar seeds, instruções, evidências e limitações. Registrar bugs com passos, esperado/obtido e gravidade. Testes sistêmicos adicionais ficam na reserva de integração, sem duplicar os SP dos testes locais.
