import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Campo } from '../../../../components/ui/Campo'
import type { JanelaDaVisita } from './mensagensAcompanhamento'

type FormularioDataHoraProps = {
  // Nome da região (aria-label), por exemplo "Agendar Triagem"
  titulo: string
  // Valores iniciais: a sugestão (agendar) ou a data atual do agendamento (reagendar)
  dataInicial: string
  horaInicial: string
  // Quando conhecida, limita o campo de data à janela válida (o servidor continua validando)
  janela?: JanelaDaVisita
  rotuloSalvar: string
  enviando: boolean
  // Erro de uma regra que não pertence a um campo (estudo fora de estado, visita já agendada…)
  erroGeral?: string | null
  // Erro que pertence à data (fora da janela, no passado ou erro por campo do servidor)
  erroData?: string | null
  // Recebe a data e hora locais, sem fuso (aaaa-mm-ddTHH:mm), como o contrato pede
  onSalvar: (dataHora: string) => void
  onCancelar: () => void
}

// Formulário de data e hora usado para agendar e para reagendar (RQ-T77-06 e 07). Valida só o obrigatório;
// as regras de negócio (janela, passado, estado do estudo) são do servidor, e o erro volta para esta tela.
export function FormularioDataHora({
  titulo,
  dataInicial,
  horaInicial,
  janela,
  rotuloSalvar,
  enviando,
  erroGeral,
  erroData,
  onSalvar,
  onCancelar,
}: FormularioDataHoraProps) {
  const [data, setData] = useState(dataInicial)
  const [hora, setHora] = useState(horaInicial)
  const [erroLocal, setErroLocal] = useState<{ data?: string; hora?: string }>({})
  const campoData = useRef<HTMLInputElement>(null)
  const campoHora = useRef<HTMLInputElement>(null)

  // Foco no primeiro campo com erro vindo do servidor (RQ-T77-19)
  useEffect(() => {
    if (erroData) campoData.current?.focus()
  }, [erroData])

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados: { data?: string; hora?: string } = {}
    if (!data) encontrados.data = 'A data é obrigatória'
    if (!hora) encontrados.hora = 'A hora é obrigatória'
    setErroLocal(encontrados)
    if (encontrados.data) {
      campoData.current?.focus()
      return
    }
    if (encontrados.hora) {
      campoHora.current?.focus()
      return
    }
    onSalvar(`${data}T${hora}`)
  }

  return (
    <form className="formulario formulario--agendamento" aria-label={titulo} onSubmit={enviar} noValidate>
      <div className="formulario__linha">
        <Campo rotulo="Data" erro={erroLocal.data ?? erroData ?? undefined}>
          <input
            ref={campoData}
            type="date"
            value={data}
            min={janela?.inicio}
            max={janela?.fim}
            onChange={(e) => {
              setData(e.target.value)
              setErroLocal((atual) => ({ ...atual, data: undefined }))
            }}
          />
        </Campo>
        <Campo rotulo="Hora" erro={erroLocal.hora}>
          <input
            ref={campoHora}
            type="time"
            value={hora}
            onChange={(e) => {
              setHora(e.target.value)
              setErroLocal((atual) => ({ ...atual, hora: undefined }))
            }}
          />
        </Campo>
      </div>
      {erroGeral && (
        <p className="alerta alerta--erro" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="formulario__acoes">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Salvando…' : rotuloSalvar}
        </button>
        <button type="button" className="botao botao--secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
