> Atualização de responsabilidade — 13/11/2026: por determinação explícita de Dan nesta conversa, Pessoa, conta, login, sessão e permissões do Acadêmico são implementados por A. A atribuição anterior dessa identidade a D e a espera pela Fundação estão superadas para este escopo. D mantém as demais responsabilidades de infraestrutura; contratos consumidos por outros módulos continuam sujeitos ao acordo com seus responsáveis. Não há alteração de pontos nem aceite automático do incremento.

# Pacote 01_ACADEMICO — Integrante A

Responsável nominal: Dan (A). Prazo final: 30/11/2026.

Pessoa, aluno, professor, currículo, matrícula, inscrição e resultados acadêmicos. Consumir impedimentos financeiros e bibliotecários sem acessar diretamente as tabelas desses módulos.

15 incrementos; 72 SP de referência. Pontos incluem interface, aplicação, persistência e testes locais. Não equivalem a horas nem asseguram cabimento no prazo. Todos os RF listados permanecem no escopo; P0/P1/P2 do arquivo anterior passam a indicar apenas ordem de construção.

Leia primeiro AGENTS.md na raiz e documentos/entrega_modular/GOVERNANCA.md. Consulte documentos/planejamento/PLANO_TESTES_ENTREGA.md para QA/REL. As dependências identificam interfaces/dados necessários; usar implementações simuladas locais até o provedor estar disponível e depois executar obrigatoriamente os testes reais.

## [ACA-01][3 SP] Alunos e professores

Atualização técnica em 13/11/2026: implementados cadastro de vínculos para Pessoa existente, busca de Professor por CPF, ficha completa e pré-requisitos da conta. 153 testes unitários e 37 de integração aprovados. Aguardam homologação humana; uma conta antiga de Professor sem vínculo foi identificada para regularização. Sem alteração de pontos/aceite. Roteiro: ../academico/ENTREGA_PESSOA_VINCULOS_CONTA.md.

- Referência: ACA-01,02. Ordem: P0. Dependências: identidade BAS-02 incorporada ao trabalho de A; implementação disponível para revisão, sem aceite final. Veja ../academico/IDENTIDADE_ACADEMICA.md.
- Resultado e aceite: Cadastrar/editar/consultar; identidade duplicada recusada.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-02][5 SP] Cursos, disciplinas, matriz e período

Fechamento técnico — 22/11/2026: ACA-02 está concluído tecnicamente, sem etapa de homologação manual por decisão de Dan. Curso, Disciplina, Matriz, Componentes, Pré-requisitos, Publicação/Cópia de versões e Período Letivo foram integrados, documentados e validados por testes automatizados. A referência de execução, evidências e limitações está no [roteiro de Períodos e conferência do ACA-02](../academico/ACA02_PERIODOS_HOMOLOGACAO.md). Refinamentos de UX continuam no backlog próprio e não bloqueiam a entrega. RF-22 continua dependendo do plano individual em incremento posterior. Esta atualização não altera os 5 SP.

Atualização de pré-requisitos em 16/11/2026: implementadas configuração em matriz em elaboração, validação de ciclos, proteção de componentes e auditoria V005. Validação final: 190 testes unitários/de interface e 66 de integração MySQL aprovados. [Roteiro de homologação](../academico/ACA02_PRE_REQUISITOS_HOMOLOGACAO.md). Publicação/cópia de versões, períodos e refinamentos de UX continuam pendentes. Sem alteração de pontos ou aceite final.

Refinamentos registrados em 16/11/2026 para implementação futura: calendário sincronizado com digitação, análise da versão curricular, clareza do fluxo de salvar e comparação explícita de revisões de disciplina. Ver [avaliação e critérios de UX](../academico/ACA02_REFINAMENTOS_UX.md). O significado institucional da versão permanece pendente; nenhuma nova restrição foi aplicada.

Atualização técnica em 16/11/2026: Curso/Disciplina e matriz em elaboração com componentes implementados para homologação. Migrações V003/V004, composição nunca vazia, revisão histórica da disciplina e concorrência verificadas. Suíte de 15/11: 181 testes unitários/de interface e 53 de integração aprovados. Pré-requisitos, publicação/cópia de versões e períodos continuam pendentes. Sem alteração de pontos ou aceite final. Roteiro: [Matrizes e componentes](../academico/ACA02_MATRIZES_HOMOLOGACAO.md).

- Referência: ACA-04,22,30. Ordem: P0. Dependências: BAS-01.
- Resultado e aceite: Matriz versionada; período com datas válidas; pré-requisitos sem ciclos.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-03][5 SP] Turmas, salas e alocação

Conclusão técnica em 22/11/2026 (commit bb5445f): Entidades de Sala, Turma e Horário, caso de uso GestaoTurmas, migrações V008 e V009, persistência MySQL com validação transacional de conflitos de horário de sala/professor e auditoria, interface Swing com fluxo guiado (cadastro, consulta, ficha, edição, transições e alocação) e demonstração em memória implementadas e validadas por 339 testes automatizados (unitários, EDT e integração MySQL). Roteiro: [Homologação ACA-03](../academico/ACA03_HOMOLOGACAO.md) e [Arquitetura ACA-03](../academico/ACA03_ARQUITETURA.md). Esta atualização não altera os 5 SP.

- Referência: ACA-03,05,20. Ordem: P0. Dependências: ACA-01,02.
- Resultado e aceite: Professor, disciplina e período vinculados; capacidade e conflitos de sala/professor validados.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-04][8 SP] Matrícula, inscrição, aulas e frequência

Nota de implementação aprovada em 11/11/2026: incluir consulta por número de MatriculaCurso, apresentando aluno e curso; contemplar alunos sem matrícula e com múltiplos vínculos. Não renomear idAluno como matrícula. Esta nota não altera pontos, prazo ou status de aceite.

- Referência: ACA-18,19,10,21. Ordem: P0. Dependências: ACA-03,05,INT-01.
- Resultado e aceite: Distinguir vínculo e inscrição; recusar duplicidade, pré-requisito, conflito e falta de vaga, inclusive concorrente. Só professor autorizado registra aula com conteúdo obrigatório e frequência dos alunos inscritos; impedir frequência duplicada e divisão por zero.
- Aceite integrado ACA-07: ao concluir o fluxo ACA-04, executar na aplicação Swing o roteiro completo de avaliações com dados criados pelo próprio ACA-04: professor responsável e coordenador cadastram avaliação e lançam/corrigem notas; aluno consulta somente as próprias notas/resultados; validar persistência, permissões, navegação de retorno ao menu e funcionamento da janela durante as operações. Essa homologação pertence a este cartão para ser feita junto com o fluxo acadêmico, sem abrir nova pendência de execução no ACA-07.
- A implementação ACA-07 precisa estar disponível apenas para essa homologação integrada; isso não bloqueia o desenvolvimento independente do ACA-04. O aceite conjunto aguarda os dois fluxos.
- Dependências externas: registrar como bloqueios no próprio incremento contratos/provedores indisponíveis, incluindo consulta real de impedimentos quando aplicável. Simulações podem apoiar desenvolvimento, mas não contam como integração concluída nem satisfazem o aceite.
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


## [ACA-07][5 SP] Avaliações, notas e resultados

- Referência: ACA-11,12. Ordem: P0. Dependências: ACA-04.
- Resultado e aceite: Notas de 0,0 a 10,0 com uma casa decimal; antecedência mínima de sete dias corridos; médias HALF_UP. Após exame, MF = (MP + EF) / 2 e aprovação se MF >= 5,0; nota ausente permanece pendente.
- Entrega: caso de uso, persistência/migração, interface utilizável, validações e autorização aplicáveis; dados de demonstração.
- Testes: caminho válido, entrada inválida, acesso indevido e reexecução quando houver escrita; acrescentar cenários específicos do aceite.
- Evidência: comando de teste e resultado, roteiro curto reproduzível, referência do commit e captura da interface quando aplicável.
- Subtarefas para agentes: (1) conferir contrato/modelo; (2) implementar caso de uso e persistência; (3) integrar interface; (4) testar e revisar. O integrante integra e aceita o resultado.

## [ACA-08][5 SP] Auditoria do diário e pauta final

- Referência: ACA-16,24. Ordem: P1. Dependências: ACA-07.
- Resultado e aceite: Coordenador pode auditar todas as turmas, registrar análise/homologação e fechar a pauta somente quando não houver nota de prova ausente, exame pendente para aluno elegível ou pendência obrigatória de conteúdo/frequência informada pelo ACA-04. Aluno inelegível ao exame é reprovado pela média das provas. Após homologação, alterações de avaliações/notas ficam bloqueadas até reabertura auditada pelo coordenador.
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

- Referência: ACA-07,26. Ordem: P2. Dependências: identidade BAS-02 incorporada ao trabalho de A; implementação disponível para revisão, sem aceite final. Veja ../academico/IDENTIDADE_ACADEMICA.md.
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
