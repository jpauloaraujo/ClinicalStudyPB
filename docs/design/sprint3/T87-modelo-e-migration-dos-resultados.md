# T87 · Modelo e migration dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Sáb 14/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Criar a tabela, a entidade e o repositório do **resultado de uma atividade de avaliação** de uma visita realizada: um resultado por atividade e por visita agendada, com autor e data. Os resultados são fictícios; nenhum dado real de pessoa é armazenado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Acao` (`acoes`) tem `categoria` (`CategoriaAcao`: `AVALIACAO_CLINICA`, `EXAME_LABORATORIAL`, `EXAME_IMAGEM`, `QUESTIONARIO`, `MEDICACAO`, `OUTROS`). A regra 4 vale para a categoria `AVALIACAO_CLINICA` (ajuste 4 das convenções).
- `VisitaAgendada` e a migration `V6_19_1` vêm da T73; esta task usa a faixa `V6_21_*`.
- Estilo das entidades e das constraints: o mesmo da T73 e da `V4`. A migration não pode alterar nada da Sprint 2.
- Colunas, constraints e nomes: seção 5 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V6_21_1__create_evaluation_results.sql` | Criar | Tabela `resultados_atividade` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/ResultadoAtividade.java` | Criar | Entidade JPA |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ResultadoAtividadeRepository.java` | Criar | Consultas por visita agendada e por atividade |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/FlywayMigrationIntegrationTest.java` | Alterar | Tabela, `UNIQUE` e chaves estrangeiras |
| `docs/diagramas/dominio.md` | Alterar | Acrescenta `ResultadoAtividade` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T87-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Migration `V6_21_1__create_evaluation_results.sql`

- **RQ-T87-01** Tabela `resultados_atividade`: `id UUID` (PK), `visita_agendada_id UUID NOT NULL` (FK para `visitas_agendadas`), `acao_id UUID NOT NULL` (FK para `acoes` com `ON DELETE RESTRICT`), `valor VARCHAR(200) NOT NULL`, `unidade VARCHAR(30)`, `observacao VARCHAR(500)`, `registrado_por_id UUID NOT NULL`, `registrado_em TIMESTAMP NOT NULL`, `alterado_por_id UUID NOT NULL`, `alterado_em TIMESTAMP NOT NULL` (os dois `*_por_id` são FKs para `usuarios`).
- **RQ-T87-02** `UNIQUE (visita_agendada_id, acao_id)` (`uk_resultados_atividade_visita_acao`): **um resultado por atividade** e por visita agendada.
- **RQ-T87-03** Índice em `visita_agendada_id`; `CREATE TABLE IF NOT EXISTS`; tipos compatíveis com H2 (modo PostgreSQL) e PostgreSQL 16; nenhuma `ON DELETE CASCADE`.

### Entidade e repositório

- **RQ-T87-04** `ResultadoAtividade` com `visitaAgendada` e `acao` (`@ManyToOne LAZY`, obrigatórios), `valor`, `unidade`, `observacao`, `registradoPor`, `registradoEm`, `alteradoPor`, `alteradoEm`; Lombok e `@JsonIgnore` nas associações, como `VisitaAgendada`.
- **RQ-T87-05** `ResultadoAtividadeRepository` com `findByVisitaAgendadaId`, `findByVisitaAgendadaIdAndAcaoId` e `countByVisitaAgendadaId`.

### Verificação

- **RQ-T87-06** Subir o backend em **banco vazio** e em **banco populado** (perfil `demo`), no H2 e no PostgreSQL; `ddl-auto=validate` passa.
- **RQ-T87-07** Atualizar `FlywayMigrationIntegrationTest`: a tabela existe, o `UNIQUE` recusa o segundo resultado da mesma atividade na mesma visita e as FKs recusam referências inexistentes.
- **RQ-T87-08** Atualizar `docs/diagramas/dominio.md` com `ResultadoAtividade` e a relação com `VisitaAgendada` e `Acao`.
- **RQ-T87-09** Documentar no código (Javadoc) e na descrição do Pull Request que os resultados são **fictícios** (LGPD): nenhum dado real é armazenado.

## 5. Contrato, regras e códigos de erro

- Sem endpoints. A regra 4 (só visita `REALIZADA`, só `AVALIACAO_CLINICA`) é do serviço (T89); aqui o banco só garante a unicidade e as referências.

## 6. Testes

- `FlywayMigrationIntegrationTest` (tabela, `UNIQUE`, FKs).
- `ResultadoAtividadeRepositoryTest`: salvar, buscar por visita e por atividade e violar o `UNIQUE`.

## 7. Critérios de aceite

- [ ] A migration roda em banco vazio e em banco populado, no H2 e no PostgreSQL.
- [ ] A entidade passa na validação do Hibernate e o `UNIQUE` está provado por teste.
- [ ] O diagrama de domínio inclui o resultado.

## 8. Cuidados e fora de escopo

- Não criar serviço, DTO nem endpoint aqui (T88 a T90).
- O valor é texto curto livre; não modelar tipos numéricos nem faixas de referência.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
