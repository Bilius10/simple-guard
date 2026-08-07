# Task 1.1: Inicializar Estrutura Self-Hosted do Produto

Fase: MVP

## Contexto e Objetivo

Como administrador tecnico, quero subir a base local do SimpleGuard com API, web, banco, IdP e proxy, para operar uma instancia self-hosted inicial de forma previsivel.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar estrutura inicial de deploy self-hosted.
- [x] Subir API, web admin, PostgreSQL/PostGIS, IdP e reverse proxy.
- [x] Expor health checks basicos.
- [x] Documentar variaveis de ambiente obrigatorias.
- [x] Nao versionar segredos.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `.codex/bmad/output/planning-artifacts/architecture/architecture-SimpleGuard-2026-08-05/ARCHITECTURE-SPINE.md`

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

- [x] Validar configuracao obrigatoria da API.
- [x] Validar falha clara quando variavel obrigatoria estiver ausente.

## Cenarios de Validacao Manual

- [x] Dado um ambiente com Docker, quando subir a configuracao local, entao API, web, banco, IdP e proxy devem iniciar.
- [x] Dado a stack local ativa, quando acessar a URL da web e o health da API, entao ambos devem responder corretamente.

## Criterio de Conclusao

- Stack local sobe de forma reproduzivel e health checks basicos funcionam.
