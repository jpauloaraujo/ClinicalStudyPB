import { useCallback, useState } from 'react'
import { converterErro } from '../../../../api/erros'
import { participantesApi } from '../../../../api/participantes'
import type { CandidatoResponse } from '../../../../api/tipos'
import { Confirmacao } from '../../../../components/ui/Confirmacao'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import type { Estudo } from '../../../../types/estudo'
import { mensagemDeInclusao } from '../mensagensInclusao'
import '../recrutamento.css'

type SecaoInclusaoProps = {
  estudo: Estudo
  candidato: CandidatoResponse
  // Chamado depois de incluir, para a ficha recarregar a situação do candidato
  aoIncluir: () => void
}

type PreCondicao = { rotulo: string; atendida: boolean; pendencia: string }

// RF11 (T61): inclusão do candidato como participante. A tela espelha as quatro pré-condições (estudo em
// recrutamento, candidato elegível, consentimento e vaga), mas quem decide é o backend, que pode recusar mesmo
// com o botão habilitado (por exemplo, se outra pessoa ocupou a última vaga).
export function SecaoInclusao({ estudo, candidato, aoIncluir }: SecaoInclusaoProps) {
  const usuario = useUsuarioLogado()
  const carregarVagas = useCallback(() => participantesApi.vagas(estudo.id), [estudo.id])
  const vagasCarga = useCarregamento(carregarVagas)

  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const incluido = candidato.situacao === 'INCLUIDO'
  const vagas = vagasCarga.dados
  const preCondicoes: PreCondicao[] = [
    {
      rotulo: 'Estudo em recrutamento',
      atendida: estudo.status === 'RECRUTAMENTO',
      pendencia: 'O estudo não está em recrutamento.',
    },
    {
      rotulo: 'Candidato elegível',
      atendida: candidato.resultadoAvaliacao === 'APROVADO',
      pendencia: 'Falta uma avaliação de elegibilidade aprovada.',
    },
    {
      rotulo: 'Consentimento registrado',
      atendida: candidato.consentimentoRegistrado,
      pendencia: 'Falta registrar o consentimento do candidato.',
    },
    {
      rotulo: 'Vaga disponível',
      atendida: vagas !== null && vagas.restantes > 0,
      pendencia: vagas === null ? 'Verificando as vagas…' : 'Vagas esgotadas.',
    },
  ]
  const todasAtendidas = preCondicoes.every((p) => p.atendida)
  const podeIncluir = usuario.perfil === 'COORDENADOR' && todasAtendidas && !incluido && !enviando

  async function incluir() {
    setEnviando(true)
    setErro(null)
    try {
      await participantesApi.incluir(estudo.id, candidato.id)
      setConfirmando(false)
      vagasCarga.recarregar()
      aoIncluir()
    } catch (falha) {
      const convertido = converterErro(falha)
      setConfirmando(false)
      setErro(mensagemDeInclusao(convertido.codigo, convertido.mensagem))
      vagasCarga.recarregar()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="secao pilha" aria-label="Inclusão">
      <h3>Inclusão no estudo</h3>

      {incluido ? (
        <p className="alerta alerta--ok" role="status">
          Candidato incluído como participante do estudo.
        </p>
      ) : (
        <>
          <ul className="itens" aria-label="Pré-condições da inclusão">
            {preCondicoes.map((condicao) => (
              <li key={condicao.rotulo} className="item">
                <div className="item__texto">
                  <div className="item__titulo">
                    {condicao.rotulo}
                    <span className={condicao.atendida ? 'etiqueta etiqueta--ok' : 'etiqueta'}>
                      {condicao.atendida ? 'Atendida' : 'Pendente'}
                    </span>
                  </div>
                  {!condicao.atendida && <p className="item__descricao">{condicao.pendencia}</p>}
                </div>
              </li>
            ))}
          </ul>

          {vagas && (
            <p className="texto-suave">
              {vagas.incluidos} de {vagas.limite} vagas ocupadas · {vagas.restantes}{' '}
              {vagas.restantes === 1 ? 'vaga restante' : 'vagas restantes'}
            </p>
          )}
          {vagasCarga.erro && (
            <p className="alerta alerta--erro" role="alert">
              {vagasCarga.erro}
            </p>
          )}
          {usuario.perfil !== 'COORDENADOR' && (
            <p className="alerta alerta--info" role="status">
              Seu perfil tem acesso apenas para consulta da inclusão.
            </p>
          )}
          {erro && (
            <p className="alerta alerta--erro" role="alert">
              {erro}
            </p>
          )}
          {usuario.perfil === 'COORDENADOR' && (
            <div className="formulario__acoes">
              <button type="button" className="botao" disabled={!podeIncluir} onClick={() => setConfirmando(true)}>
                Incluir no estudo
              </button>
            </div>
          )}
        </>
      )}

      {confirmando && (
        <Confirmacao
          titulo="Incluir candidato no estudo?"
          mensagem={`${candidato.codigo} será incluído(a) como participante do estudo e passará a ocupar uma vaga. Confirma a inclusão?`}
          rotuloConfirmar="Confirmar"
          processando={enviando}
          onConfirmar={incluir}
          onCancelar={() => setConfirmando(false)}
        />
      )}
    </section>
  )
}
