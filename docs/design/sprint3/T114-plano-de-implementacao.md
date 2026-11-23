# T114 · Plano de implementação: E2E mobile (Maestro), capturas e documentação

> **Sprint 3** · PBI-24 · RF17 (RF02 e RF03 no mobile) · **Estimativa do planejamento:** 2h · **Depende de:** T110, T112 (e, para os dados, T111)
> Guia da task: [T114-e2e-mobile-maestro-capturas-e-documentacao.md](T114-e2e-mobile-maestro-capturas-e-documentacao.md) (12 requisitos `RQ-T114-NN`). Contrato: [00-convencoes-e-contrato-compartilhado.md](00-convencoes-e-contrato-compartilhado.md). Plano irmão: [T113](T113-plano-de-implementacao.md) (independente; a T114 não precisa dele).
> Caminhos relativos à raiz do repositório, salvo indicação. **MOB** = `mobile/ClinicalStudyPB/`.
> Plano escrito em 22/11/2026. **Nada foi implementado.**

## 0. Leitura rápida

Esta task é a única do PBI-24 que **exige uma ação fora do código**: rodar o fluxo num emulador Android (ou celular) e guardar a evidência. Este ambiente de desenvolvimento **não tem `maestro` nem `adb`** instalados (conferido em 22/11), então o plano separa o que se faz no código e na documentação (feito aqui) do que **precisa da máquina com emulador** (feito pelo responsável, com roteiro pronto).

| Fato verificado em 22/11 | Efeito no plano |
|---|---|
| Login, Meus estudos e detalhe existem na branch `feat/task112-meus-estudos-detalhe` (PR #43), com T110 dentro. T111 está no PR #42. Nenhuma está na `develop` | Pré-condições E1 e E2 |
| O login tem `accessibilityLabel` em **E-mail** e **Senha**; a lista tem `testID="lista-estudos"` e cartões com `accessibilityRole="button"` e rótulo "título, status e data"; as abas têm título (Início, Estudos, Visitas, Perfil); o botão **Sair** é o componente `Botao` (`ui.tsx`), **sem `testID`** | RQ-02: poucos identificadores faltam (decisão 3) |
| `app.json` tem `scheme: "clinicalstudypb"` e **não** tem `android.package` nem `ios.bundleIdentifier` | Decisão 1 (Expo Go × development build) |
| Contas reais do seed: `participante@clinicalstudy.test` (perfil `demo`, vinculada ao **P1** pela T111) e `coordenador@clinicalstudy.test` (V2, existe em todos os perfis). Estudo de demonstração: **"Demo - Acompanhamento de participantes"** (execução) | RQ-01 e RQ-03 usam estes dados; o status exato exibido para P1 é conferido no Passo 2 |
| `docs/imagens/sprint3/` já existe (capturas do RF15 e do fluxo de acompanhamento da web); não há `docs/sprint3/e2e-mobile-evidencia.md` nem `.maestro/` | Criar |
| No perfil `demo`, `participante@` **deixou de ser a conta sem vínculo** (T111) | O fluxo base depende da T111 estar no backend usado. Para a lista vazia, outra conta |

### Pré-condições de entrada

| # | Pré-condição | Como conferir | Se não estiver pronta |
|---|---|---|---|
| E1 | T110 e T112 mergeadas (#38, #43) | `gh pr view 43 --json state` | Criar a branch a partir de `origin/feat/task112-meus-estudos-detalhe` |
| E2 | T111 no backend que o emulador usa (#42), para `participante@` ter estudo | `GET /api/me/estudos` com a conta devolve 1 estudo | Mesclar `origin/feat/task111-vinculo-conta-participante` na branch local do backend |
| E3 | Máquina com **Java 17+**, **Android Studio com um AVD**, **Maestro CLI** e o backend em perfil `demo` | `java -version`, `adb devices`, `maestro --version` | **Bloqueia só o RQ-05** (rodar e gravar); o resto (arquivos, README, documento) segue |

## 1. Decisões de projeto

1. **Como o app abre no emulador (appId).** O guia prevê **Expo Go** (`appId: host.exp.exponent`, abrir por link `exp://`). Risco real: o Expo Go mostra telas próprias na primeira abertura (menu do desenvolvedor, aviso de versão), que o fluxo teria de contornar, e o `clearState` apaga também o estado do Expo Go. Alternativa mais estável: **development build** (`npx expo run:android`), que exige `android.package` no `app.json` (ex.: `br.edu.uepb.clinicalstudypb`) e dá ao Maestro um `appId` próprio. **Padrão do plano: Expo Go, como o guia manda.** Se o Passo 4 mostrar instabilidade, trocar pelo development build e registrar em `e2e-mobile-evidencia.md` com o motivo. Alterar o `app.json` é mudança de configuração do app: decidir antes, não no meio do fluxo.
2. **Backend e URL.** Perfil `demo` (`SPRING_PROFILES_ACTIVE=demo`) e `EXPO_PUBLIC_API_URL=http://10.0.2.2:8081` no emulador. **Sem mocks:** o E2E prova o app contra a API real (o app em modo mock não serve de evidência).
3. **Identificadores, sem coordenadas (RQ-02).** Primeiro tentar **texto e rótulos acessíveis** que já existem (`E-mail`, `Senha`, `Entrar`, abas, `Sair`, cartão pelo título do estudo). Só onde o Maestro não achar de forma estável, acrescentar `testID` **sem mudar comportamento**: candidatos, `Botao` aceitar `testID` opcional e repassar ao `Pressable`; `testID` no título do detalhe e no botão de mostrar/ocultar senha. Cada `testID` entra com teste de componente trivial (existe e foi repassado) para o `npm test` continuar verde.
4. **Repetibilidade (RQ-04).** Todo fluxo começa com `launchApp` com `clearState: true` e termina no login (logout), para poder rodar de novo. Nenhum fluxo cria dado: só lê o seed `demo`.
5. **CI com emulador fica fora** (decisão do Planning, T94). O fluxo roda local com evidência; a decisão de CI vai para a Sprint 4 e fica escrita no documento de evidência.
6. **Segredos e dados.** Só contas fictícias do seed (`senha-de-teste`). Nenhuma captura deve mostrar dados reais.
7. **Passagem de bastão (RQ-12).** Avisar I5 (PBI-25) no PR: esqueleto (T108), `/me` (T109), vínculo (T111) e fluxo base (T114). Ação humana.

## 2. Passos de implementação

### Passo 0 · Branch e linha de base (10 min)

- Conferir E1 e E2. Branch `feat/task114-e2e-mobile-maestro`, a partir do que a E1 indicar.
- MOB: `npm ci`, `npm test`, `npm run lint`, `npm run typecheck`; anotar a linha de base (esperado: 129 testes). Deixar `docs/tutorial-inicializacao.md` fora do PR.

### Passo 1 · Mapear os identificadores (20 min) (RQ 02)

- Rodar o app em modo mock **só para inspecionar** a hierarquia (`maestro studio` no emulador, ou `adb shell uiautomator dump`), e anotar, por tela, como cada elemento é achado: login (E-mail, Senha, Entrar, mensagem de erro de perfil), abas, cartão do estudo, título e status do detalhe, voltar, Perfil → Sair.
- Decidir quais precisam de `testID` (decisão 3). Se este passo exigir o emulador (E3), fazer junto do Passo 4.

### Passo 2 · Conferir os dados do seed (10 min) (RQ 01)

- Subir o backend em perfil `demo` e consultar `GET /api/me/estudos` e `GET /api/me/estudos/{id}` com `participante@clinicalstudy.test`. Anotar **título e status exatos** que o app mostra para o P1; o fluxo confere esses textos.
- Registrar a conta recusada (`coordenador@clinicalstudy.test`) e a mensagem "exclusivo para participantes" como o app a exibe hoje.

### Passo 3 · Ajustes mínimos de código (20 min) (RQ 02)

- MOB `src/components/ui.tsx`: `Botao` aceita `testID?` e repassa. Telas: `testID` só nos pontos do Passo 1 que precisarem.
- Teste de componente para cada `testID` novo (`getByTestId`) em `src/telas/__tests__/`. `npm test`, `lint` e `typecheck` verdes.
- Se o Passo 1 mostrar que não falta nada, **pular** este passo (nada a alterar).

### Passo 4 · Escrever os fluxos Maestro (40 min) (RQ 01, 03, 04)

- MOB `.maestro/fluxo-participante.yaml`: `launchApp` com `clearState`; aguardar a tela de login; preencher e-mail e senha; tocar em Entrar; confirmar a aba Estudos; abrir Meus estudos; abrir o detalhe (conferindo o título e o status do Passo 2); voltar; Perfil → Sair; confirmar o retorno ao login. Cada etapa com `assertVisible`, e `takeScreenshot` nos pontos de captura (login, lista, detalhe).
- MOB `.maestro/login-perfil-recusado.yaml` (**opcional**, RQ-03): login com `coordenador@clinicalstudy.test`; `assertVisible` da mensagem "exclusivo para participantes" e `takeScreenshot`. Mesma abertura com `clearState`.
- Se for Expo Go: o `openLink` do `exp://<IP>:8082` entra no início do fluxo (variável de ambiente do Maestro para o endereço). Se for development build: `appId` próprio. O ponto de troca fica numa só linha de cada arquivo.
- **Nada de coordenadas** de tela (RQ-02).

### Passo 5 · Executar localmente e guardar a evidência (30 min) (RQ 05, 07) · **exige E3**

- Subir o backend (`demo`), o Expo (`npm start`, porta 8082) e o emulador; rodar `maestro test .maestro/fluxo-participante.yaml` e `maestro test .maestro/login-perfil-recusado.yaml`.
- Guardar a **evidência**: `maestro record` (vídeo) ou as capturas do `takeScreenshot`, e o texto da saída com o fluxo aprovado. Rodar **duas vezes seguidas** para provar a repetibilidade (RQ-04).
- Anotar problemas (timing, diálogos do Expo Go) e abrir **Bug no Azure Boards** para defeitos de plataforma (RQ-07). Ação humana.
- Se o emulador não estiver disponível, a task **não pode ser dada como concluída**: o RQ-05 e o critério de aceite 1 ficam abertos e registrados como lacuna.

### Passo 6 · Capturas (15 min) (RQ 08)

- Salvar em `docs/imagens/sprint3/`: `rf17-login-mobile.png`, `rf17-login-perfil-recusado.png`, `rf17-meus-estudos.png`, `rf17-detalhe-estudo.png` (nomes do guia). Conferir que não mostram dado real.

### Passo 7 · Documentação (30 min) (RQ 06, 09, 10)

- `docs/sprint3/e2e-mobile-evidencia.md`: comando usado, versões (Maestro, emulador/AVD, Expo SDK 58), data, `appId` escolhido e por quê (decisão 1), link ou arquivo da evidência, resultado das duas execuções, problemas encontrados, e a decisão de **CI com emulador fora** (Sprint 4).
- MOB `README.md`: seção "E2E com Maestro": instalar o Maestro, pré-requisitos (Java, AVD), variáveis (`EXPO_PUBLIC_API_URL=http://10.0.2.2:8081`), subir o backend em `demo`, comando `maestro test .maestro/fluxo-participante.yaml`, como gravar.
- `README.md` (raiz): RF17 e login mobile (RF02 e RF03 no mobile) com comportamento, regras (só o participante entra; só vê os próprios dados), erros e as capturas, no formato das seções dos outros RF.
- `docs/design/sprint3/README.md`: marcar a T114; se o emulador não rodou, marcar 🟡 com a lacuna.

### Passo 8 · Fechamento (15 min) (RQ 11, 12)

- Zerar o **Remaining Work** das tasks do PBI-24 e registrar os Bugs no Azure Boards. **Ação humana** (sem acesso daqui).
- Avisar **I5** no PR (decisão 7). **Ação humana.**
- Conferir `npm test`, `lint`, `typecheck` do app e que as suítes web e de backend não foram afetadas (esta task não toca neles).

## 3. Mapa requisito × entrega

| RQ | Entregue em | Verificação |
|---|---|---|
| 01 | Passos 2 e 4 | `maestro test` aprovado (login, estudos, detalhe com título e status, logout) |
| 02 | Passos 1, 3 e 4 | Nenhuma coordenada nos `.yaml`; testes dos `testID` novos |
| 03 | Passo 4 | `login-perfil-recusado.yaml` aprovado (opcional) |
| 04 | Passos 4 e 5 | `clearState` no início; duas execuções seguidas aprovadas |
| 05 | Passo 5 | Gravação ou capturas e saída do comando (**exige emulador**) |
| 06 | Passo 7 | `docs/sprint3/e2e-mobile-evidencia.md` |
| 07 | Passo 5 | Bugs no Azure Boards (humano) |
| 08 | Passo 6 | Quatro imagens em `docs/imagens/sprint3/` |
| 09 | Passo 7 | `README.md` com RF17 e login mobile |
| 10 | Passo 7 | `README` do app com Maestro |
| 11 | Passo 8 | Azure Boards (humano) |
| 12 | Passo 8 | Aviso à I5 no PR (humano) |

## 4. Inventário de arquivos (rastreabilidade do que será tocado)

| Ação | Arquivo | Passo | RQ |
|---|---|---|---|
| Criar | `mobile/ClinicalStudyPB/.maestro/fluxo-participante.yaml` | 4 | 01, 02, 04 |
| Criar (opcional) | `mobile/ClinicalStudyPB/.maestro/login-perfil-recusado.yaml` | 4 | 03 |
| Alterar (só se faltar identificador) | MOB `src/components/ui.tsx`, telas e testes em `src/telas/__tests__/` | 3 | 02 |
| Alterar (só se a decisão 1 mudar) | MOB `app.json` (`android.package`) | 1 | 01 |
| Criar | `docs/imagens/sprint3/rf17-login-mobile.png`, `rf17-login-perfil-recusado.png`, `rf17-meus-estudos.png`, `rf17-detalhe-estudo.png` | 6 | 08 |
| Criar | `docs/sprint3/e2e-mobile-evidencia.md` | 7 | 05, 06 |
| Alterar | `mobile/ClinicalStudyPB/README.md`, `README.md`, `docs/design/sprint3/README.md` | 7 | 09, 10 |

Nenhuma alteração em backend nem web.

## 5. Riscos e cuidados

- **Sem emulador não há evidência** (RQ-05): é o único requisito que depende de ambiente. Não declarar a task concluída sem ele.
- **Expo Go instável para E2E** (diálogos de primeira abertura, `clearState` do próprio Expo Go): ver decisão 1; plano de troca já escrito.
- **`participante@` mudou de papel** (T111): no `demo` ela já tem estudo; se o backend usado não tiver a T111, o fluxo cai no estado "lista vazia". Conferir E2 antes de culpar o app.
- **Texto exato do status** depende do seed: conferir no Passo 2 em vez de presumir.
- **Timing:** preferir `assertVisible` com espera a pausas fixas; registrar instabilidade em vez de aumentar tempo às cegas.
- **Maestro no Windows:** exige Java e o AVD no `PATH`; se der problema de plataforma, registrar como Bug (RQ-07), não contornar com coordenadas.
- **Escopo:** sem fluxo de visitas (T121, I5), sem CI com emulador, sem teste de iOS.

## 6. Estimativa

| Bloco | Tempo |
|---|---|
| Passos 0 a 3 (base, mapa, seed, ajustes) | 1h00 |
| Passo 4 (fluxos) | 0h40 |
| Passo 5 (execução e evidência, no emulador) | 0h30 |
| Passos 6 a 8 (capturas, documentação, fechamento) | 1h00 |
| **Total** | **~3h10** |

O planejamento prevê 2h; os mapeamentos e a documentação do Expo Go explicam a diferença. Se apertar, cortar nesta ordem: `login-perfil-recusado.yaml` (RQ-03 é opcional), a gravação em vídeo (ficar só com as capturas), os `testID` desnecessários. **Nunca cortar:** o fluxo base aprovado em emulador, o `clearState`, o documento de evidência e o aviso à I5. O prazo de 21/11 já passou: combinar o novo prazo com o grupo.

## 7. Critérios de aceite (conferência final)

- [ ] Existe um fluxo E2E mobile (login, meus estudos, detalhe e logout) que passa em emulador, com evidência guardada e duas execuções seguidas.
- [ ] As capturas do login, da recusa de outro perfil e dos estudos estão no repositório, e o README descreve o RF17 e o login mobile.
- [ ] O Remaining Work do PBI-24 está zerado, os Bugs registrados e a I5 avisada.

## 8. Índice de rastreabilidade deste plano

| Pergunta | Onde achar |
|---|---|
| De onde vem cada requisito? | Guia [T114](T114-e2e-mobile-maestro-capturas-e-documentacao.md), seção 4 (`RQ-T114-01` a `12`) e seção 9 (especificação: RF17, RF02, RF03; "pelo menos um fluxo E2E no mobile") |
| Qual passo entrega cada requisito? | Seção 3 (mapa RQ × entrega) |
| Quais arquivos serão tocados? | Seção 4 |
| O que depende de ação humana ou de emulador? | Passos 5 e 8; decisão 7; seção 5 |
| Onde registrar a evidência? | `docs/sprint3/e2e-mobile-evidencia.md` e `docs/imagens/sprint3/` |
| Onde registrar falhas de teste? | `docs/testes/registro-de-defeitos.md`, linhas `D-T114-NN`, **antes** de corrigir |
