# T56 · Testes do RF09 (elegibilidade)

> **Sprint 2** · PBI-14 · RF09 Avaliação de elegibilidade  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Qui 05/11  
> **Depende de:** T54  
> **Situação:** ✅ Implementada (`AvaliacaoEndpointTest`, `AvaliacaoRequestValidacaoTest` e `ElegibilidadeCombinacoesTest`, além dos testes já existentes)

## 1. Objetivo

Completar a cobertura da elegibilidade: a regra pura já tem matriz de testes; faltam os testes dos endpoints e os casos que o plano pede e ainda não existem.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existe: `DominioRecrutamentoTest` (regra pura), `AvaliacaoServiceTest` (8 testes de serviço), `AvaliacaoHistoricoTest` e a avaliação em `RecrutamentoIntegrationTest`.
- Não coberto ainda por teste dedicado: resposta de critério de outro estudo na regra pura, e os códigos HTTP 201, 400, 403, 404 e 409 em um único arquivo de endpoint.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AvaliacaoEndpointTest.java` | Criar | Endpoints de avaliação com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DominioRecrutamentoTest.java` | Alterar | Acrescentar os casos da matriz que faltam (ver abaixo) |

## 4. O que fazer em cada arquivo

### Casos da matriz da regra pura a conferir

- Tudo de inclusão aplicado e nenhuma exclusão → elegível.
- Uma inclusão determinante não aplicada → reprovado; várias falhas juntas aparecem todas.
- Estudo sem critérios de exclusão; resposta faltando; resposta de critério de outro estudo.

### AvaliacaoEndpointTest

- 201 com resultado e pontuação; 400 com respostas incompletas, repetidas ou de outro estudo; 403 para pesquisador e participante nas escritas; 401; 404 (candidato inexistente); 409 (estudo fora de recrutamento e candidato incluído).
- Reavaliação: o `GET` devolve as duas avaliações, a vigente primeiro.

### Apoio compartilhado dos testes (criar uma vez, reutilizar nas tasks T50, T53, T56, T59 e T62)

- Os auxiliares do `RecrutamentoIntegrationTest` são métodos privados e não servem a outras classes. Criar `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` (utilitário ao lado de `TokenDeTeste`), se ainda não existir, com métodos estáticos que recebem o `MockMvc` e o token do perfil.
- `criarEstudo(mvc, tokenPesquisador, limite)` → id: `POST /api/estudos` com `status = PLANEJAMENTO` e o responsável do seed `22222222-2222-4222-8222-222222222222`.
- `criarCriterio(mvc, token, estudoId, tipo, titulo, descricao)` → id; `criarVisita(mvc, token, estudoId, nome, dia)` → id.
- `avancar(mvc, tokenPesquisador, estudoId, destino)`: `POST /api/estudos/{id}/transicoes`. `estudoEmRecrutamento(mvc, tokenPesquisador, limite)` → id: cria um critério de inclusão, uma visita e avança para recrutamento.
- `criarCandidato(mvc, tokenCoordenador, estudoId, nome)` → id; `avaliarAprovado(mvc, tokenCoordenador, candidatoId, estudoId)`: responde "se aplica" a todos os critérios de inclusão e "não se aplica" aos de exclusão; `consentir(mvc, tokenCoordenador, candidatoId)`.
- Quem chegar primeiro cria a classe com essas assinaturas; os demais só a reutilizam, sem mudar as assinaturas.

## 7. Critérios de aceite

- [ ] Todas as linhas da matriz do plano têm um teste.
- [ ] Os 409 conferem o código do erro.

## 8. Cuidados e fora de escopo

- O `POST` de avaliação responde 201 (`@ResponseStatus(CREATED)`).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
