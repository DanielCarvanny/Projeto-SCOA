# Evidências de Testes — Incremento ACA-03

Data: 22/11/2026. Responsável: A/Dan — Módulo Acadêmico.

## 1. Resumo dos Resultados

* **Testes unitários e de interface Swing:**
  - Total executado: 245 testes
  - Falhas: 0
  - Erros: 0
  - Ignorados: 0
  - Classes cobertas: `SalaTest`, `TurmaTest`, `GestaoTurmasTest`, `FluxoTurmasTest` e regressão completa dos incrementos anteriores (`Alunos`, `Professores`, `Identidade`, `Curriculo`, `Matrizes`, `PreRequisitos`, `Periodos`).

* **Testes de integração com MySQL 8.4 em Docker (`*IT`):**
  - Total executado: 94 testes
  - Falhas: 0
  - Erros: 0
  - Ignorados: 0
  - Classes cobertas: `RepositorioTurmasMysqlIT`, `CatalogoMysqlIT`, `IdentidadeMysqlIT`, `InstalacaoIdentidadeMysqlIT`, `MatrizesMysqlIT`, `PeriodosMysqlIT`, `PreRequisitosMysqlIT`, `PublicacaoMysqlIT`, `RepositorioAlunosMysqlIT`, `RepositorioProfessoresMysqlIT`.

## 2. Capturas de Tela

* `aca03-ficha-sala.png`: Consulta e exibição da ficha de manutenção de salas.
* `aca03-ficha-turma.png`: Consulta de turmas com horários alocados, situação e controle de transições.

## 3. Invariantes Comprovados

- Conflito de horários de docentes no mesmo período (`CONFLITO_PROFESSOR`) recusado no MySQL e na aplicação.
- Conflito de horários de salas no mesmo período (`CONFLITO_SALA`) recusado no MySQL e na aplicação.
- Recusa de sala com capacidade insuficiente (`CAPACIDADE_INSUFICIENTE`).
- Recusa de sala indisponível (`SALA_INDISPONIVEL`).
- Recusa de operações em período encerrado (`PERIODO_NAO_EDITAVEL`).
- Controle de concorrência por `versao_registro` (`CONFLITO_EDICAO`).
- Auditoria transacional com autor, instante UTC e idempotência por UUID em `sala_auditoria` e `turma_auditoria`.
