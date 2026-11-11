# T107 · E2E, capturas e documentação do RF16

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 2h · **Prazo:** Qui 19/11  
> **Depende de:** T104  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar o RF16 no navegador (retirar um participante com visitas futuras e conferir a marca RETIRADO e o cancelamento no histórico, e o erro do motivo vazio), guardar as capturas, atualizar a documentação e passar o bastão ao PBI-24.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Infraestrutura E2E e specs anteriores (`ApiE2E`, `FichaParticipantePage` do E2E criada na T99, `agendamento.spec.ts`).
- O estado de partida precisa de um participante **com visitas futuras agendadas** (criado por `ApiE2E`: participante incluído, visita prevista com janela futura, agendamento).
- O cenário da retirada foi deixado de fora da T99 até o PBI-23 existir.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | Método `retirar` |
| `web/ClinicalStudyPB/e2e/pages/FichaParticipantePage.ts` | Alterar | Botão, diálogo e marca RETIRADO |
| `web/ClinicalStudyPB/e2e/tests/retirada.spec.ts` | Criar | Cenários de sucesso e de erro |
| `web/ClinicalStudyPB/e2e/tests/fluxo-acompanhamento.spec.ts` | Alterar | Acrescenta o passo da retirada ao fluxo (opcional) |
| `README.md` | Alterar | RF16 com as capturas |
| `docs/imagens/sprint3/` | Alterar | Capturas finais do RF16 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T107-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cenários Playwright (`retirada.spec.ts`)

- **RQ-T107-01** **Sucesso:** o coordenador abre a ficha de um participante com visitas futuras agendadas, clica em **Registrar retirada**, preenche motivo, solicitante e data, vê na confirmação o aviso de irreversibilidade e a **quantidade de visitas afetadas**, confirma; confere a marca **RETIRADO** na ficha e na lista de participantes e, no **histórico**, que as visitas futuras aparecem **canceladas** com o motivo "retirada do estudo" e que as já realizadas permanecem.
- **RQ-T107-02** **Estado depois da retirada:** os botões Agendar, Reagendar, Registrar desfecho e Registrar retirada não aparecem; a aba Avaliações mostra o aviso de participante retirado.
- **RQ-T107-03** **Erro:** o motivo vazio (ou curto) bloqueia a confirmação e nada é gravado.
- **RQ-T107-04** **Vaga:** o estudo continua mostrando a mesma ocupação de vagas depois da retirada.
- **RQ-T107-05** Estado de partida montado por `ApiE2E`; dados únicos por execução.

### Capturas e documentação

- **RQ-T107-06** Salvar `rf16-retirada-confirmacao`, `rf16-retirada-historico` e `rf16-retirada-erro-motivo`; copiar as finais para `docs/imagens/sprint3/`.
- **RQ-T107-07** Atualizar o `README.md` com o RF16.
- **RQ-T107-08** Zerar o Remaining Work das tasks do PBI-23 no Azure Boards e registrar os Bugs.
- **RQ-T107-09** Passar o bastão a I7 (PBI-24): a coluna de situação ATIVO/RETIRADO (T101), o cancelamento em cascata e o participante retirado de demonstração (T105) estão na `main`; I7 revisa os Pull Requests deste PBI.

## 6. Testes

- `retirada.spec.ts` no CI; E2E dos PBIs anteriores e da Release 1 sem regressão.

## 7. Critérios de aceite

- [ ] A retirada com visitas futuras, a marca RETIRADO e o cancelamento no histórico estão provados no navegador.
- [ ] O erro do motivo vazio está coberto.
- [ ] Capturas e README atualizados; o PBI-23 está 100% na `main` e o PBI-24 pode começar.

## 8. Cuidados e fora de escopo

- Não depender de datas fixas.
- Não montar o estado pela interface: use `ApiE2E`.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF16** (Módulo D — Acompanhamento; interface Web): “Registrar a retirada de um participante do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação, e o histórico fica preservado.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T107 · E2E, capturas e documentação do RF16.** Primeiro escreva o cenário Playwright em que o coordenador registra a retirada de um participante com visitas futuras e confere a marca RETIRADO e o cancelamento das visitas no histórico do participante, e o cenário de erro do motivo vazio. Depois salve capturas nomeadas de sucesso e de erro. Por fim, atualize o README com o RF16 e zere o Remaining Work no Azure Boards.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
