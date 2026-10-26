import { useCallback, useState } from 'react'
import { Link } from 'react-router'
import { useUsuarioLogado } from '../../contexts/sessao'
import { useCarregamento } from '../../hooks/useCarregamento'
import { extrairErro } from '../../services/erros'
import { usuariosApi } from '../../services/recursos'
import { rotulosPerfil, type Usuario } from '../../types/usuario'

// Listagem de usuários (T17 / RF01) — apenas administrador
export function UsuariosPage() {
  const logado = useUsuarioLogado()
  const carregar = useCallback(() => usuariosApi.listar(), [])
  const { dados: usuarios, erro, carregando, recarregar } = useCarregamento(carregar)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  async function excluir(usuario: Usuario) {
    if (!window.confirm(`Excluir o usuário "${usuario.nome}"? Esta ação não pode ser desfeita.`)) {
      return
    }
    setErroAcao(null)
    try {
      await usuariosApi.excluir(usuario.id)
      recarregar()
    } catch (falha) {
      setErroAcao(extrairErro(falha).mensagem)
    }
  }

  return (
    <>
      <div className="barra-titulo">
        <h1>Usuários</h1>
        <Link to="/usuarios/novo" className="botao">
          Novo usuário
        </Link>
      </div>

      {(erro || erroAcao) && (
        <p className="alerta alerta--erro" role="alert">
          {erro ?? erroAcao}
        </p>
      )}
      {carregando && <p className="carregando">Carregando usuários…</p>}

      {usuarios && usuarios.length === 0 && <p>Nenhum usuário cadastrado.</p>}
      {usuarios && usuarios.length > 0 && (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Perfil</th>
                <th>Situação</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {usuarios.map((usuario) => (
                <tr key={usuario.id}>
                  <td>{usuario.nome}</td>
                  <td>{usuario.email}</td>
                  <td>{rotulosPerfil[usuario.perfil]}</td>
                  <td>
                    <span className={usuario.ativo ? 'etiqueta etiqueta--ok' : 'etiqueta'}>
                      {usuario.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td className="tabela__acoes">
                    <Link to={`/usuarios/${usuario.id}`} className="botao botao--secundario botao--pequeno">
                      Editar
                    </Link>
                    <button
                      type="button"
                      className="botao botao--perigo botao--pequeno"
                      onClick={() => excluir(usuario)}
                      disabled={usuario.id === logado.id}
                      title={usuario.id === logado.id ? 'Você não pode excluir a própria conta' : undefined}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
