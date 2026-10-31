# T65 — Requisitos e critérios de aceite (E2E com Playwright)

Oct 31, 2026 · @Carlos André

## Escopo da T65

A T65 entrega a infraestrutura de testes E2E com Playwright na aplicação web e um primeiro teste (login válido, login inválido e logout) rodando no CI. Ela depende só do login web da Sprint 1 (T21), vale 3 horas e é responsabilidade de I7. Este documento lista tudo o que precisa existir para ela ser considerada pronta.

|  | O que é |
| --- | --- |
| Entra na T65 | Instalação e configuração do Playwright; usuários de teste; login por API com sessão reaproveitada por perfil; page object da tela de login; teste E2E de login válido, inválido e logout; job do GitHub Actions com PostgreSQL, backend e web; comando documentado no README |
| Fica de fora | Fluxo completo da Release 1 (T66), dados de demonstração (T63), bloqueio de 80% de cobertura (T64), E2E do aplicativo mobile (Sprint 3) e page objects das demais telas |
| Entrega para a T66 | Login por API por perfil, estrutura de pastas e fixtures, configuração do CI funcionando e convenções de escrita dos testes, de modo que a T66 só acrescente page objects e cenários |

Por que ela existe: a especificação do projeto exige pelo menos um fluxo E2E automatizado no sistema web e a execução automatizada dos testes a cada pull request ou integração à branch principal. A T65 cobre a base disso, e a T66 completa o fluxo.

## Pré-requisitos a confirmar antes de começar

Seis coisas da Sprint 1 decidem como a T65 é escrita; confirme cada uma antes de abrir o editor. Eu só vi o plano da Sprint 1 e a especificação, não o código, então os detalhes marcados como "anotar" precisam ser conferidos no repositório.

- [ ] **Login web (T21) na branch principal e funcionando:** credenciais válidas entram, inválidas são rejeitadas e o logout encerra o acesso na interface (critério de aceite do PBI-05). Sem isso, não há o que testar.
- [ ] **Endpoint de login do backend (T20):** anotar método, caminho, corpo da requisição e formato da resposta (o plano fala em autenticação por token). É ele que o login por API vai chamar.
- [ ] **Onde o front guarda a autenticação:** anotar se o token fica em localStorage, sessionStorage, cookie ou só em memória. Isso decide como a sessão será reaproveitada (veja o risco 1 no fim do doc).
- [ ] **Como subir backend e web localmente:** anotar a porta do Spring Boot e a do Vite, as variáveis de ambiente e a conexão com o PostgreSQL, conforme o README (T04) e o workflow da T05.
- [ ] **Perfis que fazem login na web:** pesquisador, coordenador e administrador. O participante usa o aplicativo mobile, cujo login está previsto para sprints posteriores, então fica fora da T65. Anotar também para qual tela cada perfil é levado depois de entrar.
- [ ] **Textos reais da tela de login:** rótulos dos campos, texto do botão, mensagem de erro e botão de sair. Os seletores dos testes saem daqui.

Também confira se o workflow da T05 já sobe o backend e o web, para reaproveitar o que existir em vez de duplicar.

## Requisitos e critérios de aceite

São 15 requisitos; a T65 só está pronta quando todos os critérios abaixo forem verificáveis na branch principal. Os nomes de pastas e scripts são sugestões: se o projeto já tiver outra convenção, mantenha a dele e preserve o critério.

| ID | Requisito | Critério de aceite |
| --- | --- | --- |
| RQ-01 | Playwright instalado na aplicação web, com versão fixa e scripts npm | Em um clone limpo, `npm ci` e `npx playwright --version` funcionam; a versão está no `package-lock.json`; existem os scripts `test:e2e` (roda tudo), `test:e2e:headed` (com navegador visível) e `test:e2e:report` (abre o relatório) |
| RQ-02 | `playwright.config.ts` configurado | Define o diretório dos testes, o navegador Chromium, a URL base vinda de variável de ambiente, timeouts, `retries` só no CI (0 localmente), trace na primeira repetição, captura de tela e vídeo só em falha, e relatórios HTML e de lista. Uma falha gera esses artefatos; um teste instável é repetido no CI |
| RQ-03 | Configuração por variáveis de ambiente | URL do front, URL da API e credenciais de cada perfil vêm de variáveis; existe `.env.e2e.example` versionado com valores fictícios e `.env.e2e` fica no `.gitignore`; sem as variáveis obrigatórias o teste falha com mensagem clara dizendo qual falta |
| RQ-04 | Usuários de teste reproduzíveis | Existe ao menos um usuário por perfil com login web (pesquisador, coordenador, administrador), criado automaticamente antes da execução por script SQL, seed ou chamada de API. Rodar duas vezes seguidas não falha por duplicidade; as senhas ficam como hash no banco; nada disso existe fora do ambiente de teste |
| RQ-05 | Login por API com sessão reaproveitada por perfil | Uma etapa de setup chama o endpoint de login (T20), guarda o estado de autenticação de cada perfil em `e2e/.auth/<perfil>.json` e roda a cada execução, para nunca usar sessão vencida. Um teste de exemplo abre uma rota protegida já autenticado, sem passar pela tela de login, para cada perfil; os arquivos de sessão estão no `.gitignore` |
| RQ-06 | Page object da tela de login | Existe `LoginPage` com localizadores por papel ou rótulo (`getByRole`, `getByLabel`) e métodos como `goto()`, `login(email, senha)`, `expectLoggedIn()` e `expectErrorMessage()`. Existe também uma ação de logout (no page object da barra de navegação ou no mesmo). Os testes não têm seletores soltos: só chamam o page object |
| RQ-07 | Teste de login válido | Entrar com credenciais válidas leva à tela autenticada esperada para o perfil (URL e elemento visível conferidos) e deixa o estado de autenticação presente |
| RQ-08 | Teste de login inválido | Com senha errada, a mensagem de erro aparece, o usuário continua na tela de login e nenhuma sessão é criada. Repita com um e-mail inexistente e confira que a mensagem não revela qual dos dois campos está errado; se revelar, registre um Bug no Azure Boards |
| RQ-09 | Teste de logout | Depois de entrar, acionar “sair” volta à tela de login; abrir uma rota protegida redireciona ao login; o botão Voltar do navegador não reabre a área autenticada (critério do PBI-05: o logout encerra o acesso na interface) |
| RQ-10 | Testes independentes e estáveis | Cada teste usa contexto novo e não depende da ordem; não há pausas fixas (`waitForTimeout`), só esperas automáticas e verificações do próprio Playwright. Passa com `--repeat-each=10` e com `--workers=1` e `--workers=4`, sem falha em nenhuma |
| RQ-11 | Job de E2E no GitHub Actions | Roda em `pull_request` e em `push` na branch principal; sobe PostgreSQL como service, backend e web; espera o backend responder (health check) e o web abrir antes de testar; instala o Chromium com `npx playwright install --with-deps chromium`; tem `timeout-minutes` definido. Fica verde na main e fica vermelho quando um teste é quebrado de propósito (verifique isso uma vez) |
| RQ-12 | Artefatos do CI | O `playwright-report` e a pasta `test-results` (com os traces) são enviados com `if: always()` e retenção definida. Em uma falha forçada, dá para baixar o relatório e abrir o trace |
| RQ-13 | Comando documentado no README | Existe a seção “Testes E2E” com pré-requisitos, instalação do navegador, como subir o ambiente local, como rodar tudo e um teste só, modo com navegador e relatório, variáveis e usuários de teste, e como o CI executa. Um colega que não fez a T65 roda os testes seguindo só o README em um clone limpo |
| RQ-14 | Qualidade e segurança do código | O `tsc` e o lint passam; o `.gitignore` cobre `playwright-report/`, `test-results/`, `e2e/.auth/` e `.env.e2e`; não há credencial real em nenhum arquivo, log ou captura; o Vitest dos testes de componente ignora a pasta `e2e/` e não executa os testes do Playwright; o PR é revisado por outro integrante |
| RQ-15 | Base reaproveitável pela T66 | A estrutura é `e2e/pages`, `e2e/fixtures`, `e2e/setup` e `e2e/tests`, com um helper para escolher o perfil. Acrescentar um page object e um teste novos não exige mexer na configuração nem no workflow |

## Checklist de implementação, na ordem de execução

Siga a ordem: cada bloco prepara o seguinte, e o CI só vale a pena depois que tudo passa localmente. As horas são uma divisão sugerida das 3h da task.

### 1. Preparar (cerca de 15 min)

- [ ] Criar a branch da task a partir da main atualizada
- [ ] Confirmar os pré-requisitos da seção anterior e anotar as respostas no PR
- [ ] Subir backend, banco e web localmente e entrar manualmente com um usuário, para ver o fluxo real

### 2. Instalar e configurar (cerca de 35 min) — RQ-01, RQ-02, RQ-03, RQ-14, RQ-15

- [ ] Instalar o Playwright como devDependency na aplicação web e criar os scripts npm
- [ ] Criar a estrutura `e2e/pages`, `e2e/fixtures`, `e2e/setup` e `e2e/tests`
- [ ] Escrever o `playwright.config.ts` (base URL por variável, Chromium, retries só no CI, trace, captura e vídeo em falha, relatórios)
- [ ] Criar `.env.e2e.example` e atualizar o `.gitignore` (`playwright-report/`, `test-results/`, `e2e/.auth/`, `.env.e2e`)
- [ ] Excluir `e2e/` da configuração do Vitest para que ele não execute os testes do Playwright

### 3. Usuários e sessão (cerca de 30 min) — RQ-04, RQ-05

- [ ] Definir como os usuários de teste são criados (seed, SQL ou API do administrador) e fazer isso ser repetível
- [ ] Criar o setup que chama o endpoint de login para cada perfil e salva o estado em `e2e/.auth/<perfil>.json`
- [ ] Criar a fixture que escolhe o perfil e abre uma rota protegida já autenticado
- [ ] Validar com um teste de exemplo para cada um dos três perfis; se o token ficar só em memória, aplicar o plano alternativo do risco 1

### 4. Page object e testes (cerca de 40 min) — RQ-06 a RQ-10

- [ ] Escrever `LoginPage` e a ação de logout com localizadores por papel ou rótulo
- [ ] Teste de login válido (URL, elemento da tela autenticada e estado de autenticação)
- [ ] Teste de login inválido (senha errada e e-mail inexistente)
- [ ] Teste de logout (volta ao login, rota protegida redireciona, Voltar não reabre)
- [ ] Rodar `--repeat-each=10` e com 1 e 4 workers; corrigir o que for instável antes de seguir

### 5. CI (cerca de 35 min) — RQ-11, RQ-12

- [ ] Criar o job `e2e` (ou arquivo de workflow) com gatilhos em `pull_request` e `push` na main
- [ ] Adicionar o PostgreSQL como service, com as mesmas variáveis usadas localmente
- [ ] Subir o backend em segundo plano e esperar o health check; construir e servir o web e esperar a porta abrir
- [ ] Instalar o Chromium com `--with-deps`, executar os testes e enviar relatório e traces com `if: always()`
- [ ] Definir `timeout-minutes` e, se o projeto usar, o cache de dependências
- [ ] Abrir o PR, ver o job verde e depois quebrar um teste de propósito para ver o job vermelho e o trace baixar; desfazer a quebra

### 6. Documentar e fechar (cerca de 25 min) — RQ-13, RQ-14

- [ ] Escrever a seção “Testes E2E” do README
- [ ] Pedir a um colega que siga só o README em clone limpo e rode os testes
- [ ] Conferir que nenhuma credencial real aparece em arquivo, log ou captura
- [ ] Passar `tsc` e lint, pedir a revisão e fazer o merge na main com o CI verde
- [ ] Atualizar a task no Azure Boards (Remaining Work 0, estado Done) e avisar o time de que a T66 já pode usar a base

## Definition of Done e evidências

A T65 só vai para Done quando todos os 15 requisitos estiverem atendidos e as evidências abaixo estiverem anexadas ao PR e à task no Azure Boards. Isso segue a Definition of Done da Sprint 2: critérios atendidos, código revisado por outro integrante, CI verde, PR integrado à main e documentação atualizada.

- [ ] Link do PR aprovado por outro integrante e integrado à branch principal
- [ ] Link de uma execução verde do job de E2E na main
- [ ] Link ou captura da execução vermelha de propósito, com o trace aberto, provando que o CI detecta falhas
- [ ] Relatório HTML do Playwright (captura ou artefato) com os testes de login válido, inválido e logout passando
- [ ] Saída do `--repeat-each=10` sem falhas
- [ ] Link da seção “Testes E2E” do README e o nome de quem a validou em clone limpo
- [ ] Remaining Work em 0 e estado Done na task T65 no Azure Boards, com um comentário apontando a estrutura à T66

Se a task passar das 3 horas estimadas, registre quanto passou e por quê (por exemplo, token só em memória ou ajuste do workflow da T05), porque isso alimenta o burndown e a retrospectiva da Sprint.

## Pontos que costumam ser esquecidos

Sete armadilhas comuns fazem um E2E quebrar no CI ou não ser reaproveitável. Confira cada uma antes de abrir o PR.

| # | Armadilha | Como evitar |
| --- | --- | --- |
| 1 | O token fica só em memória do React: o estado salvo pelo Playwright guarda cookies e localStorage, então a sessão reaproveitada não autentica | Veja onde o front guarda o token (pré-requisitos). Se for em memória, faça o login pela tela uma vez por perfil no setup e salve o estado depois, e combine com I5 guardar o token em storage se isso fizer sentido para a aplicação |
| 2 | Arquivos de sessão ou `.env.e2e` vão parar no Git | Atualize o `.gitignore` no mesmo commit que cria a pasta e confira com `git status` antes do commit |
| 3 | O CI começa os testes antes de o backend e o web estarem prontos | Espere o health check do backend e a porta do web antes de rodar; sem isso o teste falha de forma intermitente |
| 4 | O Vitest executa os arquivos `*.spec.ts` do Playwright e falha | Exclua `e2e/` na configuração do Vitest, ou nomeie os arquivos de forma que ele não os capture |
| 5 | CORS: o web em uma porta chama a API em outra e o navegador bloqueia | Confira a configuração de CORS do backend para a origem usada no CI, ou use o proxy do Vite se o projeto já usar |
| 6 | Banco “sujo”: usuários de teste duplicados na segunda execução, ou bloqueio de conta se o backend limitar tentativas de login | Faça o seed idempotente, use um usuário e e-mail inexistente nos cenários de erro e confira se o backend tem limite de tentativas |
| 7 | Seletores presos ao texto ou à estrutura do HTML quebram quando a tela muda | Use papel e rótulo (`getByRole`, `getByLabel`); só use `data-testid` quando não houver alternativa e combine o nome com I5 e I6, que mantêm as telas |

Por fim, a T65 foi pensada para não conflitar com nenhuma task da Sprint 2, mas a T66 vai reaproveitar tudo. Se na T65 a sessão por perfil ficar boa, a T66 só escreve os cenários; se ficar frágil, o custo reaparece lá, na sexta 06/11.
