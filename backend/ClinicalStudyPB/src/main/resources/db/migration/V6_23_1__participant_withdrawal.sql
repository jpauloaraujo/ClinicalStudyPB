-- T101 (Sprint 3, RF16): situação da participação (ATIVO ou RETIRADO) e registro de retirada de participante.
-- Participantes incluídos nas Sprints anteriores recebem padrão ATIVO. A retirada é irreversível e preserva o
-- histórico: nenhuma chave estrangeira usa ON DELETE CASCADE.

ALTER TABLE participacoes
    ADD COLUMN IF NOT EXISTS situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
        CONSTRAINT chk_participacoes_situacao CHECK (situacao IN ('ATIVO', 'RETIRADO'));

CREATE TABLE IF NOT EXISTS retiradas_participacao (
    id                 UUID         NOT NULL,
    participacao_id    UUID         NOT NULL,
    motivo             VARCHAR(500) NOT NULL,
    solicitante        VARCHAR(20)  NOT NULL,
    data_retirada      DATE         NOT NULL,
    registrado_por_id  UUID         NOT NULL,
    registrado_em      TIMESTAMP    NOT NULL,

    CONSTRAINT pk_retiradas_participacao PRIMARY KEY (id),
    CONSTRAINT uk_retiradas_participacao UNIQUE (participacao_id),
    CONSTRAINT fk_retiradas_participacao_participacao FOREIGN KEY (participacao_id)
        REFERENCES participacoes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_retiradas_participacao_registrado_por FOREIGN KEY (registrado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_retiradas_participacao_solicitante CHECK (
        solicitante IN ('PARTICIPANTE', 'PESQUISADOR', 'COORDENACAO')
    )
);
