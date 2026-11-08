import { fireEvent, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { consentimentoApi } from '../../../../api/consentimento'
import type { CandidatoResponse, ConsentimentoResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import type { Estudo } from '../../../../types/estudo'
import type { PerfilUsuario } from '../../../../types/usuario'
import { SecaoConsentimento } from './SecaoConsentimento'

vi.mock('../../../../api/consentimento', () => ({ consentimentoApi: { buscar: vi.fn(), registrar: vi.fn() } }))

const buscar = vi.mocked(consentimentoApi.buscar)
const registrar = vi.mocked(consentimentoApi.registrar)

const estudo: Estudo = { ...estudoBase, status: 'RECRUTAMENTO' }
const candidato: CandidatoResponse = {
  id: 'cand-1', estudoId: estudo.id, codigo: 'CAND-001', nomeFicticio: 'J. S.', dataNascimento: '1990-04-12',
  contato: null, usuarioId: null, situacao: 'ELEGIVEL', resultadoAvaliacao: 'APROVADO', dataAvaliacao: '2026-11-03T10:00:00',
  consentimentoRegistrado: false, dataConsentimento: null,
}
const comprovante: ConsentimentoResponse = {
  id: 'cons-1', candidatoId: candidato.id, dataHora: '2026-11-03T09:30:00', responsavel: 'Coordenadora Fictícia',
  versaoTermo: 'TCLE-SIMULADO v1',
}

const aoRegistrar = vi.fn()

function renderizar(opcoes: { perfil?: PerfilUsuario; estudoDaFicha?: Estudo; candidatoDaFicha?: CandidatoResponse } = {}) {
  renderizarAba(<SecaoConsentimento candidato={opcoes.candidatoDaFicha ?? candidato} aoRegistrar={aoRegistrar} />, {
    perfil: opcoes.perfil ?? 'COORDENADOR',
    estudo: opcoes.estudoDaFicha ?? estudo,
  })
}

const naoRegistrado = () => buscar.mockRejectedValue(erroHttp(404, 'Consentimento não registrado para o candidato'))

describe('SecaoConsentimento (T58 e T59)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    naoRegistrado()
  })

  it('mostra o termo simulado, o aviso de dados fictícios e o botão Registrar desabilitado até confirmar', async () => {
    renderizar()

    const regiao = await screen.findByRole('region', { name: 'Consentimento' })
    expect(await within(regiao).findByText(/Consentimento simulado, dados fictícios/)).toBeInTheDocument()
    expect(within(regiao).getByLabelText('Termo de consentimento livre e esclarecido (simulado)')).toHaveTextContent('TCLE-SIMULADO v1')
    expect(within(regiao).getByRole('button', { name: 'Registrar' })).toBeDisabled()

    await userEvent.click(within(regiao).getByRole('checkbox', { name: /Confirmo/ }))

    expect(within(regiao).getByRole('button', { name: 'Registrar' })).toBeEnabled()
  })

  it('registra sem enviar data quando ela não foi editada (o servidor usa o instante atual) e avisa a ficha', async () => {
    registrar.mockResolvedValue(comprovante)
    renderizar()
    await userEvent.click(await screen.findByRole('checkbox', { name: /Confirmo/ }))

    await userEvent.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(registrar).toHaveBeenCalledWith('cand-1', {})
    expect(aoRegistrar).toHaveBeenCalledTimes(1)
  })

  it('envia a data e hora quando ela é editada', async () => {
    registrar.mockResolvedValue(comprovante)
    renderizar()
    await userEvent.click(await screen.findByRole('checkbox', { name: /Confirmo/ }))

    fireEvent.change(screen.getByLabelText('Data e hora do consentimento'), { target: { value: '2026-11-03T09:30' } })
    await userEvent.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(registrar).toHaveBeenCalledWith('cand-1', { dataHora: '2026-11-03T09:30:00' })
  })

  it('depois de registrar mostra o comprovante e some com o formulário', async () => {
    registrar.mockResolvedValue(comprovante)
    renderizar()
    await userEvent.click(await screen.findByRole('checkbox', { name: /Confirmo/ }))
    buscar.mockResolvedValue(comprovante)

    await userEvent.click(screen.getByRole('button', { name: 'Registrar' }))

    const regiao = screen.getByRole('region', { name: 'Consentimento' })
    expect(await within(regiao).findByText('Comprovante do consentimento')).toBeInTheDocument()
    expect(within(regiao).queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
    expect(within(regiao).queryByRole('checkbox')).not.toBeInTheDocument()
  })

  it('consentimento já registrado abre direto no comprovante, com data, responsável e versão do termo', async () => {
    buscar.mockResolvedValue(comprovante)
    renderizar()

    const regiao = await screen.findByRole('region', { name: 'Consentimento' })
    expect(await within(regiao).findByText('Comprovante do consentimento')).toBeInTheDocument()
    expect(within(regiao).getByText('Coordenadora Fictícia')).toBeInTheDocument()
    expect(within(regiao).getByText('TCLE-SIMULADO v1')).toBeInTheDocument()
    expect(within(regiao).getByText(/03\/11\/2026/)).toBeInTheDocument()
    expect(within(regiao).queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
    expect(within(regiao).queryByText(/Consentimento simulado, dados fictícios/)).not.toBeInTheDocument()
  })

  it('mostra a mensagem do servidor quando o consentimento é duplicado (409) e mantém o formulário', async () => {
    registrar.mockRejectedValue(erroHttp(409, 'O consentimento deste candidato já foi registrado', 'CONSENTIMENTO_DUPLICADO'))
    renderizar()
    await userEvent.click(await screen.findByRole('checkbox', { name: /Confirmo/ }))

    await userEvent.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('O consentimento deste candidato já foi registrado')
    expect(screen.getByRole('button', { name: 'Registrar' })).toBeEnabled()
    expect(aoRegistrar).not.toHaveBeenCalled()
  })

  it('mostra o erro 400 da data futura', async () => {
    registrar.mockRejectedValue(erroHttp(400, 'A data e hora do consentimento não pode ser futura'))
    renderizar()
    await userEvent.click(await screen.findByRole('checkbox', { name: /Confirmo/ }))

    await userEvent.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('não pode ser futura')
  })

  it('mostra o erro de carregamento do servidor e não oferece o formulário', async () => {
    buscar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
  })

  it('indica o carregamento enquanto o comprovante não chega', async () => {
    buscar.mockReturnValue(new Promise(() => {}))
    renderizar()

    expect(await screen.findByText('Carregando consentimento…')).toBeInTheDocument()
  })

  it('fica somente leitura para quem não é coordenador', async () => {
    renderizar({ perfil: 'PESQUISADOR' })

    expect(await screen.findByText('Seu perfil tem acesso apenas para consulta do consentimento.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
  })

  it('não permite registrar com o estudo fora de recrutamento', async () => {
    renderizar({ estudoDaFicha: { ...estudo, status: 'EXECUCAO' } })

    expect(await screen.findByText('O consentimento só pode ser registrado com o estudo em recrutamento.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
  })

  it('não permite registrar para candidato já incluído', async () => {
    renderizar({ candidatoDaFicha: { ...candidato, situacao: 'INCLUIDO' } })

    expect(await screen.findByText(/o consentimento não pode mais ser registrado/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar' })).not.toBeInTheDocument()
  })
})
