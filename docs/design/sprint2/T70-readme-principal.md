# T70 · README principal

> **Sprint 2** · PBI-18 · Documentação e entrega da Release 1  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Sex 06/11  
> **Depende de:** T64, T66, T69  
> **Situação:** 🟡 Implementada no repositório; faltam os nomes dos 7 integrantes e as capturas de cada integrante (as capturas de todas as telas estão em `docs/imagens/`, geradas por `web/ClinicalStudyPB/scripts/capturar-telas.mjs`)

## 1. Objetivo

Fechar o `README.md` da raiz com as sete seções exigidas, capturas de tela e instruções validadas em um clone limpo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O README já tem as seções 1 a 10 (apresentação, arquitetura, funcionalidades, stack, instalação, testes, API, releases, estrutura e documentos de referência) e foi atualizado até a Sprint 2.
- Falta o que depende de outras tasks: capturas de tela, números finais de testes e cobertura, credenciais da T63 e links das três releases.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `README.md` | Alterar | Completar as lacunas abaixo |
| `docs/capturas/` (ou `docs/img/`) | Criar | Capturas de tela de sucesso e de erro, uma pasta por tela, enviadas por quem implementou |

## 4. O que fazer em cada arquivo

### Lacunas a fechar

- Seção 3 (funcionalidades RF01 a RF11): capturas de sucesso e de erro de cada requisito.
- Seção 5: usuários e senhas fictícias do perfil `demo` (T63) e o comando do ambiente completo (T67).
- Seção 6: relatório final do JaCoCo (T64) e resultado do E2E (T66), com os números reais.
- Seção 7: link do Swagger UI e do contrato `docs/api/openapi-sprint2.yaml`.
- Seção 8: links das três releases (a Release 1 só existe depois da T72).

### Validação

- Seguir o README em um clone limpo, sem ajuda, e corrigir o que travar.
- Conferir que todos os links relativos abrem (especialmente os de nomes com acento em `docs/sprint2/`).

## 7. Critérios de aceite

- [ ] As sete seções completas, com capturas.
- [ ] Instruções validadas em um clone limpo.

## 8. Cuidados e fora de escopo

- Capturas só com dados fictícios.
- A documentação é responsabilidade de todos: cada integrante envia as capturas das telas que fez até sexta ao meio-dia.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
