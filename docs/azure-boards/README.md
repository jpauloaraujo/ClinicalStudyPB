# Backlog da Sprint 2 para o Azure Boards

[sprint2-backlog.csv](sprint2-backlog.csv) reúne a Sprint 2 do planejamento oficial ([Planejamento_da_Sprint_2_ClinicalStudyPB.md](../sprint2/Planejamento_da_Sprint_2_ClinicalStudyPB.md)) no formato de importação do Azure Boards: **10 PBIs (PBI-09 a PBI-18), 37 tasks e 143 horas**. O arquivo foi gerado a partir do planejamento e a contagem foi conferida na geração.

> Isto é só a **preparação**: a importação precisa ser feita por quem tem acesso ao projeto no Azure DevOps (I2).

## Como importar

1. Em **Project Settings > Project configuration > Iterations**, crie a iteração **Sprint 2** com início em 27/10/2026 e fim em 09/11/2026. Se o caminho completo for outro (por exemplo `ClinicalStudyPB\Sprint 2`), ajuste a coluna `Iteration Path` do CSV.
2. Em **Boards > Backlogs**, abra o menu **Column options / Import work items** (ou **Work items > Import work items**) e escolha o CSV.
3. Confira a prévia da importação e clique em **Import**.

## Colunas do arquivo

| Coluna | Conteúdo |
|---|---|
| `Work Item Type` | `Product Backlog Item` (PBI) ou `Task` |
| `Title 1` / `Title 2` | Nível do item: o PBI usa `Title 1` e cada task, `Title 2`, o que as cria como filhas do PBI anterior |
| `Iteration Path` | `Sprint 2` |
| `Priority` | PBIs: 1 (Alta) ou 2 (Média, só o PBI-12). Tasks em branco |
| `Original Estimate` e `Remaining Work` | Horas de cada task (soma 143 h) |
| `Tags` | `Sprint2` e, nas tasks, o responsável (`I1` a `I7`) |
| `Description` | PBI: critério de aceite. Task: responsável, prazo, dependências e os passos do planejamento |

## O que ainda é manual

- **Assigned To:** o planejamento identifica as pessoas por `I1` a `I7`. Depois da importação, atribua cada task ao integrante correspondente (o código fica na tag).
- **Dependências:** o CSV não cria vínculos. As dependências de cada task estão na descrição; registre-as como **Predecessor** e **Successor** (aba Links de cada task).
- **Capacidade:** preencha a disponibilidade dos sete integrantes em **Sprints > Capacity** (12 h por semana, conforme o planejamento).
- **Effort do PBI:** o planejamento estima as horas nas tasks; o campo Effort serve para a estimativa relativa.
- **Conferência:** o Taskboard deve mostrar todas as tasks por responsável e o burndown deve partir de 143 h.
