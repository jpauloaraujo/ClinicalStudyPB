# T06 · Inicializar projeto Spring Boot (controllers, services, repositories)

> **Sprint 1** · PBI-02 · Estrutura inicial do Spring Boot e PostgreSQL  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Criar o projeto Spring Boot com as camadas definidas, para que cada módulo siga o mesmo caminho: controller → service → repository.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Implementação inicial: `docs/sprint1/implementacao-backend-sprint1.md`.
- Pacote raiz: `UEPB.ESWII.ClinicalStudyPB`; classe principal `ClinicalStudyPbApplication`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/pom.xml` | Existente | Dependências: Web MVC, Data JPA, Security, validação, Flyway, PostgreSQL, springdoc, H2 e JaCoCo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/ClinicalStudyPbApplication.java` | Existente | Classe principal |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/` | Existente | Endpoints REST |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/` | Existente | Regras de negócio |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/` | Existente | Spring Data JPA |
| `backend/ClinicalStudyPB/mvnw, mvnw.cmd` | Existente | Maven Wrapper (`./mvnw` ou `mvnw.cmd`) |

## 4. O que fazer em cada arquivo

### Receita para um módulo novo (usada em toda a Sprint 2)

- 1. Entidade e enum em `domain/` e migration em `resources/db/migration`.
- 2. Repositório em `repository/`.
- 3. DTOs `XxxRequest` e `XxxResponse` (records) em `controller/dto/`, com Bean Validation e mensagens em português.
- 4. Serviço em `service/` com `@Service` e `@Transactional`; regras violadas viram `RegraNegocioException` (400) ou `ConflitoRegraException` (409, com código).
- 5. Controller em `controller/` com `@PreAuthorize(Permissoes.X)` e anotações do springdoc.
- 6. Testes: serviço com Mockito, DTO com Validator, endpoint com MockMvc e tokens reais.

### Regras de dependência

- Controller não acessa repositório; serviço não conhece `HttpServletRequest`; regra pura sem Spring fica em `domain/regra`.

## 6. Testes

- `ClinicalStudyPbApplicationTests` (a aplicação sobe) e a suíte completa com `./mvnw test`.

## 7. Critérios de aceite

- [ ] O backend inicia e conecta ao banco.
- [ ] Há um exemplo de cada camada para copiar.

## 8. Cuidados e fora de escopo

- Não colocar regra de negócio no controller.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
