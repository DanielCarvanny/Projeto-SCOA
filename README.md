# Projeto SCOA

Sistema de Controle Acadêmico — entrega prevista em 30/11/2026.

Estado revisado em 15/11/2026: Pessoa, Alunos, Professores, contas, login e permissões implementados no Acadêmico, com persistência MySQL e telas Swing. Última suíte executada em 13/11: 153 testes unitários/de apresentação e 37 de integração MySQL aprovados. Dan informou que os testes manuais funcionaram. Acabamento de telas/mensagens e regularização de uma conta antiga identificada em 13/11 permanecem registrados; isso não representa aceite final ou publicação.

Consulte o [índice da documentação](documentos/README.md) para entender a finalidade de cada arquivo.

## Comece aqui

1. [Regras comuns dos agentes](AGENTS.md).
2. [Guia dos integrantes](documentos/Guias/GUIA_DOS_INTEGRANTES.md).
3. [Governança e contratos propostos](documentos/entrega_modular/GOVERNANCA.md).
4. [Resumo dos quatro pacotes](documentos/entrega_modular/RESUMO.md).
5. [Plano de testes e entrega](documentos/planejamento/PLANO_TESTES_ENTREGA.md).
6. [Especificação oficial](documentos/requisitos/Trabalho%20AMS%202026.pdf).

Pacotes: [Acadêmico](documentos/entrega_modular/01_ACADEMICO.md), [Financeiro](documentos/entrega_modular/02_FINANCEIRO.md), [Biblioteca](documentos/entrega_modular/03_BIBLIOTECA.md), [Almoxarifado e Administrativo](documentos/entrega_modular/04_ALMOXARIFADO_ADMINISTRATIVO.md).

## Organização e fontes de verdade

Git mantém regras, contratos, pacotes e critérios de aceite. Trello mantém andamento, responsáveis e evidências. Google Docs apresenta a visão gerencial. Link do Trello: a informar. Link do Google Docs: pendente de publicação; [conteúdo preparado](documentos/planejamento/PLANEJAMENTO_GERENCIAL_GOOGLE_DOCS.md).

O [backlog anterior](documentos/historico/BACKLOG_ENTREGA_SUBSTITUIDO.md) é histórico e não deve ser executado como plano vigente.

## Antes de implementar

Preencher responsáveis A/B/C/D; registrar stack/versões, estrutura de diretórios e comandos de execução/teste; materializar contratos compartilhados; resolver parâmetros de negócio pendentes. Monólito modular e MySQL são as diretrizes documentadas. O Acadêmico usa Java 21, Maven, Swing e MySQL, com identidade real implementada. Os demais módulos e seus contratos compartilhados seguem a governança da equipe.

Consultar [avaliação da pasta](documentos/historico/AVALIACAO_PROJETO.md). Mudanças desta reorganização não foram commitadas automaticamente.

## Primeiro ciclo de implementação

[Proposta técnica Java/Maven](documentos/decisoes/ADR-001-BASE-JAVA-ACADEMICO.md) e [guia de execução/evidências TDD](documentos/historico/academico/TDD_ALUNOS.md). Com JDK 21 e Maven configurados, execute `mvn test` na raiz. A demonstração Swing é executável conforme o guia abaixo.

## Banco local em Docker

MySQL 8.4 com volume persistente e porta local 3307. Execute `docker compose up -d --wait` na raiz, após configurar `.env`. Consulte [configuração e operação do MySQL](documentos/Guias/MYSQL_DOCKER.md). O adaptador Java e a migração acadêmica são validados no banco de teste isolado; o banco de desenvolvimento usa a identidade implementada pelo Acadêmico.
## Persistência acadêmica e testes de integração

Consulte [execução TDD com MySQL](documentos/Guias/EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md) e o [contrato proposto com a fundação](documentos/contratos/CONTRATO_ACADEMICO_FUNDACAO_PROPOSTA.md). O serviço de teste usa compose.test.yaml e a porta 3308. Os testes não usam nem migram o banco de desenvolvimento da porta 3307. A suíte inclui identidade real no MySQL, sempre com dados sintéticos, além de testes isolados com simulações.

## Interface inicial de Alunos

Consulte o [registro histórico das telas e dados pré-carregados](documentos/Guias/GUIA_TELAS_ALUNOS.md), o [fluxo por CPF](documentos/historico/academico/GUIA_CADASTRO_CPF.md) e o [seed MySQL de testes](src/test/resources/db/academico/README.md). A demonstração usa dados sintéticos em memória. Para o fluxo vigente persistente, consulte [Pessoa, vínculos e conta](documentos/academico/ENTREGA_PESSOA_VINCULOS_CONTA.md).

## Professores

[Guia de Professores, critérios e evidências](documentos/academico/ENTREGA_PROFESSORES.md). O inicializador de demonstração existente agora apresenta abas Alunos e Professores; o cadastro mantém o fluxo guiado em três etapas.

## Acadêmico persistente com login

Pessoa, contas e permissões agora usam MySQL. Consulte [instalação, primeiro coordenador e homologação](documentos/academico/IDENTIDADE_ACADEMICA.md). Com Java 21/Maven no ambiente e .env configurado, execute na raiz:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Inicializar
.\executar-academico.ps1 -Acao Abrir
```

Inicializar é executado uma única vez, em terminal interativo, para definir suas credenciais. Não existe senha inicial fixa. DemonstracaoAlunos continua sendo a demonstração separada em memória. Os testes manuais foram reportados como funcionando por Dan; a revisão do pacote está em [REVISÃO DO PACOTE ACA-01](documentos/academico/REVISAO_PACOTE_ACA01.md). Não houve publicação.