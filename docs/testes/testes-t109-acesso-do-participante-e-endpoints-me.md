# Testes da T109 — Acesso do participante e endpoints /me no backend

| | |
|---|---|
| **Task** | T109 · Acesso do participante e endpoints /me no backend (PBI-24, Sprint 3) |
| **Design da task** | [T109](../design/sprint3/T109-acesso-do-participante-e-endpoints-me-no-backend.md) |
| **Ramo testado** | `feat/task109-acesso-participante-endpoints-me` (sobre `feat/task108-esqueleto-app`) |
| **Data da execução** | 19/11/2026 |
| **Ferramentas** | JUnit 5, Mockito, MockMvc, H2 em modo PostgreSQL (mesmas migrations do Flyway), AssertJ |
| **Comando** | `./mvnw test` em `backend/ClinicalStudyPB` |
| **Decisão de escopo** | Opção **A**: `situacaoParticipacao` sempre `ATIVO` e `retirada` sempre `null` até a T101 a T103 (ver seção 6) |

## 1. Escopo

Os testes desta task são os mínimos pedidos pelo design (seção 6 da T109): teste de unidade do `AcessoParticipante` e teste de fumaça dos endpoints. A bateria completa é da [T113](../design/sprint3/T113-testes-de-backend-e-de-componente-do-app.md).

## 2. Testes escritos

| Arquivo | Casos | Foco | RQ |
|---|---|---|---|
| `AcessoParticipanteTest` (unidade, Mockito) | 8 | Lista da conta, lista vazia sem vínculo, usuário nulo, 404 de estudo e de participação de outra conta, ids nulos sem consulta ao banco | 04, 05, 07 |
| `MeuEstudoResponseTest` (unidade) | 4 | Resumo: até 160 inteiro, 161 cortado em 160 com reticências, sem espaço antes das reticências, nulo | 10 |
| `MeEndpointTest` (fumaça, MockMvc + H2) | 11 | Lista só da conta e ordenada, sem dados de candidato; conta sem vínculo (200 `[]`); `?participanteId` ignorado; detalhe; 404 de outro participante idêntico ao de estudo inexistente; 404 sem vínculo; 403 para ADMIN, PESQUISADOR e COORDENADOR; 401; POST recusado | 01, 05, 09 a 12 |
| `DemoContaParticipanteTest` | 3 | `R__demo_24_conta_participante.sql`: vincula ao P5, é idempotente, não faz nada sem o P5, não vincula a conta do seed V2 | — |
| `ContratoOpenApiSprint3Test` (alterado) | +5 casos, 1 alteração | 12 operações; `/me` existe na API; schemas `MeuEstudoResponse`, `MeuEstudoDetalheResponse` e `RetiradaDoMeuEstudo` com os campos do DTO; enum `SituacaoParticipacao` | 01 a 03 |

## 3. Resultado da primeira rodada (antes de qualquer correção)

`./mvnw test` na suíte inteira do backend: **703 testes, 2 falhas, 0 erros, 0 ignorados.** As duas falhas estão registradas em [registro-de-defeitos](registro-de-defeitos.md) como D-T109-01 e D-T109-02, **antes** de qualquer alteração de código ou de teste.

## 4. Falhas encontradas e correções

### D-T109-01 · `ContratoOpenApiSprint3Test.operacoesDeclaramAsRespostasDeErro`

| Campo | Valor |
|---|---|
| **Mensagem** | `[get /api/me/estudos] Expecting HashSet: ["200", "401", "403"] to contain: ["401", "403", "404", "409"] but could not find the following element(s): ["404", "409"]` |
| **Teste** | `ContratoOpenApiSprint3Test.operacoesDeclaramAsRespostasDeErro:226` (teste **já existente**, da T74) |
| **Causa** | O teste exige 401, 403, 404 e 409 em **toda** operação do contrato. Isso valia para a agenda e o agendamento, onde há id na URL e regras de negócio. `GET /api/me/estudos` não recebe id (não há o que não encontrar) e não tem regra de negócio (não há 409). O contrato declara só o que o endpoint pode devolver; declarar 404 e 409 ali seria documentar respostas impossíveis |
| **De quem é o erro** | Da **premissa do teste**, não do código nem do contrato |
| **Determinístico?** | Sim |
| **Correção prevista** | Manter a exigência de 401 e 403 em toda operação e exigir 404 só nas operações **com parâmetro de caminho** e 409 só fora de `/api/me/**`. Nenhuma asserção foi removida das operações já cobertas |
| **Estado** | Ver o fim desta seção |

### D-T109-02 · `MeEndpointTest.detalheDoEstudoDaConta`

| Campo | Valor |
|---|---|
| **Mensagem** | `JSON path "$.resumo.length()" Expected: a value less than or equal to <160> but: was null` |
| **Teste** | `MeEndpointTest.detalheDoEstudoDaConta:156` (teste **novo**, escrito nesta task) |
| **Causa** | A função `length()` do JsonPath vale para listas e objetos; em um texto devolve `null` neste ambiente. O resumo estava correto (o teste vizinho `endsWith("…")` passou e `MeuEstudoResponseTest` confirma 160 caracteres) |
| **De quem é o erro** | Do **teste novo**; o código está correto |
| **Determinístico?** | Sim |
| **Correção prevista** | Trocar a asserção por `matchesPattern("(?s).{1,160}")` sobre o próprio texto, que verifica o mesmo limite |
| **Estado** | Ver o fim desta seção |

### Estado das correções

| ID | Alteração feita | Teste alterado? | Estado |
|---|---|---|---|
| D-T109-01 | Em `ContratoOpenApiSprint3Test.operacoesDeclaramAsRespostasDeErro`, 401 e 403 seguem exigidos em toda operação; 404 passa a ser exigido em toda operação **exceto** `/api/me/**` sem parâmetro de caminho; 409 passa a ser exigido em toda operação **exceto** `/api/me/**`. Nenhuma operação anterior perdeu cobertura | Sim, o teste existente foi **refinado**, não removido nem desabilitado | Fechado |
| D-T109-02 | Em `MeEndpointTest.detalheDoEstudoDaConta`, `jsonPath("$.resumo.length()")` trocado por `jsonPath("$.resumo").value(matchesPattern("(?s).{1,160}"))` | Sim, teste novo desta task | Fechado |

Nenhum código de produção foi alterado por causa das falhas.

## 5. Resultado final

Segunda rodada, `./mvnw test`: **703 testes, 0 falhas, 0 erros, 0 ignorados.** Dos 703, **31 são novos** (8 + 4 + 11 + 3 do `AcessoParticipanteTest`, `MeuEstudoResponseTest`, `MeEndpointTest` e `DemoContaParticipanteTest`, mais 5 casos no `ContratoOpenApiSprint3Test`); os outros 672 já existiam e continuam passando (RF01 a RF11 e Sprint 3 intactos).

Também conferidos, sem falha:

| Verificação | Resultado |
|---|---|
| `npm run typecheck` em `mobile/ClinicalStudyPB` com os tipos regenerados | Sem erro |
| `npx openapi-typescript@7.13.0 ../../docs/api/openapi-sprint3.yaml -o src/api/schema-sprint3.d.ts` | Gerou `MeuEstudoResponse`, `MeuEstudoDetalheResponse`, `RetiradaDoMeuEstudo` e as operações `meusEstudos` e `meuEstudo` |

### 5.1 Requisitos × testes

| RQ | Atendido? | Teste |
|---|---|---|
| 01 Contrato dos endpoints | Sim | `meDoContratoExistemNaApi`, `contratoTemAsOperacoesEsperadas`, `apiNaoTemEndpointDaSprint3ForaDoContrato` |
| 02 Schemas (motivo da retirada não exposto) | Sim | `schemaMeTemOsCamposDoDto` (3 casos), `situacaoParticipacaoDoContratoIgualAoDoBackend` |
| 03 Contagem e tipos do app | Sim, com 12 (e não 14) | `contratoTemAsOperacoesEsperadas`; ver pendência P1 |
| 04 `AcessoParticipante` e 404 | Sim | `AcessoParticipanteTest`, `estudoDeOutroParticipanteRecebe404` |
| 05 Sem vínculo, lista vazia | Sim | `contaSemVinculoRecebeListaVazia`, `AcessoParticipanteTest` |
| 06 Retiradas na lista | **Parcial** | Nenhuma retirada existe ainda; ver pendência P2 |
| 07 Só recebe `UUID` | Sim | Assinatura do componente; `AcessoParticipanteTest` |
| 08 `Permissoes.PARTICIPANTE` | Sim | `demaisPerfisRecebem403` |
| 09 `MeController` | Sim | `MeEndpointTest` |
| 10 Resumo, status e responsável | Sim para o resumo, o status e o responsável; **parcial** para `situacaoParticipacao` | `MeuEstudoResponseTest`, `detalheDoEstudoDaConta` |
| 11 Nenhum dado de outro nem de candidato | Sim | `listaSoOsEstudosDaConta`, `idPorParametroEhIgnorado`, `detalheDoEstudoDaConta` |
| 12 Erros 401, 403, 404 | Sim | `demaisPerfisRecebem403`, `semTokenRecebe401`, `estudoDeOutroParticipanteRecebe404` |
| 13 Swagger e contrato | Sim (`@Tag`, `@Operation`); o teste de contrato não tinha `@Disabled` | `contratoTemAsOperacoesEsperadas` |
| 14 Avisar a I5 | **Pendente (ação humana)** | Avisar no PR |

## 6. O que as próximas tasks precisam alterar

Consolidado no documento [T109 · pendências para as próximas tasks](../design/sprint3/T109-pendencias-para-proximas-tasks.md).
