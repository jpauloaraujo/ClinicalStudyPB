# T04 · Criar README com estrutura e instruções iniciais

> **Sprint 1** · PBI-01 · Configuração do repositório e arquitetura  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Dar a quem clona o repositório tudo para entender o projeto e executá-lo: escopo, arquitetura, stack, instalação, testes e documentação.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `README.md` | Existente | README principal na raiz, com 10 seções |

## 4. O que fazer em cada arquivo

### Estrutura do README

- 1. Apresentação e escopo (perfis, módulos, equipe). 2. Arquitetura (backend, web, mobile). 3. Funcionalidades desenvolvidas. 4. Stack e versões.
- 5. Instalação e execução (pré-requisitos, banco, backend, web, mobile, credenciais de demonstração do seed V2).
- 6. Testes e cobertura (backend, web, E2E, CI). 7. Documentação da API (Swagger). 8. Releases. 9. Estrutura do repositório. 10. Documentos de referência.

### Como manter

- Toda task que muda um comando, uma porta, uma variável ou um endpoint atualiza o README no mesmo PR.
- Nunca colocar credencial real; usar só as fictícias do seed.
- Links relativos para `docs/`; conferir os de nomes com acento.

### Validação

- Seguir o README em um clone limpo e anotar onde travar (parte da T70).

## 7. Critérios de aceite

- [ ] Quem clona consegue subir banco, backend e web só com o README.
- [ ] Todos os links abrem.

## 8. Cuidados e fora de escopo

- O README da raiz é diferente do README da release (T71).

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
