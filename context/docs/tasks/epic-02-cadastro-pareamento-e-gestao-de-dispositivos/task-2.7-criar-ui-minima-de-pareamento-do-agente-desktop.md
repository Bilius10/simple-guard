# Task 2.7: Criar UI Minima de Pareamento do Agente Desktop

Fase: MVP

## Contexto e Objetivo

Como usuario do computador monitorado, quero parear o agente desktop por uma interface minima, para vincular Windows, Linux ou macOS a uma instancia SimpleGuard usando o contrato seguro definido na task 2.4.

Esta task existe porque a task 2.4 deve permanecer restrita ao contrato/core Rust. A UI desktop exige decisao e implementacao propria, mesmo reaproveitando o core.

Nao considerar pronta se houver implementacao parcial, tela sem contrato real, comportamento apenas mockado, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Criar UI minima do agente desktop para `Desktop Agent / 01 - Boas-vindas`.
- [ ] Criar UI minima do agente desktop para `Desktop Agent / 02 - Pareamento`.
- [ ] Reutilizar o core Rust da task 2.4 para montar payload e validar configuracao.
- [ ] Exibir URL da instancia, codigo de pareamento, nome do computador, usuario/SO detectado e estado de validacao.
- [ ] Tratar estados: aguardando codigo, conectando, validando, pareado e falha.
- [ ] Nao implementar tray, daemon, telemetria, comandos remotos, capacidades ou despareamento nesta task.
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
- Rust continua sendo o core do agente.
- Tauri pode ser usado apenas para UI/tray/configuracao local, conforme arquitetura.
- Se Tauri for adotado, manter a UI pequena e operacional, sem duplicar a web admin.
- Se a dependencia de Tauri extrapolar 3 a 5 dias, dividir a task em scaffold de UI e fluxo de pareamento.

## Dependencias e Bloqueios

- Depende da task 2.4 para contrato Rust.
- Bloqueio: se o ambiente Rust/Tauri nao estiver definido no projeto, registrar decisao tecnica antes de fechar.
- Bloqueio: nao substituir chamada real de pareamento por mock permanente.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em subtasks independentes sem alterar o objetivo da task.
- Nao agrupar refatoracoes, melhorias visuais ou capacidades futuras que nao sejam necessarias para cumprir o criterio de aceite.

## Testes Unitarios Obrigatorios

- [ ] Rust: estados de UI de pareamento e validacao de entrada.
- [ ] UI desktop: renderizacao dos campos e mensagens de falha quando aplicavel.

## Cenarios de Validacao Manual

- [ ] Abrir UI desktop e confirmar tela de boas-vindas.
- [ ] Informar URL/codigo/nome do computador e simular pareamento com API local.
- [ ] Confirmar dispositivo desktop pareado na API.
- [ ] Simular plataforma divergente ou chave ausente e validar mensagem de falha.

## Criterio de Conclusao

- Agente desktop possui UI minima para iniciar e concluir pareamento sem implementar capacidades futuras.
