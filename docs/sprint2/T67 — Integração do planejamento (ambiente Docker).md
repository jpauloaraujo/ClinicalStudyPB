# T67 — Integração e correções: planejamento do estudo

> Plano (Sprint 2): subir o ambiente completo sem mocks com Docker Compose, percorrer o fluxo de planejamento (criar estudo, definir critérios e visitas, avançar de estado) nos perfis de pesquisador e coordenador, corrigir os defeitos registrando cada um como Bug no Azure Boards ligado à task de origem, e confirmar que RF01 a RF04 continuam funcionando.

## 1. Ambiente completo

| Arquivo | O que faz |
|---|---|
| `docker-compose.yml` | Três serviços: `db` (PostgreSQL 16), `backend` (Spring Boot) e `web` (build do Vite servido por nginx). Cada um tem healthcheck, e o compose só sobe o próximo quando o anterior está saudável. |
| `backend/ClinicalStudyPB/Dockerfile` | Dois estágios: Maven + JDK 21 compila o jar (sem testes, que rodam no CI); JRE 21 Alpine executa, sem root. |
| `web/ClinicalStudyPB/Dockerfile` + `nginx.conf` | Node 22 gera o build com `VITE_API_URL` e `VITE_USE_MOCKS=false`; nginx serve os arquivos e devolve o `index.html` nas rotas do React Router (`/estudos/...`). |
| `.dockerignore` (backend e web) | Deixa fora da imagem `target/`, `node_modules/`, relatórios e arquivos locais com credenciais (`application-dev.properties`, `.env.e2e`). |
| `.env.example` (raiz) | Variáveis opcionais do compose. Sem `.env`, valem os mesmos padrões fictícios. |
| `scripts/verificar-ambiente.py` | Percorre o fluxo pela API contra o ambiente no ar (detalhes na seção 2). Só usa a biblioteca padrão do Python 3. |
| `.github/workflows/ci.yml`, job **Ambiente Docker (T67)** | Sobe o compose no GitHub Actions, roda o script e publica os logs dos containers quando falha. |

Decisões:

- **Portas iguais às do desenvolvimento local** (web 5173, API 8081). A origem do navegador continua `http://localhost:5173`, então o CORS e o `.env.e2e.example` dos testes E2E funcionam sem mudança. O PostgreSQL fica na 5433 do host para não conflitar com uma instalação local.
- **`VITE_API_URL` é a URL vista pelo navegador**, não pelo container. Por isso o padrão é `http://localhost:8081`, e o valor entra no build da imagem (argumento `VITE_API_URL`).
- **Perfil `demo` por padrão**: migrations comuns mais os dados fictícios da T63, que servem à T68 e à apresentação. `SPRING_PROFILES_ACTIVE=prod` sobe só com as migrations (não existe `application-prod.properties`; o nome só evita carregar o perfil `dev`).
- **`SPRING_JPA_SHOW_SQL=false`** no compose, para os logs do container mostrarem erros e não cada consulta.
- **O mobile fica fora do Docker**, porque o Expo roda no dispositivo ou no emulador. Ele aponta para `http://<IP-da-máquina>:8081`.

## 2. Roteiro verificado (`scripts/verificar-ambiente.py`)

| Grupo | Verificações |
|---|---|
| Ambiente | web responde em `/login`; rota interna abre o `index.html` (sem 404 do nginx); `/v3/api-docs` responde; preflight CORS libera a origem da web |
| Regressão RF01–RF04 | senha errada → 401; API sem token → 401; login dos três perfis; `/api/auth/me` devolve o pesquisador; administrador lista e cadastra usuário (201); pesquisador não lista usuários (403); estudo sem título → 400 com `errosCampo[].campo = titulo`; coordenador não cria estudo (403) |
| Planejamento (RF05–RF07) | estudo nasce em `PLANEJAMENTO`; coordenador vê o estudo; avançar sem critério → 409 `ESTUDO_SEM_CRITERIO_INCLUSAO`; critérios de inclusão e exclusão (201); critério repetido ignorando maiúsculas e espaços → 409 `CRITERIO_DUPLICADO`; coordenador não cadastra critério (403) mas consulta os dois; avançar sem visita → 409 `ESTUDO_SEM_VISITA_PREVISTA`; visita (201); visita no mesmo dia → 409 `VISITA_DIA_DUPLICADO`; coordenador consulta a visita; coordenador não avança (403); pular para `EXECUCAO` → 409 `TRANSICAO_ESTADO_INVALIDA`; pesquisador avança para `RECRUTAMENTO` (200); critério depois do planejamento → 409; histórico registra o avanço |

Cada execução cria um estudo e um usuário novos, com sufixo de horário, então o script pode ser repetido no mesmo banco.

### Roteiro manual na web (pesquisador e coordenador)

A parte visual não é coberta pelo script e é conferida no navegador, em http://localhost:5173:

1. **Pesquisador** (`demo.pesquisador@clinicalstudy.test`): criar estudo → abrir o detalhe → aba Critérios vazia mostra a mensagem de lista vazia → cadastrar inclusão e exclusão → aba Visitas → cadastrar visita → tentar avançar antes de ter critério e visita (a mensagem deve explicar o motivo) → avançar para recrutamento.
2. Em cada tela: o indicador de carregamento aparece enquanto a API responde, o erro da API aparece como mensagem clara (sem JSON cru) e não aparece botão que o perfil não pode usar.
3. **Coordenador** (`demo.coordenador@clinicalstudy.test`): abrir o mesmo estudo → critérios e visitas aparecem só para leitura (sem botões de criar, editar, excluir ou avançar estado).
4. **Administrador** (`demo.admin@clinicalstudy.test`): tela de usuários lista e cadastra (RF02/RF03).

## 3. Defeitos encontrados

Cada defeito vira um **Bug** no Azure Boards, ligado (Related) à task de origem e à T67.

| # | Título do Bug | Task de origem | Passos para reproduzir | Esperado | Obtido | Correção |
|---|---|---|---|---|---|---|
| B-T67-01 | CORS com origens fixas no código impede servir a web em outra porta ou host | Azure 3060 / 3063 (login JWT e segurança da API, Sprint 1) | Servir a web em uma origem diferente de `localhost:5173` (outra porta no compose, ou o IP da máquina na rede) e fazer login | Origem configurável sem recompilar | Navegador bloqueia a chamada (preflight 403), porque `SecurityConfig` só aceitava `localhost:5173` e `localhost:8082` fixos | Propriedade `app.cors.allowed-origins` / variável `CORS_ALLOWED_ORIGINS`, com os mesmos padrões. Coberto por `CorsIntegrationTest`. **Contrato não muda.** |

Ainda não há defeitos de contrato, mensagens, estados de carregamento, lista vazia ou permissões visuais registrados. A primeira execução do job no CI e o roteiro manual da seção 2 completam esta tabela; acrescente aqui cada defeito novo com o número do Bug no Azure Boards.

## 4. Como conferir

```bash
docker compose up --build -d --wait
python3 scripts/verificar-ambiente.py   # termina com "N de N verificações passaram."
docker compose down -v
```

No GitHub, o job **Ambiente Docker (T67)** do PR precisa estar verde.

## 5. Pendências

- Job **Ambiente Docker (T67)** verde no GitHub (a primeira execução real das imagens).
- Roteiro manual da seção 2 na web, com os defeitos visuais registrados como Bug.
- Registrar o B-T67-01 (e os próximos) no Azure Boards.
