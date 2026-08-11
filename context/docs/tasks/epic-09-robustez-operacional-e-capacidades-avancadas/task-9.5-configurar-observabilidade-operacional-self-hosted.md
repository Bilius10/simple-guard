# Task 9.5: Configurar Observabilidade Operacional Self-Hosted

Fase: fase posterior

## Contexto e Objetivo

Como administrador tecnico, quero diagnosticar saude da instancia, ingestao, fila de comandos, autenticacao, PDF e agentes, para operar o SimpleGuard self-hosted sem falhas silenciosas durante incidente.

Esta task cobre o requisito nao funcional de observabilidade do PRD e complementa a task 1.1, que entrega apenas health checks basicos do deploy inicial.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Expor metricas de saude da API, banco, IdP, proxy, fila de comandos e geracao de PDF.
- [ ] Registrar falhas de ingestao, comando, PDF, autenticacao e comunicacao com agente.
- [ ] Criar painel operacional minimo para instancia self-hosted ou documentar endpoints/metricas para Prometheus/Grafana.
- [ ] Definir logs sem vazamento de localizacao sensivel, tokens, chaves ou payloads de comando.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/produto/simpleguard-prd.md`
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/pesquisa/simpleguard-risk-stack-research.md`

Notas tecnicas:
- Observabilidade deve ajudar a reduzir falhas silenciosas, especialmente comando critico e alerta.
- Preferir endpoints e metricas simples antes de adicionar stack pesada.
- Nao implementar backup/restore completo nesta task; se necessario, criar task separada.

## Dependencias e Bloqueios

- Depende da task 1.1 para deploy e health checks basicos.
- Depende das tasks de ingestao, comandos e PDF quando as metricas exigirem eventos reais.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, dividir em metricas por subsistema.
- Nao agrupar retencao, LGPD, cadeia de custodia ou mapa offline nesta task.

## Testes Unitarios Obrigatorios

- [ ] Backend: metricas de fila, falhas de ingestao e falhas de comando.
- [ ] Backend: mascaramento de campos sensiveis em logs.

## Cenarios de Validacao Manual

- [ ] Derrubar dependencia em ambiente teste e confirmar falha visivel.
- [ ] Gerar comando expirado e confirmar metrica/log operacional.
- [ ] Confirmar que logs nao exibem coordenada sensivel, token ou chave.

## Criterio de Conclusao

- Operador consegue diagnosticar saude e falhas relevantes da instancia self-hosted sem expor dados sensiveis.
