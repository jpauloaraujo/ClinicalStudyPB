import { useCallback, useState, type FormEvent } from 'react'
import { consentimentoApi } from '../../../../api/consentimento'
import { converterErro } from '../../../../api/erros'
import type { CandidatoResponse } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

type SecaoConsentimentoProps = {
  candidato: CandidatoResponse
  // Chamado depois de registrar, para a ficha recarregar a situação do candidato
  aoRegistrar: () => void
}

export const VERSAO_PADRAO_DO_TERMO = 'TCLE-SIMULADO v1'

// Valor de um <input type="datetime-local"> para o instante atual (horário local, sem fuso)
function agoraParaCampo(): string {
  const agora = new Date()
  agora.setMinutes(agora.getMinutes() - agora.getTimezoneOffset())
  return agora.toISOString().slice(0, 16)
}

function formatarDataHora(valor: string): string {
  const data = new Date(valor)
  return Number.isNaN(data.getTime()) ? valor : data.toLocaleString('pt-BR')
}

// RF10 (T58): consentimento livre e esclarecido simulado, dentro da ficha do candidato. O coordenador lê o
// termo, confirma e registra; depois só o comprovante (data, responsável e versão do termo) é exibido.
export function SecaoConsentimento({ candidato, aoRegistrar }: SecaoConsentimentoProps) {
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const carregar = useCallback(() => consentimentoApi.buscar(candidato.id), [candidato.id])
  const { dados: comprovante, erro, statusErro, carregando, recarregar } = useCarregamento(carregar)

  const [confirmado, setConfirmado] = useState(false)
  const [dataHora, setDataHora] = useState(agoraParaCampo)
  const [dataAlterada, setDataAlterada] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erroRegistro, setErroRegistro] = useState<string | null>(null)

  // 404 significa apenas "ainda não registrado"; qualquer outro erro é falha de carregamento
  const naoRegistrado = !comprovante && statusErro === 404
  const falhaDeCarga = erro !== null && statusErro !== 404

  const bloqueio =
    usuario.perfil !== 'COORDENADOR'
      ? 'Seu perfil tem acesso apenas para consulta do consentimento.'
      : estudo.status !== 'RECRUTAMENTO'
        ? 'O consentimento só pode ser registrado com o estudo em recrutamento.'
        : candidato.situacao === 'INCLUIDO'
          ? 'O candidato já foi incluído no estudo; o consentimento não pode mais ser registrado.'
          : null

  async function registrar(evento: FormEvent) {
    evento.preventDefault()
    if (!confirmado || bloqueio) return
    setEnviando(true)
    setErroRegistro(null)
    try {
      // Sem edição manual, o servidor usa o instante atual (evita divergência de fuso entre navegador e API)
      await consentimentoApi.registrar(candidato.id, dataAlterada ? { dataHora: `${dataHora}:00` } : {})
      recarregar()
      aoRegistrar()
    } catch (falha) {
      setErroRegistro(converterErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="secao pilha" aria-label="Consentimento">
      <h3>Consentimento</h3>

      {carregando && <p className="carregando">Carregando consentimento…</p>}
      {falhaDeCarga && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}

      {comprovante && (
        <div className="resultado resultado--aprovado" role="status">
          <div>Comprovante do consentimento</div>
          <dl className="resumo-estudo">
            <dt>Data e hora</dt>
            <dd>{formatarDataHora(comprovante.dataHora)}</dd>
            <dt>Responsável</dt>
            <dd>{comprovante.responsavel}</dd>
            <dt>Versão do termo</dt>
            <dd>{comprovante.versaoTermo}</dd>
          </dl>
        </div>
      )}

      {naoRegistrado && (
        <>
          <p className="alerta alerta--info" role="note">
            Consentimento simulado, dados fictícios: este registro não tem valor legal.
          </p>
          <div className="termo" tabIndex={0} role="document" aria-label="Termo de consentimento livre e esclarecido (simulado)">
            <strong>{VERSAO_PADRAO_DO_TERMO}</strong>
            <p>
              Declaro que fui informado(a), em linguagem clara, sobre o objetivo do estudo, os procedimentos, as visitas
              previstas, os possíveis riscos e benefícios e o caráter voluntário da participação.
            </p>
            <p>
              Entendo que posso retirar meu consentimento a qualquer momento, sem prejuízo, e que meus dados serão
              tratados de forma sigilosa e utilizados apenas para fins acadêmicos.
            </p>
            <p>Este termo é uma simulação para fins didáticos; nenhum dado real é coletado.</p>
          </div>

          {bloqueio ? (
            <p className="alerta alerta--info" role="status">
              {bloqueio}
            </p>
          ) : (
            <form className="pilha" onSubmit={registrar} noValidate aria-label="Registrar consentimento">
              <label className="campo campo--checkbox">
                <input type="checkbox" checked={confirmado} onChange={(e) => setConfirmado(e.target.checked)} />
                <span>Confirmo que o termo foi apresentado ao candidato e que ele consente em participar (simulado).</span>
              </label>
              <label className="campo">
                <span>Data e hora do consentimento</span>
                <input
                  type="datetime-local"
                  value={dataHora}
                  max={agoraParaCampo()}
                  onChange={(e) => {
                    setDataHora(e.target.value)
                    setDataAlterada(true)
                  }}
                />
              </label>
              {erroRegistro && (
                <p className="alerta alerta--erro" role="alert">
                  {erroRegistro}
                </p>
              )}
              <div className="formulario__acoes">
                <button type="submit" className="botao" disabled={!confirmado || enviando}>
                  {enviando ? 'Registrando…' : 'Registrar'}
                </button>
              </div>
            </form>
          )}
        </>
      )}
    </section>
  )
}
