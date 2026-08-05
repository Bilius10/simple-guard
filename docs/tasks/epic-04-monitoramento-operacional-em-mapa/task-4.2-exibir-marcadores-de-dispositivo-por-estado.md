# Task 4.2: Exibir Marcadores de Dispositivo por Estado

Fase: MVP

Como administrador, quero ver dispositivos no mapa com estados visuais claros, para identificar seguro, atencao, alerta, offline e bloqueado.

Referencias:
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Implementar marcadores MapLibre.
- Mapear estados para cor/icone.
- Criar variantes `safe`, `watch`, `attention`, `alert`, `offline`, `locked`, `selected`, `stale-data`.

Testes unitarios:
- Frontend: mapeamento estado-cor-icone.

Cenarios de validacao manual:
- Carregar fixture com todos os estados e conferir marcadores.

Criterio de conclusao:
- Estados operacionais sao legiveis diretamente no mapa.
