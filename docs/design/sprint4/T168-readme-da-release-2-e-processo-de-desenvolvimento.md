# T168 · README da Release 2 e processo de desenvolvimento

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 5h · **Prazo:** Seg 07/12 (manhã)  
> **Depende de:** T167  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Escrever o README da Release 2 e o processo de desenvolvimento das Sprints 3 e 4.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `releases/release1/README.md` como modelo; documentos de processo das Sprints 3 e 4.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `releases/release2/README.md` | Criar | Documento da release |
| `docs/imagens/release2/` | Criar | Burndowns |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T168-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T168-01** Objetivo e sprints abrangidas.
- **RQ-T168-02** Situação de RF12 a RF22: concluído, parcial ou não implementado, com justificativa; confirmar que RF01 a RF11 continuam funcionando.
- **RQ-T168-03** Funcionalidades com capturas.
- **RQ-T168-04** Alterações de arquitetura e stack.
- **RQ-T168-05** Testes e cobertura (T163).
- **RQ-T168-06** Problemas conhecidos e pendências (T162).
- **RQ-T168-07** Para **cada** sprint (3 e 4): backlog e tarefas; papéis e contribuições dos sete integrantes; burndown do Azure DevOps com análise do planejado × realizado; revisão com entregas, impedimentos e ações de melhoria.
- **RQ-T168-08** Incluir a passagem de bastão da Sprint 3 entre as ações de melhoria.
- **RQ-T168-09** Confirmar contribuições com cada integrante.
- **RQ-T168-10** Entregar até 07/12 de manhã, antes da tag.

## 5. Contrato, regras e códigos de erro

Entregas, "Documentação de cada release".

## 6. Testes

Revisão por I7 e links conferidos.

## 7. Critérios de aceite

- [ ] Documento completo, revisado e na `main`.

## 8. Cuidados e fora de escopo

- Burndown é a imagem real do Azure DevOps.
- O que for decidido na Review de 07/12 e não puder entrar antes da tag fica registrado como pendente.

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

> **T168 · README da Release 2 e processo de desenvolvimento.** Primeiro crie releases/release2/README.md com objetivo, Sprints 3 e 4, situação de cada requisito RF12 a RF22 (concluído, parcial ou não implementado, com justificativa), funcionalidades com capturas, alterações de arquitetura, testes e cobertura, e problemas conhecidos. Depois escreva a seção Processo de desenvolvimento das duas sprints: backlog e tasks, papéis e contribuições dos sete integrantes, burndown do Azure DevOps com a análise do planejado contra o realizado, e a revisão com impedimentos e ações de melhoria. Inclua a mudança da Sprint 3 para a passagem de bastão como uma das ações de melhoria.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
