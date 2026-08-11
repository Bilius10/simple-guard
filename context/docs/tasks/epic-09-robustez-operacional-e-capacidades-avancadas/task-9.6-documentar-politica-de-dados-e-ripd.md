# Task 9.6: Documentar Politica de Dados e RIPD

Fase: fase posterior

## Contexto e Objetivo

Como responsavel pela instancia, quero documentar finalidade, minimizacao, retencao, direitos do titular e riscos de privacidade, para que o SimpleGuard possa evoluir para operacao publica ou gerenciada com base LGPD clara.

Esta task cobre as lacunas de privacidade e LGPD do PRD. Ela nao substitui analise juridica formal, mas cria o artefato tecnico necessario antes de release publico ou oferta gerenciada.

Nao considerar pronta se houver implementacao parcial, comportamento apenas mockado sem contrato claro, ausencia de testes unitarios ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Documentar finalidade por tipo de dado: localizacao, telemetria tecnica, identificador de dispositivo, eventos, comandos, logs e PDFs.
- [ ] Documentar minimizacao: quais dados sao obrigatorios, opcionais, ausentes/null e proibidos.
- [ ] Relacionar politica de retencao com a task 9.2.
- [ ] Mapear riscos principais: vazamento de localizacao, abuso/stalking, tomada de conta, perda de acesso e compartilhamento de relatorio.
- [ ] Definir checklist tecnico para RIPD/DPIA antes de operacao publica ou gerenciada.
- [ ] Testes unitarios obrigatorios implementados e passando quando houver regra automatizada.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/produto/simpleguard-prd.md`
- `docs/pesquisa/simpleguard-risk-stack-research.md`
- `task-9.2-configurar-retencao-de-dados.md`

Notas tecnicas:
- Separar documento tecnico de decisao juridica final.
- Nao implementar usuarios secundarios, consentimento delegado ou oferta gerenciada nesta task.
- Se alguma regra de minimizacao for automatizavel, criar validação no backend/agente junto da implementacao correspondente.

## Dependencias e Bloqueios

- Depende da definicao de tipos de dados coletados nas tasks 3.1, 3.2, 3.7 e 3.8.
- Depende da task 9.2 para prazos de retencao.
- Bloqueio: se a base legal ou politica de consentimento ainda nao estiver definida, registrar como decisao pendente e nao afirmar conformidade plena.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao ultrapassar 3 a 5 dias, separar documento de finalidade, matriz de dados e checklist RIPD.
- Nao agrupar usuarios secundarios, alertas multicanal ou oferta gerenciada nesta task.

## Testes Unitarios Obrigatorios

- [ ] Backend/agente: validar regras automatizadas de minimizacao apenas quando forem implementadas nesta task ou por dependencia direta.

## Cenarios de Validacao Manual

- [ ] Revisar matriz de dados contra os payloads reais de agente e API.
- [ ] Confirmar que PDF e logs indicam limitacoes e nao carregam dados desnecessarios.

## Criterio de Conclusao

- Plataforma possui politica tecnica de dados e checklist RIPD suficientes para orientar release publico ou operacao gerenciada.
