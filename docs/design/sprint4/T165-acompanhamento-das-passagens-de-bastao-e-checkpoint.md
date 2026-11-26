# T165 · Acompanhamento das passagens de bastão e checkpoint

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Qua 02/12  
> **Depende de:** Leitura de todos os PBIs  
> **Situação:** 📋 Entrega fora do código (especificada neste documento)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Acompanhar cada passagem de bastão, conduzir o checkpoint de 02/12 e escrever o rascunho do processo da Sprint 4.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/sprint3/checkpoint-17-11.md` e `processo-de-desenvolvimento-sprint3.md` como modelos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint4/checkpoint-02-12.md` | Criar | Ata |
| `docs/sprint4/processo-de-desenvolvimento-sprint4.md` | Criar | Rascunho |
| `docs/imagens/sprint4/` | Alterar | Burndown |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T165-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T165-01** Em cada passagem (25/11, 27/11, 29/11, 01/12, 03/12, 05/12), conferir: PBI integrado, CI verde, dados de demonstração e nota de bastão.
- **RQ-T165-02** Registrar atraso e ação no mesmo dia.
- **RQ-T165-03** Atualizar burndown e Remaining Work e guardar a imagem do gráfico.
- **RQ-T165-04** Checkpoint de 15 minutos: o que foi entregue, se a sequência está no prazo e o que cortar.
- **RQ-T165-05** Se houver corte, seguir a ordem do planejamento e nunca cortar RF19 a RF22 nem a entrega.
- **RQ-T165-06** Rascunhar objetivo, backlog, organização da equipe, burndown (planejado × realizado) e revisão.
- **RQ-T165-07** Marcar o que só se completa na Review de 07/12.

## 5. Contrato, regras e códigos de erro

Cerimônias e riscos do planejamento.

## 6. Testes

Conferir cada afirmação contra o repositório e o CI.

## 7. Critérios de aceite

- [ ] Ata do checkpoint e rascunho publicados.

## 8. Cuidados e fora de escopo

- Não preencher contribuição ou número que não foi verificado.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- Entregas (seção 10): “Cada release é entregue pelo repositório no GitHub, com código-fonte e documentação daquela versão.”
- Identificação da entrega: “Criar a tag `release1`, `release2` ou `release3` apontando para a versão entregue”, “Criar uma GitHub Release associada à tag, com resumo e link para o README da entrega” e “Apresentar o sistema funcionando, demonstrando requisitos, interfaces e testes”.

### 9.2 Critério de aceite do PBI (planejamento)

> a Sprint foi planejada e acompanhada no Azure Boards; README principal e README da Release 2 estão completos, com o processo das Sprints 3 e 4; os diagramas renderizam no GitHub; a tag release2 e a GitHub Release existem; a apresentação foi ensaiada.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T165 · Acompanhamento das passagens de bastão e checkpoint.** Primeiro confira, a cada passagem, se o PBI anterior está na main com dados de demonstração e nota para o próximo dono, e atualize o burndown. Depois, na quarta 02/12, conduza o checkpoint de 15 minutos: o que já foi entregue, se a sequência está no prazo e o que cortar. Por fim, escreva o rascunho da seção Processo de desenvolvimento da Sprint 4.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
