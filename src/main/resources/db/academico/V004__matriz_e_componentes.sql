-- ACA-02: matriz em elaboração, componentes e snapshots transacionais.
CREATE TABLE matriz_curricular (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    curso_id BIGINT NOT NULL,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_as_cs NOT NULL,
    versao VARCHAR(255) COLLATE utf8mb4_0900_as_cs NOT NULL,
    inicio_vigencia DATE NOT NULL,
    fim_vigencia DATE NULL,
    situacao VARCHAR(20) NOT NULL CHECK(situacao IN ('EM_ELABORACAO','VIGENTE','ENCERRADA')),
    versao_registro BIGINT NOT NULL CHECK(versao_registro>0),
    UNIQUE KEY uk_matriz_versao(curso_id,codigo,versao),
    UNIQUE KEY uk_matriz_curso(id,curso_id),
    CONSTRAINT fk_matriz_curso FOREIGN KEY(curso_id) REFERENCES curso(id) ON DELETE RESTRICT,
    CHECK(CHAR_LENGTH(TRIM(codigo))>0),
    CHECK(CHAR_LENGTH(TRIM(versao))>0),
    CHECK(fim_vigencia IS NULL OR fim_vigencia>=inicio_vigencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE componente_curricular (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    matriz_curricular_id BIGINT NOT NULL,
    disciplina_id BIGINT NOT NULL,
    disciplina_revisao BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL CHECK(tipo IN ('OBRIGATORIO','ELETIVO')),
    periodo_ideal INT NOT NULL CHECK(periodo_ideal>0),
    carga_horaria_considerada INT NOT NULL CHECK(carga_horaria_considerada>0),
    UNIQUE KEY uk_componente_disciplina(matriz_curricular_id,disciplina_id),
    UNIQUE KEY uk_componente_matriz(id,matriz_curricular_id),
    CONSTRAINT fk_componente_matriz FOREIGN KEY(matriz_curricular_id) REFERENCES matriz_curricular(id) ON DELETE RESTRICT,
    CONSTRAINT fk_componente_revisao FOREIGN KEY(disciplina_id,disciplina_revisao)
        REFERENCES disciplina_historico(item_id,versao_registro) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE matriz_historico (
    matriz_id BIGINT NOT NULL,
    revisao BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    versao VARCHAR(255) NOT NULL,
    inicio_vigencia DATE NOT NULL,
    fim_vigencia DATE NULL,
    situacao VARCHAR(20) NOT NULL,
    autor_id BIGINT NOT NULL,
    instante DATETIME(6) NOT NULL,
    operacao_id CHAR(36) NOT NULL UNIQUE,
    origem VARCHAR(40) NOT NULL DEFAULT 'ACADEMICO_MATRIZ',
    PRIMARY KEY(matriz_id,revisao),
    CONSTRAINT fk_matriz_historico FOREIGN KEY(matriz_id) REFERENCES matriz_curricular(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE componente_historico (
    matriz_id BIGINT NOT NULL,
    matriz_revisao BIGINT NOT NULL,
    componente_id BIGINT NOT NULL,
    disciplina_id BIGINT NOT NULL,
    disciplina_revisao BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    periodo_ideal INT NOT NULL,
    carga_horaria_considerada INT NOT NULL,
    PRIMARY KEY(matriz_id,matriz_revisao,disciplina_id),
    CONSTRAINT fk_componente_hist_matriz FOREIGN KEY(matriz_id,matriz_revisao)
        REFERENCES matriz_historico(matriz_id,revisao) ON DELETE RESTRICT,
    CONSTRAINT fk_componente_hist_disciplina FOREIGN KEY(disciplina_id,disciplina_revisao)
        REFERENCES disciplina_historico(item_id,versao_registro) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

