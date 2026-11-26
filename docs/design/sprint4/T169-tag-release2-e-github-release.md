# T169 · Tag release2 e GitHub Release

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Seg 07/12 (antes da apresentação)  
> **Depende de:** T168  
> **Situação:** 📋 Entrega fora do código (especificada neste documento)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a tag `release2` e a GitHub Release sobre o commit candidato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Commit indicado na T163; a avaliação usa a versão da tag.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `releases/release2/README.md` | Alterar | Última conferência antes da tag |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T169-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T169-01** Confirmar CI verde na `main` no commit a marcar, incluindo os seis jobs.
- **RQ-T169-02** Confirmar que o commit contém o README principal e o da release (T167 e T168); se for posterior ao candidato da T163, só com mudanças de documentação, registradas.
- **RQ-T169-03** Criar a tag anotada `release2` nesse commit e enviá-la.
- **RQ-T169-04** Criar a GitHub Release "Release 2" com resumo (RF12 a RF22) e link para `releases/release2/README.md`.
- **RQ-T169-05** Abrir a release e o link a partir de um navegador para conferir.
- **RQ-T169-06** Fazer um clone da tag e subir com Docker Compose.
- **RQ-T169-07** Não mover nem recriar a tag depois; nada é alterado nela.

## 5. Contrato, regras e códigos de erro

Entregas, "Identificação da entrega".

## 6. Testes

Clone da tag e `docker compose up --build`.

## 7. Critérios de aceite

- [ ] Tag e GitHub Release publicadas antes da apresentação.

## 8. Cuidados e fora de escopo

- Publicar tag e release é ação irreversível na prática: confirmar o commit com I1 antes.

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

> **T169 · Tag release2 e GitHub Release.** Primeiro confirme que a branch principal está verde no CI e que o commit é o candidato indicado na T163. Depois crie a tag release2 e a GitHub Release, com o resumo e o link para releases/release2/README.md. A avaliação usa a versão da tag, então nada é alterado depois dela.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
