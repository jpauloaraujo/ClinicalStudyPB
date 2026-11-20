import { fireEvent, render, screen } from '@testing-library/react-native';
import { Carregando } from '@/components/Carregando';
import { EstadoErro } from '@/components/EstadoErro';
import { EstadoVazio } from '@/components/EstadoVazio';
import { Selo } from '@/components/Selo';

describe('componentes base', () => {
  it('EstadoVazio mostra título e texto', async () => {
    await render(<EstadoVazio titulo="Nenhum estudo" texto="Você ainda não participa de estudos." />);

    expect(screen.getByText('Nenhum estudo')).toBeTruthy();
    expect(screen.getByText('Você ainda não participa de estudos.')).toBeTruthy();
  });

  it('EstadoVazio funciona só com o título', async () => {
    await render(<EstadoVazio titulo="Sem visitas" />);

    expect(screen.getByText('Sem visitas')).toBeTruthy();
  });

  it('EstadoErro mostra a mensagem e chama "Tentar de novo"', async () => {
    const aoTentar = jest.fn();
    await render(<EstadoErro mensagem="Falha ao carregar." aoTentarDeNovo={aoTentar} />);

    expect(screen.getByText('Falha ao carregar.')).toBeTruthy();
    await fireEvent.press(screen.getByText('Tentar de novo'));
    expect(aoTentar).toHaveBeenCalledTimes(1);
  });

  it('EstadoErro não mostra o botão sem a ação de repetir', async () => {
    await render(<EstadoErro mensagem="Falha ao carregar." />);

    expect(screen.queryByText('Tentar de novo')).toBeNull();
  });

  it('Selo exibe o rótulo em texto, em qualquer tom', async () => {
    for (const tom of ['neutro', 'info', 'sucesso', 'aviso', 'perigo'] as const) {
      await render(<Selo rotulo={`Selo ${tom}`} tom={tom} />);
      expect(screen.getByText(`Selo ${tom}`)).toBeTruthy();
    }
  });

  it('Carregando expõe o texto acessível', async () => {
    await render(<Carregando texto="Carregando estudos…" />);

    expect(screen.getByText('Carregando estudos…')).toBeTruthy();
    expect(screen.getByLabelText('Carregando estudos…')).toBeTruthy();
  });

  it('Carregando usa o texto padrão', async () => {
    await render(<Carregando />);

    expect(screen.getByText('Carregando…')).toBeTruthy();
  });
});
