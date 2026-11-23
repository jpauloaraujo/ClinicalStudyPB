// Dados em memória do modo de mocks (EXPO_PUBLIC_USE_MOCKS=true). Seguem o contrato da Sprint 3
// (docs/design/sprint3/00-convencoes-e-contrato-compartilhado.md, seção 3) e o participante de demonstração.
// Uso apenas em desenvolvimento e testes de componente: a entrega roda contra a API real.

export const EMAIL_PARTICIPANTE_DEMO = 'participante@clinicalstudy.test';
export const SENHA_DEMO = 'senha-de-teste';
export const TOKEN_MOCK = 'token-de-mock';

export type UsuarioMock = { id: string; nome: string; email: string; perfil: 'PARTICIPANTE' };

export const usuarioMock: UsuarioMock = {
  id: 'd6300000-0000-4000-8000-000000002401',
  nome: 'Pessoa Teta (Ficticia)',
  email: EMAIL_PARTICIPANTE_DEMO,
  perfil: 'PARTICIPANTE',
};

// T110: contas extras do modo de mocks, para exercitar sem backend a regra 8 (só o participante entra) e o
// participante sem participação vinculada (a lista /api/me/estudos vem vazia, como no seed real)
export type ContaMock = {
  email: string;
  token: string;
  usuario: { id: string; nome: string; email: string; perfil: 'ADMINISTRADOR' | 'PESQUISADOR' | 'COORDENADOR' | 'PARTICIPANTE' };
  semEstudos: boolean;
};

export const EMAIL_SEM_ESTUDO = 'sem.vinculo@clinicalstudy.test';

export const CONTAS_MOCK: ContaMock[] = [
  { email: EMAIL_PARTICIPANTE_DEMO, token: TOKEN_MOCK, usuario: usuarioMock, semEstudos: false },
  {
    email: EMAIL_SEM_ESTUDO,
    token: 'token-de-mock-sem-estudo',
    usuario: {
      id: 'd6300000-0000-4000-8000-000000002402',
      nome: 'Pessoa Sem Estudo (Ficticia)',
      email: EMAIL_SEM_ESTUDO,
      perfil: 'PARTICIPANTE',
    },
    semEstudos: true,
  },
  ...(['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'] as const).map((perfil) => {
    const email = `${perfil.toLowerCase()}@clinicalstudy.test`;
    return {
      email,
      token: `token-de-mock-${perfil.toLowerCase()}`,
      usuario: { id: `d6300000-0000-4000-8000-00000000250${perfil.length}`, nome: `${perfil} (Ficticio)`, email, perfil },
      semEstudos: false,
    };
  }),
];

export type MeuEstudoMock = {
  participacaoId: string;
  estudoId: string;
  titulo: string;
  resumo: string;
  statusEstudo: 'RECRUTAMENTO' | 'EXECUCAO' | 'ENCERRADO';
  dataInclusao: string;
  situacaoParticipacao: 'ATIVO' | 'RETIRADO';
};

export type MeuEstudoDetalheMock = MeuEstudoMock & {
  objetivo: string;
  dataInicio: string;
  dataFim: string;
  responsavelNome: string;
  retirada: { dataRetirada: string } | null;
};

export type MinhaVisitaMock = {
  id: string;
  participacaoId: string;
  estudoId: string;
  estudoTitulo: string;
  visitaNome: string;
  dataHora: string;
  situacao: 'AGENDADA' | 'REALIZADA' | 'AUSENTE' | 'CANCELADA';
  dataHoraRealizada: string | null;
  motivo: string | null;
  atividades: string[];
};

const OBJETIVO =
  'Avaliar a eficácia e a segurança de uma intervenção fictícia no controle da pressão arterial em adultos, ' +
  'com acompanhamento por visitas periódicas e registro dos resultados de cada avaliação.';

export const estudosMock: MeuEstudoDetalheMock[] = [
  {
    participacaoId: 'd6300000-0000-4000-8000-000000002411',
    estudoId: 'd6300000-0000-4000-8000-000000002421',
    titulo: 'Estudo de pressão arterial (demonstração)',
    resumo: OBJETIVO.slice(0, 160),
    statusEstudo: 'EXECUCAO',
    dataInclusao: '2026-10-30',
    situacaoParticipacao: 'ATIVO',
    objetivo: OBJETIVO,
    dataInicio: '2026-10-01',
    dataFim: '2027-05-01',
    responsavelNome: 'Coordenador de Demonstração',
    retirada: null,
  },
  {
    participacaoId: 'd6300000-0000-4000-8000-000000002412',
    estudoId: 'd6300000-0000-4000-8000-000000002422',
    titulo: 'Estudo de sono (demonstração)',
    resumo: 'Estudo fictício sobre a qualidade do sono.',
    statusEstudo: 'EXECUCAO',
    dataInclusao: '2026-09-15',
    situacaoParticipacao: 'RETIRADO',
    objetivo: 'Estudo fictício sobre a qualidade do sono.',
    dataInicio: '2026-09-01',
    dataFim: '2027-03-01',
    responsavelNome: 'Coordenador de Demonstração',
    retirada: { dataRetirada: '2026-11-10' },
  },
];

const ESTUDO_PRINCIPAL = estudosMock[0];

function visita(parcial: Partial<MinhaVisitaMock> & Pick<MinhaVisitaMock, 'id' | 'visitaNome' | 'dataHora'>): MinhaVisitaMock {
  return {
    participacaoId: ESTUDO_PRINCIPAL.participacaoId,
    estudoId: ESTUDO_PRINCIPAL.estudoId,
    estudoTitulo: ESTUDO_PRINCIPAL.titulo,
    situacao: 'AGENDADA',
    dataHoraRealizada: null,
    motivo: null,
    atividades: ['Aferir pressão arterial'],
    ...parcial,
  };
}

export const proximasVisitasMock: MinhaVisitaMock[] = [
  visita({ id: 'd6300000-0000-4000-8000-000000002431', visitaNome: 'Retorno 30 dias', dataHora: '2026-11-29T09:30:00-03:00' }),
];

export const historicoVisitasMock: MinhaVisitaMock[] = [
  visita({
    id: 'd6300000-0000-4000-8000-000000002432',
    visitaNome: 'Triagem',
    dataHora: '2026-10-30T09:00:00-03:00',
    situacao: 'REALIZADA',
    dataHoraRealizada: '2026-10-30T09:10:00-03:00',
  }),
];
