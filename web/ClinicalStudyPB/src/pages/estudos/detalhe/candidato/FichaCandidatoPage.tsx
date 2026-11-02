import { useCallback } from 'react'
import { Link, useParams } from 'react-router'
import { candidatosApi } from '../../../../api/candidatos'
import { rotulosSituacao } from '../../../../api/tipos'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'
import { SecaoAvaliacao } from './SecaoAvaliacao'
import { SecaoConsentimento } from './SecaoConsentimento'
import { SecaoInclusao } from './SecaoInclusao'

// Ficha do candidato (T52): dados do candidato e uma seção para cada etapa do funil. Cada seção é um
// componente próprio (avaliação T55, consentimento T58, inclusão T61) para as tasks não conflitarem.
export function FichaCandidatoPage() {
  const { estudo } = useEstudoDetalhe()
  const { candidatoId = '' } = useParams()
  const carregar = useCallback(() => candidatosApi.buscar(candidatoId), [candidatoId])
  const { dados: candidato, erro, statusErro, carregando, recarregar } = useCarregamento(carregar)

  if (carregando) {
    return <p className="carregando">Carregando candidato…</p>
  }

  if (!candidato || candidato.estudoId !== estudo.id) {
    const naoEncontrado = statusErro === 404 || statusErro === 400 || (candidato && candidato.estudoId !== estudo.id)
    return (
      <div className="pilha">
        <h2>{naoEncontrado ? 'Candidato não encontrado' : 'Não foi possível carregar o candidato'}</h2>
        <p className="alerta alerta--erro" role="alert">
          {naoEncontrado ? 'O candidato solicitado não existe neste estudo.' : erro}
        </p>
        <p>
          <Link to="../candidatos" relative="path" className="botao botao--secundario">
            Voltar para candidatos
          </Link>
        </p>
      </div>
    )
  }

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <h2>
          {candidato.codigo} · {candidato.nomeFicticio}
        </h2>
        <Link to="../candidatos" relative="path" className="botao botao--secundario botao--pequeno">
          ← Candidatos
        </Link>
      </div>

      <section className="secao" aria-label="Dados do candidato">
        <h3>Dados do candidato</h3>
        <dl className="resumo-estudo">
          <dt>Situação</dt>
          <dd>
            <span className="etiqueta">{rotulosSituacao[candidato.situacao]}</span>
          </dd>
          <dt>Data de nascimento</dt>
          <dd>{formatarData(candidato.dataNascimento)}</dd>
          <dt>Contato</dt>
          <dd>{candidato.contato ?? '—'}</dd>
        </dl>
      </section>

      <SecaoAvaliacao estudo={estudo} candidato={candidato} aoAvaliar={recarregar} />
      <SecaoConsentimento candidato={candidato} />
      <SecaoInclusao candidato={candidato} />
    </div>
  )
}
