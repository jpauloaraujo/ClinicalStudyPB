# T58 · Tela de consentimento

> **Sprint 2** · PBI-15 · RF10 Consentimento simulado  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Qui 05/11  
> **Depende de:** T52, T57  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Na ficha do candidato, a seção Consentimento: texto do termo simulado, confirmação, data e hora, botão Registrar e, depois, o comprovante.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `SecaoConsentimento.tsx` ocupava o espaço reservado da ficha com um texto provisório e recebia só `{candidato}`; a T58 o substituiu e acrescentou a prop `aoRegistrar`.
- API pronta: `consentimentoApi.buscar(candidatoId)` (404 enquanto não registrado) e `consentimentoApi.registrar(candidatoId, dados)`.
- Padrão a imitar: `SecaoAvaliacao.tsx` (carrega, envia, trata erros e recarrega).
- Hook de carga: `hooks/useCarregamento` (`dados`, `erro`, `statusErro`, `carregando`, `recarregar`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoConsentimento.tsx` | Existente (ampliado na implementação) | Trocar o corpo do componente pelo formulário e pelo comprovante |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/FichaCandidatoPage.tsx` | Existente (ampliado na implementação) | Passar `aoRegistrar={recarregar}` a `<SecaoConsentimento>`, no mesmo padrão do `aoAvaliar` da `SecaoAvaliacao`, para atualizar a situação do candidato |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/candidato/SecaoConsentimento.test.tsx` | Existente (criado na implementação) | Testes de componente da seção |
| `web/ClinicalStudyPB/src/mocks/handlers.ts` | Conferir | O mock de consentimento já existe; ajustar só se divergir do contrato |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/recrutamento.css` | Existente (ampliado na implementação) | Estilos do termo rolável e do comprovante |

## 4. O que fazer em cada arquivo

### SecaoConsentimento.tsx

- Carregar o consentimento com `consentimentoApi.buscar`; `statusErro === 404` significa "ainda não registrado" e não é erro de tela.
- Sem registro: mostrar o termo simulado em uma caixa rolável (`TCLE-SIMULADO v1`), o aviso "Consentimento simulado, dados fictícios", a caixa de confirmação obrigatória, o campo de data e hora (padrão: agora) e o botão **Registrar**, desabilitado até a confirmação ser marcada.
- Ao registrar, enviar `{dataHora, versaoTermo}`; tratar 409 (`CONSENTIMENTO_DUPLICADO`, `CANDIDATO_JA_INCLUIDO`, `ESTUDO_FORA_DE_RECRUTAMENTO`) e 400 (data futura) com `converterErro(...).mensagem` em um alerta.
- Com registro: mostrar o comprovante (data e hora, responsável, versão do termo) e esconder o formulário.
- Só o coordenador registra; para outros perfis a seção é somente leitura.
- Chamar `aoRegistrar()` depois de registrar para a ficha recarregar a situação do candidato (`CONSENTIDO`).

### Nomes acessíveis que o E2E (T66) usa na seção Consentimento (implementados)

- A seção continua com `aria-label="Consentimento"` (região "Consentimento").
- Caixa de confirmação: `role=checkbox` com nome contendo "confirm" (por exemplo, "Confirmo o consentimento do candidato").
- Botão de nome exato "Registrar", desabilitado até a caixa ser marcada.
- Depois de registrar, aparece o texto "Comprovante" e a versão do termo (`TCLE-SIMULADO v1`); o botão "Registrar" deixa de existir.
- A situação do candidato na ficha passa a "Consentido" (a ficha é recarregada por `aoRegistrar`).

## 5. Contrato, regras e códigos de erro

- `POST /api/candidatos/{id}/consentimento` → 201 com `{id, candidatoId, dataHora, responsavel, versaoTermo}`; erros 400, 403, 404, 409 conforme o YAML.

## 6. Testes

- `SecaoConsentimento.test.tsx` (use `renderizarAba` e `erroHttp` de `src/test/ambiente.tsx`): botão desabilitado sem confirmação; registro envia os dados; comprovante aparece e o formulário some; erro 409 aparece em alerta; consentimento já registrado abre direto no comprovante; perfil sem permissão não vê o formulário.

## 7. Critérios de aceite

- [ ] O botão só habilita com a confirmação marcada.
- [ ] Depois de registrar, o comprovante substitui o formulário.
- [ ] Nenhuma mudança fora de `SecaoConsentimento` e do mínimo na ficha.

## 8. Cuidados e fora de escopo

- Não alterar `SecaoAvaliacao` nem `SecaoInclusao`: cada colega trabalha na sua seção.
- Revogar o consentimento não existe nesta Sprint.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
