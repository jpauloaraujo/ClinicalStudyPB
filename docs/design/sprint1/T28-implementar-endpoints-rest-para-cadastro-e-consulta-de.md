# T28 · Implementar endpoints REST para cadastro e consulta de estudos

> **Sprint 1** · PBI-07 · RF04 Cadastro de estudos clínicos  
> **Responsável:** I3 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T27, T24  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Expor o CRUD de estudos em `/api/estudos` com a autorização correta e a documentação no Swagger.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/EstudoController.java` | Existente | Cinco operações com `@PreAuthorize` e anotações do springdoc |

## 4. O que fazer em cada arquivo

### Operações

- `GET /api/estudos` e `GET /api/estudos/{id}`: pesquisador e coordenador.
- `POST` (201 com `Location`), `PUT /{id}` e `DELETE /{id}` (204): somente pesquisador.

### Observação

- O controller devolve a entidade `Estudo` (com o responsável sem senha). Os demais controllers da Sprint 2 usam DTOs próprios; um endpoint de estudo novo deve preferir DTO.

## 5. Contrato, regras e códigos de erro

- Códigos: 200/201/204, 400, 401, 403, 404 e 409.

## 6. Testes

- `EstudoIntegrationTest` (8 testes).

## 7. Critérios de aceite

- [ ] Cada operação responde conforme a matriz de permissões.
- [ ] Aparece no Swagger UI em `/swagger-ui.html`.

## 8. Cuidados e fora de escopo

- O estado do estudo não muda por estes endpoints: use `POST /api/estudos/{id}/transicoes`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
