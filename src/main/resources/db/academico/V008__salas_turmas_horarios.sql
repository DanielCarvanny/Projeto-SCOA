CREATE TABLE sala (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, identificacao VARCHAR(100) NOT NULL, localizacao VARCHAR(255) NOT NULL,
 capacidade INT NOT NULL, recursos TEXT NOT NULL, situacao VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 versao_registro BIGINT NOT NULL, operacao_criacao CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 UNIQUE KEY uk_sala_identificacao(identificacao), UNIQUE KEY uk_sala_operacao(operacao_criacao),
 CHECK(capacidade>0), CHECK(situacao IN ('DISPONIVEL','INDISPONIVEL')), CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE turma (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, periodo_id BIGINT NOT NULL, disciplina_id BIGINT NOT NULL, professor_id INT NOT NULL,
 codigo VARCHAR(50) NOT NULL, capacidade INT NOT NULL, situacao VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 versao_registro BIGINT NOT NULL, operacao_criacao CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 UNIQUE KEY uk_turma_periodo_codigo(periodo_id,codigo), UNIQUE KEY uk_turma_operacao(operacao_criacao),
 INDEX ix_turma_professor_periodo(professor_id,periodo_id),
 FOREIGN KEY(periodo_id) REFERENCES periodo_letivo(id) ON DELETE RESTRICT, FOREIGN KEY(disciplina_id) REFERENCES disciplina(id) ON DELETE RESTRICT,
 FOREIGN KEY(professor_id) REFERENCES professor(id) ON DELETE RESTRICT, CHECK(capacidade>0),
 CHECK(situacao IN ('PLANEJADA','ABERTA','EM_ANDAMENTO','ENCERRADA','CANCELADA')), CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE horario_turma (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, turma_id BIGINT NOT NULL, sala_id BIGINT NULL, dia_semana VARCHAR(12) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 hora_inicio TIME NOT NULL, hora_fim TIME NOT NULL, versao_registro BIGINT NOT NULL,
 UNIQUE KEY uk_horario_turma(turma_id,dia_semana,hora_inicio,hora_fim), INDEX ix_horario_sala(sala_id,dia_semana,hora_inicio,hora_fim),
 FOREIGN KEY(turma_id) REFERENCES turma(id) ON DELETE RESTRICT, FOREIGN KEY(sala_id) REFERENCES sala(id) ON DELETE RESTRICT,
 CHECK(dia_semana IN ('SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO','DOMINGO')), CHECK(hora_fim>hora_inicio), CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE turma_auditoria (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, turma_id BIGINT NOT NULL, evento VARCHAR(20) NOT NULL, autor_id BIGINT NOT NULL,
 instante DATETIME(6) NOT NULL, operacao_id CHAR(36) NOT NULL, UNIQUE KEY uk_turma_auditoria_operacao(operacao_id),
 FOREIGN KEY(turma_id) REFERENCES turma(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
