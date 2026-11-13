# Relatório de Testes — ClinicalStudyPB, Sprints 1 a 3

| | |
|---|---|
| **Projeto** | ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos (Engenharia de Software II, UEPB, 2026.2) |
| **Versão testada** | Ramo `develop` no commit `499c951` (Sprint 3 em andamento: T73, T74, T87 e T95 integradas). Também foi testado o Pull Request #12 (T75), ainda não integrado |
| **Tipo do documento** | Relatório consolidado de execução de testes (estrutura inspirada na IEEE 829 / ISO 29119-3). Complementa o [relatório da Release 1](relatorio-de-testes-release1.md) |
| **Data da execução** | 13/11/2026 |
| **Escopo funcional** | Sprint 1 (RF01 a RF04), Sprint 2 (RF05 a RF11) e início da Sprint 3 (RF12 a RF15: modelo, contrato e dados de demonstração) |
| **Resultado geral** | **Aprovado na `develop`:** 478 testes de backend (476 passaram, 0 falhas, 2 ignorados de propósito), 133 de web e 29 de E2E; cobertura de linhas do código de negócio de **99,2%** (meta: 80%). **Reprovado no Pull Request #12:** 1 erro de backend (seção 8) |

---

## 1. Objetivo e escopo

Registrar, em um só lugar, **todos os testes automatizados que o projeto tem desde a Sprint 1 até o ponto atual da Sprint 3**, os resultados da última execução, a cobertura de código e os defeitos conhecidos, mantendo a rastreabilidade entre requisito, teste e defeito. O detalhe de estratégia, técnicas e evidências da Release 1 continua no [relatório da Release 1](relatorio-de-testes-release1.md); este documento o estende e o atualiza com a Sprint 3.

**Como cada teste foi atribuído a uma sprint:** pela **data de criação do arquivo de teste** no repositório. Sprint 1: até 31/10/2026; Sprint 2: de 01/11 a 09/11/2026; Sprint 3: a partir de 10/11/2026. Quando uma classe de teste nasceu em uma sprint e recebeu casos em outra, a tabela diz quantos casos são de cada uma.

**Fora do escopo:** o aplicativo mobile ainda não tem testes automatizados (só `lint` e `tsc --noEmit`); testes de carga, desempenho e segurança ofensiva; navegadores além do Chromium.

## 2. Resumo dos resultados por sprint

| Suíte | Sprint 1 | Sprint 2 | Sprint 3 (em andamento) | **Total** | Falhas |
|---|---|---|---|---|---|
| Backend (JUnit) | 72 | 362 | 44 (2 ignorados) | **478** | 0 |
| Web (Vitest) | 9 | 124 | 0 | **133** | 0 |
| E2E (Playwright) | 9 | 20 | 0 | **29** | 0 |
| Mobile (Jest/Maestro) | 0 | 0 | 0 (previstos T113, T114 e T120) | **0** | — |
| **Total de casos automatizados** | **90** | **506** | **44** | **640** | **0** |
| Roteiro de recrutamento (T68) | — | 58 verificações, 58 aprovadas | — | 58 | 0 |

Observações:

- Os roteiros T67 e T68 e o job Docker rodam no CI; os números acima repetem os da Release 1, porque não foram alterados nesta sprint.
- Web e E2E não tiveram casos novos na Sprint 3 até aqui: as telas do agendamento e do histórico são das tasks T77, T79 e T97.
- **Pull Request #12 (T75), não integrado:** rodado à parte, o backend dele mais a `develop` tem 488 testes e **1 erro** (seção 8).

### 2.1 Backend por sprint e por nível

| Sprint | Casos | Foco |
|---|---|---|
| 1 | 72 | Usuários, autenticação, perfis, estudos, migrations iniciais, Swagger |
| 2 | 362 | Critérios, visitas, estados, candidatos, elegibilidade, consentimento, inclusão e concorrência, contrato, CORS, dados de demonstração |
| 3 | 44 | Modelo da visita agendada (T73), contrato do agendamento e demos (T74), modelo dos resultados (T87), DTOs do histórico e demo do P4 (T95) |

## 3. Ambiente de teste

| Item | Valor |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, JUnit 5, Mockito, MockMvc, Spring Security Test |
| Banco nos testes | H2 em memória, modo PostgreSQL (`DATABASE_TO_LOWER=TRUE`), com as mesmas migrations do Flyway e `ddl-auto=validate` |
| Banco no CI | Mesmo conjunto também contra PostgreSQL real (job "Backend (testes no PostgreSQL)"). **Não foi executado nesta máquina** |
| Cobertura | JaCoCo 0.8.14, mínimo de 80% de linhas do código de negócio, bloqueante no `verify` |
| Web | Vitest 133 casos; Playwright 29 casos (Chromium) |
| Sistema | Windows 11, JDK 21, Maven 3.9.15 |

---

## 4. Resultados detalhados do backend

### 4.1 Por classe de teste

| Classe | Sprint | Casos | Falhas | Ignorados | Foco |
|---|---|---|---|---|---|
| `AuthenticationIntegrationTest` | 1 | 13 | 0 | 0 | Login válido e inválido, token, `/me`, expiração (RF02) |
| `ClinicalStudyPbApplicationTests` | 1 | 1 | 0 | 0 | O contexto da aplicação sobe (Spring, Flyway e Hibernate) |
| `EstudoIntegrationTest` | 1 | 8 | 0 | 0 | CRUD de estudos (RF04) |
| `EstudoServiceTest` | 1 | 8 | 0 | 0 | Regras do estudo |
| `FlywayMigrationIntegrationTest` | 1 e 3 | 12 | 0 | 0 | Migrations aplicam, schema bate com as entidades; UNIQUE, CHECKs e ausência de cascata das tabelas novas (3 da Sprint 1 e 9 da Sprint 3) |
| `OpenApiDocumentationIntegrationTest` | 1 | 2 | 0 | 0 | Swagger UI e `/v3/api-docs` |
| `PermissaoAcessoIntegrationTest` | 1 | 15 | 0 | 0 | Matriz de permissões por perfil (RF03) |
| `RegrasDeContaIntegrationTest` | 1 | 8 | 0 | 0 | Regras do último administrador e da própria conta |
| `UsuarioDtoValidationTest` | 1 | 4 | 0 | 0 | Validação do cadastro de usuário |
| `UsuarioIntegrationTest` | 1 | 10 | 0 | 0 | CRUD de usuários (RF01) |
| `AvaliacaoEndpointTest` | 2 | 17 | 0 | 0 | Endpoints de avaliação |
| `AvaliacaoHistoricoTest` | 2 | 1 | 0 | 0 | Histórico com critérios carregados do banco |
| `AvaliacaoRequestValidacaoTest` | 2 | 7 | 0 | 0 | Validação da avaliação |
| `AvaliacaoServiceTest` | 2 | 8 | 0 | 0 | Serviço de avaliação (RF09) |
| `CandidatoIntegrationTest` | 2 | 14 | 0 | 0 | Endpoints, paginação e filtro de candidatos |
| `CandidatoServiceTest` | 2 | 9 | 0 | 0 | Regras de candidatos (RF08) |
| `ConsentimentoEndpointTest` | 2 | 15 | 0 | 0 | Endpoints de consentimento |
| `ConsentimentoServiceTest` | 2 | 15 | 0 | 0 | Serviço de consentimento (RF10) |
| `ContratoOpenApiTest` | 2 | 3 | 0 | 0 | API igual ao contrato OpenAPI da Sprint 2 |
| `CorsIntegrationTest` | 2 | 3 | 0 | 0 | CORS padrão |
| `CorsOrigensConfiguradasIntegrationTest` | 2 | 2 | 0 | 0 | CORS configurável |
| `CriterioDescricaoRepetidaTest` | 2 | 1 | 0 | 0 | Descrição repetida |
| `CriterioEndpointTest` | 2 | 19 | 0 | 0 | Endpoints de critérios |
| `CriterioRequestValidacaoTest` | 2 | 19 | 0 | 0 | Limites do critério |
| `CriterioServiceTest` | 2 | 13 | 0 | 0 | Regras de critérios (RF05) |
| `DemoDataIntegrationTest` | 2 e 3 | 5 | 0 | 0 | Dados de demonstração e idempotência (estudos da T63 e de acompanhamento da T74) (3 da Sprint 2 e 2 da Sprint 3) |
| `DominioRecrutamentoTest` | 2 | 10 | 0 | 0 | Regras puras de domínio do recrutamento |
| `ElegibilidadeCombinacoesTest` | 2 | 11 | 0 | 0 | 8 combinações e casos-limite da regra de elegibilidade |
| `EstudoTransicaoCombinacoesServiceTest` | 2 | 16 | 0 | 0 | Combinações de pré-condições de transição |
| `EstudoTransicaoEndpointTest` | 2 | 4 | 0 | 0 | Endpoint de transição |
| `EstudoTransicaoIntegrationTest` | 2 | 4 | 0 | 0 | Histórico de estados |
| `EstudoTransicaoRf07Test` | 2 | 21 | 0 | 0 | Transições e bloqueios do RF07 |
| `EstudoTransicaoServiceTest` | 2 | 7 | 0 | 0 | Serviço de transição |
| `MaquinaEstadosEstudoTest` | 2 | 27 | 0 | 0 | Máquina de estados (RF07) |
| `MigracaoSeguraTest` | 2 | 2 | 0 | 0 | Migrations não destrutivas |
| `ParticipacaoConcorrenciaTest` | 2 | 3 | 0 | 0 | Concorrência na última vaga |
| `ParticipacaoEndpointTest` | 2 | 15 | 0 | 0 | Endpoints de participantes e vagas |
| `ParticipacaoServiceTest` | 2 | 8 | 0 | 0 | Pré-condições da inclusão (RF11) |
| `PlanejamentoIntegrationTest` | 2 | 8 | 0 | 0 | Planejamento: critérios, visitas e contrato de validação |
| `RecrutamentoIntegrationTest` | 2 | 11 | 0 | 0 | Fluxo de recrutamento ponta a ponta na API |
| `VisitaConsentimentoIntegrationTest` | 2 | 9 | 0 | 0 | Visitas e consentimento integrados |
| `VisitaEndpointTest` | 2 | 24 | 0 | 0 | Endpoints de visitas e atividades |
| `VisitaRequestValidacaoTest` | 2 | 19 | 0 | 0 | Limites de visita e atividade |
| `VisitaServiceTest` | 2 | 14 | 0 | 0 | Regras de visitas e atividades (RF06) |
| `ContratoOpenApiSprint3Test` | 3 | 14 | 0 | 2 | Contrato da Sprint 3: caminhos, erros, schemas iguais aos DTOs; 2 casos desligados até a T76 e a T96 |
| `DemoHistoricoParticipanteTest` | 3 | 3 | 0 | 0 | Script de demonstração do histórico do P4 (T95) |
| `HistoricoParticipanteDtoTest` | 3 | 6 | 0 | 0 | DTOs e resumo do histórico do participante (T95) |
| `ResultadoAtividadeRepositoryTest` | 3 | 5 | 0 | 0 | Entidade e repositório dos resultados das atividades (T87) |
| `VisitaAgendadaRepositoryTest` | 3 | 5 | 0 | 0 | Entidade, repositório, UNIQUE e controle de versão otimista da visita agendada (T73) |
| **Total (49 classes)** | | **478** | **0** | **2** | |

> `TokenDeTeste`, `CenarioRecrutamento` e `CenarioVisitaAgendada` (pasta `support/`) são apoio dos testes e não contam como casos.

### 4.2 Cobertura de código (JaCoCo)

A regra do projeto mede o **código de negócio** (pacotes `domain` e `service`). `config`, DTOs e a classe `main` ficam fora da medição.

| Pacote | Linhas cobertas | % linhas | Ramos cobertos | % ramos | Métodos |
|---|---|---|---|---|---|
| `controller` | 107 / 107 | 100,0% | 6 / 8 | 75,0% | 60 / 60 |
| `domain.enums` | 53 / 53 | 100,0% | 10 / 10 | 100,0% | 11 / 11 |
| `domain.model` | 33 / 33 | 100,0% | 17 / 20 | 85,0% | 16 / 16 |
| `domain.regra` | 39 / 39 | 100,0% | 39 / 40 | 97,5% | 11 / 11 |
| `security` | 62 / 64 | 96,9% | 4 / 8 | 50,0% | 16 / 16 |
| `service` | 496 / 501 | 99,0% | 172 / 182 | 94,5% | 107 / 109 |
| **Código de negócio (`domain` + `service`)** | **621 / 626** | **99,2%** | | | |
| **Backend inteiro (exceto main, config e DTOs)** | **790 / 797** | **99,1%** | | | |

**Meta do projeto: 80%. Resultado: 99,2%**, o mesmo valor da Release 1 (o código novo da Sprint 3, `VisitaAgendada`, `ResultadoAtividade` e os enums, está 100% coberto). As 7 linhas sem cobertura são as mesmas da Release 1: `UsuarioService` (5 linhas: sobrecarga `excluir(id)` sem solicitante, exclusão de administrador que não é o solicitante e corrida de cadastro com o mesmo e-mail) e `SecurityConfig` (2 linhas: falha de inicialização com `JWT_SECRET` ausente ou curto). Nenhuma delas é de risco alto.

---

## 5. Resultados dos testes de interface (web)

Não mudaram na Sprint 3 (nenhuma tela nova foi integrada até aqui). Detalhe e foco de cada arquivo estão nas seções 8.1 e 8.2 do [relatório da Release 1](relatorio-de-testes-release1.md#8-resultados-dos-testes-de-interface-web).

### 5.1 Vitest: 133 casos, 0 falhas

| Arquivo | Sprint | Casos |
|---|---|---|
| `pages/estudos/detalhe/EstudoDetalhePage.test.tsx` | 1 | 9 |
| `api/erros.test.ts` | 2 | 7 |
| `contexts/AuthContext.test.tsx` | 2 | 6 |
| `mocks/mocks.test.tsx` | 2 | 10 |
| `IndicadorEstado.test.tsx` | 2 | 8 |
| `AbaCriterios.test.tsx` | 2 | 20 |
| `AbaVisitas.test.tsx` | 2 | 17 |
| `AbaCandidatos.test.tsx` | 2 | 11 |
| `FichaCandidatoPage.test.tsx` | 2 | 12 |
| `SecaoConsentimento.test.tsx` | 2 | 12 |
| `SecaoInclusao.test.tsx` | 2 | 16 |
| `AbaParticipantes.test.tsx` | 2 | 5 |

### 5.2 Playwright (E2E): 29 casos, 0 falhas

| Arquivo | Sprint | Casos |
|---|---|---|
| `login.spec.ts` | 1 | 6 |
| `sessao.spec.ts` | 1 | 3 |
| `setup/ambiente.setup.ts`, `setup/auth.setup.ts` | 2 | 2 |
| `acesso-por-perfil.spec.ts` | 2 | 4 |
| `regressao-sprint1.spec.ts` | 2 | 4 |
| `regras-recrutamento.spec.ts` | 2 | 7 |
| `fluxo-completo.spec.ts` | 2 | 2 |
| `vagas-esgotadas.spec.ts` | 2 | 1 |

---

## 6. Rastreabilidade: requisitos × testes

### 6.1 Sprints 1 e 2 (RF01 a RF11): todos verificados

| RF | Requisito | Sprint | Backend (casos) | Web | E2E | Situação |
|---|---|---|---|---|---|---|
| RF01 | Cadastro de usuários | 1 | `UsuarioIntegrationTest` (10), `RegrasDeContaIntegrationTest` (8), `UsuarioDtoValidationTest` (4) | — | 1 | ✅ |
| RF02 | Autenticação e logout | 1 | `AuthenticationIntegrationTest` (13) | `AuthContext.test` (6) | 9 | ✅ |
| RF03 | Restrição por perfil | 1 | `PermissaoAcessoIntegrationTest` (15) | `EstudoDetalhePage.test` (9) | 4 | ✅ |
| RF04 | Cadastro de estudos | 1 | `EstudoIntegrationTest` (8), `EstudoServiceTest` (8), `PlanejamentoIntegrationTest` (8) | — | 3 | ✅ |
| RF05 | Critérios | 2 | `CriterioServiceTest` (13), `CriterioRequestValidacaoTest` (19), `CriterioEndpointTest` (19), `CriterioDescricaoRepetidaTest` (1) | `AbaCriterios.test` (20) | 1 | ✅ |
| RF06 | Visitas e atividades | 2 | `VisitaServiceTest` (14), `VisitaRequestValidacaoTest` (19), `VisitaEndpointTest` (24) | `AbaVisitas.test` (17) | 1 | ✅ |
| RF07 | Estados do estudo | 2 | `MaquinaEstadosEstudoTest` (27), `EstudoTransicaoRf07Test` (21), `EstudoTransicaoCombinacoesServiceTest` (16), `EstudoTransicaoServiceTest` (7), `EstudoTransicaoEndpointTest` (4), `EstudoTransicaoIntegrationTest` (4) | `IndicadorEstado.test` (8) | 1 | ✅ |
| RF08 | Candidatos | 2 | `CandidatoServiceTest` (9), `CandidatoIntegrationTest` (14) | `AbaCandidatos.test` (11), `FichaCandidatoPage.test` (12) | 2 | ✅ |
| RF09 | Elegibilidade | 2 | `AvaliacaoServiceTest` (8), `AvaliacaoHistoricoTest` (1), `AvaliacaoRequestValidacaoTest` (7), `AvaliacaoEndpointTest` (17), `ElegibilidadeCombinacoesTest` (11), `DominioRecrutamentoTest` (10) | `FichaCandidatoPage.test` (12) | 2 | ✅ |
| RF10 | Consentimento | 2 | `ConsentimentoServiceTest` (15), `ConsentimentoEndpointTest` (15), `VisitaConsentimentoIntegrationTest` (9) | `SecaoConsentimento.test` (12) | 2 | ✅ |
| RF11 | Inclusão e vagas | 2 | `ParticipacaoServiceTest` (8), `ParticipacaoEndpointTest` (15), `ParticipacaoConcorrenciaTest` (3), `RecrutamentoIntegrationTest` (11) | `SecaoInclusao.test` (16), `AbaParticipantes.test` (5) | 2 | ✅ |

### 6.2 Sprint 3 (RF12 a RF15): situação dos testes por task

| Task | PBI · RF | O que a task entrega | Testes existentes | Situação |
|---|---|---|---|---|
| T73 | 19 · RF12 | Modelo e migration da visita agendada | `VisitaAgendadaRepositoryTest` (5), `FlywayMigrationIntegrationTest` (+5: tabela, UNIQUE, CHECKs, ausência de cascata) | ✅ Testado na `develop` |
| T74 | 19 · RF12 | Contrato OpenAPI do agendamento e demo | `ContratoOpenApiSprint3Test` (14, 2 ignorados), `DemoDataIntegrationTest` (+2) | ✅ Testado; 1 caso depende da T76 |
| T75 | 19 · RF12 | Regras de agendamento | `AgendamentoVisitaServiceTest` (10), no **PR #12** | ❌ **1 erro** (seção 8, D-S3-01) |
| T87 | 21 · RF14 | Modelo e migration dos resultados | `ResultadoAtividadeRepositoryTest` (5), `FlywayMigrationIntegrationTest` (+4) | ✅ Testado na `develop` |
| T95 | 22 · RF15 | Contrato e modelo de leitura do histórico | `HistoricoParticipanteDtoTest` (6), `DemoHistoricoParticipanteTest` (3), `ContratoOpenApiSprint3Test` | ✅ Testado; o caso da API depende da T96 |
| T76, T78, T79 | 19 · RF12 | Endpoints, testes de backend e E2E do agendamento | — | ⬜ A implementar |
| T80 a T86 | 20 · RF13 | Desfecho da visita | — | ⬜ A implementar |
| T88 a T93 | 21 · RF14 | Resultados (contrato, regras, endpoints, tela, testes) | — | ⬜ A implementar |
| T96 a T100 | 22 · RF15 | Consulta, tela, testes e E2E do histórico | — | ⬜ A implementar |
| T101 a T121 | 23 a 25 · RF16 a RF18 | Retirada, app do participante | — | ⬜ A implementar |

Os 2 casos ignorados do `ContratoOpenApiSprint3Test` (`@Disabled("T76")` e `@Disabled("T96")`) são **intencionais**: eles provam que o contrato foi escrito antes do código e devem ser ligados quando os endpoints existirem. Se continuarem desligados depois da T76 e da T96, a rastreabilidade do contrato fica quebrada.

---

## 7. Roteiros de sistema pela API

Sem alteração desde a Release 1: roteiro do recrutamento (T68), 58 de 58 verificações aprovadas, e roteiro do planejamento (T67), executado no job Docker do CI. Ver seções 9.1 e 9.2 do [relatório da Release 1](relatorio-de-testes-release1.md).

---

## 8. Defeitos

O registro completo, com identificador, origem, causa, evidência e situação, está em [registro-de-defeitos.md](registro-de-defeitos.md). Resumo:

| ID | Descrição | Onde | Severidade | Situação |
|---|---|---|---|---|
| D-S3-01 | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela` falha: o teste agenda uma data passada e a regra (correta, conforme o design) a recusa | Pull Request #12 (T75), branch `task/t75/domain` | Média (bloqueia o merge) | **Aberto** |
| OBS-12-01 a OBS-12-04 | Quatro pontos de revisão do mesmo PR (entidade e códigos de erro diferentes do design) | Pull Request #12 | A confirmar | **Aberto** |
| B-T67-01 a B-AUD-01 | Seis defeitos da Release 1 | Sprints 1 e 2 | — | Todos corrigidos |

Nenhum defeito aberto na `develop`.

## 9. Riscos, limitações e recomendações

| # | Risco ou limitação | Impacto | Recomendação |
|---|---|---|---|
| R1 | Os testes foram rodados no H2; o job `Backend (testes no PostgreSQL)` do CI não foi executado aqui. O SQL da Sprint 3 usa `INTERVAL '35' DAY` e `CHECK` com `OR` | Médio | Conferir o job verde antes de promover a `develop` para a `main` |
| R2 | O PR #12 não pode entrar com o teste falhando (o CI também falharia) | Alto para o PBI-19 | Corrigir o teste, que contradiz a regra do design (ver D-S3-01) |
| R3 | O aplicativo mobile não tem teste automatizado | Médio, até a T113 e a T114 | Cumprir as tasks previstas |
| R4 | Dois casos de contrato desligados | Baixo | Ligar na T76 e na T96 |
| R5 | Os testes de web e de E2E ainda não cobrem nada da Sprint 3 | Baixo agora | As tasks T79, T86, T93, T97 e T99 trazem os casos |
| R6 | O PR #12 mudou a entidade `VisitaAgendada` (OBS-12-01 a 03) | Médio | Revisar antes do merge |

## 10. Conclusão

Na `develop`, as três sprints têm **640 casos automatizados e nenhuma falha**, e a cobertura do código de negócio está em **99,2%**, acima da meta de 80%. Os requisitos RF01 a RF11 estão verificados; o início da Sprint 3 (T73, T74, T87 e T95) está testado. O que impede dar a Sprint 3 como verificada é o que falta implementar (T75 a T121) e o **erro do Pull Request #12**, que deve ser resolvido antes do merge.

## 11. Como reproduzir

```bash
# Backend: testes, cobertura e bloqueio de 80% (no diretório backend/ClinicalStudyPB)
./mvnw -B verify
#   relatório: target/site/jacoco/index.html
#   resultados: target/surefire-reports/

# Web: testes, lint, tipos e build (no diretório web/ClinicalStudyPB)
npm test
npm run lint
npx tsc -b

# E2E (backend em http://localhost:8081, web em http://localhost:5173 e .env.e2e criado a partir do .env.e2e.example)
npm run test:e2e
```

Para testar um Pull Request antes do merge, crie a branch dele com `git fetch origin pull/NUMERO/head:pr-NUMERO` e rode `./mvnw -B verify` nela. O Pull Request #12 foi reproduzido assim, e também sobre o resultado do merge com a `develop` (`pull/12/merge`).
