import { isAxiosError } from 'axios'

export type ErroContrato = {
  mensagem: string
  // Código estável da regra de negócio violada (presente nos 409)
  codigo?: string
  status?: number
  // Mensagens por campo do formulário: { descricao: 'A descrição deve ter ...' }
  campos: Record<string, string>
}

type CorpoErro = {
  erro?: string
  codigo?: string
  // Lista por campo, aceita caso a API passe a devolvê-la além do formato atual
  erros?: { campo: string; mensagem: string }[]
  // Lista por campo que o backend devolve em todo erro de validação (400): [{campo, erro}]
  errosCampo?: { campo: string; erro: string }[]
  // Aliases do formato novo; "erro" e "codigo" continuam presentes para os clientes existentes
  mensagem?: string
  codigoRegra?: string
}

const MENSAGENS_POR_STATUS: Record<number, string> = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Seu perfil não tem permissão para esta ação.',
  404: 'Registro não encontrado.',
}

// Converte o formato único de erro da API ({"erro", "codigo"}) em mensagem geral, código da regra e
// mensagens por campo. Na validação (400) a API devolve "campo: mensagem" em "erro".
export function converterErro(erro: unknown): ErroContrato {
  if (!isAxiosError(erro)) {
    return { mensagem: 'Erro inesperado. Tente novamente.', campos: {} }
  }
  if (!erro.response) {
    return { mensagem: 'Não foi possível conectar à API. Verifique se o backend está em execução.', campos: {} }
  }

  const status = erro.response.status
  const corpo = (erro.response.data ?? {}) as CorpoErro
  const campos: Record<string, string> = {}

  if (Array.isArray(corpo.erros)) {
    for (const item of corpo.erros) {
      campos[item.campo] = item.mensagem
    }
  }
  if (Array.isArray(corpo.errosCampo)) {
    for (const item of corpo.errosCampo) {
      campos[item.campo] ??= item.erro
    }
  }
  let mensagem = corpo.erro ?? corpo.mensagem ?? MENSAGENS_POR_STATUS[status] ?? `Erro ${status} na API.`

  if (status === 400 && corpo.erro) {
    const separador = corpo.erro.indexOf(': ')
    if (separador > 0 && /^\w+$/.test(corpo.erro.slice(0, separador))) {
      const campo = corpo.erro.slice(0, separador)
      mensagem = corpo.erro.slice(separador + 2)
      campos[campo] ??= mensagem
    }
  }
  return { mensagem, codigo: corpo.codigo ?? (status === 409 ? corpo.codigoRegra : undefined), status, campos }
}
