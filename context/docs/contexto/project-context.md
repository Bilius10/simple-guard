---
project_name: "SimpleGuard"
user_name: "Joao Vitor Oliveira"
date: "2026-08-03"
sections_completed:
  - product_domain
  - product_pillars
  - source_material
  - reference_experience
  - business_rules
  - mvp_priorities
  - future_scope
  - discovery_gaps
  - planning_rules
existing_patterns_found: 0
status: "discovery"
optimized_for_llm: true
---

# Project Context for AI Agents

Este projeto esta em fase de discovery e planejamento. Ainda nao existe uma base de codigo consolidada para ser tratada como fonte de verdade arquitetural.

Use este arquivo para manter consistencia entre artefatos BMAD, decisoes de produto, UX, arquitetura, epicos e stories. Nao trate lacunas abertas como decisoes tomadas.

## Product Domain

- O produto e o `SimpleGuard`, uma plataforma de seguranca pessoal para monitoramento e recuperacao de dispositivos em caso de perda ou roubo.
- O foco nao e marketing, entretenimento, gamificacao ou storytelling promocional. O foco e resposta rapida, preservacao de evidencias e autonomia do usuario.
- A plataforma deve permitir monitorar varios dispositivos em um mapa unico, bloquear remotamente, manter trilha de auditoria e gerar evidencias para seguradoras e autoridades.
- O produto deve priorizar soberania do usuario, operacao self-hosted e preservacao dos dados do dispositivo.
- O usuario principal conhecido e o administrador/proprietario dos dispositivos.

## Product Pillars

- **Resposta rapida:** permitir que o usuario entenda o estado do incidente e tome acao com o minimo de navegacao.
- **Preservacao de dados:** bloquear o dispositivo sem apagar dados automaticamente.
- **Evidencia tecnica:** manter historico, trajetoria, paradas, eventos e dados tecnicos exportaveis em PDF.
- **Autonomia operacional:** favorecer controle local/self-hosted e reduzir dependencia de terceiros quando isso nao prejudicar seguranca.
- **Legibilidade sob pressao:** priorizar mapa, estados claros, alertas persistentes e leitura tecnica imediata.
- **Seguranca de acesso:** considerar senha mestra, biometria quando disponivel e chave fisica de recuperacao.

## Source Material

- `referencias/descricao_ideia_plataforma.md` e a fonte principal da visao de produto, dores, objetivos, regras de negocio, riscos e prioridades.
- `referencias/descricao_plataforma_referencia.md` e as imagens em `referencias/*.png` sao referencia de experiencia, navegacao e linguagem visual.
- Figma oficial do SimpleGuard: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0
- Para tarefas de frontend da plataforma web, app celular e app computador, usar o Figma oficial junto com os UX specs em `docs/ux/`.
- A referencia "Rastreador Aranha" nao deve contaminar o dominio com elementos ficcionais, marcas, personagens, venda de ingressos, social proof, UGC promocional, lore, AR ou objetivos de campanha.
- A referencia deve ser usada apenas para informar mapa dominante, HUD retro-tech, ritmo de onboarding, linguagem visual pixel-art, composicao dos paineis, log cronologico e sensacao de sistema de vigilancia.
- Quando houver conflito entre a referencia visual e a utilidade operacional do SimpleGuard, a utilidade operacional vence.

## Reference UX Direction

- O produto deve transmitir uma central de monitoramento tatica, direta e legivel.
- O mapa e a area primaria da interface. Paineis, logs e controles devem apoiar o mapa, nao competir com ele.
- A aplicacao deve evitar visual de dashboard SaaS generico, landing page, cards promocionais e layout de marketing.
- A linguagem visual desejada inclui molduras e paineis em azul/ciano de alto contraste, fundo escuro com mapa dominante, tipografia com cara de terminal/arcade/pixel e uso de verde, amarelo, vermelho e azul para estados.
- O estilo pode ser forte, mas a leitura operacional deve vir antes de ornamentacao.
- O onboarding pode simular inicializacao de sistema, autoteste e carregamento de modulos, desde que seja rapido e nao bloqueie a resposta em emergencia.
- O log deve ser cronologico, auditavel e orientado a eventos reais: localizacao recebida, parada detectada, alerta aberto, bloqueio solicitado, bloqueio confirmado, relatorio gerado.
- O painel tecnico deve exibir no minimo bateria, sinal, tipo de rede, ultima atualizacao, coordenadas e endereco quando disponivel.
- Interacoes criticas, como bloqueio remoto, devem ter confirmacao explicita e indicar estado pendente, confirmado ou falho.

## Business Rules

- **Atualizacao de localizacao:** o intervalo alvo e 1 minuto.
- **Definicao de parada:** um dispositivo e considerado parado apos permanecer na mesma posicao por 10 minutos.
- **Bloqueio remoto:** deve impedir uso e preservar dados; nao deve fazer wipe automatico.
- **Alertas persistentes:** alertas permanecem ativos ate confirmacao manual do administrador.
- **Autenticacao:** deve considerar senha mestra, biometria quando disponivel e chave fisica de recuperacao.
- **Mapa operacional:** deve permitir visualizar todos os dispositivos, selecionar um alerta, centralizar o dispositivo e aproximar para contexto de rua quando disponivel.
- **Relatorio PDF:** deve conter mapa, trajetoria, horarios, pontos de parada, tempo de permanencia e dados tecnicos do dispositivo.
- **Dependencia de conectividade:** rastreamento e comando remoto dependem de conectividade ativa do dispositivo monitorado; falhas devem ser visiveis no produto.

## MVP Priorities

- Bloqueio remoto.
- Rastreamento e visualizacao em mapa.
- Alertas persistentes.
- Painel tecnico com bateria, sinal, rede, ultima atualizacao, coordenadas e endereco quando disponivel.
- Historico de deslocamento.
- Deteccao e exibicao de paradas.
- Relatorio PDF com mapa, trajetoria, paradas e dados tecnicos.
- Trilha de auditoria para eventos de seguranca e acoes administrativas.

## Approved Technical Stack

- **API/backend:** Java 21 + Spring Boot 3.
- **Plataforma web do administrador:** Angular + TypeScript.
- **SDK/agente Android:** Kotlin, com escopo leve para telemetria, alarme e comandos remotos minimos.
- **Agente de computador:** Rust como servico/daemon nativo por sistema operacional; Tauri apenas para tray/configuracao local quando houver UI. O agente deve apenas coletar/enviar telemetria e executar comandos suportados.
- **Banco de dados:** PostgreSQL + PostGIS.
- **Mapa:** MapLibre GL JS.
- **PDF:** HTML/CSS renderizado via Chromium/Playwright, a definir se executado no backend ou worker.
- **Tempo real:** WebSocket ou SSE entre plataforma web Angular e API.
- **Superficie inicial:** plataforma web operacional para cadastrar, visualizar e operar dispositivos.
- **Plataforma inicial de agente:** Android, com agentes de computador em fase posterior ou POC especifica.
- **Tecnologia do agente de computador:** Rust para core/servico; Tauri opcional para tray/configuracao.

## Future Scope

- Zonas seguras.
- Diagnostico remoto de GPS, rede movel, Wi-Fi, bateria e sensores.
- Monitoramento inteligente e alertas por comportamento.
- Usuarios secundarios autorizados, apos definicao de permissoes.
- Refinamentos visuais, animacoes, sons e microinteracoes.
- Modo Vigia ativado manualmente.

## Discovery Gaps

Estas lacunas devem permanecer explicitas em PRD, UX, arquitetura, epicos e stories ate decisao formal:

- Quem sao os usuarios secundarios autorizados a receber alertas.
- Quais niveis de permissao existem alem do administrador/proprietario.
- Como funciona o fluxo completo de recuperacao de acesso quando o dispositivo principal foi roubado.
- Como o agente instalado no dispositivo sera distribuido, autenticado e pareado com a instancia self-hosted.
- Quais sistemas operacionais de computador entram no MVP ou fases posteriores.
- Quais comandos remotos existem alem do bloqueio.
- Qual nivel de precisao geografica e aceitavel para parada, rua, predio e historico.
- Como tratar ausencia de internet, GPS desligado, bateria baixa, aparelho desligado e permissao de localizacao revogada.
- Qual formato probatorio minimo o PDF precisa atender para seguradora, boletim de ocorrencia ou autoridade.
- Quais dados pessoais podem ser armazenados e por quanto tempo.
- Como LGPD, consentimento e acesso por terceiros serao tratados.

## Planning Rules

- Todos os artefatos devem ser produzidos em portugues do Brasil.
- Explicite assuncoes e incertezas. Nao feche lacunas com invencao silenciosa.
- Separe claramente `MVP`, `fase posterior` e `ideias futuras`.
- Em UX, preserve a referencia retro-tech/pixel-art sem copiar conteudo, marca, personagens, eventos ou narrativas da referencia.
- Em tarefas de front, consultar o Figma oficial e o UX spec da superficie antes de implementar tela, componente, estado visual ou fluxo.
- Em arquitetura, prefira stack confiavel, boring tech, boa observabilidade, trilha de auditoria e deploy self-hosted simples.
- Em epicos e stories, estruture por capacidade real do produto, nao por telas isoladas.
- Em regras de seguranca, diferencie evento observado, comando solicitado, comando entregue, comando executado e confirmacao recebida.
- Em fluxos de incidente, trate falhas de conectividade como estado esperado, nao como excecao rara.
- Em relatorios, separe dado factual coletado, inferencia do sistema e acao tomada pelo usuario.
- Nao adicionar funcionalidades promocionais herdadas da referencia visual.

## Suggested Capability Breakdown

- Gestao de acesso e recuperacao.
- Cadastro, pareamento e administracao de dispositivos.
- Ingestao de localizacao e telemetria.
- Mapa operacional e monitoramento.
- Deteccao de paradas e historico.
- Bloqueio remoto e resposta a incidente.
- Evidencias, relatorios e exportacao PDF.
- Configuracoes, alertas e auditoria.
- Observabilidade, retencao de dados e administracao self-hosted.

## Usage Guidelines

- Agentes devem ler este arquivo antes de criar PRD, UX spec, arquitetura, epicos, stories ou codigo.
- Agentes devem ler tambem `contexto/project-constitution.md` antes de alterar codigo, contratos ou padroes de desenvolvimento.
- Quando uma decisao nao estiver neste arquivo nem nas referencias, marque como lacuna ou assuncao.
- Atualize este arquivo quando uma lacuna de discovery for decidida formalmente.
- Mantenha o arquivo enxuto e focado em regras que previnem erro de interpretacao.
