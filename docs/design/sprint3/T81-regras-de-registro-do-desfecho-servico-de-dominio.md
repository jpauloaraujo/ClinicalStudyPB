# T81 · Regras de registro do desfecho (serviço de domínio)

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 4h · **Prazo:** Qui 12/11  
> **Depende de:** T80  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar o serviço que registra o desfecho de uma visita aplicando a máquina da T80, as pré-condições do estudo e a regra da data, gravando quem registrou e quando, de forma **atômica**: duas requisições simultâneas nunca geram dois desfechos para a mesma visita.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `MaquinaEstadosVisita` (T80), `VisitaAgendada` com `@Version` e `VisitaAgendadaRepository` (T73), `AgendamentoService` (T75) como modelo de serviço, `EstudoAcesso`, `UsuarioAutenticado` e o bean `Clock`.
- O bloqueio de participante retirado entra na T102 (a coluna só existe a partir da T101).
- Padrão de concorrência da Sprint 2: `ParticipacaoService` bloqueia o estudo com `buscarComBloqueio`; aqui o recurso disputado é a **visita**, protegida por versão otimista e, se preciso, por bloqueio de linha.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/DesfechoVisitaService.java` | Criar | `registrar(visitaAgendadaId, comando, usuarioId)` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | `buscarComBloqueio(id)` (`PESSIMISTIC_WRITE`) se a versão otimista não bastar |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` | Alterar | Mapeia `ObjectOptimisticLockingFailureException` para 409 `VISITA_ALTERADA_CONCORRENTEMENTE` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T81-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Ordem das verificações em `registrar`

- **RQ-T81-01** (1) a visita agendada existe, senão 404; (2) o estudo da participação está em recrutamento **ou** execução, senão 409 `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` (regra 1; o encerrado bloqueia qualquer desfecho); (3) `MaquinaEstadosVisita.validarTransicao`: a visita precisa estar `AGENDADA`, senão 409 `TRANSICAO_VISITA_INVALIDA`; (4) `MaquinaEstadosVisita.validarDados` (400: data real futura e motivo ausente); (5) **realizada e ausente** exigem que a data agendada não seja futura (a data, não a hora: o dia de hoje é permitido), senão 409 `VISITA_AINDA_NAO_OCORREU`; **cancelar é permitido a qualquer momento** antes do desfecho.
- **RQ-T81-02** Ponto único e comentado `exigirParticipacaoAtiva` (preenchido na T102 com `PARTICIPANTE_RETIRADO`), como no `AgendamentoService`.

### Gravação

- **RQ-T81-03** Aplicar a situação do tipo de desfecho; para **realizada** gravar `dataHoraRealizada` (a informada ou `agora` do `Clock`); para **ausente** e **cancelada** deixar `dataHoraRealizada` nula; gravar `motivo` (cancelada) e `observacao` (ausente, cancelada ou realizada, quando informada, com `trim`; em branco vira nulo).
- **RQ-T81-04** Atualizar `alteradoPor` (usuário do token) e `alteradoEm` (`Clock`); a `versao` incrementa pelo `@Version`. `criadoPor` e `criadoEm` não mudam.
- **RQ-T81-05** Devolver a visita atualizada para o controller montar o `VisitaAgendadaResponse`.

### Atomicidade e concorrência

- **RQ-T81-06** O registro roda em **uma transação** e usa `saveAndFlush`, de modo que a violação de versão aconteça dentro do serviço: converter `ObjectOptimisticLockingFailureException` em 409 `VISITA_ALTERADA_CONCORRENTEMENTE`.
- **RQ-T81-07** Tratar também no `ApiExceptionHandler` a mesma exceção quando ela vier no `commit` (fora do método do serviço), respondendo 409 `VISITA_ALTERADA_CONCORRENTEMENTE` no formato único de erro.
- **RQ-T81-08** Resultado esperado de **duas requisições simultâneas** para a mesma visita: exatamente uma com sucesso e a outra com 409 (`VISITA_ALTERADA_CONCORRENTEMENTE` ou, se ela carregou a visita depois do commit da primeira, `TRANSICAO_VISITA_INVALIDA`). Nunca dois desfechos, nunca 500.
- **RQ-T81-09** Se a versão otimista não for suficiente nos testes de concorrência, usar `buscarComBloqueio` (`@Lock(PESSIMISTIC_WRITE)`) no repositório e documentar a escolha no Javadoc do serviço.

### Independência da camada web

- **RQ-T81-10** O serviço recebe um comando simples (`tipo`, `dataHoraRealizada`, `motivo`, `observacao`) e o id do usuário; não conhece `HttpServletRequest` nem `Jwt`. A T102 chama a mesma máquina para cancelar as visitas da retirada sem passar por este serviço de desfecho manual.

## 5. Contrato, regras e códigos de erro

- Erros: `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `TRANSICAO_VISITA_INVALIDA`, `VISITA_AINDA_NAO_OCORREU` e `VISITA_ALTERADA_CONCORRENTEMENTE` (409); `DATA_REALIZACAO_FUTURA` e `MOTIVO_OBRIGATORIO` (400); 404 para visita inexistente.

## 6. Testes

- A matriz completa e a concorrência estão na T85; nesta task escreva junto com o código os testes unitários (Mockito, `Clock` fixo) de cada verificação e da gravação por tipo de desfecho.

## 7. Critérios de aceite

- [ ] Os três desfechos funcionam e gravam autor, data e os campos do tipo.
- [ ] Cada regra violada falha com o código e o HTTP certos, na ordem definida.
- [ ] Dois desfechos simultâneos resultam em um sucesso e um 409; o serviço é atômico.

## 8. Cuidados e fora de escopo

- A comparação de "data agendada no futuro" é por **dia** (hoje é permitido); não bloquear quem registra a visita no mesmo dia, antes do horário marcado.
- Não permitir "desfazer" desfecho: não existe operação de voltar a `AGENDADA`.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF13** (Módulo D — Acompanhamento; interface Web): “Registrar a realização, ausência ou cancelamento de visitas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 1 | Visitas só são agendadas para participante incluído e ativo (não retirado), com o estudo em recrutamento ou execução; estudo encerrado bloqueia qualquer agendamento | T75, T81, T89, T102 |
| 3 | A visita agendada nasce AGENDADA e só pode virar REALIZADA, AUSENTE ou CANCELADA; esses três estados são finais. Reagendar (trocar a data) só vale para AGENDADA. Realizada e ausente não aceitam data futura; cancelar exige motivo; realizada guarda data e hora reais | T75, T80, T81, T83 |
| 6 | A retirada é registrada pelo coordenador, com motivo, quem solicitou e data (não futura), e é irreversível. O participante passa a RETIRADO e as visitas futuras AGENDADA viram CANCELADA com o motivo "retirada do estudo". A vaga continua ocupada, ou seja, o limite de vagas conta todos os que foram incluídos (ponto a validar com o PO) | T101, T102 (inclui os bloqueios nos serviços de T75, T81 e T89) e T104 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T81 · Regras de registro do desfecho (serviço de domínio).** Primeiro escreva o serviço que carrega a visita e aplica a máquina da T80. Depois acrescente a pré-condição da regra 1: estudo em recrutamento ou execução, ou 409 com código próprio (o bloqueio de participante retirado entra na T102). Em seguida bloqueie realizada e ausente para visitas com data agendada no futuro, mas permita cancelar a qualquer momento antes do desfecho. Por fim, grave quem registrou e quando, atualize a situação e a data de alteração da visita e garanta que o registro é atômico: duas requisições simultâneas não podem gerar dois desfechos para a mesma visita (controle de versão otimista ou bloqueio da linha).

### 9.5 Requisitos da especificação que atravessam esta task

- Entregas: “Tratamento de erros e validações das regras de negócio.”
- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
