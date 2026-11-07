# T48 · Backend de visitas previstas e atividades

> **Sprint 2** · PBI-12 · RF06 Visitas previstas e atividades  
> **Responsável:** I4 · **Estimativa:** 5h · **Prazo:** Ter 03/11  
> **Depende de:** T37, T38  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Definir as visitas previstas de um estudo (dia, tolerância, ordem) e as atividades de cada visita, só em planejamento.

## 2. Contexto: o que já existe e deve ser reaproveitado

- No código as atividades se chamam **ações** (`Acao`, tabela `acoes`); o plano as chamava de `Activity`. Categorias: `AVALIACAO_CLINICA`, `EXAME_LABORATORIAL`, `EXAME_IMAGEM`, `QUESTIONARIO`, `MEDICACAO`, `OUTROS`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/VisitaController.java` | Existente | Visitas em `/api/estudos/{estudoId}/visitas`; ações em `/api/visitas/{visitaId}/acoes` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/VisitaService.java` | Existente | Regras de visitas e ações |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/VisitaRequest.java, VisitaResponse.java, AcaoRequest.java, AcaoResponse.java` | Existente | Visita: nome (até 100), diaPrevisto (≥ 0), tolerância (0 a 30); ação: nome, descrição (até 500), categoria |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/VisitaPlanejada.java, Acao.java` | Existente | Entidades; remover a visita apaga suas ações |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaPlanejadaRepository.java, AcaoRepository.java` | Existente | Persistência; `countByEstudoId` usado na transição |

## 4. O que fazer em cada arquivo

### Regras

- Dois dias previstos iguais no mesmo estudo: 409 `VISITA_DIA_DUPLICADO`.
- A `ordem` é a posição da visita quando ordenadas pelo dia (1 = primeira) e é devolvida já calculada.
- Qualquer alteração exige estudo em planejamento (409 `ESTUDO_FORA_DE_PLANEJAMENTO`).
- A listagem devolve as visitas ordenadas, cada uma com suas ações.

## 5. Contrato, regras e códigos de erro

- Escrita: pesquisador. Leitura: pesquisador e coordenador.

## 6. Testes

- `RecrutamentoIntegrationTest.visitasEAcoes`; suíte dedicada prevista na T50.

## 7. Critérios de aceite

- [ ] Uma visita com ações é criada, consultada ordenada e removida.
- [ ] Dia repetido e estado bloqueado são recusados com o código certo.

## 8. Cuidados e fora de escopo

- A categoria avaliação clínica será a base dos resultados fictícios do RF14 na Sprint 3.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
