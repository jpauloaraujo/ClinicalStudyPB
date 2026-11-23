# Processo de desenvolvimento da Sprint 3 (rascunho para o README da Release 2)

> **Task T100** · rascunho da seção "Processo de desenvolvimento" da Sprint 3, no modelo da [seção 7 do README da Release 1](../../releases/release1/README.md). Escrito no checkpoint de meio de Sprint (ver a [ata](checkpoint-17-11.md)) e **completado na Review de 23/11/2026**.
>
> Legenda: **`[COMPLETAR NA REVIEW]`** marca o que só existe depois da Review (burndown final, retrospectiva, impedimentos e ações de melhoria). **`[A PREENCHER]`** marca dados que só o Azure Boards tem. Nenhum número de burndown foi inventado.

## 1. Objetivo da Sprint

A Sprint 3 (10/11 a 23/11/2026) entrega o acompanhamento dos participantes na web e o início do aplicativo do participante: o coordenador agenda visitas e registra o desfecho de cada uma, o pesquisador registra os resultados fictícios, os dois consultam o histórico do participante e a retirada é registrada; no app, o participante entra, vê seus estudos e suas visitas.

| Item | Definição |
|---|---|
| Requisitos | RF12, RF13, RF14, RF15 e RF16 (Módulo D, web); RF17 e RF18 (Módulo E, mobile); login do participante no app (RF02 e RF03 no mobile) |
| Também previsto | Pelo menos um fluxo E2E no app mobile; testes e correção de defeitos; RF01 a RF11 continuam funcionando |
| Fora do escopo (Sprint 4, proposta) | RF19 e RF20 (mobile), RF21 (painel), RF22 (relatório), E2E mobile no CI e o fechamento da Release 2 |

## 2. Backlog e tarefas (PBIs)

São **7 PBIs, 49 tasks e 146 horas**, um PBI por integrante, em janelas sequenciais de dois dias. Planejamento completo em [Planejamento_da_Sprint_3_ClinicalStudyPB_v2.md](Planejamento_da_Sprint_3_ClinicalStudyPB_v2.md); um guia por task em [docs/design/sprint3/](../design/sprint3/README.md).

| PBI | Entrega | Requisito | Resp. | Janela | Horas | Tasks |
|---|---|---|---|---|---|---|
| PBI-19 | Agendamento de visitas | RF12 | I1 | 10/11 a 11/11 | 21 h | T73 a T79 |
| PBI-20 | Realização, ausência e cancelamento | RF13 | I3 | 12/11 a 13/11 | 21 h | T80 a T86 |
| PBI-21 | Resultados fictícios das avaliações | RF14 | I6 | 14/11 a 15/11 | 21 h | T87 a T93 |
| PBI-22 | Histórico do participante e acompanhamento da Sprint | RF15 | I2 | 16/11 a 17/11 | 20 h | T94 a T100 |
| PBI-23 | Retirada de participante | RF16 | I4 | 18/11 a 19/11 | 21 h | T101 a T107 |
| PBI-24 | App: esqueleto, login, vínculo da conta e meus estudos | RF02/RF03 (mobile), RF17 | I7 | 20/11 a 21/11 | 21 h | T108 a T114 |
| PBI-25 | App: próximas visitas e histórico de comparecimento | RF18 | I5 | 22/11 a 23/11 | 21 h | T115 a T121 |
| **Total** | | | | 10/11 a 23/11 | **146 h** | **49 tasks** |

## 3. Organização da equipe

| Prática | Como funcionou na Sprint 3 |
|---|---|
| Um PBI por vez | Só um PBI em andamento; o seguinte começa quando o anterior está 100% concluído (código na `develop`, testes, README e Remaining Work zerado) |
| Um dono por PBI | Cada integrante cuida da fatia inteira (migration, regras, endpoints, tela, testes, E2E e capturas) durante a sua janela de dois dias |
| Revisão cruzada | O Pull Request de cada PBI é revisado pelo dono do PBI seguinte (o último, por I1) |
| Passagem de bastão | No fim de cada janela (11, 13, 15, 17, 19, 21 e 23/11) o dono apresenta o que ficou pronto e passa ao próximo o que ele vai usar |
| Contrato antes do código | O contrato OpenAPI da fatia é escrito no início de cada PBI (`docs/api/openapi-sprint3.yaml`) |
| Migrations em faixas | Prefixo `V6_<PBI>_...` (a maior versão da Sprint 2 era `V5`), sem conflito de numeração |
| Dados de demonstração | Cada fatia acrescenta seus dados fictícios ao perfil `demo` (`R__demo_19` a `R__demo_22` até agora) |
| Cerimônias | Planning (10/11), dailies, passagens de bastão, checkpoint (17/11) e Review com Retrospectiva (23/11) |

| Integrante | Foco | PBI | Revisor dos PRs |
|---|---|---|---|
| I1 | Scrum Master, arquitetura e DevOps | PBI-19 | I3 |
| I3 | Backend: domínio, persistência e estudos | PBI-20 | I6 |
| I6 | Frontend React: estudos e protótipos | PBI-21 | I2 |
| I2 | Product Owner, testes e documentação | PBI-22 | I4 |
| I4 | Backend: usuários, segurança e autorização | PBI-23 | I7 |
| I7 | Mobile, protótipos e testes | PBI-24 | I5 |
| I5 | Frontend React: autenticação e integração | PBI-25 | I1 |

As atribuições seguem o planejamento; o histórico do Git e o Azure Boards registram quem executou cada task. Os nomes de cada integrante constam na tabela da equipe do README principal.

## 4. Burndown (RQ-T100-07)

| Item | Registro |
|---|---|
| Imagem do burndown do dia do checkpoint (17/11) | `[A PREENCHER]`: `docs/imagens/sprint3/burndown-17-11.png` |
| Imagem do burndown final (23/11) | `[COMPLETAR NA REVIEW]` |
| Referência do planejamento | 146 horas em 49 tasks, sem margem entre janelas (os 14 dias estão todos ocupados) |

## 5. Análise do planejado contra o realizado até o checkpoint

Com base no repositório em 20/11/2026 (detalhe e evidências na [ata do checkpoint](checkpoint-17-11.md)); as horas do Azure Boards ainda precisam ser anexadas.

| Pergunta | Resposta parcial |
|---|---|
| Os PBIs 19, 20 e 21 chegaram à `develop`? | Sim no código, nos testes de backend e nas telas. Faltam os três E2E dedicados (T79, T86, T93), que a T99 cobre de forma integrada (agenda, desfecho, resultado e histórico) |
| O PBI-22 fechou na janela? | A consulta (T96) e a tela do histórico (T97) entraram em 16/11; os testes (T98), o E2E do fluxo (T99) e esta documentação (T100) foram concluídos em branches próprias, aguardando Pull Request |
| Houve atraso em cadeia? | Sim: o PBI-23 não tem branch remota, e os PBIs 24 e 25 avançaram por Pull Requests empilhados (T108 → T109 → T115 → T116) |
| Horas planejadas contra restantes | `[A PREENCHER]` com o Remaining Work do Azure Boards |
| Principais causas | Janelas em fim de semana e feriado, carga diária acima de 10 h e dependência em cadeia (riscos já previstos no planejamento) |
| Defeitos do período | Registrados com identificador no [registro de defeitos](../testes/registro-de-defeitos.md) e na [rastreabilidade](../rastreabilidade/rastreabilidade-erros-e-alteracoes.md) |

## 6. Revisão da Sprint (completar na Review de 23/11)

| Item | Registro |
|---|---|
| O que foi entregue | `[COMPLETAR NA REVIEW]` |
| Resultado do burndown final | `[COMPLETAR NA REVIEW]` |
| Impedimentos | `[COMPLETAR NA REVIEW]`. Já conhecidos: limite de requisições do Maven Central no job Docker (HTTP 429, mitigado com espelho), PBI-23 sem entregas visíveis, janelas em dias não úteis |
| Resultado da retrospectiva | `[COMPLETAR NA REVIEW]` |
| Ações de melhoria acordadas | `[COMPLETAR NA REVIEW]` |
| Itens que vão para a Sprint 4 | `[COMPLETAR NA REVIEW]` |
