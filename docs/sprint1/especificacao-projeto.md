# Especificação do Projeto — ClinicalStudyPB

> Resumo da especificação **"PROJETO – Eng.Sw.2 2026.2 — ClinicalStudyPB"** (Engenharia de Software II), mantido no repositório como referência para a equipe. Em caso de divergência, vale o documento original da disciplina.

## 1. Escopo geral

O ClinicalStudyPB é um sistema simplificado para gerenciar o ciclo de vida operacional de estudos clínicos: do cadastro do protocolo de pesquisa ao acompanhamento dos participantes e ao encerramento do estudo.

O sistema **não** realiza diagnósticos nem decisões clínicas automatizadas. Ele apoia a organização, a execução e o acompanhamento dos estudos, sempre com **dados fictícios**.

Abrange: cadastro de protocolos, recrutamento, elegibilidade, inclusão de participantes, agendamento de visitas, acompanhamento e relatórios.

## 2. Perfis de usuários

| Perfil | Responsabilidades |
|---|---|
| **Participante** | Consulta visitas agendadas, acompanha sua participação, registra informações solicitadas e pode solicitar a retirada do estudo. |
| **Pesquisador** | Cadastra protocolos, define critérios de elegibilidade, acompanha participantes, consulta dados e gera relatórios. |
| **Coordenador do Estudo** | Gerencia recrutamento, verifica elegibilidade, organiza visitas, acompanha pendências e monitora o andamento do estudo. |
| **Administrador** | Gerencia usuários, perfis de acesso, configurações e registros de auditoria. |

## 3. Requisitos funcionais

### Módulo A — Usuários e autenticação

| ID | Requisito | Interface |
|---|---|---|
| RF01 | Cadastrar e gerenciar usuários com perfis de pesquisador, coordenador, administrador e participante. | Web |
| RF02 | Permitir autenticação e encerramento de sessão. | Web/Mobile |
| RF03 | Restringir funcionalidades conforme o perfil do usuário. | Web/Mobile |

### Módulo B — Gestão de estudos

| ID | Requisito | Interface |
|---|---|---|
| RF04 | Cadastrar estudos com título, objetivo, responsável, datas e limite de participantes. | Web |
| RF05 | Definir critérios de inclusão e exclusão para cada estudo. | Web |
| RF06 | Definir visitas previstas e suas atividades. | Web |
| RF07 | Gerenciar os estados do estudo: planejamento, recrutamento, execução e encerrado. | Web |

### Módulo C — Recrutamento

| ID | Requisito | Interface |
|---|---|---|
| RF08 | Cadastrar candidatos vinculados a um estudo. | Web |
| RF09 | Registrar a avaliação dos critérios de elegibilidade. | Web |
| RF10 | Registrar o consentimento simulado do candidato. | Web |
| RF11 | Incluir candidatos elegíveis e com consentimento registrado, respeitando o limite de vagas. | Web |

### Módulo D — Acompanhamento

| ID | Requisito | Interface |
|---|---|---|
| RF12 | Agendar visitas para os participantes incluídos. | Web |
| RF13 | Registrar a realização, ausência ou cancelamento de visitas. | Web |
| RF14 | Registrar resultados fictícios das avaliações previstas. | Web |
| RF15 | Consultar o histórico de visitas e avaliações de um participante. | Web |
| RF16 | Registrar a retirada de um participante do estudo. | Web |

### Módulo E — Aplicativo do participante

| ID | Requisito | Interface |
|---|---|---|
| RF17 | Permitir ao participante consultar os estudos em que está incluído. | Mobile |
| RF18 | Exibir as próximas visitas e seu histórico de comparecimento. | Mobile |
| RF19 | Permitir confirmar presença prevista ou solicitar reagendamento de visita. | Mobile |
| RF20 | Permitir solicitar retirada voluntária do estudo. | Mobile |

### Módulo F — Monitoramento

| ID | Requisito | Interface |
|---|---|---|
| RF21 | Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes. | Web |
| RF22 | Gerar relatório simplificado de recrutamento e acompanhamento por estudo. | Web |

**Requisito surpresa:** será definido pela professora durante a Release 3.

## 4. Organização da equipe (proposta da especificação)

A especificação prevê 6 papéis. A equipe adaptou o planejamento para **7 integrantes** (ver [plano da Sprint 1](sprint1-plano.md)).

| Papel | Responsabilidades |
|---|---|
| Líder técnico / Gerente | Arquitetura, API, integração e decisões técnicas |
| Desenvolvedor Backend | Regras de negócio, persistência e testes |
| Desenvolvedor Web | Telas de estudos, protocolos e usuários |
| Desenvolvedor Web | Telas de recrutamento, visitas e relatórios |
| Desenvolvedor Mobile | Aplicativo do participante |
| QA / DevOps | Testes, integração contínua, qualidade e suporte à implementação |

Planejamento, documentação e acompanhamento das sprints são responsabilidades compartilhadas. O papel de QA não significa que apenas um integrante escreve testes.

## 5. Cronograma de sprints

| Sprint | Período |
|---|---|
| Sprint 1 | 13/10 – 26/10 |
| Sprint 2 | 27/10 – 09/11 |
| Sprint 3 | 10/11 – 23/11 |
| Sprint 4 | 24/11 – 07/12 |
| Sprint 5 | 08/12 – 17/12 |
| Sprint 6 | 02/02 – 11/02 |

## 6. Stack tecnológico

A equipe escolheu a **Stack A**: Java + Spring Boot (backend), React + TypeScript (web), React Native + Expo (mobile) e PostgreSQL (banco de dados).

Para o mobile basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas.

## 7. Estratégia de testes

| Nível | O que testar | Exemplos |
|---|---|---|
| Unitários | Regras de negócio isoladas | Elegibilidade, limite de participantes, estados do estudo |
| Integração | Comunicação entre componentes | API, serviços, repositórios e banco de dados |
| Componentes web | Comportamento das telas | Validação de formulários, botões, mensagens de erro |
| Componentes mobile | Comportamento das telas mobile | Login, listagem de visitas, confirmação |
| Ponta a ponta (E2E) | Fluxos completos | Cadastrar estudo, incluir participante e agendar visita |

**Critérios mínimos:**

- Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações).
- Testes unitários para todas as regras de negócio relevantes.
- Testes de integração para os principais endpoints.
- Testes automatizados de componentes nas interfaces web e mobile.
- Pelo menos um fluxo E2E automatizado na web e um no mobile.
- Execução automatizada dos testes a cada pull request ou integração à branch principal.

## 8. Ferramentas complementares

| Finalidade | Opções |
|---|---|
| Versionamento | GitHub, GitLab |
| Gerenciamento ágil | Azure DevOps |
| Prototipação | Figma, Penpot |
| Documentação da API | OpenAPI/Swagger |
| Teste de API | Postman, Insomnia, Cypress |
| Qualidade de código | SonarQube, SonarCloud, ESLint, Checkstyle |
| Cobertura de testes | JaCoCo, Istanbul, Coverage.py |
| Integração contínua | GitHub Actions, GitLab CI |
| Containers | Docker, Docker Compose |
| Diagramas | PlantUML, Mermaid, diagrams.net |

## 9. Regras para a escolha tecnológica

1. Cada equipe pode escolher livremente entre os stacks sugeridos.
2. O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).
3. A interface **web** atende aos perfis de pesquisador, coordenador e administrador.
4. A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.
5. O sistema deve ter persistência de dados e controle de acesso por perfil.
6. As equipes devem implementar testes automatizados e apresentar evidências de cobertura.
7. O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.
8. As tecnologias escolhidas devem ser documentadas e justificadas.

## 10. Entregas

Cada release é entregue pelo repositório no GitHub, com código-fonte e documentação daquela versão. A documentação fica no **README.md** (substitui os relatórios em PDF).

### Conteúdo obrigatório do repositório

- Código-fonte do backend, da aplicação web e do aplicativo mobile, conforme os requisitos da release.
- Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.
- Testes automatizados dos requisitos implementados.
- Tratamento de erros e validações das regras de negócio.
- Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.
- Os requisitos de releases anteriores devem continuar funcionando.

### README principal — seções obrigatórias

1. **Apresentação e escopo** — descrição, perfis, funcionalidades principais e integrantes.
2. **Arquitetura** — organização e responsabilidades dos componentes, com diagramas de casos de uso, classes/modelo de domínio, sequência dos principais fluxos e arquitetura (web, mobile, backend e banco). Podem ser imagens ou Mermaid.
3. **Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.
4. **Stack tecnológico** — tecnologias e versões (backend, web, mobile, banco, testes, ferramentas) e pré-requisitos do ambiente.
5. **Instalação e execução** — configuração do banco, backend, web, mobile em dispositivo/emulador e credenciais fictícias por perfil.
6. **Testes e cobertura** — testes implementados, resultados, relatório e evidência dos 80% de cobertura do backend.
7. **Documentação da API** — disponível no Swagger UI.

O README principal deve conter links para as três releases. Imagens, diagramas e evidências podem ficar em subpastas, referenciados por links relativos.

### Documentação de cada release

| Entrega | Documento |
|---|---|
| Release 1 | `releases/release1/README.md` |
| Release 2 | `releases/release2/README.md` |
| Release 3 | `releases/release3/README.md` |

Cada README de release apresenta:

- Objetivo da entrega e sprints abrangidas.
- Requisitos previstos, concluídos, parciais e não implementados, com justificativas.
- Funcionalidades entregues e capturas das interfaces.
- Alterações na arquitetura e no stack, quando houver.
- Resultados dos testes e cobertura da versão.
- Problemas conhecidos e pendências.
- Seção **Processo de desenvolvimento**, para cada sprint:
  1. **Backlog e tarefas** — objetivo da sprint, PBIs e tarefas.
  2. **Organização da equipe** — papéis, responsabilidades e contribuições de cada integrante.
  3. **Burndown** — gráfico do Azure DevOps com breve análise planejado × realizado.
  4. **Revisão** — o que foi entregue, impedimentos e ações de melhoria.

### Identificação da entrega

Ao finalizar cada release:

- Criar a tag `release1`, `release2` ou `release3` apontando para a versão entregue.
- Criar uma GitHub Release associada à tag, com resumo e link para o README da entrega.
- Apresentar o sistema funcionando, demonstrando requisitos, interfaces e testes.

A avaliação usa a versão identificada pela tag.

### Documentação adicional da Release 3

Após a troca de projetos, a equipe receptora registra:

- Versão recebida e resultado da instalação e execução.
- Análise da arquitetura, do código e dos testes existentes.
- Plano de manutenção e requisitos surpresa.
- Alterações e refatorações realizadas.
- Testes de regressão e atualização das instruções de execução.
