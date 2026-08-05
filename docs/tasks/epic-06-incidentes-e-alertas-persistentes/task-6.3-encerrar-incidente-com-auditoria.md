# Task 6.3: Encerrar Incidente com Auditoria

Fase: MVP

Como administrador, quero encerrar um incidente manualmente, para registrar a ocorrencia como concluida.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Encerrar incidente ativo.
- Registrar evento de auditoria.
- Definir comportamento para alertas vinculados no MVP.

Testes unitarios:
- Backend: encerramento, idempotencia e incidente inexistente.
- Frontend: confirmacao e estado encerrado.

Cenarios de validacao manual:
- Encerrar incidente ativo.
- Verificar que ele sai da lista de ativos e aparece como encerrado.

Criterio de conclusao:
- Incidente encerra somente por acao manual auditada.
