-- =============================================================================
-- Migration: V3__normalizar_emails.sql
-- Escopo:    E-mails passam a ser únicos sem diferenciar maiúsculas de minúsculas;
--            a aplicação grava e consulta sempre em minúsculas.
-- =============================================================================

UPDATE usuarios SET email = LOWER(email) WHERE email <> LOWER(email);
