# T125 · Endpoints /me do RF19 e Swagger

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Qua 25/11  
> **Depende de:** T123, T124  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor os três endpoints do RF19 sob `/api/me`, com os campos novos da visita, erros no formato único e Swagger igual ao contrato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `MeController` já é `@PreAuthorize(Permissoes.PARTICIPANTE)` e identifica a conta com `Autenticacao.idDe(jwt)`.
- `ApiExceptionHandler` já trata 400, 403, 404, 409 e falha de lock otimista.
- `MinhaVisitaResponse.de(visita, atividades, clock)` é usado por próximas e histórico.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/MeController.java` | Alterar | Três operações novas |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/PedidoReagendamentoRequest.java` | Criar | `motivo`, `dataPreferida` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/MinhaSolicitacaoResponse.java` | Criar | Resposta do pedido |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/PedidoPendenteResponse.java` | Criar | Resumo embutido na visita |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/MinhaVisitaResponse.java` | Alterar | Cinco campos novos |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/MinhasVisitasService.java` | Alterar | Preencher os campos sem consulta por visita |
| `docs/api/openapi-sprint3.yaml` | Alterar | Campos novos |
| `mobile/ClinicalStudyPB/src/api/` | Alterar | Regenerar tipos e ajustar mocks |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoMeEndpointTest.java` | Criar | Fumaça e permissões |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T125-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T125-01** `POST /api/me/visitas/{visitaAgendadaId}/confirmacao` devolve `200` com a `MinhaVisitaResponse` atualizada.
- **RQ-T125-02** `POST /api/me/visitas/{visitaAgendadaId}/pedidos-reagendamento` com `@Valid`, devolve `201` com `MinhaSolicitacaoResponse`.
- **RQ-T125-03** `GET /api/me/pedidos` devolve a lista do participante logado.
- **RQ-T125-04** `PedidoReagendamentoRequest`: `@Size(max = 500)` no motivo; a obrigatoriedade e o mínimo ficam no serviço (código `MOTIVO_OBRIGATORIO`).
- **RQ-T125-05** `MinhaVisitaResponse` ganha `presencaConfirmadaEm`, `pedidoPendente`, `podeConfirmar`, `podePedirReagendamento` e `retiradaPendente` (há pedido de retirada pendente na participação da visita).
- **RQ-T125-06** Completar os campos novos nos mocks (`api/mocks/dados.ts`) e nas fixtures dos testes do app, para `npm run typecheck` e `npm test` continuarem verdes.
- **RQ-T125-07** `MinhasVisitasService` busca os pedidos pendentes das participações em **uma** consulta e monta um mapa por visita; o número de consultas não cresce com o número de visitas.
- **RQ-T125-08** No histórico, os dois booleanos são `false`.
- **RQ-T125-09** Erros: `401` sem token; `403` para administrador, pesquisador e coordenador; `404` para visita de outra conta ou inexistente; `409` com o código da regra.
- **RQ-T125-10** `@Operation` com a regra e os códigos; conferir o Swagger contra os dois YAML e corrigir o que divergir no mesmo Pull Request.
- **RQ-T125-11** Remover o `@Disabled("T125")` do `ContratoOpenApiSprint4Test` e regenerar os tipos.
- **RQ-T125-12** O acesso aos dados da conta passa pelo componente `AcessoParticipante` (T109), nas leituras e nas escritas.

## 5. Contrato, regras e códigos de erro

Operações 1 a 3 (seção 2), DTOs (seções 3.1 e 3.2) e códigos (seção 4) das convenções.

## 6. Testes

Fumaça dos três endpoints, 401/403/404 e um 409; a matriz completa é a T127. `MinhasVisitasEndpointTest` e `MinhasVisitasServiceTest` ajustados e verdes.

## 7. Critérios de aceite

- [ ] O participante confirma, pede reagendamento e lista os pedidos.
- [ ] Próximas visitas já trazem os selos sem outra chamada.
- [ ] Contratos das três sprints verdes.

## 8. Cuidados e fora de escopo

- Nunca aceitar id de participante ou de usuário por parâmetro ou corpo.
- A resposta ao app não inclui nome de quem respondeu nem dados de outros participantes.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T125 · Endpoints /me do RF19 e Swagger.** Primeiro exponha os endpoints do contrato usando o componente de acesso do participante da T109, restritos ao perfil participante. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
