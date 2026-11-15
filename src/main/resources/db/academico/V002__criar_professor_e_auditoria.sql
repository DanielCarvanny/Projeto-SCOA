-- RF-02 e modelo físico p.229: maior titulação, situação ATIVO/INATIVO, pessoa única.
CREATE TABLE professor (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pessoa_id INT NOT NULL,
    titulacao VARCHAR(255) NOT NULL,
    situacao_funcional VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    CONSTRAINT uq_professor_pessoa UNIQUE(pessoa_id),
    CONSTRAINT fk_professor_pessoa FOREIGN KEY(pessoa_id) REFERENCES pessoa(id),
    CONSTRAINT ck_professor_titulacao CHECK(CHAR_LENGTH(TRIM(titulacao)) > 0),
    CONSTRAINT ck_professor_situacao CHECK(situacao_funcional IN ('ATIVO','INATIVO'))
) ENGINE=InnoDB;
CREATE TABLE professor_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    professor_id INT NOT NULL,
    usuario_id BIGINT NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    titulacao_anterior VARCHAR(255) NULL,
    titulacao_nova VARCHAR(255) NOT NULL,
    situacao_anterior VARCHAR(40) NULL,
    situacao_nova VARCHAR(40) NOT NULL,
    registrado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_auditoria_professor FOREIGN KEY(professor_id) REFERENCES professor(id),
    CONSTRAINT ck_professor_autor CHECK(usuario_id > 0),
    CONSTRAINT ck_professor_operacao CHECK(operacao IN ('CADASTRO','ATUALIZACAO'))
) ENGINE=InnoDB;
