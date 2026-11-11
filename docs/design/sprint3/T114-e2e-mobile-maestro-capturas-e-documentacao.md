# T114 · E2E mobile (Maestro), capturas e documentação

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 2h · **Prazo:** Sáb 21/11  
> **Depende de:** T110, T112  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
