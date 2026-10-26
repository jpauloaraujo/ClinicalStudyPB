import { useCallback, useState } from 'react'
import { Link } from 'react-router'
import { useUsuarioLogado } from '../../contexts/sessao'
import { useCarregamento } from '../../hooks/useCarregamento'
import { extrairErro } from '../../services/erros'
import { estudosApi } from '../../services/recursos'
import { rotulosStatus, type Estudo } from '../../types/estudo'

function formatarData(data: string | null): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

// Listagem de estudos (T29 / RF04). Pesquisador gerencia; coordenador só consulta.
export function EstudosPage() {
  const usuario = useUsuarioLogado()
  const podeGerenciar = usuario.perfil === 'PESQUISADOR'
  const carregar = useCallback(() => estudosApi.listar(), [])
  const { dados: estudos, erro, carregando, recarregar } = useCarregamento(carregar)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  async function excluir(estudo: Estudo) {
    if (!window.confirm(`Excluir o estudo "${estudo.titulo}"? Esta ação não pode ser desfeita.`)) {
      return
    }
    setErroAcao(null)
    try {
      await estudosApi.excluir(estudo.id)
      recarregar()
    } catch (falha) {
      setErroAcao(extrairErro(falha).mensagem)
    }
  }

  return (
    <>
      <div className="barra-titulo">
        <h1>Estudos clínicos</h1>
        {podeGerenciar && (
          <Link to="/estudos/novo" className="botao">
            Novo estudo
          </Link>
        )}
      </div>
      {!podeGerenciar && <p className="texto-suave">Seu perfil tem acesso apenas para consulta.</p>}

      {(erro || erroAcao) && (
        <p className="alerta alerta--erro" role="alert">
          {erro ?? erroAcao}
        </p>
      )}
      {carregando && <p className="carregando">Carregando estudos…</p>}

      {estudos && estudos.length === 0 && <p>Nenhum estudo cadastrado.</p>}
      {estudos && estudos.length > 0 && (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Título</th>
                <th>Responsável</th>
                <th>Período</th>
                <th>Vagas</th>
                <th>Status</th>
                {podeGerenciar && <th aria-label="Ações" />}
              </tr>
            </thead>
            <tbody>
              {estudos.map((estudo) => (
                <tr key={estudo.id}>
                  <td>
                    <strong>{estudo.titulo}</strong>
                    <div className="texto-suave tabela__detalhe">{estudo.objetivo}</div>
                  </td>
                  <td>{estudo.responsavel.nome}</td>
                  <td>
                    {formatarData(estudo.dataInicio)} a {formatarData(estudo.dataFim)}
                  </td>
                  <td>{estudo.limiteParticipantes}</td>
                  <td>
                    <span className="etiqueta">{rotulosStatus[estudo.status]}</span>
                  </td>
                  {podeGerenciar && (
                    <td className="tabela__acoes">
                      <Link to={`/estudos/${estudo.id}`} className="botao botao--secundario botao--pequeno">
                        Editar
                      </Link>
                      <button
                        type="button"
                        className="botao botao--perigo botao--pequeno"
                        onClick={() => excluir(estudo)}
                      >
                        Excluir
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
