# SimpleGuard Desktop Agent

Core Rust e app desktop Tauri minimo para pareamento do agente desktop.

## Escopo atual

- Montar payload de pareamento para `POST /api/agent/pairing/complete`.
- Validar `instance_url`, `pairing_code`, `agent_instance_id`, `platform` e `public_key`.
- Representar plataformas desktop suportadas pelo contrato da API: `WINDOWS`, `LINUX`, `MACOS`.
- Abrir janela desktop compacta para boas-vindas, pareamento e despareamento.
- Enviar pareamento e despareamento para API `http://` ou `https://` usando os dados informados na UI.
- Gerar chave EC local, registrar a chave publica no pareamento e assinar o pedido de despareamento.
- Preparar contrato, assinatura e estados de apresentacao para despareamento.
- Nao implementa tray, daemon, telemetria, comandos remotos ou instalador.

## Executar app desktop

Abrir a janela pequena de desenvolvimento:

```bash
cargo run
```

Gerar executavel local:

```bash
cargo build --release
```

Executavel gerado:

```text
target/release/simpleguard-desktop-agent.exe
```

Fluxo na janela:

- Boas-vindas abre sem tela cheia em 520x640.
- `Iniciar pareamento` mostra campos de URL da instancia e codigo.
- `Parear` chama `POST /api/agent/pairing/complete`.
- Ao parear, o agente salva o vinculo local em `simpleguard-desktop-agent.local.json`.
- Tela de despareamento chama `DELETE /api/agent/devices/{deviceId}/pairing` com assinatura local.

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
