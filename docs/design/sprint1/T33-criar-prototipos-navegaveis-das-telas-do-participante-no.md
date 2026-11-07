# T33 · Criar protótipos navegáveis das telas do participante no Figma

> **Sprint 1** · PBI-08 · Protótipos navegáveis das interfaces  
> **Responsável:** I7 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T23  
> **Situação:** 📋 Entrega fora do código (Figma ou processo): este documento especifica tudo o que precisa ser produzido, e a entrega continua sendo obrigatória

## 1. Objetivo

Produzir no Figma o protótipo navegável do aplicativo do participante (M01 a M11), em modo apresentação e sem backend. A entrega é o arquivo Figma com link compartilhado; este documento é a especificação completa.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Requisitos completos: `docs/sprint1/prototipos-mobile.md`, seções 3 a 9.
- No código já existem o login e as quatro abas do app (T12); estudos e visitas ainda são "em breve". O protótipo mostra como eles ficarão.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `Figma → arquivo "ClinicalStudyPB — Mobile"` | Criar | Páginas: 00 Capa e links, 01 Tokens e componentes, 02 Telas, 03 Fluxos, 04 Estados |
| `mobile/ClinicalStudyPB/src/app/login.tsx, mobile/ClinicalStudyPB/src/app/(tabs)/estudos.tsx, mobile/ClinicalStudyPB/src/app/(tabs)/visitas.tsx, mobile/ClinicalStudyPB/src/app/(tabs)/perfil.tsx` | Existente | Telas do app que o protótipo deve espelhar |
| `README.md`, seção 10 | Alterar | Registrar o link do protótipo |

## 4. O que fazer em cada arquivo

### Telas (conteúdo mínimo e ações)

- M01 Login: logo/nome, e-mail, senha, botão Entrar, aviso de dados fictícios; ações: Entrar, mostrar/ocultar senha.
- M02 Início: saudação com o nome, card da próxima visita, atalho para Meus estudos; ações: abrir a visita, abrir estudos.
- M03 Meus estudos: cards com título, status do estudo e data de inclusão; ação: abrir o detalhe.
- M04 Detalhe do estudo: título, objetivo, responsável, período, status da participação e visitas previstas; ações: ver visitas, Solicitar retirada.
- M05 Visitas — Próximas: lista por data com data/hora, nome, estudo e status (agendada, confirmada, reagendamento solicitado); ação: abrir o detalhe.
- M06 Visitas — Histórico: visitas passadas com status (realizada, ausente, cancelada) e resumo de comparecimento.
- M07 Detalhe da visita: data/hora, local, atividades previstas e status; ações: Confirmar presença, Solicitar reagendamento.
- M08 Confirmar presença: resumo da visita; ações: Confirmar, Cancelar.
- M09 Solicitar reagendamento: motivo, observação e sugestão de nova data; ações: Enviar, Cancelar.
- M10 Solicitar retirada: aviso do impacto, motivo e confirmação explícita; ações: Confirmar retirada, Voltar.
- M11 Perfil: nome, e-mail, perfil "Participante"; ação: Sair.

### Estados e componentes

- Estados: erro de login, lista vazia (estudos e visitas), carregando, sucesso e erro de cada ação.
- Componentes: botão, campo de texto, card de estudo, card de visita, etiqueta de status, modal de confirmação, barra de abas (Início, Estudos, Visitas, Perfil).
- Dados fictícios de apoio: dois estudos, quatro visitas futuras e três passadas, nomes e datas inventados.

### Acessibilidade e identidade

- Alvos de toque de pelo menos 44 × 44 px; contraste de texto 4,5:1; texto base de 16 px ou mais; retirada com botão destrutivo e confirmação explícita.
- Mesma paleta e tipografia da web (primária `#1d5fa8`, perigo `#b42318`), alinhada com I6.

### Entrega

- Telas em molduras de 375 × 812; nomes `M01 Login`...`M11 Perfil`; modo apresentação com início em M01; link com permissão de visualização; registrar no README e no Azure Boards.

## 7. Critérios de aceite

- [ ] Telas M01 a M11 prontas, com componentes reutilizáveis.
- [ ] Estados de erro, vazio e sucesso prototipados.
- [ ] Link compartilhado e registrado.

## 8. Cuidados e fora de escopo

- Esta task é obrigatória, mesmo sendo fora do código.
- Alinhar com I3 os status de visita (agendada, confirmada, realizada, ausente, cancelada) antes de desenhar.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
