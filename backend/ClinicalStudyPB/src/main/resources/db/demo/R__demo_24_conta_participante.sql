-- T109/T111/T105 (Sprint 3, RF17/RF16) - Vincula as contas de demonstracao do participante a candidatos ja incluidos,
-- para o app mostrar "Meus estudos" na demonstracao. Somente dados ficticios. Perfil demo.
-- Roda depois dos scripts R__demo_19, R__demo_20 e R__demo_23 (ordem alfabetica da descricao: demo 19 < demo 20 < demo 23 < demo 24),
-- que criam o P1 e o P6 do estudo "Demo - Acompanhamento de participantes".
--   participante@clinicalstudy.test       (seed V2) -> P1 = CAND-001 "Pessoa Teta (Ficticia)" (T111): tem visita
--                                                      realizada com resultado e visita agendada, para o PBI-25 e o E2E mobile
--   demo.participante@clinicalstudy.test  (T63)     -> P6 = CAND-006 "Pessoa Ni (Ficticia)" (T105/RQ-T111-13):
--                                                      participante retirado com visita cancelada por "retirada do estudo"
-- Em bancos que ja tinham rodado a versao anterior da T111 (conta demo temporariamente no P5), o primeiro UPDATE limpa
-- o vinculo do P5 somente quando o P6 existe sem conta, para entao vincula-la ao P6 de forma idempotente.
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

-- Transicao de bancos demo anteriores a T105: libera demo.participante do P5 quando o P6 existe e ainda esta sem conta
UPDATE candidatos
SET usuario_id = NULL
WHERE id = CAST('d6300000-0000-4000-8000-000000002012' AS UUID)
  AND usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
  AND EXISTS (SELECT 1 FROM candidatos p6
              WHERE p6.id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID)
                AND p6.usuario_id IS NULL);

UPDATE candidatos
SET usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
WHERE id = CAST('d6300000-0000-4000-8000-000000002311' AS UUID)
  AND usuario_id IS NULL
  AND EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
  AND NOT EXISTS (SELECT 1 FROM candidatos c
                  WHERE c.usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test'));
