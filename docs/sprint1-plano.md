# Planejamento da Sprint 1

> **Release 1** · Sprint 1 · 13 a 26 de outubro de 2026 · 7 integrantes · Scrum · Azure DevOps (Azure Boards) · GitHub
>
> Documento de apoio ao planejamento, para discussão da equipe. Estimativas, divisão de trabalho e tecnologias complementares são sugestões a serem acordadas pelo time.

## 1. Visão geral e objetivo

**Objetivo:** estabelecer a base tecnológica e entregar uma primeira versão funcional da aplicação web, contemplando cadastro e gerenciamento de usuários (RF01), autenticação (RF02), controle de acesso (RF03) e cadastro de estudos (RF04), além de protótipos navegáveis de web e mobile.

**Stack A:** Java com Spring Boot (backend), React + TypeScript (web), React Native + Expo (mobile) e PostgreSQL (banco de dados). Os testes serão implementados gradualmente com ferramentas compatíveis com essa stack.

**Delimitação do escopo:** na Sprint 1, login e permissões serão demonstrados principalmente na web. O login e as funcionalidades completas do aplicativo mobile ficam para sprints posteriores.

## 2. Product Backlog Items selecionados

| ID | PBI / Entrega esperada | Prioridade | Horas |
|---|---|---|---|
| PBI-01 | Configuração do repositório e arquitetura | Alta | 18h |
| PBI-02 | Estrutura inicial do Spring Boot e PostgreSQL | Alta | 20h |
| PBI-03 | Inicialização das aplicações web e mobile | Alta | 14h |
| PBI-04 | RF01 — Cadastro e gerenciamento de usuários | Alta | 20h |
| PBI-05 | RF02 — Autenticação e logout (web) | Alta | 18h |
| PBI-06 | RF03 — Controle de acesso por perfil (web) | Alta | 14h |
| PBI-07 | RF04 — Cadastro de estudos clínicos | Alta | 23h |
| PBI-08 | Protótipos navegáveis das interfaces | Média | 15h |
| **Total** | **8 PBIs / 35 tasks** | | **142h** |

## 3. Sprint Backlog

### PBI-01 — Configuração do repositório e arquitetura

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T01 | Definir e documentar arquitetura Spring Boot + React + React Native + PostgreSQL | I1 | 4h |
| T02 | Configurar GitHub, estratégia de branches e regras de Pull Request | I1 | 5h |
| T03 | Elaborar diagrama inicial da arquitetura | I2 | 3h |
| T04 | Criar README com estrutura e instruções iniciais | I7 | 3h |
| T05 | Configurar GitHub Actions para build e verificações básicas | I1 | 3h |

**Critério de aceite:** repositório organizado, arquitetura documentada, orientações iniciais de execução e pipeline básica funcionando.

### PBI-02 — Estrutura inicial do Spring Boot e PostgreSQL

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T06 | Inicializar projeto Spring Boot (controllers, services, repositories) | I3 | 5h |
| T07 | Configurar PostgreSQL e conexão da aplicação | I1 | 4h |
| T08 | Criar modelos JPA iniciais de usuários e estudos | I3 | 5h |
| T09 | Criar migrations iniciais com Flyway | I2 | 3h |
| T10 | Configurar documentação da API com springdoc-openapi / Swagger UI | I2 | 3h |

**Critério de aceite:** backend inicia, conecta ao PostgreSQL, executa migrations e disponibiliza documentação da API.

### PBI-03 — Inicialização das aplicações web e mobile

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T11 | Inicializar React + TypeScript com Vite e React Router | I5 | 4h |
| T12 | Inicializar React Native + Expo com TypeScript e navegação básica | I7 | 5h |
| T13 | Desenvolver layout base e navegação inicial da aplicação web | I6 | 3h |
| T14 | Configurar cliente HTTP Axios e variáveis de ambiente no React | I5 | 2h |

**Critério de aceite:** projetos web e mobile executam localmente; frontend web tem navegação e cliente de API configurados.

### PBI-04 — RF01 — Cadastro e gerenciamento de usuários

*Como administrador, quero cadastrar e gerenciar usuários por perfil para controlar quem utiliza o sistema.*

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T15 | Criar DTOs e validações de usuários | I3 | 3h |
| T16 | Implementar serviços e endpoints de cadastro e gerenciamento de usuários | I4 | 6h |
| T17 | Desenvolver formulário e listagem de usuários no React | I5 | 6h |
| T18 | Implementar testes unitários e de integração do módulo | I2 | 5h |

**Critério de aceite:** administrador cadastra, consulta e gerencia usuários dos quatro perfis, com validação e persistência.

### PBI-05 — RF02 — Autenticação e logout (web)

*Como usuário, quero entrar e sair do sistema com segurança para acessar minhas funcionalidades.*

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T19 | Configurar Spring Security, senhas protegidas e autenticação por token | I4 | 6h |
| T20 | Implementar endpoint de login e validação de credenciais | I4 | 4h |
| T21 | Desenvolver login web, gerenciamento de autenticação e logout | I5 | 5h |
| T22 | Criar testes de login válido, inválido e acesso não autenticado | I7 | 3h |

**Critério de aceite:** login web funciona com credenciais válidas; credenciais inválidas são rejeitadas; logout encerra o acesso na interface. Documentar expiração/revogação caso se use JWT.

### PBI-06 — RF03 — Controle de acesso por perfil (web)

*Como administrador, quero restringir funcionalidades de acordo com o perfil para proteger os dados e as operações.*

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T23 | Definir matriz de permissões dos quatro perfis | I1 | 2h |
| T24 | Implementar autorização por perfil no Spring Security | I4 | 5h |
| T25 | Proteger rotas e funcionalidades da aplicação React | I6 | 4h |
| T26 | Criar testes de permissões e acessos negados | I2 | 3h |

**Critério de aceite:** usuários sem permissão não acessam endpoints nem telas restritas; a autorização é aplicada no backend.

### PBI-07 — RF04 — Cadastro de estudos clínicos

*Como pesquisador, quero cadastrar estudos com suas informações principais para organizar as pesquisas.*

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T27 | Implementar DTOs, validações e serviço de estudos | I3 | 6h |
| T28 | Implementar endpoints REST para cadastro e consulta de estudos | I3 | 4h |
| T29 | Desenvolver formulário e listagem de estudos no React | I6 | 8h |
| T30 | Implementar testes unitários e de integração de estudos | I2 | 3h |
| T31 | Integrar telas com a API e exibir erros de validação | I5 | 2h |

**Critério de aceite:** pesquisador autorizado cadastra e consulta estudos com título, objetivo, responsável, datas e limite de participantes; dados persistem no PostgreSQL.

### PBI-08 — Protótipos navegáveis das interfaces

| Task | Descrição | Resp. | Horas |
|---|---|---|---|
| T32 | Criar protótipos navegáveis das principais telas web no Figma | I6 | 5h |
| T33 | Criar protótipos navegáveis das telas do participante no Figma | I7 | 5h |
| T34 | Preparar fluxo de navegação mobile para demonstração | I7 | 3h |
| T35 | Revisar fluxos e registrar ajustes e documentação | I1 | 2h |

**Critério de aceite:** links dos protótipos navegáveis web e mobile disponíveis para revisão e demonstração.

## 4. Distribuição entre os 7 integrantes

| Integrante | Foco principal | Horas |
|---|---|---|
| I1 | Scrum Master, arquitetura e DevOps | 20h |
| I2 | Product Owner, testes e documentação | 20h |
| I3 | Backend: domínio, persistência e estudos | 23h |
| I4 | Backend: usuários, segurança e autorização | 21h |
| I5 | Frontend React: autenticação e integração | 19h |
| I6 | Frontend React: estudos e protótipos web | 20h |
| I7 | Mobile React Native, protótipos e testes | 19h |
| **Total** | | **142h** |

Atribuições provisórias: substituir I1–I7 pelos nomes após a reunião. Testes, documentação e revisão de código são responsabilidades compartilhadas.

## 5. Capacidade e viabilidade

| Indicador | Hipótese |
|---|---|
| Disponibilidade por integrante | 12 horas por semana |
| Capacidade em duas semanas | 7 × 12h × 2 = 168 horas |
| Horas planejadas nas tasks | 142 horas |
| Margem de capacidade | 26 horas (≈ 15%) |

**Ponto de atenção:** I3 recebeu 23 horas, próximo ao limite hipotético de 24 horas. Se a disponibilidade for menor, negociar escopo e redistribuir tasks.

## 6. Organização no Azure DevOps

Processo **Scrum** no Azure Boards: os 8 itens entram como PBIs na iteração Sprint 1, e as 35 atividades como Tasks filhas.

1. Cadastrar a Sprint 1 (13/10/2026 a 26/10/2026) em *Project Settings > Project configuration > Iterations*.
2. Criar os PBIs em *Boards > Backlogs* com título, descrição, prioridade e critérios de aceite.
3. Associar os PBIs à Sprint 1 e criar as Tasks filhas em *Boards > Sprints > Backlog*.
4. Preencher *Assigned To* e *Remaining Work* (horas) em cada Task e mantê-los atualizados.
5. Registrar a disponibilidade em *Sprints > Capacity* e acompanhar pelo Taskboard/Burndown.
6. Vincular commits e Pull Requests aos itens de trabalho e registrar impedimentos.

**Effort ≠ horas:** o campo *Effort* do PBI é estimativa relativa (ex.: Story Points). Horas vão nas Tasks, em *Remaining Work*.

## 7. Ordem de execução e dependências

Início: PBI-01 (repositório/arquitetura) e, em paralelo, PBI-08 (prototipação). Em seguida, PBI-02 (backend/banco) e PBI-03 (web/mobile). Com a infraestrutura pronta, RF01 e RF02; depois RF03 e RF04.

| Período | Ênfase |
|---|---|
| Semana 1 — 13/10 a 19/10 | Infraestrutura e arquitetura; protótipos; modelos de dados; início dos endpoints e telas. |
| Semana 2 — 20/10 a 26/10 | Integração web/API; autenticação e autorização; cadastro de estudos; testes, ajustes e demonstração. |

## 8. Definition of Done (proposta)

- Critérios de aceite do PBI atendidos e funcionalidade demonstrável com dados fictícios.
- Código integrado, revisado por outro integrante e sem falhas conhecidas que impeçam o fluxo principal.
- Testes relevantes executados e resultados registrados; backend validado com persistência real quando aplicável.
- Pull Request aprovado e integrado à branch principal; documentação e configurações atualizadas.
- Para PBIs de infraestrutura/protótipos, entrega verificável (repositório, pipeline, documentação ou link navegável).

## 9. Eventos Scrum

| Evento | Finalidade |
|---|---|
| Sprint Planning — 13/10 | Validar meta, PBIs, horas, responsáveis, capacidade e dependências. |
| Daily Scrum — dias úteis | Sincronizar o trabalho e expor impedimentos (timebox de 15 minutos). |
| Sprint Review — até 26/10 | Demonstrar o incremento, receber feedback e atualizar o Product Backlog. |
| Sprint Retrospective — até 26/10 | Rever colaboração, estimativas e práticas técnicas; propor melhorias para a Sprint 2. |

## 10. Decisões pendentes e riscos

**A validar:** versões exatas da stack; padrão de autenticação (incluindo expiração de tokens); formato das permissões; bibliotecas de teste e prototipação; estratégia de migração e configuração local do PostgreSQL.

**Riscos:** esforço insuficiente para RF01–RF04; dependências entre backend e frontend; dificuldades iniciais com Spring Security; pouca experiência com as tecnologias. **Resposta:** limitar o trabalho em progresso, demonstrar integração cedo e renegociar PBIs se a capacidade real for menor.

---

Base: especificação "PROJETO – Eng.Sw.2 2026.2 — ClinicalStudyPB" (ver [especificacao-projeto.md](especificacao-projeto.md)). A especificação prevê seis integrantes; este planejamento foi adaptado para sete por decisão da equipe.
