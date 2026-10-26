import { EmBreve, Tela } from '@/components/ui';

// M05/M06 Visitas — conteúdo depende dos endpoints do participante (RF18, RF19)
export default function VisitasScreen() {
  return (
    <Tela titulo="Visitas">
      <EmBreve
        requisito="RF18"
        texto="Suas próximas visitas e o histórico de comparecimento aparecerão aqui."
      />
      <EmBreve requisito="RF19" texto="Você poderá confirmar presença ou pedir reagendamento de cada visita." />
    </Tela>
  );
}
