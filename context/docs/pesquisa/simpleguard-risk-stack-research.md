---
title: "Pesquisa: Riscos e Stack Tecnica do SimpleGuard"
project_name: "SimpleGuard"
status: "draft"
created: "2026-08-04"
updated: "2026-08-04"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/produto/simpleguard-product-brief.md"
  - "docs/discovery/simpleguard-discovery.md"
research_scope:
  - "riscos regulatorios, operacionais e de privacidade"
  - "stack para mapa, geolocalizacao, PDF, autenticacao forte e comando remoto"
---

# Pesquisa: Riscos e Stack Tecnica do SimpleGuard

Este documento nao e parecer juridico. Ele consolida riscos e escolhas tecnicas provaveis para orientar PRD, UX e arquitetura.

## Resumo Executivo

O SimpleGuard deve ser tratado como produto de alto risco de privacidade, mesmo quando for self-hosted, porque combina localizacao, telemetria, comando remoto, historico e evidencias. A decisao arquitetural mais importante e separar claramente tres responsabilidades: controle do usuario, agente no dispositivo e servico de coordenacao.

Para o MVP, a stack aprovada e:

- **API/backend:** Java 21 + Spring Boot 3.
- **Plataforma web do administrador:** Angular + TypeScript.
- **SDK/agente Android:** Kotlin, com escopo leve para telemetria e comandos minimos.
- **Agentes de computador:** Rust como servico/daemon nativo por sistema operacional, com Tauri opcional apenas para tray/configuracao local.
- **Mapa:** MapLibre GL JS + tiles vetoriais OSM/OpenMapTiles, preferencialmente self-hosted ou provedor pago com contrato claro.
- **Geodados:** PostgreSQL + PostGIS.
- **Telemetria temporal:** PostgreSQL particionado ou TimescaleDB se o volume justificar.
- **Geolocalizacao do dispositivo:** agente Android nativo em Kotlin no MVP; navegador/PWA nao deve ser assumido como suficiente para rastreamento em background.
- **PDF:** HTML/CSS renderizado com Playwright/Chromium para relatorio visual; validar PDF/A com veraPDF se evidencia de longo prazo virar requisito.
- **Autenticacao forte:** OIDC com IdP self-hosted, WebAuthn/passkeys para login e reautenticacao de comando critico.
- **Canal de comando remoto:** comandos assincronos, assinados, idempotentes, com fila e estados auditaveis. Para mobile, considerar FCM/APNs como wake-up, nao como unica fonte de verdade.

## Riscos Regulatorios e de Privacidade

### 1. LGPD e base legal

Risco: tratar geolocalizacao, historico, identificadores de dispositivo, dados de rede e eventos de seguranca sem finalidade clara, minimizacao e transparencia.

Direcao:

- Documentar finalidade por dado: localizacao, bateria, rede, identificador do dispositivo, eventos de bloqueio, logs e PDF.
- Evitar coleta continua sem necessidade operacional explicita.
- Definir base legal por finalidade. Para uso do proprio titular, consentimento/execucao de contrato podem ser caminhos, mas precisam ser validados juridicamente.
- Tratar geolocalizacao como dado de alto impacto pratico, ainda que a LGPD nao a liste expressamente como dado sensivel.

Fontes: LGPD arts. 1, 3, 4, 6, 7 e 46; ANPD RIPD.

### 2. Excecao domestica nao resolve produto comercial

Risco: assumir que "self-hosted" elimina LGPD.

Direcao:

- A LGPD nao se aplica ao tratamento por pessoa natural para fins exclusivamente particulares e nao economicos, mas essa excecao nao cobre automaticamente fornecedor, suporte, telemetria, cloud, updates, analytics ou instancia gerenciada.
- Se o SimpleGuard como fornecedor acessar dados, hospedar instancia, receber logs ou operar push/telemetria, precisa mapear papel de controlador/operador.
- Se o usuario operar tudo localmente sem acesso do fornecedor, o risco regulatorio do fornecedor diminui, mas continuam riscos de design, abuso, seguranca e termos de uso.

### 3. RIPD e alto risco

Risco: lancar sem avaliacao estruturada de impacto.

Direcao:

- Produzir RIPD ou DPIA equivalente antes do MVP publico.
- Incluir tipos de dados, finalidade, fonte de coleta, compartilhamentos, retencao, riscos ao titular, salvaguardas e mitigacoes.
- Atualizar o RIPD quando entrar usuario secundario, integracao externa, relatorio para terceiros ou instancia gerenciada.

### 4. Incidentes de seguranca

Risco: vazamento de localizacao/historico ou tomada de conta permitir perseguicao, extorsao ou comando remoto indevido.

Direcao:

- Plano de resposta a incidente desde o MVP.
- Registro de incidentes com dados pessoais por prazo minimo exigido pela regulacao aplicavel.
- Comunicacao a ANPD e titular quando houver risco ou dano relevante.
- Criptografia forte, segregacao por instancia/tenant, logs imutaveis e rotacao de chaves.

### 5. Abuso, vigilancia e stalking

Risco: o produto ser usado para rastrear terceiros sem autorizacao.

Direcao:

- MVP com apenas administrador/proprietario nao elimina risco de abuso sobre dispositivos de terceiros.
- Exigir prova de controle/pareamento fisico do dispositivo.
- Exibir indicador local no agente quando adequado, termos claros de uso autorizado e mecanismos de remocao/despareamento.
- Bloquear casos de uso de vigilancia oculta.

### 6. Evidencia e cadeia de custodia

Risco: PDF parecer "prova" quando na verdade e apenas relatorio gerado por sistema sem garantias de integridade.

Direcao:

- Separar no relatorio: dado coletado, inferencia do sistema, acao do usuario e estado do comando.
- Registrar hash do relatorio e dos eventos usados na geracao.
- Manter logs append-only.
- Avaliar assinatura digital, carimbo temporal e PDF/A na fase 2 se o posicionamento probatorio for central.

### 7. Provedor de mapas e vazamento indireto

Risco: enviar coordenadas e padroes de uso para servidores de tile/geocoding de terceiros.

Direcao:

- Nao usar `tile.openstreetmap.org` para producao com uso intenso, offline ou comercial sem observar politica.
- Para soberania e privacidade, preferir tiles self-hosted ou provedor contratado.
- Evitar enviar coordenadas confidenciais a geocoding externo sem base legal, politica e contrato.

### 8. Transferencia internacional e subprocessadores

Risco: FCM, APNs, cloud, mapas, observabilidade e e-mail gerarem compartilhamento internacional de dados.

Direcao:

- Minimizar payload em push: usar apenas wake-up/command id, sem localizacao ou detalhes sensiveis.
- Documentar subprocessadores e regioes.
- Definir se o MVP permite operar 100% self-hosted sem servicos externos, exceto dependencias inevitaveis por plataforma.

## Riscos Operacionais

- **Bloqueio remoto depende da plataforma:** Android e Apple restringem APIs de lock a cenarios de device admin, device owner, profile owner, MDM, supervision ou permissoes especiais. PWA/web app nao bloqueia aparelho por si so.
- **Offline e atraso sao normais:** comando remoto deve aceitar estado pendente, expirado, falho, entregue e confirmado.
- **Localizacao de 1 minuto consome bateria:** intervalo deve ser configuravel ou adaptativo futuramente.
- **Precisao de parada exige tolerancia:** "mesma posicao por 10 minutos" precisa de raio, margem de erro GPS e regra contra jitter.
- **Fila local pode ser adulterada:** dados offline no agente precisam de armazenamento criptografado, sequencia monotona e assinatura/HMAC por evento.
- **Alarme remoto pode criar risco fisico:** exigir confirmacao, limite de repeticao e estado claro.
- **Self-hosted aumenta variabilidade:** backup, TLS, DNS, atualizacao, observabilidade e hardening precisam ser simples.
- **Perda do dispositivo principal afeta acesso:** recuperacao precisa ser desenhada antes de comandos criticos em producao.

## Stack Aprovada Para MVP

### API e Backend

Decisao:

- Java 21 + Spring Boot 3.

Racional:

- Stack madura para API self-hosted, seguranca, auditoria, jobs, integracao com PostgreSQL/PostGIS e operacao previsivel.
- Ecossistema forte para autenticacao, observabilidade, validacao, migracoes e testes.

Cuidados:

- Separar comandos remotos, ingestao de telemetria, auditoria e geracao de PDF em modulos bem definidos.
- Evitar acoplamento entre regra de produto e detalhes especificos do agente Android.

### Plataforma Web do Administrador

Decisao:

- Angular + TypeScript.

Racional:

- Entrega a superficie principal onde o administrador cadastra, visualiza e opera dispositivos.
- Combina bem com mapa operacional, paineis tecnicos, logs e atualizacao em tempo real.
- Comunica com a API Java/Spring Boot via HTTP e WebSocket/SSE.
- Evita transformar o hub operacional em um instalavel desktop antes de validar o produto.

Cuidados:

- A plataforma web e superficie operacional do administrador, nao agente de bloqueio do dispositivo.
- O hub web deve ser responsivo para uso em computador e pode evoluir para PWA se isso for necessario.

### SDK/Agente Android

Decisao:

- Kotlin para o agente Android inicial.

Racional:

- Plataforma inicial mais indicada para POC de telemetria, alarme e bloqueio, por ter APIs documentadas de device policy/device management.
- Permite validar rastreamento em background, armazenamento offline local e canal de comando remoto.

Cuidados:

- Nao prometer paridade com iOS, Windows, macOS ou Linux antes de POCs especificas.
- Validar se o MVP exige bloqueio real do sistema operacional ou modo limitado de bloqueio pelo agente.
- Manter o agente Android leve: coleta, armazenamento local minimo, sincronizacao, alarme e execucao de comandos suportados.

### SDKs/Agentes de Computador

Decisao:

- Tecnologia a definir por sistema operacional, priorizando agentes leves.

Racional:

- Os agentes de computador nao devem replicar a plataforma de administracao.
- O escopo esperado e enviar telemetria, manter identificacao/pareamento e executar comandos suportados pelo sistema operacional.

Cuidados:

- Evitar UI rica nos agentes.
- Definir por POC quais sistemas entram primeiro: Windows, Linux ou macOS.
- Validar permissoes reais de bloqueio, alarme, coleta de rede e localizacao por sistema operacional.

### Mapa e UX Operacional

Recomendacao:

- MapLibre GL JS na plataforma web Angular.
- Tiles vetoriais OSM/OpenMapTiles.
- TileServer GL ou provedor compativel para servir tiles.
- Estilo customizado retro-tech/pixel-art em cima de tiles vetoriais.

Racional:

- MapLibre e open source, renderiza mapas interativos via WebGL e suporta vector tiles.
- OpenMapTiles permite gerar/servir tiles proprios, mantendo atribuicao OSM/OpenMapTiles.
- Evita dependencia direta de Google Maps/Mapbox e combina melhor com self-hosted.

Cuidados:

- Sempre exibir atribuicao.
- Nao usar servidores publicos do OSM como backend de producao.
- Definir cache e limite de uso.

### Geodados e Historico

Recomendacao:

- PostgreSQL como banco principal.
- PostGIS para coordenadas, distancias, trajetorias, raio de parada e consultas espaciais.
- TimescaleDB opcional se houver alto volume de telemetria temporal.

Modelo inicial:

- `devices`
- `device_locations`
- `device_events`
- `remote_commands`
- `incident_reports`
- `audit_log`

Cuidados:

- Usar `geography(Point, 4326)` para calculos em lat/lon quando distancia real importar.
- Indexar por `device_id`, tempo e geografia.
- Separar telemetria bruta de inferencias, como parada detectada.

### Geolocalizacao

Recomendacao:

- A plataforma web Angular nao deve ser tratada como fonte principal de localizacao do dispositivo monitorado.
- Rastreamento do dispositivo monitorado deve vir do agente Android nativo em Kotlin no MVP.
- A plataforma web pode usar Geolocation API apenas para localizar o administrador, se necessario.

Racional:

- Geolocation API em navegador exige HTTPS e permissao explicita, e nao e base confiavel para coleta persistente em background.
- Mobile/desktop exigem capacidades nativas para permissao, background, bateria, rede e lock.

### PDF de Evidencias

Recomendacao MVP:

- Gerar relatorio em HTML/CSS server-side.
- Renderizar PDF com Playwright/Chromium quando for necessario preservar o visual do mapa e do relatorio.
- Armazenar snapshot estatico do mapa usado no PDF.

Fase 2:

- Validar PDF/A com veraPDF.
- Avaliar assinatura digital, hash do pacote de evidencias e carimbo temporal.

Alternativa:

- WeasyPrint e bom para documentos HTML/CSS mais tradicionais, mas pode ser menos adequado se o relatorio depender de renderizacao exata de mapa/canvas/WebGL.

### Autenticacao Forte

Recomendacao:

- Usar OIDC com IdP self-hosted: Keycloak ou authentik.
- Habilitar WebAuthn/passkeys para login forte.
- Exigir reautenticacao para bloqueio remoto, alarme e exportacao sensivel.
- Manter recovery codes/chave fisica como fluxo separado de emergencia.

Cuidados:

- Sessao curta ou reautenticacao para acoes criticas.
- Cookies seguros, TLS obrigatorio e protecao contra CSRF.
- Nao usar SMS OTP como fator principal para o caso em que o celular foi roubado.

### Canal de Comando Remoto

Recomendacao:

- Backend registra comando com `command_id`, tipo, alvo, payload minimo, validade, assinatura e estado.
- Agente do dispositivo busca ou recebe notificacao de comando.
- FCM/APNs podem acordar o app/agente, mas o comando real deve ser consultado e validado no backend.
- MQTT sobre TLS e boa opcao para agentes sempre conectados ou cenarios IoT/desktop.
- WebSocket/SSE servem bem para atualizar o painel do administrador, nao como unica camada de comando para mobile.

Estados minimos:

- `requested`
- `queued`
- `delivered`
- `executed`
- `confirmed`
- `failed`
- `expired`
- `cancelled`

Regras:

- Comandos devem ser idempotentes.
- Cada comando deve ter expiracao.
- Payload deve ser minimo.
- O agente deve validar assinatura, alvo, nonce e validade.
- Toda transicao deve gerar evento auditavel.

## Decisoes Aprovadas Para Arquitetura

- MVP: plataforma web Angular/TypeScript + API Java 21/Spring Boot 3 self-hosted + banco PostgreSQL/PostGIS + agente Android Kotlin.
- Nao prometer bloqueio remoto universal antes de validar plataformas alem de Android.
- Tratar Android como plataforma inicial para POC de bloqueio/alarme, porque ha APIs documentadas para device admin/device management.
- Tratar Apple como dependente de MDM/supervision/user enrollment; nao assumir paridade com Android.
- Usar MapLibre/OpenMapTiles para manter coerencia com soberania e custo previsivel.
- Projetar auditoria desde o inicio; nao adicionar depois.
- Projetar privacidade como produto: minimizacao, retencao, exportacao, exclusao e RIPD.

## Lacunas Tecnicas Para Fechar Antes do PRD Final

- O MVP Android exige lock real do sistema operacional ou aceita "modo bloqueio do agente"?
- O alarme remoto precisa tocar em background/tela bloqueada?
- Qual raio define "mesma posicao" para parada?
- O armazenamento offline local precisa sobreviver a reboot/desinstalacao?
- O PDF precisa ter valor probatorio formal ou apenas ser relatorio operacional?
- A instancia self-hosted tera update automatico?
- O produto tera modo 100% offline de mapas ou apenas mapas online com tiles privados?

## Fontes Consultadas

- LGPD, Lei 13.709/2018, texto compilado: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm
- LGPD art. 46, seguranca desde concepcao: https://normas.leg.br/?urn=urn%3Alex%3Abr%3Afederal%3Alei%3A2018-08-14%3B13709%21art46
- LGPD art. 48, comunicacao de incidente: https://normas.leg.br/?urn=urn%3Alex%3Abr%3Afederal%3Alei%3A2018-08-14%3B13709%402022-10-25%21art48
- ANPD, RIPD: https://www.gov.br/anpd/pt-br/canais_atendimento/agente-de-tratamento/relatorio-de-impacto-a-protecao-de-dados-pessoais-ripd
- ANPD, comunicacao de incidente: https://www.gov.br/anpd/pt-br/canais_atendimento/agente-de-tratamento/comunicado-de-incidente-de-seguranca-cis
- ANPD, Regulamento de Comunicacao de Incidente de Seguranca: https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-aprova-o-regulamento-de-comunicacao-de-incidente-de-seguranca
- MapLibre GL JS docs: https://maplibre.org/maplibre-gl-js/docs
- OpenStreetMap Tile Usage Policy: https://operations.osmfoundation.org/policies/tiles/
- OpenMapTiles docs: https://openmaptiles.org/docs/
- PostGIS docs, geography data type: https://postgis.net/docs/en/using_postgis_dbmanagement.html
- TimescaleDB hypertables: https://docs.timescale.com/use-timescale/latest/hypertables/
- MDN Geolocation API: https://developer.mozilla.org/en-US/docs/Web/API/Geolocation_API
- MDN Permissions API: https://developer.mozilla.org/en-US/docs/Web/API/Permissions_API
- Android DevicePolicyManager `lockNow`: https://developer.android.com/reference/android/app/admin/DevicePolicyManager
- Android Management API `issueCommand`: https://developers.google.com/android/management/reference/rest/v1/enterprises.devices/issueCommand
- Apple Device Lock MDM command: https://developer.apple.com/documentation/devicemanagement/device-lock-command
- Firebase Cloud Messaging docs: https://firebase.google.com/docs/cloud-messaging
- Apple Push Notification service docs: https://developer.apple.com/documentation/usernotifications/establishing-a-certificate-based-connection-to-apns
- FIDO passkeys: https://fidoalliance.org/passkeys/
- W3C WebAuthn Level 3: https://www.w3.org/TR/webauthn-3/
- OWASP ASVS: https://owasp.org/www-project-application-security-verification-standard/
- Playwright `page.pdf`: https://playwright.dev/docs/api/class-page
- WeasyPrint: https://weasyprint.org/
- veraPDF validation: https://docs.verapdf.org/validation/
- MQTT 5.0 OASIS Standard: https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html
- MDN WebSocket API: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
- MDN Server-Sent Events: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
- OpenTelemetry docs: https://opentelemetry.io/docs/what-is-opentelemetry/
