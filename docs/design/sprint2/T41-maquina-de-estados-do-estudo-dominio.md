# T41 · Máquina de estados do estudo (domínio)

> **Sprint 2** · PBI-10 · RF07 Estados do estudo  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Sex 30/10  
> **Depende de:** T36, T38  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Garantir, em uma classe de domínio sem Spring, que o estudo só avança um estado por vez e que sair de planejamento exige critério de inclusão e visita.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento anterior (T38, T41, T51 juntas): `docs/sprint2/T38-T41-T51-implementacao.md`. Matriz e pré-condições: `docs/diagramas/dominio.md`, seções 2 e 3.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/MaquinaEstadosEstudo.java` | Existente | Regra pura: `proximoEstado`, `transicaoPermitida`, `validar(origem, destino, criteriosInclusao, visitas)` e `rotulo` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/EstudoTransicaoService.java` | Existente | `avancar(estudoId, destino, usuarioId)`: bloqueia o estudo, valida, muda o estado e grava o histórico |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/HistoricoStatusEstudo.java, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/HistoricoStatusEstudoRepository.java` | Existente | Histórico: estudo, estado anterior, novo, usuário e data |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/CriterioRepository.java, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaPlanejadaRepository.java` | Existente | `contarInclusaoPorEstudo` e `countByEstudoId`, consultados na saída de planejamento |

## 4. O que fazer em cada arquivo

### Regras de `validar`

- Primeiro a sequência: só `destino.ordinal() == origem.ordinal() + 1` (409 `TRANSICAO_ESTADO_INVALIDA`); origem ou destino ausente também é transição inválida.
- Depois, só na saída de planejamento: sem critério de inclusão (409 `ESTUDO_SEM_CRITERIO_INCLUSAO`), sem visita (409 `ESTUDO_SEM_VISITA_PREVISTA`); o critério vem primeiro.

### Serviço

- Estudo carregado com bloqueio de escrita; conta critérios e visitas; chama `validar`; atualiza o estado; grava o histórico com o usuário; usuário inexistente impede o avanço.
- Sem dependência de Spring Web na regra pura, para testá-la sem subir o servidor.

## 6. Testes

- `MaquinaEstadosEstudoTest` (27), `EstudoTransicaoServiceTest` (7), `EstudoTransicaoCombinacoesServiceTest` (16), `EstudoTransicaoIntegrationTest` (4). Veja a T44.

## 7. Critérios de aceite

- [ ] As quatro regras funcionam em teste rápido, sem servidor.
- [ ] Cada avanço fica no histórico.

## 8. Cuidados e fora de escopo

- O estado só muda por este serviço; `POST` e `PUT /api/estudos` não o alteram.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
