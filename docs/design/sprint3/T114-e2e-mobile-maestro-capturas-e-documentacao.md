# T114 · E2E mobile (Maestro), capturas e documentação

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 2h · **Prazo:** Sáb 21/11  
> **Depende de:** T110, T112  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Cumprir a exigência da especificação de **pelo menos um fluxo E2E no app mobile**: um fluxo Maestro que abre o app, entra com o participante de demonstração, abre Meus estudos e o detalhe de um estudo e sai, rodando localmente em emulador com evidência. Salvar as capturas, atualizar a documentação, zerar o Remaining Work e passar o bastão ao PBI-25.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O app roda em Expo (`expo start --port 8082`); o `app.json` tem `scheme: "clinicalstudypb"` e **não** define `android.package` nem `ios.bundleIdentifier`: no Expo Go o `appId` do Maestro é `host.exp.exponent` (Android) e o app é aberto pelo link `exp://...`; um *development build* teria o próprio id.
- O backend precisa estar no ar com o perfil `demo` (`participante@clinicalstudy.test` vinculado ao P1 pela T111, senha `senha-de-teste`) e o app com `EXPO_PUBLIC_API_URL=http://10.0.2.2:8081` no emulador Android.
- Rodar emulador no CI é pesado: a decisão de rodar o Maestro no CI ou só localmente foi tomada no Planning (T94). **Por padrão, roda localmente com evidência.**
- I5 acrescenta os passos de visitas na T121.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/.maestro/fluxo-participante.yaml` | Criar | Fluxo base: login, estudos, detalhe e logout |
| `mobile/ClinicalStudyPB/.maestro/login-perfil-recusado.yaml` | Criar | Login com outro perfil (opcional, se houver tempo) |
| `mobile/ClinicalStudyPB/README.md` | Alterar | Como instalar o Maestro e rodar os fluxos |
| `docs/imagens/sprint3/` | Criar | Capturas do login, da recusa de outro perfil e dos estudos |
| `README.md` | Alterar | RF17 e login mobile |
| `docs/sprint3/e2e-mobile-evidencia.md` | Criar | Como a evidência foi gerada e onde está |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T114-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Fluxo Maestro base

- **RQ-T114-01** Instalar o **Maestro** (CLI) e escrever `.maestro/fluxo-participante.yaml`: abrir o app, aguardar a tela de login, preencher `participante@clinicalstudy.test` e `senha-de-teste`, tocar em Entrar, **confirmar a aba Estudos**, abrir **Meus estudos** e **o detalhe de um estudo** (conferindo o título do estudo de demonstração e o status), voltar e **sair** pelo Perfil, confirmando o retorno ao login.
- **RQ-T114-02** Usar **identificadores acessíveis** (`accessibilityLabel`/`testID`) nos elementos do fluxo (campos, botões, cartão do estudo) em vez de coordenadas; se faltar identificador, acrescentá-lo na tela, sem mudar o comportamento.
- **RQ-T114-03** Escrever também (se couber) o fluxo de **recusa de outro perfil**: login com `coordenador@clinicalstudy.test` mostra a mensagem "exclusivo para participantes".
- **RQ-T114-04** O fluxo deve ser **repetível** (começa sempre no login: limpar o estado do app com `launchApp: clearState`), sem depender de dados criados à mão além do perfil `demo`.

### Execução e evidência

- **RQ-T114-05** Rodar o fluxo **localmente em emulador Android** (ou celular físico) com o backend em perfil `demo`, e guardar a **evidência**: gravação (`maestro record`) ou as capturas do passo a passo (`takeScreenshot`), com a saída do comando mostrando o fluxo aprovado.
- **RQ-T114-06** Registrar em `docs/sprint3/e2e-mobile-evidencia.md` o comando usado, a versão do Maestro e do emulador, a data e o link/arquivo da evidência; o CI com emulador **não** é exigido nesta task (decisão do Planning).
- **RQ-T114-07** Anotar os problemas encontrados (por exemplo, instabilidade de timing) e os defeitos de plataforma como Bug no Azure Boards.

### Capturas e documentação

- **RQ-T114-08** Salvar as capturas do **login** (`rf17-login-mobile`), da **recusa de outro perfil** (`rf17-login-perfil-recusado`) e dos **estudos** (`rf17-meus-estudos` e `rf17-detalhe-estudo`) em `docs/imagens/sprint3/`.
- **RQ-T114-09** Atualizar o `README.md` com o **RF17** e o **login mobile (RF02 e RF03 no mobile)**: comportamento, regras (só o participante entra; participante só vê os próprios dados), erros e capturas.
- **RQ-T114-10** Documentar no `README` do app como instalar o Maestro e rodar os fluxos (`maestro test .maestro/fluxo-participante.yaml`).
- **RQ-T114-11** Zerar o **Remaining Work** das tasks do PBI-24 no Azure Boards e registrar os Bugs.
- **RQ-T114-12** Avisar **I5** (PBI-25) para acrescentar os passos de visitas na T121 e passar o bastão: esqueleto do app (T108), endpoints `/me` (T109), vínculo da conta (T111) e o fluxo E2E mobile base (T114) estão na `main`; I5 revisa os Pull Requests deste PBI (I1 revisa os do PBI-25).

## 6. Testes

- O próprio fluxo Maestro (`maestro test`), executado localmente com evidência.
- A suíte web e a de backend continuam passando.

## 7. Critérios de aceite

- [ ] Existe pelo menos um fluxo E2E mobile (login, meus estudos, detalhe e logout) que passa em emulador, com evidência guardada.
- [ ] As capturas do login, da recusa de outro perfil e dos estudos estão no repositório e o README descreve o RF17 e o login mobile.
- [ ] O Remaining Work do PBI-24 está zerado e I5 foi avisado.

## 8. Cuidados e fora de escopo

- Não depender de coordenadas de tela: use identificadores acessíveis.
- O E2E mobile no CI fica para decisão do Planning (Sprint 4 se não couber agora).

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF17** (Módulo E — Aplicativo do participante; interface Mobile): “Permitir ao participante consultar os estudos em que está incluído.”
- **RF02** (Módulo A — Usuários e autenticação; interface Web/Mobile): “Permitir autenticação e encerramento de sessão.”
- **RF03** (Módulo A — Usuários e autenticação; interface Web/Mobile): “Restringir funcionalidades conforme o perfil do usuário.”
- Escopo do planejamento para este PBI: “RF17 e RF18 (Módulo E, interface Mobile), mais o login do participante no app (RF02 e RF03 no mobile)”.

### 9.2 Critério de aceite do PBI (planejamento)

> O participante entra no app com a conta vinculada pelo coordenador, vê a lista dos estudos em que está incluído e o detalhe de cada um, e sai do app; outros perfis são recusados com mensagem clara; o participante nunca vê dados de outro; existe pelo menos um fluxo E2E no app. Ao fechar, em 21/11, este PBI deixa na main o esqueleto do app, os endpoints /me e o vínculo da conta que o PBI-25 usa.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T114 · E2E mobile (Maestro), capturas e documentação.** Primeiro instale o Maestro e escreva o fluxo base: abrir o app, entrar com o participante de demonstração, abrir Meus estudos e o detalhe de um estudo, e sair; avise I5 para acrescentar os passos de visitas (T121). Depois rode o fluxo localmente em emulador e guarde a gravação ou as capturas como evidência, já que o CI com emulador fica para decisão do Planning. Por fim, salve capturas do login, da recusa de outro perfil e dos estudos, atualize o README com RF17 e o login mobile e zere o Remaining Work no Azure Boards.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- Para o mobile “basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas”.
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
