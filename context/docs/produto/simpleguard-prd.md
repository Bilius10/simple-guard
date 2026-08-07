---
title: "PRD: SimpleGuard"
project_name: "SimpleGuard"
status: "draft"
created: "2026-08-04"
updated: "2026-08-04"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/discovery/simpleguard-discovery.md"
  - "docs/produto/simpleguard-product-brief.md"
  - "docs/pesquisa/simpleguard-risk-stack-research.md"
  - "referencias/descricao_ideia_plataforma.md"
  - "referencias/descricao_plataforma_referencia.md"
---

# PRD: SimpleGuard

## 1. Resumo

O SimpleGuard e uma plataforma self-hosted de seguranca patrimonial e pessoal para monitoramento, resposta a incidente e producao de evidencias sobre dispositivos perdidos ou roubados.

O produto deve operar como uma central de monitoramento: mapa dominante, leitura tecnica imediata, telemetria do dispositivo, historico de deslocamento, deteccao de paradas, alertas persistentes, bloqueio remoto, emissao de alarme e relatorio PDF do incidente.

Este PRD nao descreve uma landing page, campanha promocional, experiencia de entretenimento, gamificacao ou narrativa ficcional. A referencia visual retro-tech/pixel-art deve informar linguagem de interface, paineis, mapa e log, mas a prioridade e utilidade operacional sob pressao.

## 2. Objetivos do Produto

- Permitir que o administrador localize rapidamente dispositivos em perda ou roubo.
- Permitir resposta operacional com bloqueio remoto e alarme, sem wipe automatico.
- Preservar evidencias tecnicas: localizacao, trajetoria, paradas, telemetria, eventos e acoes administrativas.
- Gerar relatorio PDF compreensivel para seguradora, boletim de ocorrencia ou autoridade.
- Manter autonomia do usuario por meio de operacao self-hosted e controle local.
- Tornar falhas de conectividade, GPS, bateria e comando remoto visiveis como estados normais do dominio.

## 3. Nao Objetivos

- Criar experiencia promocional, narrativa, social ou gamificada.
- Copiar marca, personagens, lore, eventos, UGC, midia ou CTAs da referencia Rastreador Aranha.
- Prometer bloqueio remoto universal sem validacao por plataforma.
- Apagar dados automaticamente como resposta padrao.
- Tratar PDF como prova juridica formal sem assinatura, hash, carimbo temporal ou cadeia de custodia definidos.
- Incluir usuarios secundarios, permissoes delegadas ou times no MVP.

## 4. Usuario-Alvo

### 4.1 Perfil Primario

Administrador/proprietario tecnico ou semitecnico que possui dispositivos pessoais ou profissionais com dados sensiveis e precisa monitorar, bloquear, emitir alarme e documentar incidentes de perda ou roubo.

### 4.2 Caracteristicas

- Valoriza privacidade, soberania e controle local.
- Aceita operar ou contratar uma instancia self-hosted.
- Precisa tomar decisoes sob pressao.
- Quer evitar wipe automatico para preservar dados e evidencias.
- Precisa transformar eventos tecnicos em registro compreensivel para terceiros.

### 4.3 Decisao de Escopo

O MVP contempla apenas um papel: administrador/proprietario. Usuarios secundarios e niveis de permissao ficam fora do MVP.

## 5. Proposta de Valor

Para proprietarios que precisam responder rapidamente a perda ou roubo de dispositivos, o SimpleGuard oferece um hub operacional self-hosted que mostra localizacao, historico, estado tecnico, alertas e comandos remotos, alem de gerar evidencias em PDF. Diferente de rastreadores simples, o produto combina preservacao de dados, trilha auditavel e resposta a incidente em uma unica superficie operacional.

## 6. Principios de Produto

- **Resposta rapida:** acao critica com minimo de navegacao.
- **Preservacao de dados:** bloqueio sem wipe automatico.
- **Evidencia tecnica:** separar dado coletado, inferencia do sistema e acao do usuario.
- **Autonomia operacional:** favorecer self-hosted, controle local e dependencia minima de terceiros.
- **Legibilidade sob pressao:** mapa, estados e log devem ser claros antes de serem esteticos.
- **Auditoria por padrao:** eventos e comandos devem ter trilha cronologica e estados verificaveis.

## 7. Escopo do MVP

### 7.1 Capacidades Incluidas

- Cadastro, pareamento e administracao de dispositivos.
- Plataforma web do administrador para monitoramento operacional.
- API self-hosted em Java/Spring Boot.
- Agente Android em Kotlin, leve e focado em telemetria/comandos.
- Agentes de computador em Rust como servico/daemon nativo por sistema operacional, com Tauri opcional apenas para tray/configuracao local.
- Ingestao de localizacao e telemetria em intervalo alvo de 1 minuto.
- Mapa operacional com todos os dispositivos.
- Selecao de dispositivo ou alerta com centralizacao no mapa.
- Painel tecnico do dispositivo.
- Historico de deslocamento.
- Deteccao e exibicao de paradas.
- Alertas persistentes ate confirmacao manual.
- Bloqueio remoto com confirmacao explicita.
- Emissao de alarme remoto com confirmacao explicita.
- Estados auditaveis de comandos remotos.
- Log cronologico de eventos e acoes administrativas.
- Relatorio PDF do incidente.
- Registro local no agente quando offline e envio posterior quando a conexao retornar, condicionado a viabilidade da plataforma.

### 7.2 Fora do MVP

- Usuarios secundarios autorizados.
- Niveis de permissao alem do administrador.
- Zonas seguras.
- Diagnostico remoto completo de sensores.
- Monitoramento inteligente por comportamento.
- Wipe remoto automatico.
- Integracoes com seguradoras, delegacias ou sistemas externos.
- Assinatura digital formal, PDF/A, carimbo temporal e cadeia de custodia completa.
- Animacoes, sons e pixel-art avancada sem impacto operacional.

## 8. Fluxo Principal de Incidente

### 8.1 Preparacao

1. Administrador acessa a plataforma web SimpleGuard.
2. Administrador configura autenticacao inicial.
3. Administrador cadastra e pareia um dispositivo.
4. Sistema valida permissao de localizacao, telemetria minima e canal de comando.
5. Dispositivo passa a enviar localizacao e telemetria no intervalo alvo.

### 8.2 Declaracao ou Deteccao de Incidente

1. Administrador abre o hub de monitoramento.
2. Sistema exibe todos os dispositivos no mapa com estado atual.
3. Administrador seleciona dispositivo ou alerta ativo.
4. Sistema centraliza o mapa e apresenta painel tecnico.
5. Sistema destaca ultima localizacao, confiabilidade do dado e historico recente.

### 8.3 Analise Operacional

1. Administrador revisa trajetoria, paradas e eventos.
2. Sistema indica se o dispositivo esta online, offline, com bateria critica ou sem localizacao recente.
3. Log cronologico exibe eventos observados, inferencias e acoes do usuario.

### 8.4 Resposta

1. Administrador solicita bloqueio remoto ou alarme.
2. Sistema exige confirmacao explicita e, quando aplicavel, reautenticacao.
3. Sistema registra comando como solicitado.
4. Sistema entrega ou enfileira comando conforme conectividade.
5. Agente executa a acao quando possivel.
6. Sistema registra confirmacao, falha ou expiracao.

### 8.5 Acompanhamento e Evidencia

1. Alerta permanece ativo ate confirmacao manual do administrador.
2. Sistema continua coletando telemetria quando houver conectividade.
3. Administrador gera relatorio PDF do incidente.
4. PDF inclui mapa, trajetoria, paradas, horarios, dados tecnicos, eventos e acoes tomadas.
5. Administrador encerra o incidente manualmente.

## 9. Regras de Negocio

| Regra | Definicao |
|---|---|
| Atualizacao de localizacao | Intervalo alvo de 1 minuto. |
| Parada | Dispositivo considerado parado apos permanecer na mesma posicao por 10 minutos. |
| Bloqueio remoto | Deve impedir uso e preservar dados; nao deve executar wipe automatico. |
| Alarme remoto | Deve exigir confirmacao explicita e registrar estado do comando. |
| Alertas | Permanecem ativos ate confirmacao manual do administrador. |
| Mapa operacional | Deve mostrar todos os dispositivos e permitir centralizar dispositivo ou alerta. |
| PDF | Deve conter mapa, trajetoria, horarios, paradas, tempo de permanencia e dados tecnicos. |
| Conectividade | Rastreamento e comandos dependem de conectividade ativa; falhas devem aparecer no produto. |
| Auditoria | Eventos observados, comandos e acoes administrativas devem ser registrados separadamente. |

## 10. Requisitos Funcionais

### RF-01 - Autenticacao do Administrador

- O sistema deve permitir acesso seguro do administrador/proprietario.
- O sistema deve considerar senha mestra no MVP.
- O sistema deve preparar suporte a biometria, passkeys/WebAuthn ou chave fisica quando a plataforma permitir.
- Acoes criticas devem poder exigir reautenticacao.

**Criterios de aceite**

- Administrador consegue entrar e sair da instancia.
- Sessao expirada bloqueia acesso a dados operacionais.
- Bloqueio remoto e alarme nao podem ser enviados sem confirmacao explicita.

### RF-02 - Cadastro e Pareamento de Dispositivos

- O sistema deve permitir cadastrar dispositivo monitorado.
- O sistema deve gerar fluxo de pareamento entre hub e agente.
- O sistema deve registrar identificador do dispositivo, nome amigavel e estado de pareamento.
- O agente deve provar controle do dispositivo durante o pareamento.

**Criterios de aceite**

- Dispositivo pareado aparece no mapa e na lista operacional.
- Dispositivo nao pareado nao envia telemetria aceita pelo backend.
- Eventos de pareamento e despareamento aparecem na auditoria.

### RF-03 - Ingestao de Localizacao e Telemetria

- O agente deve enviar localizacao em intervalo alvo de 1 minuto quando permitido pela plataforma.
- O agente deve enviar bateria, sinal, tipo de rede, ultima atualizacao e coordenadas.
- Quando endereco estiver disponivel, o sistema deve exibi-lo como dado derivado.
- Dados recebidos fora de ordem devem ser tratados sem corromper a linha do tempo.

**Criterios de aceite**

- Nova localizacao aparece no mapa e no historico.
- Painel tecnico mostra ultima atualizacao e estado de confiabilidade.
- Falhas de GPS, permissao, bateria ou conexao ficam visiveis.

### RF-04 - Mapa Operacional

- O mapa deve ser a superficie primaria do produto.
- O mapa deve mostrar todos os dispositivos cadastrados.
- Ao selecionar um dispositivo ou alerta, o mapa deve centralizar o item.
- Quando disponivel, o mapa deve aproximar para contexto de rua.
- Estados devem ser representados por cor e icone legiveis.

**Criterios de aceite**

- Administrador identifica rapidamente quais dispositivos estao seguros, em atencao, em alerta, offline ou bloqueados.
- Selecionar alerta leva ao dispositivo relacionado.
- O mapa nao deve ser substituido por cards promocionais ou layout de marketing.

### RF-05 - Painel Tecnico do Dispositivo

- O sistema deve exibir bateria, sinal, tipo de rede, ultima atualizacao, coordenadas e endereco quando disponivel.
- O painel deve indicar estado de conectividade.
- O painel deve indicar se ha comando pendente, entregue, executado, confirmado, falho ou expirado.

**Criterios de aceite**

- Dados tecnicos essenciais ficam visiveis sem navegacao profunda.
- Dados ausentes aparecem como ausentes, nao como zero ou valor inventado.
- Estados de comando aparecem com timestamp.

### RF-06 - Historico de Deslocamento

- O sistema deve exibir trajetoria historica do dispositivo.
- O historico deve permitir filtrar por janela de tempo do incidente.
- O sistema deve preservar ordem cronologica dos pontos.

**Criterios de aceite**

- Administrador consegue ver ponto atual e caminho recente.
- Pontos coletados offline e enviados depois aparecem com horario original.
- Relatorio PDF usa a mesma linha do tempo do historico.

### RF-07 - Deteccao de Paradas

- O sistema deve detectar parada quando o dispositivo permanecer na mesma posicao por 10 minutos.
- A regra precisa considerar tolerancia geografica ainda a definir.
- Paradas devem ter inicio, fim, duracao, coordenadas e endereco quando disponivel.

**Criterios de aceite**

- Paradas aparecem no mapa e no log.
- O PDF lista paradas e tempo de permanencia.
- A deteccao deve diferenciar dado coletado de inferencia do sistema.

### RF-08 - Alertas Persistentes

- O sistema deve criar alerta para incidente declarado ou evento critico.
- Alertas devem permanecer ativos ate confirmacao manual.
- Encerramento de alerta deve gerar evento de auditoria.

**Criterios de aceite**

- Alerta nao desaparece automaticamente apos atualizacao de tela.
- Administrador consegue confirmar ou encerrar alerta.
- Log registra abertura, atualizacoes e encerramento.

### RF-09 - Bloqueio Remoto

- O administrador deve poder solicitar bloqueio remoto de dispositivo.
- O bloqueio nao deve apagar dados automaticamente.
- O comando deve ser assincrono, idempotente e auditavel.
- O sistema deve exibir estado do comando.

**Estados minimos**

- `requested`
- `queued`
- `delivered`
- `executed`
- `confirmed`
- `failed`
- `expired`
- `cancelled`

**Criterios de aceite**

- Comando exige confirmacao explicita.
- Se o dispositivo estiver offline, comando fica pendente ou expira conforme regra definida.
- Cada transicao de estado gera evento auditavel.
- O usuario entende se o bloqueio foi apenas solicitado, entregue, executado ou confirmado.

### RF-10 - Alarme Remoto

- O administrador deve poder solicitar emissao de alarme remoto.
- O comando deve ter confirmacao explicita.
- O comando deve ter limite de repeticao e expiracao a definir.
- O estado do comando deve seguir o mesmo modelo de auditoria do bloqueio.

**Criterios de aceite**

- Alarme nao pode ser acionado por clique acidental.
- Sistema mostra se o alarme foi entregue, executado, confirmado, falho ou expirado.
- Evento entra no log e no relatorio do incidente.

### RF-11 - Log e Trilha de Auditoria

- O sistema deve manter log cronologico de eventos.
- O log deve separar evento observado, inferencia do sistema, comando solicitado, comando entregue, comando executado, confirmacao recebida e acao administrativa.
- O log deve registrar timestamp, ator, dispositivo, tipo de evento e resultado.

**Criterios de aceite**

- Administrador consegue reconstruir a linha do tempo do incidente.
- Eventos criticos nao podem ser editados pelo usuario comum da interface.
- PDF preserva a ordem e a classificacao dos eventos relevantes.

### RF-12 - Relatorio PDF do Incidente

- O sistema deve gerar PDF do incidente selecionado.
- O PDF deve incluir mapa, trajetoria, horarios, pontos de parada, tempo de permanencia, dados tecnicos e eventos.
- O PDF deve separar dado factual coletado, inferencia do sistema e acao do administrador.
- O PDF deve indicar limitacoes, como periodos offline ou baixa precisao.

**Criterios de aceite**

- Administrador consegue gerar PDF a partir de um incidente.
- PDF contem dados suficientes para explicar o incidente a seguradora, boletim de ocorrencia ou autoridade.
- PDF nao afirma garantia juridica formal quando cadeia de custodia nao estiver implementada.

## 11. Requisitos Nao Funcionais

### 11.1 Seguranca

- TLS obrigatorio em qualquer operacao remota.
- Comandos remotos devem ter validade, identificador unico e protecao contra repeticao.
- Payload de push deve ser minimo e nao deve carregar localizacao sensivel.
- Eventos criticos devem ser append-only ou protegidos contra alteracao silenciosa.
- Acoes criticas devem exigir confirmacao e podem exigir reautenticacao.

### 11.2 Privacidade e LGPD

- Coletar apenas dados necessarios para monitoramento, resposta e evidencia.
- Documentar finalidade de localizacao, telemetria, logs e relatorios.
- Definir retencao antes de release publico.
- Permitir exclusao/exportacao conforme modelo juridico definido.
- Produzir RIPD/DPIA antes de operacao publica ou gerenciada.

### 11.3 Disponibilidade e Operacao

- O produto deve tratar offline como estado esperado.
- O agente deve armazenar eventos localmente quando offline, se a plataforma permitir.
- O backend deve manter fila de comandos com expiracao.
- A instancia self-hosted deve ter configuracao operacional simples.

### 11.4 Desempenho

- A interface deve abrir o hub operacional sem bloqueios narrativos longos.
- Atualizacoes de mapa e painel devem refletir novos eventos em tempo util.
- Relatorio PDF deve ser gerado em tempo aceitavel para uso durante ou apos incidente.

### 11.5 Usabilidade Operacional

- A primeira tela util deve ser o hub de monitoramento, nao uma pagina promocional.
- Mapa, painel tecnico, log e comandos criticos devem estar acessiveis sem navegacao profunda.
- Texto e estados devem permanecer legiveis em desktop e mobile.
- Elementos visuais retro-tech nao podem reduzir contraste, leitura ou velocidade de acao.

### 11.6 Observabilidade

- Registrar falhas de ingestao, comando, PDF, autenticacao e agente.
- Expor saude da instancia e fila de comandos para administracao.
- Logs tecnicos devem evitar dados pessoais desnecessarios.

## 12. Estados Operacionais

### 12.1 Estado do Dispositivo

| Estado | Significado |
|---|---|
| Seguro | Dispositivo online sem alerta ativo. |
| Monitoramento | Dispositivo acompanhado ativamente pelo administrador. |
| Atencao | Sinal fraco, bateria baixa, GPS instavel ou atraso relevante. |
| Alerta | Incidente ativo ou evento critico pendente de confirmacao. |
| Offline | Sem comunicacao recente. |
| Bloqueado | Bloqueio confirmado ou executado conforme suporte da plataforma. |

### 12.2 Estado do Comando

| Estado | Significado |
|---|---|
| `requested` | Administrador solicitou comando. |
| `queued` | Backend registrou e aguarda entrega. |
| `delivered` | Agente recebeu o comando. |
| `executed` | Agente executou a acao local. |
| `confirmed` | Backend recebeu confirmacao final. |
| `failed` | Comando falhou. |
| `expired` | Comando perdeu validade antes de execucao. |
| `cancelled` | Comando foi cancelado antes da execucao. |

## 13. Direcao de UX

- A experiencia deve parecer uma central de monitoramento tatica.
- Mapa e contexto espacial sao a area primaria.
- Paineis, logs e comandos apoiam o mapa.
- Usar fundo escuro, alto contraste, azul/ciano para estrutura e verde/amarelo/vermelho/azul para estado.
- A estetica retro-tech/pixel-art deve ser aplicada como linguagem funcional de HUD.
- Onboarding pode simular inicializacao rapida, mas nao deve bloquear resposta em emergencia.
- Evitar hero, CTA promocional, social proof, cards de marketing, narrativa ficcional e elementos de campanha.

## 14. Metricas de Sucesso

- Tempo ate identificar o estado do dispositivo apos abrir o hub.
- Percentual de incidentes com linha do tempo completa.
- Percentual de comandos com estado final claro: confirmado, falho ou expirado.
- Percentual de alertas encerrados manualmente com registro de auditoria.
- Percentual de relatorios PDF gerados com mapa, trajetoria, paradas e eventos.
- Taxa de eventos offline sincronizados com sucesso apos reconexao.
- Quantidade de falhas silenciosas: meta deve ser zero para comando critico e alerta.

## 15. Stack Tecnica Aprovada

- API/backend em Java 21 + Spring Boot 3.
- Plataforma web do administrador em Angular + TypeScript.
- SDK/agente Android em Kotlin, com escopo leve para telemetria, alarme e comandos remotos minimos.
- Agentes de computador em Rust como servico/daemon nativo por sistema operacional, com Tauri opcional apenas para tray/configuracao local.
- PostgreSQL + PostGIS para geodados.
- MapLibre GL JS com tiles OSM/OpenMapTiles ou provedor contratado.
- Geracao de PDF via HTML/CSS renderizado com Chromium/Playwright.
- Autenticacao forte com OIDC e WebAuthn/passkeys quando aplicavel.
- Tempo real via WebSocket ou SSE entre plataforma web Angular e API.
- Canal de comando assincrono com fila, expiracao, assinatura e auditoria.
- Superficie inicial em formato web para cadastro, visualizacao e operacao dos dispositivos.
- Plataforma inicial de agente: Android.

## 16. Riscos e Mitigacoes

| Risco | Impacto | Mitigacao |
|---|---|---|
| Bloqueio remoto limitado por plataforma | Produto prometer mais do que consegue executar | Validar POC por sistema operacional antes de comprometer escopo publico. |
| Uso abusivo para vigilancia de terceiros | Risco legal e de seguranca | Exigir pareamento com controle fisico, indicador local quando adequado e termos de uso autorizado. |
| Vazamento de localizacao | Alto impacto de privacidade | Criptografia, minimizacao, controle self-hosted, retencao definida e hardening. |
| Offline durante incidente | Comandos e localizacao atrasados | Tratar como estado esperado, fila de comandos e armazenamento local com sincronizacao posterior. |
| PDF interpretado como prova formal | Risco de comunicacao enganosa | Indicar limitacoes e separar fatos, inferencias e acoes. |
| Mapa de terceiro vazar coordenadas | Exposicao indireta | Preferir tiles self-hosted ou provedor contratado com termos claros. |
| Perda do fator principal de autenticacao | Administrador sem acesso em emergencia | Desenhar recuperacao com chave fisica/passkeys antes de release critico. |

## 17. Lacunas Abertas

### Produto

- Confirmar se o primeiro release atende apenas uso pessoal ou tambem microempresas.
- Definir criterio de sucesso quantitativo do primeiro release.
- Definir quais comandos remotos alem de bloqueio e alarme entram no futuro.

### Plataforma

- Definir se o MVP Android exige bloqueio real do sistema operacional ou aceita modo de bloqueio limitado por agente.
- Definir como o agente sera instalado, atualizado, autenticado e despareado.
- Definir quais sistemas operacionais de computador entram no MVP ou fases posteriores.
- Definir matriz de capacidades dos agentes de computador por sistema operacional, mantendo escopo leve e sem interface rica.

### Seguranca e Acesso

- Definir fluxo completo de recuperacao quando o dispositivo principal foi roubado.
- Definir uso pratico da chave fisica de recuperacao.
- Definir politica de sessao, MFA e reautenticacao para comandos criticos.

### Dados e Evidencias

- Definir retencao de telemetria, logs e relatorios.
- Definir formato probatorio minimo do PDF.
- Definir se havera hash, assinatura, PDF/A, carimbo temporal ou cadeia de custodia.
- Definir politica LGPD, base legal, consentimento e direitos do titular.

### Operacao

- Definir raio e tolerancia para "mesma posicao" na deteccao de parada.
- Definir comportamento para GPS desligado, permissao revogada, bateria critica e aparelho desligado.
- Definir expiracao padrao de comandos remotos.
- Definir se mapas precisam operar offline ou apenas com tiles privados/contratados.

## 18. Plano de Release

### MVP

- Plataforma web operacional com mapa.
- Cadastro e pareamento de dispositivos.
- API Java/Spring Boot self-hosted.
- Agente Android em Kotlin.
- Telemetria basica.
- Historico e paradas.
- Alertas persistentes.
- Bloqueio remoto e alarme com estados auditaveis.
- Log cronologico.
- PDF de incidente.

### Fase 2

- Diagnostico remoto de GPS, rede, Wi-Fi, bateria e sensores.
- Zonas seguras.
- Regras avancadas de comportamento.
- Melhorias de evidencia: hash, assinatura, carimbo temporal e PDF/A.
- Recuperacao de acesso reforcada.
- Melhorias no agente offline.

### Backlog Futuro

- Usuarios secundarios e permissoes granulares.
- Monitoramento inteligente por comportamento.
- Alertas multicanal.
- Integracoes externas.
- Modo Vigia manual.
- Refinamentos visuais, sons e microinteracoes.

## 19. Criterios de Pronto do MVP

- Administrador consegue parear ao menos um dispositivo suportado.
- API Java/Spring Boot registra dispositivo, telemetria, eventos e comandos.
- Plataforma web Angular mostra o hub operacional.
- Agente Android Kotlin executa a POC de telemetria e comando remoto minimo.
- Dispositivo envia localizacao e telemetria basica.
- Hub mostra dispositivo no mapa com painel tecnico.
- Historico de deslocamento e paradas ficam visiveis.
- Alerta permanece ativo ate encerramento manual.
- Bloqueio remoto e alarme exigem confirmacao e registram estados.
- Falhas de conectividade aparecem de forma clara.
- Log permite reconstruir o incidente.
- PDF inclui mapa, trajetoria, paradas, eventos e dados tecnicos.
- Nenhum elemento promocional ou narrativo interfere no fluxo operacional.
