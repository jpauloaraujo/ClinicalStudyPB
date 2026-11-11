# T120 · Testes de backend e de componente

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Seg 23/11  
> **Depende de:** T116, T119  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar a ordenação e o filtro das duas consultas, a propriedade dos dados (o participante A nunca vê visita do B) e o participante retirado, no backend, e os estados e selos das telas do app com Jest e Testing Library, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Backend: `MeEstudosEndpointTest` e `VinculoContaEndpointTest` (T113) são o modelo (tokens reais, contas vinculadas); `CenarioRecrutamento` com `contaParticipante`, `vincularConta`, `agendar` e `registrarDesfecho`.
- Mobile: Jest e Testing Library (T108), mocks de `src/api/mocks/`, `visitas.test.tsx` iniciado na T117 e os testes de comparecimento da T118.
- Para ter visitas em todas as situações pela API, o teste agenda e registra desfechos (T78, T85) e retira um participante (T106).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MinhasVisitasServiceTest.java` | Criar | Ordenação, filtro e resumo (Mockito, `Clock` fixo) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MinhasVisitasEndpointTest.java` | Criar | Propriedade dos dados, retirado e permissões com tokens reais |
| `mobile/ClinicalStudyPB/src/app/__tests__/visitas.test.tsx` | Alterar | Telas de próximas e de histórico |
| `mobile/ClinicalStudyPB/src/components/__tests__/` | Criar | `CartaoVisita`, `ResumoComparecimento` e `Selo` |
| `mobile/ClinicalStudyPB/src/utils/__tests__/` | Alterar | Datas e comparecimento |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T120-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Backend: ordenação e filtro

- **RQ-T120-01** **Próximas:** só `AGENDADA` com data de agora em diante (visita agendada no passado ou de hoje já passada não aparece), ordenadas da **mais próxima para a mais distante**; visitas `REALIZADA`, `AUSENTE` e `CANCELADA` nunca aparecem em próximas.
- **RQ-T120-02** **Histórico:** só `REALIZADA`, `AUSENTE` e `CANCELADA`, da **mais recente para a mais antiga**; `AGENDADA` nunca aparece; **paginação** (`totalElementos`, `totalPaginas`, segunda página, `tamanho` 100 e inválido: 400 `PARAMETRO_INVALIDO`) e `resumo` correto e **independente da página**.
- **RQ-T120-03** Filtro por `estudoId`: com dois estudos, devolve só o do estudo pedido; `estudoId` de **outro** participante ou inexistente devolve **lista vazia** (200).
- **RQ-T120-04** Cada item traz `estudoTitulo`, `visitaNome`, as atividades previstas (nomes, ordenados), `dataHora` com deslocamento do fuso e **nenhum resultado** de avaliação; o histórico traz `motivo` nas canceladas.

### Backend: propriedade dos dados e retirado

- **RQ-T120-05** **O participante A nunca vê visita do B:** com A e B vinculados a participantes diferentes (e com visitas em todas as situações), cada um só vê as suas, em próximas, histórico e resumo; o teste cruza os dois e confere que nenhum id de B aparece na resposta de A.
- **RQ-T120-06** **Participante retirado:** `proximas` devolve vazio; `historico` mostra as visitas realizadas, ausentes e as **canceladas com o motivo "retirada do estudo"**, e o resumo conta as canceladas à parte.
- **RQ-T120-07** Participante **sem vínculo**: próximas e histórico vazios e resumo zerado (200).
- **RQ-T120-08** Permissões: **403** para administrador, pesquisador e coordenador; **401** sem token; nenhuma das duas operações aceita id de participante (o parâmetro `participacaoId` na URL não existe: tentar um caminho assim é 404).
- **RQ-T120-09** O número de consultas ao banco **não cresce** com o número de visitas (teste com 2 e com 10 visitas).
- **RQ-T120-10** Rodar também no job `Backend (PostgreSQL)` e conferir a cobertura de `MinhasVisitasService` (meta 95%).

### App: componentes

- **RQ-T120-11** **Lista de próximas:** com dados (cartões com data em português, visita, estudo e atividades), **vazia** ("Nenhuma visita agendada."), com **erro** (Tentar de novo repete a chamada) e sem conexão com a lista mantida.
- **RQ-T120-12** **Histórico:** com dados, selos de texto por situação, motivo nas canceladas, resumo ("comparecimento: 3 de 4 visitas"), vazio, erro e paginação.
- **RQ-T120-13** **Selos e cartões:** o selo mostra o texto de cada situação e o cartão tem `accessibilityLabel` completo; o formatador de datas e o cálculo de comparecimento já testados (T117, T118).
- **RQ-T120-14** `npm run lint`, `npm run typecheck` e `npm test` verdes no CI; registrar a cobertura do app se o Jest a reportar.

## 6. Testes

- As classes e arquivos da seção 3; o `ContratoOpenApiSprint3Test` verde com as **19** operações.
- Toda a suíte da Release 1 e dos PBIs anteriores continua passando.

## 7. Critérios de aceite

- [ ] A ordenação, o filtro e a paginação das duas consultas têm teste.
- [ ] Está provado que o participante A nunca vê a visita do B e que o retirado tem próximas vazias e canceladas no histórico.
- [ ] As telas do app têm testes de componente (lista, vazia, erro, selos) passando no CI.
- [ ] A cobertura de `domain` e `service` da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- O teste de propriedade dos dados é o mais importante da task.
- Não depender da data do dia: `Clock` fixo na unidade e datas relativas nos endpoints.

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

> **T120 · Testes de backend e de componente.** Escreva os testes junto com o código. Primeiro teste a ordenação e o filtro das duas consultas, a propriedade dos dados (o participante A nunca vê visita do B) e o participante retirado. Depois escreva os testes de componente das telas com Jest e Testing Library: lista com dados, lista vazia, erro e selos. Por fim, confira a cobertura da fatia.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
