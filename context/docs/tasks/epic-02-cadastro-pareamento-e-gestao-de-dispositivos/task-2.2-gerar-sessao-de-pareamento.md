# Task 2.2: Gerar Sessao de Pareamento

Fase: MVP

## Contexto e Objetivo

Como administrador, quero gerar um codigo ou QR de pareamento com validade curta, para vincular apenas dispositivo em posse fisica.

Esta task existe para entregar uma capacidade testavel do backlog ja aprovado, sem expandir escopo alem do epic correspondente.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [x] Criar sessoes curtas de pareamento.
- [x] Exibir codigo/QR e expiracao na web.
- [x] Auditar criacao e expiracao.
- [x] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [x] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [x] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
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

- [x] Backend: sessao valida, expiracao, reutilizacao bloqueada e dispositivo ja pareado.
- [x] Frontend: aguardando, expirado e erro.

## Cenarios de Validacao Manual

- [ ] Gerar codigo para dispositivo pendente.
- [ ] Aguardar expiracao e confirmar que o codigo nao pode ser reutilizado.

## Criterio de Conclusao

- Pareamento usa codigo temporario auditavel e nao reutilizavel.

## Evidencias De Implementacao

- API administrativa implementada em `POST /api/devices/{deviceId}/pairing-sessions`.
- Persistencia criada pela migration `V4__create_pairing_sessions.sql`, com auditoria, expiracao, uso unico e uma unica sessao ativa por dispositivo.
- Codigo de oito caracteres gerado com `SecureRandom`; somente o hash SHA-256 e persistido e a resposta usa `Cache-Control: no-store`.
- Validade padrao configurada em cinco minutos por `SIMPLEGUARD_PAIRING_SESSION_VALIDITY`.
- Sessoes vencidas sao expiradas periodicamente e registram motivo `TIMEOUT`; a geracao de um novo codigo expira o anterior com motivo `REPLACED`.
- Web administrativa exibe codigo, dispositivo, horario de expiracao e estados `Aguardando agente`, `Codigo expirado` e falha.
- Backend validado em 2026-08-08 com `./mvnw test`: 34 testes passando.
- Frontend validado em 2026-08-08 com `npm test`: 18 testes passando.
- Build de producao validado em 2026-08-08 com `npm run build`.
- Validacao manual integrada permanece pendente porque o acesso ao Docker via WSL nao estava disponivel nesta sessao.
