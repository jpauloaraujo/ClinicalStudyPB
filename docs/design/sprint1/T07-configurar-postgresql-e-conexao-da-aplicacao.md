# T07 · Configurar PostgreSQL e conexão da aplicação

> **Sprint 1** · PBI-02 · Estrutura inicial do Spring Boot e PostgreSQL  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T06  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Conectar o backend ao PostgreSQL por variáveis de ambiente, sem credenciais no repositório, e usar um banco em memória nos testes.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/application.properties` | Existente | Propriedades padrão, lidas de variáveis de ambiente |
| `backend/ClinicalStudyPB/src/main/resources/application-dev.properties.example` | Existente | Modelo para o `application-dev.properties` local (ignorado pelo Git) |
| `backend/ClinicalStudyPB/src/test/resources/application.properties` | Existente | Testes com H2 em memória e segredo JWT fictício |

## 4. O que fazer em cada arquivo

### Propriedades principais

- `spring.datasource.url` = `${DB_URL:jdbc:postgresql://localhost:5432/clinicalstudypb}`; usuário e senha em `DB_USERNAME` e `DB_PASSWORD` (padrão `postgres`).
- `server.port` = `${SERVER_PORT:8081}`; perfil ativo `${SPRING_PROFILES_ACTIVE:dev}`.
- `spring.jpa.hibernate.ddl-auto=validate` e `spring.jpa.open-in-view=false`; Flyway ligado em `classpath:db/migration`, com `baseline-on-migrate=true`.
- JWT: `jwt.secret` (`JWT_SECRET`), `jwt.issuer` e `jwt.expiration-minutes` (30).

### Banco local

- Criar o banco `clinicalstudypb` no PostgreSQL; as tabelas nascem das migrations na primeira subida.
- Ajustes pessoais vão no `application-dev.properties` (copiado do exemplo), nunca no arquivo versionado.

### Testes e CI

- Testes usam H2 em modo PostgreSQL; o job `backend-postgres` do CI roda a mesma suíte contra PostgreSQL real por variáveis `SPRING_DATASOURCE_*`.

## 7. Critérios de aceite

- [ ] O backend sobe contra o PostgreSQL local com o banco criado.
- [ ] Nenhuma senha real no Git.

## 8. Cuidados e fora de escopo

- Qualquer perfil novo (por exemplo, `demo` na T63) entra como `application-<perfil>.properties`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
