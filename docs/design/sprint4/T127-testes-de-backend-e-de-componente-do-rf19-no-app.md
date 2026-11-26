# T127 · Testes de backend e de componente do RF19 no app

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Qua 25/11  
> **Depende de:** T124, T126  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Fechar a cobertura de testes do RF19 no app: matriz das regras, endpoints em PostgreSQL e componentes.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Base criada nas T122, T124, T125 e T126.
- `support/CenarioRecrutamento` monta estudo, participante e agendamento; `ContaParticipanteEndpointTest` mostra como vincular a conta.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RegraSolicitacaoTest.java` | Alterar | Matriz completa |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoMeEndpointTest.java` | Alterar | Todos os status |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoConcorrenciaTest.java` | Criar | Confirmação e pedido simultâneos |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `participanteComConta(...)` |
| `mobile/ClinicalStudyPB/src/telas/__tests__/solicitacoes.test.tsx` | Criar | Botões, formulário, selos, pedidos |
| `mobile/ClinicalStudyPB/src/api/__tests__/solicitacoes.test.ts` | Criar | Módulo de API e mocks |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T127-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T127-01** Matriz unitária das regras 1 e 2: visita passada, já confirmada, com cada desfecho, de outro participante, participante retirado, retirada pendente, estudo encerrado e pedido duplicado.
- **RQ-T127-02** Endpoints: `200`, `201`, `400`, `401`, `403` (três perfis), `404` e `409` por código, conferindo o formato único.
- **RQ-T127-03** Isolamento: o participante B não confirma, não pede e não lista nada do participante A.
- **RQ-T127-04** Concorrência: quatro confirmações simultâneas dão um `200` e três `409`; quatro pedidos simultâneos dão um `201` e três `409`, com uma só linha no banco e nenhum `500`.
- **RQ-T127-05** Número de consultas de `/api/me/visitas/proximas` igual com 1 e com 10 visitas (estatísticas do Hibernate, como na T98).
- **RQ-T127-06** Rodar no job `Backend (testes no PostgreSQL)`.
- **RQ-T127-07** Componentes: visibilidade dos botões pelos booleanos; motivo obrigatório; envio desabilitado durante a requisição; selos; mensagem de 409; lista "Meus pedidos" em todos os estados.
- **RQ-T127-08** Conferir no JaCoCo as linhas de `RegraSolicitacao` e `SolicitacaoParticipanteService` (meta da fatia: 90% ou mais; mínimo do projeto: 80%) e registrar os números no Pull Request.

## 5. Contrato, regras e códigos de erro

Seção 4 das convenções.

## 6. Testes

Esta task é a de testes: os itens acima. Comandos: `./mvnw verify -Djacoco.haltOnFailure=true`, `npm test` no app.

## 7. Critérios de aceite

- [ ] Suítes de backend e do app verdes, inclusive no PostgreSQL do CI.
- [ ] Cobertura registrada no Pull Request.

## 8. Cuidados e fora de escopo

- Testes de concorrência sem `@Transactional`, com limpeza no `@AfterEach`.
- Não depender dos dados de demonstração.

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

> **T127 · Testes de backend e de componente do RF19 no app.** Escreva os testes junto com o código. Primeiro faça a matriz unitária das regras 1 e 2: visita passada, já confirmada, com desfecho, de outro participante, participante retirado e pedido duplicado. Depois teste os endpoints (200, 201, 400, 403, 404 e 409) com PostgreSQL real. Por fim, escreva os testes de componente dos botões, do motivo obrigatório e dos selos, e confira a cobertura da fatia.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
