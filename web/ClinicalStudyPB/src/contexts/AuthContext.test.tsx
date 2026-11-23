import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { lerToken, salvarToken } from '../services/api'
import { autenticacaoApi } from '../services/recursos'
import { AuthProvider } from './AuthContext'
import { useAuth } from './authContexto'

vi.mock('../services/recursos', () => ({ autenticacaoApi: { entrar: vi.fn(), usuarioAtual: vi.fn() } }))

const entrar = vi.mocked(autenticacaoApi.entrar)
const usuarioAtual = vi.mocked(autenticacaoApi.usuarioAtual)

// Tela mínima que consome o AuthContext como as telas reais fazem
function Consumidor() {
  const { usuario, token, isAuthenticated, carregando, login, logout } = useAuth()
  return (
    <div>
      <p>{carregando ? 'carregando' : isAuthenticated ? `logado: ${usuario?.nome} (${usuario?.perfil})` : 'visitante'}</p>
      <p>{`token: ${token ?? 'nenhum'}`}</p>
      <button type="button" onClick={() => void login('ana@exemplo.com', 'senha-ficticia').catch(() => undefined)}>
        Entrar
      </button>
      <button type="button" onClick={logout}>
        Sair
      </button>
    </div>
  )
}

function renderizar() {
  render(
    <AuthProvider>
      <Consumidor />
    </AuthProvider>,
  )
}

describe('AuthContext (T21)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    salvarToken(null)
  })

  it('começa como visitante, sem token', async () => {
    renderizar()

    expect(await screen.findByText('visitante')).toBeInTheDocument()
    expect(screen.getByText('token: nenhum')).toBeInTheDocument()
  })

  it('o login expõe o usuário (nome e perfil), o token e o estado autenticado', async () => {
    entrar.mockResolvedValue('jwt-ficticio')
    usuarioAtual.mockResolvedValue({ id: 'u-1', nome: 'Ana Pesquisadora', email: 'ana@exemplo.com', perfil: 'PESQUISADOR' })
    renderizar()

    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText('logado: Ana Pesquisadora (PESQUISADOR)')).toBeInTheDocument()
    expect(screen.getByText('token: jwt-ficticio')).toBeInTheDocument()
    expect(entrar).toHaveBeenCalledWith('ana@exemplo.com', 'senha-ficticia')
    expect(lerToken()).toBe('jwt-ficticio')
  })

  it('o logout descarta o token e volta a visitante', async () => {
    entrar.mockResolvedValue('jwt-ficticio')
    usuarioAtual.mockResolvedValue({ id: 'u-1', nome: 'Ana Pesquisadora', email: 'ana@exemplo.com', perfil: 'PESQUISADOR' })
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))
    await screen.findByText(/logado:/)

    await userEvent.click(screen.getByRole('button', { name: 'Sair' }))

    expect(await screen.findByText('visitante')).toBeInTheDocument()
    expect(screen.getByText('token: nenhum')).toBeInTheDocument()
    expect(lerToken()).toBeNull()
  })

  it('o participante usa o app mobile: o login na web é recusado e a sessão não é aberta', async () => {
    entrar.mockResolvedValue('jwt-participante')
    usuarioAtual.mockResolvedValue({ id: 'u-9', nome: 'Paula Participante', email: 'paula@exemplo.com', perfil: 'PARTICIPANTE' })
    renderizar()

    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText('visitante')).toBeInTheDocument()
    expect(lerToken()).toBeNull()
  })

  it('credenciais recusadas pela API deixam a sessão fechada e sem token', async () => {
    entrar.mockRejectedValue(new Error('401'))
    renderizar()

    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText('visitante')).toBeInTheDocument()
    expect(lerToken()).toBeNull()
  })

  it('useAuth fora do AuthProvider avisa com um erro claro', () => {
    const consoleErro = vi.spyOn(console, 'error').mockImplementation(() => undefined)

    expect(() => render(<Consumidor />)).toThrow('useAuth deve ser usado dentro de <AuthProvider>')

    consoleErro.mockRestore()
  })
})
