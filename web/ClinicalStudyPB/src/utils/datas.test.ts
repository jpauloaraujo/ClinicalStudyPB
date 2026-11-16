import { describe, expect, it } from 'vitest'
import { dataHoraParaData, dataHoraParaHora, formatarData, formatarDataHora, formatarJanela } from './datas'

describe('datas (T77)', () => {
  it('formata a data ISO como dd/mm/aaaa e usa um traço quando não há data', () => {
    expect(formatarData('2026-11-19')).toBe('19/11/2026')
    expect(formatarData(null)).toBe('—')
  })

  it('formata a janela como "dd/mm/aaaa a dd/mm/aaaa"', () => {
    expect(formatarJanela('2026-11-14', '2026-11-24')).toBe('14/11/2026 a 24/11/2026')
  })

  it('formata data e hora lendo o texto, sem converter o fuso do navegador', () => {
    expect(formatarDataHora('2026-11-19T09:30:00-03:00')).toBe('19/11/2026 09:30')
    // Perto da meia-noite o dia não pode mudar por causa do fuso
    expect(formatarDataHora('2026-11-19T23:50:00-03:00')).toBe('19/11/2026 23:50')
    expect(formatarDataHora(null)).toBe('—')
  })

  it('separa data e hora para preencher os campos do formulário', () => {
    expect(dataHoraParaData('2026-11-19T09:30:00-03:00')).toBe('2026-11-19')
    expect(dataHoraParaHora('2026-11-19T09:30:00-03:00')).toBe('09:30')
  })
})
