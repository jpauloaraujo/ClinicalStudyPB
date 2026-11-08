# T01 · Definir e documentar arquitetura Spring Boot + React + React Native + PostgreSQL

> **Sprint 1** · PBI-01 · Configuração do repositório e arquitetura  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** nenhuma  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Fixar a stack e a organização em camadas, para que sete pessoas escrevam código compatível entre si. O documento completo é o DAS; este guia resume as decisões que quem implementa precisa respeitar.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento de Arquitetura de Software: `docs/sprint1/arquitetura.md` (visão de containers, camadas, RBAC, domínio, estados, sequências, pacotes e ADRs).
- Versões em uso: Java 21, Spring Boot 4.1, React 19 + TypeScript + Vite, React Router 7, Expo (React Native), PostgreSQL com Flyway.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint1/arquitetura.md` | Existente | DAS: fonte das decisões |
| `docs/diagramas/arquitetura.md` | Existente | Diagrama de arquitetura atualizado na Sprint 2 (T69) |
| `README.md` | Existente | Seção 2 resume a arquitetura e a organização de cada módulo |

## 4. O que fazer em cada arquivo

### Princípios que valem para todo código novo

- Regra de negócio fica no backend (`service` e `domain/regra`); web e mobile só apresentam e validam por conveniência.
- API REST documentada em OpenAPI (springdoc); na Sprint 2 o contrato é escrito antes do código.
- Autenticação stateless por JWT; autorização declarativa com `@PreAuthorize`, repetida como guarda de rota na web.
- Banco versionado com Flyway; o Hibernate só valida o esquema (`ddl-auto=validate`).

### Camadas do backend (como está no código)

- `security/`: filtros, JWT e `Permissoes`. `controller/`: REST e `ApiExceptionHandler`; `controller/dto/`: records de entrada e saída.
- `service/`: regras de negócio e as exceções de regra (`RegraNegocioException`, `ConflitoRegraException`, `RecursoNaoEncontradoException`).
- `domain/model`, `domain/enums`, `domain/regra` (regras puras); `repository/`: Spring Data. Migrations em `resources/db/migration`.
- Diferença em relação ao DAS: o DAS prevê `dto/`, `exception/` e `domain/repository` como pacotes próprios; no código os DTOs estão em `controller/dto`, as exceções em `service` e os repositórios em `repository`. Siga o código.

### Organização de web e mobile

- Web: `src/{api, components, contexts, hooks, mocks, pages, routes, services, types, utils}`.
- Mobile: Expo Router em `src/app`, mais `src/{api, components, contexts}`.

## 7. Critérios de aceite

- [ ] Arquitetura documentada e revisada pela equipe.
- [ ] Qualquer integrante sabe em qual pacote criar cada tipo de arquivo.

## 8. Cuidados e fora de escopo

- Ao mudar uma decisão (stack, camadas, autenticação), registrar um novo ADR no DAS no mesmo PR.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
