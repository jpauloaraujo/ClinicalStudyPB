# T21 · Desenvolver login web, gerenciamento de autenticação e logout

> **Sprint 1** · PBI-05 · RF02 Autenticação e logout (web)  
> **Responsável:** I5 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T20, T14  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Entrar e sair do sistema pela web, manter a sessão ao recarregar a página e descartar tokens vencidos.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O token fica no `localStorage` (T14). O participante não usa a web: o login dele é recusado na tela.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/LoginPage.tsx` | Existente | Formulário de login e mensagem de erro |
| `web/ClinicalStudyPB/src/contexts/SessaoProvider.tsx` | Existente | Estado da sessão, restauração ao carregar e logout |
| `web/ClinicalStudyPB/src/contexts/sessao.ts` | Existente | Contexto, hooks (`useUsuarioLogado`) e o erro que recusa o perfil Participante ("O perfil Participante utiliza o aplicativo mobile.") |
| `web/ClinicalStudyPB/src/components/layout/Header.tsx` | Existente | Nome do usuário e botão Sair |
| `web/ClinicalStudyPB/src/services/recursos.ts` | Existente | `autenticacaoApi.entrar` e `usuarioAtual` |

## 4. O que fazer em cada arquivo

### Login

- Chamar `autenticacaoApi.entrar`, gravar o token e carregar o usuário com `/api/auth/me`.
- Perfil `PARTICIPANTE`: recusar com mensagem orientando a usar o aplicativo.

### Restauração da sessão

- Ao iniciar, se há token, chamar `/api/auth/me`; só descartar o token se a API responder 401, 403 ou 404 (com o backend fora do ar, manter).

### Logout

- Remover o token, limpar o usuário e navegar para `/login`.

## 6. Testes

- `e2e/tests/login.spec.ts` e `e2e/tests/sessao.spec.ts` (T65).

## 7. Critérios de aceite

- [ ] Login válido leva ao início; inválido mostra "Credenciais inválidas".
- [ ] Recarregar a página mantém a sessão.
- [ ] Sair encerra a sessão.

## 8. Cuidados e fora de escopo

- Não guardar a senha em nenhum lugar.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
