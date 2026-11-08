# T51 · Backend de candidatos

> **Sprint 2** · PBI-13 · RF08 Cadastro de candidatos  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Ter 03/11  
> **Depende de:** T38, T41  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Cadastrar, listar e editar candidatos de um estudo em recrutamento, com código gerado pelo sistema e sem duplicidade.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento anterior (T38, T41, T51 juntas): `docs/sprint2/T38-T41-T51-implementacao.md`.
- Situações do candidato e transições: `docs/diagramas/dominio.md`, seção 3.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/CandidatoController.java` | Existente | `GET`, `POST /api/estudos/{estudoId}/candidatos`; `GET`, `PUT /api/candidatos/{id}` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/CandidatoService.java` | Existente | `criar`, `atualizar`, `buscar`, `listar(estudoId, situacao, pagina, tamanho)`, `buscarEntidade` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/CandidatoRequest.java, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/CandidatoResponse.java, backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/PaginaResponse.java` | Existente | Request: nome (até 120), nascimento no passado, contato (até 180). Response: inclui avaliação vigente e consentimento |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/CandidatoRepository.java` | Existente | Consultas de duplicidade e paginação |

## 4. O que fazer em cada arquivo

### Regras

- Só com o estudo em recrutamento (409 `ESTUDO_FORA_DE_RECRUTAMENTO`).
- Código `CAND-001`, `CAND-002`... único no estudo, gerado com o estudo bloqueado para escrita.
- Mesmo nome (ignorando maiúsculas) e mesma data de nascimento no estudo: 409 `CANDIDATO_DUPLICADO`, também na edição de outro candidato.
- Situação inicial `CADASTRADO`; edição permitida até a inclusão (409 `CANDIDATO_JA_INCLUIDO`).
- Nenhum dado real ou documento pessoal é armazenado.

### Listagem

- Parâmetros `situacao`, `pagina` e `tamanho`; ordenação por código; resposta `PaginaResponse {conteudo, pagina, tamanho, totalElementos, totalPaginas}`.

### Permissões

- Escrita do coordenador; leitura de coordenador e pesquisador; administrador e participante sem acesso.

## 6. Testes

- Cobertura geral em `RecrutamentoIntegrationTest`; suíte dedicada prevista na T53.

## 7. Critérios de aceite

- [ ] Cadastrar, listar, filtrar e editar funcionam pelo Swagger UI.

## 8. Cuidados e fora de escopo

- A geração do código depende do bloqueio do estudo; não gerar o código fora da transação do serviço.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
