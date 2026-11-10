# ADR-001 — base de desenvolvimento do Acadêmico

Data: 10/11/2026. Status: proposta implementada para validação da equipe.

O usuário autorizou nesta conversa preparar a proposta e configurar a base necessária ao Acadêmico. Isso autoriza pom.xml e configuração de teste inicial; não transfere a propriedade da fundação nem autoriza implementar os módulos dos colegas.

## Decisão inicial

- Java 21, compilação com release 21, sem recursos preview.
- Maven 3.9.11 para build; JUnit Jupiter 5.14.1 para testes.
- Compiler Plugin 3.14.1; Surefire 3.5.4; versões fixadas no pom.xml.
- Um artefato Maven SCOA, com organização por módulo e camadas; não criar aplicação independente por integrante.
- UTF-8. Sem framework de aplicação, ORM, biblioteca desktop ou driver de banco neste primeiro ciclo.

Fontes técnicas: https://adoptium.net/temurin/releases/?version=21 ; https://maven.apache.org/docs/history ; https://docs.junit.org/5.14.1/user-guide/junit-user-guide-5.14.1.pdf . As versões são referências fixadas para este ciclo, sem alegação de serem as mais recentes.

## Caminhos propostos

Acadêmico: src/main/java/br/edu/scoa/academico/ e src/test/java/br/edu/scoa/academico/. Dentro do código, aplicacao e dominio já existem; apresentacao e infraestrutura serão adicionadas quando implementadas. D deve validar o mapa geral antes da inclusão dos demais módulos. Configuração raiz permanece compartilhada.

## Limite de integração

IdentidadeInstitucional é uma porta consumidora local do Acadêmico, proposta para atender autorização e consulta de pessoa. Não é implementação da fundação nem contrato compartilhado aprovado. B/D devem reconciliá-la com a interface institucional antes da integração real. usuarioId deverá vir do contexto autenticado, nunca de um campo livre da tela.

RepositorioAlunos define escrita atômica com unicidade de pessoaId. Adaptador MySQL futuro deve aplicar UNIQUE(pessoa_id), FK institucional e auditoria transacional, mapeando conflito para ALUNO_JA_CADASTRADO. Os testes unitários atuais não comprovam essas garantias no banco.

## Decisões pendentes

Validar Java/build com D e equipe; biblioteca desktop e estratégia de conexão ao MySQL; contrato real de identidade/autorização; política de auditoria; migrações e modelo de implantação. Dossiê aponta Java desktop; PDF anterior menciona navegador. Resolver essa divergência antes da interface. Nenhum desses pontos foi decidido por conveniência no teste.
