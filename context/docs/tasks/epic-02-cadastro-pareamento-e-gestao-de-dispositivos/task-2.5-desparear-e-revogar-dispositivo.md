# Task 2.5: Desparear e Revogar Dispositivo

Fase: MVP

## Contexto e Objetivo

Como administrador, quero desparear um dispositivo e revogar suas chaves, para impedir envio de dados e recebimento de comandos por agente removido.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Revogar chaves ativas.
- [ ] Atualizar estado do dispositivo.
- [ ] Rejeitar telemetria posterior.
- [ ] Exibir confirmacao perigosa na web.
- [ ] Android: implementar tela `Mobile Agent / 07 - Despareamento` com instancia atual, nome do dispositivo, consequencias e confirmacao explicita.
- [ ] Android: tratar estados de despareamento solicitado, despareado, falha ao comunicar API e despareado localmente com sincronizacao pendente.
- [ ] Desktop: implementar tela `Desktop Agent / 08 - Despareamento` com instancia atual, nome do dispositivo, aviso de impacto, confirmacao explicita e resultado.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- `docs/ux/simpleguard-ux-app-celular.md`
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

## Testes Unitarios Obrigatorios

- [ ] Backend: revogacao, idempotencia e telemetria rejeitada apos revogacao.
- [ ] Frontend: confirmacao, sucesso e falha.
- [ ] Android: renderizacao dos estados de despareamento e falha.
- [ ] Desktop: renderizacao dos estados de despareamento e falha quando a UI desktop existir.

## Cenarios de Validacao Manual

- [ ] Desparear dispositivo.
- [ ] Tentar enviar telemetria com credencial antiga e confirmar rejeicao.
- [ ] Android: desparear com API disponivel e validar retorno para estado local nao pareado.
- [ ] Android: simular falha de API e validar estado local pendente de sincronizacao.
- [ ] Desktop: desparear com API disponivel e validar retorno para estado local nao pareado quando a UI desktop existir.

## Criterio de Conclusao

- Dispositivo revogado nao opera mais na instancia.
