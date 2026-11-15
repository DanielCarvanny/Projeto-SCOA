# ACA-01 — persistência, consulta e atualização com TDD

Data: 10/11/2026. Responsável acadêmico: Dan. Alterações limitadas ao Acadêmico e à configuração de teste/build prevista no plano autorizado. Java permanece a linguagem da aplicação; SQL e YAML descrevem banco e infraestrutura. Não foram utilizados scripts Python.

## Resultado

- Repositório JDBC para cadastrar, consultar por ID e atualizar situação cadastral.
- Casos de uso de consulta/atualização com permissão por aluno e validação de entrada.
- MySQL exclusivo de testes na porta 3308, banco scoa_test, com armazenamento temporário.
- Migração acadêmica versionada com Flyway, FK e unicidade de pessoa, situação válida e auditoria local transacional.
- Dados pessoais continuam sob a fundação; a atualização implementada altera somente situação cadastral. Sem exclusão de aluno e sem matrícula automática.

A interface permanece pendente por escolha do usuário. Pessoa, autenticação e autorização reais ainda não existem. Não foi executada a migração no banco de desenvolvimento scoa e não foi criada tabela Pessoa de produção.

## Dependências e decisões

Connector/J 9.5.0; Flyway Core/MySQL 11.14.1; Failsafe 3.5.4. Proposta técnica para revisão da equipe, adicionada para executar o plano autorizado. JDBC usa sslMode=REQUIRED (conexão criptografada); para implantação além do Docker local, definir truststore e validação de identidade do servidor. Não desabilitamos SSL nem habilitamos recuperação indiscriminada de chave pública.

O Flyway emite aviso de que verificou MySQL até 8.1; os testes neste MySQL 8.4 passaram. Compatibilidade futura/upgrade requer validação da equipe, não deve ser presumida a partir deste resultado.

O histórico de migrações do Acadêmico usa flyway_academico_schema_history e localização db/academico. clean está desabilitado. Não há baseline automático em produção: adoção de esquema existente e integração com histórico global precisam de decisão por D. Nos testes o baseline 0 é explícito sobre a fixture institucional. Migração aplicada deve permanecer imutável; mudanças usam nova versão.

## Como executar em outra máquina

Pré-requisitos: JDK 21, Maven, Docker Desktop com contêineres Linux. Na raiz, copie teste.env.example para .env.test caso o arquivo ainda não exista e preencha duas senhas próprias. Não substitua credenciais existentes. A preparação desta sessão já gerou o arquivo local, ignorado pelo Git.

```powershell
docker compose -f compose.test.yaml --env-file .env.test up -d --wait --wait-timeout 180
mvn test

# Carregar somente a senha de teste, sem imprimi-la:
$linha = Get-Content .env.test | Where-Object { $_.StartsWith('SCOA_TEST_PASSWORD=') }
$env:SCOA_TEST_PASSWORD = $linha.Substring('SCOA_TEST_PASSWORD='.Length)
$env:SCOA_TEST_URL = 'jdbc:mysql://127.0.0.1:3308/scoa_test'
$env:SCOA_TEST_USER = 'scoa_test'
$env:SCOA_TEST_CONFIRM = 'CONFIRMO_BANCO_DESCARTAVEL'
mvn -Pintegracao verify
```

Os testes de integração falham se faltarem essas variáveis ou se URL/usuário não corresponderem exatamente ao ambiente descartável. Também confirmam o catálogo antes de preparar dados. Não apontar para 3307/scoa. Credenciais inválidas ou banco ausente causam falha, sem ignorar testes silenciosamente.

`mvn test` executa apenas unitários. `mvn -Pintegracao verify` executa unitários e testes MySQL. Relatórios em target/surefire-reports e target/failsafe-reports. O `.env.test` é lido pelo Compose, não automaticamente pelo Java; por isso as variáveis explícitas acima.

Para encerrar somente testes: `docker compose -f compose.test.yaml --env-file .env.test down`. O armazenamento desse serviço é descartável. O compose.yaml e o volume persistente scoa_mysql_dados de desenvolvimento são independentes.

## Evidências e cenários

RED de persistência: métodos ainda sem implementação; 12 testes de integração, 3 falhas e 7 erros após corrigir a preparação do teste de falha de auditoria. Dois testes de configuração/constraint já passavam. GREEN: os mesmos 12 testes passaram.

RED de gestão: 27 unitários, 10 falhas e 2 erros nos novos casos de uso; os 15 testes anteriores/configuração passaram. GREEN: 27 unitários e 12 integrações passaram. Um cenário adicional foi incluído para validar os três casos de uso combinados com persistência real e autorização simulada. Resultado final registrado nas evidências deste incremento.

Cobertura comportamental: cadastro e recuperação por nova conexão; autor/auditoria; duplicidade preservando o original; pessoa inexistente; consulta ausente; edição sem mudança de pessoa; edição ausente; rollback de cadastro e atualização quando a auditoria falha; duas conexões disputando a mesma pessoa; constraint de situação; reexecução de migração; credencial inválida; permissões e entradas inválidas.

Os testes usam trigger apenas na instância descartável para provocar erro controlado na auditoria. Binary log está desabilitado somente nesse serviço, permitindo essa fixture sem conceder SUPER ao usuário de testes. Nenhuma alteração equivalente foi feita no desenvolvimento.

## O que ainda impede aceite completo

Validar contrato proposto da fundação, fornecer identidade/autenticação reais, reconciliar migrações e auditoria, definir dados pessoais e política de edição, implementar interface e verificar fluxo institucional real. Testes com fixture não comprovam integração com módulo inexistente. Professores também permanece fora desta fatia de ACA-01.

## Resultado final verificado

27 testes unitários + 13 testes MySQL aprovados; zero falhas/erros/ignorados na execução final. A execução com URL de desenvolvimento foi deliberadamente recusada antes da preparação do banco, confirmando a proteção. Resumos RED/GREEN e dessa recusa estão em evidencias/ACA-01-PERSISTENCIA/. Sem commit ou push.
