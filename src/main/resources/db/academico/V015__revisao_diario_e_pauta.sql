ALTER TABLE turma
    ADD COLUMN versao_diario BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_turma_versao_diario CHECK (versao_diario >= 0);

CREATE TABLE pauta_final (
    turma_id BIGINT NOT NULL PRIMARY KEY,
    estado VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    revisao BIGINT NOT NULL,
    versao_diario BIGINT NOT NULL,
    homologada_por BIGINT NULL,
    homologada_em DATETIME(6) NULL,
    CONSTRAINT fk_pauta_final_turma FOREIGN KEY (turma_id) REFERENCES turma(id) ON DELETE RESTRICT,
    CHECK (estado IN ('ABERTA','HOMOLOGADA')),
    CHECK (revisao > 0),
    CHECK (versao_diario >= 0),
    CHECK ((estado = 'ABERTA' AND homologada_por IS NULL AND homologada_em IS NULL)
        OR (estado = 'HOMOLOGADA' AND homologada_por > 0 AND homologada_em IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pauta_final_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    turma_id BIGINT NOT NULL,
    evento VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    estado_anterior VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NULL,
    estado_novo VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    revisao_pauta BIGINT NOT NULL,
    observacao VARCHAR(1000) NOT NULL,
    autor_id BIGINT NOT NULL,
    registrado_em DATETIME(6) NOT NULL,
    origem VARCHAR(40) NOT NULL,
    operacao_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    UNIQUE KEY uk_pauta_final_evento_operacao (operacao_id),
    INDEX ix_pauta_final_auditoria_turma (turma_id, registrado_em, id),
    CONSTRAINT fk_pauta_final_auditoria_turma FOREIGN KEY (turma_id) REFERENCES turma(id) ON DELETE RESTRICT,
    CHECK (evento IN ('ANALISE_REGISTRADA','HOMOLOGADA','REABERTA')),
    CHECK (estado_anterior IS NULL OR estado_anterior IN ('ABERTA','HOMOLOGADA')),
    CHECK (estado_novo IN ('ABERTA','HOMOLOGADA')),
    CHECK (revisao_pauta > 0),
    CHECK (autor_id > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
