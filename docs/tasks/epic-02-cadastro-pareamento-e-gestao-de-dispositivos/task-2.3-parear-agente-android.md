# Task 2.3: Parear Agente Android

Fase: MVP

Como administrador, quero concluir o pareamento do agente Android com a instancia, para que o celular envie telemetria autorizada.

Referencias:
- `docs/ux/simpleguard-ux-app-celular.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Agente Android envia codigo, identificacao e chave publica.
- API valida sessao e registra chave.
- UI Android mostra estados de pareamento.

Testes unitarios:
- Backend: codigo invalido, expirado, chave ausente e sucesso.
- Android: aguardando, validando, falha e pareado.

Cenarios de validacao manual:
- Parear agente Android usando codigo valido.
- Confirmar que o dispositivo aparece como pareado na web.

Criterio de conclusao:
- Agente Android pareado recebe identidade confiavel na instancia.
