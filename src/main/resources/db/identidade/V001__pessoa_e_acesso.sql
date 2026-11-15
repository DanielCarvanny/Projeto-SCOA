-- Pessoa pode existir como referência legada somente com id. Não apagar registros.
CREATE TABLE IF NOT EXISTS pessoa (id INT NOT NULL PRIMARY KEY) ENGINE=InnoDB;
ALTER TABLE pessoa ADD cpf VARCHAR(11) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD nome VARCHAR(200) NULL,
    ADD email VARCHAR(254) NULL,
    ADD telefone VARCHAR(30) NULL,
    ADD CONSTRAINT uq_pessoa_cpf UNIQUE(cpf);
-- Campos nulos preservam referências antigas: elas não podem receber conta até saneamento explícito.
CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pessoa_id INT NOT NULL,
    login VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    esta_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 1,
    tentativas INT NOT NULL DEFAULT 0,
    bloqueado_ate TIMESTAMP(6) NULL,
    CONSTRAINT uq_usuario_pessoa UNIQUE(pessoa_id),
    CONSTRAINT uq_usuario_login UNIQUE(login),
    CONSTRAINT fk_usuario_pessoa FOREIGN KEY(pessoa_id) REFERENCES pessoa(id),
    CONSTRAINT ck_usuario_perfil CHECK(perfil IN ('COORDENADOR','ALUNO','PROFESSOR'))
) ENGINE=InnoDB;
CREATE TABLE controle_acesso (id INT PRIMARY KEY) ENGINE=InnoDB;
INSERT INTO controle_acesso(id) VALUES(1);
CREATE TABLE identidade_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NULL,
    operacao VARCHAR(40) NOT NULL,
    entidade VARCHAR(20) NOT NULL,
    entidade_id INT NULL,
    origem VARCHAR(30) NOT NULL DEFAULT 'ACADEMICO',
    instante TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;
