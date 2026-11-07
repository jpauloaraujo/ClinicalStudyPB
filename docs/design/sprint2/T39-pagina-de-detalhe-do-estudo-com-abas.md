# T39 · Página de detalhe do estudo com abas

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Qui 29/10  
> **Depende de:** Sprint 1 (T29)  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Dar a cada estudo uma página própria, com cabeçalho e cinco abas como rotas aninhadas, onde as telas das demais tasks se encaixam sem conflito de arquivos.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Rota real: `/estudos/:estudoId` (o plano chamava de `/studies/:studyId`).
- Fonte única das abas e dos perfis: `abas.ts`, no mesmo molde de `routes/navegacao.ts` (T25).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/EstudoDetalhePage.tsx` | Existente | Carrega o estudo, mostra título, estado, botão voltar, barra de abas e o `<Outlet>` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas.ts` | Existente | Lista das abas (caminho, rótulo, perfis) e `abasDoPerfil` / `perfisDaAba` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/contexto.ts` | Existente | `useEstudoDetalhe()` entrega `{estudo, recarregar}` às abas |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaResumo.tsx` | Existente | Dados do estudo |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaProvisoria.tsx` | Existente | Conteúdo provisório; hoje só a aba Participantes (T61) ainda a usa |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/estudoDetalhe.css` | Existente | Estilos do cabeçalho e das abas |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente | Rotas aninhadas, cada uma com `RequerPerfil(perfisDaAba(...))` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/EstudoDetalhePage.test.tsx` | Existente | Testes de componente |

## 4. O que fazer em cada arquivo

### Abas e perfis

- Resumo: pesquisador e coordenador. Critérios e Visitas: pesquisador. Candidatos e Participantes: coordenador.

### Como encaixar uma tela nova

- Trocar o `element` da rota da aba no `router.tsx` pelo componente novo e ler o estudo com `useEstudoDetalhe()`; não mexer em `EstudoDetalhePage`.

### Erros

- Estudo inexistente (404 ou 400 de UUID): mostrar "Estudo não encontrado" com link para a lista.

## 6. Testes

- `EstudoDetalhePage.test.tsx`: título, abas por perfil e erro de estudo inexistente.

## 7. Critérios de aceite

- [ ] Cada perfil vê só as suas abas.
- [ ] Abrir uma aba proibida mostra "Acesso negado".
- [ ] A lista de estudos abre esta página.

## 8. Cuidados e fora de escopo

- Novas abas entram em `abas.ts`; nunca repetir a lista de perfis na rota.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
