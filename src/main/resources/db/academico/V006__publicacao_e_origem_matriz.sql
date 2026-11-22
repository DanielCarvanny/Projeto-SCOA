ALTER TABLE matriz_curricular
    ADD COLUMN matriz_origem_id BIGINT NULL,
    ADD CONSTRAINT fk_matriz_origem FOREIGN KEY(matriz_origem_id) REFERENCES matriz_curricular(id) ON DELETE RESTRICT;

ALTER TABLE matriz_historico
    ADD COLUMN matriz_origem_id BIGINT NULL,
    ADD COLUMN evento VARCHAR(20) NOT NULL DEFAULT 'ALTERACAO',
    ADD CONSTRAINT ck_evento_matriz CHECK(evento IN ('ALTERACAO','PUBLICACAO','COPIA'));
