# Task 1.3: Preparar Confirmacao de Acoes Criticas

Fase: MVP

## Contexto e Objetivo

Como administrador, quero confirmar acoes criticas antes de executa-las, para evitar bloqueio ou alarme por acidente.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar contrato reutilizavel de confirmacao critica.
- [x] Preparar suporte futuro a reautenticacao/step-up.
- [x] Implementar dialog visual reutilizavel na web.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Notas tecnicas:
- Seguir arquitetura e contratos ja definidos nos documentos de referencia.
- Manter nomes, estados e eventos consistentes com `epics.md` e `ARCHITECTURE-SPINE.md`.
- Se a task tocar frontend, seguir o UX spec indicado e o Figma oficial.
- Se a task tocar backend ou agente, manter validacao de entrada, casos de erro e testes unitarios junto da implementacao.

## Dependencias e Bloqueios

- Depende das tasks anteriores do mesmo epic quando houver contrato, entidade, endpoint ou estado reutilizado.
- Bloqueio: se a dependencia necessaria nao existir, registrar no PR/branch e nao substituir por mock permanente.
- Bloqueio: se a implementacao exigir decisao marcada como lacuna nos docs, parar e registrar a decisao necessaria antes de fechar a task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [x] Backend: politica exige confirmacao para acao critica.
- [x] Frontend: abrir dialog, cancelar, confirmar e exibir erro.

## Cenarios de Validacao Manual

- [x] Clicar em acao critica simulada, cancelar e confirmar que nada foi executado.
- [x] Repetir a acao e confirmar, validando emissao do evento esperado.

## Evidencias de Validacao

- Backend: `.\mvnw.cmd test` executado com sucesso em `api` com 24 testes passando.
- Frontend: `npm.cmd test` executado com sucesso em `web-admin` com 10 testes passando.
- Frontend: `npm.cmd run build` executado com sucesso em `web-admin`.
- Cenario cancelar: coberto por `cancelsCriticalActionWithoutEmittingEventTests`.
- Cenario confirmar: coberto por `confirmsCriticalActionAndEmitsEventTests`.
- Cenario erro: coberto por `showsCriticalActionConfirmationErrorTests`.

## Criterio de Conclusao

- Nenhuma acao critica pode ser executada sem confirmacao explicita.
