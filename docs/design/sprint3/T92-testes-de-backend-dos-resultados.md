# T92 · Testes de backend dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Dom 15/11  
> **Depende de:** T89, T90  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Provar cada regra do RF14, a edição, os pendentes e os endpoints com PostgreSQL real, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `ParticipacaoServiceTest` (Mockito), `AvaliacaoEndpointTest` (matriz de validação e de permissões por perfil) e `ParticipacaoEndpointTest`.
- `CenarioRecrutamento` com os métodos das T78 e T85 e `TokenDeTeste`; para ter uma visita `REALIZADA` pela API, o teste agenda em dia permitido (hoje) e registra a realizada.
- O estudo de teste precisa de visita prevista com atividades de categoria `AVALIACAO_CLINICA` e de outra categoria (por exemplo `EXAME_LABORATORIAL`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoAtividadeServiceTest.java` | Criar | Regras isoladas, edição e pendentes (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoRequestValidacaoTest.java` | Criar | Limites do DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoAtividadeEndpointTest.java` | Criar | 200, 201, 400, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `visitaRealizada(...)` e `acaoDeAvaliacao(...)` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T92-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Regras isoladas (`ResultadoAtividadeServiceTest`)

- **RQ-T92-01** Visita `AGENDADA`, `AUSENTE` e `CANCELADA`: `VISITA_NAO_REALIZADA`; visita `REALIZADA`: aceita.
- **RQ-T92-02** Atividade de outra categoria (`EXAME_LABORATORIAL`, `MEDICACAO`, `QUESTIONARIO`...): `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`; atividade de **outra visita prevista**: `ATIVIDADE_NAO_PERTENCE_A_VISITA`.
- **RQ-T92-03** Estudo **encerrado**: `ESTUDO_ENCERRADO` na edição e no primeiro registro; recrutamento e execução aceitam.
- **RQ-T92-04** Valor vazio, só com espaços e nulo: `VALOR_OBRIGATORIO`; valor de 200 caracteres aceito.
- **RQ-T92-05** A ordem das verificações (visita não realizada vence a categoria; a categoria vence o estudo encerrado, e este vence o valor vazio).
- **RQ-T92-06** Resultado **parcial**: salvar uma atividade não exige as outras; `registrados`, `pendentes` e `total` corretos a cada passo.
- **RQ-T92-07** Edição: atualiza `valor`, `unidade`, `observacao`, `alteradoPor` e `alteradoEm`, e **não** altera `registradoPor` nem `registradoEm`; o primeiro registro grava os dois pares iguais.
- **RQ-T92-08** `bloqueio` na consulta: `VISITA_NAO_REALIZADA`, `ESTUDO_ENCERRADO` e `null` (e, depois da T102, `PARTICIPANTE_RETIRADO`).

### Validação do DTO

- **RQ-T92-09** `valor` obrigatório, 1 a 200 caracteres; `unidade` até 30; `observacao` até 500 (valores-limite 200/201, 30/31 e 500/501).

### Endpoints com PostgreSQL real

- **RQ-T92-10** `PUT`: **201** no primeiro registro e **200** na edição, com o corpo completo; 400 (valor ausente, valor com 201 caracteres, JSON malformado); 403 para **coordenador**, administrador e participante; 401 sem token; 404 (visita e atividade inexistentes); 409 de cada regra.
- **RQ-T92-11** `GET`: 200 para pesquisador e coordenador; 403 para administrador e participante; atividades de outras categorias **não** aparecem na lista; `total`, `registrados` e `pendentes` corretos; 404 para visita inexistente.
- **RQ-T92-12** Depois de registrar tudo, `pendentes` é zero; uma visita `AGENDADA` devolve as atividades sem resultado e `bloqueio = VISITA_NAO_REALIZADA`.
- **RQ-T92-13** Rodar também no job `Backend (PostgreSQL)` do CI.

### Cobertura

- **RQ-T92-14** Conferir a cobertura de `ResultadoAtividadeService` (linhas e ramos) e registrar o resultado no Pull Request; meta 95% ou mais.
- **RQ-T92-15** Toda a suíte anterior continua passando.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (9 operações).

## 7. Critérios de aceite

- [ ] Cada regra do RF14 tem teste de sucesso e de erro.
- [ ] A edição preserva o autor do primeiro registro e atualiza a última alteração.
- [ ] Os endpoints respondem 200, 201, 400, 403, 404 e 409 com tokens reais.
- [ ] A cobertura da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- Não depender da data do dia.
- O coordenador recebe **403** (permissão), nunca 409, na escrita.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
