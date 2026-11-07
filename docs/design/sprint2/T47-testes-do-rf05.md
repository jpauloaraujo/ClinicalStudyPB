# T47 · Testes do RF05

> **Sprint 2** · PBI-11 · RF05 Critérios de inclusão e exclusão  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Qua 04/11  
> **Depende de:** T45, T46  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cobrir os critérios em serviço, DTO, endpoint e componente, com um olhar independente de quem implementou.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Resultado e cobertura registrados em `docs/sprint2/T44 e T47 — resultado dos testes.md`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CriterioServiceTest.java` | Existente | Mockito (13): duplicidade, estado, tipo, não encontrado, em uso |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CriterioRequestValidacaoTest.java` | Existente | Validator sem Spring (19): descrição 4/5 e 500/501, título, tipo e peso |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CriterioEndpointTest.java` | Existente | MockMvc (19): 201, 400 por campo, 401, 403, 404, 409 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CriterioDescricaoRepetidaTest.java` | Existente | Regressão da descrição repetida (T45) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCriterios.test.tsx` | Existente | 20 testes de componente, incluindo lista vazia e erro do servidor |

## 4. O que fazer em cada arquivo

### Observações

- O 409 `CRITERIO_EM_USO` não é alcançável pelo HTTP (o estado bloqueia antes) e fica coberto só no serviço.
- O 400 traz um campo por resposta, no formato `campo: mensagem`.

## 6. Testes

- `CriterioService`: 100% das linhas e 92% dos ramos.

## 7. Critérios de aceite

- [ ] Descrição curta, longa e repetida, tipo ausente e estudo fora de planejamento têm teste.
- [ ] 201, 400, 403, 404 e 409 nos endpoints.

## 8. Cuidados e fora de escopo

- Os 409 conferem o `codigo`, não só o status.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
