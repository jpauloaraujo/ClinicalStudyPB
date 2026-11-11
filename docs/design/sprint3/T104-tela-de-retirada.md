# T104 · Tela de retirada

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Qui 19/11  
> **Depende de:** T97, T101, T103  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Acrescentar à ficha do participante o botão e o diálogo de retirada, com confirmação clara da irreversibilidade e do cancelamento das visitas futuras, e mostrar a marca **RETIRADO** na lista, na ficha, no histórico e na aba de resultados, escondendo as ações que deixam de fazer sentido.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A ficha (`FichaParticipantePage`) e a área de ações (`AreaAcoesParticipante`) foram entregues na T97; a retirada entra por esse ponto de encaixe.
- Padrões: `Confirmacao` (`src/components/ui`), `Campo`, `converterErro`, `mensagensAcompanhamento.ts`; `AbaParticipantes` (T77), `AgendamentoParticipantePage`, `VisitaAgendadaPage` e `AbaAvaliacoesVisita` (T91) têm ações que devem respeitar a situação.
- O backend do histórico e da lista de participantes precisa de pequenos acréscimos para a tela (item abaixo).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/retirada.ts` | Criar | `retiradaApi.registrar` e `.buscar` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/DialogoRetirada.tsx` | Criar | Diálogo com motivo, quem solicitou e data, e a confirmação |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AreaAcoesParticipante.tsx` | Alterar | Botão **Registrar retirada** |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/FichaParticipantePage.tsx` | Alterar | Marca RETIRADO e dados da retirada |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/LinhaDoTempoVisitas.tsx` | Alterar | Marca RETIRADO no histórico |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAvaliacoesVisita.tsx` | Alterar | Aviso e somente leitura para participante retirado |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AgendamentoParticipantePage.tsx` | Alterar | Sem botões de escrita para retirado |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.tsx` | Alterar | Marca RETIRADO na lista |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/HistoricoParticipanteResponse.java` | Alterar | `participante` ganha `situacao` e `retirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/ParticipanteAgendaResponse.java` | Alterar | Acrescenta `situacaoParticipacao` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/HistoricoParticipanteService.java` | Alterar | Preenche a marca de retirada |
| `docs/api/openapi-sprint3.yaml` | Alterar | Os campos novos do histórico e da agenda de participantes |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/*.test.tsx` | Criar | Testes de componente |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T104-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Botão e diálogo de retirada

- **RQ-T104-01** Em `AreaAcoesParticipante`, o botão **Registrar retirada** aparece **só para o coordenador**, só com o participante `ATIVO` e o estudo **não encerrado**; desabilitado com explicação nos demais casos e ausente para o pesquisador.
- **RQ-T104-02** `DialogoRetirada` com **motivo** (obrigatório, 5 a 500 caracteres, contador), **quem solicitou** (Participante, Pesquisador ou Coordenação) e **data da retirada** (padrão hoje, máximo hoje), validados também no cliente.
- **RQ-T104-03** Etapa de **confirmação** depois de preencher: aviso de que a retirada é **irreversível**, de que as visitas futuras serão **canceladas** e **a quantidade de visitas afetadas** (as visitas `AGENDADA` com data futura, contadas no cliente a partir da lista de visitas do participante); se existirem visitas **vencidas sem desfecho**, avisar que elas devem ser registradas antes (o desfecho fica bloqueado depois da retirada).
- **RQ-T104-04** Ao confirmar: chamar `retiradaApi.registrar`, fechar o diálogo, recarregar a ficha e mostrar a confirmação com o total de visitas canceladas (`visitasCanceladas`).
- **RQ-T104-05** Tratar os erros: **400** (`MOTIVO_OBRIGATORIO`, `DATA_RETIRADA_FUTURA`, campo por campo) e **409** (`PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO`), traduzidos por código em `mensagensAcompanhamento.ts`; código desconhecido mostra a mensagem do servidor.

### Marca RETIRADO e ações escondidas

- **RQ-T104-06** Mostrar a marca **RETIRADO** (texto e cor) na **lista de participantes**, no cabeçalho da **ficha**, na **linha do tempo do histórico** e na **aba de resultados**; na ficha, exibir a data, o motivo e quem solicitou (de `GET .../retirada`).
- **RQ-T104-07** Para participante retirado, **esconder ou desabilitar** as ações que não fazem mais sentido: Agendar, Reagendar, Registrar desfecho, Registrar retirada e o formulário de resultados; o histórico continua visível e **preservado**.
- **RQ-T104-08** Na aba **Avaliações** (T91), acrescentar o aviso de **participante retirado** quando `bloqueio = PARTICIPANTE_RETIRADO`, com os campos somente leitura.

### Acréscimos no backend

- **RQ-T104-09** `HistoricoParticipanteResponse.participante` ganha `situacao` (`ATIVO`/`RETIRADO`) e `retirada` (`dataRetirada`, `motivo`, `solicitante`, nulo se ativo), preenchidos por `HistoricoParticipanteService` (ponto de extensão deixado na T96); `ParticipanteAgendaResponse` ganha `situacaoParticipacao`.
- **RQ-T104-10** Atualizar o YAML da Sprint 3 e regenerar os tipos (`npm run api:tipos`); testes de backend do que foi acrescentado (resposta com e sem retirada).

### Qualidade

- **RQ-T104-11** Testes de componente (seção 6); `npm run lint`, `tsc -b` e `npm test` sem erro; rótulos acessíveis (diálogo com título "Registrar retirada", foco no primeiro campo).
- **RQ-T104-12** Entra pelo encaixe `AreaAcoesParticipante`: não reescrever a ficha.

## 5. Contrato, regras e códigos de erro

- Consome as operações 11 e 12 da seção 2 das convenções e os campos novos do histórico.

## 6. Testes

- `DialogoRetirada.test.tsx`: validações (motivo curto, data futura), a confirmação com a quantidade de visitas afetadas e o aviso das vencidas, envio do corpo certo, erros 400 e 409 e sem envio duplo.
- `AreaAcoesParticipante.test.tsx`: botão por perfil, situação e estado do estudo.
- `FichaParticipantePage`, `AbaParticipantes` e `AbaAvaliacoesVisita`: marca RETIRADO, ações escondidas e aviso.
- Backend: `HistoricoParticipanteEndpointTest` com participante retirado (`situacao` e `retirada`).

## 7. Critérios de aceite

- [ ] O coordenador registra a retirada pela interface, vendo antes a quantidade de visitas que serão canceladas e o aviso de irreversibilidade.
- [ ] A marca RETIRADO aparece na lista, na ficha, no histórico e nos resultados, e as ações de escrita somem.
- [ ] Os erros 400 e 409 mostram mensagens claras.

## 8. Cuidados e fora de escopo

- Nenhuma opção de desfazer a retirada.
- O pedido de retirada pelo próprio participante no app é do RF20 (Sprint 4).

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
