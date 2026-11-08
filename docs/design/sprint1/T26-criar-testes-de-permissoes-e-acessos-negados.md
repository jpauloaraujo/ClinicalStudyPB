# T26 · Criar testes de permissões e acessos negados

> **Sprint 1** · PBI-06 · RF03 Controle de acesso por perfil (web)  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T24  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Garantir por teste que cada perfil só acessa o que a matriz permite e que o acesso negado responde 403.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PermissaoAcessoIntegrationTest.java` | Existente | 15 testes com tokens reais dos quatro perfis |

## 4. O que fazer em cada arquivo

### Padrão

- Para cada grupo de endpoints, testar um perfil permitido (2xx) e os não permitidos (403), e a ausência de token (401).
- Ao criar endpoint novo, acrescentar o caso aqui.

## 6. Testes

- `./mvnw test`.

## 7. Critérios de aceite

- [ ] Todos os testes passam.
- [ ] Toda combinação perfil × recurso da matriz tem um caso.

## 8. Cuidados e fora de escopo

- Os testes da Sprint 2 repetem a verificação de 403 em cada endpoint novo (ver `CriterioEndpointTest`, `EstudoTransicaoRf07Test`).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
