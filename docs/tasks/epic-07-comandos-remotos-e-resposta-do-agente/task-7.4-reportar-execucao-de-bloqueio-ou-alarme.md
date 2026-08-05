# Task 7.4: Reportar Execucao de Bloqueio ou Alarme

Fase: MVP

Como administrador, quero receber o resultado da execucao do comando, para saber se ele foi entregue, executado, confirmado, falhou ou expirou.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`

Escopo:
- Agente reporta `delivered`, `executed`, `confirmed` ou `failed`.
- API valida maquina de estados.
- Cada evento aparece no log cronologico.

Testes unitarios:
- Backend: todas as transicoes permitidas e rejeicao de saltos invalidos.
- Android: envio de sucesso e falha tecnica.

Cenarios de validacao manual:
- Simular execucao bem-sucedida e falha.
- Validar estados na web.

Criterio de conclusao:
- Estado do comando reflete resultado reportado pelo agente.
