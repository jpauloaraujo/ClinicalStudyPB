import { render } from '@testing-library/react'
import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import type { ReactElement } from 'react'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router'
import { vi } from 'vitest'
import { SessaoContext, type Sessao } from '../contexts/sessao'
import type { Estudo } from '../types/estudo'
import type { PerfilUsuario } from '../types/usuario'

// Utilitários dos testes de componente das abas do estudo (Sprint 2)

export const estudoBase: Estudo = {
  id: '3f6c1a52-0d1e-4c8b-9a77-5b2e8d4f1c90',
  titulo: 'Estudo de Hipertensão Leve',
  objetivo: 'Avaliar a adesão ao tratamento em adultos com hipertensão leve.',
  responsavel: { id: 'u-pesq', nome: 'Ana Pesquisadora', email: 'ana@clinicalstudy.test' },
  dataInicio: '2026-11-01',
  dataFim: '2027-03-31',
  limiteParticipantes: 30,
  status: 'PLANEJAMENTO',
}

/** Erro HTTP da API no formato único {"erro", "codigo"}. */
export function erroHttp(status: number, mensagem: string, codigo?: string): AxiosError {
  const resposta = {
    status,
    statusText: '',
    data: { erro: mensagem, codigo },
    headers: {},
    config: { headers: new AxiosHeaders() },
  } as AxiosResponse
  return new AxiosError(mensagem, 'ERR_BAD_REQUEST', undefined, undefined, resposta)
}

export function sessaoDe(perfil: PerfilUsuario): Sessao {
  return {
    usuario: { id: 'u-1', nome: 'Usuário de Teste', email: 'teste@clinicalstudy.test', perfil },
    carregando: false,
    entrar: vi.fn(),
    sair: vi.fn(),
  }
}

type OpcoesAba = {
  perfil: PerfilUsuario
  estudo?: Estudo
  // Rota em que o elemento é montado e endereço inicial (padrão: raiz)
  rota?: string
  caminho?: string
}

/**
 * Monta uma aba como o EstudoDetalhePage faz (contexto do <Outlet>), com o perfil escolhido, sem passar
 * pelos guards de rota: assim dá para testar o modo somente leitura de perfis que não abririam a aba.
 */
export function renderizarAba(elemento: ReactElement, { perfil, estudo = estudoBase, rota = '*', caminho = '/' }: OpcoesAba) {
  const recarregar = vi.fn()
  render(
    <SessaoContext.Provider value={sessaoDe(perfil)}>
      <MemoryRouter initialEntries={[caminho]}>
        <Routes>
          <Route element={<Outlet context={{ estudo, recarregar }} />}>
            <Route path={rota} element={elemento} />
          </Route>
        </Routes>
      </MemoryRouter>
    </SessaoContext.Provider>,
  )
  return { recarregar }
}
