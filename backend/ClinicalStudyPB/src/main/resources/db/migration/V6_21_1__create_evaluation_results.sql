-- T87 (Sprint 3, RF14): resultado fictício de uma atividade de avaliação de uma visita agendada.
-- Um resultado por atividade e por visita agendada. Os resultados são fictícios: nenhum dado real de pessoa é armazenado.
-- A regra 4 (só visita REALIZADA, só atividade AVALIACAO_CLINICA) é do serviço (T89); o banco garante a unicidade
-- e as referências. Sem ON DELETE CASCADE: o histórico é preservado.
CREATE TABLE IF NOT EXISTS resultados_atividade (
    id                  UUID          NOT NULL,
    visita_agendada_id  UUID          NOT NULL,
    acao_id             UUID          NOT NULL,
    valor               VARCHAR(200)  NOT NULL,
    unidade             VARCHAR(30),
    observacao          VARCHAR(500),
    registrado_por_id   UUID          NOT NULL,
    registrado_em       TIMESTAMP     NOT NULL,
    alterado_por_id     UUID          NOT NULL,
    alterado_em         TIMESTAMP     NOT NULL,

    CONSTRAINT pk_resultados_atividade PRIMARY KEY (id),
    CONSTRAINT uk_resultados_atividade_visita_acao
        UNIQUE (visita_agendada_id, acao_id),
    CONSTRAINT fk_resultados_atividade_visita_agendada FOREIGN KEY (visita_agendada_id)
        REFERENCES visitas_agendadas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_resultados_atividade_acao FOREIGN KEY (acao_id)
        REFERENCES acoes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_resultados_atividade_registrado_por FOREIGN KEY (registrado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_resultados_atividade_alterado_por FOREIGN KEY (alterado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_resultados_atividade_visita_agendada
    ON resultados_atividade (visita_agendada_id);
