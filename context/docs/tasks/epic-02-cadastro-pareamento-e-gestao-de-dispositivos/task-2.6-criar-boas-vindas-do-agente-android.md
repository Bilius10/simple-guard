# Task 2.6: Criar Boas-vindas do Agente Android

Fase: MVP

## Contexto e Objetivo

Como usuario do dispositivo monitorado, quero entender a funcao do agente Android antes do pareamento, para saber que o app conecta o dispositivo a uma instancia SimpleGuard autorizada.

Esta task existe para mapear a tela `Mobile Agent / 01 - Boas-vindas` prevista no UX/Figma sem misturar com pareamento, telemetria ou comandos.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem estado claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Implementar tela `Mobile Agent / 01 - Boas-vindas`.
- [x] Exibir titulo `Agente SimpleGuard`.
- [x] Exibir texto curto: `Este app conecta este dispositivo a sua instancia SimpleGuard.`
- [x] Exibir lista objetiva: localizacao/telemetria, comandos remotos suportados e sincronizacao apos reconexao.
- [x] Exibir acao `Iniciar pareamento`.
- [x] Navegar da tela de boas-vindas para a tela `Mobile Agent / 02 - Pareamento` ja existente.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/ux/simpleguard-ux-app-celular.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Notas tecnicas:
- Seguir o mesmo visual do app Android ja aprovado na task 2.3.
- Nao prometer recuperacao garantida.
- Nao criar mapa, painel administrativo, comandos, permissoes ou telemetria nesta task.

## Dependencias e Bloqueios

- Depende da task 2.3 para reaproveitar a tela de pareamento como destino.
- Bloqueio: se a navegacao local do app ainda nao existir, criar estrutura minima somente para alternar boas-vindas e pareamento.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [x] Android: renderizacao dos textos de boas-vindas.
- [x] Android: acao `Iniciar pareamento` troca para a tela de pareamento.

## Cenarios de Validacao Manual

- [x] Abrir app sem pareamento e confirmar tela de boas-vindas.
- [x] Acionar `Iniciar pareamento` e confirmar abertura da tela de pareamento.

## Criterio de Conclusao

- Usuario entende a funcao do agente antes de iniciar o pareamento.
