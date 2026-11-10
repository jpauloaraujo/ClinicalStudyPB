// T70: gera as capturas de tela do README (docs/imagens/) percorrendo a aplicação real no navegador, com os
// usuários fictícios de demonstração (perfil demo da T63) e um estudo novo montado pela API.
//
//   cd web/ClinicalStudyPB && npx playwright install chromium   (uma vez)
//   # backend em http://localhost:8081 com o perfil demo e web em http://localhost:5173 (npm run dev)
//   node scripts/capturar-telas.mjs
//
// Variáveis opcionais: WEB_URL, API_URL e PASTA_SAIDA (padrão ../../docs/imagens).
import { mkdirSync } from 'node:fs'
import { resolve } from 'node:path'
import { chromium } from '@playwright/test'

const WEB = (process.env.WEB_URL ?? 'http://localhost:5173').replace(/\/$/, '')
const API = (process.env.API_URL ?? 'http://localhost:8081').replace(/\/$/, '')
const SAIDA = resolve(process.env.PASTA_SAIDA ?? '../../docs/imagens')
const SENHA = 'senha-de-teste'
const SUFIXO = String(Date.now()).slice(-6)

mkdirSync(SAIDA, { recursive: true })

async function api(metodo, caminho, corpo, token) {
  const resposta = await fetch(API + caminho, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  })
  const texto = await resposta.text()
  const dados = texto ? JSON.parse(texto) : null
  if (!resposta.ok) throw new Error(`${metodo} ${caminho} -> ${resposta.status} ${texto}`)
  return dados
}

async function token(email) {
  return (await api('POST', '/api/auth/login', { email, senha: SENHA })).token
}

const browser = await chromium.launch()

async function novaSessao(email) {
  const contexto = await browser.newContext({ viewport: { width: 1280, height: 800 }, locale: 'pt-BR' })
  const pagina = await contexto.newPage()
  await pagina.goto(`${WEB}/login`)
  await pagina.getByLabel('E-mail').fill(email)
  await pagina.getByLabel('Senha', { exact: true }).fill(SENHA)
  await pagina.getByRole('button', { name: 'Entrar' }).click()
  await pagina.getByRole('button', { name: 'Sair' }).waitFor()
  return pagina
}

async function capturar(pagina, nome) {
  await pagina.waitForLoadState('networkidle')
  await pagina.screenshot({ path: `${SAIDA}/${nome}.png`, fullPage: true })
  console.log(`  ${nome}.png`)
}

// ---------------------------------------------------------------- dados pela API
const pesquisadorToken = await token('demo.pesquisador@clinicalstudy.test')
const coordenadorToken = await token('demo.coordenador@clinicalstudy.test')
const eu = await api('GET', '/api/auth/me', undefined, pesquisadorToken)

const estudo = await api('POST', '/api/estudos', {
  titulo: `Estudo de demonstração ${SUFIXO}`,
  objetivo: 'Demonstrar o ciclo de planejamento e recrutamento com dados fictícios.',
  responsavelId: eu.id, dataInicio: '2026-12-01', dataFim: '2027-06-01', limiteParticipantes: 1, status: 'PLANEJAMENTO',
}, pesquisadorToken)
const base = `/api/estudos/${estudo.id}`
await api('POST', `${base}/criterios`, {
  tipo: 'INCLUSAO', titulo: 'Idade adulta', descricao: 'Ter dezoito anos ou mais', peso: 1, determinante: true,
}, pesquisadorToken)
await api('POST', `${base}/criterios`, {
  tipo: 'EXCLUSAO', titulo: 'Gestante', descricao: 'Estar em gestação no momento da triagem',
}, pesquisadorToken)
const visita = await api('POST', `${base}/visitas`, { nome: 'Triagem', diaPrevisto: 0, tolerancia: 0 }, pesquisadorToken)
await api('POST', `/api/visitas/${visita.id}/acoes`, {
  nome: 'Aferir pressão arterial', descricao: 'Medida simulada', categoria: 'AVALIACAO_CLINICA',
}, pesquisadorToken)

// ---------------------------------------------------------------- administrador
console.log('Administrador')
let admin = await browser.newContext({ viewport: { width: 1280, height: 800 }, locale: 'pt-BR' })
let pagina = await admin.newPage()
await pagina.goto(`${WEB}/login`)
await capturar(pagina, '01-login')
await pagina.getByLabel('E-mail').fill('demo.admin@clinicalstudy.test')
await pagina.getByLabel('Senha', { exact: true }).fill('senha-incorreta')
await pagina.getByRole('button', { name: 'Entrar' }).click()
await pagina.getByRole('alert').waitFor()
await capturar(pagina, '02-login-erro')
await pagina.getByLabel('Senha', { exact: true }).fill(SENHA)
await pagina.getByRole('button', { name: 'Entrar' }).click()
await pagina.getByRole('button', { name: 'Sair' }).waitFor()
await pagina.goto(`${WEB}/usuarios`)
await pagina.getByRole('table').waitFor()
await capturar(pagina, '03-usuarios')
await admin.close()

// ---------------------------------------------------------------- pesquisador (planejamento)
console.log('Pesquisador')
pagina = await novaSessao('demo.pesquisador@clinicalstudy.test')
await pagina.goto(`${WEB}/estudos`)
await pagina.getByRole('table').waitFor()
await capturar(pagina, '04-estudos')
await pagina.goto(`${WEB}/estudos/${estudo.id}/resumo`)
await pagina.getByText('Planejamento').first().waitFor()
await capturar(pagina, '05-estudo-resumo')
await pagina.goto(`${WEB}/estudos/${estudo.id}/criterios`)
await pagina.getByText('Idade adulta').first().waitFor()
await capturar(pagina, '06-criterios')
await pagina.goto(`${WEB}/estudos/${estudo.id}/visitas`)
await pagina.getByText('Triagem').first().waitFor()
await capturar(pagina, '07-visitas')
await pagina.context().close()

await api('POST', `${base}/transicoes`, { destino: 'RECRUTAMENTO' }, pesquisadorToken)
const candidatos = {}
for (const [chave, nome, nascimento] of [
  ['a', 'Pessoa Alfa Demonstração', '1990-04-12'],
  ['b', 'Pessoa Beta Demonstração', '1985-07-30'],
  ['c', 'Pessoa Gama Demonstração', '1992-01-15'],
]) {
  candidatos[chave] = await api('POST', `${base}/candidatos`, { nomeFicticio: nome, dataNascimento: nascimento }, coordenadorToken)
}
const criterios = await api('GET', `${base}/criterios`, undefined, coordenadorToken)
const respostas = criterios.map((c) => ({ criterioId: c.id, aplica: c.tipo === 'INCLUSAO' }))
// B fica pronto (aprovado e consentido) para mostrar o bloqueio por falta de vagas depois que A entrar
await api('POST', `/api/candidatos/${candidatos.b.id}/avaliacoes`, { respostas }, coordenadorToken)
await api('POST', `/api/candidatos/${candidatos.b.id}/consentimento`, {}, coordenadorToken)

// ---------------------------------------------------------------- coordenador (recrutamento)
console.log('Coordenador')
pagina = await novaSessao('demo.coordenador@clinicalstudy.test')
await pagina.goto(`${WEB}/estudos/${estudo.id}/candidatos`)
await pagina.getByText('CAND-001').first().waitFor()
await capturar(pagina, '08-candidatos')

const ficha = (id) => `${WEB}/estudos/${estudo.id}/candidatos/${id}`
await pagina.goto(ficha(candidatos.a.id))
const avaliacao = pagina.getByRole('region', { name: 'Avaliação de elegibilidade', exact: true })
await avaliacao.waitFor()
await capturar(pagina, '09-ficha-avaliacao')

await avaliacao.getByRole('radiogroup', { name: 'Resposta para Idade adulta' }).getByLabel('Se aplica', { exact: true }).check()
await avaliacao.getByRole('radiogroup', { name: 'Resposta para Gestante' }).getByLabel('Não se aplica', { exact: true }).check()
await avaliacao.getByRole('button', { name: 'Salvar avaliação' }).click()
await avaliacao.getByRole('status').filter({ hasText: 'Candidato elegível' }).waitFor()
await capturar(pagina, '10-avaliacao-aprovada')

const consentimento = pagina.getByRole('region', { name: 'Consentimento', exact: true })
await consentimento.getByRole('checkbox', { name: /confirm/i }).waitFor()
await capturar(pagina, '11-consentimento-termo')
await consentimento.getByRole('checkbox', { name: /confirm/i }).check()
await consentimento.getByRole('button', { name: 'Registrar' }).click()
await consentimento.getByText(/Comprovante/i).waitFor()
await capturar(pagina, '12-consentimento-comprovante')

const inclusao = pagina.getByRole('region', { name: 'Inclusão', exact: true })
await inclusao.getByRole('button', { name: 'Incluir no estudo' }).waitFor()
await capturar(pagina, '13-inclusao-precondicoes')
await inclusao.getByRole('button', { name: 'Incluir no estudo' }).click()
await pagina.getByRole('dialog').waitFor()
await capturar(pagina, '14-inclusao-confirmacao')
await pagina.getByRole('dialog').getByRole('button', { name: 'Confirmar' }).click()
await inclusao.getByText(/incluído/i).first().waitFor()
await capturar(pagina, '15-inclusao-concluida')

await pagina.goto(`${WEB}/estudos/${estudo.id}/participantes`)
await pagina.getByText('1 de 1 vagas').waitFor()
await capturar(pagina, '16-participantes')

await pagina.goto(ficha(candidatos.b.id))
await pagina.getByRole('region', { name: 'Inclusão', exact: true }).getByText(/vagas esgotadas/i).first().waitFor()
await capturar(pagina, '17-inclusao-vagas-esgotadas')

await pagina.goto(`${WEB}/usuarios`)
await pagina.getByText(/acesso negado/i).first().waitFor()
await capturar(pagina, '18-acesso-negado')
await pagina.context().close()

await browser.close()
console.log(`\nCapturas salvas em ${SAIDA}`)
