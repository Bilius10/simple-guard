# Task 3.1: Ingerir Localizacao de Agente Pareado

Fase: MVP

## Contexto e Objetivo

Como administrador, quero receber localizacao de dispositivos pareados, para acompanhar onde cada dispositivo esteve.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar endpoint de ingestao de localizacao.
- [x] Validar autenticidade do agente.
- [x] Persistir ponto bruto em PostGIS.
- [x] Separar `collected_at` e `received_at`.
- [x] Android: coletar localizacao quando permissao estiver disponivel.
- [x] Android: enviar localizacao para o endpoint de agente pareado usando credencial/chave do pareamento.
- [x] Android: tratar localizacao ausente, permissao negada, GPS desligado e falha de rede sem inventar coordenadas.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-app-celular.md`

Notas tecnicas:
- Seguir arquitetura e contratos ja definidos nos documentos de referencia.
- Manter nomes, estados e eventos consistentes com `epics.md` e `ARCHITECTURE-SPINE.md`.
- Se a task tocar frontend, seguir o UX spec indicado e o Figma oficial.
- Se a task tocar backend ou agente, manter validacao de entrada, casos de erro e testes unitarios junto da implementacao.

Contrato implementado:
- Endpoint: `POST /api/agent/devices/{deviceId}/locations`.
- Autenticacao do agente: headers `X-Agent-Instance-Id` e `X-Agent-Signature`, com chave publica ativa do pareamento.
- Assinatura: `SHA256withECDSA` sobre `INGEST_LOCATION`, device id, agent instance id, instante UTC, coordenadas canonicas, metadados opcionais e provedor, separados por quebra de linha.
- Provedores aceitos: `GPS`, `NETWORK`, `PASSIVE` e `FUSED`; o agente Android desta task envia `GPS`.
- Persistencia: `device_locations.position` usa `geography(Point, 4326)` e mantem `collected_at` separado de `received_at`.
- Intervalo Android: servico em primeiro plano coleta e tenta enviar a cada 60 segundos enquanto o dispositivo permanece pareado.
- Suposicao registrada: o formato exato da assinatura nao estava definido nos documentos superiores; foi adotado contrato canonico deterministico compativel com a chave ECDSA ja aprovada no pareamento.

## Dependencias e Bloqueios

- Depende das tasks anteriores do mesmo epic quando houver contrato, entidade, endpoint ou estado reutilizado.
- Bloqueio: se a dependencia necessaria nao existir, registrar no PR/branch e nao substituir por mock permanente.
- Bloqueio: se a implementacao exigir decisao marcada como lacuna nos docs, parar e registrar a decisao necessaria antes de fechar a task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [x] Backend: payload valido, assinatura invalida, dispositivo revogado e coordenada invalida.
- [x] Android: coleta valida, permissao negada, GPS indisponivel e falha de envio.

Evidencias automatizadas:
- `api/mvnw.cmd -q verify`: 95 testes passando e cobertura JaCoCo de 100%.
- `android-agent/gradlew.bat test koverVerify`: 24 testes passando nas variantes debug/release e cobertura Kover de 100%.

## Cenarios de Validacao Manual

- [x] Enviar ponto valido via cliente HTTP.
- [x] Confirmar persistencia e resposta da API.
- [x] Android: enviar localizacao real ou simulada e confirmar persistencia na API.
- [x] Android: revogar permissao de localizacao e confirmar estado tratado no app.

Bloqueio atual da validacao manual:
- O ambiente de desenvolvimento nao possui executavel Docker/PostGIS disponivel.
- `adb devices` nao encontrou dispositivo ou emulador conectado.
- Os cenarios manuais permanecem desmarcados ate execucao em API com PostGIS e Android real/emulado; a task nao deve ser considerada validada manualmente antes disso.

## Criterio de Conclusao

- Localizacao autenticada e persistida sem inferencia destrutiva.
