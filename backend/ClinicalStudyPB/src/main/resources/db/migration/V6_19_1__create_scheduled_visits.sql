-- T73 (Sprint 3, RF12): visita agendada, a visita real de um participante, instância de uma visita prevista.
-- Uma visita agendada por visita prevista e por participante. Sem ON DELETE CASCADE: o histórico é preservado.
CREATE TABLE IF NOT EXISTS visitas_agendadas (
    id                   UUID         NOT NULL,
    participacao_id      UUID         NOT NULL,
    visita_planejada_id  UUID         NOT NULL,
    data_hora_agendada   TIMESTAMP    NOT NULL,
    situacao             VARCHAR(20)  NOT NULL DEFAULT 'AGENDADA',
    data_hora_realizada  TIMESTAMP,
    motivo               VARCHAR(500),
    observacao           VARCHAR(500),
    versao               BIGINT       NOT NULL DEFAULT 0,
    criado_por_id        UUID         NOT NULL,
    criado_em            TIMESTAMP    NOT NULL,
    alterado_por_id      UUID         NOT NULL,
    alterado_em          TIMESTAMP    NOT NULL,

    CONSTRAINT pk_visitas_agendadas PRIMARY KEY (id),
    CONSTRAINT uk_visitas_agendadas_participacao_visita
        UNIQUE (participacao_id, visita_planejada_id),
    CONSTRAINT fk_visitas_agendadas_participacao FOREIGN KEY (participacao_id)
        REFERENCES participacoes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_agendadas_visita_planejada FOREIGN KEY (visita_planejada_id)
        REFERENCES visitas_planejadas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_agendadas_criado_por FOREIGN KEY (criado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_agendadas_alterado_por FOREIGN KEY (alterado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_visitas_agendadas_situacao CHECK (
        situacao IN ('AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA')
    ),
    CONSTRAINT chk_visitas_agendadas_campos_por_situacao CHECK (
        (situacao <> 'REALIZADA' OR data_hora_realizada IS NOT NULL)
        AND (situacao <> 'CANCELADA' OR motivo IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_visitas_agendadas_participacao
    ON visitas_agendadas (participacao_id);

CREATE INDEX IF NOT EXISTS idx_visitas_agendadas_data_situacao
    ON visitas_agendadas (data_hora_agendada, situacao);
