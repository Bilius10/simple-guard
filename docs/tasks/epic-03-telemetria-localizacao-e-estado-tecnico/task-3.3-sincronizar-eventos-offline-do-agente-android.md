# Task 3.3: Sincronizar Eventos Offline do Agente Android

Fase: MVP

Como administrador, quero que eventos coletados offline sejam sincronizados depois, para preservar o horario real do incidente.

Referencias:
- `docs/ux/simpleguard-ux-app-celular.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Implementar fila local Android.
- Sincronizar lote apos reconexao.
- Preservar horario original de coleta.

Testes unitarios:
- Backend: lote fora de ordem, duplicado e parcialmente invalido.
- Android: fila local, retry e limpeza apos sucesso.

Cenarios de validacao manual:
- Simular offline, gerar eventos, reconectar e validar ordem cronologica.

Criterio de conclusao:
- Eventos offline sincronizam sem perder origem temporal.
