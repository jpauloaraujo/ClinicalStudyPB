import { useCallback } from 'react'
import { Link, NavLink, Outlet, useParams } from 'react-router'
import { useUsuarioLogado } from '../../../contexts/sessao'
import { useCarregamento } from '../../../hooks/useCarregamento'
import { estudosApi } from '../../../services/recursos'
import { rotulosStatus } from '../../../types/estudo'
import { abasDoPerfil } from './abas'
import type { EstudoDetalheContexto } from './contexto'
import { IndicadorEstado } from './IndicadorEstado'
import './estudoDetalhe.css'

// Página de detalhe do estudo com abas (T39). As telas das próximas tasks
// (critérios, visitas, candidatos, participantes) entram como rotas aninhadas.
export function EstudoDetalhePage() {
  const { estudoId = '' } = useParams()
  const usuario = useUsuarioLogado()
  const carregar = useCallback(() => estudosApi.buscar(estudoId), [estudoId])
  const { dados: estudo, erro, statusErro, carregando, recarregar } = useCarregamento(carregar)

  if (carregando) {
    return <p className="carregando">Carregando estudo…</p>
  }

  if (!estudo) {
    // 404 para UUID inexistente e 400 para UUID inválido digitado na URL
    const naoEncontrado = statusErro === 404 || statusErro === 400
    return (
      <>
        <h1>{naoEncontrado ? 'Estudo não encontrado' : 'Não foi possível carregar o estudo'}</h1>
        <p className="alerta alerta--erro" role="alert">
          {naoEncontrado ? 'O estudo solicitado não existe ou foi excluído.' : erro}
        </p>
        <p>
          <Link to="/estudos" className="botao botao--secundario">
            Voltar para estudos
          </Link>
        </p>
      </>
    )
  }

  const contexto: EstudoDetalheContexto = { estudo, recarregar }

  return (
    <>
      <div className="estudo-detalhe__topo">
        <Link to="/estudos" className="botao botao--secundario botao--pequeno" aria-label="Voltar para a lista de estudos">
          ← Voltar
        </Link>
        {usuario.perfil === 'PESQUISADOR' && (
          <Link to={`/estudos/${estudo.id}/editar`} className="botao botao--secundario botao--pequeno">
            Editar dados
          </Link>
        )}
      </div>

      <header className="estudo-detalhe__cabecalho">
        <h1>{estudo.titulo}</h1>
        <p className="estudo-detalhe__estado">
          Estado atual: <span className="etiqueta">{rotulosStatus[estudo.status]}</span>
        </p>
      </header>

      <IndicadorEstado estudo={estudo} perfil={usuario.perfil} aoAvancar={recarregar} />

      <nav className="abas" aria-label="Seções do estudo">
        <ul>
          {abasDoPerfil(usuario.perfil).map((aba) => (
            <li key={aba.caminho}>
              <NavLink to={aba.caminho} className="abas__link">
                {aba.rotulo}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <section className="estudo-detalhe__conteudo">
        <Outlet context={contexto} />
      </section>
    </>
  )
}
