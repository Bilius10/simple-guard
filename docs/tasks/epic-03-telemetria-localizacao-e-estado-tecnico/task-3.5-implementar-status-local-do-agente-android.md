# Task 3.5: Implementar Status Local do Agente Android

Fase: MVP

Como usuario do dispositivo monitorado, quero ver se o agente Android esta ativo, offline ou limitado, para saber que o dispositivo esta pareado e monitorado.

Referencias:
- `docs/ux/simpleguard-ux-app-celular.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Implementar tela/status local do agente.
- Mostrar ultima sincronizacao e fila local.
- Indicar permissoes pendentes, offline e falha.

Testes unitarios:
- Android: mapeamento de estados e renderizacao de fila local.

Cenarios de validacao manual:
- Revogar permissao de localizacao.
- Desconectar internet e validar estados na UI.

Criterio de conclusao:
- Usuario identifica claramente estado operacional do agente.
