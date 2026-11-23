# T98 · Testes de backend e de componente do histórico

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Ter 17/11  
> **Depende de:** T96, T97  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar a consulta agregada, a ordenação, o resumo e as permissões do histórico, e os componentes da ficha, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `AvaliacaoEndpointTest`, `ParticipacaoEndpointTest` (MockMvc com tokens reais) e os testes de componente da Sprint 2 (`SecaoInclusao.test.tsx`).
- `CenarioRecrutamento` e os métodos das T78, T85 e T92 montam participante, agendamento, desfecho e resultado.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/HistoricoParticipanteServiceTest.java` | Criar | Resumo e montagem (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/HistoricoParticipanteEndpointTest.java` | Criar | 200, 403, 404 e 401 com tokens reais |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/FichaParticipantePage.test.tsx` | Alterar | Complementa os da T97 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T98-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Backend

- **RQ-T98-01** Participante **sem visitas agendadas**: todas as visitas previstas aparecem como `NAO_AGENDADA`, `naoAgendadas = previstas`, `pendentes = previstas` e os demais contadores zerados.
- **RQ-T98-02** Participante com visitas **em todas as situações** (`AGENDADA`, `REALIZADA`, `AUSENTE`, `CANCELADA` e uma não agendada): cada situação aparece certa, com datas, motivo e resultados, e o **invariante** `previstas = realizadas + ausentes + canceladas + agendadas + naoAgendadas` vale.
- **RQ-T98-03** **Ordenação** pelo dia previsto (criar as visitas previstas fora de ordem e conferir a ordem na resposta); resultados ordenados pelo nome da atividade; visita realizada sem resultado devolve lista vazia.
- **RQ-T98-04** Estudo sem visitas previstas: lista vazia e resumo zerado (200, não 404).
- **RQ-T98-05** Permissões: **200** para pesquisador e coordenador; **403** para administrador e participante; **401** sem token; **404** para participação inexistente.
- **RQ-T98-06** O histórico é somente leitura: `POST`, `PUT` e `DELETE` em `/api/participacoes/{id}/historico` respondem 405 (ou 403 pelo perfil), sem alterar dados.
- **RQ-T98-07** A consulta usa um número fixo de consultas ao banco, **independente do número de visitas** (teste com 1 e com 10 visitas previstas, contando as consultas, por exemplo com as estatísticas do Hibernate).
- **RQ-T98-08** Rodar também no job `Backend (PostgreSQL)`.

### Componente e cobertura

- **RQ-T98-09** Completar os testes de `FichaParticipantePage` da T97 com os casos de ordem e de resumo vindos de um corpo completo da API.
- **RQ-T98-10** Conferir a cobertura de `HistoricoParticipanteService` (linhas e ramos) e registrar o resultado no Pull Request; meta 95% ou mais; toda a suíte anterior continua passando.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (10 operações).

## 7. Critérios de aceite

- [ ] A consulta está provada para participante sem visitas e com visitas em todas as situações.
- [ ] A ordenação, o resumo e as permissões (200, 403, 404) têm teste.
- [ ] A cobertura da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- O invariante do resumo é o teste mais importante desta task.
- Não depender da data do dia.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF15** (Módulo D — Acompanhamento; interface Web): “Consultar o histórico de visitas e avaliações de um participante.”
- Este PBI reúne também o **acompanhamento da Sprint** (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO.

### 9.2 Critério de aceite do PBI (planejamento)

> Pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Este PBI também reúne o acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO. O Planning (T94) é uma cerimônia da terça 10/11 e acontece antes da janela do PBI.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T98 · Testes de backend e de componente do histórico.** Primeiro teste a consulta com participante sem visitas e com visitas em todas as situações. Depois teste a ordenação, o resumo e as permissões (200, 403 e 404). Por fim, confira a cobertura da fatia.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Estratégia de testes: “Testes automatizados de componentes nas interfaces web e mobile.” Em componentes web, testar o “comportamento das telas: validação de formulários, botões, mensagens de erro”. Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
