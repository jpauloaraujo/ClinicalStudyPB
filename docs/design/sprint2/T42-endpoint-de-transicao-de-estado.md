# T42 · Endpoint de transição de estado

> **Sprint 2** · PBI-10 · RF07 Estados do estudo  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Ter 03/11  
> **Depende de:** T37, T41  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Permitir que o pesquisador avance o estudo um estado por vez pela API e consulte o histórico das mudanças.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Regra pura: `domain/regra/MaquinaEstadosEstudo` (T41). Serviço: `EstudoTransicaoService`.
- Diagrama de sequência: `docs/diagramas/sequencia-avancar-estado.md`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/EstudoTransicaoController.java` | Existente | `POST /api/estudos/{estudoId}/transicoes` e `GET /api/estudos/{estudoId}/historico-status` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/TransicaoRequest.java` | Existente | `destino` obrigatório |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/HistoricoStatusResponse.java` | Existente | id, estudoId, estadoAnterior, estadoNovo, dataHora |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/EstudoTransicaoService.java` | Existente | Aplica a regra e grava o histórico em uma transação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/HistoricoStatusService.java` | Existente | Leitura do histórico |

## 4. O que fazer em cada arquivo

### Controller

- `POST`: `@PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)` (só pesquisador); devolve o `Estudo` já no novo estado.
- `GET` do histórico: `Permissoes.LEITURA`.

### Erros 409 (código na resposta)

- `TRANSICAO_ESTADO_INVALIDA`, `ESTUDO_SEM_CRITERIO_INCLUSAO`, `ESTUDO_SEM_VISITA_PREVISTA`.
- 400 para destino ausente ou inválido; 404 para estudo inexistente; 403 para coordenador, participante e administrador.

### Atualização (unificação de 07/11/2026)

- `TransicaoRequest` aceita também o nome `novoEstado` (`@JsonAlias`) além de `destino`. O `PUT` de estudo usa o mesmo bloqueio da transição para não restaurar um estado antigo. Testes: `PlanejamentoIntegrationTest.percorreSequenciaPorHttpComHistoricoSemPermitirDesvios` e `putNaoAlteraEstadoNemCriaHistorico`.

## 5. Contrato, regras e códigos de erro

- Contrato: `docs/api/openapi-sprint2.yaml`, operações `avancarEstado` e do histórico.
- Este é o único caminho para mudar o estado; `POST` e `PUT /api/estudos` não o alteram.

## 6. Testes

- `EstudoTransicaoEndpointTest`, `EstudoTransicaoRf07Test` (T44).

## 7. Critérios de aceite

- [ ] 200 no caminho feliz com o estudo atualizado.
- [ ] Cada regra violada responde 409 com o código certo.
- [ ] Coincide com o YAML (verificado pelo `ContratoOpenApiTest`).

## 8. Cuidados e fora de escopo

- Não validar a sequência de estados no controller: a regra fica em `MaquinaEstadosEstudo`.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
