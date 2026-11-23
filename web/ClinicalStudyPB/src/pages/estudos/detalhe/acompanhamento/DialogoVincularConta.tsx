import { useEffect, useId, useRef, useState, type FormEvent } from 'react'
import { converterErro } from '../../../../api/erros'
import { contaParticipanteApi } from '../../../../api/contaParticipante'
import type { ContaVinculadaResponse } from '../../../../api/tipos'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { mensagemDeVinculo } from './mensagensAcompanhamento'
import './acompanhamento.css'

type DialogoVincularContaProps = {
  participacaoId: string
  participanteNome: string
  aoFechar: () => void
  // Chamado com a conta vinculada, para a tela de origem recarregar e confirmar
  aoVincular: (conta: ContaVinculadaResponse) => void
}

export const MENSAGEM_SEM_CONTAS =
  'Não há contas de participante disponíveis. Peça ao administrador para cadastrar uma.'

// RF17 (T111): diálogo para ligar uma conta de perfil PARTICIPANTE ao participante do estudo. Lista só as contas que
// o servidor diz estarem disponíveis (ativas e ainda sem vínculo); as regras de verdade (um para um, conta ativa, perfil)
// são do backend, e o diálogo mostra a mensagem do código recusado sem fechar, para tentar outra conta.
export function DialogoVincularConta({ participacaoId, participanteNome, aoFechar, aoVincular }: DialogoVincularContaProps) {
  const idTitulo = useId()
  const primeiraOpcao = useRef<HTMLInputElement>(null)
  const { dados: contas, erro: erroDaLista, carregando, recarregar } = useCarregamento(
    contaParticipanteApi.listarDisponiveis,
  )
  const [escolhida, setEscolhida] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState<string | null>(null)

  useEffect(() => {
    primeiraOpcao.current?.focus()
  }, [contas])

  async function confirmar(evento: FormEvent) {
    evento.preventDefault()
    if (!escolhida || enviando) return
    setEnviando(true)
    setFalha(null)
    try {
      const vinculada = await contaParticipanteApi.vincular(participacaoId, escolhida)
      aoVincular(vinculada)
      aoFechar()
    } catch (erro) {
      const convertido = converterErro(erro)
      setFalha(mensagemDeVinculo(convertido.codigo, convertido.mensagem))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div
      className="dialogo-fundo"
      onKeyDown={(evento) => {
        if (evento.key === 'Escape' && !enviando) aoFechar()
      }}
    >
      <form
        className="dialogo dialogo--desfecho"
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        onSubmit={confirmar}
        noValidate
      >
        <div>
          <h2 id={idTitulo}>Vincular conta do participante</h2>
          <p className="desfecho__aviso">
            Escolha a conta de {participanteNome}. O vínculo é de uma conta para um participante e não pode ser desfeito
            nem trocado depois.
          </p>
        </div>

        {carregando && <p className="carregando">Carregando contas disponíveis…</p>}

        {!carregando && erroDaLista && (
          <div className="pilha">
            <p className="alerta alerta--erro" role="alert">
              {erroDaLista}
            </p>
            <button type="button" className="botao botao--secundario" onClick={recarregar}>
              Tentar de novo
            </button>
          </div>
        )}

        {!carregando && !erroDaLista && contas && contas.length === 0 && (
          <p className="alerta alerta--info" role="status">
            {MENSAGEM_SEM_CONTAS}
          </p>
        )}

        {!carregando && contas && contas.length > 0 && (
          <fieldset className="desfecho__opcoes">
            <legend>Contas disponíveis</legend>
            {contas.map((conta, indice) => (
              <label key={conta.usuarioId} className="desfecho__opcao">
                <input
                  ref={indice === 0 ? primeiraOpcao : undefined}
                  type="radio"
                  name="contaParticipante"
                  value={conta.usuarioId}
                  checked={escolhida === conta.usuarioId}
                  onChange={() => {
                    setEscolhida(conta.usuarioId)
                    setFalha(null)
                  }}
                />
                <span>
                  {conta.nome} <span className="texto-suave">({conta.email})</span>
                </span>
              </label>
            ))}
          </fieldset>
        )}

        {falha && (
          <p className="alerta alerta--erro" role="alert">
            {falha}
          </p>
        )}

        <div className="formulario__acoes">
          <button type="submit" className="botao" disabled={!escolhida || enviando}>
            {enviando ? 'Vinculando…' : 'Vincular'}
          </button>
          <button type="button" className="botao botao--secundario" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
