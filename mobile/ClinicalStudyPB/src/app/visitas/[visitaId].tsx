import { EmBreve, Tela } from '@/components/ui';

// M07 Detalhe da visita: a tela real é da T117/T118; aqui fica só a rota declarada pelo esqueleto (T108)
export default function DetalheVisitaScreen() {
  return (
    <Tela titulo="Visita">
      <EmBreve requisito="RF18" texto="O detalhe da visita chega nas T117 e T118." />
    </Tela>
  );
}
