# T25 · Proteger rotas e funcionalidades da aplicação React

> **Sprint 1** · PBI-06 · RF03 Controle de acesso por perfil (web)  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T21, T23  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Esconder e bloquear no front-end o que o perfil não pode usar, a partir da mesma fonte que monta o menu.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A autorização real é do backend (T24); a web só melhora a experiência.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/routes/RotaProtegida.tsx` | Existente | `RequerAutenticacao` (manda ao login) e `RequerPerfil` (mostra "Acesso negado") |
| `web/ClinicalStudyPB/src/routes/navegacao.ts` | Existente | Fonte única: itens de menu e perfis de cada rota |
| `web/ClinicalStudyPB/src/pages/AcessoNegadoPage.tsx` | Existente | Tela de acesso negado |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente | Aplica os guards; as abas do estudo usam `perfisDaAba` (T39) |

## 4. O que fazer em cada arquivo

### Para uma rota nova

- Registrar o item em `itensNavegacao` (rótulo, caminho, perfis) e usar `perfisDaRota("/caminho")` no `RequerPerfil` da rota.
- Para sub-rotas com perfil mais restrito, aninhar outro `RequerPerfil` (como em `/estudos/novo`).

## 6. Testes

- `EstudoDetalhePage.test.tsx` cobre as abas por perfil; `e2e` cobre o redirecionamento ao login.

## 7. Critérios de aceite

- [ ] Sem sessão, qualquer rota leva ao login.
- [ ] Perfil sem permissão vê "Acesso negado".
- [ ] Menu e guards nunca divergem.

## 8. Cuidados e fora de escopo

- Não duplicar a lista de perfis em outro arquivo.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
