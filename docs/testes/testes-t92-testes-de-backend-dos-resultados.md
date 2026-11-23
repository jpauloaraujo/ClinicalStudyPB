# Testes da T92 — Testes de backend dos resultados

> Guia: [T92](../design/sprint3/T92-testes-de-backend-dos-resultados.md) · PBI-21 · RF14 Resultados fictícios das avaliações previstas
> Branch: `feature/t92-testes-backend-resultados` (criada a partir da `develop` em 23/11, com T87 a T91 e T102 já integradas).
> Regra seguida: **só testes**. Nenhum código de produção foi alterado e nenhum teste existente foi excluído ou afrouxado.

## 1. Linha de base e resultado final

Execução no CI do PR (jobs `Backend (Java 21 + Maven)` com H2 e `Backend (testes no PostgreSQL)` com PostgreSQL 16):

| Verificação | `develop` antes da T92 | Depois |
|---|---|---|
| Suíte do backend | 843 testes, 0 falhas | **871 testes, 0 falhas, 0 erros, 0 ignorados**, nos dois jobs; `BUILD SUCCESS` |
| `ResultadoAtividadeServiceTest` | 19 | 34 |
| `ResultadoAtividadeEndpointTest` | 8 | 15 (rodando contra `org.postgresql.jdbc.PgConnection` no job PostgreSQL) |
| `ResultadoRequestValidacaoTest` | — | 6 |
| `ContratoOpenApiSprint3Test` | verde | verde (57 casos) |
| Cobertura do código de negócio (JaCoCo) | — | 1179 de 1189 linhas (99,2%); `All coverage checks have been met` |

Cobertura por classe da fatia (JaCoCo, `jacoco.csv` do artefato `cobertura-jacoco`):

| Classe | Linhas | Ramos | Meta (RQ-T92-14: 95%) |
|---|---|---|---|
| `ResultadoAtividadeService` | 70/70 (100%) | 34/34 (100%) | atendida |
| `ResultadoAtividadeService.ResultadosDaVisita` | 4/4 (100%) | sem ramos | atendida |
| `ResultadoAtividadeService.AtividadeComResultado` | 2/2 (100%) | 2/2 (100%) | atendida |
| `ResultadoAtividadeController` | 7/7 (100%) | 2/2 (100%) | atendida |
| `ResultadoAtividade` (entidade) | 7/7 (100%) | 6/6 (100%) | atendida |

## 2. Testes novos

| Arquivo | Testes | RQ |
|---|---|---|
| `ResultadoRequestValidacaoTest` (novo) | 6: completo e só com valor; valor 1/200/201; unidade 30/31; observação 500/501; os três acima do limite juntos; valor nulo, vazio e só com espaços passam no corpo (o serviço responde `VALOR_OBRIGATORIO`) | 09 |
| `ResultadoAtividadeServiceTest` | 15 novos: as 5 categorias fora da avaliação (parametrizado); estudo encerrado no **primeiro** registro; recrutamento e execução aceitam (parametrizado); valor de 200 caracteres; valor vazio é a última verificação; contagens a cada passo do parcial, com repositório em memória e uma edição no meio; edição troca unidade e observação; consulta sem bloqueio com participante ativo; visita ausente e cancelada com bloqueio (parametrizado) | 02 a 08 |
| `ResultadoAtividadeEndpointTest` | 7 novos sobre um cenário montado **só pela API** (`cenario()`): lista só as avaliações e conta passo a passo; edição por outro pesquisador preserva o autor; limites do corpo; 409 de cada regra e 403 do coordenador antes das regras; visita agendada sem resultados; estudo encerrado pela transição; participante retirado | 03, 07, 08, 10, 11, 12 |
| `support/CenarioRecrutamento` | `criarAcao`, `acaoDeAvaliacao`, `incluirParticipante`, `agendarVisita`, `visitaRealizada` | — |

**Cenário da API.** Estudo próprio com critérios de inclusão e exclusão; Triagem (dia 0, tolerância 0) com três avaliações (aferir pressão, escala de dor, exame físico) e coleta de sangue, questionário e medicação; Retorno 30 dias com uma avaliação. O participante é incluído hoje; a Triagem é agendada para hoje e recebe o desfecho REALIZADA pela API; o Retorno é agendado para hoje + 30. "Hoje" vem do `Clock` da aplicação, então o teste não depende da data em que roda (cuidado da seção 8 do guia).

## 3. Matriz RQ × teste (inclui o que já existia)

| RQ | Prova | Origem |
|---|---|---|
| 01 | `ResultadoAtividadeServiceTest.visitaNaoRealizada` (agendada, ausente, cancelada), `primeiroRegistroCriaComAutorEDatas` (realizada); endpoint `conflitosDeCadaRegra` | T89 e T92 |
| 02 | `todaCategoriaForaDaAvaliacaoERecusada` (5 categorias), `atividadeDeOutraVisita`; endpoint `conflitosDeCadaRegra` (coleta, questionário, medicação e avaliação do Retorno) | T89 e T92 |
| 03 | `estudoEncerrado` (edição), `estudoEncerradoRecusaOPrimeiroRegistro`, `estudoEmAndamentoAceita`; endpoint `estudoEncerradoPelaTransicao` (recrutamento e execução aceitam, encerrado recusa edição e primeiro registro) | T89 e T92 |
| 04 | `valorObrigatorio` (nulo, vazio, espaços, comando nulo), `valorDe200CaracteresEAceito`; endpoint `limitesDoCorpoPelaApi` | T89 e T92 |
| 05 | `ordemDasVerificacoes`, `valorVazioEAUltimaVerificacao` | T89 e T92 |
| 06 | `contagensACadaPasso`, `resultadoParcialSalvaUmaAtividadePorVez`; endpoint `listaSoAvaliacoesEContaPassoAPasso` | T89 e T92 |
| 07 | `primeiroRegistroCriaComAutorEDatas`, `edicaoAtualizaSoAAlteracao`, `edicaoTrocaUnidadeEObservacao`; endpoint `edicaoPreservaOAutorDoPrimeiroRegistro` (segundo pesquisador criado no teste) | T89 e T92 |
| 08 | `consultaDeVisitaNaoRealizada`, `consultaDeVisitaAusenteOuCancelada`, `consultaDeEstudoEncerrado`, `consultaSemBloqueioComParticipanteAtivo`, `participanteRetiradoBloqueiaResultados`; endpoint `participanteRetiradoPelaApi` | T89, T102 e T92 |
| 09 | `ResultadoRequestValidacaoTest` (6 testes) | T92 |
| 10 | Endpoint: `pesquisadorRegistraEEdita`, `edicaoPreservaOAutorDoPrimeiroRegistro` (201 e 200 com corpo completo); `dadosInvalidos`, `limitesDoCorpoPelaApi` (400); `autorizacaoPorPerfil`, `conflitosDeCadaRegra` (401, 403); `naoEncontrado`, `conflitosDeCadaRegra` (404); `regrasDeNegocio`, `conflitosDeCadaRegra`, `estudoEncerrado`, `estudoEncerradoPelaTransicao`, `participanteRetiradoPelaApi` (409) | T90 e T92 |
| 11 | `pesquisadorECoordenadorConsultam`, `autorizacaoPorPerfil`, `naoEncontrado`, `listaSoAvaliacoesEContaPassoAPasso` (outras categorias ausentes da lista) | T90 e T92 |
| 12 | `listaSoAvaliacoesEContaPassoAPasso` (`pendentes = 0` no fim), `visitaAgendadaDevolveAtividadesSemResultado` | T92 |
| 13 | O job `Backend (testes no PostgreSQL)` roda a suíte inteira (`./mvnw -B test` com `SPRING_DATASOURCE_*` do PostgreSQL); 871 testes verdes nele | CI |
| 14 | Seção 1: `ResultadoAtividadeService` com 100% das linhas e dos ramos | T92 |
| 15 | Seção 1: 871 testes, 0 falhas, nos dois jobs | T92 |

## 4. Critérios de aceite do guia

- [x] Cada regra do RF14 tem teste de sucesso e de erro (RQ 01 a 05).
- [x] A edição preserva o autor do primeiro registro e atualiza a última alteração (RQ 07, inclusive com outro pesquisador pela API).
- [x] Os endpoints respondem 200, 201, 400, 403, 404 e 409 com tokens reais (RQ 10 e 11).
- [x] A cobertura da fatia está acima de 80% (100% em `ResultadoAtividadeService`).

## 5. Observações

- **Coordenador na escrita:** além do 403 já testado na T90, `conflitosDeCadaRegra` prova que o coordenador recebe 403 mesmo onde o pesquisador receberia 409 (visita agendada e atividade de outra categoria), como pede a seção 8 do guia.
- **Participante retirado:** o guia deixava esse caso para "depois da T102". Como a T102 já está na `develop`, ele entrou aqui, no serviço e pela API.
- **Execução local:** a validação foi feita no CI do PR. O ambiente onde os testes foram escritos não alcança o Maven Central, então não deu para rodar `./mvnw` localmente.
