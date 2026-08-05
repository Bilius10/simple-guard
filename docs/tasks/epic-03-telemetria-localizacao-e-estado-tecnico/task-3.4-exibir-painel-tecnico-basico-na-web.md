# Task 3.4: Exibir Painel Tecnico Basico na Web

Fase: MVP

Como administrador, quero ver o estado tecnico do dispositivo selecionado, para saber se localizacao e comandos sao confiaveis.

Referencias:
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar endpoint de ultima telemetria.
- Renderizar bateria, sinal, rede, ultima atualizacao, coordenadas e precisao.
- Exibir `indisponivel` para dados ausentes.

Testes unitarios:
- Backend: endpoint de ultima telemetria.
- Frontend: dados completos, ausentes, antigos e bateria baixa.

Cenarios de validacao manual:
- Selecionar dispositivo com dados completos.
- Selecionar dispositivo com dados ausentes e confirmar mensagens corretas.

Criterio de conclusao:
- Painel tecnico exibe dados confiaveis sem inventar valores.
