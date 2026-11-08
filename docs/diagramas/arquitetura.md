# Arquitetura atualizada (T69)

Visão dos componentes ao fim da Sprint 2, com o que cada parte faz e como são verificadas. O detalhamento por camadas e o modelo de dados anteriores estão em [docs/sprint1/arquitetura.md](../sprint1/arquitetura.md).

```mermaid
flowchart TB
    subgraph Clientes
        WEB["Aplicação web<br/>React 19 + TypeScript + Vite<br/>administrador, pesquisador, coordenador"]
        MOB["Aplicativo mobile<br/>React Native + Expo<br/>participante (só login na Sprint 2)"]
    end

    subgraph Contrato["Contrato da API"]
        YAML[("docs/api/openapi-sprint2.yaml")]
        TIPOS["src/api/schema.d.ts<br/>tipos gerados"]
        MSW["Mock Service Worker<br/>VITE_USE_MOCKS=true"]
    end

    subgraph Backend["Backend Spring Boot 4 (Java 21)"]
        SEC["Segurança<br/>JWT stateless + RBAC @PreAuthorize"]
        CTRL["Controllers REST<br/>/api/..."]
        SVC["Services<br/>regras de negócio e transações"]
        DOM["Domínio<br/>entidades + RegraElegibilidade + MaquinaEstadosEstudo"]
        REPO["Repositórios Spring Data JPA"]
        SEC --> CTRL --> SVC --> DOM
        SVC --> REPO
    end

    DB[("PostgreSQL<br/>migrations Flyway V1 a V5")]

    WEB -->|"HTTPS + Bearer JWT"| SEC
    MOB -->|"HTTPS + Bearer JWT"| SEC
    REPO --> DB

    YAML -->|"npm run api:tipos"| TIPOS
    TIPOS --> WEB
    MSW -.->|"substitui a API em desenvolvimento e testes"| WEB
    YAML -.->|"ContratoOpenApiTest compara com /v3/api-docs"| CTRL

    subgraph CI["GitHub Actions"]
        J1["Backend: testes + JaCoCo (resumo e artefato)"]
        J2["Backend no PostgreSQL real"]
        J3["Web: lint, Vitest, build"]
        J4["E2E: Playwright com PostgreSQL + backend + web"]
        J5["Mobile: lint e tipos"]
    end
    J4 -.-> WEB
    J4 -.-> Backend
    J2 -.-> DB
```

| Parte | Observação |
|---|---|
| Autenticação | `POST /api/auth/login` devolve um JWT (HS256, 30 minutos); `GET /api/auth/me` devolve o usuário do token |
| Autorização | Matriz por perfil em `security/Permissoes`; sem token 401, perfil sem permissão 403 |
| Erros | `{"erro", "codigo?"}`; 409 para regra de negócio violada, sempre com `codigo` |
| Concorrência | A inclusão de participantes e a geração do código do candidato bloqueiam o estudo (`PESSIMISTIC_WRITE`) |
| Cobertura | JaCoCo mede domínio e serviços; mínimo de 80% de linhas, com bloqueio ligado no CI (`COBERTURA_BLOQUEAR: 'true'`) |
