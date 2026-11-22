-- ACA-02: catálogo e revisões imutáveis, sem alterar as migrações do ACA-01.
CREATE TABLE curso (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_as_cs NOT NULL,
    nome VARCHAR(200) NOT NULL,
    carga_horaria_total INT NOT NULL CHECK (carga_horaria_total > 0),
    situacao VARCHAR(8) NOT NULL CHECK (situacao IN ('ATIVO','INATIVO')),
    versao_registro BIGINT NOT NULL CHECK (versao_registro > 0),
    UNIQUE KEY uk_curso_codigo (codigo),
    CHECK (CHAR_LENGTH(TRIM(codigo)) > 0),
    CHECK (CHAR_LENGTH(TRIM(nome)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE curso_historico (
    item_id BIGINT NOT NULL,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_as_cs NOT NULL,
    nome VARCHAR(200) NOT NULL,
    carga_horaria_total INT NOT NULL CHECK (carga_horaria_total > 0),
    situacao VARCHAR(8) NOT NULL CHECK (situacao IN ('ATIVO','INATIVO')),
    versao_registro BIGINT NOT NULL CHECK (versao_registro > 0),
    autor_id BIGINT NOT NULL,
    instante DATETIME(6) NOT NULL,
    operacao_id CHAR(36) NOT NULL UNIQUE,
    origem VARCHAR(40) NOT NULL DEFAULT 'ACADEMICO_CATALOGO',
    PRIMARY KEY (item_id, versao_registro),
    CONSTRAINT fk_curso_historico FOREIGN KEY (item_id) REFERENCES curso(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disciplina (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_as_cs NOT NULL,
    nome VARCHAR(200) NOT NULL,
    ementa TEXT NOT NULL,
    carga_horaria INT NOT NULL CHECK (carga_horaria > 0),
    situacao VARCHAR(8) NOT NULL CHECK (situacao IN ('ATIVA','INATIVA')),
    versao_registro BIGINT NOT NULL CHECK (versao_registro > 0),
    UNIQUE KEY uk_disciplina_codigo (codigo),
    CHECK (CHAR_LENGTH(TRIM(codigo)) > 0),
    CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CHECK (CHAR_LENGTH(TRIM(ementa)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disciplina_historico (
    item_id BIGINT NOT NULL,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_as_cs NOT NULL,
    nome VARCHAR(200) NOT NULL,
    ementa TEXT NOT NULL,
    carga_horaria INT NOT NULL CHECK (carga_horaria > 0),
    situacao VARCHAR(8) NOT NULL CHECK (situacao IN ('ATIVA','INATIVA')),
    versao_registro BIGINT NOT NULL CHECK (versao_registro > 0),
    autor_id BIGINT NOT NULL,
    instante DATETIME(6) NOT NULL,
    operacao_id CHAR(36) NOT NULL UNIQUE,
    origem VARCHAR(40) NOT NULL DEFAULT 'ACADEMICO_CATALOGO',
    PRIMARY KEY (item_id, versao_registro),
    CONSTRAINT fk_disciplina_historico FOREIGN KEY (item_id) REFERENCES disciplina(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


