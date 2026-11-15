-- Pré-condição: migração da fundação com pessoa(id INT) já aplicada.
-- Não cria nem modifica tabelas de identidade. Baseline 0 explícito se o esquema já existir.
CREATE TABLE aluno (
    id INT NOT NULL AUTO_INCREMENT,
    pessoa_id INT NOT NULL,
    situacao_cadastral VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_aluno_pessoa UNIQUE (pessoa_id),
    CONSTRAINT fk_aluno_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
    CONSTRAINT ck_aluno_situacao CHECK (situacao_cadastral IN ('ATIVO', 'INATIVO'))
) ENGINE=InnoDB;

-- Auditoria local do Acadêmico; integração com auditoria institucional ainda pendente.
CREATE TABLE aluno_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT NOT NULL,
    usuario_id BIGINT NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    situacao_anterior VARCHAR(40) NULL,
    situacao_nova VARCHAR(40) NOT NULL,
    registrado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_auditoria_aluno FOREIGN KEY (aluno_id) REFERENCES aluno(id),
    CONSTRAINT ck_auditoria_autor CHECK (usuario_id > 0),
    CONSTRAINT ck_auditoria_operacao CHECK (operacao IN ('CADASTRO', 'ATUALIZACAO'))
) ENGINE=InnoDB;
