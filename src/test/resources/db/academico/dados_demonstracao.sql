-- ACA-01: dados sintéticos para scoa_test, nunca para o banco de desenvolvimento.
-- Pré-condição: fixture pessoa(id) e migração acadêmica já aplicadas pelos testes.
-- Executar pelo cliente mysql em lote SEM --force: qualquer erro interrompe o lote.
-- Não cria/edita tabelas institucionais. IDs 900001..900003 reservados à demonstração.
USE scoa_test;
SET time_zone = '+00:00';
START TRANSACTION;

INSERT INTO pessoa (id)
SELECT 900001 WHERE NOT EXISTS (SELECT 1 FROM pessoa WHERE id = 900001);
INSERT INTO pessoa (id)
SELECT 900002 WHERE NOT EXISTS (SELECT 1 FROM pessoa WHERE id = 900002);
INSERT INTO pessoa (id)
SELECT 900003 WHERE NOT EXISTS (SELECT 1 FROM pessoa WHERE id = 900003);

-- Captura somente vínculos ausentes. A reaplicação preserva situação e histórico.
CREATE TEMPORARY TABLE seed_academico_novo (
    pessoa_id INT PRIMARY KEY,
    situacao_cadastral VARCHAR(40) NOT NULL
);
INSERT INTO seed_academico_novo (pessoa_id, situacao_cadastral)
SELECT 900001, 'ATIVO' WHERE NOT EXISTS (SELECT 1 FROM aluno WHERE pessoa_id = 900001)
UNION ALL
SELECT 900002, 'INATIVO' WHERE NOT EXISTS (SELECT 1 FROM aluno WHERE pessoa_id = 900002);

INSERT INTO aluno (pessoa_id, situacao_cadastral)
SELECT pessoa_id, situacao_cadastral FROM seed_academico_novo;

-- Autor 7 é sintético; o seed não comprova autenticação institucional.
INSERT INTO aluno_auditoria (aluno_id, usuario_id, operacao, situacao_anterior, situacao_nova, registrado_em)
SELECT a.id, 7, 'CADASTRO', NULL, n.situacao_cadastral, '2026-11-10 12:00:00.000000'
FROM seed_academico_novo n JOIN aluno a ON a.pessoa_id = n.pessoa_id;

DROP TEMPORARY TABLE seed_academico_novo;
COMMIT;

SELECT p.id AS pessoa_id, a.id AS aluno_id, a.situacao_cadastral
FROM pessoa p LEFT JOIN aluno a ON a.pessoa_id = p.id
WHERE p.id IN (900001, 900002, 900003)
ORDER BY p.id;
