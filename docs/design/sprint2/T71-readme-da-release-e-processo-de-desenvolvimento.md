# T71 · README da release e processo de desenvolvimento

> **Sprint 2** · PBI-18 · Documentação e entrega da Release 1  
> **Responsável:** I2 · **Estimativa:** 4h · **Prazo:** Seg 09/11 (manhã)  
> **Depende de:** T66, T70  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

## 1. Objetivo

Criar o README da Release 1, com a situação de cada requisito, e a seção de processo de desenvolvimento das Sprints 1 e 2.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Hoje não existe a pasta `releases/` no repositório.
- Fontes: `docs/sprint1/status-sprint1.md`, `docs/sprint2/Tasks até 01-11 — mapeamento e complemento.md`, `docs/sprint2/T44 e T47 — resultado dos testes.md`, o plano da Sprint 2 e o Azure Boards (burndown e retrospectiva).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `releases/release1/README.md` | Criar | README da release |

## 4. O que fazer em cada arquivo

### Estrutura de releases/release1/README.md

- 1. **Objetivo e sprints abrangidas** (Sprint 1: 13 a 26/10; Sprint 2: 27/10 a 09/11).
- 2. **Situação de cada requisito**: tabela RF01 a RF11 com "concluído", "parcial" ou "não implementado", com justificativa.
- 3. **Funcionalidades** com capturas de tela.
- 4. **Alterações de arquitetura e stack** em relação ao plano (por exemplo: nomes dos endpoints em português, JaCoCo com bloqueio, MSW na web).
- 5. **Resultados de testes e cobertura**: totais, cobertura de domínio e serviços, resultado do E2E.
- 6. **Problemas conhecidos**: revogação de JWT não implementada, lista de erros por campo ausente, itens parciais.

### Seção "Processo de desenvolvimento" (Sprints 1 e 2)

- Backlog e tarefas: PBIs e tasks (CSV em `docs/azure-boards/sprint2-backlog.csv`; plano da Sprint 1 em `docs/sprint1/sprint1-plano.md`).
- Papéis e contribuições dos sete integrantes (tabela por pessoa, com as tasks de cada um).
- Burndown do Azure DevOps (planejado × realizado) e a análise.
- Revisão e retrospectiva: impedimentos e ações de melhoria.

## 7. Critérios de aceite

- [ ] Rascunho completo na sexta; burndown final e retrospectiva incluídos na segunda de manhã.
- [ ] Links relativos funcionando no GitHub.

## 8. Cuidados e fora de escopo

- Ser honesto sobre o que ficou parcial; a avaliação usa a versão da tag.
- O burndown e a retrospectiva dependem de dados do Azure DevOps (I2).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
