# T99 · E2E do fluxo de acompanhamento completo

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Ter 17/11  
> **Depende de:** T76, T82, T90 e os dados de demonstração (T74, T84)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Escrever o E2E que percorre o acompanhamento de ponta a ponta (agendar, registrar a realização, registrar um resultado e conferir o histórico), rodar no CI com os dados de demonstração das outras fatias, abrir Bugs para o que falhar e deixar o relatório e as capturas prontos para o README.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Esta task lê o trabalho dos PBIs 19, 20, 21 e 22, já na `main`, então pode ser escrita de uma vez.
- Infraestrutura E2E e specs das T79, T86 e T93; `fluxo-completo.spec.ts` da Release 1 (modelo de fluxo com dois perfis e `test.step`).
- Dados de demonstração das T74, T84, T90 e do histórico (T95): o estudo "Demo - Acompanhamento de participantes" e P1 a P5. O E2E monta o próprio estado pela API (dados únicos por execução) e usa os dados de demonstração só para conferir o caso de leitura.
- O cenário da retirada fica na T107 (depois que o PBI-23 existir).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/tests/fluxo-acompanhamento.spec.ts` | Criar | O fluxo completo com dois perfis |
| `web/ClinicalStudyPB/e2e/pages/FichaParticipantePage.ts` | Criar | Page object da ficha e do histórico |
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | Reaproveita `participanteIncluido`, `agendar`, `registrarDesfecho` e `registrarResultado` |
| `.github/workflows/ci.yml` | Conferir | O job E2E já roda a suíte inteira; confirmar que o novo spec entra |
| `docs/imagens/sprint3/` | Alterar | Capturas do histórico |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T99-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cenário (`fluxo-acompanhamento.spec.ts`)

- **RQ-T99-01** **Passo 1 (partida pela API):** estudo em recrutamento, participante incluído e visita prevista com atividade de avaliação cuja janela contém hoje; o participante ainda não tem visita agendada.
- **RQ-T99-02** **Passo 2 (coordenador):** abre Participantes, entra no agendamento, **agenda** a visita e, na tela da visita, **registra que foi realizada**.
- **RQ-T99-03** **Passo 3 (pesquisador, outro contexto de navegador):** abre a visita, aba Avaliações, **registra o resultado** de uma avaliação.
- **RQ-T99-04** **Passo 4 (os dois perfis):** abrem a ficha do participante e o histórico mostra a visita **realizada** com o resultado, os cartões do resumo corretos (1 realizada, 0 ausentes, 0 canceladas, 0 pendentes se só havia uma visita prevista) e a ordem das visitas.
- **RQ-T99-05** Conferir que o **coordenador** vê os resultados em modo somente leitura no histórico e que o **pesquisador** não tem botões de agendamento.
- **RQ-T99-06** Usar `test.step` para cada passo e `capturar(page, nome)` nos pontos-chave.

### Execução no CI e defeitos

- **RQ-T99-07** Rodar o spec no job E2E do CI (PostgreSQL, backend e web reais) e localmente com `--repeat-each` para provar que não é instável e que pode ser repetido no mesmo banco.
- **RQ-T99-08** Abrir **Bugs** no Azure Boards, ligados à task de origem, para o que falhar, e avisar o dono do PBI correspondente.

### Relatório e capturas

- **RQ-T99-09** Salvar `rf15-historico`, `rf15-historico-resumo` e a captura do fluxo; copiar as finais para `docs/imagens/sprint3/` e deixar o relatório do Playwright disponível para o README.
- **RQ-T99-10** Atualizar o `README.md` com o RF15 (histórico) e o fluxo de acompanhamento.

## 6. Testes

- `fluxo-acompanhamento.spec.ts` no CI e localmente (`--repeat-each 3`); E2E dos PBIs anteriores e da Release 1 sem regressão.

## 7. Critérios de aceite

- [ ] O fluxo agenda, registra a realização, registra o resultado e confere o histórico, com dois perfis.
- [ ] O spec é estável e repetível.
- [ ] Capturas e relatório estão prontos e os Bugs registrados.

## 8. Cuidados e fora de escopo

- Os cenários dos PBIs 19 a 21 já têm specs próprios: este valida a **integração** entre eles, não repete os erros.
- A retirada não entra aqui (T107).

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF15** (Módulo D — Acompanhamento; interface Web): “Consultar o histórico de visitas e avaliações de um participante.”
- Este PBI reúne também o **acompanhamento da Sprint** (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO.

### 9.2 Critério de aceite do PBI (planejamento)

> Pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Este PBI também reúne o acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO. O Planning (T94) é uma cerimônia da terça 10/11 e acontece antes da janela do PBI.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T99 · E2E do fluxo de acompanhamento completo.** Esta task lê o trabalho dos PBIs 19, 20, 21 e 22, já integrados na main, então pode ser escrita de uma vez. Primeiro escreva o cenário: o coordenador agenda uma visita e registra que foi realizada, o pesquisador registra um resultado, e o histórico mostra tudo. Depois rode no CI com os dados de demonstração das outras fatias e abra Bugs no Azure Boards, ligados à task de origem, para o que falhar. Por fim, deixe o relatório e as capturas prontos para o README. O cenário da retirada fica na T107, depois que o PBI-23 existir.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
