# ClinicalStudyPB

Sistema de Gestão de Estudos Clínicos desenvolvido na disciplina de **Engenharia de Software II (2026.2)**.

O ClinicalStudyPB gerencia de forma simplificada o ciclo de vida operacional de estudos clínicos: cadastro de protocolos, recrutamento, elegibilidade, inclusão de participantes, agendamento de visitas, acompanhamento e relatórios. O sistema **não** realiza diagnósticos nem decisões clínicas automatizadas e utiliza apenas **dados fictícios**.

> 🚧 **Projeto em desenvolvimento — Release 1 / Sprint 1 (13/10 a 26/10/2026).** Várias seções abaixo ainda serão preenchidas conforme as tarefas da sprint forem concluídas.

## Sumário

1. [Apresentação e escopo](#1-apresentação-e-escopo)
2. [Arquitetura](#2-arquitetura)
3. [Funcionalidades desenvolvidas](#3-funcionalidades-desenvolvidas)
4. [Stack tecnológico](#4-stack-tecnológico)
5. [Instalação e execução](#5-instalação-e-execução)
6. [Testes e cobertura](#6-testes-e-cobertura)
7. [Documentação da API](#7-documentação-da-api)
8. [Releases](#8-releases)
9. [Estrutura do repositório](#9-estrutura-do-repositório)
10. [Documentos de referência](#10-documentos-de-referência)

## 1. Apresentação e escopo

### Perfis de usuários

| Perfil | Interface | Responsabilidades |
|---|---|---|
| **Administrador** | Web | Gerencia usuários, perfis de acesso, configurações e registros de auditoria. |
| **Pesquisador** | Web | Cadastra protocolos, define critérios de elegibilidade, acompanha participantes, consulta dados e gera relatórios. |
| **Coordenador do Estudo** | Web | Gerencia recrutamento, verifica elegibilidade, organiza visitas, acompanha pendências e monitora o andamento do estudo. |
| **Participante** | Mobile | Consulta visitas agendadas, acompanha sua participação, registra informações solicitadas e pode solicitar a retirada do estudo. |

### Módulos funcionais

| Módulo | Requisitos |
|---|---|
| A — Usuários e autenticação | RF01 – RF03 |
| B — Gestão de estudos | RF04 – RF07 |
| C — Recrutamento | RF08 – RF11 |
| D — Acompanhamento | RF12 – RF16 |
| E — Aplicativo do participante | RF17 – RF20 |
| F — Monitoramento | RF21 – RF22 |

A descrição completa de cada requisito está em [docs/especificacao-projeto.md](docs/especificacao-projeto.md).

### Equipe

| Integrante | Papel / foco na Sprint 1 |
|---|---|
| _I1 — a definir_ | Scrum Master, arquitetura e DevOps |
| _I2 — a definir_ | Product Owner, testes e documentação |
| _I3 — a definir_ | Backend: domínio, persistência e estudos |
| _I4 — a definir_ | Backend: usuários, segurança e autorização |
| _I5 — a definir_ | Frontend React: autenticação e integração |
| _I6 — a definir_ | Frontend React: estudos e protótipos web |
| _I7 — a definir_ | Mobile React Native, protótipos e testes |

## 2. Arquitetura

O sistema é um **monorepo** com três aplicações independentes que se comunicam por uma única **API HTTP REST**:

| Componente | Pasta | Responsabilidade |
|---|---|---|
| **Backend (API REST)** | `backend/ClinicalStudyPB` | Regras de negócio, persistência, autenticação e autorização por perfil. Expõe a API documentada em OpenAPI/Swagger UI. |
| **Aplicação web** | `web/ClinicalStudyPB` | SPA React usada pelos perfis **administrador**, **pesquisador** e **coordenador**. Consome a API via HTTP. |
| **Aplicativo mobile** | `mobile/ClinicalStudyPB` | App React Native + Expo usado pelo perfil **participante**. Consome a mesma API. |
| **Banco de dados** | — | PostgreSQL, acessado apenas pelo backend via Spring Data JPA e versionado via Flyway. |

Web e mobile **não** acessam o banco diretamente: toda leitura e escrita passa pela API, que aplica validações e controle de acesso.

### Organização do backend

Aplicação Spring Boot com pacote raiz `UEPB.ESWII.ClinicalStudyPB`, organizada em camadas:

| Pacote | Conteúdo | Situação |
|---|---|---|
| `config` | Configuração do OpenAPI 3 / Swagger UI (`OpenApiConfig`) e liberação de rotas de documentação | ✅ concluído (T10) |
| `domain.model` / `domain.enums` | Entidades JPA (`Usuario`, `Estudo`) e enumerações (`PerfilUsuario`, `StatusEstudo`) | ✅ concluído (T08) |
| `controller` / `service` / `repository` | Endpoints REST, regras de negócio e interfaces Spring Data JPA | ✅ concluído (T06) |
| `db/migration` | Migrations SQL versionadas do Flyway (`V1__create_initial_tables.sql`, `V2__seed_initial_data.sql`) | ✅ concluído (T09) |
| DTOs e validações | Objetos de entrada/saída da API com Bean Validation | 🔄 iniciado (`EstudoRequest`, `Usuario*Request`, `Login*Request`) / T15 / T27 |
| Segurança | Configuração do Spring Security (`SecurityConfig`), autenticação stateless por JWT (`TokenService`), endpoint de login (`AuthController`) e senhas com BCrypt | ✅ concluído (Tasks 3060 / 3063) |


Dependências principais: Spring Web MVC, Spring Data JPA, Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`), Spring Security, Bean Validation, springdoc-openapi, driver PostgreSQL, H2 (escopo de testes) e Lombok.

### Organização da web

Projeto criado com o template React + TypeScript do Vite, com React Compiler habilitado e ESLint. Navegação (React Router), cliente HTTP (Axios) e variáveis de ambiente serão adicionados em T11, T13 e T14.

Diagramas de arquitetura (Mermaid) — T01 / T03 (ver [docs/arquitetura.md](docs/arquitetura.md)):

- [x] Arquitetura (comunicação entre web, mobile, backend e banco de dados)
- [x] Casos de uso e Matriz RBAC
- [x] Classes / modelo de domínio e DER
- [x] Sequência dos principais fluxos e máquinas de estados

## 3. Funcionalidades desenvolvidas

| RF | Descrição | Interface | Status |
|---|---|---|---|
| RF01 | Cadastro e gerenciamento de usuários | Web | 🔄 Sprint 1 |
| RF02 | Autenticação e encerramento de sessão | Web/Mobile | 🔄 Sprint 1 (web) |
| RF03 | Restrição de funcionalidades por perfil | Web/Mobile | 🔄 Sprint 1 (web) |
| RF04 | Cadastro de estudos | Web | 🔄 Sprint 1 |
| RF05 – RF22 | Demais requisitos | Web/Mobile | ⏳ Sprints futuras |

> _Descrição do comportamento e capturas de tela (casos de sucesso e de erro/validação) serão adicionadas à medida que cada requisito for concluído._

## 4. Stack tecnológico

A equipe adotou a **Stack A**:

| Camada | Tecnologia | Versão |
|---|---|---|
| Backend | Java | 21 |
| Backend | Spring Boot (Web MVC, Data JPA, Flyway, Security, Validation) | 4.1.1 |
| Backend | Maven (via Maven Wrapper `mvnw`) | 3.10.0 |
| Backend | Lombok | gerenciada pelo Spring Boot |
| Documentação da API | springdoc-openapi (Swagger UI) | 3.1.1 |
| Banco de dados | PostgreSQL (driver JDBC) | _versão do servidor a definir_ |
| Migrations | Flyway (`flyway-core` + `flyway-database-postgresql`) | 12.4.0 |
| Web | React | 19.3 |
| Web | TypeScript | 6.0 |
| Web | Vite (com React Compiler) | 8.3 |
| Web | ESLint | 10 |
| Web | React Router / Axios | _planejado (T11 / T14)_ |
| Mobile | React Native + Expo (TypeScript) | _a definir (T12)_ |
| Testes | JUnit 5 + Spring Boot Test + MockMvc + H2 (backend); demais _a definir_ | — |
| CI | GitHub Actions | _planejado (T05)_ |
| Gestão ágil | Azure DevOps (Azure Boards) | — |
| Prototipação | Figma | — |

> Versões extraídas de `backend/ClinicalStudyPB/pom.xml` e `web/ClinicalStudyPB/package.json`. Itens "planejado" ainda não estão no repositório.

## 5. Instalação e execução

### Pré-requisitos

- [Git](https://git-scm.com/)
- JDK 21 — backend (o Maven é baixado automaticamente pelo `mvnw`)
- [Node.js](https://nodejs.org/) 20.19+ ou 22.12+ e npm — web (exigência do Vite 8) e mobile
- [PostgreSQL](https://www.postgresql.org/) — banco de dados
- [Expo Go](https://expo.dev/go) em um dispositivo físico **ou** um emulador Android/iOS — mobile

### Clonar o repositório

```bash
git clone https://github.com/jpauloaraujo/ClinicalStudyPB.git
cd ClinicalStudyPB
```

### Banco de dados e Migrations (Flyway — T09)

1. Crie um banco de dados local no PostgreSQL chamado `clinicalstudypb`:
   ```sql
   CREATE DATABASE clinicalstudypb;
   ```
2. A aplicação utiliza valores padrão locais (`jdbc:postgresql://localhost:5432/clinicalstudypb`, usuário `postgres`, senha `postgres`), que podem ser sobrescritos por variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) ou copiando `src/main/resources/application-dev.properties.example` para `src/main/resources/application-dev.properties` (ignorado pelo Git).
3. Ao iniciar o backend, o **Flyway** executa automaticamente as migrations em `src/main/resources/db/migration/`:
   - `V1__create_initial_tables.sql`: cria as tabelas `usuarios` e `estudos`, chaves estrangeiras, constraints de domínio e índices.
   - `V2__seed_initial_data.sql`: popula os 4 usuários fictícios de demonstração (um por perfil) e 2 estudos clínicos fictícios.

### Backend

```bash
cd backend/ClinicalStudyPB
./mvnw spring-boot:run
```

No Windows (PowerShell/CMD), use `mvnw.cmd spring-boot:run`. A API sobe por padrão na porta **`8081`** (`http://localhost:8081`), configurável via `SERVER_PORT`.

#### Variáveis de ambiente do backend

| Variável | Descrição | Padrão | Obrigatória? |
|---|---|---|---|
| `DB_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/clinicalstudypb` | Não (local) |
| `DB_USERNAME` | Usuário do banco | `postgres` | Não (local) |
| `DB_PASSWORD` | Senha do banco | `postgres` | Não (local) |
| `SERVER_PORT` | Porta HTTP da API | `8081` | Não |
| `JWT_SECRET` | Chave secreta HS256 (mínimo de 256 bits / 32 caracteres) | — | **Sim** (ou via `application-dev.properties`) |
| `JWT_ISSUER` | Identificador do emissor do token | `clinicalstudypb` | Não |
| `JWT_EXPIRATION_MINUTES` | Tempo de validade do token em minutos | `30` | Não |

Para desenvolvimento local, copie `src/main/resources/application-dev.properties.example` para `src/main/resources/application-dev.properties` (ignorado pelo Git) para definir `jwt.secret` sem depender de variáveis do sistema operacional.

#### Autenticação, chamada de login e uso do token (Tasks 3060 / 3063)

1. **Obtenção do token (Login):**
   Envie uma requisição HTTP `POST` para `/api/auth/login` com as credenciais do usuário:
   ```json
   POST /api/auth/login
   Content-Type: application/json

   {
     "email": "admin@clinicalstudy.test",
     "senha": "senha-de-teste"
   }
   ```
   Em caso de sucesso, a API responde com status `200 OK`:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tipo": "Bearer"
   }
   ```
   Credenciais inválidas (e-mail inexistente, senha incorreta ou usuário inativo) retornam `401 Unauthorized` com a mensagem genérica `{"erro": "Credenciais inválidas"}`.

2. **Consumo de endpoints protegidos:**
   Envie o token JWT recebido no cabeçalho `Authorization` com o esquema `Bearer`:
   ```http
   GET /api/usuarios HTTP/1.1
   Host: localhost:8081
   Authorization: Bearer <token_recebido>
   ```

3. **Prazo de expiração e logout:**
   - O token emitido tem validade de **30 minutos** (configurável via `JWT_EXPIRATION_MINUTES`).
   - Nesta implementação mínima stateless, o logout é executado no lado do cliente (web/mobile) ao descartar o token armazenado.
   - O token permanece criptograficamente válido no servidor até o término de seu período de validade (`exp`). Usuários desativados têm novas tentativas de login imediatamente rejeitadas no endpoint `/api/auth/login`.

### Aplicação web

```bash
cd web/ClinicalStudyPB
npm install
npm run dev
```

A aplicação fica disponível em `http://localhost:5173`. Outros scripts:

| Comando | Ação |
|---|---|
| `npm run build` | Verifica os tipos (`tsc -b`) e gera o build de produção em `dist/` |
| `npm run preview` | Serve localmente o build de produção |
| `npm run lint` | Executa o ESLint |

> _A URL da API (variável de ambiente) será documentada em T14._

### Aplicativo mobile

> _A ser documentado (PBI-03: T12 — projeto React Native + Expo)._

### Credenciais de demonstração (Seed Flyway `V2`)

Os seguintes usuários fictícios são criados automaticamente pela migration `V2__seed_initial_data.sql`:

| Perfil | E-mail | Senha | UUID |
|---|---|---|---|
| **ADMINISTRADOR** | `admin@clinicalstudy.test` | `senha-de-teste` | `11111111-1111-4111-8111-111111111111` |
| **PESQUISADOR** | `pesquisador@clinicalstudy.test` | `senha-de-teste` | `22222222-2222-4222-8222-222222222222` |
| **COORDENADOR** | `coordenador@clinicalstudy.test` | `senha-de-teste` | `33333333-3333-4333-8333-333333333333` |
| **PARTICIPANTE** | `participante@clinicalstudy.test` | `senha-de-teste` | `44444444-4444-4444-8444-444444444444` |

## 6. Testes e cobertura

Meta mínima: **80% de cobertura de linhas do código de negócio do backend**, além de testes de componentes web e mobile e ao menos um fluxo E2E em cada interface. Os testes devem ser executados automaticamente a cada pull request.

### Backend

Testes com JUnit 5, Spring Boot Test, MockMvc e banco H2 em memória no modo de compatibilidade PostgreSQL (executando as mesmas migrations do Flyway e validando o schema JPA):

```bash
cd backend/ClinicalStudyPB
./mvnw test
```

No Windows (PowerShell): `mvnw.cmd test`.

Suítes atuais:
- `ClinicalStudyPbApplicationTests`: validação do contexto Spring Boot, Flyway e Hibernate (`ddl-auto=validate`).
- `FlywayMigrationIntegrationTest`: verificação da execução das migrations `V1` e `V2`, integridade dos usuários de seed (4 perfis + hash BCrypt) e estudos clínicos iniciais.
- `OpenApiDocumentationIntegrationTest`: verificação da especificação OpenAPI 3 (`/v3/api-docs`), esquema de segurança Bearer JWT e acesso ao Swagger UI (`/swagger-ui/index.html`).
- `UsuarioDtoValidationTest`: testes unitários de validação Bean Validation dos DTOs de cadastro e atualização de usuários.
- `UsuarioIntegrationTest`: testes de integração do CRUD de usuários com autenticação simulada (`@WithMockUser`).
- `AuthenticationIntegrationTest`: 13 testes de integração cobrindo login com credenciais válidas, senha incorreta, e-mail inexistente, usuário inativo, campos obrigatórios ausentes, acesso sem token, token malformado, adulterado, expirado, com emissor inválido, consumo com token válido, acesso público ao Swagger UI e compatibilidade do BCrypt (`PasswordEncoder`).

Comando para rodar exclusivamente os testes de autenticação e segurança:
```bash
./mvnw test -Dtest=AuthenticationIntegrationTest
```


## 7. Documentação da API

A API é documentada no padrão **OpenAPI 3** com **springdoc-openapi** (`OpenApiConfig`), incluindo metadados do projeto, organização por tags (`Usuários` e `Estudos Clínicos`), exemplos de requisição/resposta e suporte a autenticação **Bearer JWT** (botão **Authorize** no topo do Swagger UI).

Com o backend em execução (porta padrão `8081`):

- **Swagger UI (atalho):** `http://localhost:8081/swagger-ui.html`
- **Swagger UI (página principal):** `http://localhost:8081/swagger-ui/index.html`
- **Especificação OpenAPI 3 (JSON):** `http://localhost:8081/v3/api-docs`

## 8. Releases

| Release | Sprints | Documento |
|---|---|---|
| Release 1 | Sprint 1 – _a definir_ | `releases/release1/README.md` _(a criar)_ |
| Release 2 | _a definir_ | `releases/release2/README.md` _(a criar)_ |
| Release 3 | _a definir_ | `releases/release3/README.md` _(a criar)_ |

Cada entrega é identificada por uma tag (`release1`, `release2`, `release3`) e uma GitHub Release associada.

## 9. Estrutura do repositório

```text
ClinicalStudyPB/
├── backend/
│   └── ClinicalStudyPB/          # API REST — Java 21 + Spring Boot (Maven)
│       ├── pom.xml
│       ├── mvnw / mvnw.cmd
│       └── src/
│           ├── main/java/UEPB/ESWII/ClinicalStudyPB/
│           │   ├── ClinicalStudyPbApplication.java
│           │   ├── config/        # OpenApiConfig (Swagger UI + Bearer JWT)
│           │   ├── controller/    # Controllers REST, DTOs e ApiExceptionHandler
│           │   ├── domain/        # Entidades JPA (model) e Enums (enums)
│           │   ├── repository/    # Interfaces Spring Data JPA
│           │   └── service/       # Regras de negócio e exceções
│           ├── main/resources/
│           │   ├── application.properties
│           │   ├── application-dev.properties.example
│           │   └── db/migration/  # Scripts SQL versionados do Flyway (V1, V2)
│           └── test/              # Testes de integração e recursos de teste (H2)
├── web/
│   └── ClinicalStudyPB/          # Aplicação web — React + TypeScript + Vite
│       ├── package.json
│       └── src/
├── mobile/
│   └── ClinicalStudyPB/          # Aplicativo do participante — React Native + Expo
├── docs/                         # Especificação, arquitetura, planejamento e guias
├── releases/                     # (a criar) Documentação de cada release
├── .github/workflows/            # (a criar) Workflows do GitHub Actions
└── README.md
```

## 10. Documentos de referência

- [Especificação do projeto](docs/especificacao-projeto.md) — escopo, perfis, requisitos funcionais, estratégia de testes e regras de entrega.
- [Documento de Arquitetura de Software (DAS)](docs/arquitetura.md) — diagramas C4, casos de uso, matriz RBAC, classes UML, DER, máquinas de estados e sequência.
- [Planejamento da Sprint 1](docs/sprint1-plano.md) — PBIs, tasks, responsáveis, capacidade, Definition of Done e riscos.
- [Implementação do Backend — Sprint 1](docs/implementacao-backend-sprint1.md) — detalhamento das camadas, endpoints, migrations Flyway e documentação OpenAPI.
