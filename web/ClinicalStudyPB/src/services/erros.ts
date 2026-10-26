import { isAxiosError } from 'axios'

export type ErroApi = {
  mensagem: string
  // Campo do formulário, quando a API devolve "campo: mensagem" (Bean Validation)
  campo?: string
  status?: number
}

const MENSAGENS_POR_STATUS: Record<number, string> = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Seu perfil não tem permissão para esta ação.',
  404: 'Registro não encontrado.',
}

// Converte erros do Axios no formato {"erro": "..."} do ApiExceptionHandler (T31)
export function extrairErro(erro: unknown): ErroApi {
  if (!isAxiosError(erro)) {
    return { mensagem: 'Erro inesperado. Tente novamente.' }
  }
  if (!erro.response) {
    return { mensagem: 'Não foi possível conectar à API. Verifique se o backend está em execução.' }
  }

  const status = erro.response.status
  const texto = (erro.response.data as { erro?: string } | undefined)?.erro

  if (status === 400 && texto) {
    const separador = texto.indexOf(': ')
    if (separador > 0 && /^\w+$/.test(texto.slice(0, separador))) {
      return { mensagem: texto.slice(separador + 2), campo: texto.slice(0, separador), status }
    }
  }
  return { mensagem: texto ?? MENSAGENS_POR_STATUS[status] ?? `Erro ${status} na API.`, status }
}
