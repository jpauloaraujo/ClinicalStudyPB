import {
  CODIGO_PERFIL_NAO_PARTICIPANTE,
  CODIGO_SEM_CONEXAO,
  ErroApi,
  erroDaResposta,
  mensagemDoErro,
  mensagemDoLogin,
} from '@/api/erro';

describe('erroDaResposta', () => {
  it('prefere codigoRegra e mensagem do formato da Sprint 2', () => {
    const erro = erroDaResposta(409, {
      status: 409,
      codigoRegra: 'DATA_FORA_DA_JANELA',
      codigo: 'OUTRO',
      mensagem: 'Data fora da janela.',
      erro: 'Texto antigo.',
    });

    expect(erro.codigo).toBe('DATA_FORA_DA_JANELA');
    expect(erro.message).toBe('Data fora da janela.');
    expect(erro.status).toBe(409);
  });

  it('aceita o formato antigo ({ erro, codigo })', () => {
    const erro = erroDaResposta(404, { erro: 'Estudo não encontrado.', codigo: 'RECURSO_NAO_ENCONTRADO' });

    expect(erro.message).toBe('Estudo não encontrado.');
    expect(erro.codigo).toBe('RECURSO_NAO_ENCONTRADO');
  });

  it('usa a mensagem padrão sem corpo e deixa errosCampo vazio', () => {
    const erro = erroDaResposta(500, null);

    expect(erro.message).toBe('Erro 500 na API.');
    expect(erro.codigo).toBeUndefined();
    expect(erro.errosCampo).toEqual([]);
  });

  it('descarta itens incompletos de errosCampo', () => {
    const erro = erroDaResposta(400, {
      errosCampo: [{ campo: 'motivo', erro: 'obrigatório' }, { campo: 'solicitante' }, { erro: 'sem campo' }],
    });

    expect(erro.errosCampo).toEqual([{ campo: 'motivo', erro: 'obrigatório' }]);
  });
});

describe('mensagemDoLogin (T110, RQ-T110-03 e 05)', () => {
  it('usa uma mensagem só para credencial inválida, sem repassar o texto da API', () => {
    const erro = new ErroApi('Credenciais inválidas', { status: 401 });

    expect(mensagemDoLogin(erro)).toBe('E-mail ou senha inválidos, ou conta desativada.');
  });

  it('explica a recusa de perfil que não é participante', () => {
    const erro = new ErroApi('qualquer texto', { codigo: CODIGO_PERFIL_NAO_PARTICIPANTE });

    expect(mensagemDoLogin(erro)).toBe('Este aplicativo é exclusivo para participantes. Use a aplicação web.');
  });

  it('explica a falha de rede sem mostrar a URL da API', () => {
    const erro = new ErroApi('Não foi possível conectar à API em http://10.0.2.2:8081.', { codigo: CODIGO_SEM_CONEXAO });

    const mensagem = mensagemDoLogin(erro);

    expect(mensagem).toBe('Não foi possível conectar. Verifique sua conexão e tente de novo.');
    expect(mensagem).not.toContain('http');
  });

  it.each([
    ['erro 500 da API', new ErroApi('{"status":500}', { status: 500 })],
    ['erro 400 da API', new ErroApi('Erro 400 na API.', { status: 400 })],
    ['Error comum', new Error('undefined is not a function')],
    ['texto solto', 'falhou'],
    ['valor indefinido', undefined],
  ])('usa a mensagem genérica para %s', (_nome, erro) => {
    expect(mensagemDoLogin(erro)).toBe('Não foi possível entrar agora. Tente novamente.');
  });
});

describe('ErroApi', () => {
  it('é um Error com nome e valores padrão', () => {
    const erro = new ErroApi('Falhou.');

    expect(erro).toBeInstanceOf(Error);
    expect(erro.name).toBe('ErroApi');
    expect(erro.errosCampo).toEqual([]);
  });

  it('mensagemDoErro ignora Error sem mensagem', () => {
    expect(mensagemDoErro(new Error(''))).toBe('Erro inesperado. Tente novamente.');
    expect(mensagemDoErro(undefined)).toBe('Erro inesperado. Tente novamente.');
  });
});
