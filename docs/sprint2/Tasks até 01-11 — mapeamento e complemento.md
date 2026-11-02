# Tasks da Sprint 2 até 01/11: mapeamento e complemento

> Documento de referência de **o que já estava feito, o que foi acrescentado e o que ainda depende de pessoas** nas tasks da Sprint 2 com prazo até 01/11 (arquivo "Tasks_Sprint2_ate_01-11"). Regra seguida: não alterar o que já existia, só acrescentar o que faltava. Estado verificado em 02/11/2026.

## 1. Resumo

| Medida | Resultado |
|---|---|
| Tasks do documento | 21 linhas (9 tasks oficiais até 30/10, mais o cadastro no Azure Boards e as sugestões de sábado e domingo) |
| Já estavam feitas (código) | T38, T39, T41, T45, T51, T48, T54 (parte de backend), T60 (backend), T65 |
| Completadas agora | T36, T37, T38, T45, T54, T60 (testes e documentos) |
| Feitas agora, não existiam | T40, T42, T43, T46, T49, T52, T55, T64, T69, correção de um defeito |
| Só dá para preparar daqui | Cadastro no Azure Boards (CSV de importação) |
| Backend | 156 testes, 0 falhas; 93,8% de linhas em domínio e serviços |
| Web | 78 testes de componente (Vitest), 10 E2E (Playwright) e a verificação das telas novas contra a API real; `tsc`, lint e build limpos |

## 2. Mapeamento task a task

Legenda: ✅ pronto · 🟡 parcial · ➕ acrescentado agora · ⏳ depende de pessoas ou de outra task.

| Task | Resp. | Antes de hoje | Acrescentado agora | Ainda falta |
|---|---|---|---|---|
| **T36** Modelo de domínio | I1 | 🟡 Modelo em código (commit `f29fc73`) e diagrama de classes em [Implementação do modelo de domínio de recrutamento](<Implementação do modelo de domínio de recrutamento.md>) | ➕ [docs/diagramas/dominio.md](../diagramas/dominio.md): diagrama de classes, matriz de estados, pré-condições de saída de planejamento e de inclusão, as nove regras com a situação de cada uma | ⏳ Respostas do PO (I2), apresentação na daily e aprovação do PR por I3 ou I4. **Não inventei respostas do PO** |
| **T37** Contrato OpenAPI | I1 | 🟡 `docs/sprint2/OpanAPI.yaml` (6 caminhos em inglês, como `/api/studies/...`, que a API não tem) | ➕ [docs/api/openapi-sprint2.yaml](../api/openapi-sprint2.yaml): 24 operações com os caminhos reais, 29 schemas, exemplos fictícios e erros 400/401/403/404/409. ➕ `ContratoOpenApiTest` compara o YAML com o Swagger real | ⏳ Avisar o time da regra "divergência se corrige no YAML"; decidir o destino do `OpanAPI.yaml` (não foi alterado) |
| **T38** Migrations e entidades | I3 | ✅ `V4` e entidades (`f29fc73`), `V5` e histórico (`9968ae4`) | ➕ `MigracaoSeguraTest`: migra um banco só com a Sprint 1 (V1 a V3 e o seed) para a Sprint 2 e confere que nada se perde; também em banco vazio | ⏳ Provar em PostgreSQL real no CI (nunca rodou lá) |
| **T39** Detalhe do estudo com abas | I6 | ✅ `f49001f` | — | — |
| **T40** Tipos e mocks | I5 | ❌ não existia | ➕ Tipos gerados do contrato (`npm run api:tipos`), Mock Service Worker com os 24 endpoints (`VITE_USE_MOCKS`), 8 módulos de API tipados e o conversor de erros por campo. Seção 3.2 | — |
| **T41** Máquina de estados | I3 | ✅ `9968ae4` (domínio, serviço, `V5`, 38 testes) | — | — |
| **T45** Backend de critérios | I4 | ✅ `f29fc73` | ➕ Bloqueio de **descrição** repetida (o código só bloqueava título) e teste | — |
| **T64** JaCoCo no CI | I1 | 🟡 JaCoCo no `pom.xml`, só relatório local | ➕ Resumo da cobertura de domínio e serviços na execução, relatório como artefato, regra de 80% pronta (ligar em 05/11 com uma linha). Seção 3.6 | ⏳ Ligar o bloqueio em 05/11 |
| **T65** E2E com Playwright | I7 | ✅ Código e documentação (`e2e/`, job `e2e`) | — (reexecutado: 10 testes passam) | ⏳ Evidências do PR: job verde no GitHub, falha forçada no CI, revisão e README em clone limpo |
| **Azure Boards** (cadastro da Sprint) | I2 | ❌ | ➕ [docs/azure-boards/sprint2-backlog.csv](../azure-boards/sprint2-backlog.csv): 10 PBIs, 37 tasks, 143 h, no formato de importação | ⏳ **Importar no Azure Boards**, atribuir responsáveis, vincular predecessoras e preencher a capacidade. Não tenho acesso ao Azure |
| **T46** Tela de critérios | I6 | ❌ (aba provisória) | ➕ Aba Critérios completa, sobre a API (e o mock), com testes | — |
| **T52** Candidatos e ficha | I5 | ❌ (aba provisória; backend T51 ✅ `f29fc73`) | ➕ Lista paginada com filtro, cadastro, ficha do candidato com espaços reservados para T58 e T61 | — |
| **T49** Visitas e atividades | I5 | ❌ (aba provisória; backend T48 ✅) | ➕ Aba Visitas completa (cartões expansíveis, formulários de visita e de atividade) com testes | — |
| **T54** Elegibilidade | I3 | ✅ Função pura, serviço e endpoints (`f29fc73`) | ➕ `AvaliacaoServiceTest` (resposta faltando, de outro estudo, repetida, estado, situação e histórico). ➕ **Correção de um defeito** (seção 4) | — |
| **T51** Backend de candidatos | I3 | ✅ `f29fc73`, conferido em `9968ae4` | — | — |
| **T42** Endpoint de transição | I4 | ❌ (T41 já expunha só o serviço) | ➕ `POST /api/estudos/{id}/transicoes` (só pesquisador), `GET .../historico-status`, 4 testes de integração | ⏳ Ver "estado pelo PUT", seção 5 |
| **T48** Backend de visitas | I4 | ✅ `f29fc73` | ➕ Contrato YAML e Swagger coincidem (testado); tela T49 | — |
| **T43** Tela de estado | I6 | ❌ | ➕ Indicador dos 4 estados, botão de avançar só para o pesquisador, confirmação sem volta e o erro 409 em um alerta, com testes | — |
| **T55** Tela de avaliação | I7 | ❌ | ➕ Avaliação na ficha: critérios em dois grupos, Salvar só com tudo respondido, resultado e critérios reprovadores em destaque, histórico, erros 400 e 409 | — |
| **T60** Inclusão de participantes | I1 | ✅ `ParticipacaoService` completo (`f29fc73`) | ➕ `ParticipacaoServiceTest` com repositórios simulados (cada pré-condição, ordem, limite exato) | ⏳ Tela da inclusão (T61) e teste de concorrência (T62) não fazem parte deste documento |
| **T69** Diagramas | I2 | ❌ | ➕ [docs/diagramas](../diagramas/): casos de uso, classes (T36), sequência de avançar o estado, sequência de incluir participante e arquitetura. Todos validados com o parser do Mermaid | — |

## 3. O que foi acrescentado e como funciona

### 3.1 Contrato OpenAPI (T37)

[docs/api/openapi-sprint2.yaml](../api/openapi-sprint2.yaml) descreve as 14 rotas e 24 operações do recrutamento e do estado do estudo, com os **mesmos caminhos e nomes de parâmetro da API** (`/api/estudos/{estudoId}/criterios`, `/api/candidatos/{candidatoId}/avaliacoes`...). `ContratoOpenApiTest` lê o YAML e a API publicada em `/v3/api-docs` e falha se um caminho ou método do contrato não existir na API, ou se um endpoint de recrutamento da API não estiver no contrato.

O formato de erro do contrato é o que a API devolve hoje: `{"erro", "codigo?"}`, com o status HTTP identificando a classe do erro. O pedido da T37 de uma "lista de erros por campo" **não existe na API**: na validação (400) vem um campo por resposta, em `"campo: mensagem"`. O conversor da web aceita os dois formatos.

### 3.2 Tipos e mocks (T40)

| Peça | Onde | Observação |
|---|---|---|
| Tipos gerados | `web/ClinicalStudyPB/src/api/schema.d.ts` | `npm run api:tipos`. O `openapi-typescript` só aceita TypeScript 5 e o projeto usa o 6, então o script o chama pelo `npx` com a versão fixada (7.13.0) em vez de instalá-lo; o arquivo gerado fica versionado |
| Módulos de API | `src/api/*.ts` | `criteriosApi`, `visitasApi`, `acoesApi`, `candidatosApi`, `avaliacoesApi`, `consentimentoApi`, `participantesApi`, `estadoEstudoApi`; o CRUD de estudos continua em `services/recursos.ts` |
| Conversor de erros | `src/api/erros.ts` | `converterErro` devolve mensagem, código da regra e mensagens por campo |
| Mocks | `src/mocks/` | `dados.ts` (estado em memória), `handlers.ts` (24 endpoints, login, `me` e estudos), `browser.ts`, `server.ts` (testes), `iniciar.ts` |
| Liga e desliga | `VITE_USE_MOCKS=true` | `npm run dev:mock` usa o modo `mocks` do Vite (`.env.mocks`). Desligado (padrão), o MSW nem é carregado e as chamadas vão para a API real |

Os mocks reproduzem as regras principais da API (estado exigido, duplicidade, validação, permissões 401/403) e o formato de erro. O login do mock aceita e-mails que comecem com `admin`, `pesquisador` ou `coordenador` e emite um token no formato de JWT com validade longa, para a sessão sobreviver ao recarregar a página. Há dois estudos de exemplo (planejamento e recrutamento).

Critérios de aceite da T40: `src/mocks/mocks.test.tsx` renderiza as abas de critérios, visitas e candidatos **sem nenhum `vi.mock`**, lendo e gravando no MSW (10 testes); e dois testes provam que, com a variável desligada, o worker não é iniciado.

### 3.3 Estado do estudo (T42 e T43)

- **Backend:** `EstudoTransicaoController` expõe `POST /api/estudos/{id}/transicoes` (corpo `{"destino": "RECRUTAMENTO"}`, só o pesquisador) e `GET /api/estudos/{id}/historico-status`. Usa o `EstudoTransicaoService` e a `MaquinaEstadosEstudo` que a equipe já tinha; o histórico é lido por `HistoricoStatusService`. Ver [diagrama de sequência](../diagramas/sequencia-avancar-estado.md).
- **Web:** `IndicadorEstado` mostra os quatro estados, destaca o atual, e só ao pesquisador oferece "Avançar para…", com um diálogo (`Confirmacao`) avisando que não há volta. Erros 409 aparecem em um alerta com a mensagem da regra. Fica no cabeçalho do `EstudoDetalhePage`.

### 3.4 Telas do estudo (T46, T49, T52 e T55)

Todas em `web/ClinicalStudyPB/src/pages/estudos/detalhe/`:

| Tela | Arquivo | Regras |
|---|---|---|
| Critérios | `abas/AbaCriterios.tsx` | Duas listas (inclusão e exclusão); formulário com as mesmas validações do backend; peso e "determinante" só em inclusão; editar e excluir com confirmação; somente leitura com aviso fora de planejamento ou para o coordenador; "Adicione ao menos um critério de inclusão para avançar" com a lista vazia |
| Visitas e atividades | `abas/AbaVisitas.tsx` | Visitas ordenadas pelo dia previsto, cada uma em cartão expansível com as atividades; formulários de visita (dia ≥ 0, tolerância 0 a 30) e de atividade (categoria em lista); somente leitura fora de planejamento ou para o coordenador |
| Candidatos | `abas/AbaCandidatos.tsx` | Lista paginada com filtro por situação e colunas de elegibilidade e consentimento; cadastro com validação e erro de duplicidade do servidor; botão desabilitado, com explicação, fora de recrutamento; o pesquisador só consulta |
| Ficha do candidato | `candidato/FichaCandidatoPage.tsx` | Rota `/estudos/:estudoId/candidatos/:candidatoId`. Dados do candidato e uma seção por etapa, cada uma em um componente próprio para as tasks não conflitarem |
| Avaliação (T55) | `candidato/SecaoAvaliacao.tsx` | Critérios em dois grupos, "se aplica" ou "não se aplica" e observação; aviso de que em exclusão "se aplica" reprova; Salvar só com todos respondidos; resultado do backend em destaque com os critérios reprovadores; histórico; erros 400 e 409 |
| Reservadas | `candidato/SecaoConsentimento.tsx`, `candidato/SecaoInclusao.tsx` | Espaços das tasks T58 e T61: quem as fizer troca só o corpo do componente |

As abas Participantes (e a inclusão) continuam provisórias: são da T61, fora deste documento.

### 3.5 Testes e correção

| Área | Arquivos novos |
|---|---|
| Backend (+27 testes sobre os 129 anteriores) | `EstudoTransicaoEndpointTest`, `AvaliacaoServiceTest`, `ParticipacaoServiceTest`, `MigracaoSeguraTest`, `ContratoOpenApiTest`, `CriterioDescricaoRepetidaTest`, `AvaliacaoHistoricoTest` |
| Web (78 testes, 69 novos) | `erros.test.ts`, `IndicadorEstado.test.tsx`, `AbaCriterios.test.tsx`, `AbaVisitas.test.tsx`, `AbaCandidatos.test.tsx`, `FichaCandidatoPage.test.tsx`, `mocks.test.tsx` (+ `src/test/ambiente.tsx`, utilitário) |

### 3.6 JaCoCo no CI (T64)

- **`pom.xml`:** execução `verificar-cobertura` (`jacoco:check`) só sobre as classes de domínio e de serviços, com `jacoco.cobertura.minima=0.80` e `jacoco.haltOnFailure=false`. Conferido em três modos: exigindo 99% bloqueando (falha), exigindo 99% só informando (passa com aviso) e 80% bloqueando (passa).
- **`.github/scripts/resumo-cobertura.py`:** lê o `jacoco.csv` e escreve a tabela de cobertura no resumo da execução do GitHub (`$GITHUB_STEP_SUMMARY`).
- **`ci.yml`:** o job do backend publica o resumo e o relatório HTML (`cobertura-jacoco`, 14 dias). Para **bloquear em 05/11**, troque `COBERTURA_BLOQUEAR: 'false'` por `'true'` no job `backend` (uma linha): o Maven passa a falhar (`-Djacoco.haltOnFailure=true`) e o script também.

Cobertura atual: **93,8%** das linhas do código de negócio (532 de 567) e 94,1% do backend inteiro.

## 4. Defeito encontrado e corrigido

`GET /api/candidatos/{id}/avaliacoes` devolvia `criteriosReprovadores` **vazio** para um candidato reprovado, quando a leitura acontecia em outra sessão do Hibernate. O critério de uma resposta é carregado de forma preguiçosa (proxy) e não era `instanceof CriterioInclusao/CriterioExclusao`, então a regra de elegibilidade não o reconhecia. Os testes anteriores rodavam tudo em uma sessão só, onde o objeto já era o real, e não o pegaram; apareceu ao conferir a tela contra a API de verdade. Correção de uma linha em `AvaliacaoResponse` (`Hibernate.unproxy`) e teste de regressão `AvaliacaoHistoricoTest`, que limpa a sessão entre a gravação e a leitura.

## 5. O que mudou em arquivos que já existiam

Tudo aditivo, só o necessário:

| Arquivo | Mudança | Motivo |
|---|---|---|
| `backend/.../service/CriterioService.java` | +14 linhas | T45: bloquear descrição repetida |
| `backend/.../controller/dto/AvaliacaoResponse.java` | desembrulhar o proxy do critério | Defeito da seção 4 |
| `backend/pom.xml` | propriedades e execução `check` do JaCoCo | T64 |
| `.github/workflows/ci.yml` | variável, resumo e artefato de cobertura | T64 |
| `web/.../routes/router.tsx` | as abas Critérios, Visitas e Candidatos passam a usar as telas reais; nova rota da ficha | T46, T49, T52 |
| `web/.../EstudoDetalhePage.tsx` | uma linha com o `IndicadorEstado` | T43 |
| `web/.../main.tsx` | liga o mock antes de renderizar, se a variável estiver ativa | T40 |
| `web/package.json`, `package-lock.json`, `.env.example`, `vite-env.d.ts`, `eslint.config.js` | scripts, `msw`, variável de ambiente, tipagem e ignorar arquivos gerados | T40 |
| `README.md` | seções novas e números dos testes | Documentação |

Os 9 testes do detalhe do estudo (T39) continuam passando sem alteração, e os 10 E2E também.

## 6. O que ainda depende de pessoas ou de decisão

1. **Respostas do PO (T36):** nada foi inventado; [dominio.md](../diagramas/dominio.md) lista as nove regras com "confirmação do PO pendente".
2. **Estado pelo `PUT`:** `PUT /api/estudos/{id}` ainda aplica qualquer `status` (e `POST /api/estudos` aceita qualquer estado inicial), o que contorna a máquina de estados. A equipe já tinha registrado isso em `T38-T41-T51-implementacao.md`. Não mexi porque exige alterar testes existentes (`EstudoServiceTest`, `RecrutamentoIntegrationTest`) e o formulário de edição de estudos; recomendo fechar na T42 junto com o time.
3. **`OpanAPI.yaml` x `openapi-sprint2.yaml`:** os dois existem em pastas diferentes e o primeiro descreve caminhos que não existem. Sugiro apagar o primeiro depois que a equipe confirmar.
4. **Arquivos de sessão do E2E versionados:** `web/ClinicalStudyPB/e2e/.auth/*.json` estão no Git, apesar de listados no `.gitignore` (alguém os adicionou). Contêm tokens de teste e mudam a cada execução. Recomendo `git rm -r --cached web/ClinicalStudyPB/e2e/.auth`.
5. **Links do README:** vários apontam para `docs/arquitetura.md`, `docs/status-sprint1.md` etc., que foram movidos para `docs/sprint1/`. Não alterei esses links.
6. **CI no GitHub:** os jobs `e2e` e `backend-postgres` nunca rodaram lá; a `V4` e a `V5` só foram executadas em H2. Confirme no primeiro push.
7. **Cadastro no Azure Boards, revisão dos PRs, daily e evidências da T65:** são ações de pessoas.
8. **Bloqueio de 80% (T64):** ligar em 05/11.
9. **Fora do documento, ainda sem fazer:** tela de consentimento (T58), tela e botão de inclusão e aba Participantes (T61), testes de concorrência (T62) e o E2E do fluxo completo (T66, que depende da T61).

## 7. Como rodar

```bash
# Backend (testes + cobertura; a regra de 80% só informa)
cd backend/ClinicalStudyPB && ./mvnw verify
python ../../.github/scripts/resumo-cobertura.py target/site/jacoco/jacoco.csv

# Web: tipos, lint, testes de componente e build
cd web/ClinicalStudyPB
npm run api:tipos      # só quando o contrato mudar
npm run lint && npm test && npm run build

# Web sem backend (dados fictícios): entre com pesquisador@x, coordenador@x ou admin@x e qualquer senha
npm run dev:mock

# E2E (backend e web no ar, ver o README, seção "Testes E2E")
npm run test:e2e
```
