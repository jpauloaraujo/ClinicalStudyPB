# T74 · Contrato OpenAPI do agendamento e dados de demonstração

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 2h · **Prazo:** Ter 10/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Escrever, **antes do código**, o contrato dos endpoints do agendamento e o script de demonstração que dá aos demais PBIs participantes e visitas em situações diferentes. Esta task também cria a estrutura do contrato da Sprint 3 (arquivo, teste de contrato e geração de tipos) que os próximos PBIs só estendem.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O contrato da Sprint 2 é `docs/api/openapi-sprint2.yaml`, comparado com a API pelo `ContratoOpenApiTest`, que exige 24 operações e só aceita caminhos do recrutamento: **não acrescente caminhos da Sprint 3 nele**.
- O formato único de erro da Sprint 2 (`status`, `codigoRegra`, `mensagem`, `errosCampo`, `erro`, `codigo`) já é um schema `Erro` no YAML da Sprint 2: referencie-o.
- O web gera tipos com `npm run api:tipos` (`openapi-typescript 7.13.0`) em `src/api/schema.d.ts`.
- O demo da T63 está em `db/demo/R__dados_demonstracao.sql` (ids `d6300000-0000-4000-8000-...`, `NOT EXISTS`); o estudo `...000000000102` serve ao roteiro da apresentação e **não pode ser alterado**.
- Endpoints, DTOs e códigos de erro desta fatia: seções 2, 3 e 4 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint3.yaml` | Criar | Contrato da Sprint 3; esta task escreve só os caminhos do PBI-19 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Criar | Compara o YAML da Sprint 3 com `/v3/api-docs` |
| `web/ClinicalStudyPB/package.json` | Alterar | Script `api:tipos` gera os dois arquivos de tipos |
| `web/ClinicalStudyPB/src/api/schema-sprint3.d.ts` | Criar | Tipos gerados do YAML da Sprint 3 |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Alterar | Reexporta os tipos novos do agendamento |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_19_visitas_agendadas.sql` | Criar | Estudo de acompanhamento, 3 participantes e visitas agendadas |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Confere as contagens e a idempotência do novo script |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T74-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Contrato `openapi-sprint3.yaml`

- **RQ-T74-01** Criar o arquivo com `openapi: 3.0.3`, título "ClinicalStudyPB — contrato da Sprint 3 (RF12 a RF18)", `securitySchemes` Bearer JWT e `tags` iguais às dos controllers; ao fim da Sprint ele terá as 19 operações da seção 2 das convenções, e **cada PBI acrescenta só os seus caminhos** (evita conflito de merge).
- **RQ-T74-02** Descrever as 5 operações do PBI-19: `GET /api/participacoes/{participacaoId}/visitas`, `POST /api/participacoes/{participacaoId}/visitas-agendadas`, `PUT /api/visitas-agendadas/{visitaAgendadaId}`, `GET /api/estudos/{estudoId}/agenda` (parâmetros `de`, `ate`, `situacao`, `pagina`, `tamanho`) e `GET /api/estudos/{estudoId}/agenda/participantes`.
- **RQ-T74-03** Declarar os schemas `VisitaDoParticipanteResponse` (com `dataSugerida`, `janelaInicio`, `janelaFim` e `agendamento` anulável), `VisitaAgendadaRequest`, `VisitaReagendamentoRequest`, `VisitaAgendadaResponse`, `AgendaItemResponse`, `ParticipanteAgendaResponse` e o enum `SituacaoVisita`, com os exemplos fictícios da seção 3 das convenções.
- **RQ-T74-04** Para cada operação, listar as respostas `200/201`, `400`, `403`, `404` e `409`, referenciando o schema `Erro` e documentando os códigos: `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_JA_AGENDADA`, `DATA_FORA_DA_JANELA`, `DATA_NO_PASSADO` e `VISITA_NAO_ESTA_AGENDADA` (regras 1, 2 e 3). Documentar que `dataHora` na entrada é local sem fuso e na saída tem deslocamento.
- **RQ-T74-05** Registrar quem pode chamar cada operação: escrita só do coordenador, leitura de coordenador e pesquisador (descrição de cada operação).

### Teste de contrato

- **RQ-T74-06** Criar `ContratoOpenApiSprint3Test`, com o mesmo método do `ContratoOpenApiTest`: todo caminho e método do YAML da Sprint 3 existe em `/v3/api-docs` e todo caminho da Sprint 3 da API está no YAML. O regex dos caminhos da Sprint 3 é `^/api/(participacoes/.*|visitas-agendadas/.*|estudos/\{estudoId}/agenda(/.*)?|me/.*|contas-participante/.*)$`.
- **RQ-T74-07** Fixar a contagem esperada de operações (5 neste PBI) em um único lugar do teste, para que cada PBI só a atualize.
- **RQ-T74-08** Enquanto os endpoints da T76 não existirem, o teste falha de propósito: ele é a prova de que o contrato foi escrito antes do código. Marque o primeiro teste como `@Disabled("T76")` até a T76 entrar, e **remova a anotação na T76**.

### Tipos TypeScript

- **RQ-T74-09** Alterar o script `api:tipos` do `package.json` para gerar `src/api/schema.d.ts` (YAML da Sprint 2) **e** `src/api/schema-sprint3.d.ts` (YAML da Sprint 3), com a mesma versão do `openapi-typescript`.
- **RQ-T74-10** Em `src/api/tipos.ts`, reexportar `VisitaDoParticipanteResponse`, `VisitaAgendadaResponse`, `VisitaAgendadaRequest`, `VisitaReagendamentoRequest`, `AgendaItemResponse`, `ParticipanteAgendaResponse` e `SituacaoVisita`, no mesmo estilo dos tipos da Sprint 2.
- **RQ-T74-11** Rodar `npm run api:tipos`, `npx tsc -b` e `npm run lint`: nada pode quebrar.

### Dados de demonstração `R__demo_19_visitas_agendadas.sql`

- **RQ-T74-12** Criar o estudo "Demo - Acompanhamento de participantes" (`d6300000-0000-4000-8000-000000001901`, `EXECUCAO`, limite 6, responsável `demo.pesquisador@clinicalstudy.test`), com um critério de inclusão e as visitas previstas e atividades descritas na seção 7 das convenções (Triagem dia 0 ± 3, Retorno 30 dias ± 5, Retorno 60 dias ± 7, cada uma com suas atividades, entre elas as de categoria `AVALIACAO_CLINICA`).
- **RQ-T74-13** Incluir 3 participantes (candidatos com situação `INCLUIDO`, avaliação aprovada e consentimento, como no script da T63): P1 (inclusão D-20), P2 (D-3) e P3 (D-1), e as visitas agendadas de P1 (Triagem `REALIZADA`; Retorno 30 `AGENDADA` em D+10 09:30) e de P2 (Triagem `AGENDADA` em D 15:00); P3 fica sem visitas agendadas.
- **RQ-T74-14** Usar datas relativas a `CURRENT_DATE` (por exemplo `CURRENT_DATE - INTERVAL '20' DAY`), ids fixos no padrão `d6300000-0000-4000-8000-0000000019NN` e `INSERT ... SELECT ... WHERE NOT EXISTS`, para que a segunda execução não duplique nada e não restaure o que o usuário alterou. Conferir a sintaxe no H2 e no PostgreSQL.
- **RQ-T74-15** Os participantes entram **pelo SQL** (a inclusão pela API exigiria o estudo em recrutamento); o estudo é criado já em `EXECUCAO`, com uma linha em `historico_status_estudo`.
- **RQ-T74-16** Atualizar `DemoDataIntegrationTest`: contagem de estudos, participantes e visitas agendadas depois de **duas** cargas (idempotência).

## 5. Contrato, regras e códigos de erro

- O contrato é a fonte da verdade: se o Swagger divergir dele na T76, o YAML é corrigido no mesmo Pull Request da T76.
- Regras refletidas: 1 (estado do estudo), 2 (janela e unicidade) e 3 (só `AGENDADA` reagenda).

## 6. Testes

- `ContratoOpenApiSprint3Test` (criado desabilitado, habilitado na T76).
- `DemoDataIntegrationTest` com a carga dupla do novo script.
- `npm run api:tipos` seguido de `tsc -b` e lint no web.

## 7. Critérios de aceite

- [ ] `docs/api/openapi-sprint3.yaml` descreve as 5 operações com DTOs, exemplos, erros e códigos.
- [ ] Os tipos TypeScript são gerados dos dois contratos e compilam.
- [ ] O script de demonstração carrega em H2 e PostgreSQL, é idempotente e deixa o estudo de recrutamento da T63 intacto.
- [ ] A equipe consegue ver, no perfil `demo`, participantes com visitas em situações diferentes.

## 8. Cuidados e fora de escopo

- Não mexer em `openapi-sprint2.yaml` nem no `ContratoOpenApiTest` da Sprint 2.
- Os ids e datas do demo seguem a seção 7 das convenções: os próximos PBIs dependem deles.
- Sem mocks: nenhum handler MSW novo (a Sprint 3 usa a API real).

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
