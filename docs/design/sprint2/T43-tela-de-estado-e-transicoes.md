# T43 · Tela de estado e transições

> **Sprint 2** · PBI-10 · RF07 Estados do estudo  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Qua 04/11  
> **Depende de:** T39, T40, T42  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Mostrar os quatro estados do estudo no cabeçalho e permitir que o pesquisador avance para o próximo, com confirmação e mensagem clara quando uma regra impedir.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/IndicadorEstado.tsx` | Existente | Indicador dos estados, botão "avançar", confirmação e alerta de erro |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/IndicadorEstado.test.tsx` | Existente | Botão oculto para o coordenador, confirmação e erro de regra |
| `web/ClinicalStudyPB/src/api/estadoEstudo.ts` | Existente | `estadoEstudoApi.avancar(estudoId, destino)` e histórico |
| `web/ClinicalStudyPB/src/components/ui/Confirmacao.tsx` | Existente | Diálogo de confirmação reutilizável |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/EstudoDetalhePage.tsx` | Existente | Renderiza o indicador e recarrega o estudo depois de avançar |

## 4. O que fazer em cada arquivo

### Regras da tela

- O botão só aparece para o pesquisador e quando existe próximo estado.
- A confirmação avisa que não é possível voltar.
- Um 409 mostra a mensagem do backend em um alerta (por exemplo, "adicione ao menos um critério de inclusão").

## 6. Testes

- `IndicadorEstado.test.tsx`.

## 7. Critérios de aceite

- [ ] Coordenador não vê o botão.
- [ ] Confirmar avança e atualiza o cabeçalho.
- [ ] Erro de regra aparece em alerta.

## 8. Cuidados e fora de escopo

- A sequência de estados vem de `statusEstudo` em `types/estudo.ts`, espelho do enum do backend.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
