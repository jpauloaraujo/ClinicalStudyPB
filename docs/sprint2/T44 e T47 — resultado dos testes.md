# T44 e T47: resultado dos testes do RF07 e do RF05

Registro para o relatório da Sprint 2, em 02/11/2026. Os dois conjuntos de testes foram escritos **acrescentando arquivos**, sem alterar o código de produção nem os testes anteriores (a única mudança em arquivo existente é a ampliação de `AbaCriterios.test.tsx`, que a própria T47 pede).

> **Independência da revisão.** O planejamento pede que quem testa não seja quem implementou (T44: I2 revisa a lógica de I3 e o endpoint de I4; T47: I7 não implementou nada do RF05). Os arquivos abaixo foram escritos por I7 com o apoio do Claude e **ainda precisam do Pull Request revisado**. A independência da T44 só se completa se I2 revisar e assumir os testes.

## T44 · Testes do RF07 (estado do estudo)

| Passo da task | Onde está | Resultado |
|---|---|---|
| Unitários parametrizados (JUnit): 16 combinações de origem e destino, só 3 válidas | `MaquinaEstadosEstudoTest` (T41) | 27 testes passam |
| Unitários (Mockito): as duas pré-condições e a gravação do histórico | `EstudoTransicaoServiceTest` (T41) | 7 testes passam |
| **Novo:** o serviço percorrendo as 16 combinações com Mockito (3 avançam e gravam histórico; 13 recusam sem alterar o estudo nem gravar) | `EstudoTransicaoCombinacoesServiceTest` | 16 testes passam |
| Integração (MockMvc): 200 no caminho feliz, 401 sem login, 403 para coordenador, 404 e um 409 por regra | `EstudoTransicaoEndpointTest` (T42) | 4 testes passam |
| **Novo:** as 16 combinações pelo HTTP (200 nas 3 válidas, 409 `TRANSICAO_ESTADO_INVALIDA` nas 13 outras, estado preservado) | `EstudoTransicaoRf07Test` | 16 testes passam |
| **Novo:** 403 para coordenador, **participante** e administrador, sem alterar o estudo nem gravar histórico; participante não lê o histórico | `EstudoTransicaoRf07Test` | 4 testes passam |

Cobertura das classes de domínio e serviço do RF07 (JaCoCo, `mvnw verify`):

| Classe | Linhas | Ramos |
|---|---|---|
| `domain.regra.MaquinaEstadosEstudo` | 25 de 25 (100%) | 26 de 26 (100%) |
| `service.EstudoTransicaoService` | 24 de 24 (100%) | sem ramos |
| `service.EstudoAcesso` | 13 de 13 (100%) | 4 de 4 (100%) |

**Sobre "sobre PostgreSQL".** Localmente os testes de integração rodam no H2 do perfil `test`. O job `backend-postgres` do CI roda a mesma suíte em PostgreSQL, mas **ele ainda não foi executado no GitHub**; até esse job ficar verde, a parte "sobre PostgreSQL" da T44 não está comprovada.

## T47 · Testes do RF05 (critérios)

| Passo da task | Onde está | Resultado |
|---|---|---|
| Unitários do serviço: descrição repetida, estudo fora de planejamento | `CriterioServiceTest` (novo, Mockito) | 13 testes passam |
| Descrição curta e longa (limites 4/5 e 500/501), tipo ausente, título e peso | `CriterioRequestValidacaoTest` (novo, validador sem Spring) | 19 testes passam |
| Integração: 201 ao criar | `CriterioEndpointTest` (novo) | passa |
| Integração: 400 com o campo que falhou (descrição curta, descrição ausente, título ausente, tipo ausente, peso zero, descrição longa, tipo desconhecido, JSON malformado) | `CriterioEndpointTest` | passa |
| Integração: 403 por perfil (coordenador, participante e administrador nas escritas; participante e administrador na leitura) e 401 sem token | `CriterioEndpointTest` | passa |
| Integração: 404 (estudo, critério inexistente e critério de outro estudo) | `CriterioEndpointTest` | passa |
| Integração: 409 (descrição repetida, título repetido, estudo fora de planejamento ao criar, editar e excluir) | `CriterioEndpointTest` | passa |
| Componente (T46): lista vazia, erro do servidor | `AbaCriterios.test.tsx` (7 testes novos) | 20 testes passam |

`CriterioEndpointTest` tem 19 casos. Cobertura: `service.CriterioService` com 57 de 57 linhas (100%) e 33 de 36 ramos (92%).

### Dois pontos que a escrita dos testes revelou

1. **O 409 `CRITERIO_EM_USO` não é alcançável pelo HTTP.** Um candidato só existe com o estudo em recrutamento, e nesse estado a exclusão já é recusada por `ESTUDO_FORA_DE_PLANEJAMENTO`, que é verificado antes. A regra continua testada no serviço (`CriterioServiceTest.criterioEmUso`). Não alterei o código; fica a decisão de manter a regra como defesa ou removê-la.
2. **O 400 não traz "erros por campo" em lista.** O `ApiExceptionHandler` devolve só o primeiro campo inválido, no formato `{"erro":"campo: mensagem"}`. Os testes verificam isso (nome do campo no início da mensagem). Se a task exigir a lista completa de campos, é uma mudança no handler, que é código compartilhado e não alterei.

## Totais depois desta entrega

| Conjunto | Antes | Agora |
|---|---|---|
| Backend (JUnit) | 156 | **246**, 0 falhas (inclui os 3 testes novos do fechamento da T42) |
| Cobertura de linhas de domínio e serviços | 93,8% | **93,4%** (a regra de 80% segue cumprida) |
| Web (Vitest) | 78 | **85** |

Para rodar só estes testes:

```
cd backend/ClinicalStudyPB
./mvnw -B test -Dtest='EstudoTransicaoCombinacoesServiceTest,EstudoTransicaoRf07Test,CriterioServiceTest,CriterioRequestValidacaoTest,CriterioEndpointTest'

cd web/ClinicalStudyPB
npx vitest run src/pages/estudos/detalhe/abas/AbaCriterios.test.tsx
```

## Fechamentos feitos em 02/11 (fora dos testes)

- **T42, brecha do estado:** `POST /api/estudos` só aceita `status: PLANEJAMENTO` e `PUT /api/estudos/{id}` só aceita reenviar o estado atual; qualquer outro responde `409` (`ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO` e `ESTADO_ALTERADO_FORA_DA_TRANSICAO`). O estado agora só muda por `POST /api/estudos/{id}/transicoes`. Alterações em arquivos que já existiam: `EstudoService`; `EstudoServiceTest` (o teste que atualizava para RECRUTAMENTO foi ajustado e ganhou dois testes); `RecrutamentoIntegrationTest` (o auxiliar que mudava o estado pelo PUT passou a usar a transição, e o teste de critério em uso, que voltava o estudo a planejamento pelo PUT, passou a esperar `ESTUDO_FORA_DE_PLANEJAMENTO`, o que confirma o ponto 1 acima); `EstudoFormPage.tsx` (o seletor de status ficou desabilitado). O contrato OpenAPI não lista esses dois 409; vale acrescentá-los.
- **T64:** `COBERTURA_BLOQUEAR` está em `'true'` no `ci.yml`; o build falha abaixo de 80% de linhas em domínio e serviços (hoje 93%). Conferido localmente com `-Djacoco.haltOnFailure=true`.
- **T65:** `web/ClinicalStudyPB/e2e/.auth/*.json` saiu do controle de versão (`git rm --cached`; os arquivos continuam no disco e o `.gitignore` já os ignora). A remoção só vale quando for commitada.
- **PostgreSQL (T38/T44):** **não executado.** O Docker Desktop foi iniciado mas o motor não respondeu (provavelmente aguardando uma ação na janela dele), então a imagem `postgres:16` não foi baixada.

## Atualização de 07/11/2026

Depois desta entrega entraram os testes das tasks T50 e T59 e as telas T58 e T61. Totais atuais: backend **333 testes**, 0 falhas, cobertura de domínio e serviços **98,3%**; web **126** testes de componente; E2E **29** testes. Detalhes em [T50, T55, T59, T65 e T66 — implementação.md](<T50, T55, T59, T65 e T66 — implementação.md>).
