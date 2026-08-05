# Task 7.5: Mostrar Estado de Comando na Web e no Agente

Fase: MVP

Como administrador, quero ver o estado de cada comando na web e no agente, para nao confundir solicitacao com execucao real.

Referencias:
- `docs/ux/simpleguard-ux-plataforma-web.md`
- `docs/ux/simpleguard-ux-app-celular.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Exibir todos os estados de comando na web.
- Exibir comando recebido/falha no Android.
- Mostrar motivo tecnico simples quando disponivel.

Testes unitarios:
- Frontend web: renderizacao de todos os estados.
- Android: tela de comando recebido e falha.

Cenarios de validacao manual:
- Carregar fixture com todos os estados.
- Validar textos na web e Android.

Criterio de conclusao:
- Usuario entende se comando foi solicitado, entregue, executado, confirmado, falhou ou expirou.
