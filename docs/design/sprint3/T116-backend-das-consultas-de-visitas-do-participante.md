# T116 · Backend das consultas de visitas do participante

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 4h · **Prazo:** Dom 22/11  
> **Depende de:** T73, T109, T115  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar as consultas das próximas visitas e do histórico de comparecimento do participante logado, garantindo que só aparecem as visitas das participações da conta logada (regra 7) e tratando o participante retirado. Usa o componente de acesso da T109, já entregue pelo PBI-24.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AcessoParticipante` (T109) resolve as participações da conta logada; `MeController` e `Permissoes.PARTICIPANTE` (T109) são a base dos endpoints `/api/me/**`.
- `VisitaAgendadaRepository` (T73) e as consultas da agenda (T76); `VisitaPlanejada`/`Acao` (atividades previstas); `Estudo` (título).
- O retirado (T101 a T105): as visitas futuras são canceladas com o motivo "retirada do estudo", então o participante retirado tem as próximas **vazias** e as canceladas no histórico, com o motivo.
- Dados de demonstração: P1 (vinculado a `participante@clinicalstudy.test`) tem visita realizada e visita agendada; P6 (`demo.participante@clinicalstudy.test`) é retirado com visita cancelada.
- O bean `Clock` (T75) e o fuso `America/Fortaleza` definem "agora" e o deslocamento das datas na resposta.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/MinhasVisitasService.java` | Criar | `proximas(usuarioId, estudoId)` e `historico(usuarioId, estudoId, pagina, tamanho)` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Consultas por conjunto de participações, situação e período |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/MeController.java` | Alterar | `GET /api/me/visitas/proximas` e `/historico` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/` | Criar | `MinhaVisitaResponse`, `ResumoComparecimentoResponse`, `HistoricoMinhasVisitasResponse` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita os casos (19 operações) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Só se o Swagger divergir (corrige no mesmo PR) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T116-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Próximas visitas

- **RQ-T116-01** `proximas`: visitas `AGENDADA` com `dataHoraAgendada` **de agora em diante** (`Clock`), das participações da conta logada, ordenadas da **mais próxima para a mais distante**; filtro opcional por `estudoId`.
- **RQ-T116-02** Cada item traz `id`, `participacaoId`, `estudoId`, `estudoTitulo`, `visitaNome`, `dataHora` com deslocamento, `situacao` e a lista dos **nomes das atividades previstas** da visita (ordenada por nome); sem resultados.
- **RQ-T116-03** Participante **retirado**: devolve lista **vazia** (as futuras foram canceladas); participante sem vínculo: lista vazia.

### Histórico de comparecimento

- **RQ-T116-04** `historico`: visitas `REALIZADA`, `AUSENTE` e `CANCELADA` das participações da conta logada, da **mais recente para a mais antiga** (por `dataHoraAgendada` decrescente), com **paginação** (`pagina`/`tamanho`, padrão 0 e 20, máximo 100) e filtro opcional por `estudoId`.
- **RQ-T116-05** Cada item traz também `dataHoraRealizada` (realizadas) e `motivo` (**canceladas**, inclusive o "retirada do estudo" do participante retirado).
- **RQ-T116-06** `resumo` com `realizadas`, `ausentes` e `canceladas` do participante (no filtro de `estudoId`, daquele estudo), calculado no servidor e **independente da página** pedida.

### Propriedade dos dados (regra 7)

- **RQ-T116-07** Todas as consultas partem das participações devolvidas por `AcessoParticipante.participacoesDe(usuarioId)`; **nunca** de um id informado pelo cliente.
- **RQ-T116-08** Um `estudoId` que **não pertence ao participante** devolve **lista vazia** (200), não 404 e nunca dados de outro participante; um participante **sem vínculo** recebe lista vazia e resumo zerado.
- **RQ-T116-09** `MeController` já restringe ao perfil participante: administrador, pesquisador e coordenador recebem **403**; sem token, **401**; `pagina`/`tamanho`/`estudoId` inválidos: **400** `PARAMETRO_INVALIDO`.

### Qualidade

- **RQ-T116-10** Carregar visitas, estudos e atividades com **um número fixo de consultas** (projeção ou `JOIN FETCH`), sem N+1: o número de consultas não cresce com o número de visitas.
- **RQ-T116-11** Datas na resposta como `OffsetDateTime` no fuso configurado.
- **RQ-T116-12** `@Tag(name = "Participante (me)")` e `@Operation` com a descrição da ordenação e da paginação; remover os `@Disabled("T116")` do `ContratoOpenApiSprint3Test` (19 operações) e corrigir o YAML **no mesmo Pull Request** se o Swagger divergir.

## 5. Contrato, regras e códigos de erro

- Operações 18 e 19 da seção 2 das convenções; regras 7 e 9.

## 6. Testes

- A bateria completa é a T120; aqui, o `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada endpoint com os dados de demonstração.

## 7. Critérios de aceite

- [ ] O participante logado vê as suas próximas visitas e o seu histórico, ordenados e paginados como definido.
- [ ] Nunca aparecem visitas de outro participante; o retirado tem próximas vazias e canceladas com o motivo no histórico.
- [ ] O Swagger e o YAML coincidem.

## 8. Cuidados e fora de escopo

- Nenhum parâmetro de id de participante: só o token.
- Sem resultados das avaliações nas respostas.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF18** (Módulo E — Aplicativo do participante; interface Mobile): “Exibir as próximas visitas e seu histórico de comparecimento.”

### 9.2 Critério de aceite do PBI (planejamento)

> O participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 7 | A conta do participante é um usuário com perfil participante, criado pelo administrador (RF01) e vinculado pelo coordenador a um candidato já incluído, na relação um para um (o campo opcional previsto na regra 8 da Sprint 2). O participante só enxerga os próprios dados: acessar dados de outro retorna 404 | T109, T111, T116 |
| 9 | No app, "próximas visitas" são as AGENDADA de hoje em diante, da mais próxima para a mais distante; o "histórico de comparecimento" reúne REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga | T116, T117, T118 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T116 · Backend das consultas de visitas do participante.** Esta task usa o componente de acesso da T109, já entregue pelo PBI-24. Primeiro implemente a consulta das próximas visitas: AGENDADA com data de agora em diante, da mais próxima à mais distante. Depois implemente o histórico: REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga, com paginação. Em seguida garanta que só aparecem as visitas das participações da conta logada (regra 7), devolvendo vazio, e nunca dados de outro. Por fim, trate o participante retirado (visitas canceladas aparecem no histórico com o motivo, e as próximas ficam vazias), documente no Swagger e confira contra o YAML.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Entregas: “Tratamento de erros e validações das regras de negócio.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
