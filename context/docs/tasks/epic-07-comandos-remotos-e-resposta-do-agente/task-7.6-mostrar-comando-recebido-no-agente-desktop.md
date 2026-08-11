# Task 7.6: Mostrar Comando Recebido no Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como usuario do computador monitorado, quero ver quando o agente desktop recebeu, executou ou falhou em um comando remoto, para entender o estado real da resposta sem confundir solicitacao com execucao.

Esta task mapeia a tela `Desktop Agent / 06 - Comando Recebido` do UX spec. Ela complementa as tasks de comando remoto, mas nao deve implementar o motor completo de comandos se as dependencias ainda nao existirem.

Nao considerar pronta se houver implementacao parcial, tela sem estado real, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Implementar tela `Desktop Agent / 06 - Comando Recebido`.
- [ ] Exibir tipo de comando, estado de execucao, resultado, timestamp e instancia solicitante.
- [ ] Tratar estados: recebido, executando, executado, falhou e expirado.
- [ ] Mostrar motivo tecnico simples quando disponivel.
- [ ] Nao implementar comandos novos fora dos tipos aprovados pelas tasks do epic 7.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/ux/simpleguard-ux-app-computador.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Notas tecnicas:
- Reutilizar estados e eventos do epic 7.
- Se a execucao real desktop ainda nao existir, limitar esta task a renderizar estados recebidos do core/contrato aprovado, sem mock permanente.

## Dependencias e Bloqueios

- Depende das tasks 7.1, 7.2, 7.7 e 7.8 quando houver contrato de comando desktop.
- Depende da UI desktop criada na task 2.7 ou status local criado na task 3.6.
- Bloqueio: se comandos desktop ainda nao forem suportados por SO, exibir estado `nao suportado` ou manter task bloqueada.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust/UI desktop: renderizacao dos estados recebido, executando, executado, falhou e expirado.
- [ ] Rust/UI desktop: mensagem tecnica simples quando houver erro.

## Cenarios de Validacao Manual

- [ ] Carregar fixture com comando recebido.
- [ ] Carregar fixture com comando falho.
- [ ] Validar textos e cores contra o Figma.

## Criterio de Conclusao

- Usuario entende o estado do comando recebido no agente desktop.
