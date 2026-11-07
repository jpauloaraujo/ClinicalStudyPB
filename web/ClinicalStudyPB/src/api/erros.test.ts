import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import { converterErro } from './erros'

function erroDaApi(status: number, data?: unknown): AxiosError {
  const resposta = { status, statusText: '', data, headers: {}, config: { headers: new AxiosHeaders() } } as AxiosResponse
  return new AxiosError('falhou', 'ERR_BAD_REQUEST', undefined, undefined, resposta)
}

describe('converterErro', () => {
  it('converte "campo: mensagem" de um 400 em mensagem por campo', () => {
    const erro = converterErro(erroDaApi(400, { erro: 'descricao: A descrição deve ter entre 5 e 500 caracteres' }))

    expect(erro.status).toBe(400)
    expect(erro.mensagem).toBe('A descrição deve ter entre 5 e 500 caracteres')
    expect(erro.campos).toEqual({ descricao: 'A descrição deve ter entre 5 e 500 caracteres' })
  })

  it('mantém o código da regra e a mensagem de um 409, sem campos', () => {
    const erro = converterErro(erroDaApi(409, { erro: 'Não há vagas disponíveis', codigo: 'VAGAS_ESGOTADAS' }))

    expect(erro).toEqual({ status: 409, codigo: 'VAGAS_ESGOTADAS', mensagem: 'Não há vagas disponíveis', campos: {} })
  })

  it('não trata como campo um texto com dois-pontos que não é "campo: mensagem"', () => {
    const erro = converterErro(erroDaApi(400, { erro: 'Corpo da requisição inválido: verifique o JSON' }))

    expect(erro.campos).toEqual({})
    expect(erro.mensagem).toBe('Corpo da requisição inválido: verifique o JSON')
  })

  it('aceita uma lista de erros por campo, se a API a devolver', () => {
    const erro = converterErro(
      erroDaApi(400, { erro: 'Dados inválidos', erros: [{ campo: 'titulo', mensagem: 'O título é obrigatório' }, { campo: 'peso', mensagem: 'O peso deve ser maior que zero' }] }),
    )

    expect(erro.campos).toEqual({ titulo: 'O título é obrigatório', peso: 'O peso deve ser maior que zero' })
    expect(erro.mensagem).toBe('Dados inválidos')
  })

  it('usa mensagens padrão para 401, 403 e 404 sem corpo', () => {
    expect(converterErro(erroDaApi(401)).mensagem).toBe('Sua sessão expirou. Entre novamente.')
    expect(converterErro(erroDaApi(403)).mensagem).toBe('Seu perfil não tem permissão para esta ação.')
    expect(converterErro(erroDaApi(404)).mensagem).toBe('Registro não encontrado.')
    expect(converterErro(erroDaApi(500)).mensagem).toBe('Erro 500 na API.')
  })

  it('explica quando não consegue conectar à API e trata erros que não são do Axios', () => {
    expect(converterErro(new AxiosError('Network Error', 'ERR_NETWORK')).mensagem).toMatch(/conectar à API/)
    expect(converterErro(new Error('qualquer')).mensagem).toBe('Erro inesperado. Tente novamente.')
  })

  it('lê a lista de erros por campo (errosCampo) do formato novo do backend, com todos os campos de uma vez', () => {
    const erro = converterErro(
      erroDaApi(400, {
        status: 400,
        codigoRegra: 'VALIDACAO_CAMPOS',
        mensagem: 'descricao: A descrição é obrigatória',
        erro: 'descricao: A descrição é obrigatória',
        codigo: 'VALIDACAO_CAMPOS',
        errosCampo: [
          { campo: 'descricao', erro: 'A descrição é obrigatória' },
          { campo: 'tipo', erro: 'O tipo é obrigatório' },
        ],
      }),
    )

    expect(erro.campos).toEqual({ descricao: 'A descrição é obrigatória', tipo: 'O tipo é obrigatório' })
    expect(erro.status).toBe(400)
  })
})
