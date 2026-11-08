-- T63 - Demonstracao da Release 1. Somente dados ficticios.
-- Ativado exclusivamente pelo perfil demo; as migrations comuns continuam isoladas.
-- Senha dos quatro usuarios: senha-de-teste (BCrypt custo 10).
-- IDs fixos e NOT EXISTS tornam uma nova execucao segura, sem restaurar/alterar dados
-- que o usuario ja tenha editado ou participantes que tenha incluido durante a demonstracao.

INSERT INTO usuarios (id, nome, email, senha, perfil, ativo)
SELECT CAST(v.id AS UUID), v.nome, v.email,
       '$2a$10$SJRWKNMlegspfR9KsCXok.O/aW/G/jE.ygrRPv8wmEWtaULqxj5rG', v.perfil, TRUE
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000001', 'Administrador Demo (Ficticio)', 'demo.admin@clinicalstudy.test', 'ADMINISTRADOR'),
    ('d6300000-0000-4000-8000-000000000002', 'Pesquisador Demo (Ficticio)', 'demo.pesquisador@clinicalstudy.test', 'PESQUISADOR'),
    ('d6300000-0000-4000-8000-000000000003', 'Coordenador Demo (Ficticio)', 'demo.coordenador@clinicalstudy.test', 'COORDENADOR'),
    ('d6300000-0000-4000-8000-000000000004', 'Participante Demo (Ficticio)', 'demo.participante@clinicalstudy.test', 'PARTICIPANTE')
) AS v(id, nome, email, perfil)
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = CAST(v.id AS UUID) OR u.email = v.email);

INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes, status)
SELECT CAST(v.id AS UUID), v.titulo, 'Demonstrar o fluxo de recrutamento apenas com dados ficticios.',
       u.id, DATE '2026-11-01', DATE '2027-05-01', v.limite, v.status
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000101', 'Demo - Protocolo em planejamento', 10, 'PLANEJAMENTO'),
    ('d6300000-0000-4000-8000-000000000102', 'Demo - Recrutamento com ultima vaga', 2, 'RECRUTAMENTO')
) AS v(id, titulo, limite, status)
JOIN usuarios u ON u.email = 'demo.pesquisador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST(v.id AS UUID));

INSERT INTO criterios (id, estudo_id, tipo, titulo, descricao, peso, determinante)
SELECT CAST(v.id AS UUID), CAST(v.estudo AS UUID), v.tipo, v.titulo, v.descricao, v.peso, v.determinante
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000201', 'd6300000-0000-4000-8000-000000000101', 'INCLUSAO', 'Maioridade ficticia', 'Ter pelo menos dezoito anos completos.', 1.0, TRUE),
    ('d6300000-0000-4000-8000-000000000202', 'd6300000-0000-4000-8000-000000000101', 'EXCLUSAO', 'Impedimento ficticio', 'Apresentar impedimento simulado ao acompanhamento.', NULL, NULL),
    ('d6300000-0000-4000-8000-000000000203', 'd6300000-0000-4000-8000-000000000102', 'INCLUSAO', 'Maioridade ficticia', 'Ter pelo menos dezoito anos completos.', 1.0, TRUE),
    ('d6300000-0000-4000-8000-000000000204', 'd6300000-0000-4000-8000-000000000102', 'EXCLUSAO', 'Impedimento ficticio', 'Apresentar impedimento simulado ao acompanhamento.', NULL, NULL)
) AS v(id, estudo, tipo, titulo, descricao, peso, determinante)
WHERE NOT EXISTS (SELECT 1 FROM criterios c WHERE c.id = CAST(v.id AS UUID));

INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem)
SELECT CAST(v.id AS UUID), CAST(v.estudo AS UUID), v.nome, v.dia, v.tolerancia, v.ordem
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000301', 'd6300000-0000-4000-8000-000000000101', 'Triagem ficticia', 0, 0, 1),
    ('d6300000-0000-4000-8000-000000000302', 'd6300000-0000-4000-8000-000000000101', 'Retorno ficticio', 30, 5, 2),
    ('d6300000-0000-4000-8000-000000000303', 'd6300000-0000-4000-8000-000000000102', 'Triagem ficticia', 0, 0, 1),
    ('d6300000-0000-4000-8000-000000000304', 'd6300000-0000-4000-8000-000000000102', 'Retorno ficticio', 30, 5, 2)
) AS v(id, estudo, nome, dia, tolerancia, ordem)
WHERE NOT EXISTS (SELECT 1 FROM visitas_planejadas p WHERE p.id = CAST(v.id AS UUID));

INSERT INTO acoes (id, visita_id, nome, descricao, categoria)
SELECT CAST(v.id AS UUID), CAST(v.visita AS UUID), v.nome, 'Procedimento simulado, sem resultado clinico real.', v.categoria
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000401', 'd6300000-0000-4000-8000-000000000301', 'Avaliacao inicial ficticia', 'AVALIACAO_CLINICA'),
    ('d6300000-0000-4000-8000-000000000402', 'd6300000-0000-4000-8000-000000000302', 'Questionario ficticio', 'QUESTIONARIO'),
    ('d6300000-0000-4000-8000-000000000403', 'd6300000-0000-4000-8000-000000000303', 'Avaliacao inicial ficticia', 'AVALIACAO_CLINICA'),
    ('d6300000-0000-4000-8000-000000000404', 'd6300000-0000-4000-8000-000000000304', 'Questionario ficticio', 'QUESTIONARIO')
) AS v(id, visita, nome, categoria)
WHERE NOT EXISTS (SELECT 1 FROM acoes a WHERE a.id = CAST(v.id AS UUID));

-- CAND-004 e CAND-006 estao prontos para inclusao. Ha UMA vaga: o primeiro entra,
-- e a tentativa de incluir o outro demonstra VAGAS_ESGOTADAS.
INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, contato, usuario_id, situacao)
SELECT CAST(v.id AS UUID), CAST('d6300000-0000-4000-8000-000000000102' AS UUID),
       v.codigo, v.nome, DATE '1990-01-01', NULL, NULL, v.situacao
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000501', 'CAND-001', 'Pessoa Alfa (Ficticia)', 'CADASTRADO'),
    ('d6300000-0000-4000-8000-000000000502', 'CAND-002', 'Pessoa Beta (Ficticia)', 'NAO_ELEGIVEL'),
    ('d6300000-0000-4000-8000-000000000503', 'CAND-003', 'Pessoa Gama (Ficticia)', 'ELEGIVEL'),
    ('d6300000-0000-4000-8000-000000000504', 'CAND-004', 'Pessoa Delta (Ficticia)', 'CONSENTIDO'),
    ('d6300000-0000-4000-8000-000000000505', 'CAND-005', 'Pessoa Epsilon (Ficticia)', 'INCLUIDO'),
    ('d6300000-0000-4000-8000-000000000506', 'CAND-006', 'Pessoa Zeta (Ficticia)', 'CONSENTIDO')
) AS v(id, codigo, nome, situacao)
WHERE NOT EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.id AS UUID));

INSERT INTO avaliacoes_elegibilidade (id, candidato_id, data_avaliacao, avaliador_id, resultado, pontuacao, vigente)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), TIMESTAMP '2026-11-01 10:00:00', u.id,
       v.resultado, v.pontuacao, TRUE
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000602', 'd6300000-0000-4000-8000-000000000502', 'REPROVADO', 0),
    ('d6300000-0000-4000-8000-000000000603', 'd6300000-0000-4000-8000-000000000503', 'APROVADO', 1),
    ('d6300000-0000-4000-8000-000000000604', 'd6300000-0000-4000-8000-000000000504', 'APROVADO', 1),
    ('d6300000-0000-4000-8000-000000000605', 'd6300000-0000-4000-8000-000000000505', 'APROVADO', 1),
    ('d6300000-0000-4000-8000-000000000606', 'd6300000-0000-4000-8000-000000000506', 'APROVADO', 1)
) AS v(id, candidato, resultado, pontuacao)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.id AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.candidato_id = CAST(v.candidato AS UUID));

INSERT INTO respostas_avaliacao (id, avaliacao_id, criterio_id, aplica, observacao)
SELECT CAST(v.id AS UUID), CAST(v.avaliacao AS UUID), CAST(v.criterio AS UUID), v.aplica,
       'Resposta ficticia para demonstracao.'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000701', 'd6300000-0000-4000-8000-000000000602', 'd6300000-0000-4000-8000-000000000203', FALSE),
    ('d6300000-0000-4000-8000-000000000702', 'd6300000-0000-4000-8000-000000000602', 'd6300000-0000-4000-8000-000000000204', FALSE),
    ('d6300000-0000-4000-8000-000000000703', 'd6300000-0000-4000-8000-000000000603', 'd6300000-0000-4000-8000-000000000203', TRUE),
    ('d6300000-0000-4000-8000-000000000704', 'd6300000-0000-4000-8000-000000000603', 'd6300000-0000-4000-8000-000000000204', FALSE),
    ('d6300000-0000-4000-8000-000000000705', 'd6300000-0000-4000-8000-000000000604', 'd6300000-0000-4000-8000-000000000203', TRUE),
    ('d6300000-0000-4000-8000-000000000706', 'd6300000-0000-4000-8000-000000000604', 'd6300000-0000-4000-8000-000000000204', FALSE),
    ('d6300000-0000-4000-8000-000000000707', 'd6300000-0000-4000-8000-000000000605', 'd6300000-0000-4000-8000-000000000203', TRUE),
    ('d6300000-0000-4000-8000-000000000708', 'd6300000-0000-4000-8000-000000000605', 'd6300000-0000-4000-8000-000000000204', FALSE),
    ('d6300000-0000-4000-8000-000000000709', 'd6300000-0000-4000-8000-000000000606', 'd6300000-0000-4000-8000-000000000203', TRUE),
    ('d6300000-0000-4000-8000-000000000710', 'd6300000-0000-4000-8000-000000000606', 'd6300000-0000-4000-8000-000000000204', FALSE)
) AS v(id, avaliacao, criterio, aplica)
WHERE EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.avaliacao AS UUID))
  AND NOT EXISTS (SELECT 1 FROM respostas_avaliacao r WHERE r.id = CAST(v.id AS UUID));

INSERT INTO consentimentos (id, candidato_id, data_hora, responsavel_id, versao_termo)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), TIMESTAMP '2026-11-01 11:00:00', u.id, 'TCLE-SIMULADO v1'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000000804', 'd6300000-0000-4000-8000-000000000504'),
    ('d6300000-0000-4000-8000-000000000805', 'd6300000-0000-4000-8000-000000000505'),
    ('d6300000-0000-4000-8000-000000000806', 'd6300000-0000-4000-8000-000000000506')
) AS v(id, candidato)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM consentimentos c WHERE c.candidato_id = CAST(v.candidato AS UUID));

INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000000905' AS UUID), CAST('d6300000-0000-4000-8000-000000000505' AS UUID),
       CAST('d6300000-0000-4000-8000-000000000102' AS UUID), DATE '2026-11-01', u.id
FROM usuarios u WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000000505' AS UUID));

INSERT INTO historico_status_estudo (id, estudo_id, estado_anterior, estado_novo, usuario_id, data_hora)
SELECT CAST('d6300000-0000-4000-8000-000000001001' AS UUID), CAST('d6300000-0000-4000-8000-000000000102' AS UUID),
       'PLANEJAMENTO', 'RECRUTAMENTO', u.id, TIMESTAMP '2026-11-01 09:00:00'
FROM usuarios u WHERE u.email = 'demo.pesquisador@clinicalstudy.test'
  AND NOT EXISTS (SELECT 1 FROM historico_status_estudo h WHERE h.estudo_id = CAST('d6300000-0000-4000-8000-000000000102' AS UUID));
