# T49 · Tela de visitas e atividades

> **Sprint 2** · PBI-12 · RF06 Visitas previstas e atividades  
> **Responsável:** I5 · **Estimativa:** 5h · **Prazo:** Qua 04/11  
> **Depende de:** T39, T40, T48  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Aba Visitas com a lista ordenada por dia, cada visita como cartão expansível com suas atividades, e formulários de visita e de atividade.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaVisitas.tsx` | Existente | Lista, cartões expansíveis, formulários e somente leitura |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaVisitas.test.tsx` | Existente | Testes de componente |
| `web/ClinicalStudyPB/src/api/visitas.ts` | Existente | `visitasApi` (visitas e ações) |

## 4. O que fazer em cada arquivo

### Regras da tela

- Formulário de visita: nome, dia previsto e tolerância (0 a 30), com os erros do servidor por campo (dia repetido incluído).
- Formulário de ação: nome, descrição e categoria em lista de seleção (`rotulosCategoria`).
- Somente leitura fora de planejamento e para o coordenador.

## 6. Testes

- `AbaVisitas.test.tsx`.

## 7. Critérios de aceite

- [ ] Visitas aparecem ordenadas pelo dia.
- [ ] Criar, editar e excluir funcionam em planejamento; nos demais casos a aba é somente leitura.

## 8. Cuidados e fora de escopo

- Quem não está em planejamento só consulta; a API também recusa.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
