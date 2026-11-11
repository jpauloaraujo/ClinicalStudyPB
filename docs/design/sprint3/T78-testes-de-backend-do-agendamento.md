# T78 · Testes de backend do agendamento

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Qua 11/11  
> **Depende de:** T75, T76  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar as regras do RF12 e os endpoints do agendamento com testes escritos junto com o código, mantendo a cobertura de `domain` e `service` acima de 80% (o CI bloqueia abaixo).

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `ParticipacaoServiceTest` (Mockito), `ParticipacaoEndpointTest` e `ConsentimentoEndpointTest` (MockMvc com tokens reais), `ParticipacaoConcorrenciaTest` (concorrência sem `@Transactional`).
- `support/CenarioRecrutamento` monta estudo, critérios, visitas, candidatos, avaliação, consentimento e inclusão pela API; `TokenDeTeste.bearer` devolve o token de cada perfil.
- Os testes precisam de um participante incluído **com** visitas previstas cuja janela contenha uma data futura: a visita prevista de apoio deve ter `diaPrevisto` e `tolerância` tais que `hoje + dia ± tolerância` inclua um dia de hoje em diante.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `participanteIncluido`, `visitaPrevista`, `agendar` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendamentoServiceTest.java` | Criar | Matriz unitária das regras (Mockito, `Clock` fixo) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/JanelaDeAgendamentoTest.java` | Criar | Limites da janela |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendamentoRequestValidacaoTest.java` | Criar | Validação dos DTOs |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendamentoEndpointTest.java` | Criar | 201, 400, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendaEndpointTest.java` | Criar | Filtros, paginação e permissões da agenda |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendamentoConcorrenciaTest.java` | Criar | Dois agendamentos simultâneos do mesmo par |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T78-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Matriz unitária (`AgendamentoServiceTest`)

- **RQ-T78-01** Estudo em planejamento e estudo encerrado recusam o agendamento (`ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`); recrutamento e execução aceitam.
- **RQ-T78-02** Data de ontem: `DATA_NO_PASSADO`; data de hoje: aceita (se estiver na janela); data um dia antes e um dia depois da janela: `DATA_FORA_DA_JANELA`; primeiro e último dia da janela: aceitos; a mensagem do erro contém a janela.
- **RQ-T78-03** Visita prevista de outro estudo: 404; participação inexistente: 404.
- **RQ-T78-04** Agendar duas vezes o mesmo par: `VISITA_JA_AGENDADA`; agendar visita cancelada de novo: `VISITA_JA_AGENDADA`.
- **RQ-T78-05** Reagendar visita `REALIZADA`, `AUSENTE` ou `CANCELADA`: `VISITA_NAO_ESTA_AGENDADA`; reagendar fora da janela e para o passado: erros certos; reagendar para a mesma data: aceito, atualiza `alteradoEm`.
- **RQ-T78-06** A ordem das verificações: com estudo encerrado **e** data passada, vence o erro do estudo.
- **RQ-T78-07** Autor e data gravados com o usuário e o `Clock` fixo; sugestão de datas calcula `inclusão + dia previsto` e a janela para todas as visitas previstas.

### Validação e `ApiExceptionHandler`

- **RQ-T78-08** `dataHora` e `visitaPlanejadaId` obrigatórios; `dataHora` malformada responde `CORPO_INVALIDO`.
- **RQ-T78-09** `ValidacaoRegraException` vira 400 com o código informado, no formato único.

### Endpoints com PostgreSQL real

- **RQ-T78-10** `POST` 201 com `Location` e o corpo completo; 400 (campo ausente, JSON malformado, data no passado); 403 para pesquisador, participante e administrador na escrita e para administrador e participante na leitura; 401 sem token; 404 (participação, visita prevista e agendamento inexistentes); 409 de cada regra.
- **RQ-T78-11** `PUT` 200 com a nova data; 409 para visita que já tem desfecho (inserir o estado pelo repositório no teste); 403 e 404.
- **RQ-T78-12** `GET .../visitas` devolve todas as visitas previstas com janela e sugestão e o agendamento só nas agendadas.
- **RQ-T78-13** Agenda: ordenação por data, filtros `de`, `ate` e `situacao`, paginação (`totalElementos`, `totalPaginas`), `de` maior que `ate` e `situacao` inválida (400 `PARAMETRO_INVALIDO`), permissões por perfil e o progresso por participante (`visitasPrevistas`, `visitasAgendadas`).
- **RQ-T78-14** Rodar tudo também no job `Backend (PostgreSQL)` do CI (os testes não podem depender de comportamento exclusivo do H2).

### Concorrência e cobertura

- **RQ-T78-15** `AgendamentoConcorrenciaTest` (sem `@Transactional`, com limpeza no `@AfterEach`): 4 requisições simultâneas para o mesmo par resultam em exatamente um 201 e três 409 `VISITA_JA_AGENDADA`, e uma só linha no banco.
- **RQ-T78-16** Conferir a cobertura de `AgendamentoService` e `AgendaService` (linhas e ramos) e registrar o resultado na descrição do Pull Request; o objetivo da fatia é 95% ou mais.
- **RQ-T78-17** Estender `CenarioRecrutamento` sem alterar as assinaturas existentes (a Sprint 2 as usa).

## 6. Testes

- As classes da seção 3; o `ContratoOpenApiSprint3Test` da T76 continua verde.
- Toda a suíte da Sprint 2 continua passando (434 testes).

## 7. Critérios de aceite

- [ ] Cada regra de agendamento e reagendamento tem teste de sucesso e de erro.
- [ ] Os endpoints respondem 201, 200, 400, 403, 404 e 409 com tokens reais.
- [ ] A concorrência está provada e os dados limpos no `@AfterEach`.
- [ ] A cobertura de `domain` e `service` da fatia está acima de 80% (meta 95%).

## 8. Cuidados e fora de escopo

- Os testes não dependem da data do dia: usar `Clock` fixo na unidade e datas relativas a hoje nos endpoints.
- Não alterar assinaturas de `CenarioRecrutamento` já usadas pela Sprint 2.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF12** (Módulo D — Acompanhamento; interface Web): “Agendar visitas para os participantes incluídos.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Ao fechar, em 11/11, este PBI deixa na main a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T78 · Testes de backend do agendamento.** Escreva os testes junto com o código, não só no fim. Primeiro faça a matriz unitária das regras: estudo fora de recrutamento ou execução, data passada, fora da janela, duplicidade e reagendamento de visita não agendada. Depois teste os endpoints com PostgreSQL real (201, 400, 403, 404 e 409). Por fim, confira a cobertura dos serviços desta fatia, porque o CI bloqueia abaixo de 80%.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
