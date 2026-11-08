# T36 · Modelo de domínio e regras de negócio

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Qua 28/10  
> **Depende de:** Sprint 1 integrada e premissas validadas  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Definir as entidades, os estados e as regras do recrutamento antes de codificar, em um diagrama e em regras escritas aprovadas pela equipe.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Entregue: `docs/diagramas/dominio.md` (classes, matriz de estados, pré-condições e as nove regras) e `docs/sprint2/Implementação do modelo de domínio de recrutamento.md`.
- As regras vêm da especificação fornecida pela equipe ("coisas a afazer esw.txt"). A aprovação em PR por I3 ou I4 e a apresentação na daily ainda são do processo.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/diagramas/dominio.md` | Existente | Diagrama de classes em Mermaid, matriz de transição, pré-condições e as nove regras |
| `docs/sprint2/Implementação do modelo de domínio de recrutamento.md` | Existente | O que foi criado no backend (modelo, V4, controllers, serviços, DTOs) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/` | Existente | Entidades, enums e regras puras |

## 4. O que fazer em cada arquivo

### Entidades (nome do plano → código)

- `Criterion` → `Criterio` (abstrata), `CriterioInclusao` (peso, determinante), `CriterioExclusao`.
- `PlannedVisit` → `VisitaPlanejada` (dia previsto, tolerância, ordem) com `Acao` (nome, descrição, categoria).
- `Candidate` → `Candidato` (código gerado, nome fictício, nascimento, contato, usuário opcional, situação).
- `EligibilityEvaluation` → `AvaliacaoElegibilidade` e `EligibilityAnswer` → `RespostaAvaliacao`. `Consent` → `Consentimento`. `Participation` → `Participacao`. `study_status_history` → `HistoricoStatusEstudo`.

### Estados do estudo

- PLANEJAMENTO → RECRUTAMENTO → EXECUCAO → ENCERRADO, um por vez; sair de planejamento exige um critério de inclusão e uma visita.

### As nove regras

- 1. Critérios e visitas só mudam em planejamento. 2. Estados só avançam, um por vez; só o pesquisador avança; cada mudança registra usuário e data.
- 3. Candidato só com o estudo em recrutamento; código gerado; dados fictícios; nome + nascimento não repete.
- 4. Avaliação responde a todos os critérios; elegível = inclusões determinantes atendidas e nenhuma exclusão satisfeita; a nova avaliação vale e a anterior fica no histórico.
- 5. Consentimento independe da avaliação; a inclusão exige os dois. 6. Inclusão só com estudo em recrutamento, candidato elegível, consentimento e vaga, em transação com bloqueio.
- 7. Permissões: pesquisador escreve RF05 a RF07; coordenador escreve RF08 a RF11; administrador fora. 8. Usuário participante opcional no candidato (conta fica para a Sprint 3).
- 9. Erros: 400 validação, 403 perfil, 404 inexistente, 409 regra violada com código.

### Como fechar a task

- Apresentar o diagrama e as regras na daily, registrar as respostas do PO e aprovar o PR por I3 ou I4. Se o PO mudar uma regra, mudam o backend, as telas e os testes das tasks citadas na tabela de regras do `dominio.md`.

## 6. Testes

- `DominioRecrutamentoTest` (regras puras) e `MaquinaEstadosEstudoTest`.

## 7. Critérios de aceite

- [ ] Diagrama e regras aprovados em Pull Request por I3 ou I4.
- [ ] Regras registradas com as respostas do PO.

## 8. Cuidados e fora de escopo

- A ordem do critério (`ordem`) do plano não foi criada: os critérios saem ordenados por título.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
