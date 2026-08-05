# Task 7.3: Agente Android Buscar e Validar Comando

Fase: MVP

Como administrador, quero que o agente busque comandos pendentes com seguranca, para que push externo nao seja fonte de verdade.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-celular.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Agente consulta comandos pendentes via API autenticada.
- API retorna apenas comandos do dispositivo.
- Agente valida alvo, validade e assinatura.

Testes unitarios:
- Backend: isolamento por dispositivo e comando expirado.
- Android: comando valido, expirado, alvo divergente e assinatura invalida.

Cenarios de validacao manual:
- Enfileirar comando para um dispositivo.
- Confirmar que apenas o agente correto recebe o comando.

Criterio de conclusao:
- Agente so executa comando valido e destinado a ele.
