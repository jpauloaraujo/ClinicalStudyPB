# T164 · Planning: validar regras, confirmar a Release 2 e cadastrar a Sprint 4

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Ter 24/11  
> **Depende de:** Nenhuma  
> **Situação:** 📋 Entrega fora do código (especificada neste documento)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Validar as regras e os ajustes, confirmar a Release 2 e cadastrar a Sprint 4 no Azure Boards, para o PBI-26 começar no mesmo dia.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Planejamento da Sprint 4; ata e CSV de sprints anteriores em `docs/sprint3/` e `docs/azure-boards/`.
- Ajustes desta documentação na seção 1 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint4/ata-planning-sprint4.md` | Criar | Decisões |
| `docs/azure-boards/sprint4-backlog.csv` | Criar | Backlog |
| `docs/azure-boards/README.md` | Alterar | Instruções da Sprint 4 |
| `docs/design/sprint4/` | Alterar | Correções decididas pelo PO |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T164-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T164-01** Registrar na ata a resposta do PO (confirmada ou corrigida) às nove regras.
- **RQ-T164-02** Levar os ajustes marcados com **PO** (5, 6, 7 e 8) e confirmar os demais com o time, em especial `V7_`, estudo de demonstração novo e colunas extras do relatório.
- **RQ-T164-03** Perguntar também: a confirmação é permitida com reagendamento pendente? (este design: sim, a regra 1 não proíbe).
- **RQ-T164-04** Corrigir no mesmo dia as convenções e os documentos das tasks afetadas.
- **RQ-T164-05** Confirmar com a professora a data e o escopo da Release 2; se não houver resposta, registrar a pendência com data.
- **RQ-T164-06** Criar a iteração Sprint 4 (24/11 a 07/12/2026), os 7 PBIs (PBI-26 a PBI-32) e as 49 tasks com Assigned To e Remaining Work.
- **RQ-T164-07** Criar vínculos Predecessor/Successor, incluindo a passagem de bastão, e a capacity dos sete integrantes.
- **RQ-T164-08** Gerar o CSV no formato das sprints anteriores (146 horas).
- **RQ-T164-09** Confirmar as janelas, inclusive as de fim de semana (I5, I1 e I2); se alguém não puder, trocar a ordem respeitando as dependências.
- **RQ-T164-10** Levantar o carry-over da Sprint 3 e abrir os itens com responsável; acima de cerca de 15 horas, aplicar a ordem de corte.
- **RQ-T164-11** Publicar a ata e avisar o grupo no mesmo dia.

## 5. Contrato, regras e códigos de erro

Regras 1 a 9.

## 6. Testes

Conferir: 7 PBIs, 49 tasks, 146 horas no Azure Boards e no CSV.

## 7. Critérios de aceite

- [ ] Ata publicada, Sprint cadastrada e documentos corrigidos.

## 8. Cuidados e fora de escopo

- Cadastro no Azure Boards é feito por uma pessoa; o repositório guarda a evidência.

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

> **T164 · Planning: validar regras, confirmar a Release 2 e cadastrar a Sprint 4.** Esta task fecha no mesmo dia, porque I7 começa o PBI-26 logo depois. Primeiro leve as nove regras deste documento para a reunião, anote as respostas e corrija as tasks afetadas. Depois confirme com a professora a data e o escopo da Release 2. Em seguida cadastre a iteração, os PBIs, as tasks e as dependências como descrito na seção do Azure DevOps. Por fim, levante o carry-over da Sprint 3 e abra os itens ligados a quem vai resolver.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
