# ACA-02 — Períodos letivos e conferência de fechamento

Data: 20/11/2026. Responsável: A/Dan — Acadêmico.

Status: concluído tecnicamente em 22/11/2026. A homologação manual foi dispensada por decisão de Dan. Publicação/cópia de matrizes já foram revisadas e aprovadas por Dan nesta conversa. Esta entrega não altera a estimativa do ACA-02.

## O que foi adicionado

O menu do Coordenador ganhou **Períodos letivos**. Nele é possível cadastrar, consultar por descrição/situação, editar, abrir e encerrar um período. Aluno e Professor não administram esses dados; a permissão também é validada na aplicação.

O cadastro tem quatro telas:

1. **Identificação e calendário:** descrição, início e fim das atividades.
2. **Janela de matrícula:** prazo para criar o vínculo de matrícula.
3. **Janela de inscrição em turmas:** prazo para inscrições nas ofertas.
4. **Revisão:** resumo dos três intervalos antes de confirmar e salvar.

As janelas são independentes e podem começar antes das aulas. Cada fim precisa ser igual ou posterior ao seu início. Descrições iguais são permitidas; a documentação não as define como identificador único.

**Planejado** é a situação inicial. Na ficha, **Abrir período** apresenta uma confirmação própria. Aberto não significa que todas as janelas estão disponíveis agora: o instante consultado também precisa estar dentro da respectiva janela, incluindo os limites inicial/final.

**Encerrar período** também exige confirmação. Após encerrar, a edição e novas operações pelas janelas ficam bloqueadas; não há reabertura nesta versão. Encerrar não homologa pautas nem fecha diários.

Essas decisões foram autorizadas na execução do plano em 20/11/2026. Cancelar não grava. A edição parte da ficha. Confirmações repetidas do cadastro usam a mesma chave para evitar duplicação; uma edição concorrente desatualizada é recusada.

## Formatos de entrada

- Atividades: **DD/MM/AAAA**, por exemplo `01/02/2027`.
- Janelas: **DD/MM/AAAA HH:mm**, por exemplo `01/01/2027 09:00`.
- Segundos opcionais: `01/01/2027 09:00:30`; frações de segundo até seis casas também são preservadas quando existentes.
- Horário da interface: **America/Sao_Paulo**; armazenamento dos instantes: UTC.
- Datas impossíveis e horários inexistentes/ambíguos no fuso são recusados, sem ajuste silencioso.

O calendário visual continua fora desta entrega, conforme o refinamento futuro já planejado.

## Executar com MySQL

No PowerShell, na pasta `C:\Users\Dan\SCOA`:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

Entre como Coordenador e escolha **Períodos letivos**. A migração V007 cria `periodo_letivo` e `periodo_historico`. Não apaga os dados anteriores. A validação automatizada foi feita no banco descartável `scoa_test`, porta 3308; o banco padrão não foi migrado pelo agente nesta execução.

## Demonstração pré-carregada em memória

```powershell
.\executar-academico.ps1 -Acao Preparar
$javaPeriodos = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $javaPeriodos -cp "target\test-classes;target\classes" br.edu.scoa.academico.curriculo.DemonstracaoPeriodos
```

A demonstração disponibiliza **DEMO — Planejado**, **DEMO — Aberto** e **DEMO — Encerrado**. Consulte sem preencher filtros para listar os três. Todos possuem calendário de fevereiro a junho de 2027 e janelas em janeiro/fevereiro de 2027. Aberto é a situação; essas datas continuam determinando quando as operações seriam permitidas.

Identidade e persistência são simuladas somente nessa demonstração. Os dados desaparecem ao fechar e não são inseridos no MySQL. O menu principal usa a sessão e o banco reais.

## Roteiro para homologar

| Cenário | Ação | Resultado esperado |
|---|---|---|
| Cadastro válido | Descrição 2027.1; atividades 01/02/2027 a 30/06/2027; matrícula 01/01/2027 09:00 a 31/01/2027 18:00; inscrição 15/01/2027 09:00 a 10/02/2027 18:00 | Revisão mostra os três intervalos; confirmação salva Planejado |
| Cancelamento | Preencher e cancelar na revisão | Nenhum período novo |
| Data impossível | Informar 31/02/2027 | Mensagem sobre calendário, sem gravação |
| Intervalo invertido | Informar fim anterior ao início em cada janela | Mensagem identifica matrícula ou inscrição |
| Consulta | Buscar parte da descrição e filtrar situação | Apenas resultados correspondentes |
| Edição | Abrir ficha, editar descrição/datas, revisar e confirmar | Ficha e consulta apresentam a alteração |
| Cancelar abertura | Abrir período e cancelar a confirmação | Continua Planejado |
| Abertura | Confirmar abertura de um planejado | Fica Aberto; prazo continua condicionado às janelas |
| Encerramento | Confirmar encerramento de um aberto | Fica Encerrado; edição e transições ficam desabilitadas |
| Persistência | Fechar e abrir a aplicação padrão | Dados e situação continuam salvos |
| Concorrência | Carregar o mesmo período em duas aplicações; salvar na primeira e depois na segunda | Segunda escrita é recusada; atualizar ficha |
| Clique duplo | Clicar repetidamente em confirmar | Apenas uma escrita |
| Permissão | Entrar como Aluno ou Professor | Menu de administração de períodos ausente |

As condições exatas nos instantes inicial/final, expiração de sessão, UTC e reversão após falha na auditoria são verificadas automaticamente; não exigem esperar o relógio real na homologação.

## Conferência do ACA-02

| Parte | Situação técnica |
|---|---|
| Curso e Disciplina | Implementados, com validações, histórico e persistência |
| Matriz e componentes | Implementados; matriz nunca vazia e conteúdo histórico preservado |
| Pré-requisitos | Implementados; mesma matriz, sem autorreferência ou ciclos |
| Publicação e cópia de versões | Implementadas e homologadas por Dan |
| Período com datas válidas | Implementado e validado automaticamente |
| Refinamentos visuais | Permanecem no backlog de UX |
| Fechamento técnico | Concluído; commit e PR preparados na etapa de versionamento |

## Limitações e próxima integração

Turmas, inscrições e rematrículas ainda não existem neste checkout. A exceção do RF-30 sobre alterações que invalidem registros vinculados precisa ser integrada transacionalmente quando ACA-03/04 forem implementados. Não se trata de uma validação já concluída. O encerramento não verifica pautas nem diários futuros. O plano individual do RF-22 pertence a incremento posterior; o ACA-02 não entrega sozinho todo esse requisito.

Nenhum contrato de outros módulos, dependência ou configuração de CI foi alterado. A nova interface de repositório é interna ao Acadêmico. Migrações já aplicadas foram preservadas.

## Evidências

Logs RED/GREEN, validação final e captura da ficha em `documentos/evidencias/aca02-periodos/`. Testes de domínio usam instantes explícitos; a aplicação usa relógio controlado nos testes. Integração usa MySQL real e recusa execução sem confirmação de banco descartável.

Resultado final de 20/11/2026: **213 testes unitários/interface e 87 testes de integração MySQL aprovados**, sem falhas, erros ou testes ignorados. Captura da ficha renderizada e inspecionada. Incluem atualização V006 → V007, banco limpo, auditoria, idempotência concorrente, autorização, sessão expirada, fluxo completo de telas e horário inválido no fuso institucional. Em 22/11/2026, Dan dispensou a homologação manual como condição de fechamento.
