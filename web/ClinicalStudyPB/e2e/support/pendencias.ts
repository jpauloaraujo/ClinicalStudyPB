// Telas do fluxo da Release 1 que ainda não existem na web. Enquanto uma delas estiver pendente, os testes que
// dependem dela ficam marcados com `test.fixme` (aparecem como "ignorados" no relatório, com o motivo), em vez
// de falhar ou de serem apagados. Quando a tela for entregue, troque o valor abaixo para `false`: todos os
// testes dependentes passam a rodar, sem outra alteração.
export const TELAS_PENDENTES = {
  // T58: seção Consentimento da ficha do candidato (texto do termo, confirmação, botão Registrar e comprovante)
  consentimento: false,
  // T61: seção Inclusão da ficha (pré-condições e botão "Incluir no estudo") e aba Participantes ("X de N vagas")
  inclusao: false,
}

export const MOTIVO_CONSENTIMENTO = 'Depende da tela de consentimento da ficha do candidato (T58), ainda não implementada.'
export const MOTIVO_INCLUSAO = 'Depende da tela de inclusão e da aba Participantes (T61), ainda não implementadas.'
export const MOTIVO_FLUXO_COMPLETO = 'Depende das telas de consentimento (T58) e de inclusão (T61), ainda não implementadas.'

export const consentimentoPronto = () => !TELAS_PENDENTES.consentimento
export const inclusaoPronta = () => !TELAS_PENDENTES.inclusao
export const fluxoCompletoPronto = () => consentimentoPronto() && inclusaoPronta()
