# T86 · E2E, capturas e documentação do RF13

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 2h · **Prazo:** Sex 13/11  
> **Depende de:** T83  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Provar o RF13 no navegador (realizada, ausente e cancelada com motivo, e o erro do cancelamento sem motivo), guardar as capturas e atualizar a documentação e o Azure Boards, passando o bastão ao PBI-21.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Infraestrutura E2E e o spec de agendamento da T79 (`ApiE2E.agendar`, `AgendaPage`, `AgendamentoParticipantePage`).
- Para registrar **realizada** pela interface, a visita agendada precisa ter data de hoje ou do passado: o estado de partida cria a participação com a visita prevista de janela que contenha hoje e agenda nela.
- `capturar(page, nome)` salva em `test-results/capturas/`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | Método `registrarDesfecho` e a montagem de visitas agendadas para hoje |
| `web/ClinicalStudyPB/e2e/pages/VisitaAgendadaPage.ts` | Criar | Page object da tela da visita e do diálogo de desfecho |
| `web/ClinicalStudyPB/e2e/tests/desfecho-visita.spec.ts` | Criar | Cenários de sucesso e de erro |
| `README.md` | Alterar | RF13 com as capturas |
| `docs/imagens/sprint3/` | Alterar | Capturas finais do RF13 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T86-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cenários Playwright (`desfecho-visita.spec.ts`)

- **RQ-T86-01** **Sucesso:** o coordenador registra uma visita como **realizada** (data e hora reais), outra como **ausente** (com observação) e outra como **cancelada** com motivo; confere em cada uma o selo, os dados registrados e a ausência de botões de escrita; a agenda mostra as três situações.
- **RQ-T86-02** **Erro:** o cancelamento sem motivo é bloqueado (botão desabilitado e/ou mensagem `MOTIVO_OBRIGATORIO`) e nada é gravado.
- **RQ-T86-03** **Final:** tentar registrar outro desfecho em visita que já tem desfecho não é possível pela interface (sem botão) e o servidor responde 409 se chamado pela API (`ApiE2E`).
- **RQ-T86-04** O estado de partida (estudo, participante incluído, visitas agendadas em dia permitido) é montado por `ApiE2E`; dados únicos por execução.

### Capturas e documentação

- **RQ-T86-05** Salvar `rf13-desfecho-realizada`, `rf13-desfecho-cancelada` e `rf13-desfecho-erro-motivo`; copiar as finais para `docs/imagens/sprint3/`.
- **RQ-T86-06** Atualizar o `README.md` com o RF13 (comportamento, regras, erros e capturas).
- **RQ-T86-07** Zerar o Remaining Work das tasks do PBI-20 no Azure Boards e registrar os Bugs encontrados.
- **RQ-T86-08** Passar o bastão a I6 (PBI-21): máquina de estados, registro de desfecho, selo na agenda e visitas realizadas de demonstração estão na `main`; I6 revisa os Pull Requests deste PBI.

## 6. Testes

- `desfecho-visita.spec.ts` (sucesso, erro e final) no CI; suíte E2E da Release 1 e do PBI-19 sem regressão.

## 7. Critérios de aceite

- [ ] Os três desfechos e o erro do motivo estão provados no navegador.
- [ ] As capturas estão em `docs/imagens/sprint3/` e o README descreve o RF13.
- [ ] O PBI-20 está 100% na `main` e o PBI-21 pode começar.

## 8. Cuidados e fora de escopo

- Não depender de datas fixas.
- Não montar o estado pela interface: use `ApiE2E`.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
