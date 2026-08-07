# BMAD Runbook - SimpleGuard

Use esta sequencia para conduzir o discovery e transformar as referencias em backlog executavel.

## 1. Gerar contexto (Concluido)

Skill: `bmad-generate-project-context`

Prompt sugerido:
`Use a pasta referencias/ e o arquivo docs/contexto/project-context.md como base. Reforce regras de negocio, pilares do produto, UX de referencia e lacunas abertas do discovery.`

## 2. Abrir discovery (Concluido)

Skill: `bmad-brainstorming` ou `bmad-forge-idea`

Prompt sugerido:
`Quero conduzir um discovery do SimpleGuard. Use referencias/descricao_ideia_plataforma.md e docs/contexto/project-context.md. Quero consolidar proposta de valor, ICP, fluxo principal de incidente e lacunas que ainda precisam de decisao.`

## 3. Criar brief (Concluido)

Skill: `bmad-product-brief`

Prompt sugerido:
`Criar um product brief do SimpleGuard em portugues, ancorado em docs/contexto/project-context.md. Separar claramente MVP, fase 2 e backlog futuro.`

## 4. Pesquisas complementares (Concluido)

Skills:
- `bmad-market-research`
- `bmad-domain-research`
- `bmad-technical-research`

Prompts sugeridos:
- `Pesquisar concorrentes e substitutos do SimpleGuard, com foco em rastreamento, bloqueio remoto e preservacao de evidencias.`
- `Pesquisar riscos regulatorios, operacionais e de privacidade para um produto self-hosted de rastreamento de dispositivos.`
- `Pesquisar stack atual para mapa, geolocalizacao, exportacao PDF, autenticacao forte e canal de comando remoto.`

## 5. Criar PRD (Concluido)

Skill: `bmad-prd`

Prompt sugerido:
`Criar um PRD do SimpleGuard em portugues. Usar docs/contexto/project-context.md e as referencias como base. O documento deve refletir um produto operacional de seguranca patrimonial, nao uma experiencia promocional.`

## 6. Fazer debate de equipe (Concluido)

Skill: `bmad-party-mode`

Prompt sugerido:
`Simule um debate entre PM, UX Designer e Architect sobre o MVP do SimpleGuard. Quero decidir superficie inicial, grau de complexidade do bloqueio remoto, evidencias em PDF e compromissos da primeira release.`

## 7. Definir UX e POC (Concluido)

Skill: `bmad-ux`

Prompt sugerido:
`Criar UX spec do SimpleGuard em portugues. Preservar mapa dominante, HUD retro-tech, pixel-art funcional, paineis laterais e leitura operacional imediata. Nao transformar em dashboard SaaS generico.`

## 8. Definir arquitetura (Concluido)

Skill: `bmad-agent-architect` -> `bmad-architecture`

Prompt sugerido:
`Criar a arquitetura do SimpleGuard priorizando self-hosted, soberania do usuario, telemetria geoespacial, trilha de auditoria, bloqueio remoto e geracao de evidencias. Prefira stack estavel e simples de operar.`

Artefatos gerados:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `.codex/bmad/output/planning-artifacts/architecture/architecture-SimpleGuard-2026-08-05/ARCHITECTURE-SPINE.md`

## 9. Quebrar em epicos e stories

Skill: `bmad-create-epics-and-stories`

Prompt sugerido:
`Quebrar o SimpleGuard em epicos e stories organizados por capacidade de produto e backend, nao por tela. Separar MVP, fase posterior e backlog exploratorio. Para tarefas de frontend da plataforma web e apps, referenciar docs/ux/ e o Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0`

## 10. Planejar sprint e detalhar stories

Skills:
- `bmad-sprint-planning`
- `bmad-create-story`

Prompts sugeridos:
- `Gerar sprint planning inicial a partir dos epicos aprovados do MVP.`
- `Criar a proxima story pronta para desenvolvimento com contexto suficiente para implementacao. Se a story envolver frontend, incluir referencia explicita ao UX spec correspondente em docs/ux/ e ao Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0`

## 11. Validar consistencia

Skill: `bmad-check-implementation-readiness`

Prompt sugerido:
`Validar se PRD, UX, arquitetura e epicos do SimpleGuard estao alinhados e prontos para implementacao.`
