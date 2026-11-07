# T10 · Configurar documentação da API com springdoc-openapi / Swagger UI

> **Sprint 1** · PBI-02 · Estrutura inicial do Spring Boot e PostgreSQL  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T06  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Publicar a documentação interativa da API, com autenticação Bearer, para testar os endpoints sem ferramenta externa.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/config/OpenApiConfig.java` | Existente | Título "ClinicalStudyPB API", versão, esquema `bearerAuth` (HTTP Bearer JWT) e `SWAGGER_WHITELIST` |
| `backend/ClinicalStudyPB/src/main/resources/application.properties` | Existente | Propriedades `springdoc.*` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/OpenApiDocumentationIntegrationTest.java` | Existente | Garante que `/v3/api-docs` e o Swagger UI respondem |

## 4. O que fazer em cada arquivo

### Endereços

- Swagger UI: `http://localhost:8081/swagger-ui.html`; JSON: `http://localhost:8081/v3/api-docs`.
- Os caminhos de `/swagger-ui/**` e `/v3/api-docs/**` ficam fora da autenticação (`SWAGGER_WHITELIST`, usado em `SecurityConfig`).

### Ao criar um controller novo

- Anotar a classe com `@Tag(name, description)` e cada método com `@Operation(summary, description)` e `@ApiResponses` para os códigos relevantes.
- Os caminhos publicados precisam coincidir com `docs/api/openapi-sprint2.yaml`; o `ContratoOpenApiTest` falha se divergir.

### Autorizar no Swagger

- Fazer login em `POST /api/auth/login`, copiar o token e usar o botão **Authorize**; `persist-authorization=true` mantém o token ao recarregar.

## 7. Critérios de aceite

- [ ] Swagger UI abre e lista os endpoints.
- [ ] O botão Authorize aceita o token.

## 8. Cuidados e fora de escopo

- Não expor a documentação em produção sem decisão da equipe.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
