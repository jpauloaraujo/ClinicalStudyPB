import type { CandidatoResponse } from '../../../../api/tipos'
import '../recrutamento.css'

type SecaoConsentimentoProps = {
  candidato: CandidatoResponse
}

// Espaço reservado da ficha do candidato para a T58 (tela de consentimento). Quem implementar a T58
// substitui o corpo deste componente, sem precisar mexer na ficha nem nas outras seções.
export function SecaoConsentimento({ candidato }: SecaoConsentimentoProps) {
  return (
    <section className="secao secao--reservada" aria-label="Consentimento">
      <h3>Consentimento</h3>
      <p>
        {candidato.consentimentoRegistrado
          ? 'Consentimento registrado. O comprovante será exibido aqui (T58).'
          : 'O registro do consentimento simulado será feito aqui (T58).'}
      </p>
    </section>
  )
}
