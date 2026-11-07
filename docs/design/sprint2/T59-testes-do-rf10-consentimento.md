# T59 · Testes do RF10 (consentimento)

> **Sprint 2** · PBI-15 · RF10 Consentimento simulado  
> **Responsável:** I7 · **Estimativa:** 2h · **Prazo:** Sex 06/11  
> **Depende de:** T57, T58  
> **Situação:** 🟡 Parcialmente implementada (o documento separa o que existe do que falta)

## 1. Objetivo

Cobrir o consentimento com testes dedicados de serviço, endpoint e componente.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existe cobertura indireta em `RecrutamentoIntegrationTest` (consentimento antes da avaliação, fluxo completo).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ConsentimentoServiceTest.java` | Criar | Regras do serviço com Mockito |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ConsentimentoEndpointTest.java` | Criar | Endpoints com MockMvc e tokens reais |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoConsentimento.test.tsx` | Revisar | Escrito na T58; conferir as lacunas |

## 4. O que fazer em cada arquivo

### ConsentimentoServiceTest

- Data futura recusada; data omitida usa "agora"; versão omitida usa a padrão configurada.
- Duplicidade (409 `CONSENTIMENTO_DUPLICADO`), estudo fora de recrutamento e candidato incluído.
- Situação `ELEGIVEL` vira `CONSENTIDO`; as demais situações não mudam.

### ConsentimentoEndpointTest

- 201 no registro, com `responsavel` e `versaoTermo` na resposta; 400 com data futura; 403 para pesquisador, participante e administrador nas escritas; 401; 404 (candidato inexistente e `GET` sem consentimento); 409.
- O detalhe do candidato (`GET /api/candidatos/{id}`) passa a trazer `consentimentoRegistrado = true` e a data.

### Apoio compartilhado dos testes (criar uma vez, reutilizar nas tasks T50, T53, T56, T59 e T62)

- Os auxiliares do `RecrutamentoIntegrationTest` são métodos privados e não servem a outras classes. Criar `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` (utilitário ao lado de `TokenDeTeste`), se ainda não existir, com métodos estáticos que recebem o `MockMvc` e o token do perfil.
- `criarEstudo(mvc, tokenPesquisador, limite)` → id: `POST /api/estudos` com `status = PLANEJAMENTO` e o responsável do seed `22222222-2222-4222-8222-222222222222`.
- `criarCriterio(mvc, token, estudoId, tipo, titulo, descricao)` → id; `criarVisita(mvc, token, estudoId, nome, dia)` → id.
- `avancar(mvc, tokenPesquisador, estudoId, destino)`: `POST /api/estudos/{id}/transicoes`. `estudoEmRecrutamento(mvc, tokenPesquisador, limite)` → id: cria um critério de inclusão, uma visita e avança para recrutamento.
- `criarCandidato(mvc, tokenCoordenador, estudoId, nome)` → id; `avaliarAprovado(mvc, tokenCoordenador, candidatoId, estudoId)`: responde "se aplica" a todos os critérios de inclusão e "não se aplica" aos de exclusão; `consentir(mvc, tokenCoordenador, candidatoId)`.
- Quem chegar primeiro cria a classe com essas assinaturas; os demais só a reutilizam, sem mudar as assinaturas.

## 7. Critérios de aceite

- [ ] Cada regra da T57 tem um teste que falha se ela for removida.

## 8. Cuidados e fora de escopo

- O `POST` de consentimento responde 201 (`@ResponseStatus(CREATED)`).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
