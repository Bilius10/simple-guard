# Task 4.4: Atualizar Mapa e Painel via SSE

Fase: MVP

Como administrador, quero receber atualizacoes em tempo util no hub, para nao precisar recarregar a tela durante incidente.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar stream SSE de eventos operacionais.
- Atualizar mapa, painel e log resumido sem reload.
- Exibir conexao degradada quando o stream falhar.

Testes unitarios:
- Backend: publicacao SSE por tipo de evento.
- Frontend: recepcao, reconexao e estado de erro.

Cenarios de validacao manual:
- Enviar evento simulado e validar atualizacao visual imediata.

Criterio de conclusao:
- Hub reflete eventos novos em tempo util via SSE.
