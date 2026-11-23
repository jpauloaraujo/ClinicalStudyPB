# T96 · Consulta agregada do histórico no backend

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Seg 16/11  
> **Depende de:** T95  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar a consulta que junta visitas previstas, visitas agendadas e resultados de um participante, ordenada pelo dia previsto, calcular o resumo no servidor e expor o endpoint com autorização de leitura, sem o problema N+1.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Entidades e repositórios: `VisitaPlanejada` (e `VisitaPlanejadaRepository`), `VisitaAgendada` (T73), `ResultadoAtividade` (T87), `Acao`, `Participacao`/`Candidato`. Os resultados da T89 já estão na `main` e entram na consulta.
- O padrão de autorização e de paginação dos controllers existentes (`Permissoes.LEITURA`, `@Tag`, `@Operation`); o serviço não grava nada.
- Os DTOs vêm da T95; o histórico mostra **todas** as visitas previstas do estudo da participação.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/HistoricoParticipanteService.java` | Criar | `historico(participacaoId)` e o cálculo do resumo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Agendamentos da participação com a visita prevista |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ResultadoAtividadeRepository.java` | Alterar | Resultados da participação com a atividade |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaPlanejadaRepository.java` | Alterar | Visitas previstas do estudo ordenadas pelo dia previsto |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/HistoricoParticipanteController.java` | Criar | `GET /api/participacoes/{participacaoId}/historico` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso do histórico (10 operações) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T96-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Consulta agregada

- **RQ-T96-01** Montar o histórico com **no máximo 3 consultas** por requisição (visitas previstas do estudo; agendamentos da participação; resultados da participação), usando projeção ou `JOIN FETCH`, e juntar em memória por `visitaPlanejadaId` e `acaoId`: **nunca uma consulta por visita** (N+1).
- **RQ-T96-02** Devolver **um item para cada visita prevista** do estudo, ordenado por dia previsto (e `ordem`); o item sem agendamento tem `situacao = NAO_AGENDADA` e os campos de agendamento nulos; o item com agendamento traz `visitaAgendadaId`, `dataHoraAgendada`, `dataHoraRealizada`, `motivo`, `observacao` e a situação convertida.
- **RQ-T96-03** Em cada visita, `resultados` lista os resultados registrados (nome da atividade, valor, unidade e observação), ordenados por nome da atividade; visita sem resultado devolve lista vazia.
- **RQ-T96-04** O bloco `participante` traz `participacaoId`, `candidatoId`, `codigo`, `nomeFicticio` e `dataInclusao`.

### Resumo calculado no servidor

- **RQ-T96-05** `previstas` (total de visitas previstas), `realizadas`, `ausentes`, `canceladas`, `agendadas` (apenas `AGENDADA`), `naoAgendadas` (sem agendamento) e `pendentes = agendadas + naoAgendadas`.
- **RQ-T96-06** Invariante: `previstas = realizadas + ausentes + canceladas + agendadas + naoAgendadas`; um teste garante isso para qualquer combinação.

### Autorização e erros

- **RQ-T96-07** `@PreAuthorize(Permissoes.LEITURA)` (pesquisador e coordenador); **403** para administrador e participante; **401** sem token; **404** (`RECURSO_NAO_ENCONTRADO`) para participação inexistente; formato único de erro.
- **RQ-T96-08** O endpoint é **somente leitura** (nenhum `POST`/`PUT`/`DELETE` neste recurso) e funciona com o estudo em qualquer estado.

### Documentação

- **RQ-T96-09** `@Tag(name = "Histórico do participante")` e `@Operation` descrevendo a regra 5; remover o `@Disabled("T96")` do `ContratoOpenApiSprint3Test`, atualizar a contagem (10) e corrigir o YAML **no mesmo Pull Request** se o Swagger divergir.
- **RQ-T96-10** Os resultados da retirada (marca `RETIRADO` e dados da retirada) são acrescentados pela **T104**: deixar o ponto de extensão comentado em `HistoricoParticipanteService`.

## 5. Contrato, regras e códigos de erro

- Operação 10 da seção 2 das convenções; regra 5.

## 6. Testes

- A bateria completa é a T98; nesta task escreva junto com o código o teste de fumaça do endpoint e do resumo (participante P4 do demo).

## 7. Critérios de aceite

- [ ] O histórico traz todas as visitas previstas, inclusive as não agendadas, com situação, datas e resultados, ordenado pelo dia previsto.
- [ ] O resumo é calculado no servidor e fecha com o total de previstas.
- [ ] A consulta não faz N+1 e a autorização está correta.

## 8. Cuidados e fora de escopo

- Não gravar nada neste serviço.
- Não expor entidades JPA na resposta.

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

> **T96 · Consulta agregada do histórico no backend.** Primeiro implemente a consulta que junta visitas previstas, visitas agendadas e resultados, ordenada pelo dia previsto, evitando uma consulta por visita (o problema N+1) com projeção ou fetch join. Depois calcule o resumo no servidor, incluindo os pendentes (previstas sem agendamento ou ainda agendadas). Em seguida aplique a autorização de leitura para pesquisador e coordenador (403 para os outros, 404 para participante inexistente). Por fim, documente no Swagger. Os resultados da T89 já estão na main e entram na consulta; a marca de retirada é acrescentada pela T104 (PBI-23).

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
