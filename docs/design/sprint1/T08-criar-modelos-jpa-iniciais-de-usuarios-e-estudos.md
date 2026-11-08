# T08 · Criar modelos JPA iniciais de usuários e estudos

> **Sprint 1** · PBI-02 · Estrutura inicial do Spring Boot e PostgreSQL  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T06, T09  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Mapear as tabelas `usuarios` e `estudos` em entidades JPA e criar os repositórios correspondentes.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Decisões de modelagem: `docs/sprint1/implementacao-backend-sprint1.md`.
- As entidades da Sprint 2 estão em `docs/diagramas/dominio.md`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Usuario.java` | Existente | id (UUID), nome (120), email (180, único), senha (hash), perfil, ativo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Estudo.java` | Existente | id, titulo (180), objetivo (TEXT), responsavel (ManyToOne), dataInicio, dataFim, limiteParticipantes, status |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/PerfilUsuario.java, StatusEstudo.java` | Existente | ADMINISTRADOR/PESQUISADOR/COORDENADOR/PARTICIPANTE; PLANEJAMENTO/RECRUTAMENTO/EXECUCAO/ENCERRADO |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/UsuarioRepository.java` | Existente | `findByEmail`, `existsByEmail`, `countByPerfilAndAtivoTrue` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/EstudoRepository.java` | Existente | Consultas com `@EntityGraph` para evitar N+1; `buscarComBloqueio` (Sprint 2) |

## 4. O que fazer em cada arquivo

### Regras de mapeamento

- IDs são UUID; enums gravados como texto (`@Enumerated(STRING)`).
- Estudo tem `status` com padrão `PLANEJAMENTO`; o responsável é carregado de forma preguiçosa.
- A senha nunca sai em JSON (os DTOs de resposta não a incluem).
- Cada atributo precisa existir na migration: com `ddl-auto=validate` o Hibernate falha na subida se divergir.

## 6. Testes

- `FlywayMigrationIntegrationTest` valida o mapeamento contra o esquema migrado.

## 7. Critérios de aceite

- [ ] As entidades persistem e leem do PostgreSQL.
- [ ] O Hibernate valida o esquema sem erro.

## 8. Cuidados e fora de escopo

- Entidade nova = migration nova (T09); nunca alterar migration já aplicada.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
