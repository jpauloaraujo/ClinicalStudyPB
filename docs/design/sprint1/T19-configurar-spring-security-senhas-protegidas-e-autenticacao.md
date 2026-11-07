# T19 · Configurar Spring Security, senhas protegidas e autenticação por token

> **Sprint 1** · PBI-05 · RF02 Autenticação e logout (web)  
> **Responsável:** I4 · **Estimativa:** 6h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T06, T08  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Proteger a API: toda rota exige um token JWT válido, exceto o login e a documentação, e as senhas ficam guardadas com hash BCrypt.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Propriedades `jwt.secret` (variável `JWT_SECRET`), `jwt.issuer` e `jwt.expiration-minutes` (30) em `application.properties`.
- Revogação imediata de token não foi implementada (risco aceito, ver `docs/sprint1/status-sprint1.md`, seção 4).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/SecurityConfig.java` | Existente | Cadeia de filtros, CORS, BCrypt, codificador e decodificador JWT (HS256), conversão da claim `role` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/TokenService.java` | Existente | Emite o JWT com `sub` (id do usuário), `role`, emissor e expiração |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` | Existente | Responde 401 e 403 em JSON |
| `backend/ClinicalStudyPB/src/main/resources/application.properties` | Existente | Segredo, emissor e expiração do token |

## 4. O que fazer em cada arquivo

### SecurityConfig

- CSRF desligado e sessão `STATELESS`.
- `permitAll` apenas em `POST /api/auth/login` e na documentação (`OpenApiConfig.SWAGGER_WHITELIST`); `/api/**` exige autenticação.
- `JwtAuthenticationConverter` lê a claim `role` e cria a authority `ROLE_<PERFIL>`, que o `@PreAuthorize` usa.
- CORS liberando `http://localhost:5173` (web) e `http://localhost:8082` (mobile no navegador).

### TokenService

- `gerarToken(Usuario)` monta as claims e assina com `MacAlgorithm.HS256`.

### Segredo

- `JWT_SECRET` obrigatório no ambiente de execução; nos testes vem de `src/test/resources/application.properties` (valor fictício).

## 6. Testes

- `AuthenticationIntegrationTest` (T22) e `PermissaoAcessoIntegrationTest` (T26).

## 7. Critérios de aceite

- [ ] Sem token, a API responde 401.
- [ ] Token adulterado ou expirado é recusado.
- [ ] Nenhuma senha em texto puro no banco (BCrypt).

## 8. Cuidados e fora de escopo

- Não versionar o `JWT_SECRET` real.
- Mudar a expiração é uma decisão de segurança: registrar no PR.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
