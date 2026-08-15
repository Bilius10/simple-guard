# SimpleGuard Desktop Agent

Core Rust minimo para o contrato inicial do agente desktop.

## Escopo atual

- Montar payload de pareamento para `POST /api/agent/pairing/complete`.
- Validar `instance_url`, `pairing_code`, `agent_instance_id`, `platform` e `public_key`.
- Representar plataformas desktop suportadas pelo contrato da API: `WINDOWS`, `LINUX`, `MACOS`.
- Preparar contrato, assinatura e estados de apresentacao para despareamento.
- Nao implementa UI Tauri, tray, daemon, telemetria, comandos remotos ou instalador.

## Payload

```json
{
  "pairingCode": "ABCD-2345",
  "agentInstanceId": "desktop-windows-machine-01",
  "platform": "WINDOWS",
  "publicKey": "desktop-public-key-..."
}
```

## Validacao manual

1. Criar dispositivo desktop no web-admin ou API com `type=DESKTOP` e `platform=WINDOWS`, `LINUX` ou `MACOS`.
2. Gerar sessao em `POST /api/devices/{deviceId}/pairing-sessions`.
3. Enviar o payload acima para `POST /api/agent/pairing/complete`.
4. Confirmar que a resposta retorna `pairingStatus=paired` e que a chave ficou registrada para a mesma plataforma.

## Despareamento

- Endpoint: `DELETE /api/agent/devices/{deviceId}/pairing`.
- Payload assinado: `UNPAIR_DEVICE\n{deviceId}\n{agentInstanceId}`.
- Headers: `X-Agent-Instance-Id` e `X-Agent-Signature`.
- Estados prontos para a futura UI Tauri: confirmacao exigida, solicitado, despareado, falha de API e sincronizacao pendente.
