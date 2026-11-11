# T120 · Testes de backend e de componente

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Seg 23/11  
> **Depende de:** T116, T119  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
