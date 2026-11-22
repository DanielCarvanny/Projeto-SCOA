# ACA-03 — arquitetura de Turmas, Salas e Alocação

Data: 22/11/2026. Responsável: A/Dan — Acadêmico. Status: planejamento técnico; implementação ainda não iniciada.

## Escopo confirmado no documento-base

O ACA-03 cobre RF-03, RF-05 e RF-20. A documentação define uma **Turma** como oferta de uma Disciplina em um Período Letivo, com código, capacidade, professor responsável e um ou mais horários. Uma **Sala** possui identificação, localização, capacidade, recursos e situação. Cada horário pertence a uma turma e pode, inicialmente, não ter sala; a alocação de sala ocorre por horário, não por toda a turma de uma vez.

| Entidade | Dados documentados | Invariantes já definidos |
|---|---|---|
| Sala | identificação, localização, capacidade, recursos, situação | identificação única; capacidade maior que zero; situação DISPONIVEL ou INDISPONIVEL |
| Turma | código, capacidade, situação, período, disciplina, professor | código único por período; capacidade maior que zero; situações PLANEJADA, ABERTA, EM_ANDAMENTO, ENCERRADA ou CANCELADA |
| HorarioTurma | dia da semana, hora inicial/final, turma e sala opcional | único por turma/dia/início/fim; início menor que fim; dia entre SEGUNDA e DOMINGO |

Turmas não criam inscrições automaticamente. Vagas e quantidade de inscritos serão consumidas pelo ACA-04; não serão simuladas como inscritas no ACA-03.

## Limites e dependências

- Reutilizar Professor, Disciplina e Período Letivo já existentes, por interfaces e consultas internas do Acadêmico.
- A turma referencia uma Disciplina ativa e uma revisão de conteúdo explícita. A decisão de congelar a revisão na oferta será aplicada para preservar o conteúdo ofertado ao longo do período, como já ocorre na Matriz.
- A turma referencia Professor por seu identificador de vínculo, sem presumir igualdade com pessoaId ou contaId.
- O período deve existir. A regra de exigir período ABERTO para criar ou alterar turma **não está definida no documento** e não será presumida; a situação aplicável será proposta no refinamento do caso de uso.
- Matrículas, inscrições, diário, avaliações, planos de estudo e bloqueios financeiros/bibliotecários permanecem fora deste incremento.
- Não criar dependências, alterações de outro módulo ou tabelas de inscrição antecipadamente.

## Regras a implementar com TDD

1. Coordenador é o único perfil que cadastra, edita, consulta e aloca turmas/salas.
2. Código de turma é único dentro do período; a mesma disciplina pode ter mais de uma turma se os códigos forem diferentes.
3. Sala exige identificação e localização não vazias, capacidade positiva e recursos textuais opcionais.
4. Turma exige período, disciplina, professor, código, capacidade positiva e ao menos um horário antes de confirmar o cadastro.
5. Horário deve ter dia válido e intervalo estritamente positivo no mesmo dia.
6. Um professor não pode ter horários sobrepostos em turmas diferentes do mesmo período.
7. Uma sala não pode ter horários sobrepostos quando estiver alocada; sua capacidade deve comportar a turma.
8. Sala indisponível não pode receber alocação. A política de recursos necessários ainda não está formalizada; recursos serão apresentados na escolha, sem inferir requisitos automáticos.
9. Escritas, verificação de conflito e auditoria precisam ocorrer na mesma transação. A aplicação não pode confiar somente no estado carregado pela tela.
10. Edição concorrente deve ser recusada por revisão técnica. Repetir uma confirmação não pode gerar outra turma, horário ou auditoria.

## Decisões pendentes para o início da implementação

| Tema | Estado | Proposta técnica, ainda não aplicada |
|---|---|---|
| Situação de período para criar/editar turma | Não definido | Permitir somente período PLANEJADO ou ABERTO; impedir ENCERRADO |
| Situação inicial da turma | Não definido | PLANEJADA, com abertura/encerramento como transições explícitas futuras |
| Edição e cancelamento de turma com inscrições | Depende do ACA-04 | Bloquear alteração estrutural e exclusão depois da primeira inscrição; registrar cancelamento em vez de excluir |
| Conflito de professor em períodos distintos | Não definido | Comparar horários somente dentro do mesmo período até existir calendário institucional transversal |
| Recursos de sala | Sem catálogo de requisitos | Usar texto livre na sala; não validar correspondência automática até o requisito ser definido |

Essas propostas serão separadas das regras documentadas. Nenhuma será apresentada como requisito aprovado sem confirmação posterior.

## Estrutura proposta

```text
apresentacao/turmas      Fluxos de Sala, Turma, horários e alocação
aplicacao/turmas         Casos de uso, portas de persistência e autorização
dominio/turmas           Sala, Turma, HorarioTurma, estados e validação de conflito
infraestrutura/persistencia
                         Adaptadores JDBC e migrações V008+
```

As camadas de domínio não dependerão de Swing ou JDBC. Apresentação valida formato e chama aplicação; persistência aplica bloqueios, FKs e auditoria. Erros terão códigos estáveis e mensagens específicas na tela.

## Persistência proposta

As próximas migrações deverão criar `sala`, `turma`, `horario_turma` e históricos de Sala/Turma, mantendo o padrão do Acadêmico: IDs BIGINT, `versao_registro`, autor, instante UTC, origem e UUID de operação.

`horario_turma` terá FKs para Turma e Sala opcional, índice por `(sala_id, dia_semana, hora_inicio, hora_fim)` e unicidade do intervalo dentro da turma. A sobreposição não é totalmente representável por índice MySQL: a transação precisa bloquear as candidatas de sala/professor/período e verificar intervalos reais antes de gravar. O teste de concorrência deve demonstrar que duas alocações conflitantes não podem ser confirmadas ao mesmo tempo.

## Fluxos de tela

```text
Menu Acadêmico
  ├─ Salas: cadastrar → revisar → confirmar | consultar → ficha → editar
  └─ Turmas: selecionar período/disciplina/professor → dados → horários
             → revisar → confirmar | consultar → ficha → editar/alocar sala
```

Na criação de turma, disciplina, período e professor serão escolhidos por código/nome em listas; a tela não pedirá IDs técnicos. Alocação mostrará cada horário sem sala ou com sala atual, a capacidade da turma e os recursos informados na sala. Cancelar em qualquer etapa não grava.

## Ordem de execução

1. Converter as decisões pendentes aprovadas em critérios de aceite.
2. Escrever testes de domínio para Sala, horário, conflito e estados de Turma.
3. Implementar casos de uso e testes de autorização/sessão.
4. Criar migrações, persistência MySQL, auditoria, concorrência e atualização limpa.
5. Criar telas guiadas e testes fora da EDT.
6. Criar demonstração sintética, roteiro de homologação e regressão completa.

Não iniciar ACA-04 até as entidades e contratos do ACA-03 estarem persistidos e verificados.
