import type { CandidatoResponse } from '../../../../api/tipos'
import '../recrutamento.css'

type SecaoInclusaoProps = {
  candidato: CandidatoResponse
}

// Espaço reservado da ficha do candidato para a T61 (pré-condições e botão "Incluir no estudo").
// Quem implementar a T61 substitui o corpo deste componente, sem mexer na ficha nem nas outras seções.
export function SecaoInclusao({ candidato }: SecaoInclusaoProps) {
  return (
    <section className="secao secao--reservada" aria-label="Inclusão">
      <h3>Inclusão no estudo</h3>
      <p>
        {candidato.situacao === 'INCLUIDO'
          ? 'Candidato incluído como participante do estudo.'
          : 'As pré-condições e o botão de inclusão serão exibidos aqui (T61).'}
      </p>
    </section>
  )
}
