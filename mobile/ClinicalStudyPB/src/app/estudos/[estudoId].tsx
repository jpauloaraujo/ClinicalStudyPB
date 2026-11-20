import { EmBreve, Tela } from '@/components/ui';

// M04 Detalhe do estudo: a tela real é da T112; aqui fica só a rota declarada pelo esqueleto (T108)
export default function DetalheEstudoScreen() {
  return (
    <Tela titulo="Estudo">
      <EmBreve requisito="RF17" texto="O detalhe do estudo chega na T112." />
    </Tela>
  );
}
