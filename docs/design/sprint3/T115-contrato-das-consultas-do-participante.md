# T115 · Contrato das consultas do participante

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 2h · **Prazo:** Dom 22/11  
> **Depende de:** T73, T109  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Descrever no contrato os dois endpoints do participante logado (próximas visitas e histórico de comparecimento), ambos sob `/me` e sem receber o id do participante, definir o formato de cada visita e gerar os tipos TypeScript para o app.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/api/openapi-sprint3.yaml` já descreve 17 operações (PBIs 19 a 24, incluindo os `GET /api/me/estudos` da T109 e o vínculo da conta da T111); esta task acrescenta as duas últimas (17 mais 2 = 19) e o teste de contrato espera 19 operações ao fim da Sprint.
- O formato de `MinhaVisitaResponse` e de `HistoricoMinhasVisitasResponse` está na seção 3 das convenções. As visitas já têm `dataHora` com deslocamento (ISO 8601), a situação e o motivo (T73, T81).
- Regras: 7 (o participante só vê o próprio dado; devolve vazio, nunca de outro), 9 (próximas = `AGENDADA` de hoje em diante, da mais próxima para a mais distante; histórico = `REALIZADA`, `AUSENTE` e `CANCELADA`, da mais recente para a mais antiga) e o escopo do RF18: **sem resultados** das avaliações.
- O app gera tipos com `npm run api:tipos` (T108) e consome o contrato pelos módulos de `src/api/`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint3.yaml` | Alterar | Duas operações `/me/visitas/**`, schemas e erros |
| `mobile/ClinicalStudyPB/src/api/schema.d.ts` | Alterar | Regenerado (`npm run api:tipos`) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Contagem final (19 operações) e casos `@Disabled("T116")` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T115-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Operações

- **RQ-T115-01** `GET /api/me/visitas/proximas` (só `PARTICIPANTE`; parâmetro opcional `estudoId`): lista as visitas `AGENDADA` com `dataHora` de agora em diante, da **mais próxima para a mais distante**; resposta 200 `List<MinhaVisitaResponse>`.
- **RQ-T115-02** `GET /api/me/visitas/historico` (só `PARTICIPANTE`; parâmetros opcionais `estudoId`, `pagina` e `tamanho`, padrão 0 e 20, máximo 100): visitas `REALIZADA`, `AUSENTE` e `CANCELADA`, da **mais recente para a mais antiga**, **paginadas**, com o resumo de comparecimento; resposta 200 `HistoricoMinhasVisitasResponse`.
- **RQ-T115-03** Nenhuma das duas recebe o **id do participante**: ele vem do token (regra 7). Perfis: só participante (403 para os demais, 401 sem token).

### Formato das visitas

- **RQ-T115-04** `MinhaVisitaResponse`: `id`, `participacaoId`, `estudoId`, `estudoTitulo`, `visitaNome` (nome da visita prevista), `dataHora` (**data e hora com fuso, ISO 8601**, ex.: `2026-11-24T09:30:00-03:00`), `situacao` (`SituacaoVisita`), `dataHoraRealizada` (anulável), `motivo` (anulável; preenchido nas canceladas) e `atividades` (**lista com os nomes das atividades previstas**, ordenada por nome).
- **RQ-T115-05** **Sem resultados** das avaliações: o RF18 não os inclui, então o schema não tem campo de resultado (é uma decisão de privacidade e de escopo).
- **RQ-T115-06** `HistoricoMinhasVisitasResponse`: `resumo` (`realizadas`, `ausentes`, `canceladas`, totais do participante, **independentes da página**), `conteudo` (lista de `MinhaVisitaResponse`), `pagina`, `tamanho`, `totalElementos` e `totalPaginas`.
- **RQ-T115-07** Exemplos fictícios para os dois (participante com visita agendada, realizada, ausente e cancelada com motivo "retirada do estudo").

### Erros, tipos e teste de contrato

- **RQ-T115-08** Documentar `400` (`PARAMETRO_INVALIDO` para `pagina`/`tamanho`/`estudoId` inválidos), `401`, `403` e o fato de que um `estudoId` que **não é do participante** devolve **lista vazia**, nunca 404 nem dados de outro (regra 7).
- **RQ-T115-09** Rodar `npm run api:tipos` no mobile, conferir `npm run typecheck` e `npm run lint`; os tipos `MinhaVisitaResponse` e `HistoricoMinhasVisitasResponse` ficam disponíveis para as T117 e T118.
- **RQ-T115-10** Atualizar a contagem do `ContratoOpenApiSprint3Test` para **19** operações com os casos novos `@Disabled("T116")`.

## 5. Contrato, regras e códigos de erro

- Operações 18 e 19 da seção 2 das convenções; regras 7 e 9.

## 6. Testes

- `ContratoOpenApiSprint3Test` (casos novos desabilitados até a T116) e `npm run typecheck` com os tipos regenerados.

## 7. Critérios de aceite

- [ ] O YAML descreve as duas operações com DTOs, exemplos, ordenação, paginação e erros.
- [ ] Os tipos TypeScript são gerados e compilam no app.

## 8. Cuidados e fora de escopo

- Não colocar resultados de avaliações nem dados de candidato nos DTOs.
- Não receber id de participante em nenhuma das duas operações.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF18** (Módulo E — Aplicativo do participante; interface Mobile): “Exibir as próximas visitas e seu histórico de comparecimento.”

### 9.2 Critério de aceite do PBI (planejamento)

> O participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T115 · Contrato das consultas do participante.** Primeiro descreva no contrato os dois endpoints do participante logado, próximas visitas e histórico, ambos sob /me e sem receber o id do participante, que vem do token. Depois defina o formato de cada visita: id, estudo, nome da visita prevista, data e hora com fuso (ISO 8601), situação e lista com os nomes das atividades previstas, sem os resultados, que não fazem parte do RF18. Por fim, gere os tipos TypeScript desse contrato para o app.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
