# T97 · Tela do histórico do participante

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Ter 17/11  
> **Depende de:** T95, T96  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Entregar a ficha do participante (aberta pela aba Participantes) com os cartões de resumo e a linha do tempo das visitas, e deixar nela a área de ações que a retirada (T104) e o vínculo da conta (T111) vão usar.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A aba Participantes (T77) lista os participantes com o progresso de visitas; a ficha de **agendamento** (`AgendamentoParticipantePage`) e a tela da visita (`VisitaAgendadaPage`) já existem. Esta task cria a ficha com o **histórico**, em `/estudos/:estudoId/participantes/:participacaoId`.
- `SeloSituacaoVisita` (T83) já cobre as quatro situações e ganha "Não agendada" para o histórico se ainda não tiver.
- Padrões de tela: `useCarregamento`, `converterErro`, `useEstudoDetalhe`/`useUsuarioLogado`, `FichaCandidatoPage.tsx` (ficha com seções).
- O contrato do histórico é o da T95 e a API da T96.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/historico.ts` | Criar | `historicoApi.buscar(participacaoId)` |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Alterar | Rota `participantes/:participacaoId` (perfis dos dois) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.tsx` | Alterar | Linha do participante abre a ficha |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/FichaParticipantePage.tsx` | Criar | Ficha com resumo, linha do tempo e área de ações |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/LinhaDoTempoVisitas.tsx` | Criar | Visitas expansíveis com resultados |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AreaAcoesParticipante.tsx` | Criar | Ponto de encaixe das ações por perfil e situação |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/SeloSituacaoVisita.tsx` | Alterar | Inclui "Não agendada" |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/*.test.tsx` | Criar | Testes de componente |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T97-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Ficha e resumo

- **RQ-T97-01** Rota `/estudos/:estudoId/participantes/:participacaoId` para **pesquisador e coordenador**; a linha do participante na aba Participantes (nome ou botão "Ficha") abre a ficha; o botão **Agendamento** da T77 continua levando à ficha de agendamento.
- **RQ-T97-02** Cabeçalho com código, nome fictício e data de inclusão, e **cartões de resumo**: **Realizadas**, **Ausentes**, **Canceladas** e **Pendentes** (valores do `resumo` do servidor, sem recalcular no cliente).
- **RQ-T97-03** Para o coordenador, link **"Agendar visitas"** na ficha, apontando para `/participantes/:participacaoId/agendamento`.

### Linha do tempo das visitas

- **RQ-T97-04** Lista **em ordem** (pelo dia previsto) de **todas as visitas previstas**, cada uma com nome, dia previsto, data sugerida, `SeloSituacaoVisita` e as datas disponíveis (agendada e real); visitas não agendadas aparecem com "Não agendada".
- **RQ-T97-05** Cada visita é **expansível**: ao abrir, mostra data e hora agendadas e reais, motivo e observação e os **resultados** (nome da atividade, valor, unidade e observação) ou "Sem resultados registrados"; as visitas agendadas ou realizadas têm o link para a tela da visita (`VisitaAgendadaPage`).
- **RQ-T97-06** **Filtro por situação** (todas, não agendada, agendada, realizada, ausente, cancelada), com a contagem de itens exibidos. **Item de corte** (ordem de corte do planejamento): o filtro é o primeiro a sair se faltar capacidade; a linha do tempo e o resumo não podem ser cortados.
- **RQ-T97-07** Estados de **carregamento**, **lista vazia** (estudo sem visitas previstas) e **erro** do servidor com "Tentar de novo"; 404 (participante inexistente) mostra mensagem clara.

### Área de ações (ponto de encaixe)

- **RQ-T97-08** `AreaAcoesParticipante` recebe `{ participacao, perfil, aoAlterar }` e renderiza a lista de botões de ação por perfil e situação; nesta task só mostra o link "Agendar visitas" (coordenador). A **T104** acrescenta "Registrar retirada" e a **T111** acrescenta "Vincular conta" **sem reescrever** a ficha. Documentar esse contrato em comentário no topo do arquivo.
- **RQ-T97-09** Somente leitura para o pesquisador: sem botões de escrita.

### Acabamento

- **RQ-T97-10** Regiões nomeadas ("Resumo do histórico" e "Visitas do participante") e rótulos acessíveis; selo sempre com texto; datas formatadas em português (`dd/MM/yyyy HH:mm`).
- **RQ-T97-11** Testes de componente (seção 6), `npm run lint`, `tsc -b` e `npm test` sem erro.

## 5. Contrato, regras e códigos de erro

- Consome a operação 10 da seção 2 das convenções.

## 6. Testes

- `FichaParticipantePage.test.tsx`: resumo (4 cartões), todas as visitas na ordem, não agendadas, resultados ao expandir, filtro, carregamento, erro, 404, link de agendamento só para o coordenador e somente leitura para o pesquisador.
- `LinhaDoTempoVisitas.test.tsx` e `AreaAcoesParticipante.test.tsx`.
- `AbaParticipantes.test.tsx` atualizado (abrir a ficha).

## 7. Critérios de aceite

- [ ] Pesquisador e coordenador abrem a ficha e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e o resumo com realizadas, ausentes, canceladas e pendentes.
- [ ] A área de ações está pronta para a retirada e o vínculo de conta.

## 8. Cuidados e fora de escopo

- Não implementar a retirada (T104) nem o vínculo de conta (T111): só o encaixe.
- O histórico é somente leitura.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF15** (Módulo D — Acompanhamento; interface Web): “Consultar o histórico de visitas e avaliações de um participante.”
- Este PBI reúne também o **acompanhamento da Sprint** (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO.

### 9.2 Critério de aceite do PBI (planejamento)

> Pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Este PBI também reúne o acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO. O Planning (T94) é uma cerimônia da terça 10/11 e acontece antes da janela do PBI.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 5 | O histórico é só leitura para pesquisador e coordenador e mostra todas as visitas previstas do participante, inclusive as ainda não agendadas, com situação, datas e resultados | T95, T96, T97 |

### 9.4 Texto original da task no planejamento

> **T97 · Tela do histórico do participante.** Primeiro crie, sobre a API da T96, a ficha do participante (aberta pela aba Participantes) com os cartões de resumo: realizadas, ausentes, canceladas e pendentes. Depois crie a linha do tempo das visitas, cada uma expansível para mostrar os resultados, com filtro por situação e estados de carregamento, lista vazia e erro. Em seguida deixe na ficha uma área de ações como ponto de encaixe para a retirada (T104) e o vínculo da conta (T111). Por fim, escreva os testes de componente.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Testes automatizados de componentes nas interfaces web e mobile.” Em componentes web, testar o “comportamento das telas: validação de formulários, botões, mensagens de erro”. Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
