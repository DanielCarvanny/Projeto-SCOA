CREATE TABLE avaliacao_academica (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    turma_id BIGINT NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    descricao VARCHAR(1000) NOT NULL,
    data_aplicacao DATE NOT NULL,
    tipo VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    versao_registro BIGINT NOT NULL,
    operacao_criacao CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    UNIQUE KEY uk_avaliacao_operacao(operacao_criacao),
    INDEX ix_avaliacao_turma_data(turma_id,data_aplicacao,id),
    CONSTRAINT fk_avaliacao_turma FOREIGN KEY(turma_id) REFERENCES turma(id) ON DELETE RESTRICT,
    CHECK(CHAR_LENGTH(TRIM(titulo))>0),
    CHECK(tipo IN ('PROVA','EXAME_FINAL')),
    CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE avaliacao_academica_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL,
    evento VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    titulo_anterior VARCHAR(120) NULL,
    titulo_novo VARCHAR(120) NOT NULL,
    data_anterior DATE NULL,
    data_nova DATE NOT NULL,
    tipo_anterior VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NULL,
    tipo_novo VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    descricao_anterior VARCHAR(1000) NULL,
    descricao_nova VARCHAR(1000) NOT NULL,
    autor_id BIGINT NOT NULL,
    registrado_em DATETIME(6) NOT NULL,
    origem VARCHAR(40) NOT NULL,
    operacao_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    UNIQUE KEY uk_avaliacao_auditoria_operacao(operacao_id),
    INDEX ix_avaliacao_auditoria(avaliacao_id,registrado_em),
    CONSTRAINT fk_avaliacao_auditoria FOREIGN KEY(avaliacao_id) REFERENCES avaliacao_academica(id) ON DELETE RESTRICT,
    CHECK(evento IN ('CADASTRO','ALTERACAO')),
    CHECK(autor_id>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE nota_avaliacao (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL,
    inscricao_id BIGINT NOT NULL,
    nota DECIMAL(3,1) NOT NULL,
    versao_registro BIGINT NOT NULL,
    UNIQUE KEY uk_nota_avaliacao_inscricao(avaliacao_id,inscricao_id),
    INDEX ix_nota_inscricao(inscricao_id,avaliacao_id),
    CONSTRAINT fk_nota_avaliacao FOREIGN KEY(avaliacao_id) REFERENCES avaliacao_academica(id) ON DELETE RESTRICT,
    CONSTRAINT fk_nota_inscricao FOREIGN KEY(inscricao_id) REFERENCES inscricao_turma(id) ON DELETE RESTRICT,
    CHECK(nota>=0.0 AND nota<=10.0),
    CHECK(versao_registro>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE nota_avaliacao_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nota_avaliacao_id BIGINT NOT NULL,
    nota_anterior DECIMAL(3,1) NULL,
    nota_nova DECIMAL(3,1) NOT NULL,
    autor_id BIGINT NOT NULL,
    registrado_em DATETIME(6) NOT NULL,
    origem VARCHAR(40) NOT NULL,
    operacao_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    UNIQUE KEY uk_nota_auditoria_operacao(operacao_id),
    INDEX ix_nota_auditoria(nota_avaliacao_id,registrado_em),
    CONSTRAINT fk_nota_auditoria FOREIGN KEY(nota_avaliacao_id) REFERENCES nota_avaliacao(id) ON DELETE RESTRICT,
    CHECK(nota_nova>=0.0 AND nota_nova<=10.0),
    CHECK(nota_anterior IS NULL OR (nota_anterior>=0.0 AND nota_anterior<=10.0)),
    CHECK(autor_id>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
