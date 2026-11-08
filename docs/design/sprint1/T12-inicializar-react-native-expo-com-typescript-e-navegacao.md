# T12 · Inicializar React Native + Expo com TypeScript e navegação básica

> **Sprint 1** · PBI-03 · Inicialização das aplicações web e mobile  
> **Responsável:** I7 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Criar o aplicativo do participante com Expo e TypeScript, com navegação (login fora das abas e quatro abas com sessão), login real na API e logout. O conteúdo de estudos e visitas (RF17 a RF20) entra nas próximas Sprints.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O app atende só o perfil Participante (matriz RBAC); os outros perfis são recusados no login.
- Telas e fluxos de referência: `docs/sprint1/prototipos-mobile.md` (M01 a M11).
- Antes desta task, `mobile/ClinicalStudyPB` era um submódulo Git quebrado (sem `.gitmodules`) e chegava vazio; foi recriado como pasta normal.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/package.json, app.json, tsconfig.json, .env.example` | Existente | Expo SDK 58; `main: expo-router/entry`; scripts com `--port 8082`; alias `@/*` → `src/*`; `EXPO_PUBLIC_API_URL` |
| `mobile/ClinicalStudyPB/src/app/_layout.tsx` | Existente | `SessaoProvider` e `Stack` com `Stack.Protected` (login sem sessão; abas com sessão) |
| `mobile/ClinicalStudyPB/src/app/login.tsx` | Existente | M01 Login: e-mail, senha, aviso de dados fictícios, erro de credencial |
| `mobile/ClinicalStudyPB/src/app/(tabs)/_layout.tsx` | Existente | Barra de abas: Início, Estudos, Visitas, Perfil |
| `mobile/ClinicalStudyPB/src/app/(tabs)/index.tsx, estudos.tsx, visitas.tsx, perfil.tsx` | Existente | M02, M03, M05/M06 e M11 (Estudos e Visitas ainda são "em breve") |
| `mobile/ClinicalStudyPB/src/api/cliente.ts` | Existente | fetch com token, erros `{"erro": "..."}`, chamadas `/api/auth/login` e `/api/auth/me` |
| `mobile/ClinicalStudyPB/src/api/armazenamento.ts` | Existente | `expo-secure-store` no celular e `localStorage` no navegador |
| `mobile/ClinicalStudyPB/src/contexts/sessao.tsx` | Existente | `SessaoProvider` e `useSessao`: restaura, entra e sai |
| `mobile/ClinicalStudyPB/src/components/tema.ts, ui.tsx` | Existente | Cores e espaçamentos iguais aos da web; `Tela`, `Cartao`, `Botao`, `EmBreve` |

## 4. O que fazer em cada arquivo

### Passo a passo

- 1. Criar o projeto Expo com TypeScript em `mobile/ClinicalStudyPB` e instalar `expo-router` e `expo-secure-store`.
- 2. Configurar `app.json` (nome, `scheme: "clinicalstudypb"`, plugins) e os scripts com `--port 8082`, porque a 8081 é do backend.
- 3. Criar `src/app/_layout.tsx` com o `SessaoProvider` e o `Stack.Protected`: `guard={!usuario}` para o login e `guard={!!usuario}` para as abas.
- 4. Criar o cliente HTTP (`api/cliente.ts`) lendo `EXPO_PUBLIC_API_URL`; guardar o token com `api/armazenamento.ts`.
- 5. No `SessaoProvider`: ao abrir, restaurar o token e chamar `/api/auth/me`; só descartar o token se a API responder 401, 403 ou 404.
- 6. Login: aceitar apenas `PARTICIPANTE`; outro perfil mostra "Este aplicativo é exclusivo para participantes".
- 7. Criar as quatro abas e o botão **Sair** na aba Perfil (apaga o token e volta ao login).

### Como rodar

- `cd mobile/ClinicalStudyPB`, `npm install`, `npm start`.
- Celular com Expo Go: `EXPO_PUBLIC_API_URL=http://<IP-da-máquina>:8081`; emulador Android: `http://10.0.2.2:8081`; navegador: `http://localhost:8081` (padrão).
- Entrar com `participante@clinicalstudy.test` / `senha-de-teste`. O backend libera CORS para `http://localhost:8082`.

### Verificações

- `npm run lint` (eslint-config-expo) e `npm run typecheck` (`tsc --noEmit`), executados no job `Mobile` do CI.
- Teste manual: sem sessão abre no login; pesquisador é recusado; participante entra; Sair volta ao login.

## 7. Critérios de aceite

- [ ] O app abre no login sem sessão e nas abas com sessão.
- [ ] Participante entra; os outros perfis são recusados.
- [ ] Sair apaga o token.
- [ ] Lint e typecheck passam.

## 8. Cuidados e fora de escopo

- Não testado em emulador nem em celular físico, só na versão web.
- A pasta `navigation/` prevista no DAS não existe: com o Expo Router a navegação está nos `_layout.tsx`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
