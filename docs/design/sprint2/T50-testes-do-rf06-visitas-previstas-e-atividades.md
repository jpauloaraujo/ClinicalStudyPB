# T50 · Testes do RF06 (visitas previstas e atividades)

> **Sprint 2** · PBI-12 · RF06 Visitas previstas e atividades  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Qui 05/11  
> **Depende de:** T48, T49  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cobrir as regras de visitas e atividades com testes dedicados, no mesmo padrão dos testes de critérios (T47).

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existe: `RecrutamentoIntegrationTest.visitasEAcoes` (fluxo feliz) e `AbaVisitas.test.tsx` (componente).
- Padrão a seguir: `CriterioServiceTest` (Mockito), `CriterioRequestValidacaoTest` (Validator sem Spring) e `CriterioEndpointTest` (MockMvc com tokens reais).
- Helper de login: `support/TokenDeTeste`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VisitaServiceTest.java` | Existente (criado na implementação) | Regras do serviço com repositórios simulados |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VisitaRequestValidacaoTest.java` | Existente (criado na implementação) | Limites do DTO, sem Spring |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VisitaEndpointTest.java` | Existente (criado na implementação) | Endpoints de visitas e ações com MockMvc |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaVisitas.test.tsx` | Existente (ampliado na implementação) | Revisar e completar lacunas (lista vazia, erro 500) |

## 4. O que fazer em cada arquivo

### VisitaServiceTest

- Dia repetido (409 `VISITA_DIA_DUPLICADO`); editar a visita mantendo o próprio dia não é conflito.
- Estudo fora de planejamento bloqueia criar, editar e excluir (parametrizar sobre os 3 estados).
- Remoção da visita remove as ações (cascata).
- A ordem é recalculada pelo dia.

### VisitaRequestValidacaoTest

- Nome: ausente, em branco, 100 e 101 caracteres. Dia: -1 inválido, 0 válido. Tolerância: -1 e 31 inválidas, 0 e 30 válidas. Ação: categoria ausente e descrição com 501 caracteres.

### VisitaEndpointTest

- 201 ao criar; 400 com o nome do campo; 403 para coordenador, participante e administrador nas escritas; 401 sem token; 404 (estudo, visita e ação inexistentes); 409 (dia repetido e estudo bloqueado).
- Ordenação da resposta e ações aninhadas na visita.

### Apoio compartilhado dos testes (criar uma vez, reutilizar nas tasks T50, T53, T56, T59 e T62)

- Os auxiliares do `RecrutamentoIntegrationTest` são métodos privados e não servem a outras classes. Criar `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` (utilitário ao lado de `TokenDeTeste`), se ainda não existir, com métodos estáticos que recebem o `MockMvc` e o token do perfil.
- `criarEstudo(mvc, tokenPesquisador, limite)` → id: `POST /api/estudos` com `status = PLANEJAMENTO` e o responsável do seed `22222222-2222-4222-8222-222222222222`.
- `criarCriterio(mvc, token, estudoId, tipo, titulo, descricao)` → id; `criarVisita(mvc, token, estudoId, nome, dia)` → id.
- `avancar(mvc, tokenPesquisador, estudoId, destino)`: `POST /api/estudos/{id}/transicoes`. `estudoEmRecrutamento(mvc, tokenPesquisador, limite)` → id: cria um critério de inclusão, uma visita e avança para recrutamento.
- `criarCandidato(mvc, tokenCoordenador, estudoId, nome)` → id; `avaliarAprovado(mvc, tokenCoordenador, candidatoId, estudoId)`: responde "se aplica" a todos os critérios de inclusão e "não se aplica" aos de exclusão; `consentir(mvc, tokenCoordenador, candidatoId)`.
- Quem chegar primeiro cria a classe com essas assinaturas; os demais só a reutilizam, sem mudar as assinaturas.

## 6. Testes

- Resultado (07/11/2026): VisitaServiceTest (14), VisitaRequestValidacaoTest (19) e VisitaEndpointTest (24) passam; AbaVisitas.test.tsx foi de 9 para 17 testes. Cada regra da T48 tem teste.

## 7. Critérios de aceite

- [ ] Uma visita com atividades é criada, consultada e removida em um teste de integração.
- [ ] Cobertura do `VisitaService` próxima de 100% das linhas.

## 8. Cuidados e fora de escopo

- Quem testa não é quem implementou: I7 revisa o trabalho de I4 e I5.
- Usar `@Transactional` nos testes de integração para reverter os dados.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
