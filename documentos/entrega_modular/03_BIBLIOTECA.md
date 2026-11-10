# Pacote 03_BIBLIOTECA — Integrante C

Responsável nominal: a preencher. Prazo final: 30/11/2026.

Acervo, circulação e multas. Conduzir também a montagem do roteiro de demonstração e evidências finais, recebendo evidências dos demais.

6 incrementos; 26 SP de referência. Pontos incluem interface, aplicação, persistência e testes locais. Não equivalem a horas nem asseguram cabimento no prazo. Todos os RF listados permanecem no escopo; P0/P1/P2 do arquivo anterior passam a indicar apenas ordem de construção.

Leia primeiro AGENTS.md na raiz e documentos/entrega_modular/GOVERNANCA.md. Consulte documentos/PLANO_TESTES_ENTREGA.md para QA/REL. As dependências identificam interfaces/dados necessários; usar implementações simuladas locais até o provedor estar disponível e depois executar obrigatoriamente os testes reais.

## [BIB-01][3 SP] Acervo, exemplares e pesquisa

- Referência: BIB-01,08. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Obra distinta de exemplar; busca e disponibilidade corretas; tipos exigidos representados.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BIB-02][5 SP] Usuários e empréstimos

- Referência: BIB-02,03. Ordem: P0. Dependências: BIB-01,BAS-02.
- Resultado e aceite: Identidade institucional, atendente e prazo registrados; exemplar tem no máximo um empréstimo aberto.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BIB-03][3 SP] Devolução do exemplar

- Referência: BIB-03. Ordem: P0. Dependências: BIB-02.
- Resultado e aceite: Data e condição registradas; disponibilidade atualizada; multa não impede recebimento físico.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BIB-04][5 SP] Multas e pendências

- Referência: BIB-05,06,10. Ordem: P0. Dependências: BIB-03.
- Resultado e aceite: Cálculo parametrizado e origem preservados; devolução não quita multa; pendência disponível ao Acadêmico.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BIB-05][5 SP] Reservas e renovações

- Referência: BIB-11,12. Ordem: P1. Dependências: BIB-02.
- Resultado e aceite: Até duas renovações; reserva alheia bloqueia; datas e contagem persistidas.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [BIB-06][5 SP] Avisos, relatórios e ajuda

- Referência: BIB-04,07,09. Ordem: P2. Dependências: BIB-02.
- Resultado e aceite: Aviso com antecedência definida e resultado registrado; relatórios conferidos; instruções acessíveis.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## Encerramento obrigatório do pacote

Executar os RF do pacote de ponta a ponta; testar permissões e transações; instalar migrações em banco limpo e existente; entregar seeds, instruções, evidências e limitações. Registrar bugs com passos, esperado/obtido e gravidade. Testes sistêmicos adicionais ficam na reserva de integração, sem duplicar os SP dos testes locais.
