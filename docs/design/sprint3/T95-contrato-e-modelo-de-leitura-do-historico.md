# T95 · Contrato e modelo de leitura do histórico

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Seg 16/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Definir no contrato o endpoint de histórico da participação e os DTOs de leitura, **sem criar tabelas novas**: esta fatia só lê o que os outros PBIs gravam. Garantir que existe um participante de demonstração com histórico completo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O histórico junta o que os PBIs anteriores gravam: visitas previstas (Sprint 2), visitas agendadas (T73 e T76), desfechos (T81) e resultados (T89). Não há tabela nem migration nesta fatia.
- Regra 5: o histórico é só leitura para pesquisador e coordenador e mostra **todas** as visitas previstas do participante, inclusive as ainda não agendadas, com situação, datas e resultados.
- O formato do `HistoricoParticipanteResponse` está na seção 3 das convenções; o `ContratoOpenApiSprint3Test` e o YAML da Sprint 3 já existem (T74).
- Demonstração: o participante **P4** (estudo "Demo - Acompanhamento de participantes") tem Triagem realizada com resultado, Retorno 30 ausente e Retorno 60 cancelada; o **P3** tem todas as visitas não agendadas.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint3.yaml` | Alterar | Operação do histórico, schemas e erros |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/HistoricoParticipanteResponse.java` | Criar | Record com os blocos `participante`, `resumo` e `visitas` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/SituacaoNoHistorico.java` | Criar | `NAO_AGENDADA`, `AGENDADA`, `REALIZADA`, `AUSENTE`, `CANCELADA` |
| `web/ClinicalStudyPB/src/api/schema-sprint3.d.ts` | Alterar | Regenerado |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Alterar | Reexporta os tipos do histórico |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Contagem (10 operações) e caso `@Disabled("T96")` |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_22_historico.sql` | Criar (se faltar) | Só se o participante P4 não tiver o histórico completo |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T95-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Contrato

- **RQ-T95-01** Descrever `GET /api/participacoes/{participacaoId}/historico` (leitura: pesquisador e coordenador) com resposta 200 `HistoricoParticipanteResponse`, 403, 404 e 401.
- **RQ-T95-02** Declarar os schemas: `HistoricoParticipanteResponse` (`participante`, `resumo`, `visitas`), `ParticipanteDoHistorico` (`participacaoId`, `candidatoId`, `codigo`, `nomeFicticio`, `dataInclusao`), `ResumoDoHistorico` (`previstas`, `realizadas`, `ausentes`, `canceladas`, `agendadas`, `naoAgendadas`, `pendentes`), `VisitaDoHistorico` (`visitaPlanejadaId`, `nome`, `ordem`, `diaPrevisto`, `tolerancia`, `dataSugerida`, `situacao` com o enum `NAO_AGENDADA`/`AGENDADA`/`REALIZADA`/`AUSENTE`/`CANCELADA`, `visitaAgendadaId`, `dataHoraAgendada`, `dataHoraRealizada`, `motivo`, `observacao`, `resultados`) e `ResultadoDoHistorico` (`acaoId`, `nome`, `valor`, `unidade`, `observacao`), com o exemplo fictício das convenções.
- **RQ-T95-03** Documentar: para **cada visita prevista** há exatamente um item (ordenado por `ordem`/dia previsto), mesmo sem agendamento (`situacao = NAO_AGENDADA`, campos de agendamento nulos); `pendentes = agendadas + naoAgendadas` (previstas sem agendamento ou ainda agendadas); a marca de retirada (`situacao` e `retirada` em `participante`) é acrescentada pela T104.
- **RQ-T95-04** Atualizar a contagem de operações do `ContratoOpenApiSprint3Test` (9 + 1 = 10), com o novo caso `@Disabled("T96")`.

### DTOs de leitura

- **RQ-T95-05** Criar os records em `controller/dto` (um arquivo por DTO público, ou `HistoricoParticipanteResponse` com records aninhados, no estilo de `AvaliacaoResponse`), com `@Schema` nos campos.
- **RQ-T95-06** Datas e horas: `OffsetDateTime` na saída (fuso da seção 1, ajuste 6); datas puras como `LocalDate`.
- **RQ-T95-07** Criar o enum `SituacaoNoHistorico` (o mesmo vocabulário de `SituacaoVisita` mais `NAO_AGENDADA`), com método de conversão a partir de `SituacaoVisita`.
- **RQ-T95-08** Gerar os tipos (`npm run api:tipos`), reexportá-los em `src/api/tipos.ts` e conferir `tsc -b` e lint.

### Dados de demonstração

- **RQ-T95-09** Conferir que o participante **P4** tem o histórico completo (Triagem `REALIZADA` com resultado, Retorno 30 `AUSENTE`, Retorno 60 `CANCELADA` com motivo) e o **P3**, todas as visitas não agendadas; se algo faltar, acrescentar em `R__demo_22_historico.sql` (ids `d6300000-0000-4000-8000-0000000022NN`, datas relativas, `NOT EXISTS`, testado em H2 e PostgreSQL).
- **RQ-T95-10** Atualizar `DemoDataIntegrationTest` se houver script novo.

## 5. Contrato, regras e códigos de erro

- Operação 10 da seção 2 das convenções.
- Sem tabela nova e sem migration: a fatia lê o que os outros PBIs gravam.

## 6. Testes

- `ContratoOpenApiSprint3Test` (caso novo desabilitado até a T96), `tsc -b` e `DemoDataIntegrationTest` (se houver script).

## 7. Critérios de aceite

- [ ] O YAML descreve o endpoint e os DTOs com exemplo fictício e códigos de erro.
- [ ] Os tipos TypeScript compilam.
- [ ] Existe participante de demonstração com histórico completo e outro sem visitas agendadas.

## 8. Cuidados e fora de escopo

- Não criar tabelas nem endpoints de escrita nesta fatia.
- Não incluir os dados de retirada agora (T104).

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
| 5 | O histórico é só leitura para pesquisador e coordenador e mostra todas as visitas previstas do participante, inclusive as ainda não agendadas, com situação, datas e resultados | T95, T96, T97 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T95 · Contrato e modelo de leitura do histórico.** Primeiro defina no contrato o endpoint de histórico da participação: para cada visita prevista, a situação (não agendada, agendada, realizada, ausente ou cancelada), as datas e a lista de resultados, mais o resumo com a contagem de cada situação. Depois escreva os DTOs de leitura, sem criar tabelas novas: esta fatia só lê o que os outros PBIs gravam. Por fim, escreva os dados de demonstração de um participante com histórico completo, usando a tabela da T73 e os dados de demonstração das T74, T84 e T90.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
