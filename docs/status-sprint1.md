# Status da Sprint 1

> **Ponto de partida para retomar o contexto do projeto.** Mostra a situação de cada uma das 35 tasks da Sprint 1 ([sprint1-plano.md](sprint1-plano.md)), quem é o responsável, onde está o código e o que ainda falta.
>
> Atualizado em **26/10/2026** (último dia da Sprint 1), na branch `feat/sprint1-pendencias`.

## 1. Resumo

| Situação | Tasks |
|---|---|
| ✅ Concluídas no código | T01 a T31 (exceto as de design abaixo) |
| 🎨 Design / documentação, fora do repositório | T32, T33, T35 e a parte de Figma da T34 |
| ⚠️ Pendências de processo | Revisão por outro integrante, PR, Azure Boards e capturas de tela (seção 5) |

**Implementadas na branch `feat/sprint1-pendencias`** (antes faltavam no `main`): T05, T12, T14, T17, T21, T24, T25, T26, T29, T30 e T31. Como foram feitas fora da divisão original de responsáveis, **cada dono deve revisar a sua parte** antes do merge (ver seção 4).

## 2. Situação de todas as tasks

Legenda: ✅ concluída · 🆕 concluída nesta branch · 🎨 fora do código

### PBI-01 — Repositório e arquitetura

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T01 | Definir e documentar a arquitetura | I1 | ✅ | [arquitetura.md](arquitetura.md) |
| T02 | GitHub, branches e regras de PR | I1 | ✅ (configuração no GitHub) | — |
| T03 | Diagrama inicial da arquitetura | I2 | ✅ | [arquitetura.md](arquitetura.md) (Mermaid) |
| T04 | README com estrutura e instruções | I7 | ✅ | [README.md](../README.md) |
| T05 | GitHub Actions (build e verificações) | I1 | 🆕 | `.github/workflows/ci.yml` |

### PBI-02 — Spring Boot e PostgreSQL

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T06 | Projeto Spring Boot (camadas) | I3 | ✅ | `backend/ClinicalStudyPB/src/main/java/...` |
| T07 | PostgreSQL e conexão | I1 | ✅ | `application.properties`, `application-dev.properties.example` |
| T08 | Modelos JPA de usuários e estudos | I3 | ✅ | `domain/model/` |
| T09 | Migrations Flyway | I2 | ✅ | `src/main/resources/db/migration/` |
| T10 | Swagger UI / springdoc | I2 | ✅ | `config/OpenApiConfig.java` |

### PBI-03 — Web e mobile

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T11 | React + TypeScript + Vite + React Router | I5 | ✅ | `web/ClinicalStudyPB/` |
| T12 | React Native + Expo + navegação | I7 | 🆕 | `mobile/ClinicalStudyPB/` — ver [implementacao-mobile.md](implementacao-mobile.md) |
| T13 | Layout base e navegação web | I6 | ✅ | [implementacao-web-layout-navegacao.md](implementacao-web-layout-navegacao.md) |
| T14 | Axios e variáveis de ambiente | I5 | 🆕 | `web/.../src/services/api.ts`, `.env.example` |

### PBI-04 — RF01 Usuários

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T15 | DTOs e validações de usuários | I3 | ✅ | `controller/dto/Usuario*.java` — [task-dtos-validacoes-usuarios.md](task-dtos-validacoes-usuarios.md) |
| T16 | Serviços e endpoints de usuários | I4 | ✅ | `UsuarioController`, `UsuarioService` |
| T17 | Formulário e listagem de usuários (React) | I5 | 🆕 | `web/.../src/pages/usuarios/` |
| T18 | Testes do módulo de usuários | I2 | ✅ | `UsuarioIntegrationTest`, `UsuarioDtoValidationTest` |

### PBI-05 — RF02 Autenticação

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T19 | Spring Security, BCrypt e token | I4 | ✅ | `security/SecurityConfig.java`, `TokenService.java` |
| T20 | Endpoint de login | I4 | ✅ | `AuthController`, `AuthService` |
| T21 | Login web, sessão e logout | I5 | 🆕 | `web/.../src/contexts/`, `pages/LoginPage.tsx`, `components/layout/Header.tsx` |
| T22 | Testes de login e acesso não autenticado | I7 | ✅ | `AuthenticationIntegrationTest` (commit `4d5b13c`) |

### PBI-06 — RF03 Controle de acesso

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T23 | Matriz de permissões | I1 | ✅ | [arquitetura.md](arquitetura.md), seção 4 |
| T24 | Autorização por perfil no Spring Security | I4 | 🆕 | `@PreAuthorize` em `UsuarioController` e `EstudoController`; `@EnableMethodSecurity` |
| T25 | Proteger rotas no React | I6 | 🆕 | `web/.../src/routes/RotaProtegida.tsx`, `router.tsx` |
| T26 | Testes de permissões e acessos negados | I2 | 🆕 | `PermissaoAcessoIntegrationTest` |

### PBI-07 — RF04 Estudos

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T27 | DTOs, validações e serviço de estudos | I3 | ✅ | `EstudoRequest`, `EstudoService` |
| T28 | Endpoints REST de estudos | I3 | ✅ | `EstudoController` |
| T29 | Formulário e listagem de estudos (React) | I6 | 🆕 | `web/.../src/pages/estudos/` |
| T30 | Testes de estudos | I2 | 🆕 | `EstudoServiceTest`, `EstudoIntegrationTest` |
| T31 | Integrar telas com a API e mostrar erros | I5 | 🆕 | `web/.../src/services/erros.ts`, formulários |

### PBI-08 — Protótipos

| Task | Descrição | Resp. | Situação | Onde está |
|---|---|---|---|---|
| T32 | Protótipos web no Figma | I6 | 🎨 sem link registrado | Figma |
| T33 | Protótipos do participante no Figma | I7 | 🎨 requisitos prontos, Figma pendente | [prototipos-mobile.md](prototipos-mobile.md) |
| T34 | Fluxo de navegação mobile para demonstração | I7 | 🆕 parte de código (abas e login no Expo); 🎨 fluxo no Figma pendente | [prototipos-mobile.md](prototipos-mobile.md), `mobile/.../src/app/` |
| T35 | Revisar fluxos e registrar ajustes | I1 | 🎨 depende de T32 a T34 | — |

## 3. O que mudou nesta branch, por área

### Backend (`backend/ClinicalStudyPB`)

- **T24:** `@EnableMethodSecurity` em `SecurityConfig`; `UsuarioController` exige `ADMINISTRADOR`; em `EstudoController`, leitura para `PESQUISADOR` e `COORDENADOR` e escrita só para `PESQUISADOR`. `ApiExceptionHandler` responde `403` com `{"erro": "Acesso negado para o seu perfil"}`.
- **Novo endpoint `GET /api/auth/me`:** devolve nome, e-mail e perfil do dono do token. A web e o mobile usam para montar a sessão, porque a resposta do login só traz o token.
- **CORS:** além de `http://localhost:5173` (web), libera `http://localhost:8082` (app mobile rodando no navegador).
- **Exclusão de usuário responsável por estudo:** antes resultava em erro 500 (FK `ON DELETE RESTRICT`); agora `UsuarioService` responde `400` com "Não é possível excluir: o usuário é responsável por estudos. Desative a conta".
- **Revisão de regras (pós-merge):** o backend passou a recusar que o administrador exclua, desative ou rebaixe a própria conta e que o último administrador ativo deixe de ser ativo; o responsável do estudo precisa ser `PESQUISADOR`; e-mails são normalizados para minúsculas (migration `V3`, login incluído); cadastro simultâneo com o mesmo e-mail vira `400` em vez de `500`; JSON malformado, enum inválido e UUID inválido respondem `400` no formato `{"erro": "..."}`; `EstudoRequest` ganhou mensagens em português. Testes em `RegrasDeContaIntegrationTest`.
- **Sessão (web e mobile):** ao restaurar a sessão, o token só é descartado quando a API responde `401`, `403` ou `404`; com o backend fora do ar ele é mantido.
- **T26 / T30:** 29 testes novos em `PermissaoAcessoIntegrationTest`, `EstudoServiceTest` e `EstudoIntegrationTest`, mais 1 em `UsuarioIntegrationTest` (exclusão acima), e o utilitário `support/TokenDeTeste`, que faz login real com os usuários do seed.
- **Cobertura:** JaCoCo no `pom.xml`. Resultado: 70 testes passando e 97% das linhas do código de negócio cobertas.

### Web (`web/ClinicalStudyPB`)

- **T14:** Axios 1.20 em `src/services/api.ts`, com `VITE_API_URL`, token no cabeçalho e logout automático quando a API responde `401`.
- **T21:** a sessão provisória com seletor de perfil (da T13) foi substituída por login real. O token fica no `localStorage`, a sessão é restaurada ao recarregar e tokens vencidos são descartados. O participante é recusado na web.
- **T25:** `RequerAutenticacao` manda para `/login`; `RequerPerfil` mostra "Acesso negado". Os perfis de cada rota vêm de `navegacao.ts`, a mesma fonte do menu.
- **T17 / T29 / T31:** telas de listagem e formulário de usuários e estudos, com validação local espelhando os DTOs e exibição dos erros da API no campo certo.

### Mobile (`mobile/ClinicalStudyPB`)

- **T12:** a pasta era um submódulo quebrado (sem `.gitmodules`) e chegava vazia para quem clonava. Foi recriada como projeto Expo SDK 58 com Expo Router: login, abas e logout. Detalhes em [implementacao-mobile.md](implementacao-mobile.md).

### CI (`.github/workflows/ci.yml`)

- **T05:** quatro jobs (backend, backend no PostgreSQL, web e mobile) a cada push e PR na `main` e na `develop`. Ver README, seção 6. O job do PostgreSQL ainda não foi executado no GitHub: confirmar que fica verde no primeiro push.

## 4. Decisões tomadas que a equipe deve validar

| Decisão | Motivo | Quem valida |
|---|---|---|
| Administrador **não** acessa `/api/estudos` | Segue a matriz RBAC da `arquitetura.md` (RF04 ❌ para administrador) | I1 / I2 |
| Responsável por um estudo novo é o pesquisador logado | `/api/usuarios` é só do administrador, então o pesquisador não tem como listar outros responsáveis | I3 / I6 |
| `GET /api/auth/me` adicionado | O `LoginResponse` não traz perfil (e o teste da T22 garante isso) | I4 |
| Mobile com Expo Router em vez de pasta `navigation/` | É o padrão do Expo SDK 58; a estrutura `api/`, `components/` e `contexts/` segue a seção 8 da arquitetura | I7 / I1 |
| Qualquer pesquisador edita e exclui qualquer estudo | O RF04 não exige dono do estudo; só o *cadastro* define o responsável. Para restringir ao dono, bastaria comparar o `sub` do token com `responsavel.id` em `EstudoService` | I3 / I2 |
| Revogação de JWT não implementada (risco aceito) | Desativar conta ou mudar perfil só vale após o token vencer (30 min); o logout descarta o token no cliente. Revogar na hora exige consulta ao banco por requisição ou lista de tokens | I4 |
| Metro do Expo na porta 8082 | A 8081 é a porta padrão do backend | I7 |
| JaCoCo sem limite mínimo obrigatório no build | Só gera o relatório; um `check` de 80% pode ser ligado depois | I1 / I2 |

## 5. O que ainda falta

### Para fechar a Sprint 1

1. **Revisão e merge da branch `feat/sprint1-pendencias`:** cada responsável revisa a sua task (tabela da seção 2) e um PR é aberto para a `main`, conforme a Definition of Done.
2. **Azure Boards:** atualizar *Remaining Work*, vincular commits e registrar os resultados dos testes de cada task.
3. **Protótipos no Figma (T32, T33, T34) e revisão (T35):** registrar os links no README.
4. **Capturas de tela** das telas web e mobile, em sucesso e em erro, na seção 3 do README.
5. **`releases/release1/README.md`**, com o processo de desenvolvimento e o burndown do Azure DevOps (exigido pela especificação).
6. **Nomes dos integrantes** no README, no lugar de I1 a I7.

### Dívidas técnicas conhecidas

- Não há testes automatizados de componentes web e mobile nem E2E. A especificação pede pelo menos um fluxo E2E em cada interface.
- O `npm audit` do mobile aponta vulnerabilidades em dependências de desenvolvimento do Expo CLI (`node-forge`, `zod`). A correção forçada quebraria o SDK; acompanhar as atualizações do Expo.
- O logout é só no cliente (JWT stateless): o token continua válido até expirar (30 minutos), mesmo se a conta for desativada ou mudar de perfil (risco aceito, seção 4).
- Os testes locais usam H2; o PostgreSQL só é exercitado pelo job da CI (ainda sem execução confirmada) e, manualmente, no login e no seed.
