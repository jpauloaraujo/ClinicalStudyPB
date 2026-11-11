# T102 · Regras da retirada (serviço de domínio)

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Qua 18/11  
> **Depende de:** T75, T80, T81, T89, T101  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Implementar o serviço que registra a retirada em **uma única transação**: valida as pré-condições, marca o participante `RETIRADO`, cancela as visitas futuras agendadas pela máquina de estados e mantém a vaga ocupada. Acrescentar o bloqueio de participante retirado nos serviços de agendamento, desfecho e resultados, que foram entregues antes de a coluna de situação existir.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `MaquinaEstadosVisita` (T80), `AgendamentoService` (T75), `DesfechoVisitaService` (T81) e `ResultadoAtividadeService` (T89) deixaram o ponto `exigirParticipacaoAtiva` comentado, para esta task preencher com `PARTICIPANTE_RETIRADO`.
- `ParticipacaoService.incluir` conta a vaga por `countByEstudoId` (inclui os retirados): a vaga **continua ocupada** (regra 6, ponto a validar com o PO); essa contagem **não pode mudar**.
- `EstudoAcesso`, `UsuarioAutenticado`, o bean `Clock` e os repositórios das visitas. A retirada pedida pelo participante no app (RF20, Sprint 4) reaproveitará este serviço: ele fica independente do endpoint web.
- `ValidacaoRegraException` (400 com código) e `ConflitoRegraException` (409).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/RetiradaService.java` | Criar | `registrar(participacaoId, comando, usuarioId)` e `buscar(participacaoId)` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendamentoService.java` | Alterar | `exigirParticipacaoAtiva` com `PARTICIPANTE_RETIRADO` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/DesfechoVisitaService.java` | Alterar | `exigirParticipacaoAtiva` com `PARTICIPANTE_RETIRADO` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ResultadoAtividadeService.java` | Alterar | `exigirParticipacaoAtiva` e o `bloqueio` da consulta |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Alterar | `buscarComBloqueio(id)` (`PESSIMISTIC_WRITE`) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Visitas `AGENDADA` futuras de uma participação |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T102-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Pré-condições (`registrar`)

- **RQ-T102-01** Carregar a participação **com bloqueio de escrita** (`buscarComBloqueio`) para serializar retiradas e agendamentos concorrentes; participação inexistente: 404.
- **RQ-T102-02** Verificar nesta ordem: (1) participante **ativo**, senão 409 `PARTICIPANTE_JA_RETIRADO` (a retirada é **irreversível**: a segunda dá 409); (2) estudo **não encerrado**, senão 409 `ESTUDO_ENCERRADO`; (3) `motivo` obrigatório com **ao menos 5 caracteres** depois de `trim`, senão 400 `MOTIVO_OBRIGATORIO`; (4) `dataRetirada` **não futura** (`Clock`), senão 400 `DATA_RETIRADA_FUTURA`.
- **RQ-T102-03** `solicitante` é obrigatório (`PARTICIPANTE`, `PESQUISADOR` ou `COORDENACAO`), validado no DTO (T103).

### Efeito da retirada (tudo na mesma transação)

- **RQ-T102-04** Gravar a `RetiradaParticipacao` (motivo, solicitante, data da retirada, quem registrou e quando) e mudar a participação para `RETIRADO`.
- **RQ-T102-05** **Cancelar as visitas futuras** ainda `AGENDADA` da participação, **usando `MaquinaEstadosVisita`** (transição `AGENDADA → CANCELADA`) com o motivo automático **"retirada do estudo"**, atualizando `alteradoPor` e `alteradoEm`. Visitas **futuras** são as de `dataHoraAgendada` posterior a agora; **não tocar** nas visitas que já têm desfecho (`REALIZADA`, `AUSENTE`, `CANCELADA`).
- **RQ-T102-06** Devolver o total de visitas canceladas (`visitasCanceladas` do `RetiradaResponse`).
- **RQ-T102-07** A operação é **atômica**: se qualquer passo falhar, nada fica gravado (nem a retirada pela metade, nem visitas canceladas sem o participante retirado).
- **RQ-T102-08** **Manter a vaga ocupada** (regra 6): não alterar a contagem de participantes do RF11; `VagasResponse` e `ParticipacaoService.incluir` continuam contando os retirados.
- **RQ-T102-09** Sem operação de desfazer: não existe método que volte a participação para `ATIVO`.

### Bloqueio de participante retirado nos outros serviços

- **RQ-T102-10** Implementar `exigirParticipacaoAtiva` em `AgendamentoService` (agendar e reagendar), `DesfechoVisitaService` (registrar desfecho) e `ResultadoAtividadeService` (registrar e editar resultado), lançando 409 `PARTICIPANTE_RETIRADO` com mensagem clara (ex.: "O participante foi retirado do estudo; não é possível alterar o acompanhamento"). A verificação entra na ordem definida: **depois** do estado do estudo e **antes** das regras do dado.
- **RQ-T102-11** Na consulta de resultados, `bloqueio` passa a devolver `PARTICIPANTE_RETIRADO` (prioridade: `VISITA_NAO_REALIZADA`, depois `ESTUDO_ENCERRADO`, depois `PARTICIPANTE_RETIRADO`).
- **RQ-T102-12** **Cada um dos três serviços ganha um teste** para o novo bloqueio (seção 6).

### Decisões a registrar

- **RQ-T102-13** Visitas `AGENDADA` **vencidas** (data passada ainda sem desfecho) **não** são canceladas pela retirada e, como o desfecho é bloqueado para quem foi retirado, o coordenador deve registrá-las **antes** de retirar; a tela (T104) avisa quando existirem. Registrar essa decisão na ata do Planning (T94) e no Javadoc do serviço.
- **RQ-T102-14** O serviço recebe um comando simples (`motivo`, `solicitante`, `dataRetirada`) e o id do usuário; não conhece `HttpServletRequest` nem `Jwt`.

## 5. Contrato, regras e códigos de erro

- Erros: `PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO` e `PARTICIPANTE_RETIRADO` (409); `MOTIVO_OBRIGATORIO` e `DATA_RETIRADA_FUTURA` (400); 404 para participação inexistente.
- Regra 6: motivo, quem solicitou e data (não futura); irreversível; visitas futuras `AGENDADA` viram `CANCELADA` com o motivo "retirada do estudo"; a vaga continua ocupada.

## 6. Testes

- A bateria completa é a T106; nesta task escreva junto com o código os testes unitários das pré-condições, do cancelamento em cascata e dos três bloqueios novos.

## 7. Critérios de aceite

- [ ] A retirada grava o motivo, o solicitante e a data, muda a participação para `RETIRADO` e cancela as visitas futuras `AGENDADA` na mesma transação.
- [ ] Uma segunda retirada, uma retirada com estudo encerrado, sem motivo ou com data futura falham com o código certo.
- [ ] Agendar, registrar desfecho e registrar resultado de participante retirado falham com `PARTICIPANTE_RETIRADO`.
- [ ] A contagem de vagas não muda.

## 8. Cuidados e fora de escopo

- Os bloqueios nos PBIs 19, 20 e 21 são editados aqui, por design: avisar os donos e manter os testes deles passando.
- A retirada não apaga histórico: visitas realizadas, ausentes e resultados ficam preservados.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
