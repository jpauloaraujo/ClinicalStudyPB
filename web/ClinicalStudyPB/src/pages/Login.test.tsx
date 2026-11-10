import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../contexts/AuthContext'
import { lerToken, salvarToken } from '../services/api'
import { autenticacaoApi } from '../services/recursos'
import { Login } from './Login'

vi.mock('../services/recursos', () => ({ autenticacaoApi: { entrar: vi.fn(), usuarioAtual: vi.fn() } }))

const entrar = vi.mocked(autenticacaoApi.entrar)

function renderizar() {
  render(
    <AuthProvider>
      <Login />
    </AuthProvider>,
  )
}

async function preencherEEnviar() {
  await userEvent.type(screen.getByLabelText('E-mail'), 'ana@exemplo.com')
  await userEvent.type(screen.getByLabelText('Senha'), 'senha-ficticia')
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
}

describe('Login com AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    salvarToken(null)
  })

  it('autentica pelo endpoint da API, guarda o token na chave do cliente HTTP e mostra o estado autenticado', async () => {
    entrar.mockResolvedValue('jwt-ficticio')
    renderizar()

    await preencherEEnviar()

    expect(entrar).toHaveBeenCalledWith('ana@exemplo.com', 'senha-ficticia')
    expect(await screen.findByText('Você está autenticado!')).toBeInTheDocument()
    expect(lerToken()).toBe('jwt-ficticio')
  })

  it('mostra o erro e não autentica quando a API recusa as credenciais', async () => {
    entrar.mockRejectedValue(new Error('rede'))
    renderizar()

    await preencherEEnviar()

    expect(await screen.findByRole('alert')).toBeInTheDocument()
    expect(lerToken()).toBeNull()
    expect(screen.queryByText('Você está autenticado!')).not.toBeInTheDocument()
  })

  it('sair remove o token', async () => {
    salvarToken('jwt-antigo')
    renderizar()

    await userEvent.click(screen.getByRole('button', { name: 'Sair' }))

    expect(lerToken()).toBeNull()
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
  })
})
