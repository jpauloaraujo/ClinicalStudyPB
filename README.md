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
| **Banco de dados** | — | PostgreSQL, acessado apenas pelo backend via Spring Data JPA. |

Web e mobile **não** acessam o banco diretamente: toda leitura e escrita passa pela API, que aplica validações e controle de acesso.

### Organização do backend

Aplicação Spring Boot com pacote raiz `UEPB.ESWII.ClinicalStudyPB`, organizada em camadas:

| Pacote | Conteúdo | Situação |
|---|---|---|
| `domain.model` | Entidades JPA do domínio (ex.: `Usuario`, mapeada para a tabela `usuarios`) | ✅ iniciado |
| controllers / services / repositories | Endpoints REST, regras de negócio e acesso a dados | ⏳ T06 |
| DTOs e validações | Objetos de entrada/saída da API com Bean Validation | ⏳ T15 / T27 |
| Segurança | Configuração do Spring Security, autenticação por token e permissões por perfil | ⏳ T19 / T24 |

Dependências principais: Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation, springdoc-openapi, driver PostgreSQL e Lombok.

### Organização da web

Projeto criado com o template React + TypeScript do Vite, com React Compiler habilitado e ESLint. Navegação (React Router), cliente HTTP (Axios) e variáveis de ambiente serão adicionados em T11, T13 e T14.

Diagramas previstos (imagens ou Mermaid) — T03:

- [ ] Arquitetura (comunicação entre web, mobile, backend e banco de dados)
- [ ] Casos de uso
- [ ] Classes / modelo de domínio
- [ ] Sequência dos principais fluxos

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
| Backend | Spring Boot (Web MVC, Data JPA, Security, Validation) | 4.1.1 |
| Backend | Maven (via Maven Wrapper `mvnw`) | 3.10.0 |
| Backend | Lombok | gerenciada pelo Spring Boot |
| Documentação da API | springdoc-openapi (Swagger UI) | 3.1.1 |
| Banco de dados | PostgreSQL (driver JDBC) | _versão do servidor a definir_ |
| Migrations | Flyway | _planejado (T09)_ |
| Web | React | 19.3 |
| Web | TypeScript | 6.0 |
| Web | Vite (com React Compiler) | 8.3 |
| Web | ESLint | 10 |
| Web | React Router / Axios | _planejado (T11 / T14)_ |
| Mobile | React Native + Expo (TypeScript) | _a definir (T12)_ |
| Testes | JUnit + Spring Boot Test (backend); demais _a definir_ | — |
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

### Banco de dados

> _A conexão com o PostgreSQL ainda não está configurada em `application.properties` (T07), e as migrations Flyway ainda não existem (T09). Nome do banco, usuário e variáveis de ambiente serão documentados aqui._

### Backend

```bash
cd backend/ClinicalStudyPB
./mvnw spring-boot:run
```

No Windows (PowerShell/CMD), use `mvnw.cmd spring-boot:run`. A API sobe por padrão em `http://localhost:8080`.

> ⚠️ Enquanto o datasource do PostgreSQL não for configurado (T07), a aplicação **não inicia**, porque o Spring Data JPA exige uma conexão com o banco.

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

### Credenciais de demonstração

> _Credenciais fictícias para cada perfil serão listadas aqui após a implementação da autenticação (PBI-05). Nunca inclua senhas ou credenciais reais no repositório._

## 6. Testes e cobertura

Meta mínima: **80% de cobertura de linhas do código de negócio do backend**, além de testes de componentes web e mobile e ao menos um fluxo E2E em cada interface. Os testes devem ser executados automaticamente a cada pull request.

### Backend

Testes com JUnit 5 e Spring Boot Test (starters de teste de Web MVC, Data JPA, Security e Validation):

```bash
cd backend/ClinicalStudyPB
./mvnw test
```

Atualmente há apenas o teste `ClinicalStudyPbApplicationTests.contextLoads`, que também depende do banco configurado (T07). A ferramenta de cobertura (ex.: JaCoCo) ainda será adicionada.

> _Testes web e mobile, resultados e relatórios de cobertura serão adicionados conforme forem implementados._

## 7. Documentação da API

A API é documentada no padrão OpenAPI com **springdoc-openapi**, que já está nas dependências do backend. Com o backend em execução, o Swagger UI fica no endereço padrão do springdoc:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

> _Como o Spring Security está nas dependências e ainda sem configuração, todas as rotas (inclusive o Swagger) exigem login com o usuário padrão gerado pelo Spring. A liberação do Swagger e a configuração final ficam para T10 e T19._

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
│           │   └── domain/model/  # Entidades JPA (ex.: Usuario)
│           ├── main/resources/application.properties
│           └── test/java/...      # Testes do backend
├── web/
│   └── ClinicalStudyPB/          # Aplicação web — React + TypeScript + Vite
│       ├── package.json
│       └── src/
├── mobile/
│   └── ClinicalStudyPB/          # Aplicativo do participante — React Native + Expo
├── docs/                         # Especificação, planejamento, diagramas e imagens
├── releases/                     # (a criar) Documentação de cada release
├── .github/workflows/            # (a criar) Workflows do GitHub Actions
└── README.md
```

## 10. Documentos de referência

- [Especificação do projeto](docs/especificacao-projeto.md) — escopo, perfis, requisitos funcionais, estratégia de testes e regras de entrega.
- [Planejamento da Sprint 1](docs/sprint1-plano.md) — PBIs, tasks, responsáveis, capacidade, Definition of Done e riscos.
