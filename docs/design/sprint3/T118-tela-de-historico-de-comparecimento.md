# T118 · Tela de histórico de comparecimento

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Seg 23/11  
> **Depende de:** T117  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a aba (ou tela) do histórico de comparecimento com as visitas realizadas, ausentes e canceladas, com selo de texto e cor, o resumo de comparecimento no topo e o motivo nas canceladas.

## 2. Contexto: o que já existe e deve ser reaproveitado

- **M06 Visitas — Histórico** do protótipo: lista de visitas passadas com status (realizada, ausente, cancelada) e **resumo de comparecimento**. A aba Visitas e o `CartaoVisita` vêm da T117; a API (`/api/me/visitas/historico`, paginada, com `resumo`) da T116.
- Regra 9: o histórico reúne `REALIZADA`, `AUSENTE` e `CANCELADA`, da mais recente para a mais antiga.
- O resumo de comparecimento é calculado **a partir das realizadas e das ausentes, sem contar as canceladas** (o cancelamento não foi falta do participante, sobretudo na retirada).
- Selos: `Selo` da T108 (texto sempre visível).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/app/(tabs)/visitas.tsx` | Alterar | Conteúdo da parte **Histórico** |
| `mobile/ClinicalStudyPB/src/components/ResumoComparecimento.tsx` | Criar | Resumo no topo ("comparecimento: 3 de 4 visitas") |
| `mobile/ClinicalStudyPB/src/components/CartaoVisita.tsx` | Alterar | Mostra motivo e data real quando existirem |
| `mobile/ClinicalStudyPB/src/api/visitas.ts` | Alterar | `visitasApi.historico` |
| `mobile/ClinicalStudyPB/src/utils/comparecimento.ts` | Criar | Cálculo do resumo e do texto |
| `mobile/ClinicalStudyPB/src/utils/__tests__/comparecimento.test.ts` | Criar | Testes do cálculo |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T118-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Lista do histórico

- **RQ-T118-01** Na parte **Histórico** da aba Visitas, mostrar as visitas **realizadas, ausentes e canceladas**, da mais recente para a mais antiga, com `Selo` **de texto e cor** (Realizada, Ausente, Cancelada), data e hora (em português, no fuso do aparelho), nome da visita e estudo.
- **RQ-T118-02** Nas **canceladas**, mostrar o **motivo** ("Motivo: retirada do estudo"); nas realizadas, a data e hora reais quando existirem.
- **RQ-T118-03** **Paginação** por rolagem infinita (carregar a próxima página ao chegar ao fim da lista) ou botão "Carregar mais", sem duplicar itens; **rolagem puxando para atualizar** recarrega do início.
- **RQ-T118-04** Estado **vazio** com texto claro: "Nenhuma visita no histórico ainda."; carregamento e erro com **Tentar de novo** como na T117/T119.
- **RQ-T118-05** Parâmetro `estudoId` (atalho do detalhe do estudo) filtra o histórico, como nas próximas.

### Resumo de comparecimento

- **RQ-T118-06** No topo da lista, o resumo **"comparecimento: X de Y visitas"**, com X = realizadas e Y = realizadas + ausentes (**as canceladas não entram**), usando o `resumo` do servidor (independente da página carregada). Sem visitas realizadas nem ausentes, mostrar "Sem visitas contabilizadas ainda" (sem dividir por zero).
- **RQ-T118-07** Opcionalmente, o percentual ao lado ("75%"); em qualquer caso o texto completo é o que importa para a acessibilidade.
- **RQ-T118-08** O cálculo fica em `utils/comparecimento.ts`, testado isoladamente: 3 realizadas, 1 ausente e 5 canceladas resulta em "3 de 4"; 0 e 0 resulta em "sem visitas contabilizadas".
- **RQ-T118-09** **Item de corte** (ordem de corte do planejamento): o resumo de comparecimento é o primeiro item do app a sair se faltar capacidade; a lista do histórico não pode ser cortada.

### Acessibilidade

- **RQ-T118-10** Selo e resumo sempre com texto; `accessibilityLabel` em cada cartão e no resumo ("Comparecimento: 3 de 4 visitas"); alvos de toque de pelo menos 44 pontos.

## 5. Contrato, regras e códigos de erro

- Consome a operação 19 da seção 2 das convenções (`HistoricoMinhasVisitasResponse`).

## 6. Testes

- `comparecimento.test.ts` e os testes de componente do histórico (lista, motivo nas canceladas, resumo, vazio, paginação); a bateria completa é a T120.

## 7. Critérios de aceite

- [ ] O participante vê o histórico de visitas realizadas, ausentes e canceladas, da mais recente para a mais antiga, com selo em texto e cor e o motivo nas canceladas.
- [ ] O resumo de comparecimento conta realizadas e ausentes e ignora as canceladas.
- [ ] O estado vazio tem texto claro.

## 8. Cuidados e fora de escopo

- Cancelada não conta como falta: não entra no resumo de comparecimento.
- Sem resultados das avaliações nem ações do RF19 nesta tela.

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

> **T118 · Tela de histórico de comparecimento.** Primeiro crie a aba ou tela de histórico com a lista de visitas realizadas, ausentes e canceladas, cada uma com selo de texto e cor. Depois acrescente o resumo no topo (por exemplo, "comparecimento: 3 de 4 visitas"), calculado a partir das realizadas e ausentes, sem contar as canceladas. Por fim, mostre o motivo nas canceladas e trate lista vazia.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
