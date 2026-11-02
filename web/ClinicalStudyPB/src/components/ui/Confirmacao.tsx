import { useEffect, useId, useRef } from 'react'

type ConfirmacaoProps = {
  titulo: string
  mensagem: string
  rotuloConfirmar: string
  perigosa?: boolean
  processando?: boolean
  onConfirmar: () => void
  onCancelar: () => void
}

// Diálogo de confirmação acessível (role="dialog"), no lugar do window.confirm, para poder ser
// estilizado e testado. Esc cancela e o foco começa no botão de cancelar (a ação segura).
export function Confirmacao({
  titulo,
  mensagem,
  rotuloConfirmar,
  perigosa = false,
  processando = false,
  onConfirmar,
  onCancelar,
}: ConfirmacaoProps) {
  const idTitulo = useId()
  const idMensagem = useId()
  const botaoCancelar = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    botaoCancelar.current?.focus()
  }, [])

  return (
    <div
      className="dialogo-fundo"
      onKeyDown={(evento) => {
        if (evento.key === 'Escape' && !processando) onCancelar()
      }}
    >
      <div
        className="dialogo"
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        aria-describedby={idMensagem}
      >
        <h2 id={idTitulo}>{titulo}</h2>
        <p id={idMensagem}>{mensagem}</p>
        <div className="formulario__acoes">
          <button
            type="button"
            className={perigosa ? 'botao botao--perigo' : 'botao'}
            onClick={onConfirmar}
            disabled={processando}
          >
            {processando ? 'Aguarde…' : rotuloConfirmar}
          </button>
          <button
            type="button"
            className="botao botao--secundario"
            ref={botaoCancelar}
            onClick={onCancelar}
            disabled={processando}
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  )
}
