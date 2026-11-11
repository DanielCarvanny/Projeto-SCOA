# Projeto SCOA

Sistema de Controle Acadêmico — entrega prevista em 30/11/2026.

Estado em 10/11: cadastro, consulta e atualização de situação de Alunos; interface Swing com telas guiadas e demonstração em memória. Últimas execuções: 78 testes unitários/de apresentação e 13 de integração MySQL aprovados. Integração institucional real e Professores permanecem pendentes.

## Comece aqui

1. [Regras comuns dos agentes](AGENTS.md).
2. [Guia dos integrantes](documentos/GUIA_DOS_INTEGRANTES.md).
3. [Governança e contratos propostos](documentos/entrega_modular/GOVERNANCA.md).
4. [Resumo dos quatro pacotes](documentos/entrega_modular/RESUMO.md).
5. [Plano de testes e entrega](documentos/PLANO_TESTES_ENTREGA.md).
6. [Especificação oficial](documentos/Trabalho%20AMS%202026.pdf).

Pacotes: [Acadêmico](documentos/entrega_modular/01_ACADEMICO.md), [Financeiro](documentos/entrega_modular/02_FINANCEIRO.md), [Biblioteca](documentos/entrega_modular/03_BIBLIOTECA.md), [Almoxarifado e Administrativo](documentos/entrega_modular/04_ALMOXARIFADO_ADMINISTRATIVO.md).

## Organização e fontes de verdade

Git mantém regras, contratos, pacotes e critérios de aceite. Trello mantém andamento, responsáveis e evidências. Google Docs apresenta a visão gerencial. Link do Trello: a informar. Link do Google Docs: pendente de publicação; [conteúdo preparado](documentos/PLANEJAMENTO_GERENCIAL_GOOGLE_DOCS.md).

O [backlog anterior](documentos/historico/BACKLOG_ENTREGA_SUBSTITUIDO.md) é histórico e não deve ser executado como plano vigente.

## Antes de implementar

Preencher responsáveis A/B/C/D; registrar stack/versões, estrutura de diretórios e comandos de execução/teste; materializar contratos compartilhados; resolver parâmetros de negócio pendentes. Monólito modular e MySQL são as diretrizes documentadas. Há uma base Maven proposta e testes do cadastro de aluno. A persistência acadêmica e a interface inicial Swing estão implementadas e testadas; a integração com identidade real permanece pendente.

Consultar [avaliação da pasta](documentos/AVALIACAO_PROJETO.md). Mudanças desta reorganização não foram commitadas automaticamente.

## Primeiro ciclo de implementação

[Proposta técnica Java/Maven](documentos/decisoes/ADR-001-BASE-JAVA-ACADEMICO.md) e [guia de execução/evidências TDD](documentos/TDD_ALUNOS.md). Com JDK 21 e Maven configurados, execute `mvn test` na raiz. A demonstração Swing é executável conforme o guia abaixo.

## Banco local em Docker

MySQL 8.4 com volume persistente e porta local 3307. Execute `docker compose up -d --wait` na raiz, após configurar `.env`. Consulte [configuração e operação do MySQL](documentos/MYSQL_DOCKER.md). O adaptador Java e a migração acadêmica são validados no banco de teste isolado; o banco de desenvolvimento aguarda a fundação real.
## Persistência acadêmica e testes de integração

Consulte [execução TDD com MySQL](documentos/EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md) e o [contrato proposto com a fundação](documentos/CONTRATO_ACADEMICO_FUNDACAO_PROPOSTA.md). O serviço de teste usa compose.test.yaml e a porta 3308. Os testes não usam nem migram o banco de desenvolvimento da porta 3307. A identidade usada na suíte é uma fixture sintética.

## Interface inicial de Alunos

Consulte o [guia vigente das telas e dados pré-carregados](documentos/GUIA_TELAS_ALUNOS.md), o [fluxo por CPF](documentos/GUIA_CADASTRO_CPF.md) e o [seed MySQL de testes](src/test/resources/db/academico/README.md). A demonstração usa dados sintéticos em memória; a integração institucional real permanece pendente.
