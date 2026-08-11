# Task 3.8: Coletar e Enviar Telemetria Tecnica do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como administrador, quero receber bateria, rede, sinal e estado tecnico de computadores pareados quando disponiveis, para avaliar confiabilidade operacional sem extrapolar capacidades do sistema operacional.

Esta task separa coleta/envio desktop da ingestao generica da API e do fluxo Android.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Definir contrato local Rust para telemetria tecnica desktop.
- [ ] Coletar bateria quando aplicavel.
- [ ] Coletar estado de rede/conectividade quando aplicavel.
- [ ] Coletar sinal ou qualidade de rede quando aplicavel.
- [ ] Coletar estado de permissoes/capacidades quando aplicavel.
- [ ] Enviar telemetria tecnica ao endpoint de agente pareado reaproveitando o contrato da task 3.2.
- [ ] Representar dados indisponiveis como `null`/ausentes, sem usar `0` como fallback.
- [ ] Registrar estado local para exibicao na task 3.6.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- `task-3.2-ingerir-telemetria-tecnica-do-dispositivo.md`
- `task-9.1-mapear-capacidades-desktop-por-sistema-operacional.md`

Notas tecnicas:
- Reutilizar o core Rust iniciado na task 2.4.
- Cada SO deve declarar capacidade real ou limitada.
- Nao implementar comando remoto, instalador, autostart ou atualizacao nesta task.

## Dependencias e Bloqueios

- Depende da task 2.4 para identidade/chave do agente desktop.
- Depende da task 3.2 para endpoint e contrato de ingestao.
- Bloqueio: se a matriz da task 9.1 nao existir, implementar apenas contrato/adaptador minimo e marcar capacidades como limitadas por SO.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust: telemetria valida, bateria ausente, rede ausente, valores fora de faixa e falha de envio.
- [ ] Rust: dados indisponiveis serializam como ausentes/null conforme contrato da API.

## Cenarios de Validacao Manual

- [ ] Simular telemetria desktop completa e confirmar persistencia na API.
- [ ] Simular notebook sem bateria ou desktop sem bateria e confirmar dado ausente.
- [ ] Simular falha de rede e confirmar evento pendente para retry.

## Criterio de Conclusao

- Agente desktop envia telemetria tecnica real quando disponivel e comunica dados ausentes corretamente.
