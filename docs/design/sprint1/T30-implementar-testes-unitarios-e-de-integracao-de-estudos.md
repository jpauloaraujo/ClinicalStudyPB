# T30 · Implementar testes unitários e de integração de estudos

> **Sprint 1** · PBI-07 · RF04 Cadastro de estudos clínicos  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T27, T28  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cobrir por teste as regras de estudo e os endpoints, com tokens reais.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoServiceTest.java` | Existente | Regras com repositórios simulados (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoIntegrationTest.java` | Existente | CRUD pelo MockMvc, com permissões |

## 4. O que fazer em cada arquivo

### Cenários

- Data final anterior à inicial, responsável inexistente ou sem perfil de pesquisador.
- Estado: criar fora de planejamento e mudar o estado pelo `PUT` são recusados (409).
- Exclusão de estudo com candidatos recusada.
- Permissões: coordenador só lê; administrador não acessa.

## 6. Testes

- `./mvnw test`.

## 7. Critérios de aceite

- [ ] Todos os testes passam.

## 8. Cuidados e fora de escopo

- Ao mudar uma regra de `EstudoService`, atualizar o teste correspondente no mesmo PR.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
