# Task 3.7: Coletar e Enviar Localizacao do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como administrador, quero receber localizacao de computadores pareados quando o sistema operacional permitir, para acompanhar dispositivos desktop/notebook sem prometer capacidade que a plataforma nao suporta.

Esta task existe para separar a coleta/envio desktop da ingestao generica da API e do fluxo Android, pois Windows, Linux e macOS possuem capacidades e permissoes diferentes.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Definir contrato local Rust para evento de localizacao desktop.
- [ ] Coletar localizacao quando suportado pelo SO e permissao estiver disponivel.
- [ ] Enviar localizacao ao endpoint de agente pareado reaproveitando o contrato da task 3.1.
- [ ] Representar localizacao indisponivel como ausencia de dado, sem coordenada inventada.
- [ ] Tratar permissao negada, recurso nao suportado, falha de rede e dispositivo nao pareado.
- [ ] Registrar estado local para exibicao na task 3.6.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-app-computador.md`
- `task-3.1-ingerir-localizacao-de-agente-pareado.md`
- `task-9.1-mapear-capacidades-desktop-por-sistema-operacional.md`

Notas tecnicas:
- Reutilizar o core Rust iniciado na task 2.4.
- Localizacao desktop pode ser limitada ou indisponivel; isso deve aparecer como estado, nao como erro generico.
- Nao implementar mapa, historico, paradas ou painel administrativo nesta task.

## Dependencias e Bloqueios

- Depende da task 2.4 para identidade/chave do agente desktop.
- Depende da task 3.1 para endpoint e contrato de ingestao.
- Bloqueio: se a matriz da task 9.1 nao existir, implementar apenas adaptador/contrato e marcar capacidades como limitadas por SO.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust: payload valido, localizacao ausente, permissao negada e plataforma sem suporte.
- [ ] Rust: falha de envio nao apaga evento local pendente.

## Cenarios de Validacao Manual

- [ ] Simular localizacao disponivel e confirmar persistencia na API.
- [ ] Simular localizacao indisponivel e confirmar estado limitado no agente.
- [ ] Simular falha de rede e confirmar evento pendente para retry.

## Criterio de Conclusao

- Agente desktop envia localizacao real quando suportada e comunica limitacoes sem inventar dados.
