# T57 · Backend de consentimento

> **Sprint 2** · PBI-15 · RF10 Consentimento simulado  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qua 04/11  
> **Depende de:** T51  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Registrar o consentimento simulado de um candidato, com comprovante de data, responsável e versão do termo.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ConsentimentoController.java` | Existente | `GET` e `POST /api/candidatos/{candidatoId}/consentimento` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ConsentimentoService.java` | Existente | Regras e persistência |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/ConsentimentoRequest.java, ConsentimentoResponse.java` | Existente | Request: `dataHora` e `versaoTermo` opcionais; response: `id`, `candidatoId`, `dataHora`, `responsavel` (nome de quem aplicou o termo) e `versaoTermo` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Consentimento.java` | Existente | Entidade (um por candidato) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ConsentimentoRepository.java` | Existente | `existsByCandidatoId` |

## 4. O que fazer em cada arquivo

### Regras

- Estudo em recrutamento (409 `ESTUDO_FORA_DE_RECRUTAMENTO`); candidato não incluído (409 `CANDIDATO_JA_INCLUIDO`); um por candidato (409 `CONSENTIMENTO_DUPLICADO`).
- Data e hora padrão: agora; data futura é recusada (400).
- Versão padrão do termo: propriedade `app.consentimento.versao-termo` (valor `TCLE-SIMULADO v1`).
- Se o candidato estava `ELEGIVEL`, a situação passa a `CONSENTIDO`. O consentimento independe da avaliação.
- `GET` sem consentimento registrado responde 404.

## 5. Contrato, regras e códigos de erro

- Escrita: coordenador. Leitura: coordenador e pesquisador. Revogação fica fora (RF16, Sprint 3).

## 6. Testes

- `RecrutamentoIntegrationTest.consentimentoPodeVirAntesDaAvaliacao`; suítes dedicadas na T59: `ConsentimentoServiceTest` e `ConsentimentoEndpointTest`.

## 7. Critérios de aceite

- [ ] Registrar duas vezes dá 409.
- [ ] O resumo do consentimento aparece no detalhe do candidato.

## 8. Cuidados e fora de escopo

- Os testes dedicados de serviço e de endpoint foram escritos na T59.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
