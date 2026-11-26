# Documentos de design da Sprint 4

Um documento por task, com **o que implementar, em qual arquivo, com quais regras, quais testes** e a lista numerada de requisitos (`RQ-Tnnn-NN`), mais a origem deles (seção 9: especificação e planejamento, sem paráfrase).

> **Comece pelas [convenções e o contrato compartilhado](00-convencoes-e-contrato-compartilhado.md).** Ali estão os ajustes em relação ao planejamento, os 14 endpoints novos, os DTOs, os códigos de erro, as migrations, os dados de demonstração e os pontos de encaixe.

- **49 tasks** (T122 a T170), **49 documentos**, **434 requisitos numerados**, 7 PBIs, 146 horas, de 24/11 a 07/12/2026. Fonte: [Planejamento da Sprint 4](../../sprint4/Planejamento_Sprint_4_ClinicalStudyPB.md).
- Legenda: ✅ implementada · 🟡 parcial · ⬜ a implementar · 📋 entrega fora do código.

## Ajustes em relação ao planejamento (confirmar na T164)

A [seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê) explica os 23. Os que quebrariam a implementação:

- **Migrations `V7_*`**, não `V4_*`: o banco já está na `V6_24_1` e o Flyway recusaria uma versão menor.
- **`ContratoOpenApiSprint3Test` precisa mudar** na T123, senão os caminhos novos `/api/me/...` o fazem falhar.
- **`candidatos` não tem data de cadastro**: a T151 cria a coluna para o filtro de período.
- **A efetivação da retirada precisa de motivo** (mínimo de 5 caracteres no serviço do RF16), embora ele seja opcional no app.
- **Dados de demonstração em estudo novo**, e a T128 ajusta as contagens do `DemoDataIntegrationTest`.

## Índice das tasks por PBI

### PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app (I7 · Ter 24/11 a Qua 25/11 · 21h)

**Tasks:** T122 a T128

**Critério de aceite do PBI:** no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T122 | Modelo e migration da confirmação e dos pedidos do participante | I7 | 3h | Ter 24/11 | T164, Sprint 3 (T73, T109) | ⬜ | [Design da T122](T122-modelo-e-migration-da-confirmacao-e-dos-pedidos-do-participante.md) · 12 requisitos |
| T123 | Contrato OpenAPI do RF19 no app | I7 | 2h | Ter 24/11 | T122 | ⬜ | [Design da T123](T123-contrato-openapi-do-rf19-no-app.md) · 10 requisitos |
| T124 | Regras de confirmação e de pedido de reagendamento | I7 | 4h | Ter 24/11 | T122 | ⬜ | [Design da T124](T124-regras-de-confirmacao-e-de-pedido-de-reagendamento.md) · 10 requisitos |
| T125 | Endpoints /me do RF19 e Swagger | I7 | 3h | Qua 25/11 | T123, T124 | ⬜ | [Design da T125](T125-endpoints-me-do-rf19-e-swagger.md) · 12 requisitos |
| T126 | Telas do app: confirmar presença e pedir reagendamento | I7 | 4h | Qua 25/11 | T125 | ⬜ | [Design da T126](T126-telas-do-app-confirmar-presenca-e-pedir-reagendamento.md) · 11 requisitos |
| T127 | Testes de backend e de componente do RF19 no app | I7 | 3h | Qua 25/11 | T124, T126 | ⬜ | [Design da T127](T127-testes-de-backend-e-de-componente-do-rf19-no-app.md) · 8 requisitos |
| T128 | E2E mobile, capturas, dados de demonstração e passagem de bastão | I7 | 2h | Qua 25/11 | T126 | ⬜ | [Design da T128](T128-e2e-mobile-capturas-dados-de-demonstracao-e-passagem-de-bastao.md) · 12 requisitos |

### PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web (I4 · Qui 26/11 a Sex 27/11 · 21h)

**Tasks:** T129 a T135

**Critério de aceite do PBI:** o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T129 | Contrato e consulta dos pedidos do estudo | I4 | 3h | Qui 26/11 | T128 | ⬜ | [Design da T129](T129-contrato-e-consulta-dos-pedidos-do-estudo.md) · 8 requisitos |
| T130 | Regras de atendimento e recusa do reagendamento | I4 | 4h | Qui 26/11 | T129, Sprint 3 (T75, T81) | ⬜ | [Design da T130](T130-regras-de-atendimento-e-recusa-do-reagendamento.md) · 13 requisitos |
| T131 | Endpoints, autorização e Swagger do atendimento | I4 | 3h | Qui 26/11 | T130 | ⬜ | [Design da T131](T131-endpoints-autorizacao-e-swagger-do-atendimento.md) · 8 requisitos |
| T132 | Aba Solicitações do estudo | I4 | 4h | Sex 27/11 | T131 | ⬜ | [Design da T132](T132-aba-solicitacoes-do-estudo.md) · 11 requisitos |
| T133 | Presença confirmada na agenda e no histórico | I4 | 2h | Sex 27/11 | T125, T131 | ⬜ | [Design da T133](T133-presenca-confirmada-na-agenda-e-no-historico.md) · 7 requisitos |
| T134 | Testes de backend e de componente do atendimento | I4 | 3h | Sex 27/11 | T130, T132, T133 | ⬜ | [Design da T134](T134-testes-de-backend-e-de-componente-do-atendimento.md) · 8 requisitos |
| T135 | E2E web, capturas e passagem de bastão | I4 | 2h | Sex 27/11 | T132 | ⬜ | [Design da T135](T135-e2e-web-capturas-e-passagem-de-bastao.md) · 9 requisitos |

### PBI-28 · RF20 Retirada voluntária pelo app (I5 · Sáb 28/11 a Dom 29/11 · 21h)

**Tasks:** T136 a T142

**Critério de aceite do PBI:** o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T136 | Regras e contrato do pedido de retirada | I5 | 3h | Sáb 28/11 | T135 | ⬜ | [Design da T136](T136-regras-e-contrato-do-pedido-de-retirada.md) · 8 requisitos |
| T137 | Endpoints /me do pedido de retirada e Swagger | I5 | 3h | Sáb 28/11 | T136 | ⬜ | [Design da T137](T137-endpoints-me-do-pedido-de-retirada-e-swagger.md) · 8 requisitos |
| T138 | Efetivação da retirada pelo coordenador | I5 | 4h | Sáb 28/11 | T136, Sprint 3 (T102) | ⬜ | [Design da T138](T138-efetivacao-da-retirada-pelo-coordenador.md) · 10 requisitos |
| T139 | Tela do app: solicitar retirada | I5 | 3h | Dom 29/11 | T137 | ⬜ | [Design da T139](T139-tela-do-app-solicitar-retirada.md) · 10 requisitos |
| T140 | Pedidos de retirada na aba Solicitações e na ficha | I5 | 3h | Dom 29/11 | T132, T138 | ⬜ | [Design da T140](T140-pedidos-de-retirada-na-aba-solicitacoes-e-na-ficha.md) · 11 requisitos |
| T141 | Testes de backend e de componente do RF20 | I5 | 3h | Dom 29/11 | T138, T139, T140 | ⬜ | [Design da T141](T141-testes-de-backend-e-de-componente-do-rf20.md) · 9 requisitos |
| T142 | E2E mobile e web, capturas e passagem de bastão | I5 | 2h | Dom 29/11 | T139, T140 | ⬜ | [Design da T142](T142-e2e-mobile-e-web-capturas-e-passagem-de-bastao.md) · 8 requisitos |

### PBI-29 · RF21 Painel do estudo (I6 · Seg 30/11 a Ter 01/12 · 21h)

**Tasks:** T143 a T149

**Critério de aceite do PBI:** pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T143 | Definição dos indicadores e contrato do painel | I6 | 2h | Seg 30/11 | T142 | ⬜ | [Design da T143](T143-definicao-dos-indicadores-e-contrato-do-painel.md) · 9 requisitos |
| T144 | Consultas agregadas do painel no backend | I6 | 4h | Seg 30/11 | T143 | ⬜ | [Design da T144](T144-consultas-agregadas-do-painel-no-backend.md) · 8 requisitos |
| T145 | Endpoints, autorização e Swagger do painel | I6 | 2h | Seg 30/11 | T144 | ⬜ | [Design da T145](T145-endpoints-autorizacao-e-swagger-do-painel.md) · 5 requisitos |
| T146 | Tela do painel do estudo | I6 | 5h | Ter 01/12 | T145 | ⬜ | [Design da T146](T146-tela-do-painel-do-estudo.md) · 11 requisitos |
| T147 | Visão geral dos estudos na página inicial | I6 | 3h | Ter 01/12 | T145 | ⬜ | [Design da T147](T147-visao-geral-dos-estudos-na-pagina-inicial.md) · 6 requisitos |
| T148 | Testes de backend e de componente do painel | I6 | 3h | Ter 01/12 | T144, T146, T147 | ⬜ | [Design da T148](T148-testes-de-backend-e-de-componente-do-painel.md) · 6 requisitos |
| T149 | E2E, capturas e passagem de bastão | I6 | 2h | Ter 01/12 | T146 | ⬜ | [Design da T149](T149-e2e-capturas-e-passagem-de-bastao.md) · 8 requisitos |

### PBI-30 · RF22 Relatório de recrutamento e acompanhamento (I3 · Qua 02/12 a Qui 03/12 · 21h)

**Tasks:** T150 a T156

**Critério de aceite do PBI:** pesquisador e coordenador geram, para um estudo e um período opcional, o relatório de recrutamento e de acompanhamento em tela, exportam em CSV e imprimem; os números conferem com o painel; o relatório mostra quem gerou e quando.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T150 | Conteúdo e contrato do relatório | I3 | 2h | Qua 02/12 | T149 | ⬜ | [Design da T150](T150-conteudo-e-contrato-do-relatorio.md) · 9 requisitos |
| T151 | Consultas do relatório no backend | I3 | 4h | Qua 02/12 | T144, T150 | ⬜ | [Design da T151](T151-consultas-do-relatorio-no-backend.md) · 12 requisitos |
| T152 | Exportação em CSV | I3 | 3h | Qua 02/12 | T151 | ⬜ | [Design da T152](T152-exportacao-em-csv.md) · 8 requisitos |
| T153 | Endpoints, autorização e Swagger do relatório | I3 | 2h | Qui 03/12 | T151, T152 | ⬜ | [Design da T153](T153-endpoints-autorizacao-e-swagger-do-relatorio.md) · 5 requisitos |
| T154 | Tela do relatório e versão para impressão | I3 | 5h | Qui 03/12 | T153 | ⬜ | [Design da T154](T154-tela-do-relatorio-e-versao-para-impressao.md) · 10 requisitos |
| T155 | Testes de backend e de componente do relatório | I3 | 3h | Qui 03/12 | T151, T152, T154 | ⬜ | [Design da T155](T155-testes-de-backend-e-de-componente-do-relatorio.md) · 7 requisitos |
| T156 | E2E, capturas e passagem de bastão | I3 | 2h | Qui 03/12 | T154 | ⬜ | [Design da T156](T156-e2e-capturas-e-passagem-de-bastao.md) · 7 requisitos |

### PBI-31 · Qualidade, regressão e preparação da Release 2 (I1 · Sex 04/12 a Sáb 05/12 · 21h)

**Tasks:** T157 a T163

**Critério de aceite do PBI:** RF01 a RF22 passam na regressão; há E2E automatizado na web e no app; a cobertura de linhas do código de negócio do backend está em 80% ou mais; o sistema sobe em um clone limpo seguindo o README; os defeitos críticos estão corrigidos e o relatório de testes está pronto para a documentação.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T157 | Dados de demonstração consolidados da Release 2 | I1 | 3h | Sex 04/12 | T156 | ⬜ | [Design da T157](T157-dados-de-demonstracao-consolidados-da-release-2.md) · 8 requisitos |
| T158 | Suíte E2E web de regressão | I1 | 4h | Sex 04/12 | T157 | ⬜ | [Design da T158](T158-suite-e2e-web-de-regressao.md) · 9 requisitos |
| T159 | E2E mobile no CI (Maestro com emulador) | I1 | 4h | Sex 04/12 | T157 | ⬜ | [Design da T159](T159-e2e-mobile-no-ci-maestro-com-emulador.md) · 9 requisitos |
| T160 | Cobertura e relatórios de teste no CI | I1 | 3h | Sáb 05/12 | T158 | ⬜ | [Design da T160](T160-cobertura-e-relatorios-de-teste-no-ci.md) · 8 requisitos |
| T161 | Execução em clone limpo e instruções para a Release 3 | I1 | 3h | Sáb 05/12 | T157 | ⬜ | [Design da T161](T161-execucao-em-clone-limpo-e-instrucoes-para-a-release-3.md) · 10 requisitos |
| T162 | Triagem e correção de defeitos da Release 2 | I1 | 3h | Sáb 05/12 | T158, T159 | ⬜ | [Design da T162](T162-triagem-e-correcao-de-defeitos-da-release-2.md) · 8 requisitos |
| T163 | Relatório de testes e passagem de bastão | I1 | 1h | Sáb 05/12 | T160, T162 | ⬜ | [Design da T163](T163-relatorio-de-testes-e-passagem-de-bastao.md) · 9 requisitos |

### PBI-32 · Planning, acompanhamento e entrega da Release 2 (I2 · Ter 24/11 e Dom 06/12 a Seg 07/12 · 20h)

**Tasks:** T164 a T170

**Critério de aceite do PBI:** a Sprint foi planejada e acompanhada no Azure Boards; README principal e README da Release 2 estão completos, com o processo das Sprints 3 e 4; os diagramas renderizam no GitHub; a tag release2 e a GitHub Release existem; a apresentação foi ensaiada.

| Task | Título | Resp. | Horas | Prazo | Depende de | Situação | Documento de design |
|---|---|---|---|---|---|---|---|
| T164 | Planning: validar regras, confirmar a Release 2 e cadastrar a Sprint 4 | I2 | 2h | Ter 24/11 | Nenhuma | 📋 | [Design da T164](T164-planning-validar-regras-confirmar-a-release-2-e-cadastrar-a-sprint-4.md) · 11 requisitos |
| T165 | Acompanhamento das passagens de bastão e checkpoint | I2 | 2h | Qua 02/12 | Leitura de todos os PBIs | 📋 | [Design da T165](T165-acompanhamento-das-passagens-de-bastao-e-checkpoint.md) · 7 requisitos |
| T166 | Diagramas atualizados da Release 2 | I2 | 3h | Dom 06/12 | T163 | ⬜ | [Design da T166](T166-diagramas-atualizados-da-release-2.md) · 6 requisitos |
| T167 | README principal atualizado | I2 | 3h | Dom 06/12 | T163, T166 | ⬜ | [Design da T167](T167-readme-principal-atualizado.md) · 10 requisitos |
| T168 | README da Release 2 e processo de desenvolvimento | I2 | 5h | Seg 07/12 (manhã) | T167 | ⬜ | [Design da T168](T168-readme-da-release-2-e-processo-de-desenvolvimento.md) · 10 requisitos |
| T169 | Tag release2 e GitHub Release | I2 | 2h | Seg 07/12 (antes da apresentação) | T168 | 📋 | [Design da T169](T169-tag-release2-e-github-release.md) · 7 requisitos |
| T170 | Roteiro e ensaio da apresentação da Release 2 | I2 | 3h | Seg 07/12 | T169 | 📋 | [Design da T170](T170-roteiro-e-ensaio-da-apresentacao-da-release-2.md) · 8 requisitos |

## Calendário de prazos por dia

| Dia       | Marco do dia                                                                                              | Tasks que devem estar concluídas |
|-----------|-----------------------------------------------------------------------------------------------------------|----------------------------------|
| Ter 24/11 | Sprint Planning; início do PBI-26                                                                         | T164, T122, T123, T124           |
| Qua 25/11 | PBI-26 concluído; bastão I7 → I4                                                                          | T125, T126, T127, T128           |
| Qui 26/11 | Backend do atendimento                                                                                    | T129, T130, T131                 |
| Sex 27/11 | PBI-27 concluído; bastão I4 → I5                                                                          | T132, T133, T134, T135           |
| Sáb 28/11 | Backend do pedido e da efetivação da retirada                                                             | T136, T137, T138                 |
| Dom 29/11 | PBI-28 concluído; bastão I5 → I6                                                                          | T139, T140, T141, T142           |
| Seg 30/11 | Backend do painel                                                                                         | T143, T144, T145                 |
| Ter 01/12 | PBI-29 concluído; bastão I6 → I3                                                                          | T146, T147, T148, T149           |
| Qua 02/12 | Backend do relatório; checkpoint                                                                          | T150, T151, T152, T165           |
| Qui 03/12 | PBI-30 concluído; congelamento de funcionalidades; bastão I3 → I1                                         | T153, T154, T155, T156           |
| Sex 04/12 | Dados consolidados e suítes E2E                                                                           | T157, T158, T159                 |
| Sáb 05/12 | Congelamento de código às 18h; bastão I1 → I2                                                             | T160, T161, T162, T163           |
| Dom 06/12 | Diagramas e README principal                                                                              | T166, T167                       |
| Seg 07/12 | Entrega da Release 2: README da release, tag, GitHub Release, apresentação, Sprint Review e Retrospectiva | T168, T169, T170                 |

## Passagens de bastão

| Passagem | Quando    | O que o próximo dono recebe                                                                       |
|----------|-----------|---------------------------------------------------------------------------------------------------|
| I2 → I7  | Ter 24/11 | Regras validadas, Sprint 4 cadastrada no Azure Boards e carry-over levantado (T164)               |
| I7 → I4  | Qua 25/11 | Tabela de pedidos do participante, regras do RF19 e pedidos de demonstração (T122, T128)          |
| I4 → I5  | Sex 27/11 | Aba Solicitações pronta para o tipo RETIRADA e recusa automática de pedidos (T130, T132)          |
| I5 → I6  | Dom 29/11 | Dados de demonstração com todas as situações de visita, pedido e retirada (T142)                  |
| I6 → I3  | Ter 01/12 | Serviço de leitura e definições dos indicadores do painel (T143, T144)                            |
| I3 → I1  | Qui 03/12 | RF19 a RF22 na main: congelamento de funcionalidades (T156)                                       |
| I1 → I2  | Sáb 05/12 | Relatório de testes, problemas conhecidos e commit candidato à tag: congelamento de código (T163) |

## Ordem de corte (se a capacidade real for menor)

Se a capacidade real for menor, a ordem de corte é: primeiro a visão geral dos estudos na página inicial (T147), depois o E2E mobile no CI (T159, mantendo a execução local) e a versão para impressão do relatório (parte da T154), e só então o filtro de presenças confirmadas na agenda (parte da T133). RF19, RF20, RF21, RF22 e a entrega da Release 2 não devem ser cortados, porque completam a especificação e são avaliados pela tag.

## Como usar estes documentos

1. Leia as convenções e depois o documento da sua task, na ordem das dependências.
2. Implemente cada `RQ` e marque os critérios de aceite no Pull Request.
3. Se algo do contrato mudar, altere as convenções no mesmo Pull Request.
4. Ao terminar o PBI, escreva a nota `docs/sprint4/bastao-pbi-NN.md`.
