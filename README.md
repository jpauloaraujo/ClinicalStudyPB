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

> _A ser documentado (PBI-01: T01 — arquitetura; T03 — diagrama inicial)._

Visão geral: uma **API REST** em Spring Boot, documentada em OpenAPI, é compartilhada pela **aplicação web** (perfis administrador, pesquisador e coordenador) e pelo **aplicativo mobile** (perfil participante). Os dados são persistidos em **PostgreSQL**.

Diagramas previstos (imagens ou Mermaid):

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
| Backend | Java + Spring Boot | _a definir_ |
| Banco de dados | PostgreSQL | _a definir_ |
| Migrations | Flyway | _a definir_ |
| Documentação da API | springdoc-openapi / Swagger UI | _a definir_ |
| Web | React + TypeScript (Vite, React Router, Axios) | _a definir_ |
| Mobile | React Native + Expo (TypeScript) | _a definir_ |
| Testes | _a definir_ | — |
| CI | GitHub Actions | — |
| Gestão ágil | Azure DevOps (Azure Boards) | — |
| Prototipação | Figma | — |

> As versões exatas serão fixadas pela equipe e registradas aqui assim que os projetos forem inicializados.

## 5. Instalação e execução

### Pré-requisitos

- [Git](https://git-scm.com/)
- JDK (versão a definir) — backend
- [Node.js](https://nodejs.org/) LTS e npm — web e mobile
- [PostgreSQL](https://www.postgresql.org/) — banco de dados
- [Expo Go](https://expo.dev/go) em um dispositivo físico **ou** um emulador Android/iOS — mobile

### Clonar o repositório

```bash
git clone https://github.com/jpauloaraujo/ClinicalStudyPB.git
cd ClinicalStudyPB
```

### Banco de dados

> _A ser documentado (PBI-02: T07 — configuração do PostgreSQL; T09 — migrations Flyway)._

### Backend

> _A ser documentado (PBI-02: T06 — projeto Spring Boot)._

### Aplicação web

> _A ser documentado (PBI-03: T11 — projeto React + Vite; T14 — variáveis de ambiente)._

### Aplicativo mobile

> _A ser documentado (PBI-03: T12 — projeto React Native + Expo)._

### Credenciais de demonstração

> _Credenciais fictícias para cada perfil serão listadas aqui após a implementação da autenticação (PBI-05). Nunca inclua senhas ou credenciais reais no repositório._

## 6. Testes e cobertura

Meta mínima: **80% de cobertura de linhas do código de negócio do backend**, além de testes de componentes web e mobile e ao menos um fluxo E2E em cada interface. Os testes devem ser executados automaticamente a cada pull request.

> _Instruções de execução, resultados e relatórios de cobertura serão adicionados conforme os testes forem implementados._

## 7. Documentação da API

A API será documentada no padrão OpenAPI e disponibilizada via **Swagger UI**.

> _O endereço do Swagger UI será informado após a configuração do springdoc-openapi (PBI-02: T10)._

## 8. Releases

| Release | Sprints | Documento |
|---|---|---|
| Release 1 | Sprint 1 – _a definir_ | `releases/release1/README.md` _(a criar)_ |
| Release 2 | _a definir_ | `releases/release2/README.md` _(a criar)_ |
| Release 3 | _a definir_ | `releases/release3/README.md` _(a criar)_ |

Cada entrega é identificada por uma tag (`release1`, `release2`, `release3`) e uma GitHub Release associada.

## 9. Estrutura do repositório

Organização prevista (as pastas serão criadas conforme as tarefas da sprint):

```text
ClinicalStudyPB/
├── backend/        # API REST — Java + Spring Boot
├── web/            # Aplicação web — React + TypeScript
├── mobile/         # Aplicativo do participante — React Native + Expo
├── docs/           # Especificação, planejamento, diagramas e imagens
├── releases/       # Documentação de cada release (release1/, release2/, release3/)
├── .github/        # Workflows do GitHub Actions
└── README.md
```

## 10. Documentos de referência

- [Especificação do projeto](docs/especificacao-projeto.md) — escopo, perfis, requisitos funcionais, estratégia de testes e regras de entrega.
- [Planejamento da Sprint 1](docs/sprint1-plano.md) — PBIs, tasks, responsáveis, capacidade, Definition of Done e riscos.
