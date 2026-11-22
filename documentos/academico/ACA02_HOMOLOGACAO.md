# ACA-02 — Curso e Disciplina: explicação e homologação

Responsável: A/Dan — Acadêmico. Entrega parcial do ACA-02, para revisão humana.

## O que mudou

O menu do Coordenador ganhou **Cursos** e **Disciplinas**. Cada opção abre seu próprio menu, com cadastro e consulta. Para editar ou inativar, consulte primeiro e abra a ficha do registro.

**Curso** representa a formação, por exemplo Análise e Desenvolvimento de Sistemas. Possui código, nome e carga horária total.

**Disciplina** representa um conteúdo do catálogo, por exemplo Programação Orientada a Objetos. Possui código, nome, ementa e carga horária. A ementa descreve o conteúdo estudado. Nesta entrega, cadastrar uma disciplina ainda não a associa a um curso: essa associação será feita pela matriz curricular.

O fluxo de cadastro é: **preencher → revisar → confirmar → ver a ficha**. Revisar não grava. Cancelar não grava. Durante operações no banco, os controles do catálogo ficam desabilitados para impedir confirmações repetidas.

Na consulta, informe código ou parte do nome, ou deixe vazio para listar todos. Selecione um resultado e clique em **Abrir cadastro selecionado**. Registros inativos continuam visíveis e identificados.

Inativar preserva os dados. Não há exclusão física nem botão de reativação nesta entrega. Editar um cadastro inativo não o reativa.

## Histórico e segurança

Cada cadastro, edição e inativação salva uma revisão completa, com autor, horário e identificador de operação. A ficha possui **Consultar histórico**, que mostra os dados de cada revisão. Datas são armazenadas em UTC e exibidas no horário de São Paulo.

Exemplo: uma disciplina nasceu com a ementa “Classes e objetos”; depois recebeu “Classes, objetos e interfaces”. O catálogo passa a mostrar o conteúdo novo, mas o histórico mantém o texto inicial. A revisão exibida é do registro, não uma versão de matriz curricular.

Somente Coordenador administra e consulta este catálogo. O serviço também verifica a permissão: a proteção não depende apenas de ocultar botões. Aluno e Professor mantêm os acessos anteriores.

Se duas telas editarem a mesma revisão, a primeira gravação vence; a segunda recebe orientação para consultar novamente. A gravação do registro e do histórico ocorre na mesma transação: falha no histórico desfaz a alteração.

## Como abrir com MySQL

Na pasta C:\Users\Dan\SCOA, com Docker Desktop ativo:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

Entre com sua conta de Coordenador já existente. Não execute Inicializar novamente se já possui uma conta.

A nova migração é **V003__catalogo_curso_disciplina.sql**. Cria curso, disciplina, curso_historico e disciplina_historico. Não modifica V001/V002 nem carrega demonstrações na base padrão. Não remova volumes Docker para testar esta atualização.

## Demonstração pré-carregada em memória

A classe de teste **br.edu.scoa.academico.curriculo.DemonstracaoCatalogo** abre Cursos e Disciplinas sem MySQL, usando dados sintéticos e acesso simulado de Coordenador. Execute sua função main na IDE, com o classpath de testes.

Também é possível executar no PowerShell, com Java 21 no PATH, após compilar:

```powershell
.\executar-academico.ps1 -Acao Preparar
java -cp "target\test-classes;target\classes" br.edu.scoa.academico.curriculo.DemonstracaoCatalogo
```

Se Java não estiver no PATH, substitua java pelo executável do JDK 21 configurado na sua IDE. Essa demonstração não testa autenticação real nem persistência. Ao fechar, todas as alterações são descartadas.

Dados pré-carregados:

| Tipo | Código | Conteúdo |
|---|---|---|
| Curso | ADS-DEMO | Análise e Desenvolvimento de Sistemas — demonstração, 2.400 horas |
| Disciplina | POO-DEMO | Programação Orientada a Objetos, 80 horas, duas revisões de ementa |

## Roteiro de homologação

Use códigos novos se já tiver executado o roteiro na base MySQL.

| Passo | O que fazer | Resultado esperado |
|---|---|---|
| 1 | Entrar como Coordenador | Menu apresenta Cursos e Disciplinas, além das funções anteriores |
| 2 | Cursos → Cadastrar; código ADS-HOM, nome Sistemas de Informação, carga 2400; Revisar | Resumo mostra os valores; nada foi gravado ainda |
| 3 | Cancelar na revisão; consultar ADS-HOM | Nenhum registro encontrado |
| 4 | Repetir e confirmar | Ficha mostra o curso ativo, revisão 1 |
| 5 | Tentar outro curso com código “ ads-hom ” | Duplicidade recusada; espaços externos e diferenças de maiúsculas não criam outro código |
| 6 | Disciplinas → Cadastrar; POO-HOM, Programação Orientada a Objetos, 80, ementa “Classes e objetos” | Revisão permite conferir e confirmar; ficha mostra ementa |
| 7 | Tentar salvar carga vazia, zero, negativa, decimal ou texto | Mensagem específica de carga; nenhuma gravação |
| 8 | Tentar código/nome vazios ou disciplina sem ementa | Mensagem específica do campo; nenhuma gravação |
| 9 | Consultar por parte do nome; selecionar e abrir ficha | Exibe o cadastro escolhido, sem exigir ID interno |
| 10 | Editar POO-HOM para ementa “Classes, objetos e interfaces”, confirmar | Revisão aumenta; ficha mostra o conteúdo novo |
| 11 | Consultar histórico | Revisões anteriores preservam nome, ementa, carga e situação daquele momento |
| 12 | Inativar POO-HOM; confirmar | Cadastro fica inativo, continua na busca e mantém histórico |
| 13 | Cancelar a confirmação de inativação de outro cadastro | Situação e histórico não mudam |
| 14 | Fechar/reabrir a aplicação MySQL e consultar | Dados e histórico continuam disponíveis |
| 15 | Abrir duas instâncias, consultar o mesmo cadastro e editar nas duas | A primeira salva; a segunda informa conflito e pede nova consulta |
| 16 | Entrar com conta Aluno e depois Professor | Não aparecem os menus administrativos do catálogo |
| 17 | Consultar Pessoas, Alunos e Professores já cadastrados | Funções e dados do ACA-01 continuam acessíveis |

Para limites: código aceita até 40 caracteres; nome até 200; carga é inteiro positivo; ementa é obrigatória e limitada a 65.535 bytes UTF-8. Caracteres acentuados podem ocupar mais de um byte. Códigos são únicos por catálogo: um Curso e uma Disciplina podem ter o mesmo código.

## Evidências técnicas

TDD registrado em logs locais: primeiro falharam as compilações por ausência das classes esperadas; depois as regras, adaptador e telas foram implementados e verificados.

Suíte completa: testes unitários/de interface e integração MySQL, incluindo rollback do histórico, unicidade, consultas parametrizadas, gravações concorrentes, instalação limpa e atualização do ACA-01. Resultado em 15/11/2026: **165 testes unitários/de interface e 45 de integração MySQL aprovados, sem falhas, erros ou testes ignorados**. Log local: tmp/tdd-curriculo-final.log, na pasta de trabalho do agente. Captura de formulário: target/aca02-disciplina.png, gerada por teste e inspecionada visualmente. A homologação interativa humana continua pendente.

Arquitetura preservada: Java 21, Maven, Swing, JDBC, MySQL e Flyway existentes. Sem dependência nova. Alterações restritas ao Acadêmico, sua migração, testes e documentação. Histórico não depende de chaves estrangeiras para contas, preservando o identificador do autor mesmo após mudanças institucionais.

## O que ainda falta no ACA-02

- Matrizes curriculares e componentes, com matriz nunca vazia.
- Publicação e versionamento que preservem a matriz utilizada pelo aluno.
- Referência explícita da matriz à revisão histórica da disciplina.
- Pré-requisitos dentro da mesma matriz, sem autorreferência ou ciclos.
- Períodos letivos e validações de datas/janelas.

As políticas de preservação histórica, matrizes diferentes por ingresso e pré-requisitos internos foram aprovadas por Dan na conversa. Estão registradas em ACA02_ARQUITETURA.md. Nesta primeira entrega, não há tela nem vínculo persistido de matriz, turma ou matrícula. O ACA-02 completo ainda não está pronto para aceite final.

## Registro da sua revisão

Para cada problema, informe: passo do roteiro, código sintético usado, resultado esperado, resultado obtido e se ocorreu em memória ou MySQL. A aprovação manual deve distinguir o catálogo entregue do restante do ACA-02.

