# Implementação — T13: Layout base e navegação inicial da web

Este material documenta o que foi implementado na task `T13` (PBI-03) em `web/ClinicalStudyPB`, seguindo o plano em [task-layout-navegacao-web.md](task-layout-navegacao-web.md).

## Resumo

- O template padrão do Vite foi substituído por uma estrutura de aplicação com cabeçalho, menu lateral, área de conteúdo e rodapé.
- A navegação usa **React Router 7** (`react-router`) com rotas aninhadas sob um layout comum.
- O menu é filtrado pelo perfil do usuário a partir de uma configuração única, reutilizável pelos guards de rota da `T25`.
- Enquanto o login (`T21`) não existe, a aplicação usa uma **sessão provisória** com os usuários do seed `V2__seed_initial_data.sql`.

## Arquivos

### Removidos (template Vite)

`src/App.tsx`, `src/App.css`, `src/assets/` (`hero.png`, `react.svg`, `vite.svg`) e `public/icons.svg`.

### Alterados

| Arquivo | Mudança |
|---|---|
| `package.json` / `package-lock.json` | Dependência `react-router@^7.18.4` |
| `index.html` | `lang="pt-BR"` e título `ClinicalStudyPB` |
| `src/main.tsx` | Renderiza `<SessaoProvider>` + `<RouterProvider router={router}>` |
| `src/index.css` | Reset, tokens globais (cores, espaçamentos, raio, fonte) e classes `.botao` / `.botao--secundario` |

### Criados

| Arquivo | Responsabilidade |
|---|---|
| `src/types/usuario.ts` | Tipo `PerfilUsuario` (espelha o enum do backend), `rotulosPerfil` e `UsuarioSessao` |
| `src/contexts/sessao.ts` | `SessaoContext`, hook `useSessao()` e `usuariosProvisorios` (IDs do seed) |
| `src/contexts/SessaoProvider.tsx` | Mantém o usuário atual e expõe `trocarPerfil()`; perfil inicial: Pesquisador |
| `src/routes/navegacao.ts` | `itensNavegacao` (rótulo, caminho, perfis) e `itensDoPerfil(perfil)` |
| `src/routes/router.tsx` | `createBrowserRouter` com todas as rotas |
| `src/components/layout/AppLayout.tsx` | Estrutura autenticada: `Header`, `Sidebar`, `<Outlet />` e rodapé; controla o menu recolhível |
| `src/components/layout/Header.tsx` | Marca (link para `/inicio`), seletor de perfil (somente em dev), nome/perfil e botão Sair desabilitado |
| `src/components/layout/Sidebar.tsx` | `<nav>` com `NavLink` para os itens do perfil; fecha o menu ao navegar |
| `src/components/layout/PublicLayout.tsx` | Cartão centralizado para telas públicas (login) |
| `src/components/layout/layout.css` | Estilos dos layouts, atalhos da página inicial e regras responsivas |
| `src/pages/InicioPage.tsx` | Saudação e cartões de atalho para os itens do menu do perfil |
| `src/pages/LoginPage.tsx` | Placeholder da `T21` com link "Continuar sem login" |
| `src/pages/NaoEncontradoPage.tsx` | Página 404 com link para o início |
| `src/pages/usuarios/UsuariosPage.tsx` | Placeholder da `T17` (RF01) |
| `src/pages/estudos/EstudosPage.tsx` | Placeholder da `T29` (RF04) |

## Rotas

| Caminho | Layout | Página | Perfis no menu |
|---|---|---|---|
| `/login` | `PublicLayout` | `LoginPage` | — |
| `/` | `AppLayout` | redireciona para `/inicio` | — |
| `/inicio` | `AppLayout` | `InicioPage` | Administrador, Pesquisador, Coordenador |
| `/usuarios` | `AppLayout` | `UsuariosPage` | Administrador |
| `/estudos` | `AppLayout` | `EstudosPage` | Pesquisador, Coordenador |
| `*` | `AppLayout` | `NaoEncontradoPage` | — |

`/usuarios` e `/estudos` já são rotas com `children`, para que `T17` e `T29` adicionem `novo` e `:id` sem reorganizar o roteador.

> O filtro por perfil existe **apenas no menu**: digitar `/usuarios` com o perfil Pesquisador ainda abre a página. O bloqueio de rotas é escopo da `T25`, e a autorização real fica no backend (`T24`).

## Sessão provisória e seletor de perfil

- `SessaoProvider` guarda um `UsuarioSessao` (`id`, `nome`, `perfil`) em estado local; nada é persistido.
- O cabeçalho mostra o seletor **"Perfil (dev)"** apenas quando `import.meta.env.DEV` é verdadeiro, ou seja, ele não aparece no build de produção.
- O Participante não está entre os perfis provisórios, pois usa apenas o app mobile.

**Para a T21:** substituir `SessaoProvider` por um `AuthContext` que exponha ao menos `usuario` (`nome` e `perfil`) e uma função de logout, e remover `trocarPerfil` e o seletor do `Header`. Os componentes de layout só dependem de `useSessao().usuario`, então a troca fica localizada.

## Layout e responsividade

- **≥ 768 px:** menu lateral fixo de 220 px ao lado do conteúdo.
- **< 768 px:** o menu vira um painel deslizante aberto pelo botão ☰ do cabeçalho (`aria-expanded`/`aria-controls`); o nome do usuário e o rótulo do seletor são ocultados. Fechado, o painel recebe `visibility: hidden` para não receber foco pelo teclado.
- Elementos semânticos `<header>`, `<nav aria-label="Menu principal">`, `<main>` e `<footer>`; o `NavLink` aplica `aria-current="page"` e a classe `active` ao item atual.
- Estilo em CSS puro com variáveis em `:root`, sem biblioteca de componentes.

## Verificação

Realizada em 26/10/2026:

- `npm run lint` e `npm run build` sem erros nem avisos.
- Navegação conferida no navegador em 1280 px e 360 px (sem rolagem horizontal), incluindo o menu móvel aberto e fechado, a página 404 e `/login` sem cabeçalho nem menu.

## Como testar

No diretório `web/ClinicalStudyPB`:

```bash
npm install
npm run dev     # http://localhost:5173
```

1. Abrir `http://localhost:5173`: deve redirecionar para `/inicio`.
2. Trocar o perfil em **Perfil (dev)**: Administrador vê Início e Usuários; Pesquisador e Coordenador veem Início e Estudos.
3. Acessar um caminho inexistente (ex.: `/abc`): deve exibir a página 404.
4. Acessar `/login`: deve exibir o cartão sem cabeçalho nem menu.
5. Reduzir a janela para menos de 768 px e usar o botão ☰ para abrir e fechar o menu.
