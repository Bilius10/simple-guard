# Task 9.4: Criar Zonas Seguras Basicas

Fase: fase posterior

Como administrador, quero definir zonas seguras, para que movimentos fora de areas esperadas possam gerar atencao.

Referencias:
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar zona segura com nome e perimetro.
- Avaliar entrada/saida com telemetria recebida.
- Exibir criacao/edicao na web.

Testes unitarios:
- Backend: ponto dentro, fora e borda do perimetro.
- Frontend: criacao, edicao e estado invalido de zona.

Cenarios de validacao manual:
- Criar zona.
- Enviar ponto fora dela e verificar alerta/estado de atencao.

Criterio de conclusao:
- Zona segura basica gera avaliacao geoespacial testavel.
