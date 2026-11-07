# T37 · Contrato OpenAPI dos endpoints da Sprint

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Qui 29/10  
> **Depende de:** T36  
> **Situação:** 🟡 Parcialmente implementada (o documento separa o que existe do que falta)

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
- O plano pede também `status` e a lista de erros por campo, que a API ainda não devolve (hoje: um campo por resposta, no texto `campo: mensagem`). O bloco "Lacuna em relação ao plano" abaixo descreve a mudança compatível que fecha isso.

### Regra de manutenção

- Divergência encontrada durante a Sprint é corrigida no YAML, no mesmo Pull Request do código.
- Depois: `npm run api:tipos`, ajustar `tipos.ts`, o módulo de API e o mock; rodar `ContratoOpenApiTest`.

### Para uma operação nova

- 1. Descrever no YAML (tag, `operationId`, parâmetros, corpo, respostas, perfis). 2. Implementar o endpoint. 3. Atualizar a contagem esperada no `ContratoOpenApiTest`. 4. Gerar os tipos e criar o handler do mock.

### Lacuna em relação ao plano e como fechá-la (formato de erro)

- O plano pede um formato único de erro com **status, código da regra, mensagem e lista de erros por campo**. Hoje a API devolve `{"erro", "codigo"?}`; falta `status` e a lista por campo.
- Mudança compatível (nada que já funciona deixa de funcionar): o corpo passa a ser `{"status": 400, "erro": "campo: mensagem", "codigo": "REGRA"?, "erros": [{"campo": "descricao", "mensagem": "..."}]}`. `erro` e `codigo` continuam como hoje; `erros` é uma lista vazia fora de validação.
- 1. `controller/ApiExceptionHandler.java`: os handlers passam a devolver `Map<String, Object>`/record com `status`; `tratarValidacao` coleta **todos** os `FieldError` em `erros` e mantém o primeiro em `erro`.
- 2. `docs/api/openapi-sprint2.yaml`: no schema `Erro`, acrescentar `status` (inteiro) e `erros` (lista de `{campo, mensagem}`), com exemplo; rodar `npm run api:tipos`.
- 3. Web: `src/api/erros.ts` já lê `erros: [{campo, mensagem}]` e continua lendo `erro`; nada a mudar além de `schema.d.ts`. Mock (`src/mocks/handlers.ts`): incluir `status` e `erros` nos corpos de erro.
- 4. Testes: em `CriterioEndpointTest` (400), conferir `$.status` e `$.erros[0].campo`; um teste de validação com dois campos inválidos deve devolver os dois em `erros`; conferir que o `erro` antigo continua igual (os testes atuais não podem quebrar).

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
