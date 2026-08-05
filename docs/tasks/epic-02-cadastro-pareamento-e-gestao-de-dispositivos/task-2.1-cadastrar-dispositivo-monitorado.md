# Task 2.1: Cadastrar Dispositivo Monitorado

Fase: MVP

Como administrador, quero cadastrar um dispositivo com nome, tipo e plataforma, para prepara-lo para pareamento.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Criar entidade/endpoint minimo de dispositivo.
- Criar formulario web de cadastro.
- Exibir dispositivo em estado `unpaired`.

Testes unitarios:
- Backend: criacao valida, campos obrigatorios e plataforma invalida.
- Frontend: formulario, validacao, sucesso e estado vazio.

Cenarios de validacao manual:
- Cadastrar Android, notebook e desktop.
- Confirmar que todos aparecem na lista como pendentes de pareamento.

Criterio de conclusao:
- Dispositivo cadastrado aparece na web e fica pronto para pareamento.
