# ACA-02 — Matrizes curriculares e componentes

Implementação e testes: 15/11/2026. Documentação concluída: 16/11/2026. Responsável: A/Dan — Acadêmico. Entrega para homologação humana; o ACA-02 completo continua em andamento.

## O que foi adicionado

O Coordenador passa a ter a opção **Matrizes curriculares** no menu acadêmico, com **Criar matriz** e **Consultar matriz**.

Uma matriz é a composição curricular de um curso. Seus componentes informam quais disciplinas fazem parte dela, se são obrigatórias ou eletivas, o período ideal e a carga horária considerada. Cadastrar a matriz não cria turma nem matrícula.

O fluxo de criação tem quatro etapas separadas:

1. Selecionar um curso já cadastrado.
2. Informar código, versão curricular e vigência.
3. Adicionar os componentes, um por vez.
4. Revisar o conjunto e confirmar.

**Aplicar à montagem** apenas atualiza a lista na tela. Somente **Confirmar e salvar** grava a matriz, seus componentes e a auditoria na mesma transação. Cancelar descarta o trabalho não confirmado.

Não é permitido salvar matriz vazia, repetir disciplina ou remover o último componente. A mesma disciplina pode participar de matrizes diferentes. Curso, código e versão identificam uma combinação única. O curso não pode ser trocado após a criação.

As datas usam DD/MM/AAAA. O fim é opcional; quando informado, precisa ser igual ou posterior ao início. Código aceita até 40 caracteres e é normalizado para maiúsculas; versão curricular aceita até 255 caracteres e tem espaços externos removidos. Período ideal e carga são inteiros positivos.

Na consulta, escolha um curso ou “Todos os cursos”, informe código/versão ou deixe o filtro vazio. Selecione uma matriz e abra sua ficha. A ficha oferece ações separadas para editar identificação/vigência, adicionar, editar e remover componentes.

## Preservação do conteúdo

O componente referencia a **revisão da disciplina escolhida**. Se a ementa do catálogo mudar depois, a matriz continua mostrando o conteúdo anterior.

Para atualizar, abra a ficha, selecione o componente, clique em **Editar componente selecionado** e depois em **Usar revisão atual da disciplina**. Confira a nova ementa, aplique à montagem e confirme a matriz. Atualizar a revisão não modifica automaticamente a carga considerada na matriz: esse campo continua explícito.

Não confunda:

- **Versão curricular:** identificação de negócio, como 2027.1.
- **Revisão da disciplina:** conteúdo histórico utilizado pelo componente.
- **Revisão técnica da matriz:** controle de edições simultâneas e auditoria.

A soma das cargas obrigatórias é exibida na ficha. Ela não substitui a carga total do curso, nem soma eletivas como se fossem obrigatórias.

Somente matrizes **em elaboração** podem ser editadas. As restrições são verificadas no serviço e no estado real do banco; não dependem apenas dos botões. Nesta entrega não existe ação de publicação.

## Abrir com MySQL

No PowerShell, na pasta C:\Users\Dan\SCOA:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

Entre com a conta de Coordenador existente. Não é necessário inicializar novamente.

A migração **V004__matriz_e_componentes.sql** acrescenta matriz_curricular, componente_curricular, matriz_historico e componente_historico. Preserva as migrações anteriores e os cadastros. Não remove volumes nem carrega exemplos na base padrão.

Os históricos guardam o cabeçalho e a composição após cada gravação, com autor, instante UTC, origem e identificador de operação. A consulta desses snapshots de matriz por tela fica para uma etapa posterior; o conteúdo histórico de disciplina utilizado aparece na ficha atual.

## Demonstração em memória

Execute a classe de teste **br.edu.scoa.academico.curriculo.DemonstracaoMatrizes** pela IDE, com Java 21 e classpath de testes. Ela já carrega:

- Curso ADS-DEMO, Sistemas — demonstração, 2.400 horas.
- Disciplina POO-DEMO, Objetos, ementa “Classes e objetos”, 80 horas.
- Matriz ADS-DEMO, versão 2027.1, início em 01/01/2027, com um componente obrigatório no primeiro período.

Alternativa no PowerShell:

```powershell
.\executar-academico.ps1 -Acao Preparar
$javaMatrizes = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $javaMatrizes -cp "target\test-classes;target\classes" br.edu.scoa.academico.curriculo.DemonstracaoMatrizes
```

Os dados desaparecem ao fechar. A identidade é simulada somente nessa demonstração; para homologar login, permissões e persistência, use a aplicação MySQL.

## Roteiro manual

| Cenário | Procedimento | Resultado esperado |
|---|---|---|
| Criação válida | Selecionar curso; preencher M-HOM, 2027.1, 01/01/2027; adicionar uma disciplina obrigatória, período 1, carga 80; revisar e confirmar | Matriz gravada em elaboração, com um componente |
| Montagem não é gravação | Adicionar um componente e cancelar antes de confirmar | Nenhuma matriz nova na consulta |
| Matriz vazia | Tentar revisar sem componentes | Mensagem específica; nenhuma gravação |
| Duplicidade de disciplina | Adicionar novamente a disciplina já presente | Recusa; permanece um componente para aquela disciplina |
| Último componente | Consultar matriz com um componente e tentar removê-lo | Remoção recusada |
| Remoção permitida | Adicionar uma segunda disciplina, salvar, depois removê-la e confirmar | Primeiro componente preservado; mudança auditada |
| Edição | Abrir ficha, editar vigência ou dados de um componente, revisar e confirmar | Ficha atualizada após confirmação |
| Cancelar edição | Alterar código/carga e cancelar | Dados anteriores recarregados, sem gravação |
| Datas inválidas | Informar 31/02/2027 ou fim anterior ao início | Recusa antes da gravação |
| Valores inválidos | Usar período/carga vazios, zero, negativos ou texto | Mensagem sobre os dados do componente |
| Versão duplicada | Criar outra matriz com mesmo curso, código e versão | Recusa; cadastro original preservado |
| Outra versão | Usar mesmo curso/código e versão diferente, com componentes | Nova matriz em elaboração; não copia automaticamente a anterior |
| Histórico de disciplina | Alterar a ementa no catálogo e consultar a matriz existente | Matriz conserva a revisão anterior |
| Atualização explícita | Editar componente, usar revisão atual, conferir e confirmar | Matriz passa a referenciar a nova revisão; carga considerada permanece explícita |
| Concorrência | Em duas instâncias, consultar a mesma matriz e salvar mudanças nas duas | Primeira salva; segunda recebe conflito e deve consultar novamente |
| Persistência | Fechar/reabrir aplicação MySQL e consultar | Composição, datas e revisão preservadas |
| Permissão | Entrar como Aluno ou Professor | Sem menu administrativo de matrizes |
| Regressão | Consultar Pessoas, Alunos, Professores, Cursos e Disciplinas | Fluxos anteriores preservados |

## Validação técnica

Testes cobrem domínio, autorização, interface, MySQL real, duplicidades, snapshots, rollback, remoções concorrentes, instalação limpa e atualização desde V003 preservando o histórico de disciplina. Resultado: **181 testes unitários/de interface e 53 de integração MySQL aprovados**, sem falhas, erros ou testes ignorados. Execução em 15/11/2026, log local tmp/tdd-matriz-final.log. O código foi reconferido em 16/11/2026; a homologação humana permanece pendente. A captura target/aca02-matriz.png foi inspecionada visualmente.

Cada atualização bloqueia a linha da matriz e confere sua revisão antes de alterar a composição. IDs dos componentes mantidos são preservados. A regra de composição não vazia é aplicada no domínio e no caso de gravação atômica; SQL administrativo direto não deve ser usado para contornar esses casos de uso.

## Fronteira desta entrega

Ainda faltam no ACA-02: configuração de pré-requisitos, publicação/cópia de versões e períodos letivos. Associação a matrículas e turmas será integrada nos incrementos correspondentes. Nenhum desses fluxos é apresentado como concluído.

A entrega não muda pontos ou critérios de aceite, não cria dependências novas e não modifica módulos de outros integrantes. Commit, PR e aceite final continuam dependentes do fluxo de revisão humana.
