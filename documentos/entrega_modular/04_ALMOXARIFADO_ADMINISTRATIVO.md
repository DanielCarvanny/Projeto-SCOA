# Pacote 04_ALMOXARIFADO_ADMINISTRATIVO — Integrante D

Responsável nominal: a preencher. Prazo final: 30/11/2026.

Materiais, patrimônio, recursos e folha. Entregar a fundação compartilhada primeiro; os demais fornecem testes e migrações de seus módulos.

13 incrementos; 49 SP de referência. Pontos incluem interface, aplicação, persistência e testes locais. Não equivalem a horas nem asseguram cabimento no prazo. Todos os RF listados permanecem no escopo; P0/P1/P2 do arquivo anterior passam a indicar apenas ordem de construção.

Leia primeiro GOVERNANCA.md. As dependências identificam interfaces/dados necessários; usar implementações simuladas locais até o provedor estar disponível e depois executar obrigatoriamente os testes reais.

## [BAS-01][3 SP] Aplicação executável e banco versionado

- Referência: Técnico. Ordem: P0. Dependências: —.
- Resultado e aceite: Colega instala a partir do README, aplica migrações e executa verificação básica.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BAS-02][5 SP] Identidade, login e permissões

- Referência: ADM-09. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Pessoa comum aos módulos; acesso indevido recusado também no servidor; papéis necessários disponíveis.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BAS-03][3 SP] Auditoria e dados de demonstração

- Referência: RnF transversais. Ordem: P0. Dependências: BAS-02.
- Resultado e aceite: Alteração relevante registra autor/data/origem; ambiente de demonstração pode ser reconstruído.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ALM-01][3 SP] Produtos e localizações

- Referência: ALM-01,02,05. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Tipos e unidade definidos; local e limites coerentes; regra de código de barras decidida.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ALM-02][5 SP] Movimentações e níveis de estoque

- Referência: ALM-04,09. Ordem: P0. Dependências: ALM-01,BAS-02.
- Resultado e aceite: Entrada/saída com autor; impedir saldo negativo inclusive concorrente; alertar mínimo/máximo.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ALM-03][3 SP] Retirada e devolução de material

- Referência: ALM-10. Ordem: P1. Dependências: ALM-02.
- Resultado e aceite: Responsável e prazo registrados; devolução vinculada à retirada; disponibilidade coerente.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ALM-04][5 SP] Tombamento, transferência e baixa

- Referência: ALM-06,07,08. Ordem: P1. Dependências: ALM-01,ADM-01.
- Resultado e aceite: Tombo único; histórico por setor; baixa justificada torna bem indisponível.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ALM-05][3 SP] Relatórios de estoque e patrimônio

- Referência: ALM-03. Ordem: P2. Dependências: ALM-02,04.
- Resultado e aceite: Saldos, movimentações e bens por setor conferem com registros.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ADM-01][3 SP] Funcionários, setores e centros de custo

- Referência: ADM-01,05. Ordem: P0. Dependências: BAS-02.
- Resultado e aceite: Identidade comum, vínculo e recursos registrados; acesso pessoal restrito.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ADM-02][5 SP] Distribuição e autorização de recursos

- Referência: ADM-02,03. Ordem: P0. Dependências: ADM-01.
- Resultado e aceite: Origem/destino e decisão auditáveis; limite aplicado; aprovação distinta de execução.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ADM-03A][5 SP] Calcular folha por competência

- Referência: ADM-06. Ordem: P1. Dependências: ADM-01.
- Resultado e aceite: Persistir rubricas, parâmetros e totais; excluir inativos/pendências; reprocessar sem duplicar competência.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ADM-03B][3 SP] Validar e emitir folha

- Referência: ADM-07. Ordem: P1. Dependências: ADM-03A.
- Resultado e aceite: Registrar validação; emitir consolidado e demonstrativo individual a partir do cálculo validado; acesso remuneratório restrito.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ADM-04][3 SP] Relatórios por centro de custo

- Referência: ADM-08. Ordem: P2. Dependências: ADM-02.
- Resultado e aceite: Filtros e totais conferidos; acesso limitado.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## Encerramento obrigatório do pacote

Executar os RF do pacote de ponta a ponta; testar permissões e transações; instalar migrações em banco limpo e existente; entregar seeds, instruções, evidências e limitações. Registrar bugs com passos, esperado/obtido e gravidade. Testes sistêmicos adicionais ficam na reserva de integração, sem duplicar os SP dos testes locais.
