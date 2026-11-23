# T73 · Modelo de domínio e migration da visita agendada

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Ter 10/11  
> **Depende de:** Sprint 2 (tabelas de participação e de visita prevista)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a tabela, a entidade e o repositório da **visita agendada** (a visita real de um participante, instância de uma visita prevista). Esta task é a base de cinco PBIs (resultados, histórico, retirada e as duas telas do app): deve ser a primeira da Sprint a entrar na `main`, em um Pull Request pequeno, e o grupo deve ser avisado quando entrar.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Participacao` (`participacoes`) guarda a `dataInclusao`, o "dia 0" do cronograma. `VisitaPlanejada` já calcula `dataPrevista`, `inicioDaJanela`, `fimDaJanela` e `estaNaJanela(dataInclusao, data)`: **não reescreva essa conta**, a T75 a reutiliza.
- As migrations da Sprint 2 vão até a `V5__create_study_status_history.sql`; a Sprint 3 usa o prefixo `V6_` (ajuste 1 das convenções).
- Estilo das entidades: Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`), `@Id @GeneratedValue(strategy = GenerationType.UUID)`, associações `LAZY`, `@JsonIgnore` nas associações, nomes de constraints como os da `V4`.
- `FlywayMigrationIntegrationTest` e `MigracaoSeguraTest` já verificam as migrations; o banco de teste é H2 em modo PostgreSQL com `ddl-auto=validate`, então a entidade e a migration precisam coincidir.
- Os valores da situação, as colunas e as constraints estão fixados na seção 5 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V6_19_1__create_scheduled_visits.sql` | Criar | Tabela `visitas_agendadas`, constraints e índices |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/SituacaoVisita.java` | Criar | Enum `AGENDADA`, `REALIZADA`, `AUSENTE`, `CANCELADA` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/VisitaAgendada.java` | Criar | Entidade JPA com `@Version` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Criar | Consultas básicas (as da agenda entram na T76) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/FlywayMigrationIntegrationTest.java` | Alterar | Verifica tabela, UNIQUE e CHECKs |
| `docs/diagramas/dominio.md` | Alterar | Acrescenta `VisitaAgendada` ao diagrama de classes e à descrição |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T73-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Migration `V6_19_1__create_scheduled_visits.sql`

- **RQ-T73-01** Criar a tabela `visitas_agendadas` com `id UUID` (PK `pk_visitas_agendadas`), `participacao_id UUID NOT NULL` (FK para `participacoes`), `visita_planejada_id UUID NOT NULL` (FK para `visitas_planejadas`), `data_hora_agendada TIMESTAMP NOT NULL`, `situacao VARCHAR(20) NOT NULL DEFAULT 'AGENDADA'`, `data_hora_realizada TIMESTAMP`, `motivo VARCHAR(500)`, `observacao VARCHAR(500)`, `versao BIGINT NOT NULL DEFAULT 0`, `criado_por_id UUID NOT NULL`, `criado_em TIMESTAMP NOT NULL`, `alterado_por_id UUID NOT NULL` e `alterado_em TIMESTAMP NOT NULL` (as duas colunas `*_por_id` são FKs para `usuarios`).
- **RQ-T73-02** `UNIQUE (participacao_id, visita_planejada_id)` com o nome `uk_visitas_agendadas_participacao_visita`: um agendamento por visita prevista e por participante (regra 2).
- **RQ-T73-03** `CHECK` `chk_visitas_agendadas_situacao`: `situacao IN ('AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA')`.
- **RQ-T73-04** `CHECK` `chk_visitas_agendadas_campos_por_situacao`: `REALIZADA` exige `data_hora_realizada` preenchida e `CANCELADA` exige `motivo` preenchido (a regra de negócio continua no serviço; o banco é a última barreira).
- **RQ-T73-05** Criar os índices `idx_visitas_agendadas_participacao` em `(participacao_id)` e `idx_visitas_agendadas_data_situacao` em `(data_hora_agendada, situacao)` (a agenda do estudo filtra por período e situação).
- **RQ-T73-06** Usar `CREATE TABLE IF NOT EXISTS` e `CREATE INDEX IF NOT EXISTS`; só tipos que funcionam igual no H2 (modo PostgreSQL) e no PostgreSQL 16 (`UUID`, `TIMESTAMP`, `VARCHAR`, `BIGINT`); nenhuma `ON DELETE CASCADE`.
- **RQ-T73-07** A migration não pode alterar nem apagar nada das tabelas da Sprint 2.

### Enum e entidade

- **RQ-T73-08** Criar `SituacaoVisita` com os quatro valores e Javadoc explicando que `REALIZADA`, `AUSENTE` e `CANCELADA` são finais (regra 3).
- **RQ-T73-09** Criar `VisitaAgendada` em `domain.model` mapeando a tabela: `participacao` (`@ManyToOne LAZY`, obrigatório), `visitaPlanejada` (`@ManyToOne LAZY`, obrigatório), `dataHoraAgendada` (`LocalDateTime`), `situacao` (`@Enumerated(EnumType.STRING)`, padrão `AGENDADA`), `dataHoraRealizada` (`LocalDateTime`), `motivo`, `observacao`, `versao` (`@Version Long`), `criadoPor`, `criadoEm`, `alteradoPor`, `alteradoEm`.
- **RQ-T73-10** A entidade não carrega regra de negócio além de conveniências simples: `estaAgendada()` (`situacao == AGENDADA`) e `dataAgendada()` (`dataHoraAgendada.toLocalDate()`). As regras ficam em serviço e em `MaquinaEstadosVisita` (T80).
- **RQ-T73-11** Associações com `@JsonIgnore` e `FetchType.LAZY`, como `Participacao`; os DTOs de resposta (T76) é que montam o JSON.

### Repositório

- **RQ-T73-12** `VisitaAgendadaRepository extends JpaRepository<VisitaAgendada, UUID>` com `existsByParticipacaoIdAndVisitaPlanejadaId`, `findByParticipacaoIdAndVisitaPlanejadaId`, `findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc` e `countByParticipacaoId`.
- **RQ-T73-13** Nenhuma consulta da agenda neste momento: a T76 as cria sobre este repositório.

### Verificação e integração

- **RQ-T73-14** Subir o backend em **banco vazio** e em **banco populado pela Sprint 2** (com o perfil `demo`); as duas subidas aplicam a migration sem erro e a validação do Hibernate (`ddl-auto=validate`) passa.
- **RQ-T73-15** Atualizar `FlywayMigrationIntegrationTest` para conferir que a tabela existe, que o `UNIQUE` recusa a segunda visita agendada para o mesmo par participação e visita prevista, que o `CHECK` recusa situação inválida e que o `CHECK` recusa `REALIZADA` sem data real e `CANCELADA` sem motivo.
- **RQ-T73-16** Abrir um **Pull Request pequeno** só com esta task e avisar o grupo quando entrar na `main`: T80, T87, T95, T101 e T116 dependem dele.
- **RQ-T73-17** Atualizar `docs/diagramas/dominio.md` (diagrama de classes e descrição) com a `VisitaAgendada` e suas relações.

## 5. Contrato, regras e códigos de erro

- Sem endpoints nesta task. A tabela, as colunas, as constraints e os nomes são os da seção 5 das convenções: qualquer mudança entra lá no mesmo Pull Request.
- Regra 2 (uma visita agendada por visita prevista e por participante) e a consequência da regra 3 (cancelada não pode ser agendada de novo) ficam garantidas pelo `UNIQUE`.

## 6. Testes

- `FlywayMigrationIntegrationTest` (tabela, `UNIQUE`, os dois `CHECK`).
- Teste de persistência (`VisitaAgendadaRepositoryTest`, `@SpringBootTest` + `@Transactional`): salvar, ler pelos métodos do repositório e provar que o segundo `save` do mesmo par dispara `DataIntegrityViolationException`; provar que `@Version` incrementa ao alterar.
- Rodar a suíte completa no H2 e no job `Backend (PostgreSQL)`.

## 7. Critérios de aceite

- [ ] A migration roda em banco vazio e em banco populado, no H2 e no PostgreSQL.
- [ ] A entidade passa na validação do Hibernate (`ddl-auto=validate`).
- [ ] O `UNIQUE` e os dois `CHECK` estão provados por teste.
- [ ] O diagrama de domínio mostra a `VisitaAgendada`.
- [ ] O Pull Request é pequeno, foi revisado por I3 e entrou na `main`; o grupo foi avisado.

## 8. Cuidados e fora de escopo

- Não criar endpoints, serviço nem DTO aqui (T74 a T76).
- `TIMESTAMP` sem fuso é proposital: o fuso é aplicado só na resposta da API (ajuste 6).
- Não colocar coluna de retirada aqui: a situação da participação nasce na T101.

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
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T73 · Modelo de domínio e migration da visita agendada.** Esta task é a base de cinco PBIs, então deve ser feita primeiro e integrada rápido. Primeiro, defina a entidade ScheduledVisit com id, participação, visita prevista, data e hora agendadas, situação (AGENDADA, REALIZADA, AUSENTE ou CANCELADA), motivo ou observação, usuário e data de criação e de última alteração. Depois escreva a migration V3_19_1__scheduled_visit.sql com as chaves estrangeiras, o UNIQUE de (participação, visita prevista) e o CHECK da situação, e crie a entidade JPA e o repositório. Por fim, suba o backend em banco vazio e em banco populado pela Sprint 2, abra um Pull Request pequeno e avise o grupo quando ele entrar na main, porque T80, T87, T95, T101 e T116 dependem dele.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
