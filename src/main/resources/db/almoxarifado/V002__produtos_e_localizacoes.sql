CREATE TABLE localizacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500) NOT NULL DEFAULT '',
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_localizacao_nome (nome)
) ENGINE=InnoDB;

CREATE TABLE produto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(160) NOT NULL,
    tipo VARCHAR(32) NOT NULL,
    unidade VARCHAR(12) NOT NULL,
    codigo_barras VARCHAR(64) NULL,
    codigo_barras_ativo VARCHAR(64) GENERATED ALWAYS AS
        (CASE WHEN ativo = TRUE THEN codigo_barras ELSE NULL END) STORED,
    localizacao_id BIGINT NOT NULL,
    estoque_minimo INT NOT NULL,
    estoque_maximo INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_produto_codigo_barras_ativo (codigo_barras_ativo),
    KEY ix_produto_localizacao (localizacao_id),
    CONSTRAINT fk_produto_localizacao FOREIGN KEY (localizacao_id) REFERENCES localizacao (id),
    CONSTRAINT ck_produto_tipo CHECK (tipo IN ('MATERIAL_CONSUMO', 'MATERIAL_PERMANENTE')),
    CONSTRAINT ck_produto_estoque_minimo CHECK (estoque_minimo >= 0),
    CONSTRAINT ck_produto_estoque_maximo CHECK (estoque_maximo >= estoque_minimo),
    CONSTRAINT ck_produto_codigo_barras CHECK (codigo_barras IS NULL OR codigo_barras REGEXP '^[A-Za-z0-9]{1,64}$')
) ENGINE=InnoDB;
