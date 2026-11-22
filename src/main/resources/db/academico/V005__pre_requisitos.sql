CREATE TABLE pre_requisito (
    matriz_id BIGINT NOT NULL,
    disciplina_id BIGINT NOT NULL,
    disciplina_exigida_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'APROVACAO_PREVIA' CHECK(tipo='APROVACAO_PREVIA'),
    PRIMARY KEY(matriz_id,disciplina_id,disciplina_exigida_id),
    CHECK(disciplina_id<>disciplina_exigida_id),
    FOREIGN KEY(matriz_id,disciplina_id) REFERENCES componente_curricular(matriz_curricular_id,disciplina_id) ON DELETE RESTRICT,
    FOREIGN KEY(matriz_id,disciplina_exigida_id) REFERENCES componente_curricular(matriz_curricular_id,disciplina_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pre_requisito_historico (
    matriz_id BIGINT NOT NULL,
    matriz_revisao BIGINT NOT NULL,
    disciplina_id BIGINT NOT NULL,
    disciplina_exigida_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'APROVACAO_PREVIA',
    PRIMARY KEY(matriz_id,matriz_revisao,disciplina_id,disciplina_exigida_id),
    FOREIGN KEY(matriz_id,matriz_revisao) REFERENCES matriz_historico(matriz_id,revisao) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
