# T52 · Tela de candidatos e ficha do candidato

> **Sprint 2** · PBI-13 · RF08 Cadastro de candidatos  
> **Responsável:** I5 · **Estimativa:** 5h · **Prazo:** Qua 04/11  
> **Depende de:** T39, T40, T51  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Aba Candidatos com lista paginada, filtro por situação e cadastro, e a ficha do candidato, que recebe as seções de avaliação (T55), consentimento (T58) e inclusão (T61) em componentes separados.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Rotas: `/estudos/:estudoId/candidatos` e `/estudos/:estudoId/candidatos/:candidatoId` (perfil coordenador).
- Situações do candidato: `CADASTRADO`, `EM_AVALIACAO`, `ELEGIVEL`, `NAO_ELEGIVEL`, `CONSENTIDO`, `INCLUIDO` (ver `docs/diagramas/dominio.md`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCandidatos.tsx` | Existente | Lista paginada, filtro, formulário de cadastro e botão desabilitado fora de recrutamento |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/FichaCandidatoPage.tsx` | Existente | Dados do candidato e as três seções |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoAvaliacao.tsx` | Existente | Entregue na T55 |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoConsentimento.tsx` | Existente (entregue na T58) | A T58 troca o corpo deste componente |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoInclusao.tsx` | Existente (entregue na T61) | A T61 troca o corpo deste componente |
| `web/ClinicalStudyPB/src/api/candidatos.ts` | Existente | `candidatosApi` (listar com filtro e página, buscar, criar, atualizar) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCandidatos.test.tsx, web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/FichaCandidatoPage.test.tsx` | Existente | Testes de componente |

## 4. O que fazer em cada arquivo

### Padrão de seção

- A ficha monta `<SecaoAvaliacao estudo candidato aoAvaliar={recarregar} />`; `aoAvaliar` é o `recarregar` da ficha. A T58 acrescentou `aoRegistrar` e a T61 acrescentou `estudo` e `aoIncluir`, no mesmo padrão; hoje a ficha monta `<SecaoConsentimento candidato aoRegistrar />` e `<SecaoInclusao estudo candidato aoIncluir />`.

## 6. Testes

- `AbaCandidatos.test.tsx` e `FichaCandidatoPage.test.tsx`.

## 7. Critérios de aceite

- [ ] Listar, filtrar e cadastrar funcionam; duplicidade vem do servidor como erro.
- [ ] A ficha abre pela lista e mostra as três seções.

## 8. Cuidados e fora de escopo

- Ficha de candidato de outro estudo mostra "Candidato não encontrado".

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
