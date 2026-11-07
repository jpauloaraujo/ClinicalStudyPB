# T55 · Tela de avaliação de elegibilidade

> **Sprint 2** · PBI-14 · RF09 Avaliação de elegibilidade  
> **Responsável:** I7 · **Estimativa:** 6h · **Prazo:** Qui 05/11  
> **Depende de:** T52, T54  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Na ficha do candidato, formulário para responder cada critério, resultado calculado pelo backend e histórico das avaliações.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoAvaliacao.tsx` | Existente | Formulário em dois grupos, resultado, histórico e tratamento de erros |
| `web/ClinicalStudyPB/src/api/avaliacoes.ts` | Existente | `avaliacoesApi.historico` e `avaliar` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/FichaCandidatoPage.test.tsx` | Existente | Cobre o formulário completo, o botão desabilitado e o resultado |

## 4. O que fazer em cada arquivo

### Regras da tela

- Cada critério tem "se aplica" / "não se aplica" e observação; nos critérios de exclusão, "se aplica" reprova o candidato (o texto deixa isso claro).
- Salvar fica desabilitado até todos serem respondidos.
- Mostrar o resultado com destaque e os critérios reprovadores; listar o histórico.
- Tratar 400 e 409 com a mensagem do servidor.

## 6. Testes

- `FichaCandidatoPage.test.tsx`.

## 7. Critérios de aceite

- [ ] Não é possível salvar com critério sem resposta.
- [ ] O resultado vem do backend, não é calculado na tela.

## 8. Cuidados e fora de escopo

- Depois de salvar, recarregar a ficha para atualizar a situação do candidato.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
