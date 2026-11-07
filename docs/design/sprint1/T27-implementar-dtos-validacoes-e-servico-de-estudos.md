# T27 · Implementar DTOs, validações e serviço de estudos

> **Sprint 1** · PBI-07 · RF04 Cadastro de estudos clínicos  
> **Responsável:** I3 · **Estimativa:** 6h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T08  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cadastrar e manter estudos com título, objetivo, responsável, datas e limite de participantes, recusando dados inválidos antes de gravar.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Entidade `Estudo` (T08) já traz o campo `status` com padrão `PLANEJAMENTO`.
- Desde a Sprint 2 (RF07) o estado só muda pela transição: ver T41 e T42.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/EstudoRequest.java` | Existente | Validações Bean Validation com mensagens em português |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/EstudoService.java` | Existente | Regras de negócio, criação, edição e exclusão |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/EstudoRepository.java` | Existente | Persistência e `buscarComBloqueio` (usado nas inclusões) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Estudo.java` | Existente | Entidade JPA |

## 4. O que fazer em cada arquivo

### EstudoRequest

- `titulo` obrigatório, até 180; `objetivo` obrigatório; `responsavelId` obrigatório; `limiteParticipantes` obrigatório e no mínimo 1; `dataInicio` e `dataFim` opcionais; `status` obrigatório.

### EstudoService

- Data final anterior à inicial: `RegraNegocioException` (400).
- Responsável inexistente: 404; responsável que não é `PESQUISADOR`: 400.
- `criar` só aceita `status = PLANEJAMENTO` (409 `ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO`); `atualizar` só aceita reenviar o estado atual (409 `ESTADO_ALTERADO_FORA_DA_TRANSICAO`).
- `excluir` recusa estudo com candidatos (409 `ESTUDO_COM_CANDIDATOS`), porque a FK é `ON DELETE RESTRICT`.

## 5. Contrato, regras e códigos de erro

- Erros: 400 validação ou regra, 404 inexistente, 409 regra de recrutamento.

## 6. Testes

- `EstudoServiceTest` (regras isoladas com Mockito) e `EstudoIntegrationTest` (T30).

## 7. Critérios de aceite

- [ ] Pesquisador cadastra estudo válido; dados inválidos são recusados com mensagem clara.
- [ ] Os dados persistem no PostgreSQL.

## 8. Cuidados e fora de escopo

- Qualquer pesquisador edita e exclui qualquer estudo (decisão registrada em `docs/sprint1/status-sprint1.md`, seção 4).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
