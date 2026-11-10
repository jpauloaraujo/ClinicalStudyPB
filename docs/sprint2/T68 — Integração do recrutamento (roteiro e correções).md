# T68 — Integração e correções do recrutamento

> Plano (Sprint 2): repetir para o recrutamento o roteiro da T67, com os dados de demonstração (T63): cadastrar candidato, avaliar, registrar consentimento e incluir, nos casos de sucesso e de erro; conferir paginação, mensagens por código de erro e rótulos de formulário; registrar os defeitos como Bug e corrigir os da própria área; passar pela checagem de regressão de login, usuários e estudos da Sprint 1.

## 1. O que foi entregue

| Item | Onde |
|---|---|
| Roteiro executável do recrutamento pela API (58 verificações) | `scripts/verificar-recrutamento.py` |
| Execução no CI, depois do roteiro de planejamento | job **Ambiente Docker (T67)** do `.github/workflows/ci.yml`, passo "Verifica o fluxo de recrutamento (T68)" |
| Teste de concorrência da inclusão (prova do bloqueio de vagas) | `ParticipacaoConcorrenciaTest` (T62) |
| Capturas de tela dos fluxos de sucesso e de erro | `docs/imagens/` (geradas por `web/ClinicalStudyPB/scripts/capturar-telas.mjs`) |
| Suíte E2E do fluxo completo, das regras e das vagas esgotadas | `web/ClinicalStudyPB/e2e/tests/` (T65 e T66) |

O script só usa a biblioteca padrão do Python 3, roda contra o ambiente no ar e pode ser repetido no mesmo banco (cada execução cria um estudo novo):

```bash
docker compose up --build -d --wait      # perfil demo por padrão
python3 scripts/verificar-ambiente.py    # T67: planejamento e regressão RF01–RF04
python3 scripts/verificar-recrutamento.py
docker compose down -v
```

## 2. Roteiro verificado

| Parte | Verificações |
|---|---|
| 1. Dados de demonstração (somente leitura) | Estudo "Demo - Recrutamento com ultima vaga" em `RECRUTAMENTO`, limite 2; listagem paginada (4 de 6 candidatos em 2 páginas, 2 na segunda); filtro `CONSENTIDO` devolve `CAND-004` e `CAND-006`; contador de vagas; participante demo recebe 403. Nada é alterado: a última vaga continua livre para a apresentação |
| 2. Regressão da Sprint 1 | Senha errada → 401; sem token → 401; login dos três perfis; `/api/auth/me`; administrador lista usuários; coordenador não lista usuários (403) |
| 3. Estudo fora de recrutamento | Estudo em `PLANEJAMENTO` não aceita candidato → 409 `ESTUDO_FORA_DE_RECRUTAMENTO` |
| 4. Sucesso | Estudo novo com 1 vaga, critérios, visita e avanço para recrutamento; cadastrar, avaliar (aprovado), consentir e incluir; a inclusão traz código, data e quem incluiu; a ficha passa a `INCLUIDO` |
| 5. Erros | Candidato duplicado → 409 `CANDIDATO_DUPLICADO`; já incluído → 409 `CANDIDATO_JA_INCLUIDO`; avaliação incompleta → 400 ("todos os critérios"); sem avaliação → 409 `CANDIDATO_NAO_ELEGIVEL`; sem consentimento → 409 `CONSENTIMENTO_AUSENTE`; sem vaga → 409 `VAGAS_ESGOTADAS` (a mensagem cita o limite); consentimento repetido → 409 `CONSENTIMENTO_DUPLICADO`; pesquisador não avalia nem inclui (403) mas consulta (200) |
| 6. Paginação, filtros e estado | 5 candidatos em 3 páginas de tamanho 2; filtro `CADASTRADO` devolve 3; contador 1 de 1; estudo em `EXECUCAO` não aceita candidato nem consentimento (409) |

**Resultado da execução local** (backend com perfil `demo` em H2): **58 de 58 verificações passaram**, sem alterar o backend. A execução contra o PostgreSQL do Docker Compose acontece no job do CI.

## 3. Paginação, mensagens e rótulos

| Conferência | Resultado |
|---|---|
| Paginação | `GET /api/estudos/{id}/candidatos?pagina=&tamanho=&situacao=` devolve `conteudo`, `pagina`, `tamanho`, `totalElementos` e `totalPaginas`; coberta pelo script e pelos testes de candidatos |
| Mensagens por código de erro | A web traduz os cinco códigos da inclusão (`mensagensInclusao.ts`: `ESTUDO_FORA_DE_RECRUTAMENTO`, `CANDIDATO_JA_INCLUIDO`, `CANDIDATO_NAO_ELEGIVEL`, `CONSENTIMENTO_AUSENTE`, `VAGAS_ESGOTADAS`) e cai na mensagem do servidor para um código desconhecido; testado em `SecaoInclusao.test.tsx` |
| Rótulos de formulário | As seções usam regiões nomeadas ("Avaliação de elegibilidade", "Consentimento", "Inclusão"), grupos de rádio "Resposta para {critério}" e a caixa de confirmação do consentimento. Os testes E2E localizam os campos por esses nomes acessíveis, então um rótulo errado quebra o teste |
| Erro por campo | `errosCampo` do contrato de erro é lido por `api/erros.ts` e mostrado junto ao campo |

## 4. Defeitos encontrados

| # | Defeito | Origem | Correção |
|---|---|---|---|
| B-T68-01 | `PUT`/`POST` do estudo aceitavam mudar o estado fora da transição, o que permitia abrir o recrutamento sem critério ou visita | T42 | Estado só muda por `POST /transicoes`; `PUT` com outro estado → 409 `ESTADO_ALTERADO_FORA_DA_TRANSICAO` (corrigido e testado) |
| B-T68-02 | Candidato incluído podia ser reavaliado | T54 | 409 `CANDIDATO_JA_INCLUIDO` em `AvaliacaoService`, coberto por `AvaliacaoEndpointTest` |
| B-T68-03 | A inclusão podia ultrapassar o limite de vagas com requisições simultâneas | T60 | Bloqueio pessimista do estudo (`buscarComBloqueio`), provado por `ParticipacaoConcorrenciaTest`: sem o bloqueio o teste falha |
| B-T68-04 | O `AuthContext` e o `Login` do PR #5 quebravam o lint (`any`, variável sem uso, contexto e componente no mesmo arquivo), chamavam `/login` e usavam uma chave de token própria | PR #5 | Alinhados ao projeto (endpoint `/api/auth/login`, chave do cliente HTTP) e testados em `Login.test.tsx` |

O registro no Azure Boards segue o padrão da T67 (item do tipo Task com título "Bug", filho do PBI da T68, ligado à task de origem) e só pode ser feito por quem tem acesso à conta do time.

## 5. Pendências que dependem de pessoas

- Registrar B-T68-01 a B-T68-04 no Azure Boards e ligá-los às tasks de origem.
- Confirmar que o job **Ambiente Docker (T67)** fica verde no PR (o Docker não roda na máquina em que o roteiro foi escrito).
