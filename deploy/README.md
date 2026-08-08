# Deploy local self-hosted

## Pre-requisitos

- Docker Engine com Docker Compose v2
- Portas `80` e `443` livres
- Resolucao local de `localhost` e `idp.localhost`

## Configuracao

1. Copie `deploy/.env.example` para `deploy/.env`.
2. Substitua as senhas de exemplo por valores locais fortes e distintos.
3. Nao versione `deploy/.env`.

Variaveis obrigatorias:

| Variavel | Finalidade |
| --- | --- |
| `SIMPLEGUARD_INSTANCE_ID` | Identificador estavel da instancia. |
| `SIMPLEGUARD_PUBLIC_URL` | URL publica absoluta servida pelo Caddy. |
| `POSTGRES_PASSWORD` | Senha do banco usada pela API e pelo Keycloak no ambiente local. |
| `KEYCLOAK_ADMIN_PASSWORD` | Senha inicial do administrador tecnico do Keycloak. |
| `KEYCLOAK_SIMPLEGUARD_ADMIN_PASSWORD` | Senha inicial do usuario `admin` no realm `simpleguard`. |
| `SIMPLEGUARD_OIDC_ISSUER_URI` | Issuer OIDC externo validado pela API. |
| `SIMPLEGUARD_OIDC_JWK_SET_URI` | Endpoint interno usado pela API para buscar as chaves JWT do Keycloak. |

Variaveis com valor local padrao: `POSTGRES_DB`, `POSTGRES_USER`, `KEYCLOAK_DB` e `KEYCLOAK_ADMIN_USERNAME`.

## Inicializacao

Execute na raiz do repositorio:

```powershell
docker compose --env-file deploy/.env -f deploy/compose.yaml config
docker compose --env-file deploy/.env -f deploy/compose.yaml up --build -d
docker compose --env-file deploy/.env -f deploy/compose.yaml ps
```

Rotas:

- Web admin: `https://localhost`
- API health: `https://localhost/actuator/health`
- API readiness: `https://localhost/actuator/health/readiness`
- Sessao autenticada: `https://localhost/api/session/me`
- Keycloak: `https://idp.localhost`

Credenciais locais importadas no realm `simpleguard`:

- Usuario: `admin`
- Email: `admin@simpleguard.local`
- Senha: valor de `KEYCLOAK_SIMPLEGUARD_ADMIN_PASSWORD`

O Caddy usa uma autoridade certificadora local. O navegador pode exigir que o certificado raiz do volume `caddy-data` seja confiado manualmente no host.

## Diagnostico

```powershell
docker compose --env-file deploy/.env -f deploy/compose.yaml ps
docker compose --env-file deploy/.env -f deploy/compose.yaml logs api web-admin postgres keycloak caddy
```

## Encerramento

```powershell
docker compose --env-file deploy/.env -f deploy/compose.yaml down
```

Os volumes persistentes sao preservados. Remova volumes somente quando a perda dos dados locais for intencional.
