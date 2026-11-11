# T85 · Testes de backend do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Sex 13/11  
> **Depende de:** T81, T82  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar a máquina de estados, as regras do desfecho, os endpoints e a concorrência de dois desfechos simultâneos, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `MaquinaEstadosEstudoTest` (27 casos de matriz), `EstudoTransicaoRf07Test`, `ConsentimentoEndpointTest` (tokens reais) e `ParticipacaoConcorrenciaTest`.
- `CenarioRecrutamento` com os métodos da T78 (`participanteIncluido`, `visitaPrevista`, `agendar`); `TokenDeTeste`; `Clock` fixo na unidade.
- Para registrar realizada ou ausente pela API, a data agendada não pode ser futura: o teste precisa de um participante cuja janela contenha o dia de hoje (visita prevista com `diaPrevisto` 0 e tolerância suficiente, inclusão hoje).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MaquinaEstadosVisitaTest.java` | Alterar | Matriz completa (a T80 criou o essencial) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DesfechoVisitaServiceTest.java` | Criar | Regras isoladas (Mockito, `Clock` fixo) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DesfechoRequestValidacaoTest.java` | Criar | Validação do DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DesfechoEndpointTest.java` | Criar | 200, 400, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DesfechoConcorrenciaTest.java` | Criar | Dois desfechos simultâneos na mesma visita |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `registrarDesfecho(mvc, token, visitaAgendadaId, corpo)` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T85-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Matriz de transições e regras

- **RQ-T85-01** As **16 combinações** origem e destino da máquina: as 3 válidas (`AGENDADA` para os três desfechos) e as 13 inválidas, incluindo todas a partir de `REALIZADA`, `AUSENTE` e `CANCELADA` (estados finais) e "para o mesmo estado".
- **RQ-T85-02** Dados: data real futura (400 `DATA_REALIZACAO_FUTURA`), data real de agora e do passado (aceitas), motivo ausente, em branco e com 4 caracteres (`MOTIVO_OBRIGATORIO`) e com 5 (aceito), observação com 500 e 501 caracteres.
- **RQ-T85-03** Estudo: em recrutamento e execução aceitam; em planejamento e **encerrado** recusam (`ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`).
- **RQ-T85-04** Data agendada futura: realizada e ausente recusam (`VISITA_AINDA_NAO_OCORREU`), **cancelada aceita**; data agendada de hoje aceita as três.
- **RQ-T85-05** Gravação: realizada guarda `dataHoraRealizada` (informada ou o `Clock`), ausente e cancelada não; autor e data de alteração; campos que não pertencem ao tipo são ignorados.
- **RQ-T85-06** Ordem das verificações: com estudo encerrado **e** transição inválida, vence o erro do estudo; com transição inválida e data futura, vence a transição.

### Endpoints com tokens reais (e PostgreSQL)

- **RQ-T85-07** `POST .../desfecho`: 200 para cada tipo, com o corpo atualizado; 400 (tipo ausente, tipo inexistente, JSON malformado, data futura, motivo ausente); 403 para pesquisador, participante e administrador; 401 sem token; 404 para visita inexistente; 409 de cada regra (transição inválida, estudo fora do estado, visita ainda não ocorreu).
- **RQ-T85-08** `GET /api/visitas-agendadas/{id}`: 200 para coordenador e pesquisador, 403 para administrador e participante, 404 e 401.
- **RQ-T85-09** Depois de registrar, o `GET` mostra a situação e os campos do desfecho e o `PUT` de reagendamento responde 409 `VISITA_NAO_ESTA_AGENDADA`.

### Concorrência e cobertura

- **RQ-T85-10** `DesfechoConcorrenciaTest` (sem `@Transactional`, com limpeza no `@AfterEach`): 2 e depois 4 requisições simultâneas de desfecho para a **mesma** visita resultam em exatamente **um 200** e os demais 409 (`VISITA_ALTERADA_CONCORRENTEMENTE` ou `TRANSICAO_VISITA_INVALIDA`), com um único desfecho gravado e nenhum 500.
- **RQ-T85-11** Provar que o teste detecta a falha: remover temporariamente o controle de versão e verificar que ele falha (mutação manual descrita na descrição do Pull Request).
- **RQ-T85-12** Conferir a cobertura de `MaquinaEstadosVisita` e `DesfechoVisitaService` (linhas e ramos); registrar o resultado no Pull Request.
- **RQ-T85-13** Toda a suíte anterior continua passando, também no job `Backend (PostgreSQL)`.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (7 operações).

## 7. Critérios de aceite

- [ ] As três transições válidas e todas as inválidas têm teste.
- [ ] Data futura, motivo ausente e estudo encerrado estão cobertos.
- [ ] Os endpoints respondem 200, 400, 403, 404 e 409 com tokens reais.
- [ ] Dois desfechos simultâneos resultam em um sucesso e um 409, provado por teste.

## 8. Cuidados e fora de escopo

- Não depender da data do dia: `Clock` fixo na unidade e datas relativas a hoje nos endpoints.
- O teste de concorrência é o único que não reverte por `@Transactional`: sempre limpar os dados.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF13** (Módulo D — Acompanhamento; interface Web): “Registrar a realização, ausência ou cancelamento de visitas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T85 · Testes de backend do desfecho.** Escreva os testes junto com o código. Primeiro faça a matriz de transições: as três válidas e todas as inválidas a partir de estados finais. Depois teste data futura, motivo ausente e estudo encerrado. Por fim, teste os endpoints (200, 400, 403, 404 e 409) e a concorrência de dois desfechos simultâneos para a mesma visita, que deve resultar em um sucesso e um 409.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
