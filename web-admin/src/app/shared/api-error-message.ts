interface ApiErrorPayload {
  readonly mensagem?: unknown;
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  const payload = errorPayload(error);
  const message = payloadMessage(payload);
  return message ?? fallback;
}

function errorPayload(error: unknown): unknown {
  if (!isRecord(error)) {
    return error;
  }
  return 'error' in error ? error['error'] : error;
}

function payloadMessage(payload: unknown): string | null {
  if (typeof payload === 'string') {
    try {
      return payloadMessage(JSON.parse(payload) as unknown);
    } catch {
      return null;
    }
  }

  if (!isRecord(payload)) {
    return null;
  }

  const message = (payload as ApiErrorPayload).mensagem;
  return typeof message === 'string' && message.trim().length > 0
    ? message.trim()
    : null;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
