import { describe, expect, it } from 'vitest'
import { erroHttp } from '../../../../test/ambiente'
import { interpretarErroDeAgendamento, mensagemDeAgendamento } from './mensagensAcompanhamento'

const janela = { inicio: '2026-11-20', fim: '2026-11-30' }

describe('mensagensAcompanhamento (T77)', () => {
  it.each([
    ['ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO', /não está em recrutamento nem em execução/],
    ['VISITA_JA_AGENDADA', /já está agendada/],
    ['DATA_NO_PASSADO', /anterior a hoje/],
    ['VISITA_NAO_ESTA_AGENDADA', /ainda está agendada/],
    ['PARTICIPANTE_RETIRADO', /não está ativo/],
  ])('traduz %s', (codigo, esperado) => {
    expect(mensagemDeAgendamento(codigo, 'servidor')).toMatch(esperado)
  })

  it('DATA_FORA_DA_JANELA mostra a janela quando a tela a conhece e usa a do servidor quando não', () => {
    expect(mensagemDeAgendamento('DATA_FORA_DA_JANELA', 'servidor', janela)).toContain('20/11/2026 a 30/11/2026')
    expect(mensagemDeAgendamento('DATA_FORA_DA_JANELA', 'Data fora da janela de 20/11/2026 a 30/11/2026')).toBe(
      'Data fora da janela de 20/11/2026 a 30/11/2026',
    )
  })

  it('código desconhecido ou ausente cai na mensagem do servidor', () => {
    expect(mensagemDeAgendamento('NOVO_CODIGO', 'Mensagem do servidor')).toBe('Mensagem do servidor')
    expect(mensagemDeAgendamento(undefined, 'Mensagem do servidor')).toBe('Mensagem do servidor')
  })

  it('interpreta o erro: data para as regras de data, geral para as demais', () => {
    expect(interpretarErroDeAgendamento(erroHttp(409, 'x', 'DATA_FORA_DA_JANELA'), janela)).toEqual({
      geral: null,
      data: expect.stringContaining('20/11/2026 a 30/11/2026'),
    })
    expect(interpretarErroDeAgendamento(erroHttp(400, 'x', 'DATA_NO_PASSADO'))).toEqual({
      geral: null,
      data: 'A data da visita não pode ser anterior a hoje.',
    })
    expect(interpretarErroDeAgendamento(erroHttp(409, 'x', 'VISITA_JA_AGENDADA'))).toEqual({
      geral: expect.stringContaining('já está agendada'),
      data: null,
    })
  })

  it('erro por campo dataHora do servidor vai para o campo de data', () => {
    const falha = erroHttp(400, 'dataHora: A data e hora são obrigatórias', 'VALIDACAO_CAMPOS')
    expect(interpretarErroDeAgendamento(falha)).toEqual({ geral: null, data: 'A data e hora são obrigatórias' })
  })
})
