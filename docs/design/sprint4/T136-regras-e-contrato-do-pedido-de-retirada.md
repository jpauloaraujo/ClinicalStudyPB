# T136 · Regras e contrato do pedido de retirada

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Sáb 28/11  
> **Depende de:** T135  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Acrescentar ao serviço de pedidos o tipo `RETIRADA`, ligar o bloqueio do RF19 e descrever o contrato do pedido e da efetivação.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `SolicitacaoParticipanteService` e `RegraSolicitacao` (T124) já recebem `retiradaPendente`.
- `RetiradaService` (T102) tem os códigos `PARTICIPANTE_JA_RETIRADO` e `ESTUDO_ENCERRADO`.
- `AcessoParticipante.participacaoDoEstudo` devolve 404 para estudo de outra conta.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/RegraSolicitacao.java` | Alterar | `validarPedidoRetirada`, `podeSolicitarRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/SolicitacaoParticipanteService.java` | Alterar | `pedirRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AcessoParticipante.java` | Alterar | `idDaParticipacaoNoEstudo`, sem carregar a entidade |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Alterar | Projeção do id |
| `docs/api/openapi-sprint4.yaml` | Alterar | Operações 8 e 9 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | 9 operações |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RegraSolicitacaoTest.java` | Alterar | Regra 4 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T136-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T136-01** `pedirRetirada(usuarioId, estudoId, cienteDosEfeitos, motivo)`: obter só o id com `AcessoParticipante.idDaParticipacaoNoEstudo` (projeção; 404), **bloquear a participação** com `buscarComBloqueio` sem tê-la carregado antes, validar e criar a solicitação `PENDENTE` do tipo `RETIRADA`, sem visita.
- **RQ-T136-02** Ordem: `ESTUDO_ENCERRADO` → `PARTICIPANTE_JA_RETIRADO` → `PEDIDO_RETIRADA_PENDENTE` → `CIENCIA_OBRIGATORIA` (nulo ou falso).
- **RQ-T136-03** Motivo opcional, aparado, vazio vira nulo, máximo 500 (regra 4: sem justificativa obrigatória).
- **RQ-T136-04** O pedido não muda participação nem visitas.
- **RQ-T136-05** Com retirada pendente, confirmar e pedir reagendamento dão `RETIRADA_PENDENTE` e os booleanos `podeConfirmar`/`podePedirReagendamento` ficam falsos (cobrir com teste).
- **RQ-T136-06** Reagendamentos que já estavam pendentes continuam pendentes até a efetivação.
- **RQ-T136-07** Contrato: `POST /api/me/estudos/{estudoId}/pedido-retirada` (201, 400, 401, 403, 404, 409) e `POST /api/solicitacoes/{solicitacaoId}/retirada` (201 `RetiradaResponse`, 400, 401, 403, 404, 409), com `PedidoRetiradaRequest` e `EfetivacaoRetiradaRequest`.
- **RQ-T136-08** Atualizar a contagem de contrato (9), com `@Disabled` até as T137 e T138, e gerar os tipos.

## 5. Contrato, regras e códigos de erro

Operações 8 e 9; códigos e ordem da seção 4; ajustes 6 e 8.

## 6. Testes

Cada código, a ordem, motivo nulo/vazio/500, e o bloqueio do RF19.

## 7. Critérios de aceite

- [ ] Um pedido por participação; ciência obrigatória; motivo opcional.
- [ ] RF19 bloqueado enquanto pendente.

## 8. Cuidados e fora de escopo

- O bloqueio da participação evita dois pedidos simultâneos.
- Nenhuma nova migration: a tabela já aceita o tipo.

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

> **T136 · Regras e contrato do pedido de retirada.** Primeiro implemente no serviço de pedidos o tipo RETIRADA: participação ativa, nenhum pedido de retirada pendente, ciência dos efeitos obrigatória e motivo opcional (regra 4). Depois inclua nas regras da T124 o bloqueio de confirmações e de novos pedidos de reagendamento enquanto a retirada estiver pendente. Por fim, descreva no contrato o pedido de retirada sob /me e a efetivação pelo coordenador, com os códigos de erro.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
