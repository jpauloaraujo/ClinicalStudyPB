# T61 · Tela de inclusão e lista de participantes

> **Sprint 2** · PBI-16 · RF11 Inclusão de participantes  
> **Responsável:** I6 · **Estimativa:** 5h · **Prazo:** Sex 06/11  
> **Depende de:** T52, T60  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Na ficha do candidato, mostrar as quatro pré-condições da inclusão e o botão "Incluir no estudo"; na aba Participantes, listar os incluídos com o contador "X de N vagas".

## 2. Contexto: o que já existe e deve ser reaproveitado

- Espaços já existentes: `SecaoInclusao.tsx` (texto provisório; hoje recebe só `{candidato}` e passa a receber também `estudo` e `aoIncluir`) e a rota `participantes` em `router.tsx`, que hoje usa `AbaProvisoria`.
- API pronta: `participantesApi.listar(estudoId)`, `participantesApi.vagas(estudoId)` e `participantesApi.incluir(estudoId, candidatoId)`.
- Dados das pré-condições vêm do que já existe: `useEstudoDetalhe().estudo.status`, `candidato.resultadoAvaliacao` e `candidato.consentimentoRegistrado` (resposta de `GET /api/candidatos/{id}`) e `vagas.restantes`.
- Padrões a imitar: `SecaoAvaliacao.tsx` (seção da ficha) e `AbaCandidatos.tsx` (aba com lista); `Confirmacao.tsx` para o diálogo; `useCarregamento` para carregar.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoInclusao.tsx` | Existente (ampliado na implementação) | Lista das 4 pré-condições, botão Incluir com confirmação e mensagem do erro |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.tsx` | Existente (criado na implementação) | Lista de participantes e contador "X de N vagas" |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/mensagensInclusao.ts` | Existente (criado na implementação) | Tradução dos códigos de erro da inclusão em frases claras |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente (ampliado na implementação) | Trocar `<AbaProvisoria titulo="Participantes" />` por `<AbaParticipantes />` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/FichaCandidatoPage.tsx` | Existente (ampliado na implementação) | Passar `estudo={estudo}` e `aoIncluir={recarregar}` a `<SecaoInclusao>`, no mesmo padrão da `SecaoAvaliacao`, para atualizar a situação depois de incluir |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoInclusao.test.tsx` | Existente (criado na implementação) | Testes da seção |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.test.tsx` | Existente (criado na implementação) | Testes da aba |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaProvisoria.tsx` | Removido na implementação | Só a aba Participantes a usava |

## 4. O que fazer em cada arquivo

### SecaoInclusao.tsx

- Carregar `participantesApi.vagas(estudo.id)`; montar a lista de pré-condições, cada uma como atendida ou pendente: (1) estudo em recrutamento; (2) candidato elegível (`resultadoAvaliacao === "APROVADO"`); (3) consentimento registrado (`consentimentoRegistrado`); (4) vaga disponível (`restantes > 0`).
- Botão **Incluir no estudo** habilitado só com as quatro atendidas e só para o coordenador; ao clicar, abrir `Confirmacao`; ao confirmar, chamar `participantesApi.incluir`, recarregar a ficha (`aoIncluir`) e as vagas.
- Candidato já incluído (`situacao === "INCLUIDO"`): mostrar "Candidato incluído" e esconder o botão.
- Erro 409: mostrar `mensagensInclusao[codigo]` (cai na mensagem do servidor se o código for desconhecido).

### mensagensInclusao.ts

- Mapa por código: `ESTUDO_FORA_DE_RECRUTAMENTO`, `CANDIDATO_JA_INCLUIDO`, `CANDIDATO_NAO_ELEGIVEL`, `CONSENTIMENTO_AUSENTE`, `VAGAS_ESGOTADAS`. O código chega em `converterErro(...)` (campo `codigo`, ver `src/api/erros.ts`).

### AbaParticipantes.tsx

- Carregar `listar` e `vagas`; tabela com código, nome fictício, data de inclusão (`formatarData`) e quem incluiu; cabeçalho "X de N vagas" (`incluidos` de `limite`); estados de carregamento, vazio e erro.
- Atualizar depois de uma inclusão: a aba é recarregada ao ser aberta, pois cada aba é uma rota.

### Nomes acessíveis que o E2E (T66) usa na inclusão e em Participantes (implementados)

- A seção continua com `aria-label="Inclusão"` (região "Inclusão").
- Botão de nome exato "Incluir no estudo", habilitado só com as quatro pré-condições atendidas; ao clicar abre um `role=dialog` com o botão "Confirmar".
- Sem vaga, a pré-condição "Vaga disponível" aparece pendente com o texto "Vagas esgotadas" e o botão fica desabilitado. Sem consentimento, o texto da pré-condição contém "consentimento" e o botão fica desabilitado.
- Depois de incluir, a seção contém "incluído" e a situação do candidato na ficha passa a "Incluído".
- Aba Participantes: uma tabela com o código e o nome fictício de cada participante e um texto no formato "1 de 5 vagas" (incluídos de limite).

## 5. Contrato, regras e códigos de erro

- Códigos 409 da inclusão: `ESTUDO_FORA_DE_RECRUTAMENTO`, `CANDIDATO_JA_INCLUIDO`, `CANDIDATO_NAO_ELEGIVEL`, `CONSENTIMENTO_AUSENTE`, `VAGAS_ESGOTADAS` (ver T60).

## 6. Testes

- `SecaoInclusao.test.tsx`: cada pré-condição pendente desabilita o botão; todas atendidas habilitam; confirmar chama a API com o `candidatoId`; 409 de vagas esgotadas mostra a mensagem; incluído esconde o botão; coordenador-only.
- `AbaParticipantes.test.tsx`: lista, contador, vazio e erro. Use `renderizarAba` e `erroHttp` (`src/test/ambiente.tsx`).
- Mock: conferir `src/mocks/handlers.ts` (inclusão e vagas já existem).

## 7. Critérios de aceite

- [ ] O botão só habilita com as quatro pré-condições atendidas.
- [ ] Depois de incluir, a situação vira "Incluído" e o contador aumenta.
- [ ] Cada código de erro tem uma mensagem clara.

## 8. Cuidados e fora de escopo

- Não alterar `SecaoAvaliacao` nem `SecaoConsentimento`.
- A API é quem decide: a tela só espelha as pré-condições; mesmo com o botão habilitado o servidor pode recusar (outra pessoa pode ter ocupado a vaga).
- Esta task é a que destrava a T66 (E2E do fluxo completo).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
