// Converte a data ISO da API (aaaa-mm-dd) para dd/mm/aaaa
export function formatarData(data: string | null): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}
