import { useCallback, useState, type FormEvent } from 'react'
import { avaliacoesApi } from '../../../../api/avaliacoes'
import { criteriosApi } from '../../../../api/criterios'
import { converterErro } from '../../../../api/erros'
import type { AvaliacaoResponse, CandidatoResponse, CriterioResponse } from '../../../../api/tipos'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import type { Estudo } from '../../../../types/estudo'
import '../recrutamento.css'

type SecaoAvaliacaoProps = {
  estudo: Estudo
  candidato: CandidatoResponse
  // Chamado depois de salvar, para a ficha recarregar a situação do candidato
  aoAvaliar: () => void
}

type Resposta = { aplica: boolean | null; observacao: string }

function formatarDataHora(valor: string): string {
  const data = new Date(valor)
  return Number.isNaN(data.getTime()) ? valor : data.toLocaleString('pt-BR')
}

function ResultadoDaAvaliacao({ avaliacao }: { avaliacao: AvaliacaoResponse }) {
  const aprovado = avaliacao.resultado === 'APROVADO'
  return (
    <div className={aprovado ? 'resultado resultado--aprovado' : 'resultado resultado--reprovado'} role="status">
      <div>{aprovado ? 'Candidato elegível' : 'Candidato não elegível'}</div>
      <div className="texto-suave">Pontuação: {avaliacao.pontuacao}</div>
      {!aprovado && avaliacao.criteriosReprovadores.length > 0 && (
        <>
          <div>Critérios que levaram à inelegibilidade:</div>
          <ul>
            {avaliacao.criteriosReprovadores.map((criterio) => (
              <li key={criterio.criterioId}>
                {criterio.titulo} ({criterio.tipo === 'INCLUSAO' ? 'inclusão não atendida' : 'exclusão aplicada'})
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}

function GrupoDeCriterios({
  titulo,
  criterios,
  respostas,
  desabilitado,
  aoResponder,
  aoObservar,
  explicacao,
}: {
  titulo: string
  criterios: CriterioResponse[]
  respostas: Record<string, Resposta>
  desabilitado: boolean
  aoResponder: (id: string, aplica: boolean) => void
  aoObservar: (id: string, observacao: string) => void
  explicacao?: string
}) {
  return (
    <section className="pilha" aria-label={titulo}>
      <h4>{titulo}</h4>
      {explicacao && <p className="texto-suave">{explicacao}</p>}
      {criterios.length === 0 && <p className="texto-suave">Nenhum critério neste grupo.</p>}
      {criterios.map((criterio) => {
        const resposta = respostas[criterio.id]
        return (
          <fieldset key={criterio.id} className="criterio-avaliacao" disabled={desabilitado}>
            <legend>{criterio.titulo}</legend>
            <p className="item__descricao">{criterio.descricao}</p>
            <div className="opcoes" role="radiogroup" aria-label={`Resposta para ${criterio.titulo}`}>
              <label>
                <input
                  type="radio"
                  name={`criterio-${criterio.id}`}
                  checked={resposta?.aplica === true}
                  onChange={() => aoResponder(criterio.id, true)}
                />
                Se aplica
              </label>
              <label>
                <input
                  type="radio"
                  name={`criterio-${criterio.id}`}
                  checked={resposta?.aplica === false}
                  onChange={() => aoResponder(criterio.id, false)}
                />
                Não se aplica
              </label>
            </div>
            <label className="campo">
              <span>Observação ({criterio.titulo})</span>
              <input value={resposta?.observacao ?? ''} maxLength={500} onChange={(e) => aoObservar(criterio.id, e.target.value)} />
            </label>
          </fieldset>
        )
      })}
    </section>
  )
}

// RF09 (T55): avaliação de elegibilidade dentro da ficha do candidato. O coordenador responde a todos os
// critérios do estudo; o backend calcula o resultado, que aparece em destaque junto do histórico.
export function SecaoAvaliacao({ estudo, candidato, aoAvaliar }: SecaoAvaliacaoProps) {
  const carregarCriterios = useCallback(() => criteriosApi.listar(estudo.id), [estudo.id])
  const carregarHistorico = useCallback(() => avaliacoesApi.historico(candidato.id), [candidato.id])
  const criteriosCarga = useCarregamento(carregarCriterios)
  const historicoCarga = useCarregamento(carregarHistorico)

  const [respostas, setRespostas] = useState<Record<string, Resposta>>({})
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const criterios = criteriosCarga.dados ?? []
  const inclusao = criterios.filter((c) => c.tipo === 'INCLUSAO')
  const exclusao = criterios.filter((c) => c.tipo === 'EXCLUSAO')
  const historico = historicoCarga.dados ?? []
  const vigente = historico.find((a) => a.vigente)

  const bloqueio =
    estudo.status !== 'RECRUTAMENTO'
      ? 'A avaliação só pode ser registrada com o estudo em recrutamento.'
      : candidato.situacao === 'INCLUIDO'
        ? 'O candidato já foi incluído no estudo e não pode ser reavaliado.'
        : null
  const semCriterios = criteriosCarga.dados !== null && criterios.length === 0
  const todosRespondidos = criterios.length > 0 && criterios.every((c) => respostas[c.id]?.aplica != null)

  function responder(id: string, aplica: boolean) {
    setRespostas((atual) => ({ ...atual, [id]: { aplica, observacao: atual[id]?.observacao ?? '' } }))
  }

  function observar(id: string, observacao: string) {
    setRespostas((atual) => ({ ...atual, [id]: { aplica: atual[id]?.aplica ?? null, observacao } }))
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    if (!todosRespondidos) return
    setEnviando(true)
    setErro(null)
    try {
      await avaliacoesApi.avaliar(candidato.id, {
        respostas: criterios.map((c) => ({
          criterioId: c.id,
          aplica: respostas[c.id].aplica as boolean,
          observacao: respostas[c.id].observacao.trim() || undefined,
        })),
      })
      setRespostas({})
      historicoCarga.recarregar()
      aoAvaliar()
    } catch (falha) {
      // 400 (resposta faltando ou sobrando) e 409 (estudo fora de recrutamento, candidato incluído)
      setErro(converterErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="secao pilha" aria-label="Avaliação de elegibilidade">
      <h3>Avaliação de elegibilidade</h3>

      {vigente && (
        <div className="pilha">
          <ResultadoDaAvaliacao avaliacao={vigente} />
        </div>
      )}
      {!vigente && historicoCarga.dados && <p className="texto-suave">Este candidato ainda não foi avaliado.</p>}

      {(criteriosCarga.erro || historicoCarga.erro) && (
        <p className="alerta alerta--erro" role="alert">
          {criteriosCarga.erro ?? historicoCarga.erro}
        </p>
      )}
      {bloqueio && (
        <p className="alerta alerta--info" role="status">
          {bloqueio}
        </p>
      )}
      {semCriterios && <p className="alerta alerta--info" role="status">Este estudo ainda não tem critérios para avaliar.</p>}

      {criteriosCarga.dados && criterios.length > 0 && (
        <form className="pilha" onSubmit={salvar} noValidate aria-label="Nova avaliação">
          <h4>{vigente ? 'Nova avaliação (reavaliação)' : 'Registrar avaliação'}</h4>
          <GrupoDeCriterios
            titulo="Critérios de inclusão"
            criterios={inclusao}
            respostas={respostas}
            desabilitado={bloqueio !== null || enviando}
            aoResponder={responder}
            aoObservar={observar}
          />
          <GrupoDeCriterios
            titulo="Critérios de exclusão"
            criterios={exclusao}
            respostas={respostas}
            desabilitado={bloqueio !== null || enviando}
            aoResponder={responder}
            aoObservar={observar}
            explicacao="Atenção: nos critérios de exclusão, “se aplica” reprova o candidato."
          />
          {erro && (
            <p className="alerta alerta--erro" role="alert">
              {erro}
            </p>
          )}
          <div className="formulario__acoes">
            <button type="submit" className="botao" disabled={!todosRespondidos || bloqueio !== null || enviando}>
              {enviando ? 'Salvando…' : 'Salvar avaliação'}
            </button>
            {!todosRespondidos && bloqueio === null && (
              <span className="texto-suave">Responda a todos os critérios para salvar.</span>
            )}
          </div>
        </form>
      )}

      {historico.length > 0 && (
        <div className="pilha">
          <h4>Histórico de avaliações</h4>
          <ul className="itens" aria-label="Histórico de avaliações">
            {historico.map((avaliacao) => (
              <li key={avaliacao.id} className="item">
                <div className="item__texto">
                  <div className="item__titulo">
                    {formatarDataHora(avaliacao.dataAvaliacao)}
                    <span className={avaliacao.resultado === 'APROVADO' ? 'etiqueta etiqueta--ok' : 'etiqueta etiqueta--perigo'}>
                      {avaliacao.resultado === 'APROVADO' ? 'Aprovado' : 'Reprovado'}
                    </span>
                    {avaliacao.vigente && <span className="etiqueta">Vigente</span>}
                  </div>
                  <p className="item__descricao">
                    Avaliador: {avaliacao.avaliador} · Pontuação: {avaliacao.pontuacao}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
