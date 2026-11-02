import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { criteriosApi } from '../../../../api/criterios'
import type { CriterioResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AbaCriterios } from './AbaCriterios'

vi.mock('../../../../api/criterios', () => ({
  criteriosApi: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn(), excluir: vi.fn() },
}))

const listar = vi.mocked(criteriosApi.listar)
const criar = vi.mocked(criteriosApi.criar)
const atualizar = vi.mocked(criteriosApi.atualizar)
const excluir = vi.mocked(criteriosApi.excluir)

const idade: CriterioResponse = {
  id: 'c-1', estudoId: estudoBase.id, tipo: 'INCLUSAO', titulo: 'Idade adulta',
  descricao: 'Candidato com 18 anos ou mais', peso: 2, determinante: true,
}
const gestante: CriterioResponse = {
  id: 'c-2', estudoId: estudoBase.id, tipo: 'EXCLUSAO', titulo: 'Gestante',
  descricao: 'Candidata em gestação confirmada', peso: null, determinante: null,
}

async function preencher(titulo: string, descricao: string) {
  await userEvent.type(screen.getByLabelText(/^Título/), titulo)
  await userEvent.type(screen.getByLabelText(/^Descrição/), descricao)
}

describe('AbaCriterios (T46)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listar.mockResolvedValue([idade, gestante])
  })

  it('lista os critérios de inclusão e de exclusão em grupos separados', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    const inclusao = await screen.findByRole('region', { name: 'Critérios de inclusão' })
    expect(within(inclusao).getByText('Idade adulta')).toBeInTheDocument()
    expect(within(inclusao).getByText('Peso 2')).toBeInTheDocument()
    expect(within(inclusao).getByText('Determinante')).toBeInTheDocument()
    const exclusao = screen.getByRole('region', { name: 'Critérios de exclusão' })
    expect(within(exclusao).getByText('Gestante')).toBeInTheDocument()
    expect(listar).toHaveBeenCalledWith(estudoBase.id)
  })

  it('pede ao menos um critério de inclusão quando a lista está vazia', async () => {
    listar.mockResolvedValue([gestante])
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByText('Adicione ao menos um critério de inclusão para avançar.')).toBeInTheDocument()
  })

  it('valida os campos localmente e não chama a API enquanto houver erro', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await preencher('Tosse', 'abc')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(screen.getByText('A descrição deve ter entre 5 e 500 caracteres')).toBeInTheDocument()
    expect(criar).not.toHaveBeenCalled()
  })

  it('exige o título', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await userEvent.type(screen.getByLabelText(/^Descrição/), 'Uma descrição válida')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(screen.getByText('O título é obrigatório')).toBeInTheDocument()
    expect(criar).not.toHaveBeenCalled()
  })

  it('cria o critério com os campos informados e recarrega a lista', async () => {
    criar.mockResolvedValue({ ...idade, id: 'c-3', titulo: 'Sem alergia' })
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await preencher('Sem alergia', 'Sem alergia conhecida ao medicamento')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(criar).toHaveBeenCalledWith(estudoBase.id, {
      tipo: 'INCLUSAO', titulo: 'Sem alergia', descricao: 'Sem alergia conhecida ao medicamento', peso: 1, determinante: true,
    })
    expect(listar).toHaveBeenCalledTimes(2)
  })

  it('esconde peso e determinante e não os envia em um critério de exclusão', async () => {
    criar.mockResolvedValue(gestante)
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await userEvent.selectOptions(screen.getByLabelText(/^Tipo/), 'EXCLUSAO')
    expect(screen.queryByLabelText(/^Peso/)).not.toBeInTheDocument()
    await preencher('Fumante', 'Fumante há mais de cinco anos')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(criar).toHaveBeenCalledWith(estudoBase.id, { tipo: 'EXCLUSAO', titulo: 'Fumante', descricao: 'Fumante há mais de cinco anos' })
  })

  it('mostra a mensagem da API quando o critério é duplicado (409)', async () => {
    criar.mockRejectedValue(erroHttp(409, 'Já existe um critério de inclusão com esse título neste estudo', 'CRITERIO_DUPLICADO'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await preencher('Idade adulta', 'Outra descrição qualquer')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe um critério de inclusão com esse título neste estudo')
  })

  it('mostra no campo o erro de validação devolvido pelo servidor', async () => {
    criar.mockRejectedValue(erroHttp(400, 'titulo: O título deve ter no máximo 120 caracteres'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await preencher('Título qualquer', 'Descrição qualquer')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(await screen.findByText('O título deve ter no máximo 120 caracteres')).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('edita um critério sem permitir trocar o tipo', async () => {
    atualizar.mockResolvedValue(idade)
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    const inclusao = await screen.findByRole('region', { name: 'Critérios de inclusão' })

    await userEvent.click(within(inclusao).getByRole('button', { name: 'Editar' }))

    const formulario = screen.getByRole('form', { name: 'Editar critério' })
    expect(within(formulario).getByLabelText(/^Tipo/)).toBeDisabled()
    expect(within(formulario).getByLabelText(/^Título/)).toHaveValue('Idade adulta')
    await userEvent.clear(within(formulario).getByLabelText(/^Título/))
    await userEvent.type(within(formulario).getByLabelText(/^Título/), 'Maior de idade')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Salvar alterações' }))

    expect(atualizar).toHaveBeenCalledWith(estudoBase.id, 'c-1', expect.objectContaining({ tipo: 'INCLUSAO', titulo: 'Maior de idade' }))
  })

  it('exclui só depois da confirmação', async () => {
    excluir.mockResolvedValue()
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    const exclusao = await screen.findByRole('region', { name: 'Critérios de exclusão' })

    await userEvent.click(within(exclusao).getByRole('button', { name: 'Excluir' }))
    const dialogo = screen.getByRole('dialog', { name: 'Excluir critério?' })
    await userEvent.click(within(dialogo).getByRole('button', { name: 'Cancelar' }))
    expect(excluir).not.toHaveBeenCalled()

    await userEvent.click(within(exclusao).getByRole('button', { name: 'Excluir' }))
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Excluir' }))

    expect(excluir).toHaveBeenCalledWith(estudoBase.id, 'c-2')
  })

  it('fica somente leitura, com aviso, quando o estudo não está em planejamento', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR', estudo: { ...estudoBase, status: 'RECRUTAMENTO' } })

    expect(await screen.findByText('Idade adulta')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('não está mais em planejamento')
    expect(screen.queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Excluir' })).not.toBeInTheDocument()
    expect(screen.queryByRole('form')).not.toBeInTheDocument()
  })

  it('fica somente leitura para o coordenador', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Idade adulta')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Seu perfil tem acesso apenas para consulta')
    expect(screen.queryByRole('button', { name: 'Adicionar critério' })).not.toBeInTheDocument()
  })

  it('mostra o erro de carregamento', async () => {
    listar.mockRejectedValue(erroHttp(403, 'Acesso negado para o seu perfil'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado para o seu perfil')
  })

  // T47: lacunas dos testes de T46
  it('com a lista vazia mostra os dois grupos sem itens e já deixa o formulário disponível', async () => {
    listar.mockResolvedValue([])
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    const inclusao = await screen.findByRole('region', { name: 'Critérios de inclusão' })
    expect(within(inclusao).getByText('Adicione ao menos um critério de inclusão para avançar.')).toBeInTheDocument()
    expect(within(inclusao).queryByRole('listitem')).not.toBeInTheDocument()
    const exclusao = screen.getByRole('region', { name: 'Critérios de exclusão' })
    expect(within(exclusao).getByText('Nenhum critério de exclusão cadastrado.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Adicionar critério' })).toBeInTheDocument()
  })

  it('com a lista vazia para o coordenador não pede para adicionar e não mostra o formulário', async () => {
    listar.mockResolvedValue([])
    renderizarAba(<AbaCriterios />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Nenhum critério de inclusão cadastrado.')).toBeInTheDocument()
    expect(screen.queryByText('Adicione ao menos um critério de inclusão para avançar.')).not.toBeInTheDocument()
    expect(screen.queryByRole('form')).not.toBeInTheDocument()
  })

  it('mostra uma mensagem de erro do servidor (500) ao criar e mantém o que foi digitado', async () => {
    criar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    await screen.findByText('Idade adulta')

    await preencher('Tosse', 'Tosse persistente')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.getByLabelText(/^Título/)).toHaveValue('Tosse')
    expect(screen.getByLabelText(/^Descrição/)).toHaveValue('Tosse persistente')
    expect(screen.getByRole('button', { name: 'Adicionar critério' })).toBeEnabled()
  })

  it('mostra o erro do servidor ao editar e fica no modo de edição', async () => {
    atualizar.mockRejectedValue(erroHttp(409, 'Já existe um critério de inclusão com essa descrição neste estudo', 'CRITERIO_DUPLICADO'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    const inclusao = await screen.findByRole('region', { name: 'Critérios de inclusão' })

    await userEvent.click(within(inclusao).getByRole('button', { name: 'Editar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe um critério de inclusão com essa descrição')
    expect(screen.getByRole('form', { name: 'Editar critério' })).toBeInTheDocument()
  })

  it('mostra o motivo quando o servidor recusa a exclusão (409) e fecha a confirmação', async () => {
    excluir.mockRejectedValue(erroHttp(409, 'Critérios e visitas só podem ser alterados com o estudo em planejamento', 'ESTUDO_FORA_DE_PLANEJAMENTO'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })
    const exclusao = await screen.findByRole('region', { name: 'Critérios de exclusão' })

    await userEvent.click(within(exclusao).getByRole('button', { name: 'Excluir' }))
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Excluir' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('só podem ser alterados com o estudo em planejamento')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByText('Gestante')).toBeInTheDocument()
  })

  it('mostra o erro de servidor (500) no carregamento e não exibe grupos de critérios', async () => {
    listar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.queryByRole('region', { name: 'Critérios de inclusão' })).not.toBeInTheDocument()
  })

  it('indica o carregamento enquanto a lista não chega', async () => {
    listar.mockReturnValue(new Promise(() => {}))
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByText('Carregando critérios…')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Critérios de inclusão' })).not.toBeInTheDocument()
  })
})
