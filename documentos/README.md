# Documentação do SCOA

Este índice explica o papel de cada documento. Os registros históricos e evidências mostram o estado da época; não substituem requisitos, backlog ou decisões vigentes.

## Por onde começar

1. Leia as [regras comuns dos agentes](../AGENTS.md) e o [guia dos integrantes](Guias/GUIA_DOS_INTEGRANTES.md).
2. Consulte o [resumo dos pacotes](entrega_modular/RESUMO.md) e o pacote do seu módulo.
3. Para testar os fluxos atuais, comece por [Pessoa, vínculos e conta](academico/ENTREGA_PESSOA_VINCULOS_CONTA.md). Consulte também o [guia de Professores e demonstração integrada](academico/ENTREGA_PROFESSORES.md) e a [ficha de Pessoa/Aluno](academico/ENTREGA_FICHA_PESSOA_ALUNO.md).
4. Para a versão persistente com login, leia [Identidade Acadêmica](academico/IDENTIDADE_ACADEMICA.md). A proposta antiga com a Fundação é histórica para este escopo; Pessoa e acesso agora são implementados pelo Acadêmico.

## Estrutura

| Pasta | Finalidade |
|---|---|
| requisitos/ | Especificação original do produto |
| entrega_modular/ | Backlog vigente, responsáveis e governança |
| planejamento/ | Planejamento gerencial e testes de entrega |
| Guias/ | Orientações práticas para integrantes, execução e banco |
| academico/ | Entregas implementadas, roteiros de validação e limites |
| contratos/ | Propostas de integração e dependências externas |
| decisoes/ | Decisões técnicas, com seu status de aprovação |
| evidencias/ | Resultados de testes por incremento e etapa |
| historico/ | Diagnósticos e versões anteriores para consulta |

## Requisitos, backlog e planejamento

| Documento | Para que serve |
|---|---|
| [Trabalho AMS 2026.pdf](requisitos/Trabalho%20AMS%202026.pdf) | Requisitos, modelos e regras do produto; fonte original para conferir o comportamento esperado |
| [GOVERNANCA.md](entrega_modular/GOVERNANCA.md) | Propriedade dos módulos, organização e integração entre integrantes |
| [RESUMO.md](entrega_modular/RESUMO.md) | Distribuição dos pacotes e estimativas de referência |
| [01_ACADEMICO.md](entrega_modular/01_ACADEMICO.md) | Incrementos e critérios de aceite do Acadêmico |
| [02_FINANCEIRO.md](entrega_modular/02_FINANCEIRO.md) | Incrementos e critérios de aceite do Financeiro |
| [03_BIBLIOTECA.md](entrega_modular/03_BIBLIOTECA.md) | Incrementos e critérios de aceite da Biblioteca |
| [04_ALMOXARIFADO_ADMINISTRATIVO.md](entrega_modular/04_ALMOXARIFADO_ADMINISTRATIVO.md) | Fundação, Almoxarifado e Administrativo |
| [PLANO_TESTES_ENTREGA.md](planejamento/PLANO_TESTES_ENTREGA.md) | Cartões QA/REL, fluxos integrados, homologação e liberação |
| [PLANEJAMENTO_GERENCIAL_GOOGLE_DOCS.md](planejamento/PLANEJAMENTO_GERENCIAL_GOOGLE_DOCS.md) | Conteúdo preparado para apresentação gerencial no Google Docs |

## Guias e entregas

| Documento | Para que serve |
|---|---|
| [GUIA_DOS_INTEGRANTES.md](Guias/GUIA_DOS_INTEGRANTES.md) | Como trabalhar com os pacotes e as regras dos agentes |
| [GUIA_TELAS_ALUNOS.md](Guias/GUIA_TELAS_ALUNOS.md) | Navegação e dados da demonstração de Alunos; contém etapas registradas e avisos de atualização |
| [MYSQL_DOCKER.md](Guias/MYSQL_DOCKER.md) | Configuração inicial e operação do MySQL Docker; conferir os avisos e o estado registrado na data |
| [EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md](Guias/EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md) | Preparação dos testes MySQL e evidências da persistência de Alunos |
| [ENTREGA_FICHA_PESSOA_ALUNO.md](academico/ENTREGA_FICHA_PESSOA_ALUNO.md) | Ficha completa, edição pessoal, testes e integração institucional pendente |
| [FECHAMENTO_ACA01.md](academico/FECHAMENTO_ACA01.md) | Checklist de conclusão, revisão do diagrama/UC01/UC07, entregas de B/D e homologação integrada pendente |
| [ENTREGA_UX_CADASTROS.md](academico/ENTREGA_UX_CADASTROS.md) | Roteiro atual: escolha de cadastro, CPF, dados compartilhados, títulos e resultados dos testes |
| [ENTREGA_PROFESSORES.md](academico/ENTREGA_PROFESSORES.md) | Cadastro, consulta, edição, migração e testes de Professores; como abrir as abas da demonstração |
| [Seed MySQL](../src/test/resources/db/academico/README.md) | Instruções dos exemplos SQL; a carga pertence aos recursos de teste, fora desta pasta |

O guia pessoal `Guias/GUIA_LOCAL_ACESSO_MYSQL.md`, quando presente nesta máquina, contém instruções locais e credenciais. Está excluído pelo Git local e não deve ser adicionado com `git add -f`.

## Contratos e decisões

| Documento | Para que serve |
|---|---|
| [CONTRATO_ACADEMICO_FUNDACAO_PROPOSTA.md](contratos/CONTRATO_ACADEMICO_FUNDACAO_PROPOSTA.md) | Portas consumidoras propostas pelo Acadêmico; validação B/D e implementação institucional ainda pendentes |
| [MENSAGEM_FUNDACAO_PESSOA_ALUNO.md](contratos/MENSAGEM_FUNDACAO_PESSOA_ALUNO.md) | Mensagem preparada para encaminhar a B/D; não é evidência de aprovação |
| [ADR-001-BASE-JAVA-ACADEMICO.md](decisoes/ADR-001-BASE-JAVA-ACADEMICO.md) | Proposta inicial Java/Maven e limites da base; ler seu status e contexto temporal |

## Evidências e histórico

Em `evidencias/`, cada pasta identifica uma etapa do ACA-01: base, persistência, CPF, telas, ficha ou Professores. Arquivos RED registram falhas esperadas do TDD; GREEN/final/integração registram execuções posteriores. Uma falha antiga preservada não significa falha atual. Confira o relatório da entrega e a data do log.

| Documento histórico | O que registra |
|---|---|
| [AVALIACAO_PROJETO.md](historico/AVALIACAO_PROJETO.md) | Diagnóstico da pasta em 09/11, anterior às implementações |
| [BACKLOG_ENTREGA_SUBSTITUIDO.md](historico/BACKLOG_ENTREGA_SUBSTITUIDO.md) | Backlog anterior, substituído pelos pacotes de entrega_modular |
| [TDD_ALUNOS.md](historico/academico/TDD_ALUNOS.md) | Primeiro caso de uso e configuração inicial de testes |
| [PLANO_INTERFACE_ALUNOS.md](historico/academico/PLANO_INTERFACE_ALUNOS.md) | Plano da primeira interface, anterior à navegação guiada |
| [GUIA_INTERFACE_ALUNOS.md](historico/academico/GUIA_INTERFACE_ALUNOS.md) | Primeira tela com operações reunidas e identificação por IDs |
| [GUIA_CADASTRO_CPF.md](historico/academico/GUIA_CADASTRO_CPF.md) | Introdução do CPF antes da separação em telas |

Ao criar documentos, coloque-os na pasta correspondente, atualize este índice e mantenha links relativos. Registre data e status em propostas/relatórios. Preserve logs e versões antigas no histórico sem tratá-los como orientação vigente.


Revisão para versionamento: [pacote ACA-01 — 15/11/2026](academico/REVISAO_PACOTE_ACA01.md).
