# T108 · Esqueleto do app: navegação, cliente HTTP e contexto de autenticação

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sex 20/11  
> **Depende de:** Sprint 1 (app Expo com login, T12)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Preparar o app Expo da Sprint 1 para receber as telas do PBI-24 e do PBI-25: cliente HTTP no formato único de erro da Sprint 2, tipos gerados do contrato, modo de mocks, Jest e Testing Library rodando no CI e os componentes básicos de tela. Avisar I5, dono do último PBI, de que o PBI-25 começa sobre este esqueleto.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O app (`mobile/ClinicalStudyPB`) já tem Expo SDK 58, Expo Router (login fora das abas com `Stack.Protected`, abas Início, Estudos, Visitas e Perfil), sessão no `expo-secure-store` (`src/contexts/sessao.tsx`) e login (`src/app/login.tsx`). Hoje as abas Estudos e Visitas são `EmBreve`.
- `src/api/cliente.ts` usa `fetch` (sem axios), lê só `{ erro }` do corpo de erro e não trata 401 de forma central; `tokenArmazenado` guarda o token na chave `clinicalstudypb.token`. `src/components/ui.tsx` já exporta `Tela`, `Cartao`, `Botao` e `EmBreve`; `tema.ts` exporta `cores`, `espaco` e `raio`.
- O formato único de erro da Sprint 2 é `{ status, codigoRegra, mensagem, errosCampo[{campo, erro}], erro, codigo }`.
- A web gera tipos com `openapi-typescript@7.13.0` (`npm run api:tipos`); o mobile segue o mesmo método. O CI mobile (`.github/workflows/ci.yml`, job `mobile`) hoje roda `npm ci`, `npm run lint` e `npm run typecheck`.
- O mobile **não tem testes automatizados** (apontado no relatório de testes da Release 1) e o E2E mobile é a T114.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/api/cliente.ts` | Alterar | Formato único de erro e tratamento central de 401 |
| `mobile/ClinicalStudyPB/src/api/schema.d.ts` | Criar | Tipos gerados de `docs/api/openapi-sprint3.yaml` |
| `mobile/ClinicalStudyPB/src/api/mocks/` | Criar | Respostas em memória com `EXPO_PUBLIC_USE_MOCKS=true` |
| `mobile/ClinicalStudyPB/src/contexts/sessao.tsx` | Alterar | Registra o callback de sessão expirada |
| `mobile/ClinicalStudyPB/src/components/` | Criar | `Selo`, `EstadoVazio`, `EstadoErro` e `Carregando` (junto ao `ui.tsx` existente) |
| `mobile/ClinicalStudyPB/src/app/_layout.tsx` | Alterar | Declara as telas fora das abas (detalhe do estudo e da visita) |
| `mobile/ClinicalStudyPB/package.json` | Alterar | Scripts `test` e `api:tipos` e dependências de teste |
| `mobile/ClinicalStudyPB/jest.config.js (ou "jest" no package.json)` | Criar | Preset `jest-expo` e alias `@/` |
| `mobile/ClinicalStudyPB/src/api/__tests__/cliente.test.ts` | Criar | Teste de exemplo do cliente |
| `.github/workflows/ci.yml` | Alterar | Passo `npm test` no job Mobile |
| `mobile/ClinicalStudyPB/README.md` | Criar ou alterar | Como rodar com mocks e com o backend |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T108-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cliente HTTP e formato único de erro

- **RQ-T108-01** Atualizar `src/api/cliente.ts` para ler o corpo de erro da Sprint 2: `ErroApi` passa a carregar `status`, `codigo` (do `codigoRegra`), `mensagem` e `errosCampo` (lista `{ campo, erro }`); a mensagem exibida é a `mensagem` do servidor, com fallback `Erro {status} na API.`; falha de rede continua `Não foi possível conectar à API em {URL}.` com `codigo` próprio (`SEM_CONEXAO`).
- **RQ-T108-02** Tratar o **401 de forma central**: quando uma chamada (exceto `POST /api/auth/login`) responde 401, o cliente chama um callback registrado (`definirAoExpirarSessao`), que **limpa o token e a sessão** e leva ao login; o `SessaoProvider` registra e remove o callback, como a web faz em `services/api.ts`.
- **RQ-T108-03** Manter o `Authorization: Bearer` do token do `SecureStore`, `Content-Type: application/json` e o timeout/abort razoável (por exemplo, 15 s), para a lista não ficar carregando para sempre.
- **RQ-T108-04** Fornecer um helper `mensagemDoErro(erro: unknown): string` que as telas usam para mostrar a mensagem (ErroApi, falha de rede ou erro inesperado).

### Tipos gerados e modo de mocks

- **RQ-T108-05** Acrescentar o script `api:tipos` (`npx --yes openapi-typescript@7.13.0 ../../docs/api/openapi-sprint3.yaml -o src/api/schema.d.ts`) e gerar o arquivo; as T109 e T115 o regeneram quando acrescentam os caminhos do participante. Os módulos de API (`estudos.ts`, `visitas.ts`) usam esses tipos, nunca `any`.
- **RQ-T108-06** Criar o **modo de mocks**: com `EXPO_PUBLIC_USE_MOCKS=true`, o cliente responde com dados em memória de `src/api/mocks/` (login, `/api/auth/me`, estudos, detalhe, próximas visitas e histórico), com os mesmos formatos do contrato e os dados do participante de demonstração; sem a variável, usa a API real. Os mocks servem ao desenvolvimento e aos testes de componente; **a entrega usa a API real**.
- **RQ-T108-07** Os mocks cobrem também os estados de erro (por exemplo, uma chave `EXPO_PUBLIC_MOCK_ERRO` ou dados que provocam 500) para exercitar as telas.

### Jest e Testing Library

- **RQ-T108-08** Adicionar as dependências de desenvolvimento compatíveis com o Expo SDK 58 (`jest`, `jest-expo`, `@testing-library/react-native`, `@types/jest` e o que o preset pedir), o script `npm test` (`jest`) e a configuração com o preset `jest-expo` e o alias `@/` apontando para `src/`.
- **RQ-T108-09** Escrever **um teste de exemplo rodando**: o cliente converte o corpo de erro da Sprint 2 em `ErroApi` e chama o callback de sessão expirada no 401 (com `fetch` simulado); e um teste de renderização de um componente básico (`EstadoVazio`).
- **RQ-T108-10** Acrescentar o passo `npm test` ao job `mobile` do `.github/workflows/ci.yml` (depois do lint e da verificação de tipos); `npm run lint`, `npm run typecheck` e `npm test` passam localmente e no CI.

### Navegação e componentes base

- **RQ-T108-11** Em `src/app/_layout.tsx`, manter o login fora das abas e as abas só com sessão de participante, e **declarar as telas fora das abas** que o PBI-24 e o PBI-25 criam: `estudos/[estudoId]` (M04) e `visitas/[visitaId]` (M07), com cabeçalho e botão voltar em português (títulos "Estudo" e "Visita").
- **RQ-T108-12** Criar em `src/components/` os componentes base reutilizados pelas telas: `Selo` (rótulo curto em texto com cor, nunca só cor), `EstadoVazio` (ícone/título/texto), `EstadoErro` (mensagem e botão **Tentar de novo**) e `Carregando` (indicador com texto acessível). Alvos de toque de pelo menos 44 pontos, `accessibilityRole` e `accessibilityLabel` onde o texto não bastar.
- **RQ-T108-13** Reaproveitar `Tela`, `Cartao`, `Botao`, `cores`, `espaco` e `raio`: não criar um segundo tema.

### Documentação e aviso

- **RQ-T108-14** Documentar no `README.md` do app: variáveis (`EXPO_PUBLIC_API_URL`, `EXPO_PUBLIC_USE_MOCKS`), como rodar **com mocks** e **com o backend** (perfil `demo`), a URL do emulador Android (`http://10.0.2.2:8081`) e do celular físico (IP da máquina), os comandos `npm test` e `npm run api:tipos` e o participante de demonstração (`participante@clinicalstudy.test`, senha `senha-de-teste`).
- **RQ-T108-15** Avisar **I5**, dono do PBI-25, de que o esqueleto está na `main` (cliente, tipos, mocks, Jest e componentes base).

## 5. Contrato, regras e códigos de erro

- Sem endpoints novos. O cliente passa a entender o formato único de erro (seção 4 das convenções).

## 6. Testes

- Teste do cliente (erro da Sprint 2, falha de rede e 401 com callback) e de um componente base, rodando no CI.
- `npm run lint`, `npm run typecheck` e `npm test` sem erro.

## 7. Critérios de aceite

- [ ] O cliente HTTP entende o formato único de erro e trata o 401 limpando a sessão.
- [ ] Os tipos são gerados do contrato e o modo de mocks funciona; a entrega usa a API real.
- [ ] Jest e Testing Library rodam no job Mobile do CI com um teste de exemplo.
- [ ] O README do app explica como rodar com mocks e com o backend; I5 foi avisado.

## 8. Cuidados e fora de escopo

- Não implementar as telas de estudos e de visitas aqui (T112, T117 e T118).
- Não criar um cliente novo: evoluir o `src/api/cliente.ts` existente.
- Sem mocks na entrega: o fluxo final roda contra a API real.

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

> **T108 · Esqueleto do app: navegação, cliente HTTP e contexto de autenticação.** O app Expo da Sprint 1 já tem Expo Router, login, sessão no SecureStore e as abas Início, Estudos, Visitas e Perfil; esta task o prepara para receber as telas do PBI-24 e do PBI-25. Primeiro atualize o cliente HTTP para o formato único de erro da Sprint 2 (status, código da regra, mensagem e erros por campo), com tratamento central de 401 que limpa a sessão e volta ao login. Depois gere os tipos TypeScript a partir do YAML da Sprint, como na web, e crie um modo de mocks ativado por variável de ambiente. Em seguida configure Jest e Testing Library no app, com um teste de exemplo rodando no job Mobile do CI. Por fim, documente no README do app como rodar com mocks e com o backend e avise I5, dono do último PBI, que o PBI-25 começa sobre este esqueleto.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- Para o mobile “basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas”.
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
