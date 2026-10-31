import fs from 'node:fs/promises'
import path from 'node:path'
import { expect, request, test as setup, type APIRequestContext } from '@playwright/test'
import {
  credenciaisBootstrap,
  credenciaisDe,
  exigirVariaveis,
  perfisE2E,
  urlApi,
  urlBase,
  type PerfilE2E,
} from '../support/env'
import { caminhoSessao, CHAVE_TOKEN } from '../support/sessao'

type UsuarioApi = { id: string; email: string }

async function entrarPorApi(api: APIRequestContext, email: string, senha: string): Promise<string> {
  const resposta = await api.post('/api/auth/login', { data: { email, senha } })
  expect(resposta.ok(), `login por API de ${email} falhou (HTTP ${resposta.status()})`).toBeTruthy()
  return ((await resposta.json()) as { token: string }).token
}

// Cria o usuário de cada perfil ou, se já existe, redefine senha e situação: roda quantas vezes
// for preciso sem duplicar nem deixar a conta inativa ou com senha antiga
async function garantirUsuariosDeTeste(api: APIRequestContext, tokenAdmin: string) {
  const cabecalho = { Authorization: `Bearer ${tokenAdmin}` }
  const lista = await api.get('/api/usuarios', { headers: cabecalho })
  expect(lista.ok(), `não foi possível listar usuários (HTTP ${lista.status()})`).toBeTruthy()
  const existentes = (await lista.json()) as UsuarioApi[]

  for (const perfil of perfisE2E) {
    const { email, senha } = credenciaisDe(perfil)
    const nome = `E2E ${perfil.charAt(0)}${perfil.slice(1).toLowerCase()}`
    const existente = existentes.find((u) => u.email.toLowerCase() === email.toLowerCase())

    const resposta = existente
      ? await api.put(`/api/usuarios/${existente.id}`, {
          headers: cabecalho,
          data: { nome, email, senha, perfil, ativo: true },
        })
      : await api.post('/api/usuarios', { headers: cabecalho, data: { nome, email, senha, perfil } })
    expect(resposta.ok(), `não foi possível preparar o usuário ${email} (HTTP ${resposta.status()})`).toBeTruthy()
  }
}

// O front guarda o token no localStorage; o estado salvo o recoloca na origem da aplicação
async function salvarSessao(perfil: PerfilE2E, token: string) {
  const arquivo = caminhoSessao(perfil)
  await fs.mkdir(path.dirname(arquivo), { recursive: true })
  const estado = {
    cookies: [],
    origins: [{ origin: new URL(urlBase()).origin, localStorage: [{ name: CHAVE_TOKEN, value: token }] }],
  }
  await fs.writeFile(arquivo, JSON.stringify(estado, null, 2))
}

setup('prepara usuários de teste e sessão de cada perfil', async () => {
  exigirVariaveis()
  const api = await request.newContext({ baseURL: urlApi() })

  const bootstrap = credenciaisBootstrap()
  const tokenAdmin = await entrarPorApi(api, bootstrap.email, bootstrap.senha)
  await garantirUsuariosDeTeste(api, tokenAdmin)

  for (const perfil of perfisE2E) {
    const { email, senha } = credenciaisDe(perfil)
    await salvarSessao(perfil, await entrarPorApi(api, email, senha))
  }
  await api.dispose()
})
