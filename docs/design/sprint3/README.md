# Documentos de design da Sprint 3

Cada task da Sprint 3 tem um documento que diz **o que implementar, em qual arquivo, com quais regras e em que ordem**, e traz a lista numerada de **todos os requisitos** (`RQ-Tnn-NN`) que precisam estar atendidos para a task ser dada como concluída, mais a **origem desses requisitos** (seção 9: requisito funcional da especificação, critério de aceite do PBI, regras de negócio, texto original da task e requisitos transversais da especificação). Como a Sprint é feita em sequência, um PBI de cada vez, por pessoas diferentes, os documentos foram escritos para formar **um conjunto coerente**: os nomes, os endpoints, os DTOs, os códigos de erro, as tabelas e os dados de demonstração são os mesmos em todos e estão fixados em um só lugar.

> **Comece pelo [contrato e pelas convenções compartilhadas](00-convencoes-e-contrato-compartilhado.md).** Ele traz os ajustes em relação ao planejamento (seção 1), os 19 endpoints novos, os DTOs, os códigos de erro, as tabelas, os dados de demonstração e os pontos de encaixe das telas. Se uma task precisar mudar algo ali, a mudança entra no mesmo Pull Request.

- **49 tasks** (T73 a T121), **49 documentos**, **579 requisitos numerados**, 7 PBIs, 146 horas, de 10/11 a 23/11/2026. Os documentos seguem o [Planejamento da Sprint 3](../../sprint3/Planejamento_da_Sprint_3_ClinicalStudyPB_v2.md).
- Legenda: ✅ implementada · 🟡 parcial · ⬜ a implementar · 📋 entrega fora do código (cerimônia ou processo), especificada no próprio documento e ainda obrigatória. Todas as tasks da Sprint 3 começam em ⬜; a T94 e a T100 são 📋.
- Caminhos e nomes são os **reais do código** (`/api/participacoes/...`, `/api/me/estudos`), em português, como na Sprint 2. Cada documento segue o modelo da Sprint 2: objetivo, contexto (o que reaproveitar), onde implementar, requisitos, contrato, testes, critérios de aceite e cuidados.
- Cada PBI tem um dono, uma janela de dois dias e só começa quando o anterior está 100% na `main`. O Pull Request de cada PBI é revisado pelo dono do seguinte (o último, por I1).

## Ajustes em relação ao planejamento

Dez pontos do planejamento foram ajustados para funcionar com o código real da Sprint 2; todos estão explicados, com o motivo, na [seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê) e precisam ser **confirmados no Planning (T94)**. Os principais:

- **Migrations com prefixo `V6_`**, não `V3_`: a Sprint 2 terminou em `V5`, e o Flyway recusaria uma versão menor que a última aplicada.
- **Demonstração com scripts repetíveis** (`R__demo_NN_*.sql`), no padrão da T63, e datas relativas a `CURRENT_DATE`.
- **Caminhos em português** e um contrato separado (`openapi-sprint3.yaml`) com teste próprio, para não quebrar o `ContratoOpenApiTest` da Sprint 2.
- **Erros 400 com código próprio** (`ValidacaoRegraException`, criada na T75) e **fuso** `America/Fortaleza` para as datas da API.
- **A aba Participantes passa a ser visível ao pesquisador** (somente leitura), senão ele não chega aos resultados (RF14) nem ao histórico (RF15).

## Índice das tasks por PBI

### PBI-19 · RF12 Agendamento de visitas (I1 · Ter 10/11 a Qua 11/11 · 21h)

**Tasks:** T73 a T79 · **Depende de:** Sprint 2.

**Critério de aceite do PBI:** O coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Deixa na `main` a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T73 | Modelo de domínio e migration da visita agendada | I1 | 3h | Ter 10/11 | Sprint 2 (tabelas de participação e de visita prevista) | ⬜ | [Design da T73](T73-modelo-de-dominio-e-migration-da-visita-agendada.md) · 17 requisitos |
| T74 | Contrato OpenAPI do agendamento e dados de demonstração | I1 | 2h | Ter 10/11 | T73 | ⬜ | [Design da T74](T74-contrato-openapi-do-agendamento-e-dados-de-demonstracao.md) · 16 requisitos |
| T75 | Regras de agendamento (serviço de domínio) | I1 | 4h | Ter 10/11 | T73, T74 | ⬜ | [Design da T75](T75-regras-de-agendamento-servico-de-dominio.md) · 14 requisitos |
| T76 | Endpoints, autorização e Swagger do agendamento | I1 | 3h | Qua 11/11 | T75 | ⬜ | [Design da T76](T76-endpoints-autorizacao-e-swagger-do-agendamento.md) · 13 requisitos |
| T77 | Telas de agendamento | I1 | 4h | Qua 11/11 | T74, T76 | ⬜ | [Design da T77](T77-telas-de-agendamento.md) · 20 requisitos |
| T78 | Testes de backend do agendamento | I1 | 3h | Qua 11/11 | T75, T76 | ⬜ | [Design da T78](T78-testes-de-backend-do-agendamento.md) · 17 requisitos |
| T79 | E2E, capturas e documentação do RF12 | I1 | 2h | Qua 11/11 | T77 | ⬜ | [Design da T79](T79-e2e-capturas-e-documentacao-do-rf12.md) · 8 requisitos |

### PBI-20 · RF13 Realização, ausência e cancelamento de visitas (I3 · Qui 12/11 a Sex 13/11 · 21h)

**Tasks:** T80 a T86 · **Depende de:** PBI-19.

**Critério de aceite do PBI:** O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T80 | Máquina de estados da visita e contrato do desfecho | I3 | 3h | Qui 12/11 | T73 | ⬜ | [Design da T80](T80-maquina-de-estados-da-visita-e-contrato-do-desfecho.md) · 15 requisitos |
| T81 | Regras de registro do desfecho (serviço de domínio) | I3 | 4h | Qui 12/11 | T80 | ⬜ | [Design da T81](T81-regras-de-registro-do-desfecho-servico-de-dominio.md) · 10 requisitos |
| T82 | Endpoints, autorização e Swagger do desfecho | I3 | 3h | Qui 12/11 | T81 | ⬜ | [Design da T82](T82-endpoints-autorizacao-e-swagger-do-desfecho.md) · 9 requisitos |
| T83 | Telas de registro do desfecho | I3 | 4h | Sex 13/11 | T80, T82 | ⬜ | [Design da T83](T83-telas-de-registro-do-desfecho.md) · 11 requisitos |
| T84 | Dados de demonstração e integração com a agenda | I3 | 2h | Sex 13/11 | T76, T82 | ⬜ | [Design da T84](T84-dados-de-demonstracao-e-integracao-com-a-agenda.md) · 11 requisitos |
| T85 | Testes de backend do desfecho | I3 | 3h | Sex 13/11 | T81, T82 | ⬜ | [Design da T85](T85-testes-de-backend-do-desfecho.md) · 13 requisitos |
| T86 | E2E, capturas e documentação do RF13 | I3 | 2h | Sex 13/11 | T83 | ⬜ | [Design da T86](T86-e2e-capturas-e-documentacao-do-rf13.md) · 8 requisitos |

### PBI-21 · RF14 Resultados fictícios das avaliações (I6 · Sáb 14/11 a Dom 15/11 · 21h)

**Tasks:** T87 a T93 · **Depende de:** PBI-19, PBI-20.

**Critério de aceite do PBI:** O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita realizada; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta em modo somente leitura.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T87 | Modelo e migration dos resultados | I6 | 3h | Sáb 14/11 | T73 | ⬜ | [Design da T87](T87-modelo-e-migration-dos-resultados.md) · 9 requisitos |
| T88 | Contrato OpenAPI dos resultados | I6 | 2h | Sáb 14/11 | T87 | ⬜ | [Design da T88](T88-contrato-openapi-dos-resultados.md) · 8 requisitos |
| T89 | Regras de registro de resultados (serviço de domínio) | I6 | 4h | Sáb 14/11 | T84, T88 | ⬜ | [Design da T89](T89-regras-de-registro-de-resultados-servico-de-dominio.md) · 13 requisitos |
| T90 | Endpoints, autorização, Swagger e dados de demonstração | I6 | 3h | Dom 15/11 | T89 | ⬜ | [Design da T90](T90-endpoints-autorizacao-swagger-e-dados-de-demonstracao.md) · 7 requisitos |
| T91 | Tela de resultados da visita | I6 | 4h | Dom 15/11 | T88, T90 | ⬜ | [Design da T91](T91-tela-de-resultados-da-visita.md) · 12 requisitos |
| T92 | Testes de backend dos resultados | I6 | 3h | Dom 15/11 | T89, T90 | ⬜ | [Design da T92](T92-testes-de-backend-dos-resultados.md) · 15 requisitos |
| T93 | E2E, capturas e documentação do RF14 | I6 | 2h | Dom 15/11 | T91 | ⬜ | [Design da T93](T93-e2e-capturas-e-documentacao-do-rf14.md) · 8 requisitos |

### PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint (I2 · Seg 16/11 a Ter 17/11 (Planning em 10/11) · 20h)

**Tasks:** T94 a T100 · **Depende de:** PBI-19, PBI-20, PBI-21.

**Critério de aceite do PBI:** Pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Reúne também o Planning, o checkpoint e o E2E do fluxo completo.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T94 | Planning: validar regras e cadastrar a Sprint 3 no Azure Boards | I2 | 2h | Ter 10/11 (cerimônia, antes da janela) | Nenhuma | 📋 | [Design da T94](T94-planning-validar-regras-e-cadastrar-a-sprint-3-no-azure-boards.md) · 17 requisitos |
| T95 | Contrato e modelo de leitura do histórico | I2 | 3h | Seg 16/11 | T73 | ⬜ | [Design da T95](T95-contrato-e-modelo-de-leitura-do-historico.md) · 10 requisitos |
| T96 | Consulta agregada do histórico no backend | I2 | 4h | Seg 16/11 | T95 | ⬜ | [Design da T96](T96-consulta-agregada-do-historico-no-backend.md) · 10 requisitos |
| T97 | Tela do histórico do participante | I2 | 4h | Ter 17/11 | T95, T96 | ⬜ | [Design da T97](T97-tela-do-historico-do-participante.md) · 11 requisitos |
| T98 | Testes de backend e de componente do histórico | I2 | 2h | Ter 17/11 | T96, T97 | ⬜ | [Design da T98](T98-testes-de-backend-e-de-componente-do-historico.md) · 10 requisitos |
| T99 | E2E do fluxo de acompanhamento completo | I2 | 3h | Ter 17/11 | T76, T82, T90 e os dados de demonstração (T74, T84) | ⬜ | [Design da T99](T99-e2e-do-fluxo-de-acompanhamento-completo.md) · 10 requisitos |
| T100 | Checkpoint de integração e acompanhamento da Sprint | I2 | 2h | Ter 17/11 | PBIs 19, 20 e 21 (leitura) | 📋 | [Design da T100](T100-checkpoint-de-integracao-e-acompanhamento-da-sprint.md) · 8 requisitos |

### PBI-23 · RF16 Retirada de participante (I4 · Qua 18/11 a Qui 19/11 · 21h)

**Tasks:** T101 a T107 · **Depende de:** PBI-19, PBI-20, PBI-21, PBI-22.

**Critério de aceite do PBI:** O coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação e o histórico fica preservado.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T101 | Modelo, migration e contrato da retirada | I4 | 3h | Qua 18/11 | T73 | ⬜ | [Design da T101](T101-modelo-migration-e-contrato-da-retirada.md) · 15 requisitos |
| T102 | Regras da retirada (serviço de domínio) | I4 | 4h | Qua 18/11 | T75, T80, T81, T89, T101 | ⬜ | [Design da T102](T102-regras-da-retirada-servico-de-dominio.md) · 14 requisitos |
| T103 | Endpoints, autorização e Swagger da retirada | I4 | 3h | Qua 18/11 | T102 | ⬜ | [Design da T103](T103-endpoints-autorizacao-e-swagger-da-retirada.md) · 5 requisitos |
| T104 | Tela de retirada | I4 | 4h | Qui 19/11 | T97, T101, T103 | ⬜ | [Design da T104](T104-tela-de-retirada.md) · 12 requisitos |
| T105 | Dados de demonstração e conferência do cancelamento em cascata | I4 | 1h | Qui 19/11 | T76, T102 | ⬜ | [Design da T105](T105-dados-de-demonstracao-e-conferencia-do-cancelamento-em-cascata.md) · 8 requisitos |
| T106 | Testes de backend da retirada | I4 | 4h | Qui 19/11 | T102, T103 | ⬜ | [Design da T106](T106-testes-de-backend-da-retirada.md) · 18 requisitos |
| T107 | E2E, capturas e documentação do RF16 | I4 | 2h | Qui 19/11 | T104 | ⬜ | [Design da T107](T107-e2e-capturas-e-documentacao-do-rf16.md) · 9 requisitos |

### PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile) (I7 · Sex 20/11 a Sáb 21/11 · 21h)

**Tasks:** T108 a T114 · **Depende de:** Sprint 2, PBI-22 (ficha para o vínculo da conta).

**Critério de aceite do PBI:** O participante entra no app com a conta vinculada pelo coordenador, vê a lista dos estudos em que está incluído e o detalhe de cada um, e sai do app; outros perfis são recusados com mensagem clara; o participante nunca vê dados de outro; existe pelo menos um fluxo E2E no app.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T108 | Esqueleto do app: navegação, cliente HTTP e contexto de autenticação | I7 | 3h | Sex 20/11 | Sprint 1 (app Expo com login, T12) | ⬜ | [Design da T108](T108-esqueleto-do-app-navegacao-cliente-http-e-contexto-de-autenticacao.md) · 15 requisitos |
| T109 | Acesso do participante e endpoints /me no backend | I7 | 4h | Sex 20/11 | Sprint 2 (participação e campo de usuário participante) | ⬜ | [Design da T109](T109-acesso-do-participante-e-endpoints-me-no-backend.md) · 14 requisitos |
| T110 | Login e logout no app com a API real | I7 | 3h | Sex 20/11 | T108 | ⬜ | [Design da T110](T110-login-e-logout-no-app-com-a-api-real.md) · 12 requisitos |
| T111 | Vínculo da conta do participante (backend e web) | I7 | 3h | Sáb 21/11 | T109, T97 (ficha do PBI-22) | ⬜ | [Design da T111](T111-vinculo-da-conta-do-participante-backend-e-web.md) · 14 requisitos |
| T112 | Telas Meus estudos e detalhe do estudo | I7 | 3h | Sáb 21/11 | T108, T109 | ⬜ | [Design da T112](T112-telas-meus-estudos-e-detalhe-do-estudo.md) · 11 requisitos |
| T113 | Testes de backend e de componente do app | I7 | 3h | Sáb 21/11 | T109, T110, T111, T112 | ⬜ | [Design da T113](T113-testes-de-backend-e-de-componente-do-app.md) · 16 requisitos |
| T114 | E2E mobile (Maestro), capturas e documentação | I7 | 2h | Sáb 21/11 | T110, T112 | ⬜ | [Design da T114](T114-e2e-mobile-maestro-capturas-e-documentacao.md) · 12 requisitos |

### PBI-25 · RF18 App: próximas visitas e histórico de comparecimento (I5 · Dom 22/11 a Seg 23/11 · 21h)

**Tasks:** T115 a T121 · **Depende de:** PBI-19, PBI-23, PBI-24.

**Critério de aceite do PBI:** O participante logado vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T115 | Contrato das consultas do participante | I5 | 2h | Dom 22/11 | T73, T109 | ⬜ | [Design da T115](T115-contrato-das-consultas-do-participante.md) · 10 requisitos |
| T116 | Backend das consultas de visitas do participante | I5 | 4h | Dom 22/11 | T73, T109, T115 | ⬜ | [Design da T116](T116-backend-das-consultas-de-visitas-do-participante.md) · 12 requisitos |
| T117 | Tela de próximas visitas | I5 | 4h | Dom 22/11 | T108, T115, T116 | ⬜ | [Design da T117](T117-tela-de-proximas-visitas.md) · 11 requisitos |
| T118 | Tela de histórico de comparecimento | I5 | 3h | Seg 23/11 | T117 | ⬜ | [Design da T118](T118-tela-de-historico-de-comparecimento.md) · 10 requisitos |
| T119 | Integração com a API real e tratamento de estados | I5 | 3h | Seg 23/11 | T116, T117, T118 | ⬜ | [Design da T119](T119-integracao-com-a-api-real-e-tratamento-de-estados.md) · 9 requisitos |
| T120 | Testes de backend e de componente | I5 | 3h | Seg 23/11 | T116, T119 | ⬜ | [Design da T120](T120-testes-de-backend-e-de-componente.md) · 14 requisitos |
| T121 | Capturas, fluxo E2E mobile das visitas e documentação | I5 | 2h | Seg 23/11 | T114, T119 | ⬜ | [Design da T121](T121-capturas-fluxo-e2e-mobile-das-visitas-e-documentacao.md) · 8 requisitos |

## Calendário de prazos por dia

Cada dia pertence a um único PBI; ao fim do segundo dia de cada janela, o PBI está 100% concluído e integrado na `main`. Os fins de semana e o feriado de sexta 20/11 (Dia da Consciência Negra) fazem parte das janelas dos PBIs 21, 24 e 25.

| Dia | PBI e responsável | Marco do dia | Tasks |
|---|---|---|---|
| Ter 10/11 | PBI-19 · I1 | Sprint Planning; regras validadas; Sprint 3 cadastrada; tabela da visita agendada, contrato e regras do agendamento | T94 (Planning), T73, T74, T75 |
| Qua 11/11 | PBI-19 · I1 | PBI-19 100%: endpoints, telas, testes e E2E do agendamento na `main` | T76, T77, T78, T79 |
| Qui 12/11 | PBI-20 · I3 | Máquina de estados, regras e endpoints do desfecho | T80, T81, T82 |
| Sex 13/11 | PBI-20 · I3 | PBI-20 100%: telas do desfecho, dados de demonstração, testes e E2E | T83, T84, T85, T86 |
| Sáb 14/11 | PBI-21 · I6 | Modelo, contrato e regras dos resultados | T87, T88, T89 |
| Dom 15/11 | PBI-21 · I6 | PBI-21 100%: endpoints, tela de resultados, testes e E2E | T90, T91, T92, T93 |
| Seg 16/11 | PBI-22 · I2 | Contrato e consulta agregada do histórico | T95, T96 |
| Ter 17/11 | PBI-22 · I2 | PBI-22 100%: ficha com histórico, testes, E2E do fluxo e checkpoint de meio de Sprint | T97, T98, T99, T100 |
| Qua 18/11 | PBI-23 · I4 | Migration da retirada, regras com cascata e bloqueios, endpoints | T101, T102, T103 |
| Qui 19/11 | PBI-23 · I4 | PBI-23 100%: tela, dados de demonstração, testes e E2E da retirada | T104, T105, T106, T107 |
| Sex 20/11 (feriado) | PBI-24 · I7 | Esqueleto do app, endpoints `/me` e login mobile | T108, T109, T110 |
| Sáb 21/11 | PBI-24 · I7 | PBI-24 100%: vínculo da conta, meus estudos, testes e E2E mobile base | T111, T112, T113, T114 |
| Dom 22/11 | PBI-25 · I5 | Contrato, backend das visitas do participante e tela de próximas visitas | T115, T116, T117 |
| Seg 23/11 | PBI-25 · I5 | PBI-25 100%; Sprint Review e Retrospectiva; burndown final | T118, T119, T120, T121 |

## Passagens de bastão

| Passagem | Quem entrega | Quem recebe | O que fica pronto na `main` | Prazo |
|---|---|---|---|---|
| PBI-19 para PBI-20 | I1 | I3 | Tabela e entidade da visita agendada (T73), regras e endpoints do agendamento, agenda com ponto de encaixe (`AcoesDaVisita`, `abasVisita.ts`) e dados de demonstração (T74) | Qua 11/11 |
| PBI-20 para PBI-21 | I3 | I6 | Máquina de estados da visita (T80), registro de desfecho, selo na agenda e visitas realizadas de demonstração (T84) | Sex 13/11 |
| PBI-21 para PBI-22 | I6 | I2 | Resultados das avaliações (T89 e T90) e dados de demonstração com resultados | Dom 15/11 |
| PBI-22 para PBI-23 | I2 | I4 | Ficha do participante com histórico e área de ações para a retirada e o vínculo da conta (T97) | Ter 17/11 |
| PBI-23 para PBI-24 | I4 | I7 | Coluna de situação ATIVO ou RETIRADO (T101), cancelamento em cascata e participante retirado de demonstração (T105) | Qui 19/11 |
| PBI-24 para PBI-25 | I7 | I5 | Esqueleto do app (T108), endpoints `/me` (T109), vínculo da conta (T111) e fluxo E2E mobile base (T114) | Sáb 21/11 |
| PBI-25 para a Review | I5 | Equipe | Próximas visitas e histórico de comparecimento no app, capturas e README | Seg 23/11 |

## Ordem de corte (se a capacidade real for menor)

1. A agenda do estudo por período (T77) e o filtro por situação do histórico (T97).
2. O reagendamento (T75 e T77) e o resumo de comparecimento do app (T118).
3. Só então os cenários de erro dos E2E.

**Não devem ser cortados:** o agendamento, o desfecho, os resultados, o histórico, a retirada, o login mobile e as listas de estudos e visitas do app: formam o fluxo de acompanhamento que a Sprint promete. Os itens de corte estão marcados nos documentos das tasks correspondentes.

## Como usar estes documentos

1. Leia as [convenções e o contrato compartilhado](00-convencoes-e-contrato-compartilhado.md) e o documento da sua task.
2. Cada requisito `RQ-Tnn-NN` é um item de revisão: o revisor do Pull Request confere um por um.
3. Escreva os testes **junto com o código**; cada PBI traz a sua task de testes de backend (e de componente) e a de E2E.
4. Ao terminar a janela, faça a passagem de bastão (tabela acima), zere o Remaining Work no Azure Boards e avise o dono do PBI seguinte.
5. Se o Planning (T94) ou a implementação mudar algo, corrija a seção 1 das convenções e o documento da task afetada **no mesmo dia**.
