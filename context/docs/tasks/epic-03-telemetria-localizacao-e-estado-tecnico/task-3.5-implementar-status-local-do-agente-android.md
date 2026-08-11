# Task 3.5: Implementar Status Local do Agente Android

Fase: MVP

## Contexto e Objetivo

Como usuario do dispositivo monitorado, quero ver se o agente Android esta ativo, offline ou limitado, para saber que o dispositivo esta pareado e monitorado.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Implementar tela/status local do agente.
- [ ] Implementar tela `Mobile Agent / 03 - Permissoes` com lista de permissoes, estados e acoes curtas.
- [ ] Implementar tela `Mobile Agent / 04 - Status Ativo` com estado geral, ultima sincronizacao, bateria, rede, fila local e instancia pareada.
- [ ] Implementar tela `Mobile Agent / 06 - Offline / Fila Local` quando houver perda de conexao ou eventos pendentes.
- [ ] Mostrar ultima sincronizacao e fila local.
- [ ] Indicar permissoes pendentes, offline e falha.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/ux/simpleguard-ux-app-celular.md`
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

- [ ] Android: mapeamento de estados e renderizacao de fila local.
- [ ] Android: renderizacao da tela de permissoes com estados `concedida`, `pendente` e `bloqueada`.
- [ ] Android: renderizacao da tela de status ativo.
- [ ] Android: renderizacao do estado offline/fila local.

## Cenarios de Validacao Manual

- [ ] Revogar permissao de localizacao.
- [ ] Desconectar internet e validar estados na UI.
- [ ] Gerar evento local pendente e confirmar exibicao na fila local.

## Criterio de Conclusao

- Usuario identifica claramente estado operacional do agente.
