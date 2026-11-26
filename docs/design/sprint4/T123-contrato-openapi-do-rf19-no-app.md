# T123 · Contrato OpenAPI do RF19 no app

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 2h · **Prazo:** Ter 24/11  
> **Depende de:** T122  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Publicar o contrato das três operações do RF19 no app, os campos novos da visita do participante e os testes de contrato da Sprint 4.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/api/openapi-sprint3.yaml` define `MinhaVisitaResponse`; `ContratoOpenApiSprint3Test` compara os campos do schema com os do record e usa o regex `me/.*`.
- `npm run api:tipos` (web e app) gera `schema.d.ts` e `schema-sprint3.d.ts` com `openapi-typescript@7.13.0`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint4.yaml` | Criar | Contrato da Sprint 4 (3 operações nesta task) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Campos novos de `MinhaVisitaResponse` (entram junto com o record, na T125) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Criar | Contrato × API |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Restringir o regex de `me/` |
| `mobile/ClinicalStudyPB/package.json` | Alterar | `api:tipos` gera também `schema-sprint4.d.ts` |
| `web/ClinicalStudyPB/package.json` | Alterar | Idem |
| `mobile/ClinicalStudyPB/src/api/schema-sprint4.d.ts` | Criar | Tipos gerados |
| `web/ClinicalStudyPB/src/api/schema-sprint4.d.ts` | Criar | Tipos gerados |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T123-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T123-01** Criar `openapi-sprint4.yaml` (OpenAPI 3, `bearerAuth`, schema `ErroResponse` igual ao da Sprint 3) só com os caminhos desta fatia: operações 1 a 3 da seção 2.
- **RQ-T123-02** `POST /api/me/visitas/{visitaAgendadaId}/confirmacao`: sem corpo; `200 MinhaVisitaResponse`; `401`, `403`, `404`, `409`.
- **RQ-T123-03** `POST /api/me/visitas/{visitaAgendadaId}/pedidos-reagendamento`: corpo `PedidoReagendamentoRequest`; `201 MinhaSolicitacaoResponse`; `400`, `401`, `403`, `404`, `409`.
- **RQ-T123-04** `GET /api/me/pedidos`: `200` lista de `MinhaSolicitacaoResponse`, mais recentes primeiro; `401`, `403`. Nenhuma operação recebe id de participante.
- **RQ-T123-05** Declarar `PedidoReagendamentoRequest` e `MinhaSolicitacaoResponse` (seção 3.1), com exemplos fictícios, `required` e limites (`minLength`/`maxLength`).
- **RQ-T123-06** Documentar em cada operação os códigos das regras 1 e 2 (seção 4), na ordem em que são verificados, com um exemplo de corpo de erro.
- **RQ-T123-07** Descrever os campos novos de `MinhaVisitaResponse` (seção 3.2) no YAML da Sprint 3, na mesma entrega que altera o record (T125), para o teste da Sprint 3 continuar verde.
- **RQ-T123-08** Trocar em `ContratoOpenApiSprint3Test` o trecho `me/.*` por `me/(estudos(/\{estudoId})?|visitas/(proximas|historico))`.
- **RQ-T123-09** Criar `ContratoOpenApiSprint4Test` com o método e o regex da seção 2: contrato com 3 operações; todo caminho da Sprint 4 publicado pela API está no YAML; toda operação declara `401` e `403`, e as de escrita, `400` ou `409` conforme a regra. O teste "contrato não promete endpoint inexistente" nasce com `@Disabled("T125")`.
- **RQ-T123-10** Atualizar o script `api:tipos` dos dois projetos, gerar os arquivos e conferir `npm run typecheck` (app) e `npm run build` (web).

## 5. Contrato, regras e códigos de erro

Operações 1 a 3, DTOs das seções 3.1 e 3.2 e códigos da seção 4 das convenções.

## 6. Testes

`ContratoOpenApiSprint4Test` e `ContratoOpenApiSprint3Test` verdes; `ContratoOpenApiTest` (Sprint 2) inalterado.

## 7. Critérios de aceite

- [ ] O YAML da Sprint 4 descreve as três operações com os códigos de erro.
- [ ] Os tipos gerados compilam na web e no app.
- [ ] Os testes de contrato das três sprints passam.

## 8. Cuidados e fora de escopo

- Acrescentar só os caminhos desta fatia; os PBIs seguintes acrescentam os deles.
- Não editar `schema-*.d.ts` à mão.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T123 · Contrato OpenAPI do RF19 no app.** Primeiro descreva, sob /me, a confirmação de presença de uma visita, o pedido de reagendamento de uma visita e a listagem dos pedidos do participante com situação e resposta. Depois acrescente à visita do RF18 os campos de confirmação e de pedido pendente, para o app mostrar os selos sem outra chamada. Por fim, escreva os códigos de erro das regras 1 e 2 em docs/api/openapi-sprint4.yaml, só com os caminhos desta fatia.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
