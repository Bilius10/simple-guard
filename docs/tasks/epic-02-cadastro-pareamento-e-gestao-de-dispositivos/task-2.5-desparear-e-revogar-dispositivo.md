# Task 2.5: Desparear e Revogar Dispositivo

Fase: MVP

Como administrador, quero desparear um dispositivo e revogar suas chaves, para impedir envio de dados e recebimento de comandos por agente removido.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Revogar chaves ativas.
- Atualizar estado do dispositivo.
- Rejeitar telemetria posterior.
- Exibir confirmacao perigosa na web.

Testes unitarios:
- Backend: revogacao, idempotencia e telemetria rejeitada apos revogacao.
- Frontend: confirmacao, sucesso e falha.

Cenarios de validacao manual:
- Desparear dispositivo.
- Tentar enviar telemetria com credencial antiga e confirmar rejeicao.

Criterio de conclusao:
- Dispositivo revogado nao opera mais na instancia.
