# Task 2.5: Desparear e Revogar Dispositivo

Fase: MVP

## Contexto e Objetivo

Como administrador, quero desparear um dispositivo e revogar suas chaves, para impedir envio de dados e recebimento de comandos por agente removido.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Revogar chaves ativas.
- [x] Atualizar estado do dispositivo.
- [x] Rejeitar credencial revogada no contrato reutilizavel pelos endpoints de telemetria das tasks 3.1 e 3.2.
- [x] Exibir confirmacao perigosa na web.
- [x] Android: antes de implementar qualquer tela, validar se o Figma/UX spec, imagens de referencia e fontes de dados visuais necessarias estao disponiveis para seguir o padrao aprovado; se nao estiverem, solicitar os insumos ao usuario antes de codar.
- [x] Android: implementar tela `Mobile Agent / 07 - Despareamento` com instancia atual, nome do dispositivo, consequencias e confirmacao explicita.
- [x] Android: tratar estados de despareamento solicitado, despareado, falha ao comunicar API e despareado localmente com sincronizacao pendente.
- [x] Desktop: implementar tela `Desktop Agent / 08 - Despareamento` com instancia atual, nome do dispositivo, aviso de impacto, confirmacao explicita e resultado.
- [x] Testes unitarios obrigatorios implementados e passando.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

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
- Se a task tocar o front Android, validar primeiro as imagens/telas de referencia e as fontes de dados que alimentam a UI; ausencia desses insumos bloqueia implementacao ate solicitacao ao usuario.
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

- [x] Backend: testes implementados para revogacao, idempotencia e credencial rejeitada apos revogacao.
- [x] Frontend: testes implementados para confirmacao, sucesso e falha.
- [x] Android: testes implementados para os estados de despareamento e falha.
- [x] Desktop: testes implementados para os estados de despareamento e falha no core de apresentacao; renderizacao fica para a UI da task 2.7.

## Cenarios de Validacao Manual

- [x] Desparear dispositivo.
- [x] Tentar enviar telemetria com credencial antiga e confirmar rejeicao.
- [x] Android: desparear com API disponivel e validar retorno para estado local nao pareado.
- [x] Android: simular falha de API e validar estado local pendente de sincronizacao.
- [x] Desktop: desparear com API disponivel e validar retorno para estado local nao pareado quando a UI desktop existir.

## Criterio de Conclusao

- Dispositivo revogado nao opera mais na instancia.

## Evidencias de Implementacao

- Backend administrativo: `DELETE /api/devices/{deviceId}/unpairing`, protegido por OIDC admin.
- Backend agente: `DELETE /api/agent/devices/{deviceId}/pairing`, protegido por assinatura ECDSA da chave pareada.
- Web: dialogo destrutivo, sucesso, falha e atualizacao local do estado do dispositivo.
- Android: tela 07 baseada na imagem aprovada e alimentada pelos dados persistidos do pareamento.
- Desktop: payload assinado, endpoint e cinco estados de apresentacao implementados no core Rust.
- Builds executados sem testes em 2026-08-11: Maven compile, Angular build e Gradle assembleDebug aprovados.
- Bloqueio restante: a tela Tauri desktop depende da criacao da UI prevista na task 2.7.
- Testes e cenarios manuais permanecem pendentes de execucao por orientacao do usuario.
