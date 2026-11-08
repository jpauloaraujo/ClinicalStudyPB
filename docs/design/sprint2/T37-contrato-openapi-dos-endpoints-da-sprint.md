# T37 · Contrato OpenAPI dos endpoints da Sprint

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Qui 29/10  
> **Depende de:** T36  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter um contrato único dos endpoints da Sprint 2, escrito antes do código, para web e backend andarem em paralelo e os tipos TypeScript saírem dele.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O contrato oficial é `docs/api/openapi-sprint2.yaml` (OpenAPI 3.0.3, 24 operações). O `docs/sprint2/OpanAPI.yaml` antigo descreve caminhos que a API não tem e não é mais usado.
- Os caminhos reais estão em português (`/api/estudos/...`), diferentes dos do plano (`/studies/...`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint2.yaml` | Existente | Contrato: paths, schemas, exemplos fictícios e respostas de erro |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiTest.java` | Existente | Compara o contrato com `/v3/api-docs` nos dois sentidos e exige 24 operações |
| `web/ClinicalStudyPB/src/api/schema.d.ts` | Existente (gerado) | Tipos gerados do YAML por `npm run api:tipos` |
| `web/ClinicalStudyPB/src/mocks/handlers.ts` | Existente | Mocks de cada operação do contrato |

## 4. O que fazer em cada arquivo

### As 24 operações

- Estado: `POST /api/estudos/{estudoId}/transicoes` (avancarEstado); `GET .../historico-status`.
- Critérios: `GET`, `POST` em `/api/estudos/{estudoId}/criterios`; `PUT`, `DELETE` em `.../criterios/{id}`.
- Visitas: `GET`, `POST` em `/api/estudos/{estudoId}/visitas`; `PUT`, `DELETE` em `.../visitas/{id}`; ações: `POST /api/visitas/{visitaId}/acoes`, `PUT` e `DELETE .../acoes/{id}`.
- Candidatos: `GET`, `POST` em `/api/estudos/{estudoId}/candidatos`; `GET`, `PUT /api/candidatos/{id}`.
- Elegibilidade: `GET`, `POST /api/candidatos/{candidatoId}/avaliacoes`. Consentimento: `GET`, `POST /api/candidatos/{candidatoId}/consentimento`.
- Participantes: `GET`, `POST /api/estudos/{estudoId}/participantes`; `GET .../participantes/vagas`.

### Formato de erro

- `{"erro": "mensagem", "codigo": "REGRA"}`: 400 validação, 401 sem token, 403 perfil, 404 inexistente, 409 regra violada (sempre com `codigo`).
- O formato agora traz também `status` e a lista de erros por campo (`errosCampo`), conforme o plano; `erro` e `codigo` foram mantidos. Veja o bloco "Formato de erro implementado" abaixo.

### Regra de manutenção

- Divergência encontrada durante a Sprint é corrigida no YAML, no mesmo Pull Request do código.
- Depois: `npm run api:tipos`, ajustar `tipos.ts`, o módulo de API e o mock; rodar `ContratoOpenApiTest`.

### Para uma operação nova

- 1. Descrever no YAML (tag, `operationId`, parâmetros, corpo, respostas, perfis). 2. Implementar o endpoint. 3. Atualizar a contagem esperada no `ContratoOpenApiTest`. 4. Gerar os tipos e criar o handler do mock.

### Formato de erro implementado (vindo da main, unificado em 07/11/2026)

- `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` passou a devolver, em todo erro: `status`, `codigoRegra`, `mensagem` e `errosCampo` (lista de `{campo, erro}`; vazia fora da validação), mantendo os aliases `erro` e `codigo` para os clientes existentes.
- Na validação (400) `errosCampo` traz **todos** os campos inválidos; `erro` continua com o primeiro no formato `campo: mensagem`.
- O contrato (`docs/api/openapi-sprint2.yaml`, schema `Erro`) documenta os campos novos; `src/api/schema.d.ts` foi regenerado; `src/api/erros.ts` lê `errosCampo` e continua lendo `erro`; o mock devolve o mesmo formato de campos.
- Testes: `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PlanejamentoIntegrationTest.validacaoRetornaTodosOsCamposEContratoCompativel` e `web/ClinicalStudyPB/src/api/erros.test.ts`.

## 6. Testes

- `ContratoOpenApiTest` (3 testes): mesmo conjunto de operações no contrato e na API publicada.

## 7. Critérios de aceite

- [ ] I5 gera os tipos TypeScript a partir do arquivo.
- [ ] O teste de contrato passa.
- [ ] Todo erro da API traz `status`; erro de validação traz a lista completa em `erros`; `erro` e `codigo` não mudaram.

## 8. Cuidados e fora de escopo

- Os endpoints de usuários e estudos da Sprint 1 não estão neste contrato (ele cobre a Sprint 2), exceto o que a transição de estado devolve.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
