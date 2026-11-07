# T03 · Elaborar diagrama inicial da arquitetura

> **Sprint 1** · PBI-01 · Configuração do repositório e arquitetura  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter um diagrama que mostre atores, clientes, API, banco e CI, versionado como código no repositório.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Diagrama de containers (C4 nível 2) em Mermaid: `docs/sprint1/arquitetura.md`, seção 2. Versão atualizada: `docs/diagramas/arquitetura.md`.
- ADR-05: diagramas como código em Mermaid, renderizados pelo GitHub.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint1/arquitetura.md` | Existente | Seção 2: atores (4 perfis), Web SPA, Mobile, API REST, Swagger, PostgreSQL e pipeline |
| `docs/diagramas/arquitetura.md` | Existente | Versão da Sprint 2, com a web e o mobile contra a mesma API |

## 4. O que fazer em cada arquivo

### Como alterar o diagrama

- Editar o bloco ` ```mermaid ` no próprio `.md`; não exportar imagem.
- Cada seta tem rótulo com o protocolo (REST/JSON, JDBC).
- Validar a sintaxe abrindo a prévia no GitHub antes de aprovar o PR (cuidado com `;` e aspas dentro de rótulos).

### O que o diagrama deve mostrar

- Atores: Administrador, Pesquisador, Coordenador (web) e Participante (mobile).
- Web e mobile falam com a mesma API com `Authorization: Bearer <JWT>`.
- API ↔ PostgreSQL por JDBC/Hibernate com migrations Flyway; GitHub Actions rodando testes e cobertura.

## 7. Critérios de aceite

- [ ] O diagrama renderiza no GitHub.
- [ ] Está de acordo com o código atual.

## 8. Cuidados e fora de escopo

- Atualize o diagrama quando entrar um novo container (por exemplo, um serviço de relatórios).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
