# T13 · Desenvolver layout base e navegação inicial da aplicação web

> **Sprint 1** · PBI-03 · Inicialização das aplicações web e mobile  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T11  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Substituir a tela padrão do template Vite por um layout (cabeçalho, menu lateral, conteúdo e rodapé) e pela navegação inicial com React Router, com o menu e os perfis definidos em uma única configuração.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Plano original: `docs/sprint1/task-layout-navegacao-web.md`; implementação: `docs/sprint1/implementacao-web-layout-navegacao.md`.
- As telas de usuários, estudos e login, o Axios, os guards e o login real foram feitos nas tasks T17, T29, T21, T14 e T25.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/components/layout/AppLayout.tsx` | Existente | Layout autenticado: Header, Sidebar e `<Outlet />` |
| `web/ClinicalStudyPB/src/components/layout/PublicLayout.tsx` | Existente | Layout público, usado pelo login |
| `web/ClinicalStudyPB/src/components/layout/Header.tsx` | Existente | Nome do sistema, usuário · perfil e botão Sair |
| `web/ClinicalStudyPB/src/components/layout/Sidebar.tsx` | Existente | Itens de menu filtrados por perfil |
| `web/ClinicalStudyPB/src/components/layout/layout.css, web/ClinicalStudyPB/src/index.css` | Existente | Tokens de cor, espaçamento e tipografia; layout responsivo |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente | `createBrowserRouter` com rotas aninhadas |
| `web/ClinicalStudyPB/src/routes/navegacao.ts` | Existente | Itens do menu: rótulo, caminho e perfis |
| `web/ClinicalStudyPB/src/pages/InicioPage.tsx, NaoEncontradoPage.tsx` | Existente | Início com atalhos por perfil e 404 |

## 4. O que fazer em cada arquivo

### Rotas iniciais

- `/login` (layout público), `/` → redireciona para `/inicio`, `/inicio` (Administrador, Pesquisador, Coordenador), `/usuarios` (Administrador), `/estudos` (Pesquisador e Coordenador), `*` → 404.
- Sub-rotas aninhadas desde o início, para as telas seguintes se encaixarem.

### Layout

- Header com nome do sistema (link para `/inicio`), usuário e perfil e botão Sair; Sidebar com `NavLink` e item ativo destacado; área de conteúdo com `<h1>`; rodapé com o aviso de sistema acadêmico com dados fictícios.
- Abaixo de 768 px a sidebar vira menu recolhível acionado por botão no header.
- Acessibilidade: `<header>`, `<nav aria-label="Menu principal">`, `<main>`, `<footer>`, foco visível e `aria-current="page"`.

### Estilo

- CSS puro com variáveis em `:root`: `--cor-primaria #1d5fa8`, `--cor-fundo #f4f6f9`, `--cor-perigo #b42318`, `--cor-sucesso #067647`, espaçamentos de 4 a 32 px, raio 8 px, header de 56 px e sidebar de 220 px.
- Sem biblioteca de componentes.

### Passo a passo

- 1. Remover os arquivos de exemplo do template (`App.tsx`, `App.css`, imagens). 2. Criar os tokens em `index.css`. 3. Criar `navegacao.ts`. 4. Criar os layouts e as páginas. 5. Montar `router.tsx`. 6. Conferir em tela estreita e larga.

## 6. Testes

- `npm run lint` e `npm run build`; conferência manual em larguras acima e abaixo de 768 px.

## 7. Critérios de aceite

- [ ] Cada perfil vê só os itens do menu a que tem direito.
- [ ] A rota inexistente mostra o 404.
- [ ] O layout funciona em celular e em desktop.

## 8. Cuidados e fora de escopo

- O filtro do menu é só usabilidade: a restrição real é do backend (T24) e dos guards (T25).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
