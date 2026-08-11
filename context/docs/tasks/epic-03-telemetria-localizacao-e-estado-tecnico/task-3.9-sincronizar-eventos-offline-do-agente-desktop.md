# Task 3.9: Sincronizar Eventos Offline do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como administrador, quero que eventos coletados offline pelo agente desktop sejam enviados apos reconexao, para preservar horario real de coleta e evitar perda de telemetria.

Esta task cria o equivalente desktop da fila offline Android, respeitando o core Rust e as diferencas de Windows, Linux e macOS.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Implementar fila local desktop no core Rust para eventos de localizacao e telemetria tecnica.
- [ ] Persistir `collected_at` separado do momento de envio.
- [ ] Fazer retry apos reconexao.
- [ ] Limpar eventos locais apos sucesso confirmado pela API.
- [ ] Preservar ordem cronologica sem corromper eventos fora de ordem.
- [ ] Tratar duplicidade, falha parcial, evento invalido e dispositivo revogado.
- [ ] Expor quantidade de eventos pendentes para a UI da task 3.6.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- `task-3.3-sincronizar-eventos-offline-do-agente-android.md`
- `task-3.7-coletar-e-enviar-localizacao-do-agente-desktop.md`
- `task-3.8-coletar-e-enviar-telemetria-tecnica-do-agente-desktop.md`

Notas tecnicas:
- Reutilizar o core Rust iniciado na task 2.4.
- Nao implementar UI rica, comandos remotos ou relatorios nesta task.
- A fila local deve ser simples, auditavel e recuperavel apos reinicio do agente.

## Dependencias e Bloqueios

- Depende da task 2.4 para identidade/chave do agente desktop.
- Depende das tasks 3.7 e 3.8 para tipos de evento desktop.
- Bloqueio: se persistencia local segura ainda nao estiver definida, registrar decisao tecnica antes de fechar.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust: enfileirar evento, retry, sucesso com limpeza, falha parcial e ordem cronologica.
- [ ] Rust: evento invalido nao bloqueia indefinidamente eventos validos.

## Cenarios de Validacao Manual

- [ ] Simular offline, gerar eventos desktop e confirmar fila local.
- [ ] Reconectar e confirmar envio na API.
- [ ] Simular falha parcial e confirmar retry apenas do que ficou pendente.

## Criterio de Conclusao

- Eventos desktop offline sincronizam sem perder origem temporal nem inventar dados.
