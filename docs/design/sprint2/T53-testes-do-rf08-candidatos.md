# T53 · Testes do RF08 (candidatos)

> **Sprint 2** · PBI-13 · RF08 Cadastro de candidatos  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qui 05/11  
> **Depende de:** T51  
> **Situação:** 🟡 Parcialmente implementada (o documento separa o que existe do que falta)

## 1. Objetivo

Cobrir as regras de candidatos com testes dedicados, no padrão de `CriterioServiceTest` e `CriterioEndpointTest`.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existe cobertura geral em `RecrutamentoIntegrationTest` (`fluxoCompleto`, `listaPaginadaComFiltro`, `permissoesPorPerfil`) e em `AbaCandidatos.test.tsx`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CandidatoServiceTest.java` | Criar | Regras do serviço com repositórios simulados |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CandidatoRequestValidacaoTest.java` | Criar | Limites do DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CandidatoEndpointTest.java` | Criar | Endpoints com MockMvc e tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/CandidatoCodigoSequencialTest.java` | Criar | Geração do código em cadastros seguidos e em estudos diferentes |

## 4. O que fazer em cada arquivo

### CandidatoServiceTest

- Duplicidade: mesmo nome fictício (ignorando maiúsculas) e mesma data de nascimento no estudo (409 `CANDIDATO_DUPLICADO`), também na edição de outro candidato.
- Estudo fora de recrutamento: 409 em cada estado diferente de recrutamento (parametrizado).
- Edição de candidato incluído: 409 `CANDIDATO_JA_INCLUIDO`.
- Situação inicial `CADASTRADO`.

### CandidatoRequestValidacaoTest

- Data de nascimento futura e hoje inválidas; nome em branco, 120 e 121 caracteres; contato com 181.

### CandidatoEndpointTest

- 201 com `Location` e código gerado; 400 com o nome do campo; 403 para pesquisador e participante nas escritas, 403 para administrador em tudo; 401; 404; 409.
- Paginação (`pagina`, `tamanho`, `totalElementos`, `totalPaginas`) e filtro por situação.

### CandidatoCodigoSequencialTest

- Três cadastros seguidos geram `CAND-001`, `CAND-002`, `CAND-003`; um segundo estudo reinicia em `CAND-001`.

### Apoio compartilhado dos testes (criar uma vez, reutilizar nas tasks T50, T53, T56, T59 e T62)

- Os auxiliares do `RecrutamentoIntegrationTest` são métodos privados e não servem a outras classes. Criar `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` (utilitário ao lado de `TokenDeTeste`), se ainda não existir, com métodos estáticos que recebem o `MockMvc` e o token do perfil.
- `criarEstudo(mvc, tokenPesquisador, limite)` → id: `POST /api/estudos` com `status = PLANEJAMENTO` e o responsável do seed `22222222-2222-4222-8222-222222222222`.
- `criarCriterio(mvc, token, estudoId, tipo, titulo, descricao)` → id; `criarVisita(mvc, token, estudoId, nome, dia)` → id.
- `avancar(mvc, tokenPesquisador, estudoId, destino)`: `POST /api/estudos/{id}/transicoes`. `estudoEmRecrutamento(mvc, tokenPesquisador, limite)` → id: cria um critério de inclusão, uma visita e avança para recrutamento.
- `criarCandidato(mvc, tokenCoordenador, estudoId, nome)` → id; `avaliarAprovado(mvc, tokenCoordenador, candidatoId, estudoId)`: responde "se aplica" a todos os critérios de inclusão e "não se aplica" aos de exclusão; `consentir(mvc, tokenCoordenador, candidatoId)`.
- Quem chegar primeiro cria a classe com essas assinaturas; os demais só a reutilizam, sem mudar as assinaturas.

## 7. Critérios de aceite

- [ ] Cada regra da T51 tem um teste que falha se ela for removida.

## 8. Cuidados e fora de escopo

- Preparar o estudo em recrutamento pela transição (não pelo `PUT`), como em `EstudoTransicaoRf07Test`.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
