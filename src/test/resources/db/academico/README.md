# Dados sintéticos do Acadêmico

Arquivo: `src/test/resources/db/academico/dados_demonstracao.sql`.

O banco MySQL de testes é real; os registros são fictícios. Este seed não alimenta a demonstração Swing, que continua em memória.

| Pessoa | Vínculo inicial | Finalidade |
|---|---|---|
| 900001 | Aluno ATIVO | Consultar e alterar situação |
| 900002 | Aluno INATIVO | Testar situação distinta |
| 900003 | Sem aluno | Testar criação de vínculo |

IDs de aluno são gerados pelo banco e aparecem na saída do script. O seed registra auditoria sintética de cadastro com autor 7 e instante fixo. CPF, nome, e-mail e telefone não existem na fixture MySQL pessoa(id); não foram criados dados institucionais paralelos.

## Aplicar no PowerShell

Na raiz `C:\Users\Dan\SCOA`, com Docker e banco de testes disponíveis:

```powershell
docker compose --env-file .env.test -f compose.test.yaml up -d --wait
Get-Content -Raw -LiteralPath .\src\test\resources\db\academico\dados_demonstracao.sql | docker compose --env-file .env.test -f compose.test.yaml exec -T mysql-testes sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 -u"$MYSQL_USER" "$MYSQL_DATABASE"'
```

O comando utiliza a credencial do container, sem imprimir a senha. Não adicionar `--force`: em caso de falha o lote deve parar, e a conexão encerrada reverte a transação pendente. Execute uma carga por vez, sem suíte de integração concorrente.

Pré-condição: executar a preparação/suíte descrita em `documentos/EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md` caso o banco não tenha pessoa, aluno e aluno_auditoria. O seed não substitui migrações nem instala a Fundação. Seu `USE scoa_test` fixa o esquema de destino. Não trocar para scoa.

Reaplicar não duplica pessoas, alunos ou auditoria; não restaura uma situação que você tenha alterado. Preserva registros preexistentes e não usa DELETE/TRUNCATE. Os IDs de pessoa acima são reservados a estes exemplos.

## Consultar

```sql
SELECT p.id AS pessoa_id, a.id AS aluno_id, a.situacao_cadastral
FROM pessoa p LEFT JOIN aluno a ON a.pessoa_id = p.id
WHERE p.id IN (900001, 900002, 900003);

SELECT h.* FROM aluno_auditoria h
JOIN aluno a ON a.id = h.aluno_id
WHERE a.pessoa_id IN (900001, 900002);
```

A suíte de integração limpa dados antes de cada teste: reaplique este arquivo depois de rodá-la. O banco de testes usa tmpfs e perde dados quando reiniciado; mantenha este seed versionado para reconstruí-los. Os valores ATIVO/INATIVO da tabela acima descrevem a primeira aplicação.
