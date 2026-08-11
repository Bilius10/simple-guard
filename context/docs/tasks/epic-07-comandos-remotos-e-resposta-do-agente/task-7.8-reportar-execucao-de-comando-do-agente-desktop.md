# Task 7.8: Reportar Execucao de Comando do Agente Desktop

Fase: fase posterior

## Contexto e Objetivo

Como administrador, quero receber o resultado de comandos executados pelo agente desktop, para diferenciar comando solicitado, entregue, executado, confirmado, falho, expirado ou nao suportado.

Esta task cobre o equivalente desktop da task 7.4 e alimenta a tela `Desktop Agent / 06 - Comando Recebido` prevista na task 7.6.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Agente desktop reporta `delivered`, `executed`, `confirmed`, `failed`, `expired` ou `unsupported` conforme contrato aprovado.
- [ ] API valida maquina de estados e rejeita saltos invalidos.
- [ ] Cada transicao gera evento cronologico auditavel.
- [ ] Resultado inclui timestamp do agente, timestamp de recebimento no servidor e erro tecnico quando houver falha.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- `task-7.4-reportar-execucao-de-bloqueio-ou-alarme.md`
- `task-7.7-agente-desktop-buscar-e-validar-comando.md`

Notas tecnicas:
- Reaproveitar endpoint/modelo da task 7.4 quando possivel, adicionando validações especificas de plataforma apenas se forem necessarias.
- Separar evento observado pelo agente de inferencia do backend.
- Nao implementar dashboard web novo nesta task; estados web continuam na task 7.5.

## Dependencias e Bloqueios

- Depende das tasks 7.1, 7.2, 7.4 e 7.7.
- Bloqueio: se a matriz desktop da task 9.1 indicar capacidade indisponivel, reportar `unsupported` ou manter comando bloqueado por tipo de dispositivo.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar novos comandos, UI rica, instalador ou alteracoes de mapa nesta task.

## Testes Unitarios Obrigatorios

- [ ] Rust: envio de sucesso, falha tecnica, expirado localmente e nao suportado.
- [ ] Backend: transicoes permitidas, rejeicao de saltos invalidos e auditoria gerada.

## Cenarios de Validacao Manual

- [ ] Simular comando desktop executado com sucesso.
- [ ] Simular falha tecnica e validar mensagem auditavel.
- [ ] Validar estado correspondente na web e na tela desktop de comando recebido.

## Criterio de Conclusao

- Estado do comando desktop reflete o resultado real reportado pelo agente, sem confundir solicitacao com execucao.
