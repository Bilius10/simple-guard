# Task 6.1: Abrir Incidente para um Dispositivo

Fase: MVP

Como administrador, quero abrir um incidente para um dispositivo, para organizar a resposta a perda ou roubo.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar incidente ativo vinculado ao dispositivo.
- Registrar abertura na auditoria.
- Exibir incidente ativo no hub.

Testes unitarios:
- Backend: criacao, dispositivo inexistente e incidente duplicado ativo.
- Frontend: acao de abrir incidente e erro.

Cenarios de validacao manual:
- Abrir incidente para dispositivo cadastrado.
- Confirmar status ativo no hub.

Criterio de conclusao:
- Incidente ativo nasce auditado e visivel.
