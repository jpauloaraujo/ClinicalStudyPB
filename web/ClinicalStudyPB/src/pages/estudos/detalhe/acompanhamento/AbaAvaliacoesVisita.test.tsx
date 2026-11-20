import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type {
  AtividadeComResultado,
  ResultadoAtividadeResponse,
  ResultadosDaVisitaResponse,
} from '../../../../api/tipos'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import { erroHttp, renderizarAba } from '../../../../test/ambiente'
import type { PerfilUsuario } from '../../../../types/usuario'
import { AbaAvaliacoesVisita } from './AbaAvaliacoesVisita'

vi.mock('../../../../api/visitasAgendadas', () => ({
  visitasAgendadasApi: {
    buscar: vi.fn(),
    registrarDesfecho: vi.fn(),
    resultados: vi.fn(),
    registrarResultado: vi.fn(),
  },
}))

const resultadosApi = vi.mocked(visitasAgendadasApi.resultados)
const registrarResultado = vi.mocked(visitasAgendadasApi.registrarResultado)

// Dados fictícios: uma visita REALIZADA com duas avaliações, uma registrada e uma pendente
function resultadoDe(sobrescrever: Partial<ResultadoAtividadeResponse> = {}): ResultadoAtividadeResponse {
  return {
    acaoId: 'acao-pressao',
    valor: '120/80',
    unidade: 'mmHg',
    observacao: 'Sentado, após 5 minutos',
    registradoPor: 'Pesquisador Demo (Ficticio)',
    registradoEm: '2026-11-14T10:00:00-03:00',
    alteradoPor: 'Pesquisador Demo (Ficticio)',
    alteradoEm: '2026-11-14T10:00:00-03:00',
    ...sobrescrever,
  }
}

const pressao: AtividadeComResultado = {
  acaoId: 'acao-pressao',
  nome: 'Aferir pressão arterial',
  descricao: 'Medida simulada',
  resultado: resultadoDe(),
}

const peso: AtividadeComResultado = {
  acaoId: 'acao-peso',
  nome: 'Aferir peso',
  descricao: null,
  resultado: null,
}

function resultadosDaVisita(sobrescrever: Partial<ResultadosDaVisitaResponse> = {}): ResultadosDaVisitaResponse {
  return {
    visitaAgendadaId: 'va-0',
    situacaoVisita: 'REALIZADA',
    bloqueio: null,
    total: 2,
    registrados: 1,
    pendentes: 1,
    atividades: [pressao, peso],
    ...sobrescrever,
  }
}

function renderizar(perfil: PerfilUsuario = 'PESQUISADOR') {
  renderizarAba(<AbaAvaliacoesVisita visitaAgendadaId="va-0" />, { perfil })
}

async function atividade(nome: string) {
  return within(await screen.findByRole('article', { name: nome }))
}

describe('AbaAvaliacoesVisita (T91)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    resultadosApi.mockResolvedValue(resultadosDaVisita())
  })

  it('lista as avaliações com nome, descrição, selo e os valores já registrados, e o contador', async () => {
    renderizar()

    const regiao = await screen.findByRole('region', { name: 'Avaliações da visita' })
    expect(resultadosApi).toHaveBeenCalledWith('va-0')
    expect(within(regiao).getByText(/1 de 2/)).toHaveTextContent('1 de 2 avaliações registradas')

    const registrada = await atividade('Aferir pressão arterial')
    expect(registrada.getByText('Medida simulada')).toBeInTheDocument()
    expect(registrada.getByText('registrado')).toBeInTheDocument()
    expect(registrada.getByLabelText('Valor (Aferir pressão arterial)')).toHaveValue('120/80')
    expect(registrada.getByLabelText('Unidade (Aferir pressão arterial)')).toHaveValue('mmHg')
    expect(registrada.getByLabelText('Observação (Aferir pressão arterial)')).toHaveValue('Sentado, após 5 minutos')
    expect(registrada.getByText(/Última alteração por Pesquisador Demo \(Ficticio\) em 14\/11\/2026 10:00/)).toBeInTheDocument()

    const pendente = await atividade('Aferir peso')
    expect(pendente.getByText('pendente')).toBeInTheDocument()
    expect(pendente.getByLabelText('Valor (Aferir peso)')).toHaveValue('')
  })

  it('salva uma atividade por vez: envia o corpo, atualiza o selo, o contador e a autoria', async () => {
    let concluir: (resultado: ResultadoAtividadeResponse) => void = () => {}
    registrarResultado.mockReturnValue(new Promise((resolver) => (concluir = resolver)))
    renderizar()
    const pendente = await atividade('Aferir peso')
    const salvar = pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })
    expect(salvar).toBeDisabled()

    await userEvent.type(pendente.getByLabelText('Valor (Aferir peso)'), ' 72,5 ')
    await userEvent.type(pendente.getByLabelText('Unidade (Aferir peso)'), 'kg')
    await userEvent.click(salvar)

    expect(registrarResultado).toHaveBeenCalledTimes(1)
    expect(registrarResultado).toHaveBeenCalledWith('va-0', 'acao-peso', { valor: '72,5', unidade: 'kg', observacao: null })
    // Durante o envio o botão fica desabilitado
    expect(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })).toBeDisabled()

    concluir(
      resultadoDe({
        acaoId: 'acao-peso',
        valor: '72,5',
        unidade: 'kg',
        observacao: null,
        alteradoPor: 'Ana Pesquisadora',
        alteradoEm: '2026-11-16T15:20:00-03:00',
      }),
    )

    expect(await pendente.findByRole('status')).toHaveTextContent('Resultado salvo.')
    expect(pendente.getByText('registrado')).toBeInTheDocument()
    expect(pendente.getByText(/Última alteração por Ana Pesquisadora em 16\/11\/2026 15:20/)).toBeInTheDocument()
    expect(screen.getByText(/2 de 2/)).toHaveTextContent('2 de 2 avaliações registradas')
    // Sem nova alteração, não há o que salvar
    expect(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })).toBeDisabled()
  })

  it('editar um resultado registrado usa o mesmo formulário e só habilita Salvar quando algo mudou', async () => {
    registrarResultado.mockResolvedValue(resultadoDe({ valor: '130/85', alteradoEm: '2026-11-16T16:00:00-03:00' }))
    renderizar()
    const registrada = await atividade('Aferir pressão arterial')
    const salvar = registrada.getByRole('button', { name: 'Salvar (Aferir pressão arterial)' })
    const valor = registrada.getByLabelText('Valor (Aferir pressão arterial)')
    expect(salvar).toBeDisabled()

    await userEvent.clear(valor)
    await userEvent.type(valor, '120/80')
    expect(salvar).toBeDisabled()

    await userEvent.clear(valor)
    await userEvent.type(valor, '130/85')
    expect(salvar).toBeEnabled()
    await userEvent.click(salvar)

    expect(registrarResultado).toHaveBeenCalledWith('va-0', 'acao-pressao', {
      valor: '130/85',
      unidade: 'mmHg',
      observacao: 'Sentado, após 5 minutos',
    })
    expect(await registrada.findByRole('status')).toHaveTextContent('Resultado salvo.')
    expect(screen.getByText(/1 de 2/)).toBeInTheDocument()
  })

  it('valida no cliente: valor obrigatório e tamanhos máximos, com contadores', async () => {
    renderizar()
    const pendente = await atividade('Aferir peso')
    const valor = pendente.getByLabelText('Valor (Aferir peso)')

    await userEvent.type(valor, '   ')
    expect(pendente.getByText('Informe o valor do resultado.')).toBeInTheDocument()
    expect(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })).toBeDisabled()

    await userEvent.clear(valor)
    await userEvent.type(valor, '72')
    expect(pendente.getByText('2/200')).toBeInTheDocument()

    await userEvent.type(pendente.getByLabelText('Unidade (Aferir peso)'), 'u'.repeat(31))
    expect(pendente.getByText('31/30')).toBeInTheDocument()
    expect(pendente.getByText('A unidade deve ter no máximo 30 caracteres.')).toBeInTheDocument()
    expect(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })).toBeDisabled()
    expect(pendente.getByText('0/500')).toBeInTheDocument()
  })

  it('erro de valor vazio do servidor aparece junto do campo valor', async () => {
    registrarResultado.mockRejectedValue(erroHttp(400, 'O valor é obrigatório', 'VALOR_OBRIGATORIO'))
    renderizar()
    const pendente = await atividade('Aferir peso')

    await userEvent.type(pendente.getByLabelText('Valor (Aferir peso)'), '72')
    await userEvent.click(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' }))

    const campo = (await pendente.findByText('Informe o valor do resultado.')).closest('label')
    expect(campo).toHaveClass('campo--invalido')
    expect(within(campo as HTMLElement).getByLabelText('Valor (Aferir peso)')).toBeInTheDocument()
    expect(pendente.queryByRole('alert')).not.toBeInTheDocument()
    expect(pendente.getByText('pendente')).toBeInTheDocument()
  })

  it('erro de validação por campo do servidor (tamanho) aparece junto do campo', async () => {
    registrarResultado.mockRejectedValue(erroHttp(400, 'observacao: A observação deve ter no máximo 500 caracteres'))
    renderizar()
    const pendente = await atividade('Aferir peso')

    await userEvent.type(pendente.getByLabelText('Valor (Aferir peso)'), '72')
    await userEvent.click(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' }))

    const mensagem = await pendente.findByText('A observação deve ter no máximo 500 caracteres')
    expect(mensagem).toHaveClass('campo__erro')
  })

  it('erro de regra é traduzido pelo código, e código desconhecido mostra a mensagem do servidor', async () => {
    registrarResultado.mockRejectedValueOnce(erroHttp(409, 'mensagem do servidor', 'ESTUDO_ENCERRADO'))
    registrarResultado.mockRejectedValueOnce(erroHttp(409, 'Regra nova do servidor', 'CODIGO_DESCONHECIDO'))
    renderizar()
    const pendente = await atividade('Aferir peso')
    await userEvent.type(pendente.getByLabelText('Valor (Aferir peso)'), '72')

    await userEvent.click(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' }))
    expect(await pendente.findByRole('alert')).toHaveTextContent(
      'O estudo foi encerrado: os resultados não podem mais ser registrados nem editados.',
    )

    await userEvent.click(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' }))
    expect(await pendente.findByRole('alert')).toHaveTextContent('Regra nova do servidor')
  })

  it('o coordenador vê os resultados em texto, sem botões, com o aviso', async () => {
    renderizar('COORDENADOR')

    expect(await screen.findByText('Somente o pesquisador registra resultados.')).toBeInTheDocument()
    const registrada = await atividade('Aferir pressão arterial')
    expect(registrada.getByText('120/80')).toBeInTheDocument()
    expect(registrada.getByText('mmHg')).toBeInTheDocument()
    expect((await atividade('Aferir peso')).getByText('Sem resultado registrado.')).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Salvar/ })).not.toBeInTheDocument()
    expect(screen.getByText(/1 de 2/)).toBeInTheDocument()
  })

  it('visita não realizada: aviso e campos desabilitados', async () => {
    resultadosApi.mockResolvedValue(
      resultadosDaVisita({
        situacaoVisita: 'AGENDADA',
        bloqueio: 'VISITA_NAO_REALIZADA',
        registrados: 0,
        pendentes: 2,
        atividades: [{ ...pressao, resultado: null }, peso],
      }),
    )
    renderizar()

    expect(await screen.findByText('Os resultados só podem ser registrados em visitas realizadas.')).toBeInTheDocument()
    expect(screen.getByText(/0 de 2/)).toBeInTheDocument()
    const pendente = await atividade('Aferir peso')
    expect(pendente.getByLabelText('Valor (Aferir peso)')).toBeDisabled()
    expect(pendente.getByLabelText('Unidade (Aferir peso)')).toBeDisabled()
    expect(pendente.getByLabelText('Observação (Aferir peso)')).toBeDisabled()
    expect(pendente.getByRole('button', { name: 'Salvar (Aferir peso)' })).toBeDisabled()
  })

  it('estudo encerrado: aviso explicando o motivo e tudo somente leitura', async () => {
    resultadosApi.mockResolvedValue(resultadosDaVisita({ bloqueio: 'ESTUDO_ENCERRADO' }))
    renderizar()

    expect(await screen.findByText(/O estudo foi encerrado: os resultados ficam disponíveis somente para consulta/)).toBeInTheDocument()
    expect((await atividade('Aferir pressão arterial')).getByText('120/80')).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Salvar/ })).not.toBeInTheDocument()
  })

  it('visita sem atividades de avaliação mostra o texto próprio', async () => {
    resultadosApi.mockResolvedValue(resultadosDaVisita({ total: 0, registrados: 0, pendentes: 0, atividades: [] }))
    renderizar()

    expect(await screen.findByText('Esta visita não tem avaliações previstas.')).toBeInTheDocument()
    expect(screen.queryByText(/avaliações registradas/)).not.toBeInTheDocument()
  })

  it('indica o carregamento', async () => {
    resultadosApi.mockReturnValue(new Promise(() => {}))
    renderizar()

    expect(await screen.findByText('Carregando avaliações…')).toBeInTheDocument()
  })

  it('mostra o erro do servidor e permite tentar de novo', async () => {
    resultadosApi.mockRejectedValueOnce(erroHttp(500, 'Erro interno do servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('article', { name: 'Aferir peso' })).toBeInTheDocument()
    expect(resultadosApi).toHaveBeenCalledTimes(2)
  })
})
