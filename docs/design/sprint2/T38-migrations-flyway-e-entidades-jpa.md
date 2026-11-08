# T38 · Migrations Flyway e entidades JPA

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Qui 29/10  
> **Depende de:** T36  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Criar as tabelas, as entidades e os repositórios do recrutamento, provando que a migração é segura em banco vazio e em banco já populado pela Sprint 1.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento anterior, que cobre T38, T41 e T51 juntas: `docs/sprint2/T38-T41-T51-implementacao.md`.
- Modelo e diagrama: `docs/diagramas/dominio.md`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V4__create_recruitment_tables.sql` | Existente | criterios, visitas_planejadas, acoes, candidatos, avaliacoes_elegibilidade, respostas_avaliacao, consentimentos, participacoes |
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V5__create_study_status_history.sql` | Existente | historico_status_estudo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/` | Existente | Criterio (abstrata), CriterioInclusao, CriterioExclusao, VisitaPlanejada, Acao, Candidato, AvaliacaoElegibilidade, RespostaAvaliacao, Consentimento, Participacao, HistoricoStatusEstudo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/` | Existente | Um repositório por agregado, com só as consultas necessárias |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MigracaoSeguraTest.java` | Existente | Migra um banco só com a Sprint 1 e confere que nada se perde |

## 4. O que fazer em cada arquivo

### Restrições importantes

- `uk_visitas_estudo_dia` (dia único por estudo), `chk_visitas_tolerancia` (0 a 30), `chk_visitas_dia_previsto` (≥ 0).
- `uk_candidatos_estudo_codigo` e `uk_candidatos_estudo_nome_nascimento`; `chk_candidatos_situacao` (6 situações).
- `uk_consentimentos_candidato` e `uk_participacoes_candidato` (um por candidato).
- `chk_avaliacoes_resultado` (APROVADO/REPROVADO); `uk_respostas_avaliacao_criterio`; `chk_criterios_tipo` e `chk_criterios_campos_por_tipo`.
- V5: histórico com FK do estudo `ON DELETE CASCADE` e do usuário `RESTRICT`.

### Nomes

- As tabelas seguem o português do código (`historico_status_estudo`, não `study_status_history`). Os critérios usam herança de tabela única (coluna `tipo`).

### Prova de segurança da migração

- Subir o Flyway em banco vazio e em banco com V1 a V3 populado; o Hibernate valida o esquema sem erro (`ddl-auto=validate`).

## 6. Testes

- `FlywayMigrationIntegrationTest`, `MigracaoSeguraTest` (H2) e o job `backend-postgres` do CI (PostgreSQL real, prova final).

## 7. Critérios de aceite

- [ ] O Flyway aplica V4 e V5 em banco vazio e populado.
- [ ] O Hibernate valida o esquema sem erro.

## 8. Cuidados e fora de escopo

- A prova em PostgreSQL real depende do job `backend-postgres` ficar verde no GitHub.
- Tabela nova = migration `V6` ou maior; nunca editar a V4 ou a V5.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
