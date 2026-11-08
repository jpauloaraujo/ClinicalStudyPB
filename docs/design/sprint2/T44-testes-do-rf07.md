# T44 · Testes do RF07

> **Sprint 2** · PBI-10 · RF07 Estados do estudo  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Qua 04/11  
> **Depende de:** T41, T42  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cobrir a regra de estados em três níveis: regra pura, serviço com Mockito e endpoint com tokens reais, incluindo as 16 combinações de origem e destino.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Resultado e cobertura registrados em `docs/sprint2/T44 e T47 — resultado dos testes.md`.
- Quem testa não é quem implementou: I2 revisa a lógica de I3 e o endpoint de I4.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MaquinaEstadosEstudoTest.java` | Existente | Parametrizado: 16 combinações (3 válidas), pré-condições, ordem das verificações |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoServiceTest.java` | Existente | Mockito: histórico, recusas sem efeito, estudo e usuário inexistentes |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoCombinacoesServiceTest.java` | Existente | Mockito: as 16 combinações no serviço (3 gravam histórico, 13 recusam) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoIntegrationTest.java` | Existente | H2 + Flyway: ciclo completo e mapeamento da entidade |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoEndpointTest.java` | Existente | MockMvc: 200, 409 por regra, 403, 401, 404, 400 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoRf07Test.java` | Existente | MockMvc: as 16 combinações pelo HTTP, 403 de coordenador, participante e administrador, e a brecha do `PUT`/`POST` |

## 4. O que fazer em cada arquivo

### Matriz coberta

- 16 combinações: 3 → 200 e 13 → 409 `TRANSICAO_ESTADO_INVALIDA`, com o estado preservado.
- Duas pré-condições (critério e visita) e a prioridade do critério.
- Histórico: usuário, estados e data gravados; nada gravado quando recusa.
- Perfis: só o pesquisador avança (403 para os outros três); sem token, 401; estudo inexistente, 404; destino ausente ou inválido, 400.

### Como estender

- Nova regra de transição = novo caso na matriz de `MaquinaEstadosEstudoTest` e um 409 no endpoint com o código novo.

## 6. Testes

- Resultado de 02/11/2026: `MaquinaEstadosEstudo` com 100% de linhas e de ramos; `EstudoTransicaoService` com 100% das linhas.
- A parte "sobre PostgreSQL" roda no job `backend-postgres` do CI.

## 7. Critérios de aceite

- [ ] Todos os testes passam.
- [ ] Cobertura da classe de domínio registrada para o relatório da Sprint.

## 8. Cuidados e fora de escopo

- Preparar o estudo pelo endpoint de transição, não por `PUT`.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
