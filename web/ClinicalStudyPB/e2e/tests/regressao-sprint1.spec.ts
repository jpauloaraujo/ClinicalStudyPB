import { capturar } from '../support/capturas'
import { sufixoUnico } from '../support/unico'
import { comoPerfil, expect, test } from '../fixtures'

// Regressão mínima da Sprint 1 (RQ-09 e RQ-10 da T66): cadastro de usuário (RF01) e de estudo (RF04).
// Login válido, inválido e logout (RF02) já são cobertos por login.spec.ts e sessao.spec.ts (T65).

test.describe('regressão RF01: cadastro de usuário', () => {
  test.use(comoPerfil('ADMINISTRADOR'))

  test('o administrador cadastra um usuário com nome e e-mail únicos e o vê na listagem', async ({ page, paginaUsuarios }) => {
    const sufixo = sufixoUnico()
    const nome = `Usuário E2E ${sufixo}`
    const email = `e2e.usuario.${sufixo}@clinicalstudy.test`

    await paginaUsuarios.goto()
    await paginaUsuarios.cadastrar({ nome, email, senha: 'senha-e2e-usuario', perfil: 'Coordenador' })

    await paginaUsuarios.expectNaListagem(nome, email)
    await capturar(page, 'regressao-usuario-cadastrado')
  })
})

test.describe('regressão RF04: cadastro de estudo', () => {
  test.use(comoPerfil('PESQUISADOR'))

  test('o pesquisador cadastra um estudo com todos os campos e o vê na lista e no detalhe', async ({
    page,
    paginaEstudos,
    paginaEstudo,
  }) => {
    const titulo = `Estudo E2E ${sufixoUnico()}`

    await paginaEstudos.cadastrar({
      titulo,
      objetivo: 'Avaliar um protocolo fictício de ponta a ponta.',
      dataInicio: '2026-11-10',
      dataFim: '2027-05-10',
      limite: 20,
    })
    await paginaEstudos.abrir(titulo)

    await paginaEstudo.expectEstado('Planejamento')
    await expect(page.getByText('Avaliar um protocolo fictício de ponta a ponta.')).toBeVisible()
    await capturar(page, 'regressao-estudo-cadastrado')
  })

  test('campos obrigatórios vazios mostram o erro de validação e nada é cadastrado', async ({ page, paginaEstudos }) => {
    await paginaEstudos.goto()
    await paginaEstudos.abrirFormularioNovo()

    await paginaEstudos.salvar()

    await paginaEstudos.expectErroDeCampo('O título é obrigatório')
    await paginaEstudos.expectErroDeCampo('O objetivo é obrigatório')
    await expect(page).toHaveURL(/\/estudos\/novo$/)
  })

  test('data final anterior à inicial é recusada', async ({ paginaEstudos }) => {
    await paginaEstudos.goto()
    await paginaEstudos.abrirFormularioNovo()
    await paginaEstudos.preencher({
      titulo: `Estudo datas ${sufixoUnico()}`,
      objetivo: 'Objetivo fictício.',
      dataInicio: '2026-12-10',
      dataFim: '2026-12-01',
      limite: 5,
    })

    await paginaEstudos.salvar()

    await paginaEstudos.expectErroDeCampo('A data final não pode ser anterior à data inicial')
  })
})
