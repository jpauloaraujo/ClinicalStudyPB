# T63 · Dados fictícios de demonstração

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qui 05/11  
> **Depende de:** T45, T48, T51, T57  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter, em um perfil `demo`, dois estudos prontos (um em planejamento e outro em recrutamento, a uma vaga do limite) e candidatos em várias situações, para demonstrar o fluxo e o bloqueio por vagas.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Já existem 4 usuários fictícios, um por perfil, no seed `V2__seed_initial_data.sql` (senha `senha-de-teste`, hash BCrypt). Reaproveitar; não criar usuários novos com senha nova.
- Credenciais de demonstração estão documentadas no `README.md`, seção 5 ("Credenciais de demonstração").
- Esquema das tabelas: `V4__create_recruitment_tables.sql` e `V5__create_study_status_history.sql`.
- O Flyway está com `ddl-auto=validate`; qualquer script novo precisa respeitar as constraints (UNIQUE de candidato por nome e nascimento, CHECK de situações).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__dados_demonstracao.sql` | Existente (vindo da main) | Carga repetível: usuários, 2 estudos (planejamento e recrutamento, com uma última vaga), critérios, visitas, ações, candidatos, avaliações, consentimentos, participação e histórico |
| `backend/ClinicalStudyPB/src/main/resources/application-demo.properties` | Existente (vindo da main) | Perfil `demo`: `spring.flyway.locations=classpath:db/migration,classpath:db/demo` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Existente (vindo da main) | O perfil normal não carrega a demonstração; o perfil `demo` cria os dados; rodar de novo não duplica nem desfaz alterações |

## 4. O que fazer em cada arquivo

### R__dados_demonstracao.sql

- Idempotente: usar UUIDs fixos e `INSERT ... ON CONFLICT DO NOTHING` (PostgreSQL), ou `WHERE NOT EXISTS`, para rodar duas vezes sem duplicar.
- **Estudo A, "Demonstração — Planejamento":** estado `PLANEJAMENTO`, responsável `pesquisador@clinicalstudy.test`, 1 critério de inclusão determinante, 1 de exclusão e 2 visitas com ações.
- **Estudo B, "Demonstração — Recrutamento":** estado `RECRUTAMENTO`, `limite_participantes = 3`, com critérios e visita; **2 participantes incluídos** (a uma vaga do limite) e linhas em `historico_status_estudo`.
- Cerca de 6 candidatos no estudo B, códigos `CAND-001`..`CAND-006`, nomes fictícios, em situações diferentes: cadastrado, elegível, não elegível, consentido e 2 incluídos (com avaliação vigente, respostas e consentimento coerentes com a situação).
- Nenhum dado real; nenhuma credencial nova.

### application-demo.properties

- Só acrescenta o local extra do Flyway; o resto vem do `application.properties`. Fora do perfil `demo` o script nem é lido.

### Consistência

- Depois de rodar, conferir pela API que `GET /api/estudos/{idB}/participantes/vagas` devolve `incluidos = 2`, `limite = 3`, `restantes = 1` e que um terceiro candidato elegível e consentido pode ser incluído, e o quarto é recusado.

### Colunas exatas das tabelas (V4 e V5) e ordem dos INSERTs

- Ordem por causa das chaves estrangeiras: `estudos` → `criterios` → `visitas_planejadas` → `acoes` → `candidatos` → `avaliacoes_elegibilidade` → `respostas_avaliacao` → `consentimentos` → `participacoes` → `historico_status_estudo`.
- `estudos`: id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes (≥ 1), status.
- `criterios`: id, estudo_id, tipo (`INCLUSAO`/`EXCLUSAO`), titulo, descricao, peso, determinante. Inclusão exige `peso > 0` e `determinante` preenchido; exclusão exige os dois nulos.
- `visitas_planejadas`: id, estudo_id, nome, dia_previsto (≥ 0), tolerancia (0 a 30), ordem (≥ 1); dia único por estudo. `acoes`: id, visita_id, nome, descricao, categoria (`AVALIACAO_CLINICA`, `EXAME_LABORATORIAL`, `EXAME_IMAGEM`, `QUESTIONARIO`, `MEDICACAO`, `OUTROS`).
- `candidatos`: id, estudo_id, codigo, nome_ficticio, data_nascimento, contato, usuario_id, situacao (`CADASTRADO`, `EM_AVALIACAO`, `ELEGIVEL`, `NAO_ELEGIVEL`, `CONSENTIDO`, `INCLUIDO`); código e (nome, nascimento) únicos por estudo.
- `avaliacoes_elegibilidade`: id, candidato_id, data_avaliacao, avaliador_id, resultado (`APROVADO`/`REPROVADO`), pontuacao, vigente. `respostas_avaliacao`: id, avaliacao_id, criterio_id, aplica, observacao; uma por critério e avaliação.
- `consentimentos`: id, candidato_id (único), data_hora, responsavel_id, versao_termo. `participacoes`: id, candidato_id (único), estudo_id, data_inclusao, incluido_por_id. `historico_status_estudo`: id, estudo_id, estado_anterior, estado_novo, usuario_id, data_hora.
- Coerência: candidato `INCLUIDO` tem avaliação vigente `APROVADO`, consentimento e participação; `CONSENTIDO` tem avaliação aprovada e consentimento; `ELEGIVEL` só avaliação aprovada; `NAO_ELEGIVEL` avaliação reprovada; `CADASTRADO` nada. O estudo B tem um registro de histórico para a passagem PLANEJAMENTO → RECRUTAMENTO.
- Usar como `avaliador_id`, `responsavel_id` e `incluido_por_id` o coordenador do seed V2 e como responsável do estudo o pesquisador do seed V2 (UUIDs em `V2__seed_initial_data.sql`).

### Situação (unificação de 07/11/2026)

- Implementada na branch `main` por outra pessoa; o guia acima descreve a mesma ideia e as colunas continuam valendo como referência.
- Para usar: `SPRING_PROFILES_ACTIVE=demo`. Falta apenas conferir se as credenciais de demonstração estão listadas no README (parte da T70).

## 6. Testes

- Subir com `SPRING_PROFILES_ACTIVE=demo` em um banco vazio e de novo (segunda subida não duplica).
- Opcional: `DadosDemonstracaoTest` com o local extra do Flyway no H2, conferindo contagens.

## 7. Critérios de aceite

- [ ] O script é repetível sem duplicar dados.
- [ ] Os dois estudos e os candidatos aparecem na web.
- [ ] Documentação com usuários e senhas fictícias, sem credencial real.

## 8. Cuidados e fora de escopo

- O E2E da T66 depende destes dados (estudo B a uma vaga do limite).
- Se o H2 dos testes não conseguir executar o script, não incluir `db/demo` no local padrão: manter só no perfil `demo`.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
