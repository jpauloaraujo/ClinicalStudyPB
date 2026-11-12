-- T74 (Sprint 3, RF12) - Estudo de acompanhamento com tres participantes e visitas agendadas. Somente dados ficticios.
-- Perfil demo. Depende dos usuarios demo criados por R__dados_demonstracao.sql (T63), que roda antes (ordem alfabetica da descricao).
-- O estudo de recrutamento da T63 (...000000000102) NAO e alterado: ele serve ao roteiro da apresentacao da Release 1.
--
-- Estudo "Demo - Acompanhamento de participantes" (EXECUCAO, limite 6), D = data da carga:
--   P1 CAND-001 "Pessoa Teta (Ficticia)"  incluido em D-20: Triagem REALIZADA (D-20 09:00, realizada 09:10); Retorno 30 AGENDADA em D+10 09:30; Retorno 60 sem agendamento
--   P2 CAND-002 "Pessoa Iota (Ficticia)"  incluido em D-3:  Triagem AGENDADA em D 15:00; demais sem agendamento
--   P3 CAND-003 "Pessoa Kappa (Ficticia)" incluido em D-1:  nenhuma visita agendada (para demonstrar o agendamento)
-- Os participantes entram pelo SQL (a inclusao pela API exigiria o estudo em recrutamento).
-- Datas relativas a CURRENT_DATE; ids fixos 0000000019NN; INSERT ... WHERE NOT EXISTS, entao repetir a carga nao duplica
-- nem restaura o que o usuario alterou. Os participantes P4 a P6 sao criados pelos PBIs 20 e 23.

INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes, status)
SELECT CAST('d6300000-0000-4000-8000-000000001901' AS UUID), 'Demo - Acompanhamento de participantes',
       'Demonstrar o agendamento, o desfecho e o historico das visitas apenas com dados ficticios.',
       u.id, CAST(CURRENT_DATE - INTERVAL '40' DAY AS DATE), CAST(CURRENT_DATE + INTERVAL '180' DAY AS DATE), 6, 'EXECUCAO'
FROM usuarios u
WHERE u.email = 'demo.pesquisador@clinicalstudy.test'
  AND NOT EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID));

INSERT INTO criterios (id, estudo_id, tipo, titulo, descricao, peso, determinante)
SELECT CAST('d6300000-0000-4000-8000-000000001902' AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID),
       'INCLUSAO', 'Maioridade ficticia', 'Ter pelo menos dezoito anos completos.', 1.0, TRUE
WHERE EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM criterios c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001902' AS UUID));

-- O estudo ja esta em EXECUCAO: uma linha no historico de estados registra a entrada nessa fase
INSERT INTO historico_status_estudo (id, estudo_id, estado_anterior, estado_novo, usuario_id, data_hora)
SELECT CAST('d6300000-0000-4000-8000-000000001903' AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID),
       'RECRUTAMENTO', 'EXECUCAO', u.id, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '30' DAY + INTERVAL '9' HOUR
FROM usuarios u
WHERE u.email = 'demo.pesquisador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM historico_status_estudo h WHERE h.id = CAST('d6300000-0000-4000-8000-000000001903' AS UUID));

INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem)
SELECT CAST(v.id AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID), v.nome, v.dia, v.tolerancia, v.ordem
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001931', 'Triagem', 0, 3, 1),
    ('d6300000-0000-4000-8000-000000001932', 'Retorno 30 dias', 30, 5, 2),
    ('d6300000-0000-4000-8000-000000001933', 'Retorno 60 dias', 60, 7, 3)
) AS v(id, nome, dia, tolerancia, ordem)
WHERE EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_planejadas p WHERE p.id = CAST(v.id AS UUID));

INSERT INTO acoes (id, visita_id, nome, descricao, categoria)
SELECT CAST(v.id AS UUID), CAST(v.visita AS UUID), v.nome, 'Procedimento simulado, sem resultado clinico real.', v.categoria
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001941', 'd6300000-0000-4000-8000-000000001931', 'Aferir pressão arterial', 'AVALIACAO_CLINICA'),
    ('d6300000-0000-4000-8000-000000001942', 'd6300000-0000-4000-8000-000000001931', 'Coletar sangue', 'EXAME_LABORATORIAL'),
    ('d6300000-0000-4000-8000-000000001943', 'd6300000-0000-4000-8000-000000001932', 'Escala de dor', 'QUESTIONARIO'),
    ('d6300000-0000-4000-8000-000000001944', 'd6300000-0000-4000-8000-000000001932', 'Exame físico', 'AVALIACAO_CLINICA'),
    ('d6300000-0000-4000-8000-000000001945', 'd6300000-0000-4000-8000-000000001933', 'Avaliação final', 'AVALIACAO_CLINICA'),
    ('d6300000-0000-4000-8000-000000001946', 'd6300000-0000-4000-8000-000000001933', 'Entrega de medicação', 'MEDICACAO')
) AS v(id, visita, nome, categoria)
WHERE EXISTS (SELECT 1 FROM visitas_planejadas p WHERE p.id = CAST(v.visita AS UUID))
  AND NOT EXISTS (SELECT 1 FROM acoes a WHERE a.id = CAST(v.id AS UUID));

-- Candidatos ja incluidos, com avaliacao aprovada e consentimento, como no fluxo real de recrutamento
INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, contato, usuario_id, situacao)
SELECT CAST(v.id AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID), v.codigo, v.nome,
       DATE '1990-01-01', NULL, NULL, 'INCLUIDO'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001911', 'CAND-001', 'Pessoa Teta (Ficticia)'),
    ('d6300000-0000-4000-8000-000000001912', 'CAND-002', 'Pessoa Iota (Ficticia)'),
    ('d6300000-0000-4000-8000-000000001913', 'CAND-003', 'Pessoa Kappa (Ficticia)')
) AS v(id, codigo, nome)
WHERE EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.id AS UUID));

INSERT INTO avaliacoes_elegibilidade (id, candidato_id, data_avaliacao, avaliador_id, resultado, pontuacao, vigente)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '40' DAY + INTERVAL '10' HOUR,
       u.id, 'APROVADO', 1, TRUE
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001951', 'd6300000-0000-4000-8000-000000001911'),
    ('d6300000-0000-4000-8000-000000001952', 'd6300000-0000-4000-8000-000000001912'),
    ('d6300000-0000-4000-8000-000000001953', 'd6300000-0000-4000-8000-000000001913')
) AS v(id, candidato)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.candidato AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.id AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.candidato_id = CAST(v.candidato AS UUID));

INSERT INTO respostas_avaliacao (id, avaliacao_id, criterio_id, aplica, observacao)
SELECT CAST(v.id AS UUID), CAST(v.avaliacao AS UUID), CAST('d6300000-0000-4000-8000-000000001902' AS UUID), TRUE,
       'Resposta ficticia para demonstracao.'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001961', 'd6300000-0000-4000-8000-000000001951'),
    ('d6300000-0000-4000-8000-000000001962', 'd6300000-0000-4000-8000-000000001952'),
    ('d6300000-0000-4000-8000-000000001963', 'd6300000-0000-4000-8000-000000001953')
) AS v(id, avaliacao)
WHERE EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.avaliacao AS UUID))
  AND EXISTS (SELECT 1 FROM criterios c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001902' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM respostas_avaliacao r WHERE r.id = CAST(v.id AS UUID));

INSERT INTO consentimentos (id, candidato_id, data_hora, responsavel_id, versao_termo)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '40' DAY + INTERVAL '11' HOUR,
       u.id, 'TCLE-SIMULADO v1'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000001971', 'd6300000-0000-4000-8000-000000001911'),
    ('d6300000-0000-4000-8000-000000001972', 'd6300000-0000-4000-8000-000000001912'),
    ('d6300000-0000-4000-8000-000000001973', 'd6300000-0000-4000-8000-000000001913')
) AS v(id, candidato)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.candidato AS UUID))
  AND NOT EXISTS (SELECT 1 FROM consentimentos c WHERE c.candidato_id = CAST(v.candidato AS UUID));

-- Participacoes: a data de inclusao e o dia 0 de cada participante
INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000001921' AS UUID), CAST('d6300000-0000-4000-8000-000000001911' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '20' DAY AS DATE), u.id
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001911' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000001911' AS UUID));

INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000001922' AS UUID), CAST('d6300000-0000-4000-8000-000000001912' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '3' DAY AS DATE), u.id
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001912' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000001912' AS UUID));

INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000001923' AS UUID), CAST('d6300000-0000-4000-8000-000000001913' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), u.id
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001913' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000001913' AS UUID));

-- Visitas agendadas. P1: Triagem realizada e Retorno 30 agendado (data prevista D+10, dentro da janela). P2: Triagem hoje.
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000001981' AS UUID), CAST('d6300000-0000-4000-8000-000000001921' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001931' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '20' DAY + INTERVAL '9' HOUR, 'REALIZADA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '20' DAY + INTERVAL '9' HOUR + INTERVAL '10' MINUTE,
       NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000001921' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000001921' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000001982' AS UUID), CAST('d6300000-0000-4000-8000-000000001921' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001932' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '10' DAY + INTERVAL '9' HOUR + INTERVAL '30' MINUTE, 'AGENDADA',
       NULL, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000001921' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000001921' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000001983' AS UUID), CAST('d6300000-0000-4000-8000-000000001922' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001931' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '15' HOUR, 'AGENDADA',
       NULL, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000001922' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000001922' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID));
