# T09 · Criar migrations iniciais com Flyway

> **Sprint 1** · PBI-02 · Estrutura inicial do Spring Boot e PostgreSQL  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T07  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Versionar o esquema do banco e a carga inicial, para que todos subam o mesmo banco.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V1__create_initial_tables.sql` | Existente | Tabelas `usuarios` e `estudos` com PK, FK, UNIQUE, CHECK e índices |
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V2__seed_initial_data.sql` | Existente | 4 usuários fictícios (um por perfil, senha `senha-de-teste` em BCrypt) e 2 estudos de exemplo |
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V3__normalizar_emails.sql` | Existente | Converte e-mails para minúsculas |
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V4__create_recruitment_tables.sql, V5__create_study_status_history.sql` | Existente | Sprint 2 (T38) |

## 4. O que fazer em cada arquivo

### Constraints da V1

- `uk_usuarios_email`; `chk_usuarios_perfil` (4 perfis); `chk_estudos_status` (4 estados); `chk_estudos_limite_participantes` (≥ 1); `chk_estudos_datas` (fim ≥ início); FK `fk_estudos_responsavel` com `ON DELETE RESTRICT`.

### Regras para migrations novas

- Nome `V<n>__descricao.sql` com o próximo número livre (hoje, a próxima é a `V6`); nunca editar uma migration já aplicada.
- Usar `IF NOT EXISTS` onde possível e SQL compatível com PostgreSQL e com o H2 dos testes.
- Nomear constraints (`pk_`, `fk_`, `uk_`, `chk_`) e índices (`idx_`).
- Dados de exemplo só em migrations de seed ou no perfil `demo` (T63).

## 6. Testes

- `FlywayMigrationIntegrationTest` (as migrations aplicam) e `MigracaoSeguraTest` da Sprint 2 (migrar um banco já populado).

## 7. Critérios de aceite

- [ ] O Flyway aplica tudo em um banco vazio.
- [ ] O seed cria os 4 usuários e 2 estudos.

## 8. Cuidados e fora de escopo

- Após a V3, o e-mail é sempre gravado em minúsculas pela aplicação.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
