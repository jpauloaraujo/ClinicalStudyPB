# T45 · Backend de critérios

> **Sprint 2** · PBI-11 · RF05 Critérios de inclusão e exclusão  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Sex 30/10  
> **Depende de:** T37, T38  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cadastrar, editar, excluir e listar critérios de inclusão e exclusão de um estudo, só enquanto ele está em planejamento.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelo: `Criterio` (abstrata), `CriterioInclusao` (peso e determinante) e `CriterioExclusao`, na tabela `criterios` (V4).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/CriterioController.java` | Existente | `/api/estudos/{estudoId}/criterios` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/CriterioService.java` | Existente | Validações e regra de estado |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/CriterioRequest.java` | Existente | tipo, título (até 120), descrição (5 a 500), peso (> 0) e determinante |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/CriterioResponse.java` | Existente | Inclui peso e determinante só nos de inclusão |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/CriterioRepository.java` | Existente | Consultas por estudo; contagem de critérios de inclusão (usada na transição) |

## 4. O que fazer em cada arquivo

### Regras do serviço

- Estudo fora de planejamento: 409 `ESTUDO_FORA_DE_PLANEJAMENTO` (criar, editar e excluir).
- Título repetido (ignorando maiúsculas) no mesmo tipo e estudo: 409 `CRITERIO_DUPLICADO`; descrição repetida: o mesmo código.
- O tipo não muda na edição (400). Inclusão sem peso recebe 1 e é determinante por padrão.
- Critério já usado em avaliação não é excluído (409 `CRITERIO_EM_USO`; na prática o estado bloqueia antes).

## 5. Contrato, regras e códigos de erro

- Escrita: pesquisador. Leitura: pesquisador e coordenador. 400, 401, 403, 404, 409 conforme o YAML.

## 6. Testes

- `CriterioServiceTest`, `CriterioRequestValidacaoTest`, `CriterioEndpointTest` e `CriterioDescricaoRepetidaTest` (T47).

## 7. Critérios de aceite

- [ ] Um critério criado por API aparece na listagem.
- [ ] Depois que o estudo sai de planejamento, a escrita é recusada.

## 8. Cuidados e fora de escopo

- O peso e o flag determinante afetam a elegibilidade (T54): só critérios determinantes reprovam.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
