import { EmBreve, Tela } from '@/components/ui';

// M03 Meus estudos — conteúdo depende dos endpoints do participante (RF17)
export default function EstudosScreen() {
  return (
    <Tela titulo="Meus estudos">
      <EmBreve
        requisito="RF17"
        texto="A lista dos estudos em que você está incluído aparecerá aqui, com o detalhe e a opção de solicitar retirada (RF20)."
      />
    </Tela>
  );
}
