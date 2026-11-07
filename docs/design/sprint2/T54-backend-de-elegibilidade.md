# T54 · Backend de elegibilidade

> **Sprint 2** · PBI-14 · RF09 Avaliação de elegibilidade  
> **Responsável:** I3 · **Estimativa:** 6h · **Prazo:** Qua 04/11  
> **Depende de:** T45, T51  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Registrar a avaliação de elegibilidade de um candidato (uma resposta por critério) e calcular o resultado, preservando o histórico.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Regra pura: `domain/regra/RegraElegibilidade` (`avaliar(criterios, aplica)` devolve `Resultado(elegivel, pontuacao, falhas)`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/RegraElegibilidade.java` | Existente | Função pura, sem Spring |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AvaliacaoService.java` | Existente | Valida as respostas, grava e atualiza a situação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AvaliacaoController.java` | Existente | `GET` e `POST /api/candidatos/{candidatoId}/avaliacoes` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/AvaliacaoRequest.java, AvaliacaoResponse.java` | Existente | Respostas `{criterioId, aplica, observacao}`; resposta com resultado, pontuação, avaliador e critérios reprovadores |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/AvaliacaoElegibilidade.java, RespostaAvaliacao.java` | Existente | Entidades |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/AvaliacaoElegibilidadeRepository.java, RespostaAvaliacaoRepository.java` | Existente | Persistência e histórico |

## 4. O que fazer em cada arquivo

### Regra de elegibilidade

- Elegível quando todos os critérios de inclusão **determinantes** se aplicam e nenhum de exclusão se aplica; inclusão não determinante só altera a pontuação.
- O resultado é `APROVADO` ou `REPROVADO`, com a lista dos critérios que reprovaram.

### Serviço

- Exige resposta para todos os critérios do estudo, sem faltar, sem sobrar e sem repetir (400); critério de outro estudo é recusado.
- Bloqueia se o estudo não está em recrutamento (409 `ESTUDO_FORA_DE_RECRUTAMENTO`) ou se o candidato já foi incluído (409 `CANDIDATO_JA_INCLUIDO`).
- A nova avaliação passa a valer; a anterior fica no histórico. A situação do candidato vira `NAO_ELEGIVEL` se reprovado e, se aprovado, `CONSENTIDO` quando já há consentimento ou `ELEGIVEL` quando não há.
- Ao ler o histórico, desembrulhar os proxies do Hibernate (`Hibernate.unproxy`), senão os critérios reprovadores saem vazios (regressão em `AvaliacaoHistoricoTest`).

## 5. Contrato, regras e códigos de erro

- Escrita: coordenador. Leitura: coordenador e pesquisador.

## 6. Testes

- `DominioRecrutamentoTest`, `AvaliacaoServiceTest`, `AvaliacaoHistoricoTest` e `RecrutamentoIntegrationTest`.

## 7. Critérios de aceite

- [ ] Respostas incompletas são recusadas.
- [ ] Reavaliar preserva o histórico.
- [ ] Candidato incluído e estudo fora de recrutamento resultam em 409.

## 8. Cuidados e fora de escopo

- Divergência conhecida com o plano: o plano diz "todos os de inclusão"; o código usa só os determinantes (decisão da especificação).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
