-- T95 (Sprint 3, RF15) - Historico completo do participante P4 (CAND-004, "Pessoa Lambda (Ficticia)"), somente dados ficticios.
-- Perfil demo. Completa as tres visitas previstas do P4 no estudo "Demo - Acompanhamento de participantes":
--   Triagem REALIZADA, Retorno 30 dias AUSENTE e Retorno 60 dias CANCELADA com motivo.
-- O P3 (CAND-003) nao recebe nenhuma visita agendada: serve para mostrar o historico sem agendamentos.
-- O estudo e os participantes sao criados pelos scripts do PBI-19 e do PBI-20; sem eles este script nao faz nada.
-- Cada INSERT usa NOT EXISTS (participacao + visita prevista), entao uma nova execucao nao altera o que o usuario editou.
-- Datas relativas a CURRENT_DATE no momento da carga (D = hoje). Inclusao do P4 em D-35: Triagem em D-35, Retorno 30 em D-5
-- (agendada em D-4, dentro da tolerancia de 5 dias) e Retorno 60 em D+25.
-- Ids fixos: d6300000-0000-4000-8000-0000000022NN.

-- Triagem: realizada no dia da inclusao (agendada 09:00, realizada 09:10)
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002201' AS UUID), p.id, vp.id,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '35' DAY + INTERVAL '9' HOUR,
       'REALIZADA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '35' DAY + INTERVAL '9' HOUR + INTERVAL '10' MINUTE,
       NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM participacoes p
JOIN candidatos c ON c.id = p.candidato_id AND c.codigo = 'CAND-004'
JOIN estudos e ON e.id = p.estudo_id AND e.titulo = 'Demo - Acompanhamento de participantes'
JOIN visitas_planejadas vp ON vp.estudo_id = e.id AND vp.nome = 'Triagem'
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM visitas_agendadas va WHERE va.participacao_id = p.id AND va.visita_planejada_id = vp.id);

-- Retorno 30 dias: o participante faltou
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002202' AS UUID), p.id, vp.id,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '4' DAY + INTERVAL '10' HOUR,
       'AUSENTE',
       NULL, NULL, NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM participacoes p
JOIN candidatos c ON c.id = p.candidato_id AND c.codigo = 'CAND-004'
JOIN estudos e ON e.id = p.estudo_id AND e.titulo = 'Demo - Acompanhamento de participantes'
JOIN visitas_planejadas vp ON vp.estudo_id = e.id AND vp.nome = 'Retorno 30 dias'
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM visitas_agendadas va WHERE va.participacao_id = p.id AND va.visita_planejada_id = vp.id);

-- Retorno 60 dias: cancelada, com motivo
INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, situacao,
                               data_hora_realizada, motivo, observacao, versao,
                               criado_por_id, criado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002203' AS UUID), p.id, vp.id,
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '25' DAY + INTERVAL '10' HOUR,
       'CANCELADA',
       NULL, 'Participante em viagem', NULL, 0, u.id, CURRENT_TIMESTAMP, u.id, CURRENT_TIMESTAMP
FROM participacoes p
JOIN candidatos c ON c.id = p.candidato_id AND c.codigo = 'CAND-004'
JOIN estudos e ON e.id = p.estudo_id AND e.titulo = 'Demo - Acompanhamento de participantes'
JOIN visitas_planejadas vp ON vp.estudo_id = e.id AND vp.nome = 'Retorno 60 dias'
JOIN usuarios u ON u.email = 'demo.coordenador@clinicalstudy.test'
WHERE NOT EXISTS (SELECT 1 FROM visitas_agendadas va WHERE va.participacao_id = p.id AND va.visita_planejada_id = vp.id);
