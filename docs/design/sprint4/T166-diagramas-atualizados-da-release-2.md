# T166 · Diagramas atualizados da Release 2

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Dom 06/12  
> **Depende de:** T163  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Atualizar os diagramas da Release 2 e garantir que renderizam no GitHub.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/diagramas/`: `casos-de-uso.md` (só até RF11), `dominio.md`, `arquitetura.md` e duas sequências.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/diagramas/casos-de-uso.md` | Alterar | RF01 a RF22 |
| `docs/diagramas/dominio.md` | Alterar | Solicitações e confirmação |
| `docs/diagramas/sequencia-agendamento-desfecho-resultado.md` | Criar | Sequência |
| `docs/diagramas/sequencia-pedido-reagendamento.md` | Criar | App até a web |
| `docs/diagramas/sequencia-retirada-voluntaria.md` | Criar | Retirada |
| `docs/diagramas/arquitetura.md` | Alterar | Arquitetura atual |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T166-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T166-01** Casos de uso com os quatro perfis e RF01 a RF22, conforme os `@PreAuthorize` reais.
- **RQ-T166-02** Classes com `VisitaAgendada`, `ResultadoAtividade`, `RetiradaParticipacao` e `SolicitacaoParticipante`, com enums e cardinalidades iguais ao código.
- **RQ-T166-03** Três sequências em Mermaid, com os endpoints reais, os 409 principais e o limite das transações.
- **RQ-T166-04** Arquitetura com web, app, API, serviços, banco, migrations e CI.
- **RQ-T166-05** Conferir cada diagrama contra o código entregue, não contra o planejamento.
- **RQ-T166-06** Abrir cada arquivo no GitHub (na branch) e confirmar que renderiza.

## 5. Contrato, regras e códigos de erro

Seções 2, 5 e 6 das convenções.

## 6. Testes

Renderização no GitHub de todos os blocos.

## 7. Critérios de aceite

- [ ] Diagramas atualizados e renderizando.

## 8. Cuidados e fora de escopo

- Evitar diagramas ilegíveis: dividir por assunto.

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

> **T166 · Diagramas atualizados da Release 2.** Primeiro atualize o diagrama de casos de uso com RF01 a RF22 e o de classes com a visita agendada, os resultados, a retirada e os pedidos do participante. Depois desenhe em Mermaid três sequências: agendamento, desfecho e resultado; pedido de reagendamento do app até o atendimento na web; e retirada voluntária. Por fim, atualize a arquitetura e confira no GitHub que todos renderizam.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
