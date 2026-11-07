# T17 · Desenvolver formulário e listagem de usuários no React

> **Sprint 1** · PBI-04 · RF01 Cadastro e gerenciamento de usuários  
> **Responsável:** I5 · **Estimativa:** 6h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T16, T13, T14  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Dar ao administrador as telas para listar, criar, editar e excluir usuários, com validação no formulário que espelha a do backend.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Cliente de API: `usuariosApi` em `src/services/recursos.ts`.
- Campo reutilizável: `src/components/ui/Campo.tsx`.
- Tipos: `src/types/usuario.ts`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/usuarios/UsuariosPage.tsx` | Existente | Tabela de usuários com ações de editar e excluir |
| `web/ClinicalStudyPB/src/pages/usuarios/UsuarioFormPage.tsx` | Existente | Formulário de criação (`/usuarios/novo`) e de edição (`/usuarios/:id`) |
| `web/ClinicalStudyPB/src/types/usuario.ts` | Existente | Tipos e rótulos dos perfis |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente | Rotas `usuarios`, `usuarios/novo` e `usuarios/:id` atrás de `RequerPerfil` |

## 4. O que fazer em cada arquivo

### UsuariosPage

- Carregar a lista com `usuariosApi.listar()`; mostrar estados de carregamento, vazio e erro.
- Excluir pede confirmação (`window.confirm`); mostrar a mensagem da API se a exclusão for recusada.

### UsuarioFormPage

- Campos: nome, e-mail, perfil, senha (obrigatória na criação, opcional na edição) e ativo.
- Validar localmente com as mesmas regras de `UsuarioCriacaoRequest` (tamanhos, e-mail válido, senha de 8 a 100).
- Mostrar o erro do servidor no campo certo (ver T31).

## 6. Testes

- Verificado manualmente contra a API real; o E2E de regressão de RF01 está previsto na T66.

## 7. Critérios de aceite

- [ ] Só o administrador vê o item "Usuários" e acessa as rotas.
- [ ] Criar, editar e excluir funcionam contra a API real.

## 8. Cuidados e fora de escopo

- A sessão do administrador não pode ser derrubada por excluir a própria conta: a API já recusa, a tela só exibe o motivo.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
