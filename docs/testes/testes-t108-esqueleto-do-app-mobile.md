# Testes da T108 — Esqueleto do app mobile

| | |
|---|---|
| **Task** | T108 · Esqueleto do app: navegação, cliente HTTP e contexto de autenticação (PBI-24, Sprint 3) |
| **Design da task** | [T108](../design/sprint3/T108-esqueleto-do-app-navegacao-cliente-http-e-contexto-de-autenticacao.md) |
| **Ramo testado** | `feat/task108-esqueleto-app` (sobre a `main`) |
| **Data da execução** | 19/11/2026 |
| **Ferramentas** | Jest 30 com preset `jest-expo` 58, `@testing-library/react-native` 14 (com `test-renderer`), TypeScript 6 e ESLint |
| **Resultado** | **44 testes, 5 suítes, 0 falhas.** `npm run lint` e `npm run typecheck` sem erro. O build web (`expo export --platform web`) conclui |

## 1. Escopo

Esta é a primeira suíte automatizada do app mobile: até aqui o app só tinha `lint` e `tsc --noEmit`. Os testes cobrem o que a T108 entrega: o cliente HTTP e o formato único de erro, o tratamento central do 401, o modo de mocks, o contexto de sessão e os componentes base.

**Fora do escopo desta task** (previstos na [T113](../design/sprint3/T113-testes-de-backend-e-de-componente-do-app.md), T110 e T114):

- as telas de login, de estudos e de visitas, que ainda não têm lógica própria;
- o E2E no emulador;
- o `armazenamento.ts` (SecureStore e `localStorage`), que depende de módulos nativos e é simulado nos testes.

## 2. Como executar

No diretório `mobile/ClinicalStudyPB`:

```bash
npm ci
npm test                  # Jest: as 5 suítes
npm test -- sessao        # só os testes cujo caminho contém "sessao"
npm test -- -t "401"      # só os casos cujo nome contém "401"
npm test -- --coverage --collectCoverageFrom='src/api/**/*.ts' --collectCoverageFrom='src/contexts/**/*.tsx' --collectCoverageFrom='src/components/*.tsx' --collectCoverageFrom='!src/api/schema*.d.ts' --collectCoverageFrom='!**/__tests__/**'
npm run lint
npm run typecheck
```

Os testes não precisam de backend, emulador nem rede: `fetch` e o armazenamento são simulados, e o modo de mocks responde em memória. No CI, o passo **Testes (Jest)** roda no job **Mobile (Expo)**, depois do lint e da verificação de tipos.

## 3. Resultados por suíte

| Suíte | Arquivo | Casos | Foco |
|---|---|---|---|
| Cliente HTTP | `src/api/__tests__/cliente.test.ts` | 12 | Formato de erro, rede, 401, 204, timeout, mocks via cliente |
| Erro da API | `src/api/__tests__/erro.test.ts` | 6 | `erroDaResposta`, `ErroApi`, `mensagemDoErro` |
| Modo de mocks | `src/api/__tests__/mocks.test.ts` | 8 | Formato do contrato, 404, 500 simulado |
| Sessão | `src/contexts/__tests__/sessao.test.tsx` | 11 | Restauração, login, perfil, 401, logout |
| Componentes | `src/components/__tests__/componentes.test.tsx` | 7 | `EstadoVazio`, `EstadoErro`, `Selo`, `Carregando` |
| **Total** | | **44** | |

> Contagem conferida pela saída do Jest: **44 testes, 5 suítes, 0 falhas, 0 ignorados**.

### 3.1 Cliente HTTP (`cliente.test.ts`)

| Caso | Verifica | RQ |
|---|---|---|
| Converte o corpo de erro da Sprint 2 em `ErroApi` | `status`, `codigo` (de `codigoRegra`), `mensagem` e `errosCampo` | 01 |
| Mensagem padrão quando o corpo não é JSON | `Erro 503 na API.` | 01 |
| Falha de rede | `ErroApi` com `codigo: 'SEM_CONEXAO'` e mensagem `Não foi possível conectar à API em …` | 01 |
| 401 fora do login | Envia `Authorization: Bearer` e chama o callback de sessão expirada | 02, 03 |
| 401 do login | **Não** chama o callback e mantém a mensagem da API | 02 |
| 200 com corpo JSON | Devolve o corpo já convertido | 03 |
| 204 sem corpo | Resolve `undefined` | 03 |
| Timeout | Aborta a chamada em 15 s, envia `Content-Type: application/json` e vira `SEM_CONEXAO` | 03 |
| `mensagemDoErro` | `ErroApi`, `Error` comum e valor inesperado | 04 |
| Mocks pelo cliente (3 casos) | Login e consulta sem chamar a rede; 500 com `EXPO_PUBLIC_MOCK_ERRO`; credencial inválida e token ausente, com o callback chamado uma vez | 06, 07 |

### 3.2 Erro da API (`erro.test.ts`)

| Caso | Verifica |
|---|---|
| Prefere `codigoRegra` e `mensagem` | Formato novo tem precedência sobre `codigo` e `erro` |
| Aceita o formato antigo | `{ erro, codigo }` continua entendido |
| Sem corpo | Mensagem padrão, `codigo` indefinido, `errosCampo` vazio |
| `errosCampo` incompletos | Itens sem `campo` ou sem `erro` são descartados |
| `ErroApi` | É um `Error`, `name` `ErroApi`, `errosCampo` vazio por padrão |
| `mensagemDoErro` | `Error` sem mensagem e `undefined` caem na mensagem genérica |

### 3.3 Modo de mocks (`mocks.test.ts`)

| Caso | Verifica | RQ |
|---|---|---|
| Liga só com `EXPO_PUBLIC_USE_MOCKS=true` | `false`, `'false'` e `'true'` | 06 |
| Lista de estudos | Sem os campos exclusivos do detalhe; `resumo` com até 160 caracteres | 06 |
| Detalhe do estudo retirado | `retirada` traz só `dataRetirada`, **nunca o motivo** | 06 |
| Estudo de outro participante | 404 `RECURSO_NAO_ENCONTRADO` | 06 |
| Próximas visitas e histórico | Formato do contrato (`resumo`, `conteudo`, `totalPaginas`) | 06 |
| Visitas do participante | Sem `resultados` (RF18) | 06 |
| `EXPO_PUBLIC_MOCK_ERRO` | 500 em `/api/me/*`, mas `/api/auth/me` segue normal | 07 |
| Rota fora do mock | 404 | 06 |

### 3.4 Sessão (`sessao.test.tsx`)

| Caso | Verifica | RQ / RF |
|---|---|---|
| Sem token | Começa sem usuário | RF02 |
| Token válido | Restaura a sessão do participante | RF02 |
| Perfil diferente de participante na restauração | Descarta o token e fica sem usuário | RF03 |
| Token rejeitado (401) na restauração | Descarta o token e fica sem usuário | RF02 |
| Login do participante de demonstração | Abre a sessão e guarda o token | RF02 |
| Credenciais inválidas | Mensagem da API, sem sessão e sem token | RF02 |
| Perfil diferente de participante | `Este aplicativo é exclusivo para participantes…`, sem sessão e sem token | RF03 |
| **401 fora do login** | Limpa o token e a sessão (volta ao login) | 02 |
| Sair | Descarta o token e o usuário | RF02 |
| Desmontar o provider | Remove o callback de sessão expirada | 02 |
| `useSessao` fora do provider | Lança o erro `useSessao deve ser usado dentro de <SessaoProvider>` | — |

### 3.5 Componentes (`componentes.test.tsx`)

| Caso | Verifica | RQ |
|---|---|---|
| `EstadoVazio` com e sem texto | Título e texto exibidos | 09, 12 |
| `EstadoErro` | Mensagem e o botão **Tentar de novo** chama a ação | 12 |
| `EstadoErro` sem ação | Não mostra o botão | 12 |
| `Selo` nos 5 tons | Rótulo sempre em texto, nunca só cor | 12 |
| `Carregando` | Texto acessível (`accessibilityLabel`) e texto padrão | 12 |

## 4. Cobertura (linhas)

Medida só nos módulos que a T108 testa (`src/api`, `src/contexts`, `src/components`); as telas de `src/app` ainda não entram. Os tipos gerados (`schema*.d.ts`) e os testes ficam fora da conta.

| Arquivo | Linhas |
|---|---|
| `api/erro.ts` | 100% |
| `api/mocks/index.ts` e `mocks/dados.ts` | 100% |
| `api/cliente.ts` | 100% (linhas); o ramo da linha 63 (requisição sem token, sem o cabeçalho `Authorization`) não é exercitado |
| `contexts/sessao.tsx` | 100% (linhas); na restauração, só o 401 é testado; 403 e 404 (linhas 38–39) não |
| `components/` (`Carregando`, `EstadoErro`, `EstadoVazio`, `Selo`) | 100% |
| `components/ui.tsx` | 50% (`Tela`, `Cartao` e `EmBreve` não têm teste próprio) |
| `api/armazenamento.ts` | 0% (módulo nativo, simulado) |
| **Total do conjunto** | **88,3%** das linhas (85% dos ramos) |

A meta de 80% do projeto vale para o backend; o mobile não tem meta formal, e o número acima é só referência.

## 5. Rastreabilidade: requisitos × testes

| Requisito | Situação | Onde é verificado |
|---|---|---|
| RQ-T108-01 formato único de erro | ✅ | `cliente.test.ts`, `erro.test.ts` |
| RQ-T108-02 401 central limpa a sessão | ✅ | `cliente.test.ts`, `sessao.test.tsx` |
| RQ-T108-03 Bearer, JSON e timeout | ✅ | `cliente.test.ts` |
| RQ-T108-04 `mensagemDoErro` | ✅ | `cliente.test.ts`, `erro.test.ts` |
| RQ-T108-05 tipos gerados | 🔎 Verificação manual | `npm run api:tipos` gera os dois arquivos; `typecheck` valida |
| RQ-T108-06, 07 modo de mocks e estados de erro | ✅ | `mocks.test.ts`, `cliente.test.ts` |
| RQ-T108-08 Jest e Testing Library | ✅ | A própria execução da suíte |
| RQ-T108-09 testes de exemplo (cliente e `EstadoVazio`) | ✅ | `cliente.test.ts`, `componentes.test.tsx` |
| RQ-T108-10 `npm test` no CI | 🔎 A confirmar | Passo no `ci.yml`; só valida de fato na primeira execução do job no GitHub |
| RQ-T108-11 rotas fora das abas | 🔎 Verificação manual | `expo export --platform web` conclui com as rotas; sem teste automatizado |
| RQ-T108-12, 13 componentes base e tema | ✅ | `componentes.test.tsx` |
| RQ-T108-14 README | 🔎 Revisão de documento | `mobile/ClinicalStudyPB/README.md` |
| RQ-T108-15 avisar I5 | ⬜ Pendente | Ação manual depois do merge |

Legenda: ✅ coberto por teste automatizado · 🔎 conferido de outra forma · ⬜ pendente.

## 6. Limitações e lacunas

1. **Nada foi executado em emulador, celular ou contra o backend real.** Os testes simulam `fetch` e o armazenamento. O fluxo ponta a ponta (login com a API, 401 real) é da T114.
2. **O job Mobile do CI ainda não rodou com o passo `npm test`.** O passo foi acrescentado ao `ci.yml` e funciona localmente; a primeira execução no GitHub é a confirmação (Node 22, `npm ci`).
3. **As rotas `estudos/[estudoId]` e `visitas/[visitaId]` são telas de reserva.** Os testes delas virão com a T112, T117 e T118.
4. **Os mocks de `/api/me/*` seguem o contrato das convenções, não o YAML**, porque a T109 e a T115 ainda não acrescentaram esses caminhos ao OpenAPI. Quando acrescentarem, é preciso rodar `npm run api:tipos` e conferir os formatos dos mocks.
5. **Lacuna conhecida no teste do timeout:** ele cobre a espera pelas respostas; a leitura do corpo depois dos cabeçalhos não tem limite de tempo.

## 7. Falhas encontradas durante a execução

> **Aviso de processo.** Estas falhas foram **corrigidas antes de documentadas**, contra a regra do projeto (documentar a falha antes de corrigir). O registro abaixo foi feito depois, a partir do histórico da sessão. Nenhum teste foi removido por ser incômodo, mas **um arquivo de teste foi substituído** (D-T108-01) e três testes tiveram a forma de chamar ou de entrar alterada (D-T108-02, D-T108-05 e D-T108-07). Cada caso aponta o que mudou. Os identificadores `D-T108-01` a `D-T108-07` **ainda não constam** do [registro de defeitos](registro-de-defeitos.md): pela regra dele, o registro e a rastreabilidade de erros se atualizam em uma branch de documentação própria, nunca junto da branch de uma task. Esta seção é o detalhe a ser levado para lá.

Das sete falhas, **seis eram do teste ou da configuração** (D-T108-01, 02, 03, 05, 06 e 07) e **uma era de código de produção** (D-T108-04), achada pelo `typecheck`. Nenhuma indicou defeito no comportamento que os testes verificam.

| ID | O que falhou | Causa | Correção | Mudou o teste? |
|---|---|---|---|---|
| **D-T108-01** | 1ª execução: `EstadoVazio.test.tsx` → `SyntaxError: Unterminated string constant (23:5)`; a suíte não rodou | O arquivo foi criado por um comando de shell com *here-document* que terminou antes da hora (aviso `here-document … delimited by end-of-file`) e ficou **truncado** em `it('`, na linha 23 | O arquivo truncado foi **apagado** e recriado como `componentes.test.tsx`, com os mesmos três casos (EstadoVazio, EstadoErro, Selo), depois ampliado | **Sim:** arquivo substituído; os casos originais foram mantidos |
| **D-T108-02** | 1ª execução: `cliente.test.ts` → `ReferenceError: The module factory of jest.mock() is not allowed to reference any out-of-scope variables. Invalid variable access: tokenSalvo` | O Jest só aceita, dentro da fábrica do `jest.mock`, variáveis cujo nome começa com `mock` | Variável renomeada de `tokenSalvo` para `mockToken` no arquivo todo | Sim, só o nome da variável |
| **D-T108-03** | `npm run typecheck`: `Cannot find name 'describe'`, `'it'` e `'expect'` (TS2593, TS2304) | O TypeScript 6 não carrega `@types/*` automaticamente; o `@types/jest` estava instalado, mas não declarado | `"types": ["jest"]` em `tsconfig.json` | Não (configuração) |
| **D-T108-04** | `npm run typecheck`: `Selo.tsx … Property 'sucesso' does not exist on type …` (também `sucessoFundo`, `aviso` e `avisoFundo`) | **Defeito de produção.** Os tokens de cor foram acrescentados a `tema.ts` por uma substituição de texto que **não casou** (o arquivo usa CRLF) e falhou sem erro; o `Selo` já usava os tokens | Tokens gravados com uma expressão regular que aceita CRLF | Não; o teste do `Selo` cobre os cinco tons |
| **D-T108-05** | `npm run typecheck` em `cliente.test.ts`: `'falha' is of type 'unknown'` (TS18046) e `Cannot find name 'global'` (TS2304) | `.catch((erro) => erro)` devolve `unknown` quando a chamada tem retorno genérico; `global` não existe nos tipos do TS 6 | Helper `falhaDe(promessa): Promise<ErroApi>` e `globalThis.fetch`. **A primeira tentativa** (substituição por expressão regular) **quebrou a sintaxe** (`TS1005: ')' expected`, porque envolveu duas chamadas que não eram de falha); foi corrigida à mão | **Sim:** a forma de capturar o erro mudou; as expectativas ficaram iguais |
| **D-T108-06** | `sessao.test.tsx`, caso *recusa perfil diferente de participante*: `Cannot read properties of null (reading 'carregando')` (e, após uma tentativa de correção, `Expected: false, Received: undefined`) | O caso passava **isolado** (`-t RF03`) e falhava só depois do caso anterior, que rejeitava a promessa **dentro do `act`** (`await expect(act(...)).rejects`). O `result.current` do `renderHook` seguinte ficava nulo. Capturar o erro dentro do `act` eliminou o sintoma; o mecanismo interno da Testing Library **não foi investigado** | Helper `tentarEntrar`, que captura a falha dentro do `act`, usado nos dois casos de login que rejeitam. **A primeira tentativa** (`result.current?.carregando` em `montar()`) **não resolveu**; o `?.` continua no código por ser inofensivo | **Sim:** a forma de verificar a rejeição mudou nos dois casos; as mensagens esperadas ficaram iguais |
| **D-T108-07** | Mesmo caso, depois de D-T108-06: `Expected: "Este aplicativo é exclusivo para participantes…"`, `Received: "E-mail ou senha inválidos."` | **Erro do teste.** O login simulado só aceita `participante@clinicalstudy.test`; o teste entrava com o e-mail do coordenador e era recusado **antes** da checagem de perfil. O defeito estava escondido por D-T108-06 | O login usa o e-mail do participante e `usuarioAtual` é simulado para devolver o perfil `COORDENADOR`; a expectativa da mensagem **não mudou** | **Sim:** e-mail de entrada; a asserção foi preservada |

### 7.1 Estado depois das correções

`npm test`: **44 testes, 5 suítes, 0 falhas**; `npm run lint` e `npm run typecheck` sem erro. Nenhum caso foi removido nem desabilitado (`it.skip`, `xit`).

### 7.2 Regra daqui em diante

1. Falhou um teste: **registrar primeiro** (ID, no registro de defeitos ou, até ele ser atualizado, neste documento; mensagem exata, causa e se o erro é do teste ou do código).
2. Só depois corrigir, apontando o ID no commit.
3. **Não** apagar, desabilitar nem afrouxar um teste que falhou sem esse registro; se o teste estava errado, dizer o que mudou.

## 8. Notas técnicas para quem for escrever novos testes

- **Variáveis em `jest.mock()`:** o Jest só aceita variáveis de fora cujo nome comece com `mock` (por exemplo, `mockToken`).
- **Falha dentro de `act`:** capture o erro dentro do `act` (função `tentarEntrar` em `sessao.test.tsx`). Rejeitar a promessa dentro do `act` deixou o `result.current` do teste seguinte nulo (D-T108-06; causa observada por isolamento, mecanismo não investigado).
- **TypeScript 6:** `types` não carrega mais `@types/*` sozinho, por isso o `tsconfig.json` declara `"types": ["jest"]`.
- **Testing Library 14:** `render`, `renderHook` e `fireEvent` são assíncronos; use `await`.
- **Variáveis de ambiente:** `EXPO_PUBLIC_USE_MOCKS` e `EXPO_PUBLIC_MOCK_ERRO` são lidas a cada chamada; defina e apague em `beforeEach` e `afterEach`.
