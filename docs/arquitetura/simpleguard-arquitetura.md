---
title: "Arquitetura: SimpleGuard"
project_name: "SimpleGuard"
status: "draft"
created: "2026-08-05"
updated: "2026-08-05"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/produto/simpleguard-prd.md"
  - "docs/pesquisa/simpleguard-risk-stack-research.md"
  - "docs/ux/simpleguard-ux-plataforma-web.md"
  - ".codex/bmad/output/planning-artifacts/architecture/architecture-SimpleGuard-2026-08-05/ARCHITECTURE-SPINE.md"
---

# Arquitetura: SimpleGuard

Documento companion do BMAD architecture spine:
`.codex/bmad/output/planning-artifacts/architecture/architecture-SimpleGuard-2026-08-05/ARCHITECTURE-SPINE.md`

## 1. Resumo

O SimpleGuard sera uma plataforma self-hosted de monitoramento e resposta a incidente para dispositivos, com foco em soberania do usuario, telemetria geoespacial, trilha de auditoria, bloqueio remoto e geracao de evidencias.

A arquitetura do MVP usa:

- **Backend/API:** Java 21 + Spring Boot 3.
- **Web admin:** Angular + TypeScript.
- **Agente Android:** Kotlin.
- **Agente de computador:** Rust como servico/daemon nativo por sistema operacional; Tauri apenas para tray/configuracao local quando houver UI.
- **Banco principal:** PostgreSQL + PostGIS.
- **Mapa:** MapLibre GL JS com tiles OSM/OpenMapTiles, preferencialmente self-hosted.
- **PDF:** HTML/CSS renderizado via Chromium/Playwright.
- **Tempo real para a web:** SSE no MVP; WebSocket apenas se houver necessidade bidirecional real.
- **Comandos remotos:** fila persistente no backend, comando idempotente, expiracao, assinatura e estados auditaveis.
- **Identidade:** OIDC com IdP self-hosted, preferencialmente Keycloak ou authentik.

O MVP deve ser simples de operar: uma instalacao Docker Compose com API, web, banco, worker, IdP, proxy TLS e, quando viavel, servico de tiles.

## 2. Principios Arquiteturais

- **Self-hosted primeiro:** todos os dados sensiveis devem poder residir na infraestrutura do usuario.
- **Soberania por desenho:** dependencia externa deve ser opcional, minima ou explicitamente documentada.
- **Auditoria por padrao:** eventos criticos e comandos remotos devem produzir trilha cronologica append-only.
- **Comando assincrono:** bloqueio remoto nunca deve depender de uma resposta imediata do dispositivo.
- **Offline como estado normal:** agente, backend e UI devem tratar atraso, ausencia de GPS e perda de rede como estados esperados.
- **Evidencia separada de inferencia:** relatorios devem distinguir dado coletado, inferencia do sistema e acao do administrador.
- **Boring tech:** preferir stack madura, facil de diagnosticar, operar, versionar e restaurar.
- **Minimizacao de dados:** coletar apenas telemetria necessaria para monitoramento, resposta e evidencia.

## 3. Escopo Arquitetural do MVP

### Incluido

- Login seguro do administrador.
- Cadastro e pareamento de dispositivos.
- Agente Android pareado com uma instancia SimpleGuard.
- Ingestao de localizacao e telemetria basica.
- Persistencia geoespacial e historica.
- Deteccao de paradas.
- Alertas persistentes.
- Bloqueio remoto e alarme remoto com confirmacao explicita.
- Linha do tempo e trilha de auditoria.
- Hub web operacional com mapa, painel tecnico, log e comandos.
- Relatorio PDF de incidente.
- Deploy self-hosted documentado.

### Fora do MVP

- Multi-tenant SaaS.
- Usuarios secundarios e permissoes granulares.
- Agentes completos para Windows, macOS e Linux.
- Cadeia de custodia formal com assinatura digital, carimbo temporal e PDF/A.
- Operacao totalmente offline de mapas.
- Integracoes externas com seguradoras, autoridades ou sistemas de terceiros.

## 4. Visao C4 - Contexto

```text
Administrador
  -> Plataforma Web SimpleGuard
  -> API SimpleGuard
  -> Banco PostgreSQL/PostGIS

Dispositivo Android monitorado
  -> Agente Android SimpleGuard
  -> API SimpleGuard

API SimpleGuard
  -> IdP OIDC self-hosted
  -> Worker de PDF
  -> Servico de tiles/geocoding opcional
  -> Push provider opcional apenas como wake-up
```

### Sistemas externos opcionais

- **FCM:** apenas para acordar o agente Android ou avisar que existe comando pendente. O payload nao deve carregar localizacao, dados pessoais ou comando completo.
- **Provedor de tiles/geocoding:** opcional. Para soberania, preferir tiles self-hosted. Se houver provedor externo, documentar termos, retencao e dados enviados.
- **NTP/carimbo temporal confiavel:** fase posterior se evidencia formal virar requisito.

## 5. Visao C4 - Containers

| Container | Tecnologia | Responsabilidade |
|---|---|---|
| Web Admin | Angular + TypeScript + MapLibre GL JS | Hub operacional, mapa, log, painel tecnico, comandos e relatorios. |
| API Backend | Java 21 + Spring Boot 3 | Autenticacao, dispositivos, telemetria, incidentes, comandos, auditoria e orquestracao. |
| Worker | Java Spring Boot worker ou servico separado | Geracao de PDF, tarefas agendadas, expiracao de comandos e agregacoes. |
| Banco | PostgreSQL + PostGIS | Dados relacionais, geodados, historico, auditoria e estado operacional. |
| IdP | Keycloak ou authentik | OIDC, MFA, WebAuthn/passkeys e politicas de sessao. |
| Reverse Proxy | Caddy ou Traefik | TLS, roteamento HTTP, headers de seguranca e exposicao controlada. |
| Tile Server | TileServer GL/OpenMapTiles | Tiles vetoriais self-hosted quando soberania exigir. |
| Agente Android | Kotlin | Coleta de telemetria, armazenamento offline local e execucao de comandos suportados. |
| Agente de Computador | Rust + Tauri opcional | Servico/daemon nativo para telemetria e comandos; tray/configuracao local apenas quando necessario. |

## 6. Modulos do Backend

### 6.1 Identity & Access

- Validar tokens OIDC.
- Mapear identidade externa para usuario administrador local.
- Aplicar autorizacao por instancia.
- Exigir reautenticacao para comando critico quando o IdP suportar.

### 6.2 Device Registry

- Criar e administrar dispositivos.
- Gerar fluxo de pareamento.
- Registrar estado de pareamento, chaves do agente e metadados minimos.
- Revogar dispositivo comprometido ou removido.

### 6.3 Telemetry Ingestion

- Receber localizacao, bateria, sinal, rede, timestamp de coleta e metadados de precisao.
- Validar autenticidade do agente.
- Rejeitar telemetria de dispositivo nao pareado.
- Aceitar eventos fora de ordem sem corromper a linha do tempo.
- Persistir telemetria bruta antes de inferencias.

### 6.4 Geospatial Processing

- Normalizar coordenadas em WGS84.
- Calcular ultima localizacao confiavel.
- Detectar paradas com regra configuravel.
- Gerar trajetoria por janela de incidente.
- Separar ponto coletado de parada inferida.

### 6.5 Incident & Alert Management

- Criar incidente manualmente.
- Manter alertas persistentes ate confirmacao manual.
- Associar eventos, comandos, telemetria e relatorios ao incidente.
- Registrar abertura, atualizacao e encerramento.

### 6.6 Remote Command Orchestrator

- Criar comandos remotos com `command_id` unico.
- Persistir comando antes de qualquer tentativa de entrega.
- Controlar estados: `requested`, `queued`, `delivered`, `executed`, `confirmed`, `failed`, `expired`, `cancelled`.
- Aplicar idempotencia, expiracao, nonce e assinatura.
- Disponibilizar comando para polling seguro do agente.
- Opcionalmente enviar wake-up via push.

### 6.7 Audit Ledger

- Registrar eventos de seguranca e acoes administrativas.
- Operar em modelo append-only.
- Encadear hash de eventos criticos no MVP se o custo for baixo.
- Nunca permitir edicao silenciosa de evento critico.

### 6.8 Evidence & Report

- Montar snapshot do incidente.
- Gerar HTML de relatorio com mapa estatico, trajetoria, paradas, eventos e dados tecnicos.
- Renderizar PDF via Chromium/Playwright.
- Armazenar hash do PDF e dos eventos usados na geracao.
- Exibir limitacoes: offline, baixa precisao, lacunas de telemetria e comandos sem confirmacao.

### 6.9 Realtime Updates

- Expor stream SSE para a web admin com eventos de dispositivo, alerta, comando e incidente.
- Usar WebSocket apenas se a interface passar a exigir envio bidirecional persistente.

### 6.10 Desktop Agent

- Implementar agente de computador em Rust para Windows, macOS e Linux.
- Rodar como servico/daemon nativo do sistema operacional.
- Manter um core compartilhado para pareamento, telemetria, fila offline, validacao de comandos e sincronizacao.
- Usar Tauri somente se houver necessidade de tray app, pareamento visual ou configuracao local.
- Nao usar Electron no MVP/fase 2 por custo operacional, consumo de recursos e excesso de superficie para um agente leve.
- Cada sistema operacional deve ter adapter proprio para lock, coleta de rede, instalacao, autostart, logs e atualizacao.
- A UI administrativa continua sendo a plataforma web Angular, nao o app de computador.

## 7. Fluxos Principais

### 7.1 Pareamento do Dispositivo

1. Administrador cria dispositivo na web.
2. API gera token curto de pareamento ou QR Code.
3. Agente Android le o token.
4. Agente cria par de chaves local.
5. API valida token, registra chave publica do agente e marca dispositivo como pareado.
6. Evento de pareamento entra na auditoria.
7. Web recebe atualizacao via SSE.

### 7.2 Ingestao de Telemetria

1. Agente coleta localizacao e telemetria.
2. Agente grava evento local com timestamp de coleta.
3. Agente envia lote assinado para a API.
4. API valida dispositivo, assinatura, schema, ordem e duplicidade.
5. API persiste telemetria bruta.
6. Processador geoespacial atualiza ultima posicao, trajetoria e paradas.
7. API emite evento para web e auditoria quando aplicavel.

### 7.3 Deteccao de Parada

1. API avalia pontos por dispositivo em janela movel.
2. Sistema compara raio de tolerancia, precisao reportada e tempo acumulado.
3. Se o dispositivo permanecer na mesma area por 10 minutos, cria inferencia de parada.
4. Parada recebe inicio, fim quando disponivel, duracao, centroide, coordenadas e confiabilidade.
5. Evento de parada aparece no mapa, log e PDF.

Lacuna: o raio de tolerancia para "mesma posicao" ainda precisa ser decidido.

### 7.4 Bloqueio Remoto

1. Administrador seleciona dispositivo.
2. Web exige confirmacao explicita.
3. API pode exigir reautenticacao.
4. API cria comando `LOCK_DEVICE` em estado `requested`.
5. API valida permissao, transforma em `queued` e registra auditoria.
6. Agente recebe wake-up opcional ou consulta comandos pendentes.
7. Agente valida assinatura, alvo, nonce e validade.
8. Agente tenta executar bloqueio suportado pela plataforma.
9. Agente envia resultado: `executed`, `failed` ou detalhe de erro.
10. API atualiza estado, registra auditoria e notifica web.

Regra: bloqueio remoto deve preservar dados e nao executar wipe automatico.

### 7.5 Geracao de Evidencia em PDF

1. Administrador seleciona incidente.
2. API monta snapshot imutavel dos dados relevantes.
3. Worker gera imagem/snapshot do mapa e HTML do relatorio.
4. Chromium/Playwright renderiza PDF.
5. API calcula hash do PDF e vincula ao incidente.
6. Evento de relatorio gerado entra na auditoria.
7. Web disponibiliza download.

O PDF deve separar:

- Dado factual coletado.
- Inferencia do sistema.
- Acao do administrador.
- Limitacoes e lacunas.

## 8. Modelo de Dados Inicial

### Tabelas principais

| Tabela | Finalidade |
|---|---|
| `users` | Usuario administrador local ou mapeamento do IdP. |
| `devices` | Dispositivos cadastrados e estado atual. |
| `device_keys` | Chaves publicas, rotacao e revogacao do agente. |
| `pairing_sessions` | Sessoes curtas de pareamento. |
| `device_locations` | Pontos geoespaciais brutos. |
| `device_telemetry` | Bateria, rede, sinal e dados tecnicos. |
| `device_events` | Eventos observados pelo agente ou sistema. |
| `stops` | Paradas inferidas a partir de localizacao. |
| `incidents` | Incidentes declarados e encerrados. |
| `alerts` | Alertas persistentes vinculados a dispositivo/incidente. |
| `remote_commands` | Comandos remotos e estado atual. |
| `remote_command_events` | Transicoes de comando. |
| `audit_log` | Trilha append-only de seguranca e administracao. |
| `incident_reports` | PDFs, hashes, metadados e snapshot usado. |

### Regras de modelagem

- Usar `geography(Point, 4326)` em `device_locations` quando distancia real importar.
- Indexar `device_locations` por `device_id`, `collected_at` e geografia.
- Separar `collected_at` de `received_at`.
- Usar identificadores UUID.
- Persistir payload bruto validado quando necessario para auditoria.
- Dados ausentes devem ser `null`, nao `0` ou valor inventado.
- Eventos criticos devem ser append-only.

## 9. API Inicial

### Administrador Web

- `GET /api/devices`
- `POST /api/devices`
- `GET /api/devices/{deviceId}`
- `GET /api/devices/{deviceId}/locations`
- `GET /api/devices/{deviceId}/telemetry/latest`
- `GET /api/incidents`
- `POST /api/incidents`
- `GET /api/incidents/{incidentId}`
- `POST /api/incidents/{incidentId}/close`
- `POST /api/devices/{deviceId}/commands/lock`
- `POST /api/devices/{deviceId}/commands/alarm`
- `GET /api/commands/{commandId}`
- `GET /api/audit-log`
- `POST /api/incidents/{incidentId}/reports`
- `GET /api/reports/{reportId}/download`
- `GET /api/events/stream`

### Agente Android

- `POST /api/agent/pairing/complete`
- `POST /api/agent/telemetry`
- `GET /api/agent/commands/pending`
- `POST /api/agent/commands/{commandId}/events`
- `POST /api/agent/health`

### Health e operacao

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

## 10. Seguranca

### Transporte

- TLS obrigatorio.
- HSTS no proxy.
- Cookies `Secure`, `HttpOnly` e `SameSite`.
- CORS restrito ao dominio da instancia.

### Autenticacao do administrador

- OIDC via Keycloak ou authentik.
- WebAuthn/passkeys recomendado.
- Recovery codes ou chave fisica devem ser definidos antes de uso critico em producao.
- Reautenticacao para bloqueio remoto, alarme e exportacao sensivel.

### Autenticacao do agente

- Pareamento com token curto.
- Chave assimetrica por dispositivo.
- Rotacao e revogacao de chave.
- Rejeicao de telemetria de dispositivo nao pareado.
- Assinatura ou HMAC de lotes de telemetria e eventos criticos.

### Comandos remotos

- Comando com `command_id`, alvo, tipo, validade, nonce e assinatura.
- Payload minimo.
- Idempotencia obrigatoria.
- Expiracao obrigatoria.
- Estado auditavel para cada transicao.
- Protecao contra replay.

### Dados em repouso

- PostgreSQL com volume criptografado pelo host ou storage.
- Segredos fora do repositorio, via `.env` local ou secret manager self-hosted.
- Backup criptografado.
- Retencao configuravel para telemetria e relatorios.

## 11. Privacidade e Soberania

- Instancia deve poder operar sem analytics externo.
- Logs tecnicos nao devem incluir coordenadas quando nao forem necessarias para diagnostico.
- Push externo deve carregar apenas identificador opaco de wake-up.
- Geocoding externo deve ser evitado no MVP ou explicitamente configuravel.
- Tiles self-hosted devem ser a opcao recomendada para ambientes sensiveis.
- Exportacao e exclusao de dados devem ser planejadas antes de release publico.
- RIPD/DPIA deve existir antes de operacao gerenciada ou comercial ampla.

## 12. Observabilidade

### Logs

- Logs estruturados em JSON.
- Correlation ID por requisicao.
- Device ID e incident ID mascarados quando possivel.
- Sem coordenadas sensiveis em logs de aplicacao por padrao.

### Metricas

- Taxa de ingestao de telemetria.
- Atraso entre `collected_at` e `received_at`.
- Dispositivos offline.
- Comandos por estado.
- Comandos expirados/falhos.
- Tempo de geracao de PDF.
- Tamanho da fila de jobs.
- Erros de autenticacao e pareamento.

### Tracing

- OpenTelemetry no backend.
- Trace para ingestao, comando remoto e geracao de PDF.
- Exportador local opcional para Grafana stack.

## 13. Deploy Self-Hosted

### Perfil MVP simples

```text
docker-compose.yml
  reverse-proxy
  web-admin
  api
  worker
  postgres-postgis
  keycloak-ou-authentik
```

### Perfil soberano com mapas

```text
docker-compose.yml
  reverse-proxy
  web-admin
  api
  worker
  postgres-postgis
  keycloak-ou-authentik
  tileserver-gl
  openmaptiles-data
  prometheus
  grafana
```

### Recomendacoes operacionais

- Caddy e a opcao mais simples para TLS automatico.
- Traefik e adequado se a instalacao precisar de roteamento mais dinamico.
- PostgreSQL deve ter backup automatizado e teste de restore.
- Migracoes devem rodar com Flyway ou Liquibase.
- Configuracao deve ser feita por variaveis de ambiente documentadas.
- Instancia deve expor tela de saude operacional para administrador tecnico.

## 14. Decisoes Tecnicas

| Tema | Decisao | Racional |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 | Stack madura, segura e previsivel para self-hosted. |
| Web | Angular + TypeScript | Boa base para hub operacional, mapa e estado em tempo real. |
| Banco | PostgreSQL + PostGIS | Relacional robusto com geodados nativos. |
| Mapa | MapLibre GL JS | Open source, compatibilidade com tiles vetoriais e soberania. |
| Tiles | OpenMapTiles/TileServer GL | Evita dependencia direta de provedor fechado. |
| PDF | Chromium/Playwright | Renderiza HTML/CSS e mapas com maior fidelidade visual. |
| Tempo real | SSE no MVP | Mais simples de operar quando a web apenas recebe eventos. |
| Comando remoto | Fila persistente no PostgreSQL | Simples, auditavel e suficiente para MVP. |
| IdP | Keycloak ou authentik | OIDC self-hosted com MFA/WebAuthn. |
| Agente inicial | Android Kotlin | Melhor chance de validar telemetria, background e comandos minimos. |
| Agente computador | Rust 1.97 + Tauri 2.11.5 opcional | Rust reduz consumo e aproxima o agente das APIs nativas; Tauri entra apenas para tray/configuracao, sem duplicar a web admin. |

## 15. Roadmap Arquitetural

### MVP

- Monolito modular Spring Boot.
- Worker separado apenas se PDF e jobs afetarem latencia da API.
- Fila persistente em PostgreSQL.
- SSE para atualizacao da web.
- Agente Android Kotlin.
- PDF operacional com hash.
- Auditoria append-only.

### Fase 2

- Hash chain para eventos criticos.
- Assinatura digital, PDF/A e carimbo temporal.
- POCs de capacidade por plataforma desktop usando o agente Rust.
- Agente desktop em Rust com servico/daemon por SO e tray Tauri opcional.
- Zonas seguras.
- Retencao configuravel por tipo de dado.
- Observabilidade empacotada com Grafana/Prometheus.
- Push/wake-up por provedor com payload minimo.

### Backlog futuro

- Permissoes granulares.
- Multiusuario.
- Multi-instancia gerenciada.
- Integracoes externas.
- Mapa offline completo.
- Arquitetura multi-tenant, se o produto virar SaaS.

## 16. Lacunas Abertas

- Definir se o bloqueio Android do MVP exige Device Owner/Profile Owner ou aceita modo limitado do agente.
- Definir expiracao padrao de comandos remotos.
- Definir raio e tolerancia de parada.
- Definir politica de retencao de telemetria, auditoria e PDFs.
- Definir formato probatorio minimo do PDF.
- Definir fluxo de recuperacao de acesso quando o dispositivo principal foi roubado.
- Definir se tiles self-hosted sao obrigatorios no MVP ou perfil recomendado.
- Definir matriz de capacidades dos agentes desktop por sistema operacional: lock real, alarme, coleta de rede, instalacao, autostart e atualizacao.
- Definir se o fornecedor do SimpleGuard tera qualquer acesso remoto a instancias self-hosted.

## 17. Criterios de Pronto da Arquitetura Para Implementacao

- Repositorio conter skeleton backend Spring Boot com modulos separados.
- Repositorio conter skeleton Angular com hub operacional.
- Docker Compose local subir API, web, PostgreSQL/PostGIS e IdP.
- Banco ter migracoes iniciais para dispositivos, telemetria, comandos, incidentes, auditoria e relatorios.
- API validar OIDC e expor health checks.
- Agente Android conseguir parear e enviar telemetria simulada ou real.
- Web receber atualizacoes em tempo real via SSE.
- Comando remoto ter fluxo completo de estados, mesmo que a execucao real ainda seja POC.
- PDF ser gerado a partir de um incidente com dados de teste.
- Auditoria registrar pareamento, telemetria relevante, comando, alerta e relatorio.
