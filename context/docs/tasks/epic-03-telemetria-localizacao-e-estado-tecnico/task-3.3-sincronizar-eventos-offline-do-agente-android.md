# Task 3.3: Sincronizar Eventos Offline do Agente Android

Fase: MVP

## Contexto e Objetivo

Como administrador, quero que eventos coletados offline sejam sincronizados depois, para preservar o horario real do incidente.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Implementar fila local Android.
- [x] Sincronizar lote apos reconexao.
- [x] Preservar horario original de coleta.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/ux/simpleguard-ux-app-celular.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Notas tecnicas:
- Seguir arquitetura e contratos ja definidos nos documentos de referencia.
- Manter nomes, estados e eventos consistentes com `epics.md` e `ARCHITECTURE-SPINE.md`.
- Se a task tocar frontend, seguir o UX spec indicado e o Figma oficial.
- Se a task tocar backend ou agente, manter validacao de entrada, casos de erro e testes unitarios junto da implementacao.

## Dependencias e Bloqueios

- Depende das tasks anteriores do mesmo epic quando houver contrato, entidade, endpoint ou estado reutilizado.
- Bloqueio: se a dependencia necessaria nao existir, registrar no PR/branch e nao substituir por mock permanente.
- Bloqueio: se a implementacao exigir decisao marcada como lacuna nos docs, parar e registrar a decisao necessaria antes de fechar a task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [x] Backend: lote fora de ordem, duplicado e parcialmente invalido.
- [x] Android: fila local, retry e limpeza apos sucesso.

## Contrato Implementado

- Fila persistente privada em `filesDir/telemetry-offline-queue.json`, gravada por arquivo temporario e substituicao atomica.
- Retencao maxima de 7 dias pelo horario de entrada na fila (`queuedAt`).
- Limite operacional de 1.000 eventos; ao exceder, descartar primeiro os eventos mais antigos pelo horario original de coleta.
- Ordenacao de envio pelo menor `collectedAt` entre localizacao e telemetria tecnica, preservando o horario original no payload.
- Lotes de ate 100 eventos em `POST /api/agent/devices/{deviceId}/telemetry/batch`, com assinatura individual por evento.
- Resultados `ACCEPTED`, `DUPLICATE` e `INVALID` removem o evento da fila ativa.
- Resultados `UNAUTHORIZED`, `FAILED`, resposta ausente e falha de rede mantem o evento para retry e incrementam o diagnostico local.
- Reconexao detectada por `ConnectivityManager.NetworkCallback` dispara o escoamento da fila enquanto o foreground service estiver ativo.
- O ciclo periodico de 60 segundos permanece como fallback de coleta e sincronizacao.

## Cenarios de Validacao Manual

- [x] Simular offline, gerar eventos, reconectar e validar ordem cronologica.

## Criterio de Conclusao

- Eventos offline sincronizam sem perder origem temporal.
