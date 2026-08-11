# Matriz de Cobertura das Tasks

Fonte revisada:
- `context/docs/produto/simpleguard-prd.md`
- `context/docs/produto/simpleguard-product-brief.md`
- `context/docs/contexto/project-context.md`
- `context/docs/arquitetura/simpleguard-arquitetura.md`
- `context/docs/pesquisa/simpleguard-risk-stack-research.md`
- `context/docs/ux/simpleguard-ux-plataforma-web.md`
- `context/docs/ux/simpleguard-ux-app-celular.md`
- `context/docs/ux/simpleguard-ux-app-computador.md`

## Cobertura Funcional

| Requisito | Cobertura |
| --- | --- |
| RF-01 - Autenticacao do Administrador | Epic 1: tasks 1.1, 1.2, 1.3; Epic 7 para confirmacao de comando critico. |
| RF-02 - Cadastro e Pareamento de Dispositivos | Epic 2: tasks 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7. |
| RF-03 - Ingestao de Localizacao e Telemetria | Epic 3: tasks 3.1, 3.2, 3.3, 3.7, 3.8, 3.9. |
| RF-04 - Mapa Operacional | Epic 4: tasks 4.1, 4.2, 4.3, 4.4. |
| RF-05 - Painel Tecnico do Dispositivo | Epic 3: task 3.4; Epic 4: tasks 4.1, 4.3, 4.4; Epic 7 para estados de comando. |
| RF-06 - Historico de Deslocamento | Epic 5: tasks 5.1, 5.2, 5.4; Epic 3 para coleta/sync offline. |
| RF-07 - Deteccao de Paradas | Epic 5: task 5.3; Epic 5.4 e Epic 8 para exibicao e relatorio. |
| RF-08 - Alertas Persistentes | Epic 6: tasks 6.1, 6.2, 6.3. |
| RF-09 - Bloqueio Remoto | Epic 7: tasks 7.1, 7.2, 7.3, 7.4, 7.5; desktop em fase posterior nas tasks 7.7 e 7.8. |
| RF-10 - Alarme Remoto | Epic 7: tasks 7.1, 7.2, 7.3, 7.4, 7.5; desktop em fase posterior nas tasks 7.7 e 7.8. |
| RF-11 - Log e Trilha de Auditoria | Epics 2, 5, 6, 7 e 8; task 9.3 evolui integridade formal. |
| RF-12 - Relatorio PDF do Incidente | Epic 8: tasks 8.1, 8.2, 8.3; Epic 5 fornece historico/paradas. |

## Cobertura Nao Funcional

| Requisito | Cobertura |
| --- | --- |
| RNF 11.1 - Seguranca | Epic 1: autenticacao e confirmacao; Epic 2: pareamento/revogacao; Epic 7: validade, idempotencia, fila e auditoria; Epic 9: hardening. |
| RNF 11.2 - Privacidade e LGPD | Epic 3: minimizacao de dados ausentes/null; task 9.2: retencao; task 9.6: politica de dados e RIPD. |
| RNF 11.3 - Disponibilidade e Operacao | Epic 1: deploy self-hosted; Epic 3: offline/sync; Epic 7: fila/expiracao; task 9.7: backup, restore e hardening. |
| RNF 11.4 - Desempenho | Epic 4: hub e SSE; Epic 8: geracao de PDF. |
| RNF 11.5 - Usabilidade Operacional | Epic 4: mapa dominante; Epic 3/7/8: paineis, estados e acoes sem navegacao profunda. |
| RNF 11.6 - Observabilidade | Task 9.5: metricas, logs tecnicos e saude da instancia. |

## Cobertura de Superficies

| Superficie | Cobertura |
| --- | --- |
| Web admin | Epics 1, 2, 3, 4, 5, 6, 7 e 8, seguindo `simpleguard-ux-plataforma-web.md` e Figma oficial. |
| Agente Android | Tasks 2.3, 2.6, 3.1, 3.2, 3.3, 3.5, 7.3, 7.4, 7.5 e 2.5 para despareamento. |
| Agente Desktop | Tasks 2.4, 2.7, 3.6, 3.7, 3.8, 3.9, 7.6, 7.7, 7.8 e 9.1. |
| Self-hosted/operacao | Tasks 1.1, 9.5, 9.7 e 9.2. |

## Resultado da Revisao

Depois dos ajustes, os requisitos funcionais RF-01 a RF-12 possuem task explicita ou cobertura por epic. Os requisitos nao funcionais de seguranca, privacidade, operacao, desempenho, usabilidade e observabilidade tambem possuem cobertura rastreavel.

Lacunas que continuam como decisoes de produto, nao como ausencia de task:
- Definir se bloqueio Android sera lock real do sistema operacional ou modo limitado pelo agente.
- Definir quais sistemas operacionais desktop entram em release antes de promover tasks desktop de fase posterior para MVP.
- Definir base legal/consentimento final com apoio juridico antes de release publico ou oferta gerenciada.
- Definir se mapas precisam operar offline ou apenas com tiles privados/contratados.
