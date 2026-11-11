# T95 · Contrato e modelo de leitura do histórico

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Seg 16/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
