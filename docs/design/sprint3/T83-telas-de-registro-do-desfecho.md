# T83 · Telas de registro do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 4h · **Prazo:** Sex 13/11  
> **Depende de:** T80, T82  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Acrescentar à agenda e à tela da visita (entregues no PBI-19) o selo de situação e o diálogo de registro do desfecho (realizada, ausente ou cancelada), sem reescrever a agenda: o encaixe é o componente `AcoesDaVisita`.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Entregues na T77: `AbaAgenda`, `AgendamentoParticipantePage`, `VisitaAgendadaPage` (com `abasVisita.ts`) e `AcoesDaVisita` (hoje só com Reagendar). Os rótulos de situação eram texto simples e passam a usar o `SeloSituacaoVisita` criado aqui.
- Padrões de diálogo e formulário: `Confirmacao` e `Campo` (`src/components/ui`), `SecaoConsentimento.tsx` (formulário com confirmação e erro), `mensagensInclusao.ts`.
- API real da T82: `visitasAgendadasApi` (criar em `src/api/visitasAgendadas.ts`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/visitasAgendadas.ts` | Criar | `visitasAgendadasApi.buscar` e `.registrarDesfecho` (os resultados entram na T91) |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Alterar | Reexporta `DesfechoRequest` e `TipoDesfecho` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/SeloSituacaoVisita.tsx` | Criar | Rótulo e cor por situação, com texto |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/DialogoDesfecho.tsx` | Criar | Diálogo com as três opções |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AcoesDaVisita.tsx` | Alterar | Botão **Registrar desfecho** |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/VisitaAgendadaPage.tsx` | Alterar | Mostra o desfecho registrado, somente leitura |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAgenda.tsx` | Alterar | Usa o selo e o botão de desfecho por linha |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AgendamentoParticipantePage.tsx` | Alterar | Usa o selo |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/mensagensAcompanhamento.ts` | Alterar | Códigos do desfecho |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/*.test.tsx` | Criar | Testes do diálogo, do selo e do motivo obrigatório |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T83-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Selo de situação (`SeloSituacaoVisita`)

- **RQ-T83-01** Componente `SeloSituacaoVisita({ situacao })` com **rótulo em texto e cor**: Agendada, Realizada, Ausente e Cancelada (e "Não agendada" para o histórico, T97). O texto sempre aparece, para não depender só da cor (acessibilidade); contraste suficiente em fundo claro.
- **RQ-T83-02** Substituir os rótulos simples da T77 (agenda, ficha de agendamento e tela da visita) pelo selo.

### Diálogo e botão

- **RQ-T83-03** Em `AcoesDaVisita`, o botão **Registrar desfecho** aparece só para o coordenador, só com a visita `AGENDADA` e o estudo em recrutamento ou execução; desabilitado com explicação nos demais casos (por exemplo, estudo encerrado) e ausente para o pesquisador.
- **RQ-T83-04** `DialogoDesfecho` com três opções (radio): **Realizada** (campo data e hora, padrão agora, máximo agora, e observação opcional), **Ausente** (observação opcional) e **Cancelada** (motivo **obrigatório**, mínimo 5 caracteres, contador de caracteres e máximo 500).
- **RQ-T83-05** O botão Confirmar fica desabilitado enquanto faltar o dado exigido ou durante o envio (sem envio duplo); Cancelar fecha sem chamar a API.
- **RQ-T83-06** Mensagens por código (em `mensagensAcompanhamento.ts`): `TRANSICAO_VISITA_INVALIDA`, `DATA_REALIZACAO_FUTURA`, `MOTIVO_OBRIGATORIO`, `VISITA_AINDA_NAO_OCORREU`, `VISITA_ALTERADA_CONCORRENTEMENTE` (sugerir recarregar), `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`; código desconhecido mostra a mensagem do servidor; erro por campo aparece junto do campo.
- **RQ-T83-07** Ao registrar com sucesso: fechar o diálogo, atualizar a visita e a lista de origem (agenda ou ficha) e mostrar a confirmação.

### Visitas com desfecho

- **RQ-T83-08** Em `VisitaAgendadaPage`, visita com desfecho fica **somente leitura**, mostrando o que foi registrado: situação, data e hora reais (realizada), observação, motivo (cancelada) e quem registrou e quando (`alteradoPor`/`alteradoEm`); sem botão de escrita.
- **RQ-T83-09** Na agenda, o selo aparece em cada linha e as ações respeitam a situação.

### Testes e acabamento

- **RQ-T83-10** Testes de componente (seção 6) e `npm run lint`, `tsc -b`, `npm test` sem erro.
- **RQ-T83-11** Não reescrever a agenda: toda a mudança entra por `AcoesDaVisita`, `SeloSituacaoVisita` e `DialogoDesfecho`.

## 5. Contrato, regras e códigos de erro

- Consome as operações 6 e 7 da seção 2 das convenções.

## 6. Testes

- `DialogoDesfecho.test.tsx`: cada opção envia o corpo certo; motivo obrigatório (botão desabilitado com 4 caracteres e habilitado com 5); data futura bloqueada; erro por código; sem envio duplo.
- `SeloSituacaoVisita.test.tsx`: texto por situação.
- `AcoesDaVisita`/`VisitaAgendadaPage`: botão por perfil e situação, somente leitura com desfecho.

## 7. Critérios de aceite

- [ ] O coordenador registra realizada, ausente e cancelada (com motivo) pela interface.
- [ ] Visitas com desfecho aparecem somente leitura, com o que foi registrado.
- [ ] O selo mostra a situação em texto e cor na agenda, na ficha e na tela da visita.
- [ ] Erros das regras aparecem com mensagem clara.

## 8. Cuidados e fora de escopo

- Registro de resultados e aba de avaliações são do PBI-21 (T91).
- Não há opção de desfazer um desfecho.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
