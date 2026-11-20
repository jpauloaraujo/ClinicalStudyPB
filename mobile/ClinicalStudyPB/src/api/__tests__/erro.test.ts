import { ErroApi, erroDaResposta, mensagemDoErro } from '@/api/erro';

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
