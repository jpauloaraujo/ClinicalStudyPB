-- T90 (Sprint 3, RF14) - Resultados ficticios das avaliacoes no estudo "Demo - Acompanhamento de participantes".
-- Perfil demo. Somente dados ficticios: nenhum dado real de pessoa.
--   P1 (CAND-001): "Aferir pressao arterial" da Triagem REALIZADA = 120/80 mmHg.
--   P4 (CAND-004): "Aferir pressao arterial" da Triagem REALIZADA = 130/85 mmHg.
-- As demais avaliacoes ficam pendentes ("Coletar sangue" e EXAME_LABORATORIAL e nunca recebe resultado).
-- As visitas sao localizadas por juncao (participante + visita prevista), entao o insert do P4 so faz algo quando o
-- script do PBI-20 (R__demo_20) ja tiver criado o P4 e a Triagem dele; sem isso, nao insere nada e nao falha.
-- Cada INSERT usa NOT EXISTS (visita agendada + atividade): uma nova execucao nao duplica nem desfaz edicoes.
-- Datas relativas a carga: o resultado e registrado 1 hora depois da realizacao da visita (que e relativa a CURRENT_DATE).
-- Autor: demo.pesquisador@clinicalstudy.test. Ids fixos: d6300000-0000-4000-8000-0000000021NN.

-- P1: Triagem realizada, pressao arterial 120/80 mmHg
INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao,
                                  registrado_por_id, registrado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002101' AS UUID), va.id, a.id, '120/80', 'mmHg', NULL,
       u.id, va.data_hora_realizada + INTERVAL '1' HOUR, u.id, va.data_hora_realizada + INTERVAL '1' HOUR
FROM visitas_agendadas va
JOIN participacoes p ON p.id = va.participacao_id
JOIN candidatos c ON c.id = p.candidato_id AND c.codigo = 'CAND-001'
JOIN estudos e ON e.id = p.estudo_id AND e.titulo = 'Demo - Acompanhamento de participantes'
JOIN visitas_planejadas vp ON vp.id = va.visita_planejada_id AND vp.nome = 'Triagem'
JOIN acoes a ON a.visita_id = vp.id AND a.nome = 'Aferir pressão arterial' AND a.categoria = 'AVALIACAO_CLINICA'
JOIN usuarios u ON u.email = 'demo.pesquisador@clinicalstudy.test'
WHERE va.situacao = 'REALIZADA'
  AND NOT EXISTS (SELECT 1 FROM resultados_atividade r WHERE r.visita_agendada_id = va.id AND r.acao_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM resultados_atividade r
                  WHERE r.id = CAST('d6300000-0000-4000-8000-000000002101' AS UUID));

-- P4: Triagem realizada, pressao arterial 130/85 mmHg
INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao,
                                  registrado_por_id, registrado_em, alterado_por_id, alterado_em)
SELECT CAST('d6300000-0000-4000-8000-000000002102' AS UUID), va.id, a.id, '130/85', 'mmHg', NULL,
       u.id, va.data_hora_realizada + INTERVAL '1' HOUR, u.id, va.data_hora_realizada + INTERVAL '1' HOUR
FROM visitas_agendadas va
JOIN participacoes p ON p.id = va.participacao_id
JOIN candidatos c ON c.id = p.candidato_id AND c.codigo = 'CAND-004'
JOIN estudos e ON e.id = p.estudo_id AND e.titulo = 'Demo - Acompanhamento de participantes'
JOIN visitas_planejadas vp ON vp.id = va.visita_planejada_id AND vp.nome = 'Triagem'
JOIN acoes a ON a.visita_id = vp.id AND a.nome = 'Aferir pressão arterial' AND a.categoria = 'AVALIACAO_CLINICA'
JOIN usuarios u ON u.email = 'demo.pesquisador@clinicalstudy.test'
WHERE va.situacao = 'REALIZADA'
  AND NOT EXISTS (SELECT 1 FROM resultados_atividade r WHERE r.visita_agendada_id = va.id AND r.acao_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM resultados_atividade r
                  WHERE r.id = CAST('d6300000-0000-4000-8000-000000002102' AS UUID));
