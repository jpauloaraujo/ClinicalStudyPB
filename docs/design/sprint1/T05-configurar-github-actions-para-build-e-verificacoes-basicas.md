# T05 · Configurar GitHub Actions para build e verificações básicas

> **Sprint 1** · PBI-01 · Configuração do repositório e arquitetura  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T02, T06, T11, T12  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Rodar automaticamente, a cada push e Pull Request na `main` e na `develop`, o build e as verificações do backend, da web e do mobile, para que nenhum código quebrado seja integrado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Arquivo único: `.github/workflows/ci.yml`. Os jobs foram ampliados na Sprint 2 (T64 e T65).
- Resumo da cobertura: `.github/scripts/resumo-cobertura.py` (T64).
- Descrição dos jobs para o leitor: `README.md`, seção 6 "Integração contínua".

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `.github/workflows/ci.yml` | Existente | Define os jobs `backend`, `backend-postgres`, `web`, `e2e` e `mobile` |
| `.github/scripts/resumo-cobertura.py` | Existente | Lê `jacoco.csv` e escreve a tabela de cobertura no resumo da execução |

## 4. O que fazer em cada arquivo

### Gatilhos

- `push` e `pull_request` nas branches `main` e `develop`.
- `concurrency` por branch com `cancel-in-progress`, para não acumular execuções antigas.

### Job `backend` (Java 21 + Maven)

- `actions/setup-java` com Temurin 21 e cache do Maven; roda `./mvnw -B verify` (testes em H2, segredo JWT fictício em `src/test/resources`).
- Publica os artefatos `cobertura-jacoco` e `relatorio-testes-backend`.

### Job `backend-postgres`

- Serviço `postgres:16` e as variáveis `SPRING_DATASOURCE_*` apontando para ele; roda `./mvnw -B test` para validar as migrations no banco de produção.

### Job `web` (Node 22)

- `npm ci`, `npm run lint`, `npm test` (Vitest) e `npm run build`.

### Job `e2e`

- Sobe PostgreSQL, backend e web e roda o Playwright (ver T65).

### Job `mobile`

- `npm ci` e as verificações do projeto Expo em `mobile/ClinicalStudyPB`.

## 6. Testes

- Abrir um PR com um teste quebrado de propósito e conferir que o job correspondente fica vermelho.

## 7. Critérios de aceite

- [ ] Os cinco jobs aparecem em todo PR para `main` ou `develop`.
- [ ] Falha em qualquer job impede o merge (com a proteção da T02).

## 8. Cuidados e fora de escopo

- Não colocar segredo real no workflow; o JWT de teste é fictício.
- Ao adicionar um job novo, atualizar a lista de checks obrigatórios na proteção de branch (T02).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
