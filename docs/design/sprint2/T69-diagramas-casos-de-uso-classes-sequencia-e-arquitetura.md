# T69 · Diagramas: casos de uso, classes, sequência e arquitetura

> **Sprint 2** · PBI-18 · Documentação e entrega da Release 1  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Sex 06/11  
> **Depende de:** T36, T60  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter, em Mermaid e renderizando no GitHub, os diagramas de casos de uso, classes, sequência (avançar o estado e incluir participante) e arquitetura, atualizados com o que foi implementado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Todos os diagramas estão em `docs/diagramas/` e foram validados com o parser do Mermaid; falta só conferir a renderização na prévia do GitHub depois do push.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/diagramas/casos-de-uso.md` | Existente | Quatro perfis × RF01 a RF11, por Sprint, conforme `Permissoes` |
| `docs/diagramas/dominio.md` | Existente | Diagrama de classes (atualizado com o implementado), estados e pré-condições |
| `docs/diagramas/sequencia-avancar-estado.md` | Existente | Pesquisador → API → serviço → regra → histórico |
| `docs/diagramas/sequencia-inclusao-participante.md` | Existente | Verificações em ordem e bloqueio de escrita do estudo |
| `docs/diagramas/arquitetura.md` | Existente | Web, mobile, backend e PostgreSQL |

## 4. O que fazer em cada arquivo

### O que cada diagrama mostra

- Casos de uso: Administrador (RF01), Pesquisador (RF04 a RF07), Coordenador (RF08 a RF11), Participante (mobile); todos fazem login (RF02) e são limitados por perfil (RF03).
- Classes: as entidades do `dominio.md` com seus relacionamentos e enums.
- Sequência de estado: `POST /transicoes` → `@PreAuthorize` → `EstudoTransicaoService` → `MaquinaEstadosEstudo` → histórico → resposta.
- Sequência de inclusão: bloqueio do estudo, estudo em recrutamento, candidato, elegibilidade, consentimento, vaga, gravação e mudança de situação.
- Arquitetura: Web SPA e Mobile → API REST → PostgreSQL, com Swagger e GitHub Actions.

### Como manter

- Editar o bloco Mermaid no próprio `.md`; evitar `;` e aspas dentro de rótulos; abrir a prévia no GitHub antes de aprovar o PR.
- Atualizar o diagrama de classes sempre que entrar uma entidade nova.

## 6. Testes

- Parser do Mermaid sobre todos os blocos (feito localmente); prévia do GitHub (pendente do push).

## 7. Critérios de aceite

- [ ] Os cinco diagramas renderizam no GitHub.
- [ ] Estão de acordo com o código atual.

## 8. Cuidados e fora de escopo

- A sequência de inclusão só estava completa depois da T60, como o plano previa.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
