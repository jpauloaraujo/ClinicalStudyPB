import { useState } from 'react'
import { converterErro } from '../../../api/erros'
import { estadoEstudoApi } from '../../../api/estadoEstudo'
import { Confirmacao } from '../../../components/ui/Confirmacao'
import { rotulosStatus, statusEstudo, type Estudo } from '../../../types/estudo'
import type { PerfilUsuario } from '../../../types/usuario'
import './recrutamento.css'

type IndicadorEstadoProps = {
  estudo: Estudo
  perfil: PerfilUsuario
  // Chamado depois que o estado avançou, para a página recarregar o estudo
  aoAvancar: () => void
}

// RF07 (T43): mostra os quatro estados, destaca o atual e, só para o pesquisador, oferece o botão que
// avança para o próximo (com confirmação, pois não há volta). Erros de regra aparecem em um alerta.
export function IndicadorEstado({ estudo, perfil, aoAvancar }: IndicadorEstadoProps) {
  const indiceAtual = statusEstudo.indexOf(estudo.status)
  const proximo = statusEstudo[indiceAtual + 1]
  const podeAvancar = perfil === 'PESQUISADOR' && proximo !== undefined

  const [confirmando, setConfirmando] = useState(false)
  const [processando, setProcessando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function avancar() {
    setProcessando(true)
    setErro(null)
    try {
      await estadoEstudoApi.avancar(estudo.id, proximo)
      setConfirmando(false)
      aoAvancar()
    } catch (falha) {
      // O 409 traz a regra violada (ex.: falta um critério de inclusão) e deve ser lida pelo usuário
      setConfirmando(false)
      setErro(converterErro(falha).mensagem)
    } finally {
      setProcessando(false)
    }
  }

  return (
    <div>
      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}

      <ol className="estados" aria-label="Etapas do estudo">
        {statusEstudo.map((status, indice) => (
          <li
            key={status}
            className={
              indice === indiceAtual
                ? 'estados__item estados__item--atual'
                : indice < indiceAtual
                  ? 'estados__item estados__item--feito'
                  : 'estados__item'
            }
            aria-current={indice === indiceAtual ? 'step' : undefined}
          >
            {rotulosStatus[status]}
          </li>
        ))}
      </ol>

      {podeAvancar && (
        <div className="estados-acoes">
          <button type="button" className="botao" onClick={() => setConfirmando(true)}>
            Avançar para {rotulosStatus[proximo]}
          </button>
        </div>
      )}

      {confirmando && (
        <Confirmacao
          titulo={`Avançar para ${rotulosStatus[proximo]}?`}
          mensagem="Não é possível voltar ao estado anterior depois de avançar. Confirma a mudança de estado do estudo?"
          rotuloConfirmar="Confirmar"
          processando={processando}
          onConfirmar={avancar}
          onCancelar={() => setConfirmando(false)}
        />
      )}
    </div>
  )
}
