# T100 · Checkpoint de integração e acompanhamento da Sprint

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Ter 17/11  
> **Depende de:** PBIs 19, 20 e 21 (leitura)  
> **Situação:** 📋 Entrega fora do código (cerimônia e processo), especificada neste documento e ainda obrigatória

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Acompanhar a Sprint: atualizar o burndown e o Remaining Work, confirmar que os PBIs 19, 20 e 21 estão 100% na `main`, conduzir o checkpoint de 15 minutos no meio da Sprint (o que atrasou, o que corre risco nos PBIs 23, 24 e 25 e o que cortar) e escrever o rascunho da seção "Processo de desenvolvimento" da Sprint 3.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O Azure Boards da Sprint 3 foi cadastrado na T94; cada dono atualiza o Remaining Work diariamente. O burndown do Azure DevOps é o que o README da Release 2 mostra.
- A **ordem de corte** do planejamento: primeiro a agenda do estudo por período (T77) e o filtro por situação do histórico (T97); depois o reagendamento (T75 e T77) e o resumo de comparecimento do app (T118); só então os cenários de erro dos E2E. O agendamento, o desfecho, os resultados, o histórico, a retirada, o login mobile e as listas de estudos e visitas do app **não** devem ser cortados.
- A seção "Processo de desenvolvimento" da Release 1 (`releases/release1/README.md`, seção 7) é o modelo.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `Azure Boards` | Atualizar | Burndown, Remaining Work e vínculos |
| `docs/sprint3/checkpoint-17-11.md` | Criar | Ata do checkpoint de meio de Sprint |
| `docs/sprint3/processo-de-desenvolvimento-sprint3.md` | Criar | Rascunho da seção para o README da Release 2 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T100-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Burndown e confirmação dos PBIs

- **RQ-T100-01** Atualizar o **burndown** e o **Remaining Work** de todas as tasks e guardar a imagem do gráfico do dia (para a análise do planejado contra o realizado).
- **RQ-T100-02** Confirmar que os **PBIs 19, 20 e 21** estão 100% concluídos e integrados na `main`: todas as tasks com Remaining Work zero, Pull Requests revisados e integrados, testes passando, README atualizado. Listar na ata o que não estiver.
- **RQ-T100-03** Verificar que o CI da `main` está verde (os 6 jobs) e que os PBIs 19 a 22 entraram na ordem prevista.

### Checkpoint de 15 minutos (ter 17/11)

- **RQ-T100-04** Conduzir o checkpoint com a pauta: **o que está na `main`**, **o que atrasou**, **o que corre risco nos PBIs 23, 24 e 25** (janelas de fim de semana e feriado, e o PBI-25 que termina no dia da Review) e **o que cortar** segundo a ordem de corte.
- **RQ-T100-05** Registrar na ata `docs/sprint3/checkpoint-17-11.md` as decisões: cortes, troca de lugar entre PBIs (o PBI-24 pode trocar com o PBI-23, se necessário), reforço de revisão e itens que vão para a Sprint 4.
- **RQ-T100-06** Reavaliar os riscos da seção de riscos do planejamento (atraso em cadeia, carga diária, janelas não úteis, concorrência, migrations, E2E mobile) e registrar o status de cada um.

### Rascunho do "Processo de desenvolvimento" da Sprint 3

- **RQ-T100-07** Escrever `docs/sprint3/processo-de-desenvolvimento-sprint3.md` com: **objetivo da Sprint**, **PBIs** (7 PBIs, 49 tasks, 146 horas), **organização da equipe** (um PBI por integrante em janelas de 2 dias, revisão cruzada pelo dono do PBI seguinte, passagens de bastão), **burndown** (imagem do dia e análise parcial) e **análise do planejado contra o realizado** até o checkpoint.
- **RQ-T100-08** Deixar marcadas as partes que só podem ser completadas na Review de 23/11 (burndown final, resultado da retrospectiva, impedimentos e ações de melhoria).

## 6. Testes

- Conferência (não há teste automatizado): ata do checkpoint publicada, burndown atualizado e rascunho do processo escrito.

## 7. Critérios de aceite

- [ ] Os PBIs 19, 20 e 21 estão confirmados como concluídos (ou a ata lista o que falta).
- [ ] O checkpoint aconteceu e as decisões estão registradas.
- [ ] O rascunho da seção Processo de desenvolvimento da Sprint 3 está no repositório.

## 8. Cuidados e fora de escopo

- Não cortar itens fora da ordem de corte sem registrar a decisão e o motivo.
- O rascunho é completado na Review; não inventar números do burndown final.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF15** (Módulo D — Acompanhamento; interface Web): “Consultar o histórico de visitas e avaliações de um participante.”
- Este PBI reúne também o **acompanhamento da Sprint** (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO.

### 9.2 Critério de aceite do PBI (planejamento)

> Pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Este PBI também reúne o acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO. O Planning (T94) é uma cerimônia da terça 10/11 e acontece antes da janela do PBI.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T100 · Checkpoint de integração e acompanhamento da Sprint.** Primeiro atualize o burndown e o Remaining Work e confirme que os PBIs 19, 20 e 21 estão 100% concluídos e integrados na main. Depois, na terça 17/11, conduza o checkpoint de 15 minutos, no meio da Sprint: o que está na main, o que atrasou, o que corre risco nos PBIs 23, 24 e 25 e o que cortar. Por fim, escreva o rascunho da seção Processo de desenvolvimento da Sprint 3 (objetivo, PBIs, organização da equipe, burndown e análise) para o README da Release 2, que será completado na Review.

### 9.5 Requisitos da especificação que atravessam esta task

- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- README de cada release, seção **Processo de desenvolvimento** por sprint: (1) Backlog e tarefas, (2) Organização da equipe, (3) Burndown (“gráfico do Azure DevOps com breve análise planejado × realizado”) e (4) Revisão (“o que foi entregue, impedimentos e ações de melhoria”). Gerenciamento ágil: Azure DevOps.
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
