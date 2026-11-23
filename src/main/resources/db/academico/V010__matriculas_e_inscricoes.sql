CREATE TABLE matricula_curso (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, numero_matricula VARCHAR(40) NOT NULL, aluno_id INT NOT NULL, curso_id BIGINT NOT NULL, matriz_id BIGINT NOT NULL,
 data_ingresso DATE NOT NULL, situacao VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, versao_registro BIGINT NOT NULL, operacao_criacao CHAR(36) NOT NULL,
 UNIQUE KEY uk_matricula_numero(numero_matricula), UNIQUE KEY uk_matricula_operacao(operacao_criacao), INDEX ix_matricula_aluno(aluno_id),
 FOREIGN KEY(aluno_id) REFERENCES aluno(id) ON DELETE RESTRICT, FOREIGN KEY(curso_id) REFERENCES curso(id) ON DELETE RESTRICT, FOREIGN KEY(matriz_id) REFERENCES matriz_curricular(id) ON DELETE RESTRICT,
 CHECK(situacao IN ('ATIVA','TRANCADA','CONCLUIDA','CANCELADA')), CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE inscricao_turma (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, matricula_id BIGINT NOT NULL, turma_id BIGINT NOT NULL, situacao VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 inscrita_em DATETIME(6) NOT NULL, versao_registro BIGINT NOT NULL, operacao_criacao CHAR(36) NOT NULL,
 UNIQUE KEY uk_inscricao_operacao(operacao_criacao), INDEX ix_inscricao_matricula(matricula_id,situacao), INDEX ix_inscricao_turma(turma_id,situacao),
 FOREIGN KEY(matricula_id) REFERENCES matricula_curso(id) ON DELETE RESTRICT, FOREIGN KEY(turma_id) REFERENCES turma(id) ON DELETE RESTRICT,
 CHECK(situacao IN ('ATIVA','CANCELADA')), CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE matricula_auditoria (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,matricula_id BIGINT NOT NULL,evento VARCHAR(20) NOT NULL,autor_id BIGINT NOT NULL,instante DATETIME(6) NOT NULL,operacao_id CHAR(36) NOT NULL UNIQUE,FOREIGN KEY(matricula_id) REFERENCES matricula_curso(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE inscricao_auditoria (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,inscricao_id BIGINT NOT NULL,evento VARCHAR(20) NOT NULL,autor_id BIGINT NOT NULL,instante DATETIME(6) NOT NULL,operacao_id CHAR(36) NOT NULL UNIQUE,FOREIGN KEY(inscricao_id) REFERENCES inscricao_turma(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
