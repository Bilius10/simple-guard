# Task 9.7: Definir Backup, Restore e Hardening Self-Hosted

Fase: fase posterior

## Contexto e Objetivo

Como administrador tecnico, quero procedimentos claros de backup, restore, atualizacao e hardening da instancia self-hosted, para reduzir risco operacional sem transformar o MVP em plataforma gerenciada complexa.

Esta task cobre riscos operacionais citados na pesquisa e na arquitetura: backup, TLS, DNS, atualizacao, observabilidade e hardening. Ela complementa a task 1.1, que entrega o deploy inicial reproduzivel.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Documentar procedimento de backup do PostgreSQL/PostGIS e dos volumes relevantes.
- [ ] Documentar procedimento de restore em ambiente limpo.
- [ ] Definir checklist minimo de hardening para TLS, proxy, variaveis sensiveis, IdP e portas expostas.
- [ ] Definir estrategia simples de atualizacao de containers e migracoes.
- [ ] Garantir que segredos e backups nao sejam versionados.
- [ ] Testes unitarios obrigatorios implementados e passando quando houver script automatizado.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/pesquisa/simpleguard-risk-stack-research.md`
- `task-1.1-inicializar-estrutura-self-hosted-do-produto.md`
- `task-9.5-configurar-observabilidade-operacional-self-hosted.md`

Notas tecnicas:
- Backup deve preservar evidencias, eventos e telemetria sem expor segredos.
- Restore deve ser validado em ambiente descartavel.
- Nao implementar oferta gerenciada, multi-tenant ou automacao cloud nesta task.

## Dependencias e Bloqueios

- Depende da task 1.1 para estrutura de deploy.
- Depende da definicao final dos volumes persistentes e variaveis obrigatorias.
- Bloqueio: se o produto mudar de perfil self-hosted para oferta gerenciada, replanejar esta task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, separar backup/restore, hardening e atualizacao.
- Nao agrupar observabilidade detalhada, LGPD ou retenção nesta task.

## Testes Unitarios Obrigatorios

- [ ] Scripts: validacao de parametros obrigatorios e falha clara quando destino de backup estiver ausente, se scripts forem criados.

## Cenarios de Validacao Manual

- [ ] Gerar backup em ambiente teste.
- [ ] Restaurar em ambiente limpo e validar API, login e dados principais.
- [ ] Confirmar que segredos, dumps e arquivos de backup nao aparecem no `git status`.

## Criterio de Conclusao

- Instancia self-hosted possui caminho documentado e validavel para backup, restore, atualizacao e hardening minimo.
