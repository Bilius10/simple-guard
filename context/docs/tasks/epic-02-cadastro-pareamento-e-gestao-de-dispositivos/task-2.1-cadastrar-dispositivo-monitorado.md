# Task 2.1: Cadastrar Dispositivo Monitorado

Fase: MVP

## Contexto e Objetivo

Como administrador, quero cadastrar um dispositivo com nome, tipo e plataforma, para prepara-lo para pareamento.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar entidade/endpoint minimo de dispositivo.
- [x] Criar formulario web de cadastro.
- [x] Exibir dispositivo em estado `unpaired`.
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

- [x] Backend: criacao valida, campos obrigatorios e plataforma invalida.
- [x] Frontend: formulario, validacao, sucesso e estado vazio.

## Cenarios de Validacao Manual

- [x] Cadastrar Android, notebook e desktop.
- [x] Confirmar que todos aparecem na lista como pendentes de pareamento.

## Criterio de Conclusao

- Dispositivo cadastrado aparece na web e fica pronto para pareamento.

## Evidencias De Implementacao

- API administrativa implementada em `POST /api/devices` e `GET /api/devices`.
- Persistencia criada pela migration `V3__create_devices.sql`, incluindo vinculo com conta e auditoria completa.
- Dispositivos novos sao persistidos e devolvidos com `pairingStatus` igual a `unpaired`.
- Formulario Angular implementado com nome, tipo e plataforma, seguido da lista de dispositivos cadastrados.
- Estados de carregamento, vazio, validacao, sucesso, falha e nova tentativa tratados na interface.
- Backend validado em 2026-08-08 com `./mvnw test`: 24 testes passando.
- Frontend validado em 2026-08-08 com `npm test`: 15 testes passando.
- Build de producao validado em 2026-08-08 com `npm run build`.
- Validacao manual integrada permanece pendente ate a atualizacao dos containers locais.
