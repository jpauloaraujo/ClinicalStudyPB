-- =============================================================================
-- Migration: V5__create_study_status_history.sql
-- Projeto:   ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos
-- Escopo:    Histórico das mudanças de estado do estudo (T38 / T41, RF07).
--            Cada avanço de PLANEJAMENTO -> RECRUTAMENTO -> EXECUCAO -> ENCERRADO
--            grava o estado anterior, o novo estado, quem avançou e quando.
-- =============================================================================

CREATE TABLE IF NOT EXISTS historico_status_estudo (
    id               UUID        NOT NULL,
    estudo_id        UUID        NOT NULL,
    estado_anterior  VARCHAR(30) NOT NULL,
    estado_novo      VARCHAR(30) NOT NULL,
    usuario_id       UUID        NOT NULL,
    data_hora        TIMESTAMP   NOT NULL,

    CONSTRAINT pk_historico_status_estudo PRIMARY KEY (id),
    -- O histórico acompanha o estudo: excluir o estudo apaga o seu histórico
    CONSTRAINT fk_historico_status_estudo FOREIGN KEY (estudo_id)
        REFERENCES estudos (id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_status_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    -- Os estados só avançam, um de cada vez (mesma regra da máquina de estados do domínio)
    CONSTRAINT chk_historico_status_avanco CHECK (
        (estado_anterior = 'PLANEJAMENTO' AND estado_novo = 'RECRUTAMENTO')
        OR (estado_anterior = 'RECRUTAMENTO' AND estado_novo = 'EXECUCAO')
        OR (estado_anterior = 'EXECUCAO' AND estado_novo = 'ENCERRADO')
    )
);

CREATE INDEX IF NOT EXISTS idx_historico_status_estudo_data ON historico_status_estudo (estudo_id, data_hora);
