ALTER TABLE produto
    ADD COLUMN saldo INT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_produto_saldo CHECK (saldo >= 0);

CREATE TABLE movimentacao_estoque (
    id BIGINT NOT NULL AUTO_INCREMENT,
    produto_id BIGINT NOT NULL,
    tipo VARCHAR(8) NOT NULL,
    quantidade INT NOT NULL,
    usuario_autor_id BIGINT NOT NULL,
    instante_utc DATETIME(6) NOT NULL,
    origem VARCHAR(120) NOT NULL,
    operacao_id VARCHAR(100) NOT NULL,
    saldo_resultante INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_movimentacao_operacao (operacao_id),
    KEY ix_movimentacao_produto_instante (produto_id, instante_utc),
    CONSTRAINT fk_movimentacao_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT ck_movimentacao_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_movimentacao_saldo CHECK (saldo_resultante >= 0)
) ENGINE=InnoDB;
