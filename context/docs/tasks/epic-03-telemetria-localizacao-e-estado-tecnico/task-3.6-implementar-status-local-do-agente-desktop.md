# Task 3.6: Implementar Status Local do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como usuario do computador monitorado, quero ver se o agente desktop esta ativo, limitado ou offline, para saber que o computador esta pareado e quais capacidades estao disponiveis neste sistema operacional.

Esta task mapeia as telas `Desktop Agent / 03 - Permissoes e Capacidades`, `Desktop Agent / 04 - Status Ativo`, `Desktop Agent / 05 - Menu da Bandeja` e `Desktop Agent / 07 - Offline` do UX spec. Ela nao deve implementar telemetria completa nem comandos remotos.

Nao considerar pronta se houver implementacao parcial, estados apenas mockados sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Implementar tela `Desktop Agent / 03 - Permissoes e Capacidades`.
- [ ] Implementar tela `Desktop Agent / 04 - Status Ativo`.
- [ ] Implementar estado `Desktop Agent / 07 - Offline`.
- [ ] Implementar menu/tray minimo `Desktop Agent / 05 - Menu da Bandeja` quando a tecnologia escolhida suportar.
- [ ] Exibir capacidades reais ou explicitamente limitadas: telemetria, localizacao, rede, bateria, bloqueio, alarme e segundo plano.
- [ ] Exibir instancia pareada, ultima sincronizacao, eventos pendentes, versao do agente e estado da conexao.
- [ ] Nao implementar comandos remotos, relatorios, mapa ou dashboard administrativo no agente desktop.
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
- Reutilizar o core Rust iniciado na task 2.4.
- Reutilizar a UI minima criada na task 2.7, caso exista.
- Capacidades devem refletir limitacoes reais do SO; nao esconder limitacoes.

## Dependencias e Bloqueios

- Depende da task 2.4 para contrato/core desktop.
- Depende da task 2.7 se a UI desktop ainda nao existir.
- Bloqueio: matriz real de capacidades por SO pode depender da task 9.1; se ausente, exibir estado `limitado` com texto tecnico claro.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust: mapeamento de estados local, offline e limitado.
- [ ] UI desktop: renderizacao de capacidades, status ativo e offline.

## Cenarios de Validacao Manual

- [ ] Abrir agente pareado e confirmar status ativo.
- [ ] Simular ausencia de conexao e confirmar estado offline.
- [ ] Validar que capacidades indisponiveis aparecem como limitadas ou nao suportadas.

## Criterio de Conclusao

- Usuario identifica claramente estado operacional e limitacoes do agente desktop.
