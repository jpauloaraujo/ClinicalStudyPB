# T85 · Testes de backend do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Sex 13/11  
> **Depende de:** T81, T82  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
