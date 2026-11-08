# T64 · Cobertura mínima de 80% no CI (JaCoCo)

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Sex 30/10 (relatório) e Qui 05/11 (bloqueio)  
> **Depende de:** T38  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Medir a cobertura de linhas do código de negócio (domínio e serviços), publicar o relatório no CI e falhar o pipeline abaixo de 80%.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/pom.xml` | Existente | Plugin JaCoCo 0.8.14: propriedades `jacoco.cobertura.minima` (0.80) e `jacoco.haltOnFailure`, execução `verificar-cobertura` com `includes` em `domain/**` e `service/**` |
| `.github/workflows/ci.yml` | Existente | Variável `COBERTURA_BLOQUEAR` (hoje `true`), passo do resumo e artefato `cobertura-jacoco` |
| `.github/scripts/resumo-cobertura.py` | Existente | Tabela de cobertura no resumo da execução |

## 4. O que fazer em cada arquivo

### O que conta

- Código de negócio = pacotes `domain` e `service`. Ficam de fora: `controller` (incluindo DTOs), `config`, `security`, `repository` e a classe `main`.

### Como alternar o bloqueio

- Trocar `COBERTURA_BLOQUEAR` entre `true` e `false` no `ci.yml`; localmente: `./mvnw verify -Djacoco.haltOnFailure=true`.

### Relatório

- `backend/ClinicalStudyPB/target/site/jacoco/index.html`; no CI, o artefato `cobertura-jacoco` (14 dias).

## 6. Testes

- Medição de 02/11/2026: 93,4% das linhas de domínio e serviços.

## 7. Critérios de aceite

- [ ] O relatório aparece como artefato e no resumo.
- [ ] Abaixo de 80% o pipeline falha.

## 8. Cuidados e fora de escopo

- Ao criar uma classe de serviço ou de domínio nova, criar o teste junto: a regra é medida por pacote, não por arquivo.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
