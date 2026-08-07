---
title: "Product Brief: SimpleGuard"
project_name: "SimpleGuard"
status: "draft"
created: "2026-08-03"
updated: "2026-08-03"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/discovery/simpleguard-discovery.md"
  - "referencias/descricao_ideia_plataforma.md"
---

# Product Brief: SimpleGuard

## Resumo Executivo

O SimpleGuard e uma plataforma self-hosted de seguranca pessoal para monitorar, bloquear, emitir alarmes e gerar evidencias tecnicas sobre dispositivos perdidos ou roubados. O produto atua como uma central operacional: mapa dominante, telemetria tecnica, alertas persistentes, historico de deslocamento, deteccao de paradas e relatorio PDF para apoiar recuperacao, seguro e autoridades.

O diferencial do SimpleGuard e combinar resposta rapida com soberania do usuario. Em vez de depender apenas de solucoes comerciais fechadas ou de um ponto atual no mapa, o produto preserva dados, registra eventos auditaveis e transforma o incidente em uma linha do tempo tecnica. O bloqueio remoto nao deve apagar dados automaticamente; a prioridade e impedir uso indevido e preservar evidencias.

## Problema

Quando um dispositivo e perdido ou roubado, o proprietario precisa agir sob pressao. Ele pode perder acesso ao proprio fator de autenticacao, nao saber se o aparelho esta em movimento, nao ter historico confiavel de paradas e nao conseguir explicar tecnicamente o ocorrido para seguradora ou autoridade.

As solucoes tradicionais tendem a resolver apenas parte do problema: localizar um dispositivo, bloquear conta ou apagar dados. O SimpleGuard parte de outro foco: resposta operacional completa, com rastreamento, comandos remotos, auditoria e evidencia.

## Usuario-Alvo

### ICP Primario

Administrador/proprietario tecnico ou semitecnico que possui dispositivos pessoais ou profissionais com dados sensiveis e quer controle proprio sobre rastreamento, bloqueio, alarme e evidencias.

### Caracteristicas

- Valoriza privacidade, soberania e controle local.
- Aceita uma operacao self-hosted se isso aumentar autonomia.
- Tem preocupacao concreta com perda, roubo, seguro, auditoria e recuperacao.
- Precisa de uma interface direta para situacoes de emergencia.
- Quer evitar wipe automatico como resposta padrao.

### Decisao de Escopo

O MVP tera somente o perfil de administrador/proprietario. Usuarios secundarios, permissoes delegadas e pessoas autorizadas a receber alertas ficam fora do MVP.

## Proposta de Valor

Para proprietarios que precisam responder rapidamente a perda ou roubo de dispositivos, o SimpleGuard oferece um hub self-hosted de monitoramento e resposta que localiza, registra, bloqueia, emite alarmes e gera evidencias tecnicas confiaveis. Diferente de rastreadores simples, o produto prioriza preservacao de dados, trilha auditavel e relatorio estruturado do incidente.

## Solucao

O SimpleGuard oferece uma experiencia de central de monitoramento tatica. O mapa e a superficie principal, acompanhado por paineis tecnicos, estados claros e log cronologico.

O fluxo principal cobre:

- Preparar conta/instancia, autenticacao forte e pareamento de dispositivos.
- Receber localizacao e telemetria em intervalo alvo de 1 minuto.
- Detectar paradas quando o dispositivo permanece na mesma posicao por 10 minutos.
- Exibir dispositivo, historico, estado tecnico e eventos no mapa.
- Solicitar bloqueio remoto e emissao de alarme com confirmacao explicita.
- Registrar estados do comando: solicitado, entregue, executado, confirmado ou falho.
- Manter alertas ativos ate confirmacao manual do administrador.
- Gerar PDF com mapa, trajetoria, paradas, horarios e dados tecnicos.

## O Que Torna Diferente

- **Soberania do usuario:** orientado a operacao self-hosted e controle local.
- **Preservacao de evidencia:** bloqueia sem wipe automatico.
- **Historico operacional:** deslocamento, paradas e eventos viram trilha auditavel.
- **Resposta critica:** mapa, painel tecnico e comandos remotos focados no incidente.
- **Relatorio PDF:** evidencia exportavel, nao apenas visualizacao temporaria.
- **UX de monitoramento:** retro-tech/pixel-art funcional, sem virar campanha promocional ou dashboard SaaS generico.

## Escopo do MVP

- Cadastro e pareamento de dispositivos.
- Perfil unico de administrador/proprietario.
- Plataforma web do administrador em Angular + TypeScript.
- API/backend self-hosted em Java 21 + Spring Boot 3.
- SDK/agente Android em Kotlin como plataforma inicial, com escopo leve para telemetria e comandos minimos.
- Agentes de computador em Rust como servico/daemon nativo por sistema operacional, com Tauri opcional apenas para tray/configuracao local.
- Banco PostgreSQL + PostGIS.
- Autenticacao com senha mestra; biometria e chave fisica devem ser consideradas no desenho, mas podem depender da plataforma escolhida.
- Mapa operacional com todos os dispositivos.
- Visualizacao de dispositivo selecionado com centralizacao e aproximacao para contexto de rua quando disponivel.
- Ingestao de localizacao e telemetria em intervalo alvo de 1 minuto.
- Painel tecnico com bateria, sinal, rede, coordenadas, endereco e ultima atualizacao.
- Historico de deslocamento.
- Deteccao e exibicao de paradas apos 10 minutos na mesma posicao.
- Alertas persistentes ate confirmacao manual.
- Bloqueio remoto sem wipe automatico.
- Emissao de alarme remoto.
- Registro local no dispositivo quando estiver offline, com envio posterior quando a conexao retornar. [ASSUMPTION] Esta capacidade depende de suporte do agente instalado no dispositivo.
- Log/auditoria de eventos e acoes administrativas.
- Geracao de PDF do incidente com mapa, trajetoria, paradas e dados tecnicos.

## Fora do MVP

- Usuarios secundarios autorizados.
- Niveis de permissao alem do administrador.
- Delegacao de alertas para familiares ou terceiros.
- Zonas seguras.
- Diagnostico remoto completo.
- Monitoramento inteligente por comportamento.
- Sons, animacoes avancadas e refinamentos visuais nao essenciais.
- Wipe remoto automatico.
- Gamificacao, lore, UGC promocional, AR ou elementos herdados da referencia visual.

## Fase 2

- Diagnostico remoto de GPS, rede movel, Wi-Fi, bateria e sensores.
- Modo Vigia ativado manualmente.
- Zonas seguras.
- Melhorias na confiabilidade do armazenamento offline e sincronizacao posterior.
- Regras mais avancadas para alerta, permanencia e movimentacao.
- Refinamento do relatorio PDF com hash, assinatura, carimbo temporal ou cadeia de custodia, se isso for validado como necessario.
- Reforco de recuperacao de acesso com chave fisica e reautenticacao para comandos criticos.

## Backlog Futuro

- Usuarios secundarios e perfis delegados, caso o produto evolua para uso familiar ou microempresas.
- Permissoes granulares.
- Monitoramento inteligente por padroes de comportamento.
- Automacoes baseadas em zonas ou risco.
- Integracoes com seguradoras, boletim de ocorrencia ou armazenamento externo.
- Alertas multicanal.
- Experiencia visual mais rica com animacoes, som e microinteracoes.
- Suporte multi-inquilino, se houver decisao futura de produto gerenciado.

## Criterios de Sucesso

- O administrador consegue identificar rapidamente o estado de um dispositivo em incidente.
- O mapa mostra posicao atual, historico e paradas de forma legivel.
- O bloqueio remoto e a emissao de alarme possuem confirmacao e estado auditavel.
- Alertas nao desaparecem sem confirmacao manual.
- O PDF gerado e suficiente para explicar o incidente a terceiros.
- Falhas de conectividade, GPS e bateria aparecem como estados claros, nao como erros silenciosos.
- A UX transmite central de monitoramento sem comprometer velocidade e legibilidade.

## Lacunas Abertas Para PRD e Arquitetura

- Sistemas operacionais de computador que entram no MVP ou fases posteriores.
- Tecnologia dos agentes de computador, preservando escopo leve e sem interface rica.
- Modelo do agente instalado no dispositivo: instalacao, pareamento, atualizacao e permissoes.
- Viabilidade tecnica real de bloqueio remoto e alarme no Android.
- Fluxo completo de recuperacao de acesso quando o dispositivo principal foi roubado.
- Uso pratico da chave fisica de recuperacao.
- Retencao de telemetria, logs e relatorios.
- Formato probatorio minimo do PDF.
- Politica de dados pessoais, LGPD e consentimento.
- Precisao geografica minima para detectar parada e reduzir falso positivo.

## Direcao de UX

O produto deve parecer uma central de monitoramento tatica: mapa dominante, HUD retro-tech, paineis escuros de alto contraste, estados por cor e leitura tecnica imediata. A estetica pixel-art e referencia de linguagem visual, nao de conteudo. Em caso de conflito, clareza operacional vence ornamentacao.

## Proxima Etapa Recomendada

Usar este brief como entrada para o PRD do SimpleGuard, mantendo as lacunas abertas como decisoes explicitas. O PRD deve detalhar requisitos funcionais, estados do incidente, estados de comando remoto, modelo de auditoria, criterios de aceite e separacao entre MVP, fase 2 e backlog futuro.
