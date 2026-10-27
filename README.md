# ClinicalStudyPB

Sistema de Gestão de Estudos Clínicos desenvolvido na disciplina de **Engenharia de Software II (2026.2)**.

O ClinicalStudyPB gerencia de forma simplificada o ciclo de vida operacional de estudos clínicos: cadastro de protocolos, recrutamento, elegibilidade, inclusão de participantes, agendamento de visitas, acompanhamento e relatórios. O sistema **não** realiza diagnósticos nem decisões clínicas automatizadas e utiliza apenas **dados fictícios**.

> 🚧 **Projeto em desenvolvimento — Release 1 / Sprint 1 (13/10 a 26/10/2026).** A situação de cada task da sprint, com responsável e arquivos, está em [docs/status-sprint1.md](docs/status-sprint1.md).

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
| DTOs e validações | Objetos de entrada/saída da API com Bean Validation (`EstudoRequest`, `Usuario*Request`, `Login*Request`) | ✅ concluído (T15 / T27) |
| Segurança | Configuração do Spring Security (`SecurityConfig`), autenticação stateless por JWT (`TokenService`), endpoints `/api/auth/login` e `/api/auth/me` (`AuthController`) e senhas com BCrypt | ✅ concluído (T19 / T20 — Tasks 3060 / 3063) |
| Autorização por perfil | `@EnableMethodSecurity` + `@PreAuthorize` nos controllers, seguindo a matriz RBAC; acesso negado responde `403` com `{"erro": "Acesso negado para o seu perfil"}` | ✅ concluído (T24) |


Dependências principais: Spring Web MVC, Spring Data JPA, Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`), Spring Security (OAuth2 Resource Server para JWT), Bean Validation, springdoc-openapi, driver PostgreSQL, H2 (escopo de testes), Lombok e JaCoCo (cobertura).

#### Permissões por perfil na API (T24 / RF03)

| Endpoint | Administrador | Pesquisador | Coordenador | Participante |
|---|:---:|:---:|:---:|:---:|
| `POST /api/auth/login` (público) | ✅ | ✅ | ✅ | ✅ |
| `GET /api/auth/me` | ✅ | ✅ | ✅ | ✅ |
| `/api/usuarios/**` (todas as operações) | ✅ | ❌ | ❌ | ❌ |
| `GET /api/estudos`, `GET /api/estudos/{id}` | ❌ | ✅ | ✅ | ❌ |
| `POST`, `PUT`, `DELETE /api/estudos/**` | ❌ | ✅ | ❌ | ❌ |

Sem token válido a API responde `401`; com token de um perfil sem permissão, `403`.

### Organização da web

Projeto criado com o template React + TypeScript do Vite, com React Compiler habilitado e ESLint. O layout base e a navegação com React Router (T13) estão em `src/components/layout`, `src/routes` e `src/pages` — detalhes em [docs/implementacao-web-layout-navegacao.md](docs/implementacao-web-layout-navegacao.md).

| Pasta / arquivo | Conteúdo | Task |
|---|---|---|
| `src/services/api.ts` | Cliente Axios único, URL via `VITE_API_URL`, envio do token e tratamento de `401` | T14 |
| `src/services/recursos.ts` | Chamadas da API por recurso (autenticação, usuários, estudos) | T14 / T31 |
| `src/services/erros.ts` | Converte `{"erro": "..."}` da API em mensagem e campo do formulário | T31 |
| `src/contexts/` | Sessão real: login, restauração da sessão salva, expiração do token e logout | T21 |
| `src/routes/RotaProtegida.tsx` | Guards `RequerAutenticacao` e `RequerPerfil` (perfis lidos de `navegacao.ts`) | T25 |
| `src/pages/usuarios/` | Listagem, cadastro, edição e exclusão de usuários (administrador) | T17 |
| `src/pages/estudos/` | Listagem (pesquisador e coordenador) e cadastro/edição/exclusão (pesquisador) | T29 / T31 |

### Organização do mobile

App Expo (SDK 58) com **Expo Router**: cada arquivo em `src/app/` é uma tela. O login fica fora das abas e as abas (`Início`, `Estudos`, `Visitas`, `Perfil`) só aparecem com sessão de **participante** (`Stack.Protected`). O token fica no `expo-secure-store` (ou no `localStorage` quando o app roda no navegador). As telas de estudos e visitas mostram onde entrarão os RF17 a RF20 nas próximas sprints — detalhes em [docs/implementacao-mobile.md](docs/implementacao-mobile.md).

Diagramas de arquitetura (Mermaid) — T01 / T03 (ver [docs/arquitetura.md](docs/arquitetura.md)):

- [x] Arquitetura (comunicação entre web, mobile, backend e banco de dados)
- [x] Casos de uso e Matriz RBAC
- [x] Classes / modelo de domínio e DER
- [x] Sequência dos principais fluxos e máquinas de estados

## 3. Funcionalidades desenvolvidas

| RF | Descrição | Interface | Status |
|---|---|---|---|
| RF01 | Cadastro e gerenciamento de usuários | Web | ✅ Sprint 1 |
| RF02 | Autenticação e encerramento de sessão | Web/Mobile | ✅ Sprint 1 (web e mobile) |
| RF03 | Restrição de funcionalidades por perfil | Web/Mobile | ✅ Sprint 1 (API, web e mobile) |
| RF04 | Cadastro de estudos | Web | ✅ Sprint 1 |
| RF05 – RF22 | Demais requisitos | Web/Mobile | ⏳ Sprints futuras |

| RF | Comportamento | Casos de erro e validação |
|---|---|---|
| RF01 | O administrador lista, cadastra, edita (nome, e-mail, perfil, situação e senha opcional) e exclui usuários em `/usuarios` | Campos obrigatórios, e-mail inválido, senha com menos de 8 caracteres e e-mail já cadastrado ("Já existe um usuário com esse e-mail", vindo da API). Não é possível excluir a própria conta |
| RF02 | Login com e-mail e senha na web (`/login`) e no app; a sessão é restaurada ao reabrir; "Sair" descarta o token | "Credenciais inválidas" (senha errada, e-mail inexistente ou conta inativa); token expirado leva de volta ao login. A web recusa o participante e o app recusa os demais perfis |
| RF03 | Menu e rotas conforme o perfil; a API aplica as mesmas regras | Rota sem permissão mostra "Acesso negado"; a API responde `403` |
| RF04 | O pesquisador cadastra, edita e exclui estudos em `/estudos` (é o responsável pelos estudos que cria); o coordenador só consulta | Título, objetivo e limite (≥ 1) obrigatórios; "A data final não pode ser anterior à data inicial" aparece no campo da data |

> _Capturas de tela das interfaces ainda precisam ser adicionadas (em `docs/imagens/`) para a entrega da Release 1._

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
| Web | React Router | 7.18 |
| Web | Axios | 1.20 |
| Mobile | Expo SDK | 58 |
| Mobile | React Native | 0.88 |
| Mobile | Expo Router + expo-secure-store | 58 |
| Testes | JUnit 5 + Spring Boot Test + MockMvc + Mockito + H2 (backend); web e mobile _a definir_ | — |
| Cobertura | JaCoCo | 0.8.14 |
| CI | GitHub Actions (`.github/workflows/ci.yml`) | — |
| Gestão ágil | Azure DevOps (Azure Boards) | — |
| Prototipação | Figma | — |

> Versões extraídas de `backend/ClinicalStudyPB/pom.xml`, `web/ClinicalStudyPB/package.json` e `mobile/ClinicalStudyPB/package.json`.

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

A URL da API vem de `VITE_API_URL` (padrão `http://localhost:8081`). Para mudar, copie `.env.example` para `.env.local` (ignorado pelo Git). O backend libera CORS para `http://localhost:5173`.

> **Erro `Failed to resolve import "axios"`:** após um `git pull` que altere o `package.json`, rode `npm install` de novo em `web/ClinicalStudyPB` — o cliente HTTP (`src/services/api.ts` e `src/services/erros.ts`) depende do `axios`, adicionado depois da estrutura inicial. Se o erro continuar com o servidor já rodando, reinicie-o com `npm run dev -- --force` para o Vite descartar o cache de dependências.

### Aplicativo mobile

```bash
cd mobile/ClinicalStudyPB
npm install
npm start
```

O Metro sobe na porta **8082**, porque a 8081 é do backend. Leia o QR code com o **Expo Go** no celular ou pressione `a` (emulador Android) ou `w` (navegador). Entre com `participante@clinicalstudy.test` / `senha-de-teste`; os outros perfis são recusados no app.

A URL da API vem de `EXPO_PUBLIC_API_URL` (copie `.env.example` para `.env.local`):

| Onde o app roda | Valor de `EXPO_PUBLIC_API_URL` |
|---|---|
| Navegador (`npm run web`) ou simulador iOS | `http://localhost:8081` (padrão) |
| Emulador Android | `http://10.0.2.2:8081` |
| Celular físico (mesma rede Wi-Fi) | `http://<IP-da-máquina>:8081` |

Outros scripts: `npm run lint` (ESLint) e `npm run typecheck` (TypeScript).

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

Testes com JUnit 5, Spring Boot Test, MockMvc, Mockito e banco H2 em memória no modo de compatibilidade PostgreSQL (executando as mesmas migrations do Flyway e validando o schema JPA):

```bash
cd backend/ClinicalStudyPB
./mvnw test
```

No Windows (PowerShell): `mvnw.cmd test`. O relatório de cobertura do JaCoCo é gerado em `target/site/jacoco/index.html`.

**Resultado em 26/10/2026:** 62 testes, 0 falhas, 0 erros. Cobertura de linhas do código de negócio: **99%** (controllers 100%, services 100%, segurança 97%). Ficam fora da medição a classe `main`, `config` e os DTOs.

Suítes atuais:
- `ClinicalStudyPbApplicationTests`: validação do contexto Spring Boot, Flyway e Hibernate (`ddl-auto=validate`).
- `FlywayMigrationIntegrationTest`: verificação da execução das migrations `V1` e `V2`, integridade dos usuários de seed (4 perfis + hash BCrypt) e estudos clínicos iniciais.
- `OpenApiDocumentationIntegrationTest`: verificação da especificação OpenAPI 3 (`/v3/api-docs`), esquema de segurança Bearer JWT e acesso ao Swagger UI (`/swagger-ui/index.html`).
- `UsuarioDtoValidationTest`: testes unitários de validação Bean Validation dos DTOs de cadastro e atualização de usuários.
- `UsuarioIntegrationTest`: testes de integração do CRUD de usuários com autenticação simulada (`@WithMockUser`), incluindo o bloqueio da exclusão de um usuário responsável por estudos.
- `PermissaoAcessoIntegrationTest` (T26): 15 testes com tokens reais dos 4 perfis do seed, cobrindo a matriz de permissões de usuários e estudos (`200`/`201` quando permitido, `403` quando negado) e o `/api/auth/me`.
- `EstudoServiceTest` (T30): 6 testes unitários das regras do serviço de estudos (datas, responsável inexistente, criação e atualização), com Mockito.
- `EstudoIntegrationTest` (T30): 8 testes de integração do CRUD de estudos (cadastro com `Location`, consulta, listagem do seed, atualização, exclusão, validações `400` e `404`).
- `AuthenticationIntegrationTest`: 13 testes de integração cobrindo login com credenciais válidas, senha incorreta, e-mail inexistente, usuário inativo, campos obrigatórios ausentes, acesso sem token, token malformado, adulterado, expirado, com emissor inválido, consumo com token válido, acesso público ao Swagger UI e compatibilidade do BCrypt (`PasswordEncoder`).

Comando para rodar exclusivamente os testes de autenticação e segurança:
```bash
./mvnw test -Dtest=AuthenticationIntegrationTest
```

#### Evidência da T22 (PBI-05 — testes de login válido, inválido e acesso não autenticado)

| Item | Resultado |
|---|---|
| Classe | `AuthenticationIntegrationTest` (`src/test/java/UEPB/ESWII/ClinicalStudyPB/`) |
| Execução em 26/10/2026 | **13 testes, 0 falhas, 0 erros, 0 ignorados** (`target/surefire-reports`) |
| Contrato testado | `POST /api/auth/login` (`email`, `senha`) → `200` com `token` e `tipo: Bearer`; credenciais inválidas → `401` com `{"erro": "Credenciais inválidas"}`; corpo inválido → `400`; `/api/**` sem token válido → `401` |
| Banco dos testes | H2 em memória (modo PostgreSQL), com as migrations Flyway `V1` e `V2`; o usuário `admin@clinicalstudy.test` vem do seed |
| Segredo JWT dos testes | Valor fictício em `src/test/resources/application.properties`, sem depender de variáveis de ambiente |
| Fora do escopo | Testes da tela de login no React: a T21 está pronta, mas a ferramenta de testes da web ainda não foi escolhida |

### Web e mobile

Ainda não há testes automatizados de componentes nem E2E (a especificação pede, mas não estão no backlog da Sprint 1). Na CI, web e mobile passam por lint e verificação de tipos.

### Integração contínua (T05)

O workflow `.github/workflows/ci.yml` roda a cada push e pull request na `main`, com três jobs:

| Job | Passos |
|---|---|
| Backend | JDK 21 → `./mvnw -B verify` (testes + JaCoCo) → publica os relatórios do Surefire como artefato |
| Web | Node 22 → `npm ci` → `npm run lint` → `npm run build` |
| Mobile | Node 22 → `npm ci` → `npm run lint` → `npm run typecheck` |


## 7. Documentação da API

A API é documentada no padrão **OpenAPI 3** com **springdoc-openapi** (`OpenApiConfig`), incluindo metadados do projeto, organização por tags (`Usuários` e `Estudos Clínicos`), exemplos de requisição/resposta e suporte a autenticação **Bearer JWT** (botão **Authorize** no topo do Swagger UI).

Com o backend em execução (porta padrão `8081`):

- **Swagger UI (atalho):** `http://localhost:8081/swagger-ui.html`
- **Swagger UI (página principal):** `http://localhost:8081/swagger-ui/index.html`
- **Especificação OpenAPI 3 (JSON):** `http://localhost:8081/v3/api-docs`

Para testar endpoints protegidos no Swagger, faça o login em `POST /api/auth/login`, copie o `token` e cole em **Authorize**. Cada endpoint exige um perfil (ver a tabela de permissões na seção 2).

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
│       ├── .env.example          # VITE_API_URL
│       └── src/
│           ├── components/       # Layout (Header, Sidebar) e componentes de formulário
│           ├── contexts/         # Sessão autenticada
│           ├── hooks/            # useCarregamento (dados da API)
│           ├── pages/            # Login, início, usuários, estudos, 404 e acesso negado
│           ├── routes/           # Rotas, menu por perfil e guards
│           ├── services/         # Cliente Axios, chamadas da API e tratamento de erros
│           └── types/            # Tipos que espelham os DTOs do backend
├── mobile/
│   └── ClinicalStudyPB/          # Aplicativo do participante — React Native + Expo (SDK 58)
│       ├── app.json
│       ├── .env.example          # EXPO_PUBLIC_API_URL
│       └── src/
│           ├── app/              # Rotas do Expo Router: login e abas (tabs)
│           ├── api/              # Cliente HTTP e armazenamento seguro do token
│           ├── components/       # Tema e componentes de interface
│           └── contexts/         # Sessão do participante
├── docs/                         # Especificação, arquitetura, planejamento e guias
├── releases/                     # (a criar) Documentação de cada release
├── .github/workflows/ci.yml      # Pipeline de CI (backend, web e mobile)
└── README.md
```

## 10. Documentos de referência

- [Especificação do projeto](docs/especificacao-projeto.md) — escopo, perfis, requisitos funcionais, estratégia de testes e regras de entrega.
- [Documento de Arquitetura de Software (DAS)](docs/arquitetura.md) — diagramas C4, casos de uso, matriz RBAC, classes UML, DER, máquinas de estados e sequência.
- [Planejamento da Sprint 1](docs/sprint1-plano.md) — PBIs, tasks, responsáveis, capacidade, Definition of Done e riscos.
- [Implementação do Backend — Sprint 1](docs/implementacao-backend-sprint1.md) — detalhamento das camadas, endpoints, migrations Flyway e documentação OpenAPI.
- [Status da Sprint 1](docs/status-sprint1.md) — **ponto de partida para retomar o contexto**: situação das 35 tasks, responsáveis, arquivos de cada task e pendências.
- [Implementação do mobile](docs/implementacao-mobile.md) — estrutura do app Expo, navegação, sessão e como rodar em emulador ou celular.
- [Requisitos dos protótipos mobile](docs/prototipos-mobile.md) — telas, fluxos de navegação e roteiro de demonstração do aplicativo do participante (T33 e T34).
- [Protótipo navegável do mobile (Figma)](https://www.figma.com/design/IqDUJYAEp8GvSMO6vaEoNd/Clinical-Studies-Mobile-Design?node-id=39-568&t=t88dh0uGIHa60Db9-1) — telas do aplicativo do participante com o fluxo de navegação para demonstração (T33 e T34).
- [Protótipo navegável da web no Figma](https://www.figma.com/design/e89syvyxm6LEOzBkeLUtea) — 12 telas (login, administrador, pesquisador, coordenador e acesso negado) com navegação entre elas (T32). Para navegar, abra o arquivo e use o modo *Present*, começando pelo Login.
