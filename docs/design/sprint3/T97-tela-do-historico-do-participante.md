# T97 · Tela do histórico do participante

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Ter 17/11  
> **Depende de:** T95, T96  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
