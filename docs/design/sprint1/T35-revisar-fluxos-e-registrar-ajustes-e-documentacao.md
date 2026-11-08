# T35 · Revisar fluxos e registrar ajustes e documentação

> **Sprint 1** · PBI-08 · Protótipos navegáveis das interfaces  
> **Responsável:** I1 · **Estimativa:** 2h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T32, T33, T34  
> **Situação:** 📋 Entrega fora do código (Figma ou processo): este documento especifica tudo o que precisa ser produzido, e a entrega continua sendo obrigatória

## 1. Objetivo

Percorrer os protótipos web e mobile, conferir se cobrem os requisitos e o código, e registrar por escrito os ajustes necessários. A entrega é o documento `docs/sprint1/revisao-prototipos.md`, preenchido com o roteiro abaixo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Entradas: links do Figma (T32 e T33), roteiro de demonstração (T34), `docs/sprint1/status-sprint1.md` e as telas web/mobile implementadas.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint1/revisao-prototipos.md` | Criar | Registro da revisão (modelo abaixo) |
| `Azure Boards → T35` | Atualizar | Link do documento e lista de ajustes com responsável |

## 4. O que fazer em cada arquivo

### Roteiro da revisão

- 1. Abrir o protótipo web e percorrer os três fluxos (administrador, pesquisador, coordenador) em modo apresentação.
- 2. Abrir o protótipo mobile e percorrer F1 a F7.
- 3. Para cada fluxo, marcar na tabela se cobre o requisito e se bate com o código.
- 4. Anotar cada divergência e decidir: ajustar o protótipo ou o código (e em qual task).

### Modelo do documento `revisao-prototipos.md`

- Cabeçalho: data, revisor(es), links do Figma web e mobile.
- Tabela de cobertura: Fluxo | Requisito (RF) | Existe no protótipo? | Existe no código? | Divergência | Ação e responsável.
- Linhas mínimas: RF01 usuários; RF02 login/logout (web e mobile); RF03 acesso negado; RF04 estudos; RF05 a RF11 (detalhe do estudo, recrutamento); RF17 a RF20 (mobile).
- Lista de ajustes pedidos, cada um com task e responsável. Decisões tomadas e pendências.

### Divergências já conhecidas para conferir

- Seletor de status do formulário de estudo: desabilitado no código (o estado muda no detalhe).
- Responsável do estudo: o pesquisador logado, sem seletor.
- Mobile: login e abas existem; estudos e visitas ainda "em breve".

## 7. Critérios de aceite

- [ ] O documento está commitado com os dois links e a tabela preenchida.
- [ ] Cada ajuste tem responsável e task.
- [ ] Link do documento no Azure Boards.

## 8. Cuidados e fora de escopo

- Sem os links do Figma (T32 e T33) a revisão fica incompleta.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
