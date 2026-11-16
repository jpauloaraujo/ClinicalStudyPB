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

// Data e hora da API (aaaa-mm-ddTHH:mm:ss-03:00) para dd/mm/aaaa HH:mm. Lê o texto sem converter o fuso:
// a API já devolve o horário local do estudo, e converter pelo fuso do navegador mudaria o dia ou a hora.
export function formatarDataHora(dataHora: string | null): string {
  if (!dataHora) return '—'
  return `${formatarData(dataHoraParaData(dataHora))} ${dataHoraParaHora(dataHora)}`
}

// Parte da data (aaaa-mm-dd) de uma data e hora da API, para preencher campos de formulário
export function dataHoraParaData(dataHora: string): string {
  return dataHora.slice(0, 10)
}

// Parte da hora (HH:mm) de uma data e hora da API, para preencher campos de formulário
export function dataHoraParaHora(dataHora: string): string {
  return dataHora.slice(11, 16)
}
