# Task 2.2: Gerar Sessao de Pareamento

Fase: MVP

Como administrador, quero gerar um codigo ou QR de pareamento com validade curta, para vincular apenas dispositivo em posse fisica.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar sessoes curtas de pareamento.
- Exibir codigo/QR e expiracao na web.
- Auditar criacao e expiracao.

Testes unitarios:
- Backend: sessao valida, expiracao, reutilizacao bloqueada e dispositivo ja pareado.
- Frontend: aguardando, expirado e erro.

Cenarios de validacao manual:
- Gerar codigo para dispositivo pendente.
- Aguardar expiracao e confirmar que o codigo nao pode ser reutilizado.

Criterio de conclusao:
- Pareamento usa codigo temporario auditavel e nao reutilizavel.
