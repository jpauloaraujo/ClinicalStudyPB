import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { VisitaResponse } from '../../../../api/tipos'
import { acoesApi, visitasApi } from '../../../../api/visitas'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AbaVisitas } from './AbaVisitas'

vi.mock('../../../../api/visitas', () => ({
  visitasApi: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn(), excluir: vi.fn() },
  acoesApi: { criar: vi.fn(), atualizar: vi.fn(), excluir: vi.fn() },
}))

const listar = vi.mocked(visitasApi.listar)
const criar = vi.mocked(visitasApi.criar)
const atualizar = vi.mocked(visitasApi.atualizar)
const excluir = vi.mocked(visitasApi.excluir)
const criarAcao = vi.mocked(acoesApi.criar)
const atualizarAcao = vi.mocked(acoesApi.atualizar)
const excluirAcao = vi.mocked(acoesApi.excluir)

const inicial: VisitaResponse = {
  id: 'v-1', estudoId: estudoBase.id, nome: 'Triagem', diaPrevisto: 0, tolerancia: 0, ordem: 1,
  acoes: [{ id: 'a-1', visitaId: 'v-1', nome: 'Aferição de pressão', descricao: null, categoria: 'AVALIACAO_CLINICA' }],
}
const retorno: VisitaResponse = {
  id: 'v-2', estudoId: estudoBase.id, nome: 'Retorno mensal', diaPrevisto: 30, tolerancia: 5, ordem: 2, acoes: [],
}

describe('AbaVisitas (T49)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listar.mockResolvedValue([inicial, retorno])
  })

  it('mostra o título imediatamente e lista as visitas na ordem recebida, com as atividades', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })

    expect(screen.getByRole('heading', { level: 2, name: 'Visitas previstas e atividades' })).toBeInTheDocument()
    const cartoes = await screen.findAllByText(/^Dia \d+ · /)
    expect(cartoes.map((c) => c.textContent)).toEqual(['Dia 0 · Triagem', 'Dia 30 · Retorno mensal'])
    expect(screen.getByText('± 5 dias')).toBeInTheDocument()
    expect(screen.getByText('1 atividade')).toBeInTheDocument()
    expect(screen.getByText('Aferição de pressão')).toBeInTheDocument()
    expect(screen.getByText('Avaliação clínica', { selector: '.etiqueta' })).toBeInTheDocument()
    expect(screen.getByText('Nenhuma atividade cadastrada nesta visita.')).toBeInTheDocument()
  })

  it('valida a visita localmente: dia negativo e tolerância acima de 30', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova visita' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da visita/), 'Visita extra')
    await userEvent.type(within(formulario).getByLabelText(/^Dia previsto/), '-3')
    await userEvent.clear(within(formulario).getByLabelText(/^Tolerância/))
    await userEvent.type(within(formulario).getByLabelText(/^Tolerância/), '31')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar visita' }))

    expect(screen.getByText('O dia previsto deve ser um inteiro maior ou igual a zero')).toBeInTheDocument()
    expect(screen.getByText('A tolerância deve ficar entre 0 e 30 dias')).toBeInTheDocument()
    expect(criar).not.toHaveBeenCalled()
  })

  it('cria a visita com números e recarrega a lista', async () => {
    criar.mockResolvedValue(retorno)
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova visita' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da visita/), 'Semana 2')
    await userEvent.type(within(formulario).getByLabelText(/^Dia previsto/), '14')
    await userEvent.clear(within(formulario).getByLabelText(/^Tolerância/))
    await userEvent.type(within(formulario).getByLabelText(/^Tolerância/), '2')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar visita' }))

    expect(criar).toHaveBeenCalledWith(estudoBase.id, { nome: 'Semana 2', diaPrevisto: 14, tolerancia: 2 })
    expect(listar).toHaveBeenCalledTimes(2)
  })

  it('mostra a mensagem do servidor quando o dia previsto se repete (409)', async () => {
    criar.mockRejectedValue(erroHttp(409, 'Já existe uma visita prevista para esse dia neste estudo', 'VISITA_DIA_DUPLICADO'))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova visita' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da visita/), 'Repetida')
    await userEvent.type(within(formulario).getByLabelText(/^Dia previsto/), '30')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar visita' }))

    expect(await within(formulario).findByRole('alert')).toHaveTextContent('Já existe uma visita prevista para esse dia')
  })

  it('cadastra uma atividade em uma visita, exigindo a categoria', async () => {
    criarAcao.mockResolvedValue(inicial.acoes[0])
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova atividade da visita Triagem' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da atividade/), 'Coleta de sangue')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar atividade' }))
    expect(screen.getAllByText('A categoria é obrigatória').length).toBeGreaterThan(0)
    expect(criarAcao).not.toHaveBeenCalled()

    await userEvent.selectOptions(within(formulario).getByLabelText(/^Categoria/), 'EXAME_LABORATORIAL')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar atividade' }))

    expect(criarAcao).toHaveBeenCalledWith('v-1', { nome: 'Coleta de sangue', descricao: undefined, categoria: 'EXAME_LABORATORIAL' })
  })

  it('exclui uma atividade e uma visita (esta, só após confirmar)', async () => {
    excluirAcao.mockResolvedValue()
    excluir.mockResolvedValue()
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')

    await userEvent.click(screen.getByRole('button', { name: 'Excluir atividade' }))
    expect(excluirAcao).toHaveBeenCalledWith('v-1', 'a-1')

    await userEvent.click(screen.getByRole('button', { name: 'Excluir visita Retorno mensal' }))
    expect(excluir).not.toHaveBeenCalled()
    await userEvent.click(within(screen.getByRole('dialog', { name: 'Excluir visita?' })).getByRole('button', { name: 'Excluir' }))
    expect(excluir).toHaveBeenCalledWith(estudoBase.id, 'v-2')
  })

  it('fica somente leitura fora do planejamento', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR', estudo: { ...estudoBase, status: 'EXECUCAO' } })

    expect(await screen.findByText('Dia 0 · Triagem')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('não está mais em planejamento')
    expect(screen.queryByRole('form')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Excluir/ })).not.toBeInTheDocument()
  })

  it('fica somente leitura para o coordenador', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Dia 30 · Retorno mensal')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Seu perfil tem acesso apenas para consulta')
    expect(screen.queryByRole('form')).not.toBeInTheDocument()
  })

  it('avisa quando não há visitas', async () => {
    listar.mockResolvedValue([])
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByText(/Nenhuma visita prevista/)).toBeInTheDocument()
  })

  // T50: lacunas dos testes de T49
  it('mostra o erro de carregamento e não exibe a lista nem o formulário de visitas vazio', async () => {
    listar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.queryByText(/^Dia \d+ · /)).not.toBeInTheDocument()
  })

  it('indica o carregamento enquanto a lista não chega', async () => {
    listar.mockReturnValue(new Promise(() => {}))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByText('Carregando visitas…')).toBeInTheDocument()
  })

  it('mostra o erro do servidor (500) ao criar a visita e mantém o que foi digitado', async () => {
    criar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova visita' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da visita/), 'Semana 4')
    await userEvent.type(within(formulario).getByLabelText(/^Dia previsto/), '28')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar visita' }))

    expect(await within(formulario).findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(within(formulario).getByLabelText(/^Nome da visita/)).toHaveValue('Semana 4')
  })

  it('mostra no campo o erro de validação devolvido pelo servidor', async () => {
    criar.mockRejectedValue(erroHttp(400, 'nome: O nome deve ter no máximo 100 caracteres'))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')
    const formulario = screen.getByRole('form', { name: 'Nova visita' })

    await userEvent.type(within(formulario).getByLabelText(/^Nome da visita/), 'Visita')
    await userEvent.type(within(formulario).getByLabelText(/^Dia previsto/), '5')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Adicionar visita' }))

    expect(await screen.findByText('O nome deve ter no máximo 100 caracteres')).toBeInTheDocument()
    expect(within(formulario).queryByRole('alert')).not.toBeInTheDocument()
  })

  it('edita uma visita com os dados atuais preenchidos e envia a atualização', async () => {
    atualizar.mockResolvedValue(retorno)
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 30 · Retorno mensal')

    await userEvent.click(screen.getByRole('button', { name: 'Editar visita Retorno mensal' }))

    const formulario = screen.getByRole('form', { name: 'Editar visita' })
    expect(within(formulario).getByLabelText(/^Nome da visita/)).toHaveValue('Retorno mensal')
    expect(within(formulario).getByLabelText(/^Dia previsto/)).toHaveValue('30')
    await userEvent.clear(within(formulario).getByLabelText(/^Tolerância/))
    await userEvent.type(within(formulario).getByLabelText(/^Tolerância/), '7')
    await userEvent.click(within(formulario).getByRole('button', { name: /Salvar/ }))

    expect(atualizar).toHaveBeenCalledWith(estudoBase.id, 'v-2', { nome: 'Retorno mensal', diaPrevisto: 30, tolerancia: 7 })
    expect(listar).toHaveBeenCalledTimes(2)
  })

  it('edita uma atividade existente', async () => {
    atualizarAcao.mockResolvedValue(inicial.acoes[0])
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 0 · Triagem')

    await userEvent.click(screen.getByRole('button', { name: 'Editar atividade' }))

    const formulario = screen.getByRole('form', { name: 'Editar atividade da visita Triagem' })
    expect(within(formulario).getByLabelText(/^Nome da atividade/)).toHaveValue('Aferição de pressão')
    await userEvent.clear(within(formulario).getByLabelText(/^Nome da atividade/))
    await userEvent.type(within(formulario).getByLabelText(/^Nome da atividade/), 'Pressão arterial')
    await userEvent.click(within(formulario).getByRole('button', { name: /Salvar/ }))

    expect(atualizarAcao).toHaveBeenCalledWith('v-1', 'a-1', expect.objectContaining({ nome: 'Pressão arterial', categoria: 'AVALIACAO_CLINICA' }))
  })

  it('mostra o motivo quando o servidor recusa a exclusão da visita (409)', async () => {
    excluir.mockRejectedValue(erroHttp(409, 'Critérios e visitas só podem ser alterados com o estudo em planejamento', 'ESTUDO_FORA_DE_PLANEJAMENTO'))
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Dia 30 · Retorno mensal')

    await userEvent.click(screen.getByRole('button', { name: 'Excluir visita Retorno mensal' }))
    await userEvent.click(within(screen.getByRole('dialog', { name: 'Excluir visita?' })).getByRole('button', { name: 'Excluir' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('só podem ser alterados com o estudo em planejamento')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByText('Dia 30 · Retorno mensal')).toBeInTheDocument()
  })

  it('para o coordenador e fora de planejamento não oferece editar visita nem atividade', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Dia 0 · Triagem')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Editar/ })).not.toBeInTheDocument()
  })
})
