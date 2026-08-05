# Task 7.1: Criar Comando Remoto com Estado Inicial

Fase: MVP

Como administrador, quero solicitar um comando remoto com validade e alvo claros, para que a resposta seja auditavel desde o primeiro momento.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar comando `LOCK_DEVICE` ou `ALARM`.
- Exigir confirmacao explicita.
- Registrar `command_id`, tipo, alvo, validade e estado `requested`.

Testes unitarios:
- Backend: comando valido, dispositivo offline, nao pareado e ausencia de confirmacao.
- Frontend: dialog critico, cancelar e confirmar.

Cenarios de validacao manual:
- Solicitar bloqueio para dispositivo online e offline.
- Verificar estado inicial e auditoria.

Criterio de conclusao:
- Comando remoto nasce auditado e nunca sem confirmacao.
