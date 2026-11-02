// Perfis que entram na web; o participante usa o app mobile e fica fora dos testes E2E da web
export const perfisE2E = ['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'] as const
export type PerfilE2E = (typeof perfisE2E)[number]

export type Credenciais = { email: string; senha: string }

const VARIAVEIS_GERAIS = ['E2E_BASE_URL', 'E2E_API_URL', 'E2E_BOOTSTRAP_EMAIL', 'E2E_BOOTSTRAP_SENHA']

function variaveisDoPerfil(perfil: PerfilE2E) {
  return [`E2E_${perfil}_EMAIL`, `E2E_${perfil}_SENHA`]
}

// Falha com mensagem clara dizendo quais variáveis faltam, em vez de um erro obscuro mais adiante
export function exigirVariaveis() {
  const obrigatorias = [...VARIAVEIS_GERAIS, ...perfisE2E.flatMap(variaveisDoPerfil)]
  const faltando = obrigatorias.filter((nome) => !process.env[nome])
  if (faltando.length > 0) {
    throw new Error(
      `Variáveis de ambiente dos testes E2E ausentes: ${faltando.join(', ')}. ` +
        'Copie web/ClinicalStudyPB/.env.e2e.example para .env.e2e (ver README, seção "Testes E2E").',
    )
  }
}

function ler(nome: string): string {
  const valor = process.env[nome]
  if (!valor) {
    throw new Error(`Variável de ambiente ${nome} ausente (ver .env.e2e.example).`)
  }
  return valor
}

export const urlBase = () => ler('E2E_BASE_URL')
export const urlApi = () => ler('E2E_API_URL').replace(/\/$/, '')

export const credenciaisBootstrap = (): Credenciais => ({
  email: ler('E2E_BOOTSTRAP_EMAIL'),
  senha: ler('E2E_BOOTSTRAP_SENHA'),
})

export const credenciaisDe = (perfil: PerfilE2E): Credenciais => ({
  email: ler(`E2E_${perfil}_EMAIL`),
  senha: ler(`E2E_${perfil}_SENHA`),
})
