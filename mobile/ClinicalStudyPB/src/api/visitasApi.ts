import { ErroApi, tokenArmazenado, URL_API } from '@/api/cliente';
import { historicoVisitasMock, proximasVisitasMock } from '@/mocks/visitasMock';
import type { VisitaParticipante } from '@/types/visitas';

const USAR_MOCKS = process.env.EXPO_PUBLIC_USE_MOCKS === 'true';

type RespostaHistoricoPaginada =
  | VisitaParticipante[]
  | { content?: VisitaParticipante[] };

async function requisitarVisitas<T>(caminho: string): Promise<T> {
  const token = await tokenArmazenado.ler();
  let resposta: Response;

  try {
    resposta = await fetch(`${URL_API}${caminho}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
  } catch {
    throw new ErroApi(`Não foi possível conectar à API em ${URL_API}.`);
  }

  if (!resposta.ok) {
    const corpo = (await resposta.json().catch(() => null)) as
      | { erro?: string }
      | null;
    throw new ErroApi(
      corpo?.erro ?? `Erro ${resposta.status} na API.`,
      resposta.status
    );
  }

  return (await resposta.json()) as T;
}

export async function buscarProximasVisitasApi(): Promise<VisitaParticipante[]> {
  if (USAR_MOCKS) {
    return proximasVisitasMock;
  }

  const dados = await requisitarVisitas<VisitaParticipante[]>(
    '/api/me/visits/upcoming'
  );
  return Array.isArray(dados) ? dados : [];
}

export async function buscarHistoricoVisitasApi(): Promise<VisitaParticipante[]> {
  if (USAR_MOCKS) {
    return historicoVisitasMock;
  }

  const dados = await requisitarVisitas<RespostaHistoricoPaginada>(
    '/api/me/visits/history'
  );

  if (Array.isArray(dados)) {
    return dados;
  }

  return Array.isArray(dados?.content) ? dados.content : [];
}