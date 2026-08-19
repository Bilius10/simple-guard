# Task 9.8: Configurar Retencao Curta de Telemetria por Dispositivo

Fase: fase posterior

## Contexto e Objetivo

Como administrador, quero definir por quantos dias a telemetria sensivel de cada dispositivo ficara armazenada, para reduzir volume de dados e limitar exposicao de localizacao, rede, bateria e estado tecnico.

Esta task existe para criar uma politica simples e testavel de retencao por dispositivo, sem misturar ingestao de telemetria com limpeza historica.

Nao considerar pronta se houver implementacao parcial, limpeza sem validacao, ausencia de testes unitarios, exclusao de evidencia critica sem regra explicita ou falta de validacao manual executavel.

## Criterios de Aceite (Definition of Done)

- [ ] Adicionar configuracao de retencao de telemetria por dispositivo.
- [ ] Restringir valores permitidos inicialmente a `1`, `7` e `10` dias.
- [ ] Definir valor padrao seguro para dispositivos existentes.
- [ ] Implementar validacao de entrada no backend para impedir valores fora da politica aprovada.
- [ ] Criar limpeza agendada para dados de localizacao expirados em `device_locations`.
- [ ] Preparar extensao da limpeza para telemetria tecnica em `device_telemetry` apos a task 3.2.
- [ ] Preservar dados vinculados a incidente, evidencia ou relatorio quando houver regra de preservacao aplicavel.
- [ ] Registrar resultado da limpeza sem expor coordenadas, payloads sensiveis ou dados pessoais em logs.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Erros, estados vazios, estados de falha e dados ausentes tratados explicitamente quando aplicavel.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`
- `docs/pesquisa/simpleguard-risk-stack-research.md`
- `task-3.1-ingerir-localizacao-de-agente-pareado.md`
- `task-3.2-ingerir-telemetria-tecnica-do-dispositivo.md`
- `task-9.2-configurar-retencao-de-dados.md`

Notas tecnicas:
- A configuracao deve pertencer ao dispositivo, pois dispositivos diferentes podem exigir janelas de retencao diferentes.
- Sugestao inicial de coluna: `telemetry_retention_days integer not null default 7 check (telemetry_retention_days in (1, 7, 10))`.
- A limpeza deve usar `collected_at` como base temporal dos dados coletados pelo agente, nao `received_at`.
- `received_at` pode ser usado para diagnostico e desempate, mas nao deve substituir o horario real de coleta.
- Dados ausentes devem permanecer ausentes; limpeza nao deve criar registros agregados ou inferidos nesta task.
- Logs devem registrar contadores, device id quando necessario e janela aplicada, evitando coordenadas e payloads completos.
- Se houver dados ligados a incidente ou relatorio, parar e aplicar regra explicita antes de apagar.

## Dependencias e Bloqueios

- Depende da existencia de `devices`.
- Depende da task 3.1 para limpeza efetiva de `device_locations`.
- Depende da task 3.2 para incluir `device_telemetry` na limpeza.
- Bloqueio: se a regra de preservacao de evidencia vinculada a incidente ainda nao existir, nao apagar dados associados a evidencia sem decisao aprovada.
- Bloqueio: se a politica juridica/LGPD exigir retencao diferente, registrar decisao antes de fechar a task.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a implementacao exigir UI administrativa completa para configurar retencao, dividir em subtask propria.
- Nao agrupar configuracao global de auditoria, PDFs, backups ou retencao juridica ampla nesta task.

## Testes Unitarios Obrigatorios

- [ ] Backend: valor padrao de retencao em dispositivo novo/existente.
- [ ] Backend: rejeicao de valores fora de `1`, `7` e `10`.
- [ ] Backend: calculo de expiracao usando `collected_at`.
- [ ] Backend: limpeza remove somente localizacoes fora da janela configurada.
- [ ] Backend: limpeza preserva localizacoes dentro da janela configurada.
- [ ] Backend: dispositivo com retencao diferente remove quantidade diferente de pontos.
- [ ] Backend: dados vinculados a evidencia/incidente nao sao removidos sem regra explicita.

## Cenarios de Validacao Manual

- [ ] Configurar dispositivo com retencao de 1 dia e criar pontos antigos e recentes.
- [ ] Executar scheduler ou comando interno de limpeza em ambiente teste.
- [ ] Confirmar que pontos antigos foram removidos e pontos recentes permaneceram.
- [ ] Alterar retencao para 7 ou 10 dias e confirmar nova janela aplicada.
- [ ] Confirmar que logs da limpeza mostram contadores sem coordenadas sensiveis.

## Criterio de Conclusao

- Telemetria sensivel tem retencao curta por dispositivo, limpeza automatizada e preservacao explicita contra exclusao indevida de evidencia.
