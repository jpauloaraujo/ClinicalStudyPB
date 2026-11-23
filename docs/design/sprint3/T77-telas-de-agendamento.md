# T77 · Telas de agendamento

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Qua 11/11  
> **Depende de:** T74, T76  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Entregar as telas do RF12 sobre a API real: progresso de visitas por participante, ficha de agendamento (agendar e reagendar com a data sugerida), agenda do estudo por período e a tela da visita com o ponto de encaixe para os próximos PBIs. Também abre a aba Participantes ao pesquisador, em modo somente leitura.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Aba Participantes atual: `abas/AbaParticipantes.tsx` (lista e contador "X de N vagas"); o estudo e o perfil vêm de `useEstudoDetalhe()` e `useUsuarioLogado()`.
- Padrões: `useCarregamento`, `src/api/*.ts` (um módulo por recurso, `api` do Axios), `converterErro` (`src/api/erros.ts`), `Confirmacao` e `Campo` (`src/components/ui`), `mensagensInclusao.ts` (tradução de códigos), CSS em `recrutamento.css`.
- `abas.ts` é a fonte única das abas e dos perfis; `router.tsx` usa `perfisDaAba(...)` em cada rota.
- O E2E `acesso-por-perfil.spec.ts` afirma hoje que o pesquisador **não** vê Candidatos e Participantes: precisa ser atualizado nesta task.
- Sem mocks: as telas usam a API real da T76.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/agendamento.ts` | Criar | `agendamentoApi`: `visitasDoParticipante`, `agendar`, `reagendar`, `agenda`, `participantesDaAgenda` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas.ts` | Alterar | `participantes` para os dois perfis; nova aba `agenda` |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Alterar | Rotas `agenda`, `participantes/:participacaoId/agendamento` e `.../visitas/:visitaAgendadaId` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.tsx` | Alterar | Mostra "X de N visitas agendadas" e o acesso ao agendamento |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAgenda.tsx` | Criar | Agenda do estudo por período e situação |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AgendamentoParticipantePage.tsx` | Criar | Visitas previstas, janela, data sugerida, Agendar e Reagendar |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/VisitaAgendadaPage.tsx` | Criar | Tela da visita com as abas de `abasVisita.ts` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/abasVisita.ts` | Criar | Fonte única das abas da visita (`dados` agora; `avaliacoes` na T91) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AcoesDaVisita.tsx` | Criar | Ponto de encaixe das ações da visita (agora: Reagendar) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/mensagensAcompanhamento.ts` | Criar | Tradução dos códigos de erro do agendamento |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/*.test.tsx` | Criar | Testes de componente |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T77-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Aba Participantes e permissões

- **RQ-T77-01** Em `abas.ts`, `participantes` passa a `['PESQUISADOR', 'COORDENADOR']` e entra `{ caminho: 'agenda', rotulo: 'Agenda', perfis: ['PESQUISADOR', 'COORDENADOR'] }`; as rotas novas usam `perfisDaAba`.
- **RQ-T77-02** A lista da aba Participantes passa a mostrar, por participante, código, nome fictício, data de inclusão e o progresso **"X de N visitas agendadas"** (dados de `participantesDaAgenda`), mantendo o contador "X de N vagas" da Sprint 2.
- **RQ-T77-03** Cada linha tem o botão **Agendamento** (só coordenador) que abre `/estudos/:estudoId/participantes/:participacaoId/agendamento`; o pesquisador vê a lista e a agenda, mas nenhum botão de escrita (somente leitura).
- **RQ-T77-04** Atualizar `acesso-por-perfil.spec.ts` e os testes de `AbaParticipantes`/`EstudoDetalhePage` para o novo conjunto de abas do pesquisador.

### Ficha de agendamento do participante (`AgendamentoParticipantePage`)

- **RQ-T77-05** Listar todas as visitas previstas do estudo (de `visitasDoParticipante`) com nome, dia previsto, tolerância, **janela válida** (`dd/MM/yyyy a dd/MM/yyyy`), **data sugerida** e a situação do agendamento como **rótulo de texto** ("Agendada", "Realizada"... e "Não agendada" quando não há); a T83 troca esse rótulo pelo `SeloSituacaoVisita`.
- **RQ-T77-06** Botão **Agendar** nas visitas sem agendamento (só coordenador): abre um formulário com `data` e `hora` **já preenchidas com a sugestão** (hora padrão 09:00), os limites `min`/`max` do campo de data iguais à janela (o servidor continua validando) e o botão Salvar.
- **RQ-T77-07** Botão **Reagendar** nas visitas `AGENDADA` (só coordenador): mesmo formulário, preenchido com a data atual do agendamento, na mesma janela.
- **RQ-T77-08** Mostrar as mensagens de erro das regras traduzidas por código (`ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_JA_AGENDADA`, `DATA_FORA_DA_JANELA`, `DATA_NO_PASSADO`, `VISITA_NAO_ESTA_AGENDADA`) e, para `DATA_FORA_DA_JANELA`, a janela válida; os erros por campo aparecem junto do campo. Para código desconhecido, mostrar a mensagem do servidor.
- **RQ-T77-09** Visitas com desfecho (`REALIZADA`, `AUSENTE`, `CANCELADA`) aparecem sem botões de escrita e com o rótulo e a data real; o pesquisador vê tudo em leitura.
- **RQ-T77-10** Com o estudo fora de recrutamento ou execução, os botões ficam desabilitados com a explicação.
- **RQ-T77-11** Depois de salvar, recarregar a lista e mostrar uma confirmação; estados de carregamento, erro (com "Tentar de novo") e participante sem visitas previstas (estudo sem visitas: texto explicativo).

### Agenda do estudo (`AbaAgenda`)

- **RQ-T77-12** Tabela com data e hora, participante, visita e situação, ordenada por data, **paginada** (20 por página) e com filtros de período (`de`/`ate`) e situação; limpar filtros restaura a lista.
- **RQ-T77-13** Cada linha tem o link para a tela da visita (`VisitaAgendadaPage`); a linha usa `AcoesDaVisita` (ponto de encaixe) com a ação Reagendar (só coordenador, só visita `AGENDADA`).
- **RQ-T77-14** Lista vazia com texto claro ("Nenhuma visita no período"), erro do servidor e carregamento.
- **RQ-T77-15** **Item de corte** (ordem de corte do planejamento): se a capacidade faltar, a agenda por período é o primeiro item a sair; o agendamento por participante não pode ser cortado.

### Tela da visita (`VisitaAgendadaPage`) e pontos de encaixe

- **RQ-T77-16** Rota `/estudos/:estudoId/participantes/:participacaoId/visitas/:visitaAgendadaId`, com os dados da visita (participante, visita prevista, data e hora, situação, janela, criado e alterado por) e as abas definidas em `abasVisita.ts` (`{ caminho, rotulo, perfis }`), no mesmo modelo de `abas.ts`. Nesta task existe só a aba `dados`.
- **RQ-T77-17** `AcoesDaVisita` recebe `{ visita, aoAlterar }` e renderiza os botões de ação por perfil e situação; agora tem **Reagendar**. A T83 acrescenta **Registrar desfecho** e a T91 acrescenta a aba `avaliacoes` **sem reescrever** a tela. Documentar esse contrato em comentário no topo dos dois arquivos.

### Qualidade

- **RQ-T77-18** Todas as chamadas passam por `agendamentoApi`; nenhum `fetch` ou `axios` direto nas telas; erros convertidos com `converterErro`.
- **RQ-T77-19** Textos em português, rótulos acessíveis (`aria-label`, regiões nomeadas como "Visitas do participante" e "Agenda do estudo"), foco no primeiro campo com erro e botões desabilitados durante o envio.
- **RQ-T77-20** Escrever os testes de componente (seção 6) e passar em `npm run lint`, `tsc -b` e `npm test`.

## 5. Contrato, regras e códigos de erro

- Consome as operações 1 a 5 da seção 2 das convenções; tipos de `src/api/tipos.ts`.
- Mensagens por código em `mensagensAcompanhamento.ts` (mesmo padrão de `mensagensInclusao.ts`).

## 6. Testes

- `AgendamentoParticipantePage.test.tsx`: preenche a sugestão, agenda (chama a API com `visitaPlanejadaId` e `dataHora`), reagenda, mostra a janela no erro `DATA_FORA_DA_JANELA`, traduz cada código, perfil pesquisador somente leitura, estudo fora de recrutamento e execução, carregamento e erro.
- `AbaAgenda.test.tsx`: lista, filtros, paginação, vazio, erro e perfil.
- `AbaParticipantes.test.tsx` atualizado: progresso "X de N visitas agendadas" e botão Agendamento só para o coordenador.
- `VisitaAgendadaPage.test.tsx`: dados e `AcoesDaVisita` por perfil e situação.

## 7. Critérios de aceite

- [ ] O coordenador agenda uma visita com a data sugerida e a reagenda, vendo a janela válida.
- [ ] Datas fora da janela e estudos fora de recrutamento ou execução mostram mensagens claras.
- [ ] O pesquisador vê participantes, agenda e visitas em modo somente leitura.
- [ ] A tela da visita e `AcoesDaVisita` estão prontas para os PBIs 20 e 21 acrescentarem suas partes.

## 8. Cuidados e fora de escopo

- Não criar a ficha do participante com histórico (T97): esta task entrega só o agendamento e a tela da visita.
- Não escrever o desfecho (T83) nem os resultados (T91).
- Atualizar o E2E de acesso por perfil, senão o CI quebra.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF12** (Módulo D — Acompanhamento; interface Web): “Agendar visitas para os participantes incluídos.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Ao fechar, em 11/11, este PBI deixa na main a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 2 | Uma visita agendada por visita prevista e por participante. A data deve ser de hoje em diante e ficar dentro da janela da visita prevista: data de inclusão somada ao dia previsto, mais ou menos os dias de tolerância. Fora da janela, o sistema recusa com 409 e mostra a janela válida | T73, T75, T77 |

### 9.4 Texto original da task no planejamento

> **T77 · Telas de agendamento.** Primeiro crie, sobre a API real da T76, a lista de participantes incluídos do estudo, com o progresso "X de N visitas agendadas". Depois crie a ficha de agendamento do participante com as visitas previstas e as datas sugeridas, com os botões Agendar (data e hora já preenchidas com a sugestão) e Reagendar, mostrando a janela válida e as mensagens de erro das regras. Em seguida crie a visualização da agenda do estudo por período, com um componente de ações da visita como ponto de encaixe para o PBI-20 e o PBI-21, e deixe tudo somente leitura para o pesquisador. Por fim, escreva os testes de componente.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Testes automatizados de componentes nas interfaces web e mobile.” Em componentes web, testar o “comportamento das telas: validação de formulários, botões, mensagens de erro”. Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
