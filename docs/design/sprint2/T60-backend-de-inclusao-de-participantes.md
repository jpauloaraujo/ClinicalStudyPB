# T60 · Backend de inclusão de participantes

> **Sprint 2** · PBI-16 · RF11 Inclusão de participantes  
> **Responsável:** I1 · **Estimativa:** 7h · **Prazo:** Qui 05/11  
> **Depende de:** T41, T54, T57  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Incluir um candidato elegível e com consentimento como participante do estudo, sem nunca ultrapassar o limite de vagas, com um código de erro distinto para cada bloqueio.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Diagrama: `docs/diagramas/sequencia-inclusao-participante.md`. Pré-condições: `docs/diagramas/dominio.md`, seção 3.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ParticipacaoController.java` | Existente | `GET` e `POST /api/estudos/{estudoId}/participantes` e `GET .../participantes/vagas` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ParticipacaoService.java` | Existente | Inclusão em uma transação, com bloqueio de escrita no estudo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/EstudoRepository.java` | Existente | `buscarComBloqueio` (`PESSIMISTIC_WRITE`) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/InclusaoRequest.java, ParticipacaoResponse.java, VagasResponse.java` | Existente | Request `{candidatoId}`; response com código, nome, data de inclusão e quem incluiu; vagas `{incluidos, limite, restantes}` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Participacao.java, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Existente | Uma participação por candidato (UNIQUE) |

## 4. O que fazer em cada arquivo

### Ordem das verificações em `incluir`

- 1. Estudo carregado com bloqueio de escrita; em recrutamento (409 `ESTUDO_FORA_DE_RECRUTAMENTO`).
- 2. Candidato do estudo (404 se for de outro) e ainda não incluído (409 `CANDIDATO_JA_INCLUIDO`).
- 3. Última avaliação vigente aprovada (409 `CANDIDATO_NAO_ELEGIVEL`).
- 4. Consentimento registrado (409 `CONSENTIMENTO_AUSENTE`).
- 5. Participantes atuais menores que o limite (409 `VAGAS_ESGOTADAS`).
- Se tudo passa: gravar a participação com data e usuário e mudar a situação para `INCLUIDO`.

## 5. Contrato, regras e códigos de erro

- Escrita: coordenador. Leitura (lista e vagas): coordenador e pesquisador. `POST` responde 201.

## 6. Testes

- `ParticipacaoServiceTest` (8 testes) e `RecrutamentoIntegrationTest`; endpoint e concorrência na T62.

## 7. Critérios de aceite

- [ ] Uma inclusão válida funciona e cada regra violada é recusada com o código certo.
- [ ] Duas inclusões simultâneas na última vaga: só uma passa (T62).

## 8. Cuidados e fora de escopo

- O bloqueio só funciona dentro da transação do serviço; não chamar `incluir` de fora de uma transação.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
