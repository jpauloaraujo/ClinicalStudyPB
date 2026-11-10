# Relatório de Testes — ClinicalStudyPB, Release 1

| | |
|---|---|
| **Projeto** | ClinicalStudyPB — Sistema de Gestão de Estudos Clínicos (Engenharia de Software II, UEPB, 2026.2) |
| **Versão testada** | Release 1 (Sprints 1 e 2), ramo `develop` com as correções `fix/auth-context` e `fix/auditoria-design-sprint2` |
| **Tipo do documento** | Relatório de execução de testes (estrutura inspirada na IEEE 829 / ISO 29119-3) |
| **Data da execução** | 09/11/2026 |
| **Escopo funcional** | RF01 a RF11 (usuários, autenticação, perfis, estudos, critérios, visitas, estados, candidatos, elegibilidade, consentimento e inclusão de participantes) |
| **Resultado geral** | **Aprovado.** 434 testes de backend, 133 de web e 29 de E2E executados, **0 falhas**, cobertura de linhas do código de negócio de **99,2%** (meta: 80%) |

---

## 1. Introdução

### 1.1 Objetivo

Documentar o planejamento, a execução e os resultados dos testes da Release 1, demonstrando com evidências que os requisitos RF01 a RF11 foram implementados e verificados, quais riscos permanecem e se a versão está apta para a entrega.

### 1.2 Escopo

**Dentro do escopo:** API REST (Spring Boot), aplicação web (React) e a integração entre elas, do banco de dados às telas; regras de negócio, validações, permissões por perfil, códigos de erro, concorrência na inclusão de participantes, contrato OpenAPI, migrations e dados de demonstração.

**Fora do escopo:** funcionalidades das Releases 2 e 3 (RF12 a RF22); testes automatizados do aplicativo mobile (na Release 1 ele só tem o login e passa por lint e verificação de tipos); testes de carga, de desempenho e de penetração; testes de acessibilidade automatizados; teste em navegadores além do Chromium.

### 1.3 Referências

| Documento | Local |
|---|---|
| Especificação do projeto (RF01 a RF22) | `docs/sprint1/especificacao-projeto.md` |
| Planejamento da Sprint 2 e backlog | `docs/sprint2/Planejamento_da_Sprint_2_ClinicalStudyPB.md`, `docs/azure-boards/sprint2-backlog.csv` |
| Documentos de design por task | `docs/design/sprint1/` e `docs/design/sprint2/` |
| Contrato da API | `docs/api/openapi-sprint2.yaml` |
| Diagramas (domínio, casos de uso, sequências, arquitetura) | `docs/diagramas/` |
| Relatório da release | `releases/release1/README.md` |
| Roteiros de integração | `docs/sprint2/T67 — ...`, `docs/sprint2/T68 — ...` |

### 1.4 Definições

| Termo | Significado |
|---|---|
| RF | Requisito funcional |
| MockMvc | Cliente que chama os controllers do Spring sem abrir uma porta de rede, com toda a pilha (segurança, validação, JPA e Flyway) ativa |
| H2 / PostgreSQL | Banco em memória usado nos testes locais, em modo de compatibilidade PostgreSQL / banco de produção |
| MSW | Mock Service Worker, que intercepta chamadas HTTP no web |
| E2E | Teste de ponta a ponta: navegador real, aplicação web real, API real e banco real |
| Caixa-preta | Teste que valida o comportamento observável, sem olhar o código |

---

## 2. Estratégia de testes

### 2.1 Níveis de teste

| Nível | Objetivo | Ferramentas | Onde |
|---|---|---|---|
| **Unidade (domínio e serviços)** | Regras de negócio isoladas de banco e de HTTP, com dependências simuladas | JUnit 5, Mockito, AssertJ | `*ServiceTest`, `*RegraTest`, `MaquinaEstadosEstudoTest`, `DominioRecrutamentoTest`, `ElegibilidadeCombinacoesTest` |
| **Validação de DTOs** | Limites de tamanho, campos obrigatórios e formatos, sem Spring | Jakarta Validation | `*RequestValidacaoTest`, `UsuarioDtoValidationTest` |
| **Integração / API (endpoints)** | Controller + segurança + serviço + JPA + Flyway, com tokens JWT reais por perfil | Spring Boot Test, MockMvc, H2 | `*EndpointTest`, `*IntegrationTest` |
| **Concorrência** | Provar que o limite de vagas nunca é ultrapassado com requisições simultâneas | `ExecutorService`, `CountDownLatch`, bloqueio pessimista | `ParticipacaoConcorrenciaTest` |
| **Contrato e persistência** | API igual ao OpenAPI; migrations aplicáveis e seguras; dados de demonstração idempotentes | springdoc, Flyway | `ContratoOpenApiTest`, `FlywayMigrationIntegrationTest`, `MigracaoSeguraTest`, `DemoDataIntegrationTest` |
| **Componente (web)** | Telas e hooks isolados, com a API simulada | Vitest, Testing Library, MSW | `web/ClinicalStudyPB/src/**/*.test.ts(x)` |
| **Ponta a ponta (E2E)** | Fluxos reais de cada perfil no navegador | Playwright (Chromium) | `web/ClinicalStudyPB/e2e/` |
| **Roteiros de sistema (API)** | Fluxo completo e regressão contra o ambiente no ar, inclusive Docker | Python 3 (biblioteca padrão) | `scripts/verificar-ambiente.py`, `scripts/verificar-recrutamento.py` |

### 2.2 Técnicas de projeto de casos de teste

| Técnica | Onde foi aplicada |
|---|---|
| **Partição de equivalência** | Perfis de usuário (administrador, pesquisador, coordenador, participante), situações do candidato, estados do estudo |
| **Análise de valor-limite** | Descrição de critério (4, 5, 500 e 501 caracteres), observação da avaliação (500 e 501), versão do termo (60 e 61), tamanho de página, última vaga (limite exato), data de consentimento (hoje e futura) |
| **Tabela de decisão / combinatória** | As 8 combinações da regra de elegibilidade (`ElegibilidadeCombinacoesTest`); as combinações de transição de estado (`EstudoTransicaoCombinacoesServiceTest`, 16 casos) |
| **Teste de transição de estados** | Máquina de estados do estudo: planejamento → recrutamento → execução → encerrado, com todas as transições inválidas (`MaquinaEstadosEstudoTest`, 27 casos) |
| **Teste de permissões (RBAC)** | Matriz perfil × operação em cada endpoint: 401, 403 e sucesso |
| **Teste de regras de negócio (um caso por código de erro)** | Cada `codigoRegra` 409 tem ao menos um teste que o provoca e confere o código |
| **Teste de regressão** | Login, usuários e estudos da Sprint 1 reexecutados a cada release (E2E e roteiro T67) |
| **Teste de mutação manual** | Remoção temporária do bloqueio de vagas para provar que o teste de concorrência detecta a falha (seção 6.3) |

### 2.3 Critérios de entrada e saída

| Critério | Definição | Situação |
|---|---|---|
| **Entrada** | Código compilando, migrations aplicáveis, contrato OpenAPI publicado | ✅ Atendido |
| **Saída — funcional** | 0 falhas em todas as suítes | ✅ Atendido |
| **Saída — cobertura** | ≥ 80% de linhas no código de negócio (domínio e serviços), bloqueando o build | ✅ 99,2% |
| **Saída — requisitos** | Cada RF da release com testes de sucesso, de erro e de permissão | ✅ Seção 7 |
| **Saída — regressão** | Fluxos da Sprint 1 passando | ✅ E2E e roteiro T67 |

---

## 3. Ambiente de teste

| Item | Valor |
|---|---|
| Sistema operacional | Windows 11 Pro |
| Java / Maven | OpenJDK 21.0.5 / Maven 3.10.0 (wrapper) |
| Backend | Spring Boot 4.1.1, Spring Security (JWT HS256), Spring Data JPA, Flyway 12.4.0 |
| Banco nos testes locais | H2 em memória, modo PostgreSQL, com as mesmas migrations (V1 a V5) e `ddl-auto=validate` |
| Banco de produção / CI | PostgreSQL 16 (job `Backend (PostgreSQL)` e Docker Compose) |
| Cobertura | JaCoCo 0.8.14 |
| Web | Node 24 (CI usa Node 22), React 19.3, TypeScript 6, Vite 8, Vitest 5, Testing Library, MSW 3 |
| E2E | Playwright 1.64.1, Chromium, locale pt-BR, backend e web reais |
| Roteiros | Python 3.12 |
| Dados | Exclusivamente fictícios; usuários do seed V2 e do perfil `demo` (senha `senha-de-teste`) |
| Integração contínua | GitHub Actions (`.github/workflows/ci.yml`), 6 jobs |

**Isolamento dos dados:** os testes de API usam `@Transactional` (cada teste reverte seus dados); o teste de concorrência, que precisa de transações confirmadas, apaga os próprios dados no `@AfterEach`; os testes E2E e os roteiros criam dados com sufixo único por execução e por isso podem ser repetidos no mesmo banco.

---

## 4. Resumo dos resultados

| Suíte | Executados | Passaram | Falharam | Ignorados | Tempo |
|---|---|---|---|---|---|
| Backend (JUnit) | **434** | 434 | 0 | 0 | ≈ 50 s (somados) |
| Web (Vitest) | **133** | 133 | 0 | 0 | ≈ 12 s |
| E2E (Playwright) | **29** (27 especificações + 2 de preparação) | 29 | 0 | 0 | ≈ 14 s |
| Roteiro de recrutamento (T68) | **58** verificações | 58 | 0 | — | — |
| Roteiro de planejamento (T67) | verificações de ambiente, RF01–RF04 e RF05–RF07 | executado no job Docker do CI | — | — | — |
| Lint e build do web | `npm run lint`, `tsc -b`, `npm run build` | sem erros | — | — | — |
| **Total de casos automatizados** | **596** (434 + 133 + 29) | **596** | **0** | **0** | |

> O roteiro T67 e o job Docker precisam do Docker Compose; eles não rodaram na máquina de desenvolvimento e são validados pelo CI (ver riscos, seção 11).

### 4.1 Distribuição por nível

| Nível | Casos | % dos 596 |
|---|---|---|
| Unidade (domínio e serviços) e validação de DTOs | 196 | 33% |
| Integração / API (MockMvc, segurança, JPA e Flyway) | 216 | 36% |
| Concorrência, contrato, persistência e infraestrutura | 22 | 4% |
| Componente web | 133 | 22% |
| E2E | 29 | 5% |

Os três primeiros níveis somam os 434 casos do backend (196 + 216 + 22). A pirâmide de testes está respeitada: a base (unidade, integração e componente) concentra 91% dos casos e o topo (E2E) 5%.

---

## 5. Resultados detalhados do backend

### 5.1 Por classe de teste

| Classe | Casos | Falhas | Foco |
|---|---|---|---|
| `AuthenticationIntegrationTest` | 13 | 0 | Login válido e inválido, token, `/me`, expiração |
| `UsuarioIntegrationTest` | 10 | 0 | CRUD de usuários (RF01) |
| `RegrasDeContaIntegrationTest` | 8 | 0 | Regras do último administrador e da própria conta |
| `UsuarioDtoValidationTest` | 4 | 0 | Validação do cadastro de usuário |
| `PermissaoAcessoIntegrationTest` | 15 | 0 | Matriz de permissões por perfil (RF03) |
| `EstudoIntegrationTest` | 8 | 0 | CRUD de estudos (RF04) |
| `EstudoServiceTest` | 8 | 0 | Regras do estudo |
| `PlanejamentoIntegrationTest` | 8 | 0 | Planejamento: critérios, visitas e contrato de validação |
| `CriterioServiceTest` | 13 | 0 | Regras de critérios (RF05) |
| `CriterioRequestValidacaoTest` | 19 | 0 | Limites do critério |
| `CriterioEndpointTest` | 19 | 0 | Endpoints de critérios |
| `CriterioDescricaoRepetidaTest` | 1 | 0 | Descrição repetida |
| `VisitaServiceTest` | 14 | 0 | Regras de visitas e atividades (RF06) |
| `VisitaRequestValidacaoTest` | 19 | 0 | Limites de visita e atividade |
| `VisitaEndpointTest` | 24 | 0 | Endpoints de visitas e atividades |
| `MaquinaEstadosEstudoTest` | 27 | 0 | Máquina de estados (RF07) |
| `EstudoTransicaoRf07Test` | 21 | 0 | Transições e bloqueios do RF07 |
| `EstudoTransicaoCombinacoesServiceTest` | 16 | 0 | Combinações de pré-condições de transição |
| `EstudoTransicaoServiceTest` | 7 | 0 | Serviço de transição |
| `EstudoTransicaoEndpointTest` | 4 | 0 | Endpoint de transição |
| `EstudoTransicaoIntegrationTest` | 4 | 0 | Histórico de estados |
| `CandidatoServiceTest` | 9 | 0 | Regras de candidatos (RF08) |
| `CandidatoIntegrationTest` | 14 | 0 | Endpoints, paginação e filtro de candidatos |
| `AvaliacaoServiceTest` | 8 | 0 | Serviço de avaliação (RF09) |
| `AvaliacaoHistoricoTest` | 1 | 0 | Histórico com critérios carregados do banco |
| `AvaliacaoRequestValidacaoTest` | 7 | 0 | Validação da avaliação |
| `AvaliacaoEndpointTest` | 17 | 0 | Endpoints de avaliação |
| `ElegibilidadeCombinacoesTest` | 11 | 0 | 8 combinações + casos-limite da regra |
| `DominioRecrutamentoTest` | 10 | 0 | Regras puras de domínio |
| `ConsentimentoServiceTest` | 15 | 0 | Serviço de consentimento (RF10) |
| `ConsentimentoEndpointTest` | 15 | 0 | Endpoints de consentimento |
| `VisitaConsentimentoIntegrationTest` | 9 | 0 | Visitas e consentimento integrados |
| `ParticipacaoServiceTest` | 8 | 0 | Pré-condições da inclusão (RF11) |
| `ParticipacaoEndpointTest` | 15 | 0 | Endpoints de participantes e vagas |
| `ParticipacaoConcorrenciaTest` | 3 | 0 | Concorrência na última vaga |
| `RecrutamentoIntegrationTest` | 11 | 0 | Fluxo de recrutamento ponta a ponta na API |
| `ContratoOpenApiTest` | 3 | 0 | API igual ao contrato OpenAPI |
| `OpenApiDocumentationIntegrationTest` | 2 | 0 | Swagger UI e `/v3/api-docs` |
| `FlywayMigrationIntegrationTest` | 3 | 0 | Migrations aplicam e o schema bate com as entidades |
| `MigracaoSeguraTest` | 2 | 0 | Migrations não destrutivas |
| `DemoDataIntegrationTest` | 3 | 0 | Dados de demonstração e idempotência |
| `CorsIntegrationTest` / `CorsOrigensConfiguradasIntegrationTest` | 3 / 2 | 0 | CORS padrão e configurável |
| `ClinicalStudyPbApplicationTests` | 1 | 0 | O contexto da aplicação sobe |
| **Total (44 classes)** | **434** | **0** | |

### 5.2 Cobertura de código (JaCoCo)

A regra do projeto mede o **código de negócio** (pacotes `domain` e `service`). `config`, DTOs e a classe `main` são excluídos da medição.

| Pacote | Linhas cobertas | % linhas | Ramos cobertos | % ramos | Métodos |
|---|---|---|---|---|---|
| `controller` | 107 / 107 | 100,0% | 6 / 8 | 75,0% | 60 / 60 |
| `domain.enums` | 34 / 34 | 100,0% | — | — | 7 / 7 |
| `domain.model` | 15 / 15 | 100,0% | 14 / 14 | 100,0% | 10 / 10 |
| `domain.regra` | 39 / 39 | 100,0% | 40 / 40 | 100,0% | 11 / 11 |
| `security` | 62 / 64 | 96,9% | 4 / 8 | 50,0% | 16 / 16 |
| `service` | 496 / 501 | 99,0% | 172 / 182 | 94,5% | 107 / 109 |
| **Código de negócio (`domain` + `service`)** | **584 / 589** | **99,2%** | | | |
| **Backend inteiro (exceto main, config e DTOs)** | **753 / 760** | **99,1%** | | | |

**Meta do projeto: 80%. Resultado: 99,2%** (bloqueante: `./mvnw -B verify -Djacoco.haltOnFailure=true` falha o build abaixo disso, e o CI roda com o bloqueio ligado).

**Linhas ainda não cobertas (7 no total):**

| Classe | Linhas | O que é | Risco |
|---|---|---|---|
| `UsuarioService` | 104–105 | Sobrecarga `excluir(id)` sem informar o solicitante (não é chamada pela API, que sempre informa) | Nenhum (código de conveniência) |
| `UsuarioService` | 113 | Excluir um administrador ativo **que não é o próprio solicitante** e verificar se sobra outro administrador | Baixo; a regra da própria conta e a do último administrador têm teste, mas não essa combinação |
| `UsuarioService` | 137–138 | Tratamento de `DataIntegrityViolationException` no cadastro simultâneo com o mesmo e-mail | Baixo; o caso sequencial (e-mail duplicado) é testado |
| `SecurityConfig` | 139 e 143 | Falha de inicialização quando `JWT_SECRET` está ausente ou tem menos de 32 bytes (`security` está fora da regra dos 80%) | Baixo; é uma proteção de configuração |

**Ramos (branches):** `controller` e `security` têm só 8 ramos cada (75% e 50%), então a porcentagem varia muito com um ramo; as classes de regra de negócio estão entre 94,5% e 100%. Em `service`, 10 dos 182 ramos não foram exercitados.

---

## 6. Testes específicos e evidências

### 6.1 Regra de elegibilidade (RF09): tabela de decisão

Inclusão determinante (peso 2), inclusão desejável (peso 3) e uma exclusão; as 8 combinações de respostas ("se aplica" = S) e o resultado esperado, todas executadas em `ElegibilidadeCombinacoesTest`:

| Obrigatório | Desejável | Exclusão | Elegível | Pontos | Falhas | Resultado |
|---|---|---|---|---|---|---|
| S | S | não | **sim** | 5 | 0 | ✅ |
| S | não | não | **sim** | 2 | 0 | ✅ |
| não | S | não | não | 3 | 1 | ✅ |
| não | não | não | não | 0 | 1 | ✅ |
| S | S | S | não | 5 | 1 | ✅ |
| S | não | S | não | 2 | 1 | ✅ |
| não | S | S | não | 3 | 2 | ✅ |
| não | não | S | não | 0 | 2 | ✅ |

### 6.2 Máquina de estados do estudo (RF07)

| De \ Para | Planejamento | Recrutamento | Execução | Encerrado |
|---|---|---|---|---|
| **Planejamento** | — | ✅ permitida (exige 1 critério de inclusão e 1 visita) | ❌ `TRANSICAO_ESTADO_INVALIDA` | ❌ `TRANSICAO_ESTADO_INVALIDA` |
| **Recrutamento** | ❌ | — | ✅ permitida | ❌ |
| **Execução** | ❌ | ❌ | — | ✅ permitida |
| **Encerrado** | ❌ | ❌ | ❌ | — (final) |

Bloqueios adicionais testados: sem critério de inclusão (`ESTUDO_SEM_CRITERIO_INCLUSAO`), sem visita (`ESTUDO_SEM_VISITA_PREVISTA`), `PUT` com estado diferente (`ESTADO_ALTERADO_FORA_DA_TRANSICAO`), criação em estado diferente de planejamento (`ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO`), e o histórico de estados registrando usuário e data.

### 6.3 Concorrência na inclusão de participantes (RF11)

| Cenário | Entrada | Resultado esperado | Resultado obtido |
|---|---|---|---|
| Disputa pela última vaga | 6 candidatos prontos, 1 vaga, 6 requisições simultâneas | exatamente 1 × `201` e 5 × `409 VAGAS_ESGOTADAS`; `incluidos = 1`; 1 candidato `INCLUIDO` | ✅ conforme |
| Mesmo candidato repetido | 4 requisições simultâneas para o mesmo candidato | 1 × `201` e 3 × `409 CANDIDATO_JA_INCLUIDO`; 1 participação | ✅ conforme |
| Sem disputa | 4 candidatos diferentes, 5 vagas | 4 × `201`; `restantes = 1` | ✅ conforme |

**Prova de que o teste detecta a falha (mutação manual):** trocando temporariamente `estudoAcesso.buscarComBloqueio(...)` por `estudoAcesso.buscar(...)` em `ParticipacaoService`, o teste **falhou** (mais participantes que o limite); com o bloqueio restaurado, o teste **passou em 3 execuções seguidas**. A alteração temporária foi descartada com `git checkout`.

### 6.4 Matriz de permissões por perfil (RF03)

Verificada nos testes de endpoint de cada recurso (`*EndpointTest`, `PermissaoAcessoIntegrationTest`) e no E2E de acesso por perfil:

| Operação | Administrador | Pesquisador | Coordenador | Participante | Sem token |
|---|---|---|---|---|---|
| Usuários (RF01) | ✅ | 403 | 403 | 403 | 401 |
| Estudos: criar/editar/excluir (RF04) | 403 | ✅ | 403 | 403 | 401 |
| Estudos: consultar | 403 | ✅ | ✅ | 403 | 401 |
| Critérios, visitas e atividades: escrever (RF05, RF06) | 403 | ✅ | 403 | 403 | 401 |
| Critérios, visitas e atividades: ler | 403 | ✅ | ✅ | 403 | 401 |
| Avançar estado do estudo (RF07) | 403 | ✅ | 403 | 403 | 401 |
| Candidatos, avaliação, consentimento, inclusão: escrever (RF08–RF11) | 403 | 403 | ✅ | 403 | 401 |
| Candidatos, avaliação, consentimento, participantes, vagas: ler | 403 | ✅ | ✅ | 403 | 401 |

### 6.5 Códigos de erro de negócio

Cada código de erro 409 tem ao menos um teste que o provoca e confere o `codigoRegra`:

| Código | Regra | Testes que o cobrem (exemplos) |
|---|---|---|
| `ESTUDO_FORA_DE_RECRUTAMENTO` | A operação exige o estudo em recrutamento | Candidatos, avaliação, consentimento, inclusão; roteiros T67 e T68; E2E |
| `ESTUDO_FORA_DE_PLANEJAMENTO` | Critérios e visitas só em planejamento | `CriterioEndpointTest`, `VisitaEndpointTest` |
| `ESTUDO_SEM_CRITERIO_INCLUSAO` / `ESTUDO_SEM_VISITA_PREVISTA` | Pré-condições para sair do planejamento | `EstudoTransicaoRf07Test`, E2E de regras |
| `TRANSICAO_ESTADO_INVALIDA` | Estado inexistente ou fora da ordem | `MaquinaEstadosEstudoTest` |
| `ESTADO_ALTERADO_FORA_DA_TRANSICAO` | `PUT` não muda estado | `EstudoServiceTest`, `EstudoTransicaoEndpointTest` |
| `ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO` | Estudo novo nasce em planejamento | `EstudoServiceTest` |
| `ESTUDO_COM_CANDIDATOS` | Estudo com candidatos não é excluído | `CandidatoIntegrationTest`, `RecrutamentoIntegrationTest` |
| `CRITERIO_DUPLICADO` | Descrição repetida no mesmo tipo e estudo | `CriterioDescricaoRepetidaTest`, `CriterioEndpointTest` |
| `CRITERIO_EM_USO` | Critério usado em avaliação não é excluído | `CriterioServiceTest` |
| `VISITA_DIA_DUPLICADO` | Dois registros no mesmo dia | `VisitaEndpointTest`, roteiro T67 |
| `CANDIDATO_DUPLICADO` | Mesmo nome e data de nascimento no estudo | `CandidatoServiceTest`, E2E, roteiro T68 |
| `CANDIDATO_JA_INCLUIDO` | Já é participante | Avaliação, consentimento, inclusão, concorrência |
| `CANDIDATO_NAO_ELEGIVEL` | Sem avaliação vigente aprovada | `ParticipacaoServiceTest`, `ParticipacaoEndpointTest` |
| `CONSENTIMENTO_AUSENTE` / `CONSENTIMENTO_DUPLICADO` | Sem consentimento / segundo registro | `ConsentimentoEndpointTest`, `ParticipacaoEndpointTest` |
| `VAGAS_ESGOTADAS` | Limite do estudo atingido | `ParticipacaoServiceTest`, `ParticipacaoEndpointTest`, concorrência, E2E |
| `ACESSO_NEGADO`, `RECURSO_NAO_ENCONTRADO`, `VALIDACAO_CAMPOS` | 403, 404 e 400 com erro por campo | Todos os `*EndpointTest` |

**Lacuna conhecida:** os códigos genéricos `CORPO_INVALIDO`, `CREDENCIAIS_INVALIDAS`, `DADOS_INVALIDOS` e `PARAMETRO_INVALIDO` são verificados pelo status HTTP (400/401), mas nenhum teste confere o texto do código. O risco é baixo (são respostas genéricas), e a melhoria é uma asserção a mais.

---

## 7. Rastreabilidade: requisitos × testes

| RF | Requisito | Backend (casos) | Web (casos) | E2E | Situação |
|---|---|---|---|---|---|
| RF01 | Cadastro de usuários | `UsuarioIntegrationTest` (10), `RegrasDeContaIntegrationTest` (8), `UsuarioDtoValidationTest` (4) | — | 1 (regressão) | ✅ Verificado |
| RF02 | Autenticação e logout | `AuthenticationIntegrationTest` (13) | `AuthContext.test` (6) | 6 (login, logout, sessão) + 3 (sessão reaproveitada) | ✅ Verificado |
| RF03 | Restrição por perfil | `PermissaoAcessoIntegrationTest` (15) e a matriz em cada endpoint | `EstudoDetalhePage.test` (9) | 4 (acesso por perfil) | ✅ Verificado |
| RF04 | Cadastro de estudos | `EstudoIntegrationTest` (8), `EstudoServiceTest` (8), `PlanejamentoIntegrationTest` (8) | — | 3 (regressão) | ✅ Verificado |
| RF05 | Critérios de inclusão e exclusão | `CriterioServiceTest` (13), `CriterioRequestValidacaoTest` (19), `CriterioEndpointTest` (19), `CriterioDescricaoRepetidaTest` (1) | `AbaCriterios.test` (20) | 1 (planejamento) | ✅ Verificado |
| RF06 | Visitas e atividades | `VisitaServiceTest` (14), `VisitaRequestValidacaoTest` (19), `VisitaEndpointTest` (24) | `AbaVisitas.test` (17) | 1 (planejamento) | ✅ Verificado |
| RF07 | Estados do estudo | `MaquinaEstadosEstudoTest` (27), `EstudoTransicaoRf07Test` (21), `EstudoTransicaoCombinacoesServiceTest` (16), `EstudoTransicaoServiceTest` (7), `EstudoTransicaoEndpointTest` (4), `EstudoTransicaoIntegrationTest` (4) | `IndicadorEstado.test` (8) | 1 | ✅ Verificado |
| RF08 | Cadastro de candidatos | `CandidatoServiceTest` (9), `CandidatoIntegrationTest` (14) | `AbaCandidatos.test` (11), `FichaCandidatoPage.test` (12) | 2 | ✅ Verificado |
| RF09 | Elegibilidade | `AvaliacaoServiceTest` (8), `AvaliacaoHistoricoTest` (1), `AvaliacaoRequestValidacaoTest` (7), `AvaliacaoEndpointTest` (17), `ElegibilidadeCombinacoesTest` (11), `DominioRecrutamentoTest` (10) | `FichaCandidatoPage.test` (12) | 2 | ✅ Verificado |
| RF10 | Consentimento | `ConsentimentoServiceTest` (15), `ConsentimentoEndpointTest` (15), `VisitaConsentimentoIntegrationTest` (9) | `SecaoConsentimento.test` (12) | 2 | ✅ Verificado |
| RF11 | Inclusão e vagas | `ParticipacaoServiceTest` (8), `ParticipacaoEndpointTest` (15), `ParticipacaoConcorrenciaTest` (3), `RecrutamentoIntegrationTest` (11) | `SecaoInclusao.test` (16), `AbaParticipantes.test` (5) | 2 (fluxo completo e vagas esgotadas) | ✅ Verificado |
| — | Contrato, migrations e dados de demonstração | `ContratoOpenApiTest` (3), `OpenApiDocumentationIntegrationTest` (2), `FlywayMigrationIntegrationTest` (3), `MigracaoSeguraTest` (2), `DemoDataIntegrationTest` (3), `Cors*` (5) | `erros.test` (7), `mocks.test` (10) | — | ✅ Verificado |

Nenhum requisito do escopo ficou sem teste de sucesso, de erro e de permissão.

---

## 8. Resultados dos testes de interface (web)

### 8.1 Testes de componente (Vitest)

| Arquivo | Casos | Foco |
|---|---|---|
| `api/erros.test.ts` | 7 | Conversão do erro da API em mensagem e erros por campo |
| `contexts/AuthContext.test.tsx` | 6 | Login, logout, participante recusado, credenciais inválidas |
| `mocks/mocks.test.tsx` | 10 | As telas funcionam sobre o MSW sem alteração de código |
| `EstudoDetalhePage.test.tsx` | 9 | Abas por perfil e acesso negado |
| `IndicadorEstado.test.tsx` | 8 | Indicador e avanço de estado, mensagens de bloqueio |
| `AbaCriterios.test.tsx` | 20 | Lista, cadastro, edição, exclusão, vazios e erros |
| `AbaVisitas.test.tsx` | 17 | Visitas e atividades |
| `AbaCandidatos.test.tsx` | 11 | Lista paginada, filtro e cadastro |
| `FichaCandidatoPage.test.tsx` | 12 | Ficha, avaliação, resultado e histórico |
| `SecaoConsentimento.test.tsx` | 12 | Termo, confirmação, comprovante, erros e perfis |
| `SecaoInclusao.test.tsx` | 16 | Quatro pré-condições, confirmação, cinco códigos de erro |
| `AbaParticipantes.test.tsx` | 5 | Lista, contador "X de N vagas", vazio, erro e carregamento |
| **Total** | **133** | **0 falhas** |

Nas telas de recrutamento e de estudos são testados os estados de **carregamento**, **lista vazia**, **erro do servidor**, **acesso negado** e **somente leitura por perfil**.

### 8.2 Testes E2E (Playwright, Chromium)

| Arquivo | Casos | O que valida |
|---|---|---|
| `setup/ambiente.setup.ts`, `setup/auth.setup.ts` | 2 | Backend responde, web abre sem mocks; cria os usuários de teste e guarda a sessão de cada perfil |
| `login.spec.ts` | 6 | Login dos 3 perfis; senha errada e e-mail inexistente com a mesma mensagem; logout bloqueia a rota e o botão Voltar |
| `sessao.spec.ts` | 3 | Sessão reaproveitada abre a área autenticada sem login |
| `acesso-por-perfil.spec.ts` | 4 | Abas e botões por perfil; URL proibida mostra acesso negado |
| `regressao-sprint1.spec.ts` | 4 | RF01 (cadastro de usuário) e RF04 (cadastro de estudo e validações) |
| `regras-recrutamento.spec.ts` | 7 | Estudo sem critério e visita não avança; candidato duplicado; fora de recrutamento; avaliação incompleta; exclusão reprova e histórico; sem consentimento; consentimento repetido |
| `fluxo-completo.spec.ts` | 2 | Fluxo completo da Release 1: planeja, abre recrutamento, cadastra, avalia, consente, inclui, lista e contador |
| `vagas-esgotadas.spec.ts` | 1 | Com 1 vaga, o primeiro é incluído ("1 de 1") e o segundo é bloqueado |
| **Total** | **29** (2 de preparação + 27) | **0 falhas** |

**Estabilidade:** a suíte foi executada com `--repeat-each`, com 1 e com 4 workers, sem falhas intermitentes. Os dados são únicos por execução.

---

## 9. Roteiros de sistema pela API

### 9.1 Roteiro do recrutamento (T68): 58 de 58 verificações

| Parte | Verificações | Resultado |
|---|---|---|
| 1. Dados de demonstração (somente leitura) | 11 | ✅ |
| 2. Regressão da Sprint 1 | 6 | ✅ |
| 3. Estudo fora de recrutamento | 2 | ✅ |
| 4. Sucesso: cadastrar, avaliar, consentir e incluir | 12 | ✅ |
| 5. Erros do recrutamento | 15 | ✅ |
| 6. Paginação, filtros e estado do estudo | 12 | ✅ |

Executado contra o backend real com o perfil `demo`. A mesma execução está ligada ao job "Ambiente Docker" do CI, que a repete contra o PostgreSQL do Docker Compose.

### 9.2 Roteiro do planejamento (T67)

Verifica o ambiente (web, rota interna, Swagger, CORS), a regressão de RF01 a RF04 e o planejamento (RF05 a RF07). Roda no job "Ambiente Docker" do CI.

---

## 10. Defeitos

### 10.1 Defeitos encontrados e corrigidos na release

| ID | Descrição | Origem | Severidade | Correção | Teste que o cobre |
|---|---|---|---|---|---|
| B-T67-01 | CORS com origens fixas no código impedia servir a web em outra porta ou host | Sprint 1 (segurança) | Média | Origens configuráveis (`CORS_ALLOWED_ORIGINS`) | `CorsIntegrationTest`, `CorsOrigensConfiguradasIntegrationTest` |
| B-T68-01 | `PUT`/`POST` do estudo aceitavam mudar o estado fora da transição, permitindo abrir o recrutamento sem critério ou visita | T42 | Alta | Estado só muda pelo endpoint de transição; `409 ESTADO_ALTERADO_FORA_DA_TRANSICAO` | `EstudoServiceTest`, `EstudoTransicaoRf07Test` |
| B-T68-02 | Candidato incluído podia ser reavaliado | T54 | Média | `409 CANDIDATO_JA_INCLUIDO` na avaliação | `AvaliacaoEndpointTest` |
| B-T68-03 | A inclusão podia ultrapassar o limite de vagas sob concorrência | T60 | **Alta** | Bloqueio pessimista do estudo | `ParticipacaoConcorrenciaTest` (com mutação manual) |
| B-T68-04 | `AuthContext` e `Login` do PR #5 quebravam o lint, usavam endpoint e chave de token errados, duplicavam a `LoginPage` e não expunham o usuário exigido pela T21 | PR #5 | Média | `AuthContext` passou a ser a porta de autenticação sobre a sessão real; `Login` duplicado removido | `AuthContext.test.tsx`, E2E de login |
| B-AUD-01 | Faltava o teste de endpoint dos participantes pedido pelo design da T62, e o teste de concorrência não limpava os dados | T62 | Baixa | `ParticipacaoEndpointTest` (15) e limpeza no `@AfterEach` | os próprios testes |

### 10.2 Defeitos abertos

Nenhum defeito funcional aberto nos fluxos de RF01 a RF11 até a data desta execução. As limitações e riscos estão na seção 11.

---

## 11. Riscos, limitações e recomendações

| # | Risco / limitação | Impacto | Mitigação ou recomendação |
|---|---|---|---|
| 1 | O Docker não rodou na máquina de desenvolvimento: o `docker-compose.yml`, os Dockerfiles e os roteiros T67 e T68 contra o PostgreSQL do compose só são provados pelo job do CI | Médio | Conferir o job "Ambiente Docker" verde no PR antes de criar a tag |
| 2 | Os testes locais usam H2 em modo PostgreSQL; o bloqueio de linha da produção é o do PostgreSQL | Médio | O job `Backend (PostgreSQL)` roda a suíte no PostgreSQL 16; manter obrigatório |
| 3 | O app mobile não tem testes automatizados (só lint e tipos) | Baixo na Release 1 (só o login existe) | Incluir testes e E2E mobile na Sprint 3, antes do RF17 |
| 4 | Os E2E rodam só em Chromium | Baixo | Acrescentar Firefox e WebKit se a banca exigir compatibilidade |
| 5 | Sem medição de cobertura no web | Baixo | Ativar `@vitest/coverage-v8` com meta numérica |
| 6 | Sem testes de carga, de desempenho e de segurança profunda (além de autenticação, perfis e validação) | Baixo para a escala acadêmica | Registrar como fora de escopo; considerar ZAP e k6 em release futura |
| 7 | Sem teste automatizado de acessibilidade | Baixo | Os testes usam papéis e nomes acessíveis (regiões, grupos de rádio), o que dá uma cobertura parcial; adicionar `axe` no E2E |
| 8 | Quatro códigos de erro genéricos são conferidos só pelo status HTTP; e há 3 trechos de `UsuarioService` sem teste (seção 5.2) | Baixo | Acrescentar a asserção do `codigoRegra`, um teste de exclusão de administrador por outro administrador e um de cadastro simultâneo |
| 9 | Qualquer pesquisador edita e exclui estudos de outro pesquisador (o RF04 não exige dono) | Decisão de produto | Validar com o professor; se for requisito, abrir um RF e testes |
| 10 | O teste de concorrência usa dados confirmados no banco compartilhado entre classes de teste | Baixo | Limpeza no `@AfterEach` já implementada; rodar em ordem aleatória se a suíte crescer |

---

## 12. Conclusão

Os 596 casos automatizados (434 de backend, 133 de web e 29 de E2E) e as 58 verificações do roteiro de recrutamento **passaram integralmente**, sem falhas, erros ou testes ignorados. A cobertura de linhas do código de negócio é de **99,2%**, acima da meta de 80%, e o CI bloqueia o build abaixo dela. Todos os requisitos RF01 a RF11 têm testes de sucesso, de erro e de permissão, e todas as regras de negócio com código de erro 409 têm ao menos um teste que as provoca.

Os defeitos relevantes encontrados durante a release (incluindo uma condição de corrida na inclusão de vagas) foram corrigidos e ganharam testes de regressão; a correção do bloqueio de vagas foi validada por mutação manual.

**Recomendação: aprovar a Release 1 para a entrega**, condicionada a: (1) o job "Ambiente Docker" do CI ficar verde na `main` (risco 1); (2) os 6 jobs do CI verdes nos PRs de integração; e (3) o time registrar os defeitos no Azure Boards e completar os itens de processo da T70 a T72 (nomes da equipe, burndown, retrospectiva, tag e Release).

---

## 13. Como reproduzir

```bash
# Backend: testes, cobertura e bloqueio de 80% (no diretório backend/ClinicalStudyPB)
./mvnw -B verify -Djacoco.haltOnFailure=true
#   relatório: backend/ClinicalStudyPB/target/site/jacoco/index.html
#   resultados: backend/ClinicalStudyPB/target/surefire-reports/

# Web: testes, lint, tipos e build (no diretório web/ClinicalStudyPB)
npm test
npm run lint
npx tsc -b
npm run build

# E2E (com backend em http://localhost:8081, web em http://localhost:5173 e .env.e2e criado a partir do .env.e2e.example)
npx playwright test

# Roteiros de sistema pela API (com o ambiente no ar)
docker compose up --build -d --wait
python3 scripts/verificar-ambiente.py
python3 scripts/verificar-recrutamento.py
docker compose down -v

# Capturas de tela do README
node scripts/capturar-telas.mjs   # em web/ClinicalStudyPB, com o backend em perfil demo
```

No CI (`.github/workflows/ci.yml`) os artefatos `cobertura-jacoco`, `playwright-report` e `logs-docker-compose` ficam disponíveis por 14 dias.

---

## Apêndice A — Casos de teste representativos

| ID | Requisito | Pré-condição | Passos | Resultado esperado | Tipo | Resultado |
|---|---|---|---|---|---|---|
| CT-01 | RF02 | Usuário ativo | `POST /api/auth/login` com credenciais válidas | `200` com token JWT; `/api/auth/me` devolve o usuário | API | ✅ |
| CT-02 | RF02 | Usuário ativo | Login com senha errada e com e-mail inexistente | `401` com a mesma mensagem, sem revelar qual campo errou | API / E2E | ✅ |
| CT-03 | RF03 | Perfil coordenador | `POST /api/estudos` | `403` e nada gravado | API | ✅ |
| CT-04 | RF05 | Estudo em planejamento | Cadastrar critério com descrição de 4 caracteres | `400` com erro no campo `descricao` | Valor-limite | ✅ |
| CT-05 | RF05 | Critério "Idade adulta" existente | Cadastrar "  IDADE ADULTA " no mesmo tipo | `409 CRITERIO_DUPLICADO` | Regra | ✅ |
| CT-06 | RF07 | Estudo em planejamento sem visita | Avançar para recrutamento | `409 ESTUDO_SEM_VISITA_PREVISTA` | Regra / E2E | ✅ |
| CT-07 | RF07 | Estudo em planejamento | Avançar direto para execução | `409 TRANSICAO_ESTADO_INVALIDA` | Transição | ✅ |
| CT-08 | RF08 | Estudo em recrutamento | Cadastrar o mesmo nome e data de nascimento duas vezes | `201` e depois `409 CANDIDATO_DUPLICADO` | Regra / E2E | ✅ |
| CT-09 | RF09 | Candidato cadastrado | Avaliar respondendo só um dos dois critérios | `400` "todos os critérios"; botão desabilitado na tela | API / E2E | ✅ |
| CT-10 | RF09 | Candidato reprovado | Reavaliar como aprovado | Nova avaliação vigente; a anterior fica no histórico | API / E2E | ✅ |
| CT-11 | RF10 | Candidato avaliado | Registrar consentimento duas vezes | `201` e depois `409 CONSENTIMENTO_DUPLICADO` | API / E2E | ✅ |
| CT-12 | RF10 | Estudo em recrutamento | Registrar consentimento com data futura | `400` "não pode ser futura" | Valor-limite | ✅ |
| CT-13 | RF11 | Candidato aprovado sem consentimento | Incluir | `409 CONSENTIMENTO_AUSENTE` | Regra / E2E | ✅ |
| CT-14 | RF11 | Estudo com 1 vaga e 1 participante | Incluir outro candidato pronto | `409 VAGAS_ESGOTADAS` com o limite na mensagem; botão desabilitado | Regra / E2E | ✅ |
| CT-15 | RF11 | 1 vaga, 6 candidatos prontos | 6 inclusões simultâneas | 1 × `201` e 5 × `409` | Concorrência | ✅ |
| CT-16 | Contrato | API no ar | Comparar a API com o `openapi-sprint2.yaml` | Sem divergências | Contrato | ✅ |
| CT-17 | Dados | Banco vazio | Subir com `SPRING_PROFILES_ACTIVE=demo` duas vezes | Dados criados uma só vez, sem duplicar | Persistência | ✅ |
| CT-18 | RF03 | Perfil administrador | Abrir a URL de estudos | "Acesso negado" na tela e `403` na API | E2E | ✅ |

## Apêndice B — Histórico de execuções relevantes

| Data | Evento | Resultado |
|---|---|---|
| 07/11/2026 | Primeira execução completa do backend na Sprint 2 | 376 testes, 99,2% |
| 09/11/2026 | Após T56, T62 (concorrência), CORS da T67 e integração do PR #5 | 419 testes, 99,2% |
| 09/11/2026 | Após a auditoria dos documentos de design (`ParticipacaoEndpointTest`) | **434 testes, 0 falhas, 99,2%** |
| 09/11/2026 | Web após `AuthContext` | **133 testes**, lint e build sem erros |
| 09/11/2026 | E2E contra backend e web reais, antes e depois do `AuthProvider` | **29 de 29** nas duas execuções |
| 09/11/2026 | Roteiro de recrutamento (T68) | **58 de 58** |
