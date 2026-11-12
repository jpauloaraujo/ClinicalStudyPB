CREATE TABLE IF NOT EXISTS visitas_agendadas (
    id                       UUID         NOT NULL,
    participacao_id          UUID         NOT NULL,
    visita_planejada_id      UUID         NOT NULL,
    data_hora_agendada       TIMESTAMP    NOT NULL,
    situacao                 VARCHAR(20)  NOT NULL,
    motivo_observacao        VARCHAR(500),
    usuario_id               UUID         NOT NULL,
    data_criacao             TIMESTAMP    NOT NULL,
    data_ultima_alteracao    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_visitas_agendadas PRIMARY KEY (id),
    CONSTRAINT uk_visitas_agendadas_participacao_visita
        UNIQUE (participacao_id, visita_planejada_id),
    CONSTRAINT fk_visitas_agendadas_participacao FOREIGN KEY (participacao_id)
        REFERENCES participacoes (id) ON DELETE CASCADE,
    CONSTRAINT fk_visitas_agendadas_visita_planejada FOREIGN KEY (visita_planejada_id)
        REFERENCES visitas_planejadas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_agendadas_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_visitas_agendadas_situacao CHECK (
        situacao IN ('AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA')
    )
);

CREATE INDEX IF NOT EXISTS idx_visitas_agendadas_participacao_data
    ON visitas_agendadas (participacao_id, data_hora_agendada);
