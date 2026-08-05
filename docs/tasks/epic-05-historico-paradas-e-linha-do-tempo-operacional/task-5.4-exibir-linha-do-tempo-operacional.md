# Task 5.4: Exibir Linha do Tempo Operacional

Fase: MVP

Como administrador, quero ver eventos observados, inferencias e acoes em uma linha do tempo, para reconstruir o incidente com clareza.

Referencias:
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Consultar eventos por dispositivo/incidente.
- Renderizar timeline cronologica.
- Diferenciar `observed`, `inferred`, `user-action` e eventos de comando.

Testes unitarios:
- Backend: consulta agregada por incidente/dispositivo.
- Frontend: ordenacao, agrupamento visual e estado vazio.

Cenarios de validacao manual:
- Abrir timeline com eventos mistos.
- Validar ordem e classificacao visual.

Criterio de conclusao:
- Linha do tempo permite reconstruir eventos sem misturar fatos e inferencias.
