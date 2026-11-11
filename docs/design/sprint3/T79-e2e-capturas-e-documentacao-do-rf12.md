# T79 · E2E, capturas e documentação do RF12

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 2h · **Prazo:** Qua 11/11  
> **Depende de:** T77  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar o RF12 de ponta a ponta no navegador (agendar com a data sugerida, reagendar e recusar data fora da janela), guardar as capturas de sucesso e de erro para o README da Release 2 e atualizar a documentação e o Azure Boards.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Infraestrutura E2E da Sprint 2: `e2e/fixtures/index.ts` (fixtures por perfil), `e2e/pages/*` (page objects), `e2e/support/api.ts` (`ApiE2E` monta o estado de partida pela API), `e2e/support/capturas.ts` (`capturar(page, nome)`), dados únicos por execução.
- O fluxo completo da Release 1 (`fluxo-completo.spec.ts`) mostra o estilo de `test.step` e de asserções por papel e nome acessível.
- O estado de partida é um estudo **em recrutamento** com participante incluído e visita prevista com janela que contenha dias futuros.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | Métodos `participanteIncluido`, `visitaPrevista` e `agendar` |
| `web/ClinicalStudyPB/e2e/pages/AgendamentoParticipantePage.ts` | Criar | Page object da ficha de agendamento |
| `web/ClinicalStudyPB/e2e/pages/AgendaPage.ts` | Criar | Page object da agenda |
| `web/ClinicalStudyPB/e2e/tests/agendamento.spec.ts` | Criar | Cenários de sucesso e de erro |
| `web/ClinicalStudyPB/e2e/tests/acesso-por-perfil.spec.ts` | Alterar | Pesquisador agora vê Participantes e Agenda (se ainda não ajustado na T77) |
| `README.md` | Alterar | Seção de funcionalidades: RF12 com as capturas |
| `docs/imagens/sprint3/` | Criar | Capturas finais do RF12 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T79-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cenários Playwright (`agendamento.spec.ts`)

- **RQ-T79-01** **Sucesso:** o coordenador abre Participantes, entra no agendamento de um participante incluído, vê a janela e a data sugerida, agenda a visita com a data sugerida, confere o rótulo "Agendada" e a visita na agenda do estudo; depois reagenda para outro dia da janela e confere a nova data.
- **RQ-T79-02** **Erro:** o coordenador tenta agendar com data fora da janela e vê a mensagem com a janela válida; nada é gravado.
- **RQ-T79-03** **Permissão:** o pesquisador abre Participantes e Agenda e vê tudo em leitura, sem os botões Agendar e Reagendar.
- **RQ-T79-04** O estado de partida (estudo em recrutamento, participante incluído, visita prevista) é montado por `ApiE2E`; o fluxo testado é feito pela interface; os dados têm sufixo único por execução.

### Capturas e documentação

- **RQ-T79-05** Salvar com `capturar(page, nome)`: `rf12-agendamento-sucesso`, `rf12-agenda` e `rf12-agendamento-erro-janela`; copiar as finais para `docs/imagens/sprint3/`.
- **RQ-T79-06** Atualizar o `README.md` (funcionalidades): RF12 com o comportamento, os erros e as capturas; atualizar a contagem de testes quando a Sprint fechar.
- **RQ-T79-07** Zerar o Remaining Work das tasks do PBI-19 no Azure Boards, registrar os defeitos de integração como Bug ligado à task de origem e avisar I3 na passagem de bastão: tabela, regras e endpoints, agenda com ponto de encaixe e dados de demonstração estão na `main`.
- **RQ-T79-08** Confirmar que toda a suíte E2E da Release 1 continua passando (29 testes).

## 6. Testes

- `agendamento.spec.ts` (3 cenários) rodando local e no job E2E do CI.
- Suíte E2E da Release 1 inteira, sem regressão.

## 7. Critérios de aceite

- [ ] Os cenários de sucesso, de erro e de permissão passam, e podem ser repetidos no mesmo banco.
- [ ] As capturas de sucesso e de erro estão em `docs/imagens/sprint3/`.
- [ ] O README descreve o RF12 e o Azure Boards está atualizado.
- [ ] O PBI-19 está 100% na `main`, com Pull Requests revisados por I3.

## 8. Cuidados e fora de escopo

- Não depender de datas fixas: calcule a janela a partir de hoje.
- Não montar o estado pela interface (lento e frágil): use `ApiE2E`.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF12** (Módulo D — Acompanhamento; interface Web): “Agendar visitas para os participantes incluídos.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Ao fechar, em 11/11, este PBI deixa na main a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T79 · E2E, capturas e documentação do RF12.** Primeiro escreva o cenário Playwright em que o coordenador agenda uma visita com a data sugerida e a reagenda, e o cenário de erro com data fora da janela. Depois salve capturas nomeadas de sucesso e de erro para o README da Release 2. Por fim, atualize o README com o RF12 e zere o Remaining Work da task no Azure Boards.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
