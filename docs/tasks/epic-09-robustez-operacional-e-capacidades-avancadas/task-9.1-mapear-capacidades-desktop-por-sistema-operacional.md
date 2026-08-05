# Task 9.1: Mapear Capacidades Desktop por Sistema Operacional

Fase: fase posterior

Como administrador tecnico, quero conhecer capacidades reais por Windows, macOS e Linux, para que o produto nao prometa bloqueio ou telemetria que o SO nao suporta.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`

Escopo:
- Criar matriz de capacidades por SO.
- Cobrir lock, alarme, localizacao, rede, autostart, instalacao e atualizacao.
- Mapear capacidades detectadas no contrato Rust comum.

Testes unitarios:
- Rust: mapeamento de capacidades detectadas para contrato comum.

Cenarios de validacao manual:
- Rodar agente em pelo menos um SO alvo.
- Registrar capacidades detectadas.

Criterio de conclusao:
- Matriz desktop orienta promessas futuras por plataforma.
