-- =============================================================================
-- Migration: V1__create_initial_tables.sql
-- Projeto:   ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos
-- Escopo:    Criação das tabelas iniciais de Usuários (RF01-RF03) e Estudos (RF04)
-- =============================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id      UUID         NOT NULL,
    nome    VARCHAR(120) NOT NULL,
    email   VARCHAR(180) NOT NULL,
    senha   VARCHAR(100) NOT NULL,
    perfil  VARCHAR(30)  NOT NULL,
    ativo   BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_perfil CHECK (
        perfil IN ('ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR', 'PARTICIPANTE')
    )
);

CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios (perfil);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON usuarios (ativo);

CREATE TABLE IF NOT EXISTS estudos (
    id                   UUID         NOT NULL,
    titulo               VARCHAR(180) NOT NULL,
    objetivo             TEXT         NOT NULL,
    responsavel_id       UUID         NOT NULL,
    data_inicio          DATE,
    data_fim             DATE,
    limite_participantes INTEGER      NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'PLANEJAMENTO',

    CONSTRAINT pk_estudos PRIMARY KEY (id),
    CONSTRAINT fk_estudos_responsavel FOREIGN KEY (responsavel_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_estudos_limite_participantes CHECK (limite_participantes >= 1),
    CONSTRAINT chk_estudos_status CHECK (
        status IN ('PLANEJAMENTO', 'RECRUTAMENTO', 'EXECUCAO', 'ENCERRADO')
    ),
    CONSTRAINT chk_estudos_datas CHECK (
        data_inicio IS NULL OR data_fim IS NULL OR data_fim >= data_inicio
    )
);

CREATE INDEX IF NOT EXISTS idx_estudos_responsavel_id ON estudos (responsavel_id);
CREATE INDEX IF NOT EXISTS idx_estudos_status ON estudos (status);
