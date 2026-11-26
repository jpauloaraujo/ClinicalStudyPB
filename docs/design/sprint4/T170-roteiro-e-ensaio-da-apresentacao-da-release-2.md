# T170 · Roteiro e ensaio da apresentação da Release 2

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Seg 07/12  
> **Depende de:** T169  
> **Situação:** 📋 Entrega fora do código (especificada neste documento)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Escrever o roteiro, ensaiar e preparar o ambiente da apresentação da Release 2.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/sprint2/T72 — Roteiro de entrega e demonstração da Release 1.md` como modelo; dados da T157.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint4/roteiro-apresentacao-release2.md` | Criar | Roteiro |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T170-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T170-01** Ordem: painel; agenda, desfecho e resultados; app com confirmação e pedido de reagendamento; atendimento pelo coordenador; retirada voluntária; relatório; testes, cobertura e Swagger UI.
- **RQ-T170-02** Para cada passo: apresentador (dono do PBI), perfil, conta fictícia, tela, ação, resultado esperado e tempo.
- **RQ-T170-03** Incluir um caso de erro por fatia.
- **RQ-T170-04** Preparar o ambiente a partir da tag, com banco recriado no dia (datas relativas).
- **RQ-T170-05** Ordenar as ações para uma não invalidar a seguinte (a retirada é irreversível: usar participante próprio).
- **RQ-T170-06** Plano B: capturas e vídeo caso emulador ou rede falhem.
- **RQ-T170-07** Ensaiar com o time cronometrado e registrar ajustes.
- **RQ-T170-08** Conferir antes da apresentação: backend, web, app, Swagger e relatórios de teste abertos.

## 5. Contrato, regras e códigos de erro

Seção 7 das convenções.

## 6. Testes

Ensaio completo do roteiro.

## 7. Critérios de aceite

- [ ] Roteiro publicado, ensaio feito e ambiente pronto.

## 8. Cuidados e fora de escopo

- O documento entra depois da tag sem alterar o que foi marcado.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- Entregas (seção 10): “Cada release é entregue pelo repositório no GitHub, com código-fonte e documentação daquela versão.”
- Identificação da entrega: “Criar a tag `release1`, `release2` ou `release3` apontando para a versão entregue”, “Criar uma GitHub Release associada à tag, com resumo e link para o README da entrega” e “Apresentar o sistema funcionando, demonstrando requisitos, interfaces e testes”.

### 9.2 Critério de aceite do PBI (planejamento)

> a Sprint foi planejada e acompanhada no Azure Boards; README principal e README da Release 2 estão completos, com o processo das Sprints 3 e 4; os diagramas renderizam no GitHub; a tag release2 e a GitHub Release existem; a apresentação foi ensaiada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 9 | A Release 2 abrange as Sprints 3 e 4 e é entregue na segunda 07/12, com a apresentação no mesmo dia. Data e escopo devem ser confirmados com a professora | T164, T168 a T170 |

### 9.4 Texto original da task no planejamento

> **T170 · Roteiro e ensaio da apresentação da Release 2.** Primeiro monte o roteiro, nesta ordem: painel do estudo; agenda, desfecho e resultados; app com confirmação e pedido de reagendamento; atendimento pelo coordenador; retirada voluntária; relatório; e, por fim, testes, cobertura e Swagger UI. Depois ensaie com o time, cada dono apresentando o seu PBI. Por fim, deixe o ambiente de demonstração pronto com os dados consolidados da T157.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
