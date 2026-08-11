# Task 7.7: Agente Desktop Buscar e Validar Comando

Fase: fase posterior

## Contexto e Objetivo

Como administrador, quero que o agente desktop busque comandos pendentes com seguranca, para que computadores pareados possam responder a comandos suportados sem depender de canal push externo como fonte de verdade.

Esta task cobre o equivalente desktop da task Android 7.3. Ela deve respeitar a matriz de capacidades por sistema operacional e nao prometer bloqueio, alarme ou outra acao que Windows, Linux ou macOS nao suportem no modo implementado.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Agente desktop consulta comandos pendentes via API autenticada usando identidade/chave do pareamento.
- [ ] API retorna apenas comandos destinados ao dispositivo/instancia do agente.
- [ ] Agente valida alvo, validade, tipo de comando, assinatura e capacidade local antes de executar.
- [ ] Comandos nao suportados pelo SO geram estado explicito `unsupported` ou `failed`, conforme contrato aprovado.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- `task-2.4-definir-contrato-inicial-do-agente-desktop.md`
- `task-9.1-mapear-capacidades-desktop-por-sistema-operacional.md`

Notas tecnicas:
- Reaproveitar o modelo de comando remoto do backend das tasks 7.1 e 7.2.
- Manter nomes, estados e eventos consistentes com o PRD: `requested`, `queued`, `delivered`, `executed`, `confirmed`, `failed`, `expired`.
- Nao implementar canal push desktop nesta task; polling autenticado e suficiente enquanto nao houver decisao aprovada.
- Se a task tocar backend ou agente, manter validacao de entrada, casos de erro e testes unitarios junto da implementacao.

## Dependencias e Bloqueios

- Depende das tasks 2.4, 7.1, 7.2 e 9.1.
- Bloqueio: se o SO nao suportar a acao solicitada, registrar capacidade indisponivel em vez de simular sucesso.
- Bloqueio: se a assinatura do comando ainda nao estiver definida no backend, limitar a task ao contrato e nao executar comando real.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar UI rica, instalador, autostart, atualizacao ou novos tipos de comando nesta task.

## Testes Unitarios Obrigatorios

- [ ] Rust: comando valido, expirado, alvo divergente, assinatura invalida e capacidade nao suportada.
- [ ] Backend: isolamento por dispositivo desktop e comando expirado.

## Cenarios de Validacao Manual

- [ ] Enfileirar comando para um computador pareado.
- [ ] Confirmar que apenas o agente desktop correto recebe o comando.
- [ ] Simular comando nao suportado pelo SO e validar estado final explicito.

## Criterio de Conclusao

- Agente desktop so executa comando valido, destinado a ele e suportado pela capacidade real do sistema operacional.
