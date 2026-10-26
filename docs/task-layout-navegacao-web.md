# Plano — T13: Layout base e navegação inicial da aplicação web

> Responsável: I6 · PBI-03 (Inicialização das aplicações web e mobile) · Sprint 1 · Estimativa: 3h
>
> Base: [sprint1-plano.md](sprint1-plano.md) (T13), [arquitetura.md](arquitetura.md) (seção 4 — matriz RBAC; seção 8 — estrutura de diretórios) e [especificacao-projeto.md](especificacao-projeto.md). Telas e rotas abaixo são uma proposta para validar com a equipe.
>
> **Status:** implementado — ver [implementacao-web-layout-navegacao.md](implementacao-web-layout-navegacao.md).

> **Atualização (26/10/2026):** este é o plano original da T13. As tasks citadas como "fora do escopo" (T14, T17, T21, T25 e T29) já foram implementadas — ver [status-sprint1.md](status-sprint1.md).

## 1. Objetivo

Substituir a tela padrão do template Vite por uma estrutura de aplicação com **layout base** (cabeçalho, menu lateral e área de conteúdo) e **navegação entre rotas** com React Router, deixando prontos os pontos de encaixe para as telas de usuários (T17), estudos (T29), login (T21) e proteção de rotas por perfil (T25).

## 2. Situação atual

- `web/ClinicalStudyPB` contém apenas o template do Vite (React 19 + TypeScript + React Compiler): `App.tsx` com contador e links de exemplo, `App.css`, `index.css` e imagens em `src/assets/`.
- **React Router ainda não está instalado** (previsto em T11) e não existem as pastas definidas na seção 8 da arquitetura.
- A API já responde em `http://localhost:8081` com `/api/usuarios` e `/api/estudos`, mas a integração HTTP é escopo de T14 (Axios) e não desta task.

## 3. Escopo

**Dentro:**

- Instalar `react-router` (v7), caso T11 ainda não tenha sido integrada.
- Criar o layout autenticado (`AppLayout`) e o layout público (`PublicLayout`, usado pelo login).
- Definir as rotas iniciais com páginas *placeholder*.
- Montar o menu lateral a partir de uma configuração única, já com os perfis permitidos em cada item (base para T25).
- Página 404 e redirecionamento da raiz.
- Estilos base (tokens de cor, tipografia e espaçamento) e layout responsivo.

**Fora (outras tasks):**

| Item | Task |
|---|---|
| Formulário e listagem de usuários | T17 |
| Formulário e listagem de estudos | T29 |
| Tela de login funcional, `AuthContext` real e logout | T21 |
| Guards de rota por perfil (bloqueio efetivo) | T25 |
| Cliente Axios e variáveis de ambiente (`VITE_API_URL`) | T14 |

## 4. Estrutura de diretórios proposta

Segue a seção 8 da arquitetura; esta task cria apenas o necessário:

```text
web/ClinicalStudyPB/src/
├── main.tsx                     # monta <RouterProvider>
├── index.css                    # reset + tokens globais (substitui o CSS do template)
├── components/
│   └── layout/
│       ├── AppLayout.tsx        # Header + Sidebar + <Outlet />
│       ├── PublicLayout.tsx     # área centralizada para o login
│       ├── Header.tsx           # nome do sistema, usuário/perfil, botão Sair (inativo até T21)
│       ├── Sidebar.tsx          # itens de menu filtrados por perfil
│       └── layout.css
├── contexts/
│   ├── sessao.ts                # SessaoContext, useSessao e usuários provisórios do seed
│   └── SessaoProvider.tsx       # sessão provisória (substituída pelo AuthContext na T21)
├── pages/
│   ├── InicioPage.tsx           # boas-vindas + atalhos conforme o perfil
│   ├── LoginPage.tsx            # placeholder (T21)
│   ├── NaoEncontradoPage.tsx    # 404
│   ├── usuarios/
│   │   └── UsuariosPage.tsx     # placeholder (T17)
│   └── estudos/
│       └── EstudosPage.tsx      # placeholder (T29)
├── routes/
│   ├── router.tsx               # createBrowserRouter com todas as rotas
│   └── navegacao.ts             # configuração do menu (rótulo, caminho, perfis)
└── types/
    └── usuario.ts               # PerfilUsuario espelhando o enum do backend
```

Remover do template: `App.tsx`, `App.css`, `src/assets/hero.png`, `react.svg`, `vite.svg` e `public/icons.svg`.

## 5. Rotas iniciais

| Caminho | Layout | Página | Perfis (matriz RBAC) | Observação |
|---|---|---|---|---|
| `/login` | Público | `LoginPage` | — | Placeholder até T21 |
| `/` | — | — | — | Redireciona para `/inicio` |
| `/inicio` | App | `InicioPage` | Administrador, Pesquisador, Coordenador | Tela inicial de todos os perfis web |
| `/usuarios` | App | `UsuariosPage` | Administrador | RF01 — conteúdo em T17 |
| `/estudos` | App | `EstudosPage` | Pesquisador, Coordenador (leitura) | RF04 — conteúdo em T29 |
| `*` | App | `NaoEncontradoPage` | — | 404 com link para `/inicio` |

As sub-rotas (`/usuarios/novo`, `/usuarios/:id`, `/estudos/novo`, `/estudos/:id`) ficam para T17 e T29, mas a definição em `router.tsx` deve usar rotas aninhadas para facilitar a inclusão.

O **Participante** não usa a web (apenas o app mobile). Na T25, se ele fizer login na web, deve ver uma mensagem orientando o uso do aplicativo.

## 6. Layout base

```text
┌────────────────────────────────────────────────────────────┐
│ ClinicalStudyPB              Helena Costa · Pesquisador [Sair]│  Header
├──────────────┬─────────────────────────────────────────────┤
│ Início       │  Título da página                           │
│ Estudos      │                                             │
│              │  <Outlet /> — conteúdo da rota              │
│              │                                             │
│              │                                             │
├──────────────┴─────────────────────────────────────────────┤
│ Sistema acadêmico — dados fictícios                        │  Rodapé
└────────────────────────────────────────────────────────────┘
```

- **Header:** nome do sistema (link para `/inicio`), nome e perfil do usuário e botão **Sair** (sem ação até T21).
- **Sidebar:** itens gerados por `navegacao.ts`; item ativo destacado com `NavLink`; mostra só os itens do perfil atual.
- **Conteúdo:** título da página (`<h1>`) e `<Outlet />`.
- **Rodapé:** aviso fixo de que o sistema é acadêmico e usa apenas dados fictícios (exigência da especificação).
- **Responsividade:** abaixo de 768 px a sidebar vira um menu recolhível acionado por botão no header.
- **Acessibilidade:** `<header>`, `<nav aria-label="Menu principal">`, `<main>` e `<footer>`; foco visível; `aria-current="page"` no item ativo (o `NavLink` já faz isso).

## 7. Configuração do menu e perfil provisório

`navegacao.ts` centraliza os itens para que a T25 reutilize a mesma fonte de verdade nos guards:

```ts
import type { PerfilUsuario } from '../types/usuario'

export type ItemNavegacao = {
  rotulo: string
  caminho: string
  perfis: PerfilUsuario[]
}

export const itensNavegacao: ItemNavegacao[] = [
  { rotulo: 'Início', caminho: '/inicio', perfis: ['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'] },
  { rotulo: 'Usuários', caminho: '/usuarios', perfis: ['ADMINISTRADOR'] },
  { rotulo: 'Estudos', caminho: '/estudos', perfis: ['PESQUISADOR', 'COORDENADOR'] },
]
```

Enquanto o login (T21) não existe, o layout usa um **usuário provisório** fixo (ex.: um dos usuários do seed `V2__seed_initial_data`). Em modo de desenvolvimento, um seletor de perfil no header permite trocar entre Administrador, Pesquisador e Coordenador para conferir o menu. Esse seletor deve ser removido ou substituído pelo `AuthContext` na T21.

> O filtro do menu é apenas usabilidade. A restrição real é feita no backend (T24) e nos guards de rota (T25).

## 8. Estilo visual

- CSS puro com variáveis em `:root` (cores, espaçamentos, raio, fonte), sem biblioteca de componentes nesta sprint. Se a equipe adotar uma (ex.: MUI ou Chakra), registrar a decisão antes de T17/T29.
- Paleta sóbria em azul/cinza, adequada ao contexto clínico, alinhada aos protótipos web do Figma (T32), se já estiverem disponíveis.
- Fonte do sistema (`system-ui`) para evitar dependência externa.

## 9. Passo a passo

| # | Atividade | Tempo |
|---|---|---|
| 1 | Instalar `react-router` e remover os arquivos do template | 15 min |
| 2 | Criar `types/usuario.ts`, `routes/navegacao.ts` e `routes/router.tsx` | 30 min |
| 3 | Implementar `AppLayout`, `Header`, `Sidebar` e `PublicLayout` | 60 min |
| 4 | Criar as páginas placeholder e a 404 | 20 min |
| 5 | Estilos base, responsividade e seletor de perfil provisório | 35 min |
| 6 | Verificar `npm run lint` e `npm run build`, testar navegação e abrir PR | 20 min |
| | **Total** | **3h** |

## 10. Critérios de aceite

- `npm run dev` abre a aplicação em `/inicio` com header, menu lateral, conteúdo e rodapé.
- Os links do menu navegam sem recarregar a página, e o item ativo fica destacado.
- O menu exibe apenas os itens do perfil selecionado (Administrador → Início e Usuários; Pesquisador/Coordenador → Início e Estudos).
- `/login` é exibida sem header nem menu; um caminho inexistente mostra a página 404.
- O layout funciona em telas de 360 px de largura sem rolagem horizontal.
- `npm run lint` e `npm run build` passam sem erros.
- Nenhum resquício do template Vite (contador, logos, links externos).

## 11. Dependências e riscos

- **T11:** se a instalação do React Router ainda não tiver sido integrada, fazer nesta task e avisar o responsável (I5) para evitar conflito em `package.json`.
- **T21/T25:** dependem de `navegacao.ts` e do `AppLayout`; combinar com I5 o formato do `AuthContext` (usuário com `nome` e `perfil`) para encaixe direto.
- **T32:** se os protótipos web mudarem a estrutura (ex.: menu superior em vez de lateral), ajustar apenas os componentes de `components/layout/`.

## 12. Como testar

No diretório `web/ClinicalStudyPB`:

```bash
npm install
npm run dev     # http://localhost:5173
npm run lint
npm run build
```
