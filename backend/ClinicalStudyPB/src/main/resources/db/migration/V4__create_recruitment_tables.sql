-- =============================================================================
-- Migration: V4__create_recruitment_tables.sql
-- Projeto:   ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos
-- Escopo:    Modelo de domínio de recrutamento (RF05 a RF11): critérios, visitas
--            planejadas, ações, candidatos, avaliações de elegibilidade, consentimentos
--            e participações.
-- =============================================================================

-- Critérios de aceitação do estudo. Herança de tabela única: a coluna "tipo" distingue
-- critérios de inclusão (com peso e flag determinante) dos de exclusão (sempre determinantes).
CREATE TABLE IF NOT EXISTS criterios (
    id            UUID         NOT NULL,
    estudo_id     UUID         NOT NULL,
    tipo          VARCHAR(20)  NOT NULL,
    titulo        VARCHAR(120) NOT NULL,
    descricao     TEXT         NOT NULL,
    peso          REAL,
    determinante  BOOLEAN,

    CONSTRAINT pk_criterios PRIMARY KEY (id),
    CONSTRAINT fk_criterios_estudo FOREIGN KEY (estudo_id)
        REFERENCES estudos (id) ON DELETE CASCADE,
    CONSTRAINT chk_criterios_tipo CHECK (tipo IN ('INCLUSAO', 'EXCLUSAO')),
    CONSTRAINT chk_criterios_campos_por_tipo CHECK (
        (tipo = 'INCLUSAO' AND peso IS NOT NULL AND peso > 0 AND determinante IS NOT NULL)
        OR (tipo = 'EXCLUSAO' AND peso IS NULL AND determinante IS NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_criterios_estudo_id ON criterios (estudo_id);

-- Visitas planejadas: o roteiro do protocolo, em dias contados a partir da inclusão (dia 0).
CREATE TABLE IF NOT EXISTS visitas_planejadas (
    id            UUID         NOT NULL,
    estudo_id     UUID         NOT NULL,
    nome          VARCHAR(100) NOT NULL,
    dia_previsto  INTEGER      NOT NULL,
    tolerancia    INTEGER      NOT NULL,
    ordem         INTEGER      NOT NULL,

    CONSTRAINT pk_visitas_planejadas PRIMARY KEY (id),
    CONSTRAINT fk_visitas_estudo FOREIGN KEY (estudo_id)
        REFERENCES estudos (id) ON DELETE CASCADE,
    CONSTRAINT uk_visitas_estudo_dia UNIQUE (estudo_id, dia_previsto),
    CONSTRAINT chk_visitas_dia_previsto CHECK (dia_previsto >= 0),
    CONSTRAINT chk_visitas_tolerancia CHECK (tolerancia BETWEEN 0 AND 30),
    CONSTRAINT chk_visitas_ordem CHECK (ordem >= 1)
);

CREATE INDEX IF NOT EXISTS idx_visitas_estudo_id ON visitas_planejadas (estudo_id);

-- Ações (procedimentos) de cada visita planejada; somem junto com a visita.
CREATE TABLE IF NOT EXISTS acoes (
    id         UUID         NOT NULL,
    visita_id  UUID         NOT NULL,
    nome       VARCHAR(100) NOT NULL,
    descricao  VARCHAR(500),
    categoria  VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_acoes PRIMARY KEY (id),
    CONSTRAINT fk_acoes_visita FOREIGN KEY (visita_id)
        REFERENCES visitas_planejadas (id) ON DELETE CASCADE,
    CONSTRAINT chk_acoes_categoria CHECK (
        categoria IN ('AVALIACAO_CLINICA', 'EXAME_LABORATORIAL', 'EXAME_IMAGEM',
                      'QUESTIONARIO', 'MEDICACAO', 'OUTROS')
    )
);

CREATE INDEX IF NOT EXISTS idx_acoes_visita_id ON acoes (visita_id);

-- Candidatos em triagem. Só dados fictícios; o código (CAND-001...) é gerado pelo sistema.
CREATE TABLE IF NOT EXISTS candidatos (
    id               UUID         NOT NULL,
    estudo_id        UUID         NOT NULL,
    codigo           VARCHAR(20)  NOT NULL,
    nome_ficticio    VARCHAR(120) NOT NULL,
    data_nascimento  DATE         NOT NULL,
    contato          VARCHAR(180),
    usuario_id       UUID,
    situacao         VARCHAR(30)  NOT NULL DEFAULT 'CADASTRADO',

    CONSTRAINT pk_candidatos PRIMARY KEY (id),
    CONSTRAINT fk_candidatos_estudo FOREIGN KEY (estudo_id)
        REFERENCES estudos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_candidatos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT uk_candidatos_estudo_codigo UNIQUE (estudo_id, codigo),
    CONSTRAINT uk_candidatos_estudo_nome_nascimento UNIQUE (estudo_id, nome_ficticio, data_nascimento),
    CONSTRAINT chk_candidatos_situacao CHECK (
        situacao IN ('CADASTRADO', 'EM_AVALIACAO', 'ELEGIVEL', 'NAO_ELEGIVEL', 'CONSENTIDO', 'INCLUIDO')
    )
);

CREATE INDEX IF NOT EXISTS idx_candidatos_estudo_situacao ON candidatos (estudo_id, situacao);

-- Avaliações de elegibilidade. Um candidato pode ter várias (histórico); só a mais recente é vigente.
CREATE TABLE IF NOT EXISTS avaliacoes_elegibilidade (
    id              UUID        NOT NULL,
    candidato_id    UUID        NOT NULL,
    data_avaliacao  TIMESTAMP   NOT NULL,
    avaliador_id    UUID        NOT NULL,
    resultado       VARCHAR(20) NOT NULL,
    pontuacao       REAL        NOT NULL DEFAULT 0,
    vigente         BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_avaliacoes_elegibilidade PRIMARY KEY (id),
    CONSTRAINT fk_avaliacoes_candidato FOREIGN KEY (candidato_id)
        REFERENCES candidatos (id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacoes_avaliador FOREIGN KEY (avaliador_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_avaliacoes_resultado CHECK (resultado IN ('APROVADO', 'REPROVADO'))
);

CREATE INDEX IF NOT EXISTS idx_avaliacoes_candidato_id ON avaliacoes_elegibilidade (candidato_id);

-- Respostas da avaliação: uma por critério do estudo.
CREATE TABLE IF NOT EXISTS respostas_avaliacao (
    id            UUID         NOT NULL,
    avaliacao_id  UUID         NOT NULL,
    criterio_id   UUID         NOT NULL,
    aplica        BOOLEAN      NOT NULL,
    observacao    VARCHAR(500),

    CONSTRAINT pk_respostas_avaliacao PRIMARY KEY (id),
    CONSTRAINT fk_respostas_avaliacao FOREIGN KEY (avaliacao_id)
        REFERENCES avaliacoes_elegibilidade (id) ON DELETE CASCADE,
    CONSTRAINT fk_respostas_criterio FOREIGN KEY (criterio_id)
        REFERENCES criterios (id) ON DELETE RESTRICT,
    CONSTRAINT uk_respostas_avaliacao_criterio UNIQUE (avaliacao_id, criterio_id)
);

CREATE INDEX IF NOT EXISTS idx_respostas_criterio_id ON respostas_avaliacao (criterio_id);

-- Consentimento simulado (TCLE): no máximo um por candidato.
CREATE TABLE IF NOT EXISTS consentimentos (
    id              UUID        NOT NULL,
    candidato_id    UUID        NOT NULL,
    data_hora       TIMESTAMP   NOT NULL,
    responsavel_id  UUID        NOT NULL,
    versao_termo    VARCHAR(60) NOT NULL,

    CONSTRAINT pk_consentimentos PRIMARY KEY (id),
    CONSTRAINT uk_consentimentos_candidato UNIQUE (candidato_id),
    CONSTRAINT fk_consentimentos_candidato FOREIGN KEY (candidato_id)
        REFERENCES candidatos (id) ON DELETE CASCADE,
    CONSTRAINT fk_consentimentos_responsavel FOREIGN KEY (responsavel_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

-- Participação (inclusão): a data de inclusão é o dia 0 do cronograma do participante.
CREATE TABLE IF NOT EXISTS participacoes (
    id               UUID NOT NULL,
    candidato_id     UUID NOT NULL,
    estudo_id        UUID NOT NULL,
    data_inclusao    DATE NOT NULL,
    incluido_por_id  UUID NOT NULL,

    CONSTRAINT pk_participacoes PRIMARY KEY (id),
    CONSTRAINT uk_participacoes_candidato UNIQUE (candidato_id),
    CONSTRAINT fk_participacoes_candidato FOREIGN KEY (candidato_id)
        REFERENCES candidatos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_participacoes_estudo FOREIGN KEY (estudo_id)
        REFERENCES estudos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_participacoes_incluido_por FOREIGN KEY (incluido_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_participacoes_estudo_id ON participacoes (estudo_id);
