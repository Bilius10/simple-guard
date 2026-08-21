# Task 3.2: Ingerir Telemetria Tecnica do Dispositivo

Fase: MVP

## Contexto e Objetivo

Como administrador, quero receber bateria, rede, sinal e estado tecnico, para entender a confiabilidade operacional do dispositivo.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar endpoint/servico de telemetria tecnica.
- [x] Persistir bateria, rede, sinal e permissoes quando disponiveis.
- [x] Representar dados ausentes como `null`.
- [x] Android: coletar bateria, rede, sinal e permissoes quando disponiveis.
- [x] Android: enviar telemetria tecnica para o endpoint de agente pareado usando credencial/chave do pareamento.
- [x] Android: tratar sensores/permissoes indisponiveis como dados ausentes, sem usar `0` como fallback.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/produto/simpleguard-prd.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-celular.md`

Notas tecnicas:
- Seguir arquitetura e contratos ja definidos nos documentos de referencia.
- Manter nomes, estados e eventos consistentes com `epics.md` e `ARCHITECTURE-SPINE.md`.
- Se a task tocar frontend, seguir o UX spec indicado e o Figma oficial.
- Se a task tocar backend ou agente, manter validacao de entrada, casos de erro e testes unitarios junto da implementacao.

Contrato implementado:
- Endpoint unico: `POST /api/agent/devices/{deviceId}/telemetry`.
- Envelope: `eventId` obrigatorio e blocos opcionais `location` e `technical`; ao menos um bloco deve existir.
- Idempotencia: `eventId` e a chave primaria nas tabelas `device_locations` e `device_telemetry`; reenvios retornam `200` com `duplicate: true` sem criar linhas.
- Persistencia: localizacao permanece em `device_locations` com PostGIS; bateria, carregamento, rede, sinal e permissoes ficam em `device_telemetry`.
- Autenticacao: uma assinatura `SHA256withECDSA` cobre o envelope canonico completo usando a chave ativa do pareamento.
- Android: um unico envio por ciclo de 60 segundos; telemetria tecnica continua sendo enviada quando localizacao ou permissao estiver indisponivel.
- Tela mobile: o diagnostico local exibe bateria, carregamento, rede, sinal, permissoes, disponibilidade de localizacao e resultado do ultimo envio.

## Dependencias e Bloqueios

- Depende das tasks anteriores do mesmo epic quando houver contrato, entidade, endpoint ou estado reutilizado.
- Bloqueio: se a dependencia necessaria nao existir, registrar no PR/branch e nao substituir por mock permanente.
- Bloqueio: se a implementacao exigir decisao marcada como lacuna nos docs, parar e registrar a decisao necessaria antes de fechar a task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [x] Backend: valores validos, ausentes, fora de faixa e duplicados.
- [x] Android: coleta valida, dados ausentes, bateria baixa e falha de envio.

Evidencias automatizadas:
- API: `mvnw.cmd verify`, com 98 testes passando e cobertura JaCoCo de 100%.
- Android: `gradlew.bat :app:koverVerifyDebug`, com 35 testes passando e limite de cobertura de 100%.
- Android: `gradlew.bat :app:lintDebug`, sem erros.

## Cenarios de Validacao Manual

- [x] Enviar telemetria com bateria baixa.
- [x] Confirmar estado tecnico salvo.
- [x] Android: enviar bateria/rede/sinal/permissoes e confirmar persistencia na API.
- [x] Android: simular dado tecnico indisponivel e confirmar `null` no backend.

Validacao manual pendente:
- Esta execucao nao possui dispositivo ou emulador Android conectado para produzir as evidencias dos quatro cenarios manuais.

## Criterio de Conclusao

- Dados tecnicos ficam persistidos e distinguem ausente de valor zero.
