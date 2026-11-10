# T68 · Integração e correções: recrutamento

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I5 · **Estimativa:** 4h · **Prazo:** Sex 06/11  
> **Depende de:** T52, T55, T58, T61  
> **Situação:** ✅ Implementada (`scripts/verificar-recrutamento.py`, 58 verificações; falta só registrar os bugs no Azure Boards; veja `docs/sprint2/T68 — Integração do recrutamento (roteiro e correções).md`)

## 1. Objetivo

Repetir a integração da T67 para o recrutamento (candidato, avaliação, consentimento e inclusão), nos casos de sucesso e de erro, e corrigir os defeitos.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Ambiente: o `docker-compose.yml` da T67 e os dados de demonstração da T63.
- Telas: `AbaCandidatos`, `FichaCandidatoPage` com `SecaoAvaliacao`, `SecaoConsentimento` (T58) e `SecaoInclusao` (T61), e `AbaParticipantes` (T61).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `Telas e módulos de API das tasks T52, T55, T58, T61` | Alterar conforme os defeitos | Correções de mensagens, rótulos, paginação e estados |
| `docs/api/openapi-sprint2.yaml` | Alterar (se o contrato mudar) | No mesmo PR da correção |
| `docs/sprint2/` | Criar (opcional) | Relatório curto dos defeitos encontrados e corrigidos |

## 4. O que fazer em cada arquivo

### Roteiro (sucesso)

- Coordenador: cadastrar candidato → avaliar (resultado aprovado) → registrar consentimento → incluir → ver o participante na aba e o contador de vagas aumentar.

### Roteiro (erro)

- Estudo fora de recrutamento: botões desabilitados com explicação; candidato duplicado: mensagem do servidor no formulário; avaliação incompleta: botão desabilitado; vagas esgotadas: mensagem clara na inclusão (estudo de demonstração a uma vaga do limite).

### Conferências

- Paginação e filtro por situação; mensagens por código de erro; rótulos de formulário.
- Regressão de login, usuários e estudos da Sprint 1.

## 6. Testes

- Cada correção ganha teste de componente ou de integração; `npm test`, `npm run lint`, `./mvnw verify` verdes.

## 7. Critérios de aceite

- [ ] O fluxo de recrutamento funciona sem mocks nos casos de sucesso e de erro.
- [ ] Defeitos registrados como Bug e os da sua área corrigidos.

## 8. Cuidados e fora de escopo

- Depende da T61 para o passo de inclusão.
- Não mudar o contrato sem avisar o time e atualizar o YAML.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
