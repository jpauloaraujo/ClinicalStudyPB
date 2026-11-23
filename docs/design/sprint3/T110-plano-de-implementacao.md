# T110 · Plano de implementação: login e logout no app com a API real

> **Sprint 3** · PBI-24 · RF17, RF02 e RF03 no mobile · **Estimativa:** 3h · **Depende de:** T108 (concluída) e T109 (concluída, branch atual)
> Guia da task: [T110-login-e-logout-no-app-com-a-api-real.md](T110-login-e-logout-no-app-com-a-api-real.md). Pendências herdadas: [T109-pendencias-para-proximas-tasks.md](T109-pendencias-para-proximas-tasks.md) (P11 e P12).
> Todos os caminhos abaixo são relativos a `mobile/ClinicalStudyPB/`, salvo indicação.

## 1. Diagnóstico: o que já existe e o que falta

Estado do código na branch `feat/task109-acesso-participante-endpoints-me`:

| RQ | Situação hoje | Lacuna |
|---|---|---|
| 01 Validação | `login.tsx` valida vazio e formato do e-mail com as mensagens certas | O erro **não some** quando o usuário volta a digitar |
| 02 Botão e teclado | Botão "Entrando…" desabilitado, mostrar/ocultar senha, `keyboardType="email-address"` e `autoCapitalize="none"` | Falta `returnKeyType`/`ref` (e-mail → senha → entrar). `onSubmitEditing` da senha chama `enviar` sem proteção contra envio duplo |
| 03 Mensagens | Mostra `falha.message` direto | A mensagem da API real é "Credenciais inválidas" e a de rede inclui a URL (`Não foi possível conectar à API em ...`). Os textos exigidos pelo RQ não aparecem |
| 04 Aviso fictício | Presente | Só manter |
| 05 Outros perfis | `sessao.tsx` recusa e remove o token, mas só `PARTICIPANTE` é testado (via mock) | Falta teste dos **três** perfis; a recusa é um `ErroApi` sem código, então o login não consegue diferenciá-la de um erro da API |
| 06 Sem estudo | Início não chama a API | Criar `estudosApi.listar()` e o aviso "Você ainda não está incluído em nenhum estudo." |
| 07 Restauração | Implementada (401/403/404 descartam o token; rede mantém) | Faltam testes de 403, 404 e de API fora do ar |
| 08 Perfil | Mostra nome, e-mail e "Participante" e o botão Sair | `sair` não tem tratamento de falha do `SecureStore`; sem teste do Perfil |
| 09 Sessão expirada | `definirAoExpirarSessao` limpa token e usuário | **Nenhuma mensagem** "Sua sessão expirou. Entre novamente." chega ao login |
| 10 e 11 Dispositivo | Nunca exercitado | Roteiro manual e evidências |
| 12 Testes | Existe `sessao.test.tsx` (10 casos); não há `login.test.tsx` | Criar testes de componente do login |

## 2. Decisões de projeto

1. **Mensagem única de credencial inválida.** A API devolve 401 "Credenciais inválidas" para e-mail inexistente, senha errada e conta desativada. A tela mapeia **todo 401 do login** para "E-mail ou senha inválidos, ou conta desativada." e não repassa o texto do servidor.
2. **Código para a recusa de perfil.** Em `src/api/erro.ts`, adicionar `CODIGO_PERFIL_NAO_PARTICIPANTE` (junto de `CODIGO_SEM_CONEXAO`) e lançá-lo em `sessao.tsx`. O login distingue as falhas por **código/status**, nunca pelo texto.
3. **Função de mensagem do login em um lugar só.** `mensagemDoLogin(erro: unknown): string` em `src/api/erro.ts` (ao lado de `mensagemDoErro`), com tabela de decisão:

   | Condição | Mensagem |
   |---|---|
   | `codigo === CODIGO_PERFIL_NAO_PARTICIPANTE` | "Este aplicativo é exclusivo para participantes. Use a aplicação web." |
   | `status === 401` | "E-mail ou senha inválidos, ou conta desativada." |
   | `codigo === CODIGO_SEM_CONEXAO` | "Não foi possível conectar. Verifique sua conexão e tente de novo." |
   | qualquer outra falha (5xx, 400, erro de JS) | "Não foi possível entrar agora. Tente novamente." |

   Assim nunca aparece JSON cru nem a URL da API para o usuário.
4. **Aviso de sessão expirada no contexto.** `SessaoProvider` ganha `aviso: string | null`. O callback de `definirAoExpirarSessao` só define "Sua sessão expirou. Entre novamente." quando **havia usuário logado**; `entrar` limpa o aviso ao começar. `login.tsx` exibe o aviso acima do formulário (estilo informativo, distinto do erro). Não exibir o aviso na restauração com token inválido na abertura do app (o usuário não estava em sessão).
5. **Corrida no login.** Se o `GET /api/auth/me` logo após o login responder 401, o callback global dispararia o aviso de expiração. Proteger com uma flag: durante `entrar`, o callback global é ignorado e o erro segue para o `catch` do login (mensagem de credencial).
6. **Arquivos de teste fora de `src/app/`.** O guia manda criar `src/app/__tests__/login.test.tsx`, mas o Expo Router trata tudo em `src/app/` como rota, e um arquivo de teste ali entra no bundle. Colocar em `src/telas/__tests__/login.test.tsx` (e `inicio.test.tsx`, `perfil.test.tsx`) e registrar o desvio na seção de testes. Confirmar com `npx expo export --platform web` que nenhum arquivo de teste vira rota.
7. **Mocks cobrem os dois cenários.** O modo `EXPO_PUBLIC_USE_MOCKS=true` só aceita a conta demo, que sempre tem estudos. Adicionar uma segunda conta de mock sem vínculo (`participante@clinicalstudy.test`, mesma senha) cujo `GET /api/me/estudos` responde `[]`, espelhando o seed real (P11). Contas de outros perfis também entram no mock (`/api/auth/me` devolve o perfil conforme o token) para testar o RQ-05 sem backend.

## 3. Passos de implementação

Ordem pensada para que cada passo deixe o app compilando e os testes verdes.

### Passo 0 · Branch e linha de base (10 min)

- Criar `feat/task110-login-logout-app` a partir da branch atual (a T109 ainda não está na `main`; o T110 usa os endpoints `/me`).
- Rodar `npm test`, `npm run lint`, `npm run typecheck` em `mobile/ClinicalStudyPB` e anotar a linha de base.
- Commitar ou ignorar o `docs/tutorial-inicializacao.md` (hoje não versionado) fora deste PR.

### Passo 1 · Erros e mensagens (`src/api/erro.ts`) (20 min)

- Adicionar `CODIGO_PERFIL_NAO_PARTICIPANTE` e `mensagemDoLogin` (decisão 3).
- Testes em `src/api/__tests__/erro.test.ts`: um caso por linha da tabela, mais "erro desconhecido" e "string solta".

### Passo 2 · Sessão (`src/contexts/sessao.tsx`) (30 min)

- Lançar `new ErroApi(MENSAGEM, { codigo: CODIGO_PERFIL_NAO_PARTICIPANTE })` na recusa (mantendo o texto atual).
- Adicionar `aviso` ao tipo `Sessao` e ao `value`; callback de expiração define o aviso apenas se houver usuário (usar `useRef` para o usuário atual, para o callback não ficar com valor antigo); `entrar` limpa o aviso e usa a flag da decisão 5.
- `sair`: limpar o aviso e usar `try/finally` para que `setUsuario(null)` aconteça mesmo se `SecureStore.remover` falhar.
- Ampliar `src/contexts/__tests__/sessao.test.tsx`:
  - restauração com **403** e **404** descarta o token;
  - restauração com API fora do ar (`SEM_CONEXAO`) **mantém** o token e deixa `usuario` nulo;
  - recusa de `ADMINISTRADOR`, `PESQUISADOR` e `COORDENADOR` (`it.each`), com token removido e código `PERFIL_NAO_PARTICIPANTE`;
  - 401 fora do login define o `aviso`; 401 na restauração **não** define; `entrar` limpa o `aviso`;
  - `sair` limpa usuário e aviso mesmo quando `remover` rejeita.

### Passo 3 · Tela de login (`src/app/login.tsx`) (40 min)

- Trocar `setErro(falha.message)` por `setErro(mensagemDoLogin(falha))`.
- Limpar `erro` em `onChangeText` dos dois campos (RQ-01).
- `ref` no campo de senha; e-mail com `returnKeyType="next"` e `onSubmitEditing={() => senhaRef.current?.focus()}`; senha com `returnKeyType="go"`, `textContentType="password"`; e-mail com `textContentType="emailAddress"` e `autoCorrect={false}`.
- `enviar` retorna cedo se `enviando` for verdadeiro (cobre o Enter do teclado durante o envio).
- Exibir `aviso` da sessão (RQ-09) com `accessibilityRole="alert"`.
- Envolver o formulário em `ScrollView` com `keyboardShouldPersistTaps="handled"` dentro do `KeyboardAvoidingView` (também no Android, via `behavior="height"` ou `adjustResize` em `app.json`), para a rolagem com teclado aberto (RQ-11).
- Manter o texto "Sistema acadêmico — utiliza apenas dados fictícios." (RQ-04).

### Passo 4 · Cliente `estudosApi` (`src/api/estudos.ts`, novo) (15 min)

- `export const estudosApi = { listar: () => requisitar<MeuEstudoResponse[]>('/api/me/estudos') }` usando o tipo de `schema-sprint3.d.ts` (P13: o nome do arquivo é `schema-sprint3.d.ts`, não `schema.d.ts`).
- Lista vazia **não é erro**. A T112 reaproveita o mesmo módulo e acrescentará `detalhar(estudoId)`; não antecipar isso aqui.
- Teste em `src/api/__tests__/estudos.test.ts` com mocks ligados: lista da conta demo e lista vazia da conta sem vínculo.

### Passo 5 · Início com aviso de "sem estudo" (`src/app/(tabs)/index.tsx`) (30 min)

- Buscar `estudosApi.listar()` ao montar. Estados: carregando (`Carregando`), erro (`EstadoErro` com "Tentar de novo"), vazio (aviso "Você ainda não está incluído em nenhum estudo." via `EstadoVazio`) e com estudos (comportamento atual, sem o aviso).
- Reaproveitar `src/components/Carregando.tsx`, `EstadoErro.tsx` e `EstadoVazio.tsx`; não criar componentes novos.
- A tela de Estudos (`estudos.tsx`) segue como está; o aviso na lista de Estudos é da T112 (o RQ-T110-06 cita "e na lista de Estudos, T112").

### Passo 6 · Perfil (`src/app/(tabs)/perfil.tsx`) (10 min)

- Já mostra nome, e-mail, "Participante" e **Sair**. Só confirmar que o botão chama `sair` sem argumentos e adicionar teste. Sem confirmação em diálogo (fora de escopo).

### Passo 7 · Testes de componente (`src/telas/__tests__/`) (50 min)

`login.test.tsx` (Jest + Testing Library, `useSessao` simulado com `jest.mock('@/contexts/sessao')`):

1. e-mail e senha vazios → "Informe o e-mail e a senha." e `entrar` **não** é chamado;
2. e-mail sem formato → "Informe um e-mail válido.";
3. o erro some ao digitar de novo;
4. durante o envio, o botão mostra "Entrando…", fica desabilitado e um segundo toque não chama `entrar` outra vez (promessa pendente controlada);
5. 401 → "E-mail ou senha inválidos, ou conta desativada." (e **não** o texto do servidor);
6. `SEM_CONEXAO` → "Não foi possível conectar. Verifique sua conexão e tente de novo.";
7. erro 500 e `Error` comum → mensagem genérica, sem JSON nem URL;
8. recusa de `ADMINISTRADOR`, `PESQUISADOR`, `COORDENADOR` (`it.each`) → "Este aplicativo é exclusivo para participantes. Use a aplicação web.";
9. mostrar/ocultar senha alterna `secureTextEntry`;
10. exibe o `aviso` de sessão expirada quando presente;
11. mantém o aviso de dados fictícios.

`inicio.test.tsx`: lista vazia → aviso; lista com estudos → sem aviso; falha → estado de erro e nova tentativa.
`perfil.test.tsx`: mostra nome, e-mail e "Participante"; **Sair** chama `sair`.

### Passo 8 · Conferência automática (10 min)

- `npm test` (com cobertura, como na T108), `npm run lint`, `npm run typecheck`. Corrigir antes de seguir.
- **Se algum teste existente falhar** (por exemplo, `mocks.test.ts` ao mudar o mock de contas), registrar a falha em `docs/testes/registro-de-defeitos.md` **antes** de corrigir e nunca excluir o teste sem registro.

### Passo 9 · Validação em emulador Android e celular físico (RQ-10 e RQ-11) (40 min)

Preparação:
1. Backend no perfil `demo` em banco limpo, na porta 8081 (contas: `demo.participante@clinicalstudy.test` com estudo; `participante@clinicalstudy.test` sem vínculo; um usuário de cada perfil web da seed V2 para a recusa).
2. `.env.local` do app: `EXPO_PUBLIC_API_URL=http://10.0.2.2:8081` no emulador; `http://<IP-da-máquina>:8081` no celular (mesma rede Wi-Fi; liberar a porta 8081 no Firewall do Windows).
3. `npm run android` (Metro na 8082). No Expo Go o tráfego HTTP em texto puro funciona; se for usado dev build, habilitar `usesCleartextTraffic` por config plugin (nunca editar `android/` à mão).

Roteiro (cada linha com captura ou gravação):

| # | Cenário | Resultado esperado |
|---|---|---|
| 1 | Campos vazios → Entrar | "Informe o e-mail e a senha." |
| 2 | E-mail malformado | "Informe um e-mail válido." |
| 3 | Senha errada | "E-mail ou senha inválidos, ou conta desativada." |
| 4 | Admin, pesquisador e coordenador | Recusa; ao reabrir o app continua no login (token removido) |
| 5 | `demo.participante` | Entra; Início lista o estudo; Perfil mostra os dados |
| 6 | `participante@clinicalstudy.test` | Entra; Início mostra "Você ainda não está incluído em nenhum estudo." |
| 7 | Fechar e reabrir o app logado | Carregando, depois abas (sessão restaurada) |
| 8 | Parar o backend e reabrir | Não derruba o token; ao subir de novo, restaura |
| 9 | Invalidar o token (reiniciar backend com outro segredo JWT) e chamar uma tela autenticada | Volta ao login com "Sua sessão expirou. Entre novamente." |
| 10 | Perfil → Sair → botão Voltar do sistema | Volta ao login e Voltar **não** reabre as abas |
| 11 | Teclado aberto (e-mail e senha) | Botão Entrar alcançável por rolagem; Enter no e-mail vai para senha; Enter na senha envia |
| 12 | Área segura e contraste (entalhe, barra de navegação) | Sem texto cortado; contraste legível |

- Salvar as capturas em `docs/design/sprint3/capturas/t110/` (ou o diretório de capturas que a T114 definir) e registrar defeitos de plataforma como Bug no Azure Boards, com link no relatório.

### Passo 10 · Documentação e rastreabilidade (30 min)

- `docs/testes/testes-t110-login-e-logout-no-app.md` no mesmo formato do `testes-t108-esqueleto-do-app-mobile.md` (escopo, como executar, resultados, cobertura, matriz RQ × teste, lacunas, falhas encontradas).
- Marcar a T110 no `docs/design/sprint3/README.md` e atualizar o item "T110" das pendências da T109 (P11 e P12 atendidas).
- Falhas encontradas durante a execução: registrar em `registro-de-defeitos.md` e, para a rastreabilidade de erros, seguir a regra do projeto de usar **branch de documentação própria** (P23).
- Gravar os documentos com as ferramentas de escrita de arquivos, evitando heredoc e crases no shell.

## 4. Mapa requisito × entrega

| RQ | Entregue em | Verificação |
|---|---|---|
| 01 | Passo 3 | `login.test` casos 1 a 3 |
| 02 | Passo 3 | `login.test` casos 4 e 9; roteiro 11 |
| 03 | Passos 1 e 3 | `erro.test`; `login.test` casos 5 a 7; roteiro 3 |
| 04 | Passo 3 | `login.test` caso 11 |
| 05 | Passos 1 e 2 | `sessao.test` (3 perfis); `login.test` caso 8; roteiro 4 |
| 06 | Passos 4 e 5 | `estudos.test`; `inicio.test`; roteiro 6 |
| 07 | Passo 2 | `sessao.test` (401, 403, 404, rede); roteiro 7 e 8 |
| 08 | Passos 2 e 6 | `perfil.test`; `sessao.test` (sair); roteiro 10 |
| 09 | Passos 2 e 3 | `sessao.test` (aviso); `login.test` caso 10; roteiro 9 |
| 10 e 11 | Passo 9 | Capturas e roteiro |
| 12 | Passo 7 | `npm test` |

## 5. Riscos e cuidados

- **Não distinguir "desativada" de "senha errada".** A mensagem cobre os três casos de propósito; não tentar inferir pelo texto do servidor.
- **Não guardar senha no dispositivo**; só o token, no `SecureStore` (já é assim; não mudar `armazenamento.ts`).
- **Corrida do 401 no login** (decisão 5): sem a flag, uma senha errada poderia mostrar "sessão expirada".
- **Testes dentro de `src/app/`** viram rotas (decisão 6).
- **Mensagem de rede com URL.** Hoje `cliente.ts` inclui a URL na mensagem de `SEM_CONEXAO`; ela continua útil nos logs de desenvolvimento, mas não deve chegar à tela de login. Outras telas (T112, T119) podem continuar usando `mensagemDoErro`.
- **Escopo:** sem cadastro, "esqueci a senha", biometria nem confirmação de logout. As telas de Estudos e Visitas continuam "em breve" (T112, T117, T118).
- **A T109 ainda não está na `main`.** O PR da T110 deve apontar para onde a T109 for integrada, ou esperar o merge, para não carregar os commits dela duas vezes.

## 6. Critérios de aceite (conferência final)

- [ ] O participante entra, sai e tem a sessão restaurada; admin, pesquisador e coordenador são recusados com a mensagem certa e sem token salvo.
- [ ] O participante sem estudo entra e vê "Você ainda não está incluído em nenhum estudo.".
- [ ] A expiração do token leva ao login com "Sua sessão expirou. Entre novamente.".
- [ ] `npm test`, `npm run lint` e `npm run typecheck` passam.
- [ ] Roteiro do Passo 9 executado em emulador ou celular real, com evidências no repositório.
- [ ] Documento de testes e README da Sprint 3 atualizados.

## 7. Estimativa

| Bloco | Tempo |
|---|---|
| Passos 0 a 2 (base, erros, sessão) | 1h00 |
| Passos 3 a 6 (login, API, início, perfil) | 1h35 |
| Passos 7 e 8 (testes e conferência) | 1h00 |
| Passo 9 (dispositivo) | 0h40 |
| Passo 10 (documentação) | 0h30 |
| **Total** | **~4h45** |

A estimativa do planejamento é 3h. Os 1h45 a mais vêm do aviso de sessão expirada, dos testes de componente e da validação em dispositivo. Se o prazo (sex 20/11) apertar, cortar nesta ordem: testes de `inicio` e `perfil` (cobrir só pelo roteiro manual), depois a segunda conta de mock; nunca cortar o Passo 9, que é critério de aceite.
