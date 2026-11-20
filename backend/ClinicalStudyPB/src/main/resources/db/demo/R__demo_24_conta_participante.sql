-- T109 (Sprint 3, RF17) - Vincula a conta de demonstracao do participante a um candidato ja incluido, para o app
-- mostrar "Meus estudos" na demonstracao. Somente dados ficticios. Perfil demo.
-- Roda depois do R__demo_20_desfechos.sql (ordem alfabetica da descricao: demo 20 < demo 24), que cria o P5.
--   demo.participante@clinicalstudy.test  -> CAND-005 "Pessoa Mi (Ficticia)" do estudo "Demo - Acompanhamento de participantes"
--   participante@clinicalstudy.test (seed V2) continua SEM vinculo: serve para mostrar o aviso
--   "Voce ainda nao esta incluido em nenhum estudo." (T110).
-- Idempotente: so vincula se o candidato existe, ainda nao tem conta e a conta ainda nao esta vinculada a outro
-- candidato (a relacao e um para um, regra 7). A T111 passa a gerenciar este vinculo pela API.
UPDATE candidatos
SET usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
WHERE id = CAST('d6300000-0000-4000-8000-000000002012' AS UUID)
  AND usuario_id IS NULL
  AND EXISTS (SELECT 1 FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test')
  AND NOT EXISTS (SELECT 1 FROM candidatos c
                  WHERE c.usuario_id = (SELECT u.id FROM usuarios u WHERE u.email = 'demo.participante@clinicalstudy.test'));
