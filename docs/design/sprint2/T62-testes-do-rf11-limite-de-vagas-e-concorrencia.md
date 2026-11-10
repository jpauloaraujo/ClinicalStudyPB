# T62 · Testes do RF11 (limite de vagas e concorrência)

> **Sprint 2** · PBI-16 · RF11 Inclusão de participantes  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Sex 06/11  
> **Depende de:** T60  
> **Situação:** ✅ Implementada (`ParticipacaoEndpointTest` e `ParticipacaoConcorrenciaTest`, com limpeza dos dados no `@AfterEach`)

## 1. Objetivo

Provar que a inclusão cumpre cada regra e que, com uma só vaga e várias inclusões ao mesmo tempo, exatamente uma é aceita.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existe: `ParticipacaoServiceTest` (cada pré-condição isolada, a ordem das verificações e o limite exato) e `RecrutamentoIntegrationTest.vagasEsgotadas`.
- Foram acrescentados: o teste de endpoint por código de erro (`ParticipacaoEndpointTest`) e o teste de concorrência (`ParticipacaoConcorrenciaTest`).
- Base de dados de teste: `EstudoTransicaoRf07Test` mostra como preparar um estudo em recrutamento pela transição.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ParticipacaoEndpointTest.java` | Criar | 201, 400, 401, 403, 404 e um 409 por regra, conferindo o `codigo` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ParticipacaoConcorrenciaTest.java` | Criar | Várias inclusões simultâneas na última vaga |

## 4. O que fazer em cada arquivo

### ParticipacaoEndpointTest

- Preparar estudo em recrutamento com critérios, visita, candidatos avaliados e consentidos (use `CenarioRecrutamento`, descrito no bloco de apoio compartilhado).
- 201 com os dados da participação; 400 sem `candidatoId`; 403 para pesquisador, participante e administrador na escrita e para administrador na leitura; 401; 404 (estudo e candidato de outro estudo).
- 409 um por regra: fora de recrutamento, já incluído, não elegível, sem consentimento, vagas esgotadas.
- `GET .../participantes/vagas` acompanha a contagem: `restantes` cai de 1 para 0.

### ParticipacaoConcorrenciaTest

- **Não usar `@Transactional` na classe**: cada thread precisa da sua própria transação confirmada. Criar os dados pelo MockMvc/serviços e **limpar no `@AfterEach`** (participações, respostas, avaliações, consentimentos, candidatos, critérios, visitas, histórico e o estudo), para não sujar os outros testes que contam registros.
- Cenário: limite 1; N candidatos prontos (por exemplo 6); um `ExecutorService` com N threads e um `CountDownLatch` de largada; cada thread chama `POST /api/estudos/{id}/participantes` com um candidato diferente.
- Verificar: exatamente 1 resposta 201; as demais 409 `VAGAS_ESGOTADAS`; `GET .../vagas` mostra `incluidos = 1`; apenas 1 candidato com situação `INCLUIDO`.
- Segundo cenário: a mesma pessoa incluída por duas threads: 1 resposta 201 e 1 resposta 409 `CANDIDATO_JA_INCLUIDO`.
- Usar `latch.await` com limite de tempo e capturar exceções das threads, para o teste falhar com mensagem clara em vez de travar.

### Apoio compartilhado dos testes (criar uma vez, reutilizar nas tasks T50, T53, T56, T59 e T62)

- Os auxiliares do `RecrutamentoIntegrationTest` são métodos privados e não servem a outras classes. Criar `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` (utilitário ao lado de `TokenDeTeste`), se ainda não existir, com métodos estáticos que recebem o `MockMvc` e o token do perfil.
- `criarEstudo(mvc, tokenPesquisador, limite)` → id: `POST /api/estudos` com `status = PLANEJAMENTO` e o responsável do seed `22222222-2222-4222-8222-222222222222`.
- `criarCriterio(mvc, token, estudoId, tipo, titulo, descricao)` → id; `criarVisita(mvc, token, estudoId, nome, dia)` → id.
- `avancar(mvc, tokenPesquisador, estudoId, destino)`: `POST /api/estudos/{id}/transicoes`. `estudoEmRecrutamento(mvc, tokenPesquisador, limite)` → id: cria um critério de inclusão, uma visita e avança para recrutamento.
- `criarCandidato(mvc, tokenCoordenador, estudoId, nome)` → id; `avaliarAprovado(mvc, tokenCoordenador, candidatoId, estudoId)`: responde "se aplica" a todos os critérios de inclusão e "não se aplica" aos de exclusão; `consentir(mvc, tokenCoordenador, candidatoId)`.
- Quem chegar primeiro cria a classe com essas assinaturas; os demais só a reutilizam, sem mudar as assinaturas.

## 6. Testes

- Rodar também no job `backend-postgres` (no PostgreSQL o bloqueio de linha é o mecanismo real da produção).

## 7. Critérios de aceite

- [ ] A última vaga é aceita e a seguinte recusada.
- [ ] Em concorrência, exatamente uma inclusão passa e o total nunca excede o limite.
- [ ] Resultado registrado no relatório de testes.

## 8. Cuidados e fora de escopo

- O teste de concorrência é o único que não reverte por `@Transactional`: sempre limpar os dados.
- Se ficar instável, aumentar o número de threads antes de relaxar a asserção.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
