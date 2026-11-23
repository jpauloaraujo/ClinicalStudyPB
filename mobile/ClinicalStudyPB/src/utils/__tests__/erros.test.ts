import { ErroApi, MSG_SEM_CONEXAO } from '@/api/erro';
import { ehSemConexao, MSG_FALHA_GENERICA, mensagemDaConsulta } from '@/utils/erros';

describe('mensagens de erro das consultas (T119, RQ-T119-04 e 05)', () => {
  it('falta de conexão tem texto próprio, sem a URL da API', () => {
    const erro = new ErroApi('Não foi possível conectar à API em http://10.0.2.2:8081.', { codigo: 'SEM_CONEXAO' });

    expect(ehSemConexao(erro)).toBe(true);
    expect(mensagemDaConsulta(erro)).toBe(MSG_SEM_CONEXAO);
    expect(MSG_SEM_CONEXAO).toBe('Não foi possível conectar. Verifique sua conexão e tente de novo.');
  });

  it('erro do servidor mostra a mensagem do servidor', () => {
    expect(mensagemDaConsulta(new ErroApi('Parâmetro inválido.', { status: 400, codigo: 'PARAMETRO_INVALIDO' }))).toBe(
      'Parâmetro inválido.',
    );
  });

  it('5xx sem mensagem do servidor mostra o texto genérico', () => {
    expect(mensagemDaConsulta(new ErroApi('Erro 500 na API.', { status: 500 }))).toBe(MSG_FALHA_GENERICA);
  });

  it('nunca devolve JSON cru', () => {
    expect(mensagemDaConsulta(new ErroApi('{"erro":"x"}', { status: 500 }))).toBe(MSG_FALHA_GENERICA);
  });

  it('erro inesperado (não é ErroApi) mostra o texto genérico', () => {
    expect(mensagemDaConsulta(new Error('boom'))).toBe(MSG_FALHA_GENERICA);
    expect(mensagemDaConsulta('texto')).toBe(MSG_FALHA_GENERICA);
    expect(ehSemConexao(new Error('x'))).toBe(false);
  });
});
