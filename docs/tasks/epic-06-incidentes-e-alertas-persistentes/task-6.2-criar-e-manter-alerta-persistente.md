# Task 6.2: Criar e Manter Alerta Persistente

Fase: MVP

Como administrador, quero que alertas persistam ate minha confirmacao, para que eventos criticos nao desaparecam sozinhos.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar alerta vinculado a incidente/evento critico.
- Persistir alerta ate confirmacao manual.
- Renderizar banner/lista de alertas.

Testes unitarios:
- Backend: criacao, persistencia e confirmacao manual.
- Frontend: banner/lista e persistencia apos reload simulado.

Cenarios de validacao manual:
- Criar alerta e recarregar pagina.
- Confirmar que alerta continua ativo ate acao manual.

Criterio de conclusao:
- Alerta critico nunca some automaticamente.
