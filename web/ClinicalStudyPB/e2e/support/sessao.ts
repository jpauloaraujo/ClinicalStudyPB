import path from 'node:path'
import type { PerfilE2E } from './env'

// Mesma chave que src/services/api.ts usa para guardar o token no localStorage
export const CHAVE_TOKEN = 'clinicalstudypb.token'

// Arquivo com o estado de autenticação de cada perfil (ignorado pelo Git)
export const caminhoSessao = (perfil: PerfilE2E) =>
  path.join(import.meta.dirname, '..', '.auth', `${perfil.toLowerCase()}.json`)
