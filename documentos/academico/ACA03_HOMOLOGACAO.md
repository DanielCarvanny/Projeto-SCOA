# ACA-03 — Turmas, Salas e Alocação de Horários — Guia de Homologação

Data: 22/11/2026. Responsável: A/Dan — Acadêmico.
Status: concluído tecnicamente. Validações automatizadas (unitárias, interface desacoplada da EDT e integração MySQL 8.4) aprovadas.

---

## 1. O que foi implementado

O incremento **ACA-03** cobre os requisitos **RF-03**, **RF-05** e **RF-20** do monólito modular SCOA:

1. **Gestão de Salas:**
   - Cadastro, consulta por identificação/localização, ficha detalhada e edição com controle de concorrência por versão.
   - Campos: Identificação única, localização física, capacidade máxima positiva e recursos informativos opcionais.
   - Situação explícita: `DISPONIVEL` ou `INDISPONIVEL`.
   - Auditoria completa em `sala_auditoria` com evento (`CADASTRO`/`EDICAO`), autor, instante UTC e UUID de operação.

2. **Gestão de Turmas e Horários:**
   - Cadastro de turmas associadas a um Período Letivo (não encerrado), Disciplina ativa e Professor ativo.
   - Código único dentro do mesmo período letivo (permite repetição em períodos distintos).
   - Horários estruturados: Dia da semana (`SEGUNDA` a `DOMINGO`), hora de início e término (`HH:mm`), com validação de consistência temporal.
   - Proibição de horários sobrepostos dentro da mesma turma.

3. **Alocação de Salas e Proteção contra Conflitos:**
   - Alocação e desalocação de sala por horário específico da turma.
   - Validações transacionais no banco e na aplicação:
     - Capacidade da sala deve comportar a capacidade da turma (`CAPACIDADE_INSUFICIENTE`).
     - Sala deve estar disponível (`SALA_INDISPONIVEL`).
     - Não permite sobreposição de horários da mesma sala no mesmo período (`CONFLITO_SALA`).
     - Não permite sobreposição de horários do mesmo professor em turmas distintas do mesmo período (`CONFLITO_PROFESSOR`).

4. **Ciclo de Vida e Transições de Situação:**
   - Situação inicial: `PLANEJADA`.
   - Transições válidas:
     - `PLANEJADA` $\rightarrow$ `ABERTA` ou `CANCELADA`
     - `ABERTA` $\rightarrow$ `EM_ANDAMENTO` ou `CANCELADA`
     - `EM_ANDAMENTO` $\rightarrow$ `ENCERRADA`
   - Transições inválidas bloqueadas com código de erro `TURMA_INVALIDA`.
   - Auditoria de transições em `turma_auditoria`.

5. **Segurança e Autorização:**
   - Operações exclusivas do perfil `COORDENADOR`. Usuários nos perfis `ALUNO` e `PROFESSOR` são bloqueados na aplicação com `NAO_AUTORIZADO`.

---

## 2. Como Executar

### Opção A: Demonstração em Memória (Sem dependência de Docker/MySQL)

Com Java 21 configurado, execute na raiz do projeto:

```powershell
.\executar-academico.ps1 -Acao Preparar
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $java -cp "target\test-classes;target\classes" br.edu.scoa.academico.apresentacao.turmas.DemonstracaoTurmas
```

A demonstração carrega dados sintéticos:
- Período letivo aberto: `2027.1 — Graduação`.
- Disciplinas ativas: `Cálculo Diferencial e Integral I` (MAT101) e `Algoritmos e Estruturas de Dados` (CC102).
- Salas: `SALA-101` (45 lugares), `LAB-01` (35 lugares), `AUDITORIO` (120 lugares) e `LAB-MANUT` (Indisponível).
- Turmas: `T01-CALC` (Planejada), `T02-ALGO` (Aberta) e `T03-CALC-NOTURNO` (Sem sala alocada).

### Opção B: Aplicação Persistente com MySQL

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

1. Faça login como Coordenador.
2. No menu principal acadêmico, clique em **Turmas e salas**.

---

## 3. Roteiro de Testes e Cenários de Homologação

| # | Cenário | Ação | Resultado Esperado |
|---|---|---|---|
| 1 | Cadastro de sala válido | Cadastrar sala `LAB-10`, `Bloco B`, capacidade `35` $\rightarrow$ Revisar $\rightarrow$ Confirmar | Sala gravada como `DISPONIVEL` com ID gerado |
| 2 | Capacidade inválida | Tentar cadastrar sala com capacidade `0` ou negativa | Mensagem de erro e gravação bloqueada |
| 3 | Identificação duplicada | Tentar cadastrar sala com identificação já existente | Bloqueio com erro de unicidade (`SALA_INVALIDA`) |
| 4 | Consulta e edição de sala | Pesquisar termo $\rightarrow$ Abrir ficha $\rightarrow$ Editar localização e salvar | Ficha e banco atualizados com nova versão de registro |
| 5 | Alternar situação de sala | Na ficha da sala, clicar em "Alternar situação" | Situação alterna entre `DISPONIVEL` e `INDISPONIVEL` |
| 6 | Cadastro de turma válido | Informar período, disciplina, professor, código `T01`, capacidade `30`, horário `SEGUNDA 08:00 às 10:00` | Turma salva como `PLANEJADA` com horário registrado |
| 7 | Período encerrado | Tentar criar turma em período encerrado | Bloqueio com erro `PERIODO_NAO_EDITAVEL` |
| 8 | Conflito de professor | Tentar cadastrar turma para o mesmo professor em horário sobreposto no mesmo período | Recusa transacional com erro `CONFLITO_PROFESSOR` |
| 9 | Alocação de sala adequada | Na ficha da turma, informar ID de sala disponível e com capacidade suficiente | Sala vinculada com sucesso ao horário |
| 10 | Conflito de sala | Alocar sala já alocada em outra turma com horário concorrente no mesmo período | Recusa transacional com erro `CONFLITO_SALA` |
| 11 | Sala com capacidade menor | Alocar sala de 20 lugares em turma de 35 lugares | Recusa com erro `CAPACIDADE_INSUFICIENTE` |
| 12 | Sala indisponível | Alocar sala com situação `INDISPONIVEL` | Recusa com erro `SALA_INDISPONIVEL` |
| 13 | Desalocação de sala | Na ficha da turma, clicar em "Desalocar sala do horário" | Vínculo de sala removido (`null`) mantendo a turma |
| 14 | Transições de situação | Na ficha da turma: Abrir $\rightarrow$ Iniciar aulas $\rightarrow$ Encerrar | Situação avança conforme ciclo; auditoria registrada |
| 15 | Cancelamento de turma | Clicar em "Cancelar turma" em turma planejada ou aberta | Situação atualizada para `CANCELADA` |
| 16 | Concorrência otimista | Gravação com versão desatualizada de sala ou turma | Recusa com erro `CONFLITO_EDICAO` |
| 17 | Controle de acesso | Login como Aluno ou Professor | Menu ausente e tentativas na aplicação bloqueadas com `NAO_AUTORIZADO` |

---

## 4. Migrações e Persistência

- `V008__salas_turmas_horarios.sql`: Criação das tabelas relacionais `sala`, `turma`, `horario_turma` e `turma_auditoria`, com integridade referencial, checks e índices de unicidade.
- `V009__auditoria_sala.sql`: Criação de `sala_auditoria` com chave UUID de idempotência e trilha auditável.

---

## 5. Limitações e Próximos Passos (ACA-04)

- O controle de inscrições de alunos em turmas, contagem de vagas preenchidas, concorrência de matrículas e verificação de débitos financeiros pertencem ao incremento **ACA-04**. O ACA-03 estabelece a infraestrutura e oferta da turma sem simular inscrições prematuramente.
