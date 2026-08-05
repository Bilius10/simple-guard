# Task 7.2: Enfileirar e Expirar Comandos

Fase: MVP

Como administrador, quero que comandos fiquem pendentes ou expirem com clareza, para saber quando uma acao nao chegou ao dispositivo.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`

Escopo:
- Implementar transicao `requested` -> `queued`.
- Implementar expiracao para `expired`.
- Registrar transicoes append-only.

Testes unitarios:
- Backend: transicoes validas, expiracao e transicao invalida.

Cenarios de validacao manual:
- Criar comando com validade curta.
- Confirmar expiracao no log.

Criterio de conclusao:
- Comando pendente expira de forma auditavel.
