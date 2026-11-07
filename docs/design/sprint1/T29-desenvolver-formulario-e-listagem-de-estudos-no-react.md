# T29 · Desenvolver formulário e listagem de estudos no React

> **Sprint 1** · PBI-07 · RF04 Cadastro de estudos clínicos  
> **Responsável:** I6 · **Estimativa:** 8h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T28, T13, T25  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Dar ao pesquisador as telas para listar, cadastrar, editar e excluir estudos; o coordenador apenas consulta.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/EstudosPage.tsx` | Existente | Lista de estudos, com status; abre o detalhe (T39); excluir com confirmação |
| `web/ClinicalStudyPB/src/pages/estudos/EstudoFormPage.tsx` | Existente | Formulário de criação e de edição |
| `web/ClinicalStudyPB/src/types/estudo.ts` | Existente | Tipos `Estudo` e `EstudoDados`, rótulos do status |
| `web/ClinicalStudyPB/src/utils/datas.ts` | Existente | Formatação de datas |
| `web/ClinicalStudyPB/src/services/recursos.ts` | Existente | `estudosApi` |

## 4. O que fazer em cada arquivo

### EstudoFormPage

- Campos: título, objetivo, datas, limite de participantes. O responsável é o pesquisador logado (o administrador não lista usuários).
- O seletor de status fica desabilitado: o estado muda pelo botão de avançar na página do estudo (T43).
- Validar localmente os mesmos limites de `EstudoRequest` e mostrar os erros do servidor no campo (T31).

### EstudosPage

- Botões de cadastrar, editar e excluir só para o pesquisador; coordenador vê a lista e o detalhe.

## 6. Testes

- Verificado manualmente e no E2E da Release (T66).

## 7. Critérios de aceite

- [ ] Pesquisador cadastra e edita; coordenador só consulta.
- [ ] Erros de validação aparecem no campo certo.

## 8. Cuidados e fora de escopo

- Não reabilitar o seletor de status: a API recusa a mudança de estado fora da transição.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
