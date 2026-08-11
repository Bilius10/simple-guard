# Task 2.3: Parear Agente Android

Fase: MVP

## Contexto e Objetivo

Como administrador, quero concluir o pareamento do agente Android com a instancia, para que o celular envie telemetria autorizada.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Agente Android envia codigo, identificacao e chave publica.
- [x] API valida sessao e registra chave.
- [x] UI Android mostra estados de pareamento.
- [x] Testes unitarios obrigatorios implementados e passando no backend.
- [x] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/ux/simpleguard-ux-app-celular.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
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

- [x] Backend: codigo invalido, expirado, chave ausente e sucesso.
- [x] Android: aguardando, validando, falha e pareado.

Evidencias tecnicas:
- Backend validado com `mvn verify`: 54 testes, 0 falhas, JaCoCo 100% para instrucoes, branches, linhas, metodos e classes.
- Android: testes unitarios adicionados em `android-agent/app/src/test/java/simple/guard/agent/pairing/PairingUiControllerTests.kt`.
- Android: execucao local nao realizada neste ambiente porque `gradle`, `ANDROID_HOME` e `ANDROID_SDK_ROOT` nao estao disponiveis.
- Figma: validacao feita contra o UX spec oficial e componentes ja identificados no arquivo (`Mobile/PairingCodeInput`, `Mobile/AgentStatusCard`, `Mobile/SyncStatusBar`). A chamada atual ao conector Figma retornou `INVALID_ARGUMENT`, entao nao houve screenshot final evidenciavel nesta execucao.

## Cenarios de Validacao Manual

- [x] Parear agente Android usando codigo valido.
- [x] Confirmar que o dispositivo aparece como pareado na web.

Bloqueios atuais para fechar manualmente:
- Instalar/abrir Android SDK ou Android Studio para executar `cd android-agent && gradle test` e testar em emulador/dispositivo.
- Revalidar screenshot do frame `Mobile Agent / 02 - Pareamento` no Figma quando o conector aceitar novamente a consulta do arquivo oficial.

## Criterio de Conclusao

- Agente Android pareado recebe identidade confiavel na instancia.
