# Task 2.4: Definir Contrato Inicial do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como administrador, quero parear um computador usando o mesmo modelo de dispositivo confiavel, para que agentes desktop futuros sigam o mesmo contrato de seguranca.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Definir payload de pareamento desktop.
- [x] Criar core Rust minimo para identidade local e chave.
- [x] Reutilizar contrato seguro de agente.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`
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
- Nao implementar tela desktop nesta task; a UI minima de boas-vindas/pareamento fica na task 2.7 para manter esta task restrita ao contrato/core Rust.

## Testes Unitarios Obrigatorios

- [x] Backend: pareamento desktop, plataforma divergente e chave ausente.
- [x] Rust: validacao de configuracao local e serializacao do pedido.

## Cenarios de Validacao Manual

- [x] Simular payload de agente desktop.
- [x] Confirmar dispositivo desktop pareado na API.

## Criterio de Conclusao

- Contrato desktop inicial existe sem duplicar regras do agente Android.
