# T117 · Tela de próximas visitas

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 4h · **Prazo:** Dom 22/11  
> **Depende de:** T108, T115, T116  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar, sobre o esqueleto da T108 e a API da T116, a lista de cartões das próximas visitas na aba Visitas, com data e hora em português, o detalhe da visita, a rolagem para atualizar e o estado de lista vazia, formatando as datas no fuso do aparelho.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A aba `src/app/(tabs)/visitas.tsx` hoje mostra `EmBreve`. O protótipo (`docs/sprint1/prototipos-mobile.md`) define **M05 Visitas — Próximas** (lista ordenada por data: data e hora, nome da visita, estudo, status; estado vazio "Nenhuma visita agendada") e **M07 Detalhe da visita** (data e hora, atividades previstas, status atual). As ações "Confirmar presença" e "Solicitar reagendamento" (RF19) são da **Sprint 4**.
- Do esqueleto da T108: cliente HTTP, tipos gerados (`MinhaVisitaResponse`), mocks, `Carregando`, `EstadoErro`, `EstadoVazio`, `Selo`, `Cartao`, `Tela`; a tela `visitas/[visitaId]` já está declarada no `_layout.tsx`.
- O atalho "Ver visitas deste estudo" do detalhe do estudo (T112) navega para a aba Visitas com `estudoId`; esta task trata o parâmetro.
- As datas chegam com deslocamento (`2026-11-24T09:30:00-03:00`) e precisam ser exibidas no **fuso do aparelho**, em português.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/api/visitas.ts` | Criar | `visitasApi.proximas(estudoId?)` e `.historico(...)` (T118) |
| `mobile/ClinicalStudyPB/src/utils/datas.ts` | Criar | `formatarDataHora`, `formatarData` e `diaDaSemana` em pt-BR |
| `mobile/ClinicalStudyPB/src/app/(tabs)/visitas.tsx` | Alterar | Abas Próximas e Histórico (a de histórico é a T118) |
| `mobile/ClinicalStudyPB/src/components/CartaoVisita.tsx` | Criar | Cartão da visita reutilizado nas duas listas |
| `mobile/ClinicalStudyPB/src/app/visitas/[visitaId].tsx` | Criar | Detalhe da visita (M07 sem ações) |
| `mobile/ClinicalStudyPB/src/utils/__tests__/datas.test.ts` | Criar | Testes do formatador |
| `mobile/ClinicalStudyPB/src/app/__tests__/visitas.test.tsx` | Criar | Testes de componente (completados na T120) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T117-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Lista de próximas visitas (M05)

- **RQ-T117-01** Na aba **Visitas**, um seletor de duas partes (**Próximas** e **Histórico**; o conteúdo do histórico é a T118) com **Próximas** selecionada por padrão; a lista mostra **cartões** com **data e hora em português** (por exemplo "ter, 24/11 às 09:30"), **nome da visita**, **estudo** e as **atividades previstas** (nomes), na ordem devolvida pela API (da mais próxima para a mais distante).
- **RQ-T117-02** Tocar no cartão abre o **detalhe da visita** (`/visitas/{visitaId}`).
- **RQ-T117-03** Estado **vazio** com texto claro: "Nenhuma visita agendada." (e, para o participante sem estudo ou retirado, o texto continua correto); estados de **carregamento** e de **erro** com **Tentar de novo** são da T119, mas a tela já usa os componentes base.
- **RQ-T117-04** **Rolagem puxando para atualizar** (`RefreshControl`) recarrega a lista.
- **RQ-T117-05** Parâmetro `estudoId` (vindo do atalho do detalhe do estudo): filtra a lista e mostra o nome do estudo no topo com a opção de ver todas.

### Detalhe da visita (M07)

- **RQ-T117-06** Mostrar **data e hora**, **nome da visita**, **estudo**, as **atividades previstas** (lista) e o **status atual** em `Selo` com texto; **sem os botões** de confirmar presença e reagendar (RF19, Sprint 4), **sem resultados**.
- **RQ-T117-07** O detalhe usa os dados do cartão (passados pela navegação) e não precisa de endpoint novo; se a tela for aberta sem eles (link direto), mostra "Visita não encontrada." e o botão de voltar.

### Datas no fuso do aparelho (`utils/datas.ts`)

- **RQ-T117-08** `formatarDataHora(iso: string)` converte a data com deslocamento para o **fuso do aparelho** e devolve "ter, 24/11 às 09:30" (dia da semana abreviado em português, `dd/MM` e `HH:mm`); `formatarData(iso)` devolve "24/11/2026". Funcionam no Hermes (Android e iOS) e no navegador; datas inválidas devolvem "—" sem quebrar a tela.
- **RQ-T117-09** Testes do formatador com datas de exemplo (com deslocamento `-03:00` e com `Z`) fixando o fuso do teste (variável `TZ`) para o resultado não depender da máquina.

### Acessibilidade

- **RQ-T117-10** Alvos de toque de pelo menos 44 pontos; cada cartão com `accessibilityRole="button"` e `accessibilityLabel` completo ("Visita Retorno 30 dias, terça-feira, 24 de novembro às 09:30, estudo X, agendada"); o selo de situação **nunca depende só de cor**.
- **RQ-T117-11** Textos legíveis (tamanho e contraste do tema) e rolagem funcional com fonte grande do sistema.

## 5. Contrato, regras e códigos de erro

- Consome a operação 18 da seção 2 das convenções (`MinhaVisitaResponse`).

## 6. Testes

- `datas.test.ts` (formatador) e `visitas.test.tsx` (lista com dados, vazia, pull-to-refresh, parâmetro `estudoId`, detalhe e ausência dos botões do RF19); a bateria completa é a T120.

## 7. Critérios de aceite

- [ ] O participante logado vê as suas próximas visitas em português, na ordem certa, com estudo e atividades.
- [ ] O detalhe da visita mostra data, nome, estudo, atividades e situação, sem ações do RF19.
- [ ] Datas no fuso do aparelho; a lista vazia tem texto claro.

## 8. Cuidados e fora de escopo

- Sem confirmar presença nem solicitar reagendamento (RF19).
- Sem mocks na entrega: a tela usa a API real da T116 (os mocks são só para teste e desenvolvimento).

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF18** (Módulo E — Aplicativo do participante; interface Mobile): “Exibir as próximas visitas e seu histórico de comparecimento.”

### 9.2 Critério de aceite do PBI (planejamento)

> O participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 9 | No app, "próximas visitas" são as AGENDADA de hoje em diante, da mais próxima para a mais distante; o "histórico de comparecimento" reúne REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga | T116, T117, T118 |

### 9.4 Texto original da task no planejamento

> **T117 · Tela de próximas visitas.** Primeiro crie, sobre o esqueleto da T108 e a API da T116, a lista de cartões na aba Visitas, com data e hora em português (por exemplo, "ter, 24/11 às 09:30"), nome da visita, estudo e atividades previstas. Depois acrescente o detalhe da visita, a rolagem puxando para atualizar e o estado de lista vazia com texto claro. Em seguida formate as datas no fuso do aparelho, a partir da data com fuso que vem da API. Por fim, deixe os alvos de toque e os textos acessíveis, sem depender só de cor.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
