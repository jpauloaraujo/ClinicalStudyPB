# Testes da T110 — Login e logout no app com a API real

> Plano: [T110-plano-de-implementacao](../design/sprint3/T110-plano-de-implementacao.md) · Guia: [T110](../design/sprint3/T110-login-e-logout-no-app-com-a-api-real.md)
> Branch: `feat/task110-login-logout-app` (criada a partir de `feat/task109-acesso-participante-endpoints-me`).
> Regra seguida: **nenhum teste foi excluído nem alterado para passar**. Toda falha foi registrada na seção 4 **antes** de qualquer correção.

## 1. Linha de base (antes da T110)

Comandos em `mobile/ClinicalStudyPB`: `npm test`, `npm run lint`, `npm run typecheck`.

| Verificação | Resultado |
|---|---|
| `npm test` | 5 suítes, **44 testes passando** |
| `npm run lint` | sem erros |
| `npm run typecheck` | sem erros |

## 2. Alterações da T110 (rastreabilidade)

| Arquivo | Tipo | O que mudou | RQ |
|---|---|---|---|
| `src/api/erro.ts` | alterado | `CODIGO_PERFIL_NAO_PARTICIPANTE`, mensagens do login e `mensagemDoLogin()` | 03, 05 |
| `src/api/cliente.ts` | alterado | reexporta o código e `mensagemDoLogin` | 03 |
| `src/contexts/sessao.tsx` | alterado | `aviso` de sessão expirada, flag de login em andamento, recusa com código, `sair` com `try/finally` | 05, 07, 08, 09 |
| `src/app/login.tsx` | alterado | `mensagemDoLogin`, erro some ao digitar, foco e `returnKeyType`, bloqueio de envio duplo, `ScrollView`, exibe o aviso | 01, 02, 03, 04, 09, 11 |
| `src/api/estudos.ts` | **novo** | `estudosApi.listar()` sobre `GET /api/me/estudos` | 06 |
| `src/app/(tabs)/index.tsx` | alterado | carregando, erro, aviso "Você ainda não está incluído em nenhum estudo." | 06 |
| `src/app/(tabs)/perfil.tsx` | alterado | `Sair` não deixa rejeição sem tratamento | 08 |
| `src/api/mocks/dados.ts` e `mocks/index.ts` | alterado | contas de mock: participante sem vínculo e os 3 perfis da web; `/api/me/**` responde 403 para perfil que não é participante | 05, 06 |
| `src/api/__tests__/erro.test.ts` | ampliado | 8 casos de `mensagemDoLogin` | 03, 05 |
| `src/contexts/__tests__/sessao.test.tsx` | ampliado | 12 casos novos (3 perfis, 403/404, rede, aviso, sair) | 05, 07, 08, 09 |
| `src/api/__tests__/estudos.test.ts` | **novo** | 4 casos de `estudosApi.listar()` | 06 |
| `src/telas/__tests__/login.test.tsx` e `inicio-e-perfil.test.tsx` | **novos** | 15 e 7 testes de componente | 01 a 09 |

## 3. Resultados por suíte

Ver as seções 5 e 6 (resultados finais e suítes novas). A seção 4 registra as falhas na ordem em que apareceram.

## 4. Falhas encontradas e correções

Cada falha é descrita **antes** da correção. Nenhum teste foi excluído.

### F-T110-01 · `npm run typecheck` falhou em `login.tsx`

- **Quando:** primeira execução depois de escrever o login, o Início e a sessão.
- **Evidência:**
  - `src/app/login.tsx(82,58): error TS2339: Property 'focus' does not exist on type 'TextInputType'.`
  - `src/app/login.tsx(89,15): error TS2322: Type 'RefObject<TextInputType | null>' is not assignable to type 'Ref<_TextInputInstance> | undefined'.`
- **Causa (hipótese a confirmar na correção):** o `useRef<TextInput>` usa `TextInput` como **tipo da instância**, mas neste React Native o valor exportado `TextInput` tem outro tipo (`TextInputType`); a instância vem de `_TextInputInstance`/`React.ComponentRef<typeof TextInput>`.
- **Natureza:** erro do **código novo** (tipagem do `ref`), não de teste. O comportamento não chegou a rodar sem tipagem; os testes existentes passaram (52).
- **Correção prevista:** tipar o `ref` com `ComponentRef<typeof TextInput>`.
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-02 · `npm run lint` falhou em `(tabs)/index.tsx`

- **Quando:** mesma execução.
- **Evidência:** `src/app/(tabs)/index.tsx:29:13 Avoid calling setState() directly within an effect  react-hooks/set-state-in-effect` (regra do `eslint-config-expo` com o React Compiler).
- **Causa:** `useEffect(carregar, [carregar])` chama `setSituacao({ tipo: 'carregando' })` de forma síncrona dentro do efeito.
- **Natureza:** erro do **código novo**.
- **Correção prevista:** como o estado inicial já é "carregando", o efeito não precisa redefini-lo; só `carregar` chamado pelo botão "Tentar de novo" mostra o carregando de novo. O `setState` fica dentro do `.then`/`.catch` (callbacks assíncronos).
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-03 · `login.test` — "exige e-mail e senha antes de enviar" estourou 5000 ms

- **Quando:** primeira execução da suíte completa com os testes novos (`npm test`: 2 falhas, 88 passando, 90 no total). Na suíte isolada a falha apareceu em uma execução e sumiu em outra: é **intermitente**.
- **Evidência:** `thrown: "Exceeded timeout of 5000 ms for a test."` no **primeiro** teste do arquivo; o arquivo levou 28,7 s na suíte completa e 12,5 s isolado.
- **Causa (hipótese):** o primeiro `render` do arquivo paga o custo de transformar e carregar `react-native`, `expo-router` e `safe-area-context`; com as 8 suítes em paralelo, isso passa dos 5 s padrão do Jest. Não é defeito da tela: o mesmo teste passa quando a máquina está menos carregada.
- **Natureza:** erro do **ambiente de teste** (tempo-limite), no teste novo.
- **Correção prevista:** dar tempo-limite maior **só ao arquivo novo** (`jest.setTimeout`); o teste e suas asserções ficam como estão.
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-04 · `login.test` — "desabilita o botão durante o envio e não envia duas vezes" estourou 5000 ms

- **Quando:** mesma execução (e em 2 de 3 execuções isoladas).
- **Evidência:** `thrown: "Exceeded timeout of 5000 ms for a test."` no teste que mantém `entrar` pendente de propósito.
- **Causa (hipótese):** no Testing Library 14, `await fireEvent.press(...)` roda o manipulador dentro de `act` assíncrono e espera o que ele devolver. O manipulador `enviar` devolve a promessa de `entrar`, que o teste só resolve depois (`concluir`). O teste fica esperando uma promessa que ele mesmo controla: **impasse no teste**, não na tela.
- **Natureza:** erro do **teste novo** (o código da tela está correto: o botão fica desabilitado e há um único envio).
- **Correção prevista:** não aguardar o toque que dispara o envio pendente (`void fireEvent.press`) e deixar o `act` esvaziar as atualizações; as asserções (botão desabilitado, uma única chamada a `entrar`) ficam iguais.
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-05 · Depois da correção da F-T110-04, 11 testes de `login.test` falharam em cascata

- **Quando:** segunda execução completa (`npm test -- --coverage`): 11 falhas, 79 passando, 90 no total. `lint` e `typecheck` passaram.
- **Evidência:** `You seem to have overlapping act() calls, this is not supported. Be sure to await previous act() calls before making a new one.` no teste "desabilita o botão durante o envio…"; depois dele, todos os testes seguintes do arquivo falharam com `Unable to find an element with accessibility label: E-mail` / `Senha` / texto da tela (a árvore deixou de renderizar).
- **Causa:** a correção da F-T110-04 (`void fireEvent.press(...)`) deixa o `act` do toque **aberto** enquanto o teste abre outro `act`; o React Test Renderer fica em estado inválido e contamina os testes seguintes do mesmo arquivo. Só o primeiro (o de envio pendente) é falha real; os outros 10 são efeito dele.
- **Natureza:** erro do **meu reparo** no teste novo; a tela continua correta.
- **Correção prevista:** disparar `onPress` e `onSubmitEditing` chamando o manipulador **dentro** de `await act(async () => { ... })` com um callback que **não devolve** a promessa pendente; assim o `act` fecha antes de o teste continuar e a promessa de `entrar` só é resolvida depois, em `concluir()`. Asserções mantidas.
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-06 · Depois da correção da F-T110-05, sobrou 1 falha: `aoPressionar is not a function`

- **Quando:** terceira execução completa: 1 falha, 89 passando, 90 no total (a cascata da F-T110-05 desapareceu). `lint` e `typecheck` passaram.
- **Evidência:** `TypeError: aoPressionar is not a function` em `login.test.tsx:81`, linha `void aoPressionar()`.
- **Causa:** `getByRole('button')` devolve o elemento **nativo** (host) do `Pressable`; o `onPress` fica no componente composto, não nas props do elemento nativo, então `props.onPress` é `undefined`.
- **Natureza:** erro do **meu reparo** no teste novo; a tela continua correta.
- **Correção prevista:** usar a `userEvent` do Testing Library (`userEvent.setup()` e `await user.press(...)`), que dispara o toque pelo caminho real do `Pressable` sem esperar a promessa devolvida pelo manipulador. Se a hipótese da F-T110-04 não se confirmar, será registrada como nova falha. Asserções mantidas.
- **Estado:** corrigida (resolução na seção 5.1).

### F-T110-07 · Com a `userEvent`, `type(..., '{enter}')` falhou: `Unknown key "enter"`

- **Quando:** execução isolada do teste "desabilita o botão…" depois da correção da F-T110-06.
- **Evidência:** `Unknown key "enter" in "{enter}"` em `login.test.tsx:86`. O toque (`user.press`) e a asserção de "Entrando…" **passaram** nessa execução, o que confirma a hipótese da F-T110-04 (o `userEvent` não espera a promessa do manipulador).
- **Causa:** a sintaxe `{enter}` é do `user-event` da web; a versão para React Native envia o Enter com a opção `{ submitEditing: true }`.
- **Natureza:** erro do **teste novo** (API do Testing Library); a tela está correta.
- **Correção prevista:** `await usuario.type(campo, 'a', { submitEditing: true })`.
- **Estado:** corrigida (resolução na seção 5.1).

## 5. Resultados finais

Comandos em `mobile/ClinicalStudyPB`, depois das correções da seção 4:

| Verificação | Antes da T110 | Depois |
|---|---|---|
| `npm test` | 5 suítes · 44 testes | **8 suítes · 90 testes, todos passando** (+46) |
| `npm run lint` | limpo | limpo |
| `npm run typecheck` | limpo | limpo |
| Cobertura de linhas (arquivos tocados) | n/d | `erro.ts` 100% · `estudos.ts` 100% · `sessao.tsx` 100% · `login.tsx` 96,9% · `(tabs)/index.tsx` 100% · `(tabs)/perfil.tsx` 100% · geral 93,7% |

### 5.1 Resolução das falhas

Nenhum teste foi excluído; os testes continuam verificando o mesmo comportamento. As correções mexeram só no código novo e na forma de disparar o evento nos testes novos.

| ID | Resolução | Onde |
|---|---|---|
| F-T110-01 | `useRef<ComponentRef<typeof TextInput>>` no lugar de `useRef<TextInput>` | `src/app/login.tsx` |
| F-T110-02 | O `setState` saiu do corpo do efeito: `buscar` só atualiza o estado nos callbacks assíncronos e o botão "Tentar de novo" chama `tentarDeNovo` (que mostra o carregando e chama `buscar`) | `src/app/(tabs)/index.tsx` |
| F-T110-03 | `jest.setTimeout(30000)` só no arquivo novo | `src/telas/__tests__/login.test.tsx` |
| F-T110-04 | Confirmada a hipótese (ver F-T110-07): o toque deixou de ser disparado por `fireEvent.press`, que esperava a promessa pendente | `login.test.tsx` |
| F-T110-05 | Tentativa com `act` aberto foi descartada (causava o conflito); o teste voltou a ter um único `act` por vez | `login.test.tsx` |
| F-T110-06 | Tentativa de ler `props.onPress` do elemento nativo foi descartada; passou a usar `userEvent.setup()` e `user.press` | `login.test.tsx` |
| F-T110-07 | `user.type(campo, 'a', { submitEditing: true })` no lugar de `{enter}` | `login.test.tsx` |

Lição para os próximos testes de tela: quando o manipulador devolve uma promessa que o teste controla, usar `userEvent`, não `await fireEvent.press`; e nunca deixar um `act` aberto.

## 6. Resultados por suíte (T110)

| Suíte | Testes novos | O que cobre |
|---|---|---|
| `src/api/__tests__/erro.test.ts` | 8 (`mensagemDoLogin`: credencial, perfil, rede e 5 genéricas via `it.each`) | RQ-T110-03 e 05 |
| `src/contexts/__tests__/sessao.test.tsx` | 12 (`it.each`: 3 perfis, 403 e 404) | RQ-T110-05, 07, 08 e 09 |
| `src/api/__tests__/estudos.test.ts` | 4 | RQ-T110-06 |
| `src/telas/__tests__/login.test.tsx` | 15 | RQ-T110-01 a 05 e 09 |
| `src/telas/__tests__/inicio-e-perfil.test.tsx` | 7 | RQ-T110-06 e 08 |

Os testes novos ficaram em `src/telas/` e não em `src/app/__tests__/` (como pedia o guia) porque o Expo Router trata tudo em `src/app/` como rota.

## 7. Rastreabilidade: requisitos × testes

| RQ | Atendido por | Testes |
|---|---|---|
| RQ-T110-01 | `login.tsx` | `login.test`: vazio, formato, erro some ao digitar |
| RQ-T110-02 | `login.tsx` | `login.test`: botão desabilitado e envio único, mostrar/ocultar, teclado e `returnKeyType` |
| RQ-T110-03 | `erro.ts` (`mensagemDoLogin`), `login.tsx` | `erro.test`; `login.test`: credencial, rede, erro genérico sem JSON nem URL |
| RQ-T110-04 | `login.tsx` | `login.test`: aviso de dados fictícios |
| RQ-T110-05 | `sessao.tsx`, `erro.ts` | `sessao.test` (3 perfis, token removido); `login.test` (3 perfis, mensagem) |
| RQ-T110-06 | `estudos.ts`, `(tabs)/index.tsx`, mocks | `estudos.test`; `inicio-e-perfil.test` |
| RQ-T110-07 | `sessao.tsx` | `sessao.test`: 401 (já existia), 403, 404, API fora do ar mantém o token |
| RQ-T110-08 | `sessao.tsx`, `perfil.tsx` | `sessao.test` (sair); `inicio-e-perfil.test` (Perfil) |
| RQ-T110-09 | `sessao.tsx`, `login.tsx` | `sessao.test`: aviso, sem aviso na restauração, 401 do próprio login não vira "sessão expirada"; `login.test`: exibe o aviso |
| RQ-T110-10 | **não executado** | Ver seção 8 |
| RQ-T110-11 | `login.tsx` (`ScrollView`, `keyboardShouldPersistTaps`, `behavior="height"` no Android) | **não executado em aparelho**; ver seção 8 |
| RQ-T110-12 | todas as suítes acima | `npm test` |

## 8. Limitações e pendências

- **RQ-T110-10 e RQ-T110-11 (emulador Android e celular real) não foram executados.** Esta sessão não tinha emulador, aparelho nem o backend no ar. O roteiro de 12 cenários está no [plano, passo 9](../design/sprint3/T110-plano-de-implementacao.md). Os testes de componente rodam com a API simulada e **não substituem** a validação em dispositivo, que é critério de aceite da task. As capturas devem ir para o repositório e qualquer defeito de plataforma vira Bug.
- Os 3 perfis da web nos mocks usam e-mails inventados (`administrador@`, `pesquisador@`, `coordenador@clinicalstudy.test`); não foi conferido se coincidem com os da seed V2 do backend.
- No mock, `participante@clinicalstudy.test` **tem estudos**, ao contrário do seed real (onde está sem vínculo). Para o aviso "sem estudo" nos mocks, usar `sem.vinculo@clinicalstudy.test`; no backend real, `participante@clinicalstudy.test`.
- Não foi rodado `npx expo export` para confirmar que nenhum arquivo de teste vira rota; os testes novos estão fora de `src/app/`, então o risco é nulo para eles.
- O backend não foi alterado, e portanto os testes de backend não foram reexecutados.
