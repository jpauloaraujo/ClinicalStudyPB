# T103 · Endpoints, autorização e Swagger da retirada

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qua 18/11  
> **Depende de:** T102  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Expor o registro e a consulta da retirada com escrita só do coordenador e leitura dos dois perfis, no formato único de erro, e conferir o Swagger contra o contrato. O serviço continua independente do endpoint, para o pedido de retirada do app (RF20, Sprint 4) reaproveitá-lo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `RetiradaService` (T102) e o contrato (T101); `Permissoes.LEITURA` e `Permissoes.ESCRITA_RECRUTAMENTO`; `Autenticacao.idDe(jwt)`; `ApiExceptionHandler`.
- O `ParticipacaoResponse` já traz `situacao` desde a T101.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/RetiradaController.java` | Criar | `POST` e `GET` de `/api/participacoes/{participacaoId}/retirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RetiradaRequest.java` | Criar | `motivo`, `solicitante`, `dataRetirada` com validação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RetiradaResponse.java` | Criar | Com `visitasCanceladas` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso da retirada (12 operações) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Só se o Swagger divergir (corrige no mesmo PR) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T103-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints

- **RQ-T103-01** `POST /api/participacoes/{participacaoId}/retirada` (só coordenador) recebe `RetiradaRequest` (`motivo` `@NotBlank` e `@Size(min = 5, max = 500)`, `solicitante` `@NotNull`, `dataRetirada` `@NotNull`) e responde **201** com `RetiradaResponse` (`id`, `participacaoId`, `motivo`, `solicitante`, `dataRetirada`, `registradoPor`, `registradoEm`, `visitasCanceladas`) e `Location` do `GET`.
- **RQ-T103-02** `GET /api/participacoes/{participacaoId}/retirada` (leitura: pesquisador e coordenador) devolve a mesma resposta, com `visitasCanceladas` calculado (visitas `CANCELADA` com o motivo "retirada do estudo"); **404** se o participante ainda não foi retirado ou a participação não existe.
- **RQ-T103-03** Erros no formato único: 400 (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `MOTIVO_OBRIGATORIO`, `DATA_RETIRADA_FUTURA`), 403 (pesquisador na escrita; administrador e participante em tudo), 404, 409 (`PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO`) e 401.

### Swagger e independência do serviço

- **RQ-T103-04** `@Tag(name = "Retirada")` e `@Operation` com a descrição da regra 6 (irreversível; cancela visitas futuras; mantém a vaga). Remover o `@Disabled("T103")` do `ContratoOpenApiSprint3Test`, atualizar a contagem (12) e corrigir o YAML **no mesmo Pull Request** se o Swagger divergir.
- **RQ-T103-05** O controller só converte DTO e chama `RetiradaService`; o serviço **não** depende do controller (o RF20 do app chamará o mesmo serviço com `solicitante = PARTICIPANTE`).

## 5. Contrato, regras e códigos de erro

- Operações 11 e 12 da seção 2 das convenções.

## 6. Testes

- A bateria completa é a T106; aqui, o `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada operação.

## 7. Critérios de aceite

- [ ] O coordenador registra e consulta a retirada; o pesquisador só consulta.
- [ ] Os códigos de status e de erro seguem o contrato; o Swagger e o YAML coincidem.

## 8. Cuidados e fora de escopo

- Não criar endpoint de desfazer retirada.
- Não expor o motivo da retirada em endpoints do app (o app só mostra a data, T109).

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
