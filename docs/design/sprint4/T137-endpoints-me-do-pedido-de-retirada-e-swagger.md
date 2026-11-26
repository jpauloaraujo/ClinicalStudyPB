# T137 · Endpoints /me do pedido de retirada e Swagger

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Sáb 28/11  
> **Depende de:** T136  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor o pedido de retirada do participante e incluir o pedido pendente no detalhe do estudo do RF17.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `MeController`, `MeusEstudosService` e `MeuEstudoDetalheResponse`.
- `ContratoOpenApiSprint3Test` confere os campos do schema do detalhe.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/MeController.java` | Alterar | Operação 8 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/PedidoRetiradaRequest.java` | Criar | Corpo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/MeuEstudoDetalheResponse.java` | Alterar | Dois campos |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/MeusEstudosService.java` | Alterar | Pedido pendente |
| `docs/api/openapi-sprint3.yaml` | Alterar | Campos novos |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PedidoRetiradaEndpointTest.java` | Criar | Fumaça |
| `mobile/ClinicalStudyPB/src/api/` | Alterar | Tipos regenerados |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T137-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T137-01** `POST /api/me/estudos/{estudoId}/pedido-retirada` só para `PARTICIPANTE`, `201` com `MinhaSolicitacaoResponse`.
- **RQ-T137-02** 404 para estudo de outro participante ou inexistente, sem distinguir os dois.
- **RQ-T137-03** `MeuEstudoDetalheResponse` ganha `pedidoRetiradaPendente` (`id`, `criadoEm`) e `podeSolicitarRetirada`.
- **RQ-T137-04** Pedido de retirada aparece em `GET /api/me/pedidos`.
- **RQ-T137-05** 400 `CIENCIA_OBRIGATORIA` e `VALIDACAO_CAMPOS`; 409 por código; formato único.
- **RQ-T137-06** `@Operation` descrevendo a regra 4; conferir Swagger × YAML; reativar o teste de contrato da operação 8; regenerar tipos.
- **RQ-T137-07** O motivo da retirada efetivada continua fora das respostas de estudo do participante (regra da Sprint 3).
- **RQ-T137-08** Completar os campos novos em `estudosMock` (`api/mocks/dados.ts`) e nas fixtures dos testes do app.

## 5. Contrato, regras e códigos de erro

Operação 8, seções 3 e 4.

## 6. Testes

Fumaça, permissões e detalhe antes e depois do pedido; `MeEndpointTest` ajustado.

## 7. Critérios de aceite

- [ ] Pedido criado e visível no detalhe.
- [ ] Perfis não participantes recebem 403.

## 8. Cuidados e fora de escopo

- Nunca receber o id da participação do cliente.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF20** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir solicitar retirada voluntária do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 4 | O participante solicita a retirada pelo app sem precisar justificar (motivo opcional), mas confirma que entende os efeitos. Há no máximo um pedido pendente por participação, e enquanto ele existe o app bloqueia confirmações e novos pedidos de reagendamento | T136, T137, T139 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T137 · Endpoints /me do pedido de retirada e Swagger.** Primeiro exponha o pedido de retirada de um estudo do participante, restrito ao perfil participante e com 404 para estudo de outro. Depois inclua no detalhe do estudo do RF17 o pedido pendente, se houver. Por fim, trate os erros no formato único e confira o Swagger contra o YAML.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
