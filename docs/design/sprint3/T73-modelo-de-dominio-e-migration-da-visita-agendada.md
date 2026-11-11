# T73 · Modelo de domínio e migration da visita agendada

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Ter 10/11  
> **Depende de:** Sprint 2 (tabelas de participação e de visita prevista)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
