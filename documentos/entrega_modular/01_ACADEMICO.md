# Pacote 01_ACADEMICO — Integrante A

Responsável nominal: a preencher. Prazo final: 30/11/2026.

Pessoa, aluno, professor, currículo, matrícula, inscrição e resultados acadêmicos. Consumir impedimentos financeiros e bibliotecários sem acessar diretamente as tabelas desses módulos.

16 incrementos; 74 SP de referência. Pontos incluem interface, aplicação, persistência e testes locais. Não equivalem a horas nem asseguram cabimento no prazo. Todos os RF listados permanecem no escopo; P0/P1/P2 do arquivo anterior passam a indicar apenas ordem de construção.

Leia primeiro GOVERNANCA.md. As dependências identificam interfaces/dados necessários; usar implementações simuladas locais até o provedor estar disponível e depois executar obrigatoriamente os testes reais.

## [ACA-01][3 SP] Alunos e professores

- Referência: ACA-01,02. Ordem: P0. Dependências: BAS-02.
- Resultado e aceite: Cadastrar/editar/consultar; identidade duplicada recusada.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-02][5 SP] Cursos, disciplinas, matriz e período

- Referência: ACA-04,22,30. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Matriz versionada; período com datas válidas; pré-requisitos sem ciclos.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-03][5 SP] Turmas, salas e alocação

- Referência: ACA-03,05,20. Ordem: P0. Dependências: ACA-01,02.
- Resultado e aceite: Professor, disciplina e período vinculados; capacidade e conflitos de sala/professor validados.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-04][5 SP] Matrícula e inscrição em turma

- Referência: ACA-18,19. Ordem: P0. Dependências: ACA-03,05.
- Resultado e aceite: Distinguir vínculo e inscrição; recusar duplicidade, pré-requisito, conflito e falta de vaga, inclusive concorrente.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-05][3 SP] Envio e validação documental

- Referência: ACA-14. Ordem: P0. Dependências: ACA-01.
- Resultado e aceite: Envio não aprova documento; responsável registra aceite/recusa com justificativa.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-06][5 SP] Aulas, conteúdo e frequência

- Referência: ACA-10,21. Ordem: P0. Dependências: ACA-04.
- Resultado e aceite: Só professor autorizado registra; conteúdo exigido; sem duplicação de presença ou divisão por zero.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-07][5 SP] Avaliações, notas e resultados

- Referência: ACA-11,12. Ordem: P0. Dependências: ACA-06.
- Resultado e aceite: Escala, antecedência e fórmula aprovadas aplicadas; nota ausente permanece pendente.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-08][5 SP] Auditoria do diário e pauta final

- Referência: ACA-16,24. Ordem: P1. Dependências: ACA-07.
- Resultado e aceite: Coordenador registra análise/homologação; pendências impedem fechamento.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-09][5 SP] Boletim, histórico e horários

- Referência: ACA-08,09,25. Ordem: P1. Dependências: ACA-07.
- Resultado e aceite: Aluno consulta apenas dados próprios; resultado confere com lançamentos e plano.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-10][5 SP] Rematrícula e impedimentos

- Referência: ACA-13. Ordem: P1. Dependências: ACA-07,FIN-03,BIB-04.
- Resultado e aceite: Cada origem de impedimento identificada; regularizar uma não libera as demais; reexecução sem duplicação.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-11][3 SP] Solicitações e documentos acadêmicos

- Referência: ACA-17. Ordem: P2. Dependências: ACA-09.
- Resultado e aceite: Solicitação rastreável; documento emitido apenas com condições e dados completos.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-12A][5 SP] Transferências acadêmicas

- Referência: ACA-27. Ordem: P2. Dependências: ACA-02,04.
- Resultado e aceite: Registrar solicitação e decisão autorizada; deferimento atualiza vínculo preservando origem e histórico; recusa justificada não altera vínculo.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-12B][5 SP] Aproveitamento e equivalência

- Referência: ACA-28. Ordem: P2. Dependências: ACA-02,04.
- Resultado e aceite: Relacionar estudos apresentados aos componentes; somente deferimento altera plano/histórico; recusar pedido duplicado.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-13][5 SP] Avaliação institucional e manifestações

- Referência: ACA-07,26. Ordem: P2. Dependências: BAS-02.
- Resultado e aceite: Respostas e manifestações registradas; consulta limitada ao responsável; política de participação definida.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-14][5 SP] Ponto, prazos e metas docentes

- Referência: ACA-15,23. Ordem: P2. Dependências: ACA-01.
- Resultado e aceite: Sequência de ponto validada; metas/prazo vinculados ao docente e coordenador autorizado.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-15][5 SP] Relatórios acadêmicos e institucionais

- Referência: ACA-06,29. Ordem: P2. Dependências: ACA-07.
- Resultado e aceite: Filtros e totais conferidos; formato institucional acordado; emissão não declara transmissão externa.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## Encerramento obrigatório do pacote

Executar os RF do pacote de ponta a ponta; testar permissões e transações; instalar migrações em banco limpo e existente; entregar seeds, instruções, evidências e limitações. Registrar bugs com passos, esperado/obtido e gravidade. Testes sistêmicos adicionais ficam na reserva de integração, sem duplicar os SP dos testes locais.
