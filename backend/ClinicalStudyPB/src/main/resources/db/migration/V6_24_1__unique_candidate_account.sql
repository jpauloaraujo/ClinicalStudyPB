-- T111 (Sprint 3, RF17, regra 7): a conta do participante é vinculada a no máximo um candidato (relação um para um).
-- Vários candidatos sem conta (usuario_id nulo) continuam permitidos, no H2 e no PostgreSQL.
-- Se um banco já tiver dois candidatos com a mesma conta, esta migration falha de propósito: corrija os dados antes.

CREATE UNIQUE INDEX IF NOT EXISTS uk_candidatos_usuario ON candidatos (usuario_id);
