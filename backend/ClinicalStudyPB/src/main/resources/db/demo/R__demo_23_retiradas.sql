-- T105 (Sprint 3, RF16) - Participante P6 retirado com cancelamento em cascata de visita futura. Somente dados ficticios.
-- Perfil demo. Completa o estudo "Demo - Acompanhamento de participantes" criado pelo R__demo_19_visitas_agendadas.sql
-- (T74), que roda antes (ordem alfabetica da descricao: demo 19 < demo 20 < demo 23 < demo 24). Sem aquele estudo este
-- script nao faz nada. O estudo de recrutamento da T63 (...000000000102) e os participantes P1 a P5 NAO sao alterados.
--
-- D = data da carga. Estado equivalente ao produzido pelo RetiradaService (T102):
--   P6 CAND-006 "Pessoa Ni (Ficticia)" incluido em D-15, situacao RETIRADO:
--      retirada em D-2, motivo "Mudança de cidade", solicitante PARTICIPANTE, registrada pelo coordenador demo;
--      Triagem REALIZADA (D-15 09:00, realizada 09:10, preservada);
--      Retorno 30 dias CANCELADA (agendada para D+15 09:30, dentro da janela D+10 a D+20, motivo "retirada do estudo");
--      Retorno 60 dias sem agendamento.
-- Com P1 a P6 o estudo (limite 6) fica com 6 de 6 vagas ocupadas: a retirada nao libera a vaga.
-- Datas relativas a CURRENT_DATE; ids fixos d6300000-0000-4000-8000-0000000023NN; INSERT ... WHERE NOT EXISTS, entao
-- repetir a carga nao duplica nem restaura o que o usuario alterou.

-- Candidato P6 ja incluido, com avaliacao aprovada e consentimento, como no fluxo real de recrutamento
INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, contato, usuario_id, situacao)
SELECT CAST('d6300000-0000-4000-8000-000000002311' AS UUID), CAST('d6300000-0000-4000-8000-000000001901' AS UUID),
       'CAND-006', 'Pessoa Ni (Ficticia)', DATE '1990-01-01', NULL, NULL, 'INCLUIDO'
WHERE EXISTS (SELECT 1 FROM estudos e WHERE e.id = CAST('d6300000-0000-4000-8000-000000001901' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID));

INSERT INTO avaliacoes_elegibilidade (id, candidato_id, data_avaliacao, avaliador_id, resultado, pontuacao, vigente)
SELECT CAST('d6300000-0000-4000-8000-000000002351' AS UUID), CAST('d6300000-0000-4000-8000-000000002311' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '20' DAY + INTERVAL '10' HOUR, u.id, 'APROVADO', 1, TRUE
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a
                  WHERE a.id = CAST('d6300000-0000-4000-8000-000000002351' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a
                  WHERE a.candidato_id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID));

INSERT INTO respostas_avaliacao (id, avaliacao_id, criterio_id, aplica, observacao)
SELECT CAST('d6300000-0000-4000-8000-000000002361' AS UUID), CAST('d6300000-0000-4000-8000-000000002351' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001902' AS UUID), TRUE, 'Resposta ficticia para demonstracao.'
WHERE EXISTS (SELECT 1 FROM avaliacoes_elegibilidade a
              WHERE a.id = CAST('d6300000-0000-4000-8000-000000002351' AS UUID))
  AND EXISTS (SELECT 1 FROM criterios c WHERE c.id = CAST('d6300000-0000-4000-8000-000000001902' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM respostas_avaliacao r
                  WHERE r.id = CAST('d6300000-0000-4000-8000-000000002361' AS UUID));

INSERT INTO consentimentos (id, candidato_id, data_hora, responsavel_id, versao_termo)
SELECT CAST('d6300000-0000-4000-8000-000000002371' AS UUID), CAST('d6300000-0000-4000-8000-000000002311' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '20' DAY + INTERVAL '11' HOUR, u.id, 'TCLE-SIMULADO v1'
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM consentimentos c
                  WHERE c.candidato_id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID));

-- Participacao P6: incluido em D-15, ja com situacao RETIRADO (6 de 6 vagas ocupadas)
INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id, situacao)
SELECT CAST('d6300000-0000-4000-8000-000000002321' AS UUID), CAST('d6300000-0000-4000-8000-000000002311' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001901' AS UUID), CAST(CURRENT_DATE - INTERVAL '15' DAY AS DATE),
       u.id, 'RETIRADO'
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM candidatos c WHERE c.id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM participacoes p
                  WHERE p.candidato_id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID));

-- Registro formal da retirada em D-2 (motivo "Mudança de cidade", solicitante PARTICIPANTE)
INSERT INTO retiradas_participacao (id, participacao_id, data_retirada, motivo, solicitante,
                                    registrado_por_id, registrado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002391' AS UUID), CAST('d6300000-0000-4000-8000-000000002321' AS UUID),
       CAST(CURRENT_DATE - INTERVAL '2' DAY AS DATE), 'Mudança de cidade', 'PARTICIPANTE',
       u.id, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '2' DAY + INTERVAL '14' HOUR
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM retiradas_participacao r
                  WHERE r.participacao_id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM retiradas_participacao r
                  WHERE r.id = CAST('d6300000-0000-4000-8000-000000002391' AS UUID));

-- Visitas do P6: Triagem REALIZADA em D-15 (preservada) e Retorno 30 CANCELADA em D+15 com motivo "retirada do estudo".
-- Retorno 60 fica sem agendamento.
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002381' AS UUID), CAST('d6300000-0000-4000-8000-000000002321' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001931' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '15' DAY + INTERVAL '9' HOUR, 'REALIZADA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '15' DAY + INTERVAL '9' HOUR + INTERVAL '10' MINUTE,
       NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v
                  WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID)
                    AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001931' AS UUID));

INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002382' AS UUID), CAST('d6300000-0000-4000-8000-000000002321' AS UUID),
       CAST('d6300000-0000-4000-8000-000000001932' AS UUID),
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '15' DAY + INTERVAL '9' HOUR + INTERVAL '30' MINUTE, 'CANCELADA',
       NULL, 'retirada do estudo', NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM usuarios u
WHERE u.email = 'demo.coordenador@clinicalstudy.test'
  AND EXISTS (SELECT 1 FROM participacoes p WHERE p.id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID))
  AND EXISTS (SELECT 1 FROM visitas_planejadas vp WHERE vp.id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID))
  AND NOT EXISTS (SELECT 1 FROM visitas_agendadas v
                  WHERE v.participacao_id = CAST('d6300000-0000-4000-8000-000000002321' AS UUID)
                    AND v.visita_planejada_id = CAST('d6300000-0000-4000-8000-000000001932' AS UUID));
