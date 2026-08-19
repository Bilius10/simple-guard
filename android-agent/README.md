# SimpleGuard Android Agent

Modulo Android Kotlin para boas-vindas, pareamento, envio de localizacao e gestao do vinculo do agente.

## Fluxo implementado

- Dispositivo sem pareamento abre a tela de boas-vindas com o objetivo e as capacidades do agente.
- Acao `Iniciar pareamento` abre a tela de pareamento.
- Usuario informa URL da instancia e codigo de pareamento.
- Agente cria/recupera chave assimetrica no Android Keystore.
- Agente envia `pairingCode`, `agentInstanceId`, `platform=ANDROID` e `publicKey` para `POST /api/agent/pairing/complete`.
- UI mostra estados de pareamento: aguardando, validando, expirado, falha e pareado.
- Dispositivo pareado abre a tela `Mobile Agent / 07 - Despareamento`.
- Agente solicita permissao de localizacao e inicia servico em primeiro plano apos o pareamento.
- Servico coleta pelo GPS e envia um ponto a cada minuto enquanto o vinculo estiver ativo.
- Permissao negada, GPS desligado, localizacao ausente e falha de rede permanecem estados explicitos no app ou na notificacao persistente.
- Despareamento usa confirmacao explicita e assinatura ECDSA da chave no Android Keystore.
- Falha HTTP mantem o vinculo; falha de rede remove o vinculo local e registra sincronizacao pendente.

## Validacao local

Requer Android Studio ou Android SDK/Gradle instalado:

```bash
cd android-agent
./gradlew test koverVerify
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

## Despareamento

O agente chama `DELETE /api/agent/devices/{deviceId}/pairing` com os headers:

- `X-Agent-Instance-Id`
- `X-Agent-Signature`

A assinatura usa `SHA256withECDSA` sobre `UNPAIR_DEVICE\n{deviceId}\n{agentInstanceId}`. A chave privada nunca sai do Android Keystore.

## Envio de localizacao

O agente chama `POST /api/agent/devices/{deviceId}/locations` com os headers:

- `X-Agent-Instance-Id`
- `X-Agent-Signature`

O corpo contem latitude, longitude, precisao, altitude e velocidade quando disponiveis, provedor e `collectedAt`. Campos ausentes nao recebem valores artificiais.

A assinatura usa `SHA256withECDSA` sobre o payload canonico:

```text
INGEST_LOCATION
{deviceId}
{agentInstanceId}
{collectedAt em UTC}
{latitude canonica}
{longitude canonica}
{accuracyMeters ou vazio}
{altitudeMeters ou vazio}
{speedMetersPerSecond ou vazio}
{provider}
```
