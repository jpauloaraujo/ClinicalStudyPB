// Converte a data ISO da API (aaaa-mm-dd) para dd/mm/aaaa
export function formatarData(data: string | null): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

// A janela válida de uma visita: "dd/mm/aaaa a dd/mm/aaaa"
export function formatarJanela(inicio: string, fim: string): string {
  return `${formatarData(inicio)} a ${formatarData(fim)}`
}

// Converte a data e hora ISO 8601 da API (ex.: 2026-11-20T09:30:00-03:00) para dd/MM/yyyy HH:mm.
// A API já devolve no fuso do centro de pesquisa; o texto é lido como veio, sem converter para o fuso do navegador,
// para a tela mostrar o mesmo horário em qualquer computador (e o dia não mudar perto da meia-noite).
export function formatarDataHora(dataHora: string | null | undefined): string {
  if (!dataHora) return '—'
  const partes = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(dataHora)
  if (!partes) return dataHora
  const [, ano, mes, dia, hora, minuto] = partes
  return `${dia}/${mes}/${ano} ${hora}:${minuto}`
}

// Parte da data (aaaa-mm-dd) de uma data e hora da API, para preencher campos de formulário
export function dataHoraParaData(dataHora: string): string {
  return dataHora.slice(0, 10)
}

// Parte da hora (HH:mm) de uma data e hora da API, para preencher campos de formulário
export function dataHoraParaHora(dataHora: string): string {
  return dataHora.slice(11, 16)
}
