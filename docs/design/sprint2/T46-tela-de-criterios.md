# T46 · Tela de critérios

> **Sprint 2** · PBI-11 · RF05 Critérios de inclusão e exclusão  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Ter 03/11  
> **Depende de:** T39, T40, T45  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Aba Critérios com listas de inclusão e exclusão, formulário de cadastro e edição, exclusão com confirmação e modo somente leitura.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCriterios.tsx` | Existente | Duas listas, formulário, edição, exclusão e avisos |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCriterios.test.tsx` | Existente | 20 testes de componente |
| `web/ClinicalStudyPB/src/api/criterios.ts` | Existente | `criteriosApi` |
| `web/ClinicalStudyPB/src/components/ui/Campo.tsx, Confirmacao.tsx` | Existente | Campo com erro e diálogo |

## 4. O que fazer em cada arquivo

### Regras da tela

- Validar localmente como `CriterioRequest` (título até 120, descrição 5 a 500, peso > 0) e mostrar o erro do servidor no campo.
- Edição não permite trocar o tipo.
- Somente leitura, com aviso, se o estudo não está em planejamento ou o usuário é coordenador.
- Lista de inclusão vazia: "Adicione ao menos um critério de inclusão para avançar."

## 6. Testes

- `AbaCriterios.test.tsx`: validação, 409, erro de campo vindo do servidor, lista vazia, erro 500, somente leitura.

## 7. Critérios de aceite

- [ ] Pesquisador cria, edita e exclui em planejamento.
- [ ] Coordenador e outros estados: somente leitura.

## 8. Cuidados e fora de escopo

- Peso e determinante só aparecem para o tipo inclusão e não são enviados na exclusão.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
