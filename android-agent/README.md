# SimpleGuard Android Agent

Modulo Android Kotlin para pareamento inicial do agente.

## Fluxo implementado

- Usuario informa URL da instancia e codigo de pareamento.
- Agente cria/recupera chave assimetrica no Android Keystore.
- Agente envia `pairingCode`, `agentInstanceId`, `platform=ANDROID` e `publicKey` para `POST /api/agent/pairing/complete`.
- UI mostra estados de pareamento: aguardando, validando, expirado, falha e pareado.

## Validacao local

Requer Android Studio ou Android SDK/Gradle instalado:

```bash
cd android-agent
gradle test
```

## Pareamento com celular fisico em ambiente local

Para parear usando a API local em containers, exponha a API para a rede local:

```bash
cd /mnt/c/SimpleGuard/simple-guard
docker compose --env-file deploy/.env -f deploy/compose.yaml -f deploy/compose.mobile.yaml up -d --build
```

No app Android, use a URL da instancia com o IP do computador na rede Wi-Fi:

```text
http://IP_DO_COMPUTADOR:8080
```
