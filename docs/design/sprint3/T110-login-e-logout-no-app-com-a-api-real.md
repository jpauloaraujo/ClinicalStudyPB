# T110 · Login e logout no app com a API real

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sex 20/11  
> **Depende de:** T108  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Revisar e fechar o login e o logout do app com o novo cliente: validação, mensagens, recusa dos perfis que não são participante, aviso de participante sem estudo, sessão restaurada ao reabrir e logout no Perfil, rodando em emulador Android ou celular físico, e não só no navegador.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Tela de login `src/app/login.tsx` (M01): e-mail e senha, mostrar/ocultar senha, validação de formato, erro de credenciais. A sessão (`src/contexts/sessao.tsx`) recusa perfis que não são `PARTICIPANTE` com a mensagem "Este aplicativo é exclusivo para participantes. Use a aplicação web." e remove o token.
- A API responde **a mesma mensagem 401 "Credenciais inválidas"** para e-mail inexistente, senha errada **e conta desativada** (de propósito, para não revelar qual campo errou): o app **não consegue** distinguir "usuário desativado"; a mensagem precisa cobrir os três casos.
- A Sprint 1 só testou a versão web do app (`expo start --web`); o login em dispositivo ou emulador nunca foi exercitado. O cliente novo e a sessão expirada vêm da T108.
- Regra 8: apenas o perfil participante entra no app; o login usa o mesmo endpoint e o mesmo token da web.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/app/login.tsx` | Alterar | Validação, mensagens e botão durante o envio |
| `mobile/ClinicalStudyPB/src/contexts/sessao.tsx` | Alterar | Sessão restaurada e logout |
| `mobile/ClinicalStudyPB/src/app/(tabs)/index.tsx` | Alterar | Aviso de que ainda não está em nenhum estudo |
| `mobile/ClinicalStudyPB/src/app/(tabs)/perfil.tsx` | Alterar | Logout e dados do usuário |
| `mobile/ClinicalStudyPB/src/api/estudos.ts` | Criar | `estudosApi.listar()` (usada também na T112) |
| `mobile/ClinicalStudyPB/src/app/__tests__/login.test.tsx` | Criar | Testes de componente do login |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T110-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Tela de login

- **RQ-T110-01** Validar antes de enviar: e-mail e senha obrigatórios ("Informe o e-mail e a senha.") e e-mail com formato válido ("Informe um e-mail válido."); o erro some quando o usuário volta a digitar.
- **RQ-T110-02** Botão **Entrar desabilitado enquanto envia** (sem envio duplo), com texto "Entrando…"; campo de senha com mostrar/ocultar; teclado correto (`email-address`, sem capitalização automática) e foco/`returnKeyType` que levam do e-mail à senha e à ação de entrar.
- **RQ-T110-03** Mensagem de **credencial inválida** única ("E-mail ou senha inválidos, ou conta desativada."), pois a API não distingue os três casos (ver contexto); falha de rede mostra "Não foi possível conectar. Verifique sua conexão e tente de novo."; erro inesperado mostra mensagem genérica; nunca mostrar JSON cru.
- **RQ-T110-04** Manter o texto de aviso de dados fictícios do protótipo M01.

### Regra 8: só o participante entra

- **RQ-T110-05** **Administrador, pesquisador e coordenador** que fizerem login recebem a mensagem "Este aplicativo é exclusivo para participantes. Use a aplicação web." e a sessão **não é aberta** (o token é removido do `SecureStore`); testar os três perfis.
- **RQ-T110-06** **Participante sem participação vinculada** entra normalmente e vê, em Início (e na lista de Estudos, T112), o aviso **"Você ainda não está incluído em nenhum estudo."** (chamada a `estudosApi.listar()`; lista vazia não é erro).

### Sessão e logout

- **RQ-T110-07** Ao reabrir o app com token válido, a **sessão é restaurada** (a tela de carregamento aparece e depois as abas); com token **inválido ou expirado** (401, 403, 404 em `/api/auth/me`) o token é descartado e o usuário volta ao login; com a **API fora do ar** o token é mantido e a restauração é tentada de novo na próxima abertura (comportamento atual: preservar).
- **RQ-T110-08** `Perfil` (M11) mostra nome, e-mail e o perfil "Participante" e o botão **Sair**, que descarta o token no dispositivo e volta ao login; depois de sair, o botão Voltar do sistema **não reabre** as abas (a proteção `Stack.Protected` garante).
- **RQ-T110-09** Uma chamada autenticada que receba 401 depois do login (token expirado) leva ao login com a mensagem "Sua sessão expirou. Entre novamente." (a T108 trata o 401 centralmente).

### Dispositivo real

- **RQ-T110-10** Rodar o fluxo de login, de recusa de outro perfil, de restauração de sessão e de logout em **emulador Android ou celular físico** (além do navegador): `EXPO_PUBLIC_API_URL=http://10.0.2.2:8081` no emulador e o IP da máquina no celular; registrar capturas ou gravação como evidência e os defeitos de plataforma como Bug.
- **RQ-T110-11** Conferir o teclado, a área segura (`SafeAreaView`), a rolagem com o teclado aberto e o contraste dos textos em um aparelho real.

### Testes

- **RQ-T110-12** Testes de componente do login (Jest e Testing Library): validação, botão desabilitado durante o envio, credencial inválida, falha de rede e recusa dos três perfis (com o cliente simulado).

## 5. Contrato, regras e códigos de erro

- Usa `POST /api/auth/login` e `GET /api/auth/me` da Sprint 1 e `GET /api/me/estudos` (T109).

## 6. Testes

- `login.test.tsx` e `sessao.test.tsx` (restauração, logout, perfil recusado).
- Roteiro manual em emulador ou celular, com evidência.

## 7. Critérios de aceite

- [ ] O participante entra, sai e tem a sessão restaurada; os outros perfis são recusados com a mensagem certa.
- [ ] O participante sem estudo entra e vê o aviso.
- [ ] O fluxo foi exercitado em emulador ou celular real, com evidência.

## 8. Cuidados e fora de escopo

- Não revelar se o e-mail existe: manter uma mensagem única para credencial inválida.
- Não guardar a senha no dispositivo; só o token, no `SecureStore`.

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

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 8 | Apenas o perfil participante entra no app; os demais perfis recebem mensagem de que o acesso é pela web. O login usa o mesmo endpoint e o mesmo token da web | T108, T110 |

### 9.4 Texto original da task no planejamento

> **T110 · Login e logout no app com a API real.** Primeiro revise a tela de login da Sprint 1 com o novo cliente: validação dos campos, mensagens de credencial inválida e de usuário desativado, e botão desabilitado durante o envio. Depois confirme a regra 8: pesquisador, coordenador e administrador recebem a mensagem de que o acesso é pela web, e participante sem participação vinculada entra e vê um aviso de que ainda não está em nenhum estudo. Em seguida teste a sessão restaurada ao reabrir o app e o logout no Perfil. Por fim, rode o fluxo em emulador Android ou em celular físico, além do navegador, pois a Sprint 1 só testou a versão web.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- Para o mobile “basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas”.
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
