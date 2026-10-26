-- =============================================================================
-- Migration: V2__seed_initial_data.sql
-- Projeto:   ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos
-- Escopo:    Carga inicial determinística (seed) com dados fictícios para os
--            4 perfis de acesso e estudos clínicos demonstrativos (Sprint 1).
--            Senha padrão de todos os usuários de demonstração: "senha-de-teste"
--            (Hash BCrypt custo 10).
-- =============================================================================

-- 1. Administrador (RF01)
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo)
SELECT
    '11111111-1111-4111-8111-111111111111',
    'Administrador do Sistema (Fictício)',
    'admin@clinicalstudy.test',
    '$2a$10$SJRWKNMlegspfR9KsCXok.O/aW/G/jE.ygrRPv8wmEWtaULqxj5rG',
    'ADMINISTRADOR',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'admin@clinicalstudy.test'
);

-- 2. Pesquisador Responsável (RF04)
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo)
SELECT
    '22222222-2222-4222-8222-222222222222',
    'Dra. Helena Costa — Pesquisadora (Fictícia)',
    'pesquisador@clinicalstudy.test',
    '$2a$10$SJRWKNMlegspfR9KsCXok.O/aW/G/jE.ygrRPv8wmEWtaULqxj5rG',
    'PESQUISADOR',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'pesquisador@clinicalstudy.test'
);

-- 3. Coordenador do Estudo
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo)
SELECT
    '33333333-3333-4333-8333-333333333333',
    'Carlos Eduardo — Coordenador (Fictício)',
    'coordenador@clinicalstudy.test',
    '$2a$10$SJRWKNMlegspfR9KsCXok.O/aW/G/jE.ygrRPv8wmEWtaULqxj5rG',
    'COORDENADOR',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'coordenador@clinicalstudy.test'
);

-- 4. Participante
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo)
SELECT
    '44444444-4444-4444-8444-444444444444',
    'Mariana Silva — Participante (Fictícia)',
    'participante@clinicalstudy.test',
    '$2a$10$SJRWKNMlegspfR9KsCXok.O/aW/G/jE.ygrRPv8wmEWtaULqxj5rG',
    'PARTICIPANTE',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'participante@clinicalstudy.test'
);

-- 5. Estudos Clínicos Fictícios de Demonstração (RF04)
INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes, status)
SELECT
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
    'Estudo Clínico de Monitoramento da Hipertensão Arterial (Fictício)',
    'Avaliar a adesão ao protocolo de acompanhamento ambulatorial simplificado em participantes adultos com dados simulados.',
    u.id,
    DATE '2026-11-01',
    DATE '2027-05-01',
    50,
    'PLANEJAMENTO'
FROM usuarios u
WHERE u.email = 'pesquisador@clinicalstudy.test'
  AND NOT EXISTS (
      SELECT 1 FROM estudos WHERE id = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'
  );

INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes, status)
SELECT
    'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
    'Coorte Observacional de Qualidade do Sono e Hábitos Diários (Fictício)',
    'Acompanhar indicadores autorreferidos de rotina de sono e comparecimento a visitas periódicas simuladas.',
    u.id,
    DATE '2026-10-15',
    DATE '2027-03-15',
    30,
    'RECRUTAMENTO'
FROM usuarios u
WHERE u.email = 'pesquisador@clinicalstudy.test'
  AND NOT EXISTS (
      SELECT 1 FROM estudos WHERE id = 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'
  );
