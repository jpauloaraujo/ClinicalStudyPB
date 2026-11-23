-- T84 (Sprint 3, RF13) - Participantes P4 e P5 com visitas em todas as situacoes. Somente dados ficticios.
-- Perfil demo. Completa o estudo "Demo - Acompanhamento de participantes" criado pelo R__demo_19_visitas_agendadas.sql
-- (T74), que roda antes (ordem alfabetica da descricao: demo 19 < demo 20 < demo 22). Sem aquele estudo este script nao faz nada.
-- O estudo de recrutamento da T63 (...000000000102) e os dados do script demo 19 NAO sao alterados.
--
-- D = data da carga. Participantes incluidos pelo SQL (a inclusao pela API exigiria o estudo em recrutamento):
--   P4 CAND-004 "Pessoa Lambda (Ficticia)" incluido em D-35: Triagem REALIZADA (D-35 09:00, realizada 09:10);
--      Retorno 30 AUSENTE (agendada D-4 10:00, dentro da janela D-10 a D); Retorno 60 CANCELADA (D+25 10:00, motivo "Participante em viagem")
--   P5 CAND-005 "Pessoa Mi (Ficticia)" incluido em D-10: Triagem CANCELADA (D-8 09:00, motivo "Consulta remarcada pelo centro");
--      Retorno 30 AGENDADA em D+20 09:30; Retorno 60 sem agendamento
-- Com P1 a P5 o estudo (limite 6) fica com 5 de 6 vagas; o P6 entra no PBI-23.
-- Respeita as constraints da T73: REALIZADA tem data_hora_realizada e CANCELADA tem motivo.
-- O R__demo_22_historico.sql (T95) so insere visitas do P4 que ainda nao existem; como estas ja existem, ele nao altera nada.
-- Datas relativas a CURRENT_DATE; ids fixos 0000000020NN; INSERT ... WHERE NOT EXISTS, entao repetir a carga nao duplica
-- nem restaura o que o usuario alterou.

-- Candidatos ja incluidos, com avaliacao aprovada e consentimento, como no fluxo real de recrutamento
INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, contato, usuario_id, situacao)
SELECT CAST(v.id AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID), v.codigo, v.nome,
       DATE '1990-01-01', NULL, NULL, 'INCLUIDO'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000002011', 'CAND-004', 'Pessoa Lambda (Ficticia)'),
    ('d6300000-0000-4000-8000-000000002012', 'CAND-005', 'Pessoa Mi (Ficticia)')
) AS v(id, codigo, nome)
WHERE EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.id AS UUID));

INSERT INTO avaliacoes_elegibilidade (id, candidato_id, data_avaliacao, avaliador_id, resultado, pontuacao, vigente)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '40' DAY + INTERVAL '10' HOUR,
       u.id, 'APROVADO', 1, TRUE
FROM (VALUES
    ('d6300000-0000-4000-8000-000000002051', 'd6300000-0000-4000-8000-000000002011'),
    ('d6300000-0000-4000-8000-000000002052', 'd6300000-0000-4000-8000-000000002012')
) AS v(id, candidato)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.candidato AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.id AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.candidato_id = CAST(v.candidato AS UUID));

INSERT INTO respostas_avaliacao (id, avaliacao_id, criterio_id, aplica, observacao)
SELECT CAST(v.id AS UUID), CAST(v.avaliacao AS UUID), CAST('d6300000-0000-4000-8000-000000001902' AS UUID), TRUE,
       'Resposta ficticia para demonstracao.'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000002061', 'd6300000-0000-4000-8000-000000002051'),
    ('d6300000-0000-4000-8000-000000002062', 'd6300000-0000-4000-8000-000000002052')
) AS v(id, avaliacao)
WHERE EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a WHERE a.id = CAST(v.avaliacao AS UUID))
  AND EXISTS (SELECT 1 FROM criterios c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001902' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM respostas_avaliacao r WHERE r.id = CAST(v.id AS UUID));

INSERT INTO consentimentos (id, candidato_id, data_hora, responsavel_id, versao_termo)
SELECT CAST(v.id AS UUID), CAST(v.candidato AS UUID), CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '40' DAY + INTERVAL '11' HOUR,
       u.id, 'TCLE-SIMULADO v1'
FROM (VALUES
    ('d6300000-0000-4000-8000-000000002071', 'd6300000-0000-4000-8000-000000002011'),
    ('d6300000-0000-4000-8000-000000002072', 'd6300000-0000-4000-8000-000000002012')
) AS v(id, candidato)
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST(v.candidato AS UUID))
  AND NOT EXISTS (SELECT 1 FROM consentimentos c WHERE c.candidato_id = CAST(v.candidato AS UUID));

-- Participacoes: a data de inclusao e o dia 0 de cada participante (P4 em D-35, P5 em D-10)
INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000002021' AS UUID), CAST('d6300000-0000-4000-8000-000000002011' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '35' DAY AS DATE), u.id
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002011' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000002011' AS UUID));

INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id)
SELECT CAST('d6300000-0000-4000-8000-000000002022' AS UUID), CAST('d6300000-0000-4000-8000-000000002012' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '10' DAY AS DATE), u.id
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002012' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000002012' AS UUID));

-- Visitas agendadas do P4: uma em cada desfecho (realizada, ausente, cancelada)
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002081' AS UUID), CAST('d6300000-0000-4000-8000-000000002021' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001931' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '35' DAY + INTERVAL '9' HOUR, 'REALIZADA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '35' DAY + INTERVAL '9' HOUR + INTERVAL '10' MINUTE, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002082' AS UUID), CAST('d6300000-0000-4000-8000-000000002021' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001932' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '4' DAY + INTERVAL '10' HOUR, 'AUSENTE',
       NULL, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002083' AS UUID), CAST('d6300000-0000-4000-8000-000000002021' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001933' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '25' DAY + INTERVAL '10' HOUR, 'CANCELADA',
       NULL, 'Participante em viagem', NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001933' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002021' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001933' AS UUID));

-- Visitas agendadas do P5: Triagem cancelada e Retorno 30 ainda agendado (D+20, dentro da janela)
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002084' AS UUID), CAST('d6300000-0000-4000-8000-000000002022' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001931' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '8' DAY + INTERVAL '9' HOUR, 'CANCELADA',
       NULL, 'Consulta remarcada pelo centro', NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002022' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002022' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002085' AS UUID), CAST('d6300000-0000-4000-8000-000000002022' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001932' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '20' DAY + INTERVAL '9' HOUR + INTERVAL '30' MINUTE, 'AGENDADA',
       NULL, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002022' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002022' AS UUID)
                  AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID));
