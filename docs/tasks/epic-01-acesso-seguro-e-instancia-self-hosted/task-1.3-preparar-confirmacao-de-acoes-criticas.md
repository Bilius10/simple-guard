# Task 1.3: Preparar Confirmacao de Acoes Criticas

Fase: MVP

Como administrador, quero confirmar acoes criticas antes de executa-las, para evitar bloqueio ou alarme por acidente.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar contrato reutilizavel de confirmacao critica.
- Preparar suporte futuro a reautenticacao/step-up.
- Implementar dialog visual reutilizavel na web.

Testes unitarios:
- Backend: politica exige confirmacao para acao critica.
- Frontend: abrir dialog, cancelar, confirmar e exibir erro.

Cenarios de validacao manual:
- Clicar em acao critica simulada, cancelar e confirmar que nada foi executado.
- Repetir a acao e confirmar, validando emissao do evento esperado.

Criterio de conclusao:
- Nenhuma acao critica pode ser executada sem confirmacao explicita.
