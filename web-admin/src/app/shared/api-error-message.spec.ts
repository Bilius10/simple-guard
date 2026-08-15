import { apiErrorMessage } from './api-error-message';

describe('ApiErrorMessageTests', () => {
  it('returnsBackendMessageFromHttpErrorPayloadTests', () => {
    const message = apiErrorMessage(
      { error: { erro_code: 'DEVICE_NOT_FOUND', mensagem: 'Dispositivo nao encontrado.' } },
      'Falha local.',
    );

    expect(message).toBe('Dispositivo nao encontrado.');
  });

  it('returnsBackendMessageFromSerializedPayloadTests', () => {
    const message = apiErrorMessage(
      { error: JSON.stringify({ mensagem: 'Sessao de pareamento expirada.' }) },
      'Falha local.',
    );

    expect(message).toBe('Sessao de pareamento expirada.');
  });

  it('returnsFallbackWhenBackendMessageIsUnavailableTests', () => {
    expect(apiErrorMessage(new Error('offline'), 'API indisponivel.')).toBe('API indisponivel.');
    expect(apiErrorMessage({ error: '<html>gateway error</html>' }, 'API indisponivel.')).toBe('API indisponivel.');
    expect(apiErrorMessage(null, 'API indisponivel.')).toBe('API indisponivel.');
  });
});
