# Ata do checkpoint de integração da Sprint 3 (meio da Sprint, 17/11/2026)

> **Task T100** · PBI-22 · responsável I2 · guia: [T100](../design/sprint3/T100-checkpoint-de-integracao-e-acompanhamento-da-sprint.md)
>
> **Como ler este documento.** A parte **"Levantamento no repositório"** foi montada a partir do que está na branch `develop` (commit `5e840a1`, em 20/11/2026) e nas branches remotas, e pode ser conferida com `git log` e `git ls-tree`. O que **só a equipe sabe** (Remaining Work do Azure Boards, imagem do burndown, decisões tomadas na reunião, presença) está marcado com **`[A PREENCHER]`**: nenhum número ou decisão foi inventado.

## 1. Pauta e participantes

| Item | Registro |
|---|---|
| Data e duração | Terça 17/11/2026, 15 minutos (meio da Sprint) |
| Conduzido por | I2 (Product Owner) |
| Participantes | `[A PREENCHER]` |
| Pauta | (1) o que está na `main`; (2) o que atrasou; (3) o que corre risco nos PBIs 23, 24 e 25; (4) o que cortar pela ordem de corte |

## 2. Burndown e Remaining Work (RQ-T100-01)

| Item | Situação |
|---|---|
| Remaining Work de todas as tasks atualizado no Azure Boards | `[A PREENCHER]` (cada dono atualiza as suas tasks) |
| Imagem do burndown do dia | `[A PREENCHER]`: salvar em `docs/imagens/sprint3/burndown-17-11.png` e linkar aqui |
| Horas planejadas / restantes no dia | Planejado: 146 h (49 tasks). Restantes: `[A PREENCHER]` |

## 3. Levantamento no repositório: PBIs 19, 20 e 21 na `develop` (RQ-T100-02)

Critério do planejamento para "100% concluído": tasks com Remaining Work zero, Pull Requests revisados e integrados, testes passando e README atualizado. O repositório permite conferir o **código, os testes e a documentação**; o Remaining Work e a revisão dos PRs são conferidos no Azure Boards e no GitHub.

| PBI | Task | Evidência na `develop` | Situação |
|---|---|---|---|
| PBI-19 (RF12) | T73 modelo e migration | `V6_19_1__create_scheduled_visits.sql`, entidade `VisitaAgendada` (PRs #8, #10) | ✅ |
| | T74 contrato e demonstração | `docs/api/openapi-sprint3.yaml`, `R__demo_19_visitas_agendadas.sql` (PR #11) | ✅ |
| | T75 regras do agendamento | `AgendamentoVisitaService` (PRs #12, #17) | ✅ |
| | T76 endpoints | `AgendamentoController` (PR #21) | ✅ |
| | T77 telas | `AgendamentoParticipantePage`, `AbaAgenda` (PR #23) | ✅ |
| | T78 testes de backend | `AgendamentoVisitaServiceTest`, `AgendamentoEndpointTest`, `VisitaAgendadaRepositoryTest` | ✅ (cobertura a conferir no CI) |
| | T79 E2E do RF12 | não há spec do agendamento em `web/ClinicalStudyPB/e2e/tests/` | ⚠️ pendente: o fluxo ficou coberto de forma integrada pela T99 (`fluxo-acompanhamento.spec.ts`) |
| PBI-20 (RF13) | T80 a T82 estados, regras e endpoints | `DesfechoVisitaService`, `VisitaAgendadaController`, `DesfechoRequest` (PR #20 e anteriores) | ✅ |
| | T83 telas do desfecho | `DialogoDesfecho`, `BotaoRegistrarDesfecho` | ✅ |
| | T84 demonstração e integração com a agenda | `R__demo_20_desfechos.sql` | ✅ (pontos finos da T84 a confirmar com o dono, I3) |
| | T85 testes de backend | `DesfechoVisitaServiceTest`, `DesfechoEndpointTest`, `DesfechoConcorrenciaTest`, `DesfechoRequestValidacaoTest` | ✅ |
| | T86 E2E do RF13 | sem spec dedicado | ⚠️ pendente (coberto de forma integrada pela T99) |
| PBI-21 (RF14) | T87 a T90 modelo, contrato, regras, endpoints e demonstração | `V6_21_1__create_evaluation_results.sql`, `ResultadoAtividadeService`, `ResultadoAtividadeController`, `R__demo_21_resultados.sql` (PRs #13, #16, #18, #19) | ✅ |
| | T91 tela de resultados | `AbaAvaliacoesVisita` (PR #29) | ✅ |
| | T92 testes de backend | `ResultadoAtividade{Service,Endpoint,Repository,Demo}Test` | ✅ |
| | T93 E2E do RF14 | sem spec dedicado | ⚠️ pendente (coberto de forma integrada pela T99) |

**Conclusão do item da pauta "o que está na `main`":** o código, os testes de backend e as telas dos PBIs 19, 20 e 21 estão integrados. **Não estão** os três E2E dedicados (T79, T86 e T93), o que impede declarar os três PBIs 100% concluídos pelo critério do planejamento. Lista a levar ao Azure Boards (RQ-T100-02, "listar na ata o que não estiver"):

| Pendência | Dono do PBI | Encaminhamento proposto |
|---|---|---|
| T79 E2E do agendamento | I1 | Aproveitar o page object e o helper de API da T99 (`FichaParticipantePage`, `ApiE2E.agendar`) |
| T86 E2E do desfecho | I3 | Idem (`ApiE2E.registrarDesfecho`); acrescentar os desfechos ausente e cancelado, que a T99 não cobre |
| T93 E2E dos resultados | I6 | Idem (`ApiE2E.registrarResultado`); acrescentar edição e bloqueios |
| Decisão final sobre os 3 itens | I2 (PO) | `[A PREENCHER]`: fazer agora ou levar à Sprint 4 |

## 4. CI da `main` e ordem de integração (RQ-T100-03)

| Verificação | Resultado |
|---|---|
| O CI define 6 jobs (`backend`, `backend-postgres`, `web`, `e2e`, `docker`, `mobile`) | Confirmado em `.github/workflows/ci.yml` |
| Os 6 jobs estão verdes na `main` | `[A PREENCHER]`: conferir na aba Actions (este levantamento não consulta o GitHub). Incidente conhecido: o job `docker` falhou por limite de requisições (HTTP 429) do Maven Central; mitigado com espelho (PR #34), registrado no [registro de defeitos](../testes/registro-de-defeitos.md) |
| Ordem de entrada dos PBIs 19 a 22 | Conforme o planejamento: PBI-19 (T73 a T77), PBI-20 e PBI-21 (T80 a T91) e PBI-22 (T96 e T97) entraram nessa sequência no histórico da `develop`; a T98 e a T99 estão em branches próprias, aguardando PR |

## 5. O que atrasou (RQ-T100-04)

| Item | Registro |
|---|---|
| Atrasos com evidência no repositório | Os três E2E dedicados (T79, T86, T93) e a T84 não fecharam na janela do PBI correspondente; PRs de PBIs integrados após o fim da respectiva janela (T91 e T97 só entraram em 16/11) |
| Atrasos relatados na reunião | `[A PREENCHER]` |

## 6. Riscos nos PBIs 23, 24 e 25 (RQ-T100-04 e RQ-T100-06)

Estado do trabalho **encontrado nas branches remotas** em 20/11/2026:

| PBI | Evidência | Leitura |
|---|---|---|
| PBI-23 (RF16, I4, 18 a 19/11) | **Nenhuma branch remota das tasks T101 a T107** | Maior risco: sem código visível, a janela já passou. Falar com I4 e decidir entre reforço, corte ou Sprint 4 |
| PBI-24 (RF17, I7, 20 a 21/11) | Branches `feat/task108-esqueleto-app` e `feat/task109-acesso-participante-endpoints-me` | T108 e T109 prontas em PR; T110 a T114 sem branch |
| PBI-25 (RF18, I5, 22 a 23/11) | Branches `feature/t115-contrato-mocks-participante` e `feature/t116-backend-consultas-participante` | Contrato e backend das consultas prontos em PR; T117 a T121 sem branch, e o PBI termina no dia da Review |

Reavaliação dos riscos da seção "Riscos e respostas" do planejamento:

| Risco | Status | Base |
|---|---|---|
| Atraso em cadeia sem dias de reserva | 🔴 materializou-se | PBI-23 sem entregas; PBIs 24 e 25 começaram com PRs empilhados (T108 → T109 → T115 → T116) |
| Carga diária (cerca de 10,5 h por dia) | 🟠 em atenção | `[A PREENCHER]` com a disponibilidade informada pelos donos |
| Janelas em fim de semana e feriado (PBIs 21, 24 e 25) | 🟠 em atenção | PBI-21 fechou no prazo no código; PBIs 24 e 25 caem em 20 a 23/11 |
| PBI-25 termina no dia da Review | 🟠 em atenção | Só contrato e backend do PBI-25 existem; telas, testes e E2E dependem da 23/11 |
| Dois desfechos ou retirada pela metade por concorrência | 🟡 desfecho mitigado; retirada aberta | `DesfechoConcorrenciaTest` existe (T81 e T85); a retirada (T102 e T106) não tem código |
| Participante retirado ainda poder agendar ou registrar | 🔴 aberto | Depende da T102 (PBI-23), que não aparece nas branches |
| Conflito de versões de migration | 🟢 controlado | Maior versão da Sprint 2 era `V5`; as da Sprint 3 usam o prefixo `V6_19_1`, `V6_21_1` (a regra 10 previa trocar o prefixo `V3` pelo seguinte ao da Sprint 2) |
| E2E mobile instável ou pesado no CI | 🟠 em atenção | T114 sem branch; decisão sobre rodar no CI continua em aberto |
| Carry-over da Sprint 2 maior que o esperado | 🟠 em atenção | Defeitos abertos listados no [registro de defeitos](../testes/registro-de-defeitos.md) |
| Escopo e data da Release 2 indefinidos | 🟠 em atenção | `[A PREENCHER]`: houve retorno da professora? |

## 7. Decisões do checkpoint (RQ-T100-05)

Ordem de corte do planejamento, **sem alteração por este levantamento**: (1) agenda do estudo por período (T77) e filtro por situação do histórico (T97); (2) reagendamento (T75 e T77) e resumo de comparecimento do app (T118); (3) só então os cenários de erro dos E2E. O agendamento, o desfecho, os resultados, o histórico, a retirada, o login mobile e as listas do app **não** se cortam.

| Decisão | Registro |
|---|---|
| Cortes de escopo (com motivo) | `[A PREENCHER]` |
| Troca de lugar entre PBIs (por exemplo PBI-24 com PBI-23) | `[A PREENCHER]` |
| Reforço de revisão | Sugestão: dono do PBI seguinte revisa o PR do anterior (regra do planejamento); T115/T116 e T108/T109 já estão empilhados e devem ser aceitos na ordem T108, T109, T115 e T116 |
| Itens que vão para a Sprint 4 | `[A PREENCHER]`; candidatos: T79, T86 e T93 se não forem feitos, E2E mobile no CI, RF19 a RF22 (já previstos fora do escopo) |
| Responsáveis e prazos das ações | `[A PREENCHER]` |

## 8. Próximos passos

| Ação | Quem | Prazo |
|---|---|---|
| Atualizar o Remaining Work e anexar a imagem do burndown (seção 2) | Todos os donos / I2 | `[A PREENCHER]` |
| Confirmar os 6 jobs verdes na `main` (seção 4) | I2 | `[A PREENCHER]` |
| Decidir sobre T79, T86 e T93 e sobre o PBI-23 (seções 3 e 6) | I2 com os donos | `[A PREENCHER]` |
| Completar o [rascunho do processo de desenvolvimento](processo-de-desenvolvimento-sprint3.md) na Review de 23/11 | I2 | 23/11/2026 |
