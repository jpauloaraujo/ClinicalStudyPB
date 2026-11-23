-- T109/T111 (Sprint 3, RF17) - Vincula as contas de demonstracao do participante a candidatos ja incluidos, para o app
-- mostrar "Meus estudos" na demonstracao. Somente dados ficticios. Perfil demo.
-- Roda depois dos scripts R__demo_19 e R__demo_20 (ordem alfabetica da descricao: demo 19 < demo 20 < demo 24), que
-- criam o P1 e o P5 do estudo "Demo - Acompanhamento de participantes".
--   participante@clinicalstudy.test       (seed V2) -> P1 = CAND-001 "Pessoa Teta (Ficticia)" (T111): tem visita
--                                                      realizada com resultado e visita agendada, para o PBI-25 e o E2E mobile
--   demo.participante@clinicalstudy.test  (T63)     -> P5 = CAND-005 "Pessoa Mi (Ficticia)" (T109)
-- Desvio do guia da T111 (RQ-T111-13): o guia manda ligar a conta demo ao P6 (retirado), mas o P6 so passa a existir na
-- T105 (dados da retirada). Ate la a conta demo continua no P5; a T105 move o vinculo para o P6 (primeiro anulando o do P5).
-- Como o seed V2 deixa de ser a conta "sem vinculo" no perfil demo, o aviso "Voce ainda nao esta incluido em nenhum
-- estudo." passa a ser demonstrado com uma conta nova criada pelo administrador (fluxo real do RF01).
-- Idempotente: so vincula se o candidato existe, ainda nao tem conta e a conta ainda nao esta vinculada a outro
-- candidato (relacao um para um, regra 7, garantida pelo indice uk_candidatos_usuario). Nao troca vinculos feitos pela API.
UPDATE candidatos
SET usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'participante@clinicalstudy.test')
WHERE id = CAST('d6300000-0000-4000-8000-000000001911' AS UUID)
  AND usuario_id IS NULL
  AND EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'participante@clinicalstudy.test')
  AND NOT EXISTS (SELECT 1 FROM candidatos c
                  WHERE c.usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'participante@clinicalstudy.test'));

UPDATE candidatos
SET usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
WHERE id = CAST('d6300000-0000-4000-8000-000000002012' AS UUID)
  AND usuario_id IS NULL
  AND EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
  AND NOT EXISTS (SELECT 1 FROM candidatos c
                  WHERE c.usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test'));
