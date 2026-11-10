# T67 · Integração e correções: planejamento do estudo

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Qui 05/11  
> **Depende de:** T43, T46, T49  
> **Situação:** ✅ Implementada (ambiente Docker e roteiro `scripts/verificar-ambiente.py`; falta só ver o job do CI verde e registrar os bugs no Azure Boards)

## 1. Objetivo

Rodar web, backend e banco juntos, sem mocks, percorrer o fluxo de planejamento (estudo, critérios, visitas e avanço de estado) e corrigir os defeitos que aparecerem.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Hoje não há `docker-compose.yml` nem Dockerfiles no repositório; o plano pressupõe um ambiente completo com Docker Compose.
- Portas: backend 8081, web 5173 (liberada no CORS), PostgreSQL 5432.
- Fluxo e regras: `docs/diagramas/casos-de-uso.md` e `docs/diagramas/sequencia-avancar-estado.md`. Dados prontos: T63.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docker-compose.yml` | Criar (raiz) | Serviços `db` (postgres:16 com healthcheck), `backend` e `web` |
| `backend/ClinicalStudyPB/Dockerfile` | Criar | Build Maven (JDK 21) e execução do jar |
| `web/ClinicalStudyPB/Dockerfile` | Criar | Build do Vite com `VITE_API_URL` e servidor estático, publicado na porta 5173 do host |
| `README.md` | Alterar | Comando único para subir o ambiente completo |
| `docs/api/openapi-sprint2.yaml` | Alterar (se o contrato mudar) | Corrigir no mesmo PR da correção de código |

## 4. O que fazer em cada arquivo

### docker-compose.yml

- `db`: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` e `healthcheck` com `pg_isready`.
- `backend`: `depends_on` do `db` saudável; variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (valor fictício) e `SPRING_PROFILES_ACTIVE=demo` (T63).
- `web`: argumento de build `VITE_API_URL=http://localhost:8081`; mapear `5173:80` para respeitar o CORS.

### Roteiro de integração (checar cada item e registrar defeito como Bug ligado à task de origem)

- Pesquisador: login → criar estudo → critérios (inclusão e exclusão) → visitas e ações → tentar avançar sem critério/visita (mensagem do 409) → avançar para recrutamento.
- Coordenador: abas permitidas, somente leitura em Critérios/Visitas, sem botão de avançar.
- Conferir: CORS, campos diferentes do contrato, mensagens de erro, estados de carregamento e de lista vazia, permissões visuais.
- Regressão de RF01 a RF04: login, cadastro de usuário e de estudo.

### Onde corrigir

- Defeito de tela → arquivo da aba correspondente (`AbaCriterios`, `AbaVisitas`, `IndicadorEstado`); de contrato → YAML e depois `npm run api:tipos`; de regra → serviço do backend com teste.

## 6. Testes

- `npm test` e `./mvnw verify` continuam verdes depois de cada correção.
- Cada correção de comportamento ganha um teste.

## 7. Critérios de aceite

- [ ] O fluxo de planejamento funciona de ponta a ponta sem mocks.
- [ ] Defeitos registrados como Bug no Azure Boards, ligados à task de origem.
- [ ] RF01 a RF04 continuam funcionando.

## 8. Cuidados e fora de escopo

- Não usar `VITE_USE_MOCKS=true` nesta verificação.
- Nunca versionar senha real nem o segredo JWT de produção.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
