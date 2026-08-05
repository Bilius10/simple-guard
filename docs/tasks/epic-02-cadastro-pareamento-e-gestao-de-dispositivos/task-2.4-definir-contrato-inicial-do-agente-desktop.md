# Task 2.4: Definir Contrato Inicial do Agente Desktop

Fase: MVP

Como administrador, quero parear um computador usando o mesmo modelo de dispositivo confiavel, para que agentes desktop futuros sigam o mesmo contrato de seguranca.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Definir payload de pareamento desktop.
- Criar core Rust minimo para identidade local e chave.
- Reutilizar contrato seguro de agente.

Testes unitarios:
- Backend: pareamento desktop, plataforma divergente e chave ausente.
- Rust: validacao de configuracao local e serializacao do pedido.

Cenarios de validacao manual:
- Simular payload de agente desktop.
- Confirmar dispositivo desktop pareado na API.

Criterio de conclusao:
- Contrato desktop inicial existe sem duplicar regras do agente Android.
