---
project_name: "SimpleGuard"
date: "2026-08-03"
source_material:
  - "docs/contexto/project-context.md"
  - "referencias/descricao_ideia_plataforma.md"
workflow: "bmad-brainstorming"
mode: "creative_partner"
status: "draft"
---

# Discovery do SimpleGuard

Documento consolidado para orientar os proximos artefatos BMAD: product brief, PRD, UX spec, arquitetura, epicos e stories.

## Premissas Usadas

- O SimpleGuard esta em fase de discovery e ainda nao possui base de codigo definida.
- A stack inicial aprovada e: plataforma web Angular/TypeScript, API Java 21/Spring Boot 3, banco PostgreSQL/PostGIS e agente Android Kotlin leve.
- A fonte principal de produto e `referencias/descricao_ideia_plataforma.md`.
- `docs/contexto/project-context.md` define guardrails de produto, UX, prioridades e lacunas.
- A referencia visual retro-tech/pixel-art deve inspirar experiencia operacional, nao narrativa promocional.
- Lacunas sem decisao formal permanecem abertas neste documento.

## Proposta de Valor

O SimpleGuard e uma plataforma self-hosted de seguranca pessoal para monitorar, bloquear e produzir evidencias tecnicas sobre dispositivos perdidos ou roubados.

Para usuarios que precisam agir rapido quando um dispositivo desaparece, o SimpleGuard oferece um hub operacional com mapa, telemetria, alertas persistentes, bloqueio remoto e relatorio PDF de evidencias. Diferente de solucoes comerciais fechadas ou focadas apenas em localizacao pontual, o produto prioriza soberania do usuario, preservacao dos dados e trilha auditavel para apoiar recuperacao, seguradora e autoridades.

## Problema Central

Quando um dispositivo e perdido ou roubado, o proprietario enfrenta quatro problemas ao mesmo tempo:

- Precisa localizar o dispositivo rapidamente.
- Precisa impedir uso indevido sem apagar dados que podem servir como evidencia.
- Pode perder tambem o aparelho usado para autenticacao.
- Precisa transformar eventos tecnicos em material compreensivel e confiavel para terceiros.

## Dores Prioritarias

- Vulnerabilidade de dados pessoais apos roubo.
- Dificuldade de resposta quando o celular principal tambem e o fator de autenticacao.
- Falta de historico claro de deslocamento e paradas em solucoes tradicionais.
- Ausencia de uma visao unica para varios dispositivos.
- Baixa confiabilidade percebida de registros quando nao ha trilha de auditoria.
- Dificuldade de explicar tecnicamente o incidente para seguradora, boletim de ocorrencia ou autoridade.

## ICP Primario

### Perfil

Administrador/proprietario tecnico ou semitecnico que possui multiplos dispositivos pessoais ou profissionais e quer manter controle proprio sobre rastreamento, bloqueio e evidencias.

### Caracteristicas

- Valoriza privacidade, soberania e controle local.
- Aceita operar ou contratar uma instancia self-hosted se isso aumentar autonomia.
- Possui celulares, notebooks ou equipamentos com dados sensiveis.
- Tem preocupacao pratica com roubo, perda, seguro, auditoria e recuperacao.
- Quer uma interface direta para situacoes de stress, nao um painel analitico generico.

### Jobs To Be Done

- Quando meu dispositivo some, quero ver rapidamente onde ele esta ou esteve, para decidir a proxima acao.
- Quando suspeito de roubo, quero bloquear o dispositivo sem apagar dados, para reduzir dano e preservar evidencia.
- Quando preciso acionar seguradora ou autoridade, quero gerar um PDF confiavel com mapa, trajetoria, paradas e eventos.
- Quando perco o celular usado para autenticar, quero conseguir recuperar acesso de forma segura.
- Quando perco o celular ou sou roubado, quero poder emitir alarmer

## ICP Secundario Ainda Aberto

Possiveis perfis secundarios, ainda sem decisao:

- Familiar autorizado a receber alertas.
- Equipe de TI pequena administrando dispositivos de uma microempresa.
- Responsavel por patrimonio digital domestico.
- Pessoa de confianca para recuperacao de acesso.
- Parceiro operacional que ajuda durante um incidente.

Decisao pendente: Náo teremos perfis securandarios, somente o do administrador

## Diferenciadores

- Bloqueio remoto/emissáo de alarmes com preservacao de dados, sem wipe automatico.
- Historico de deslocamento e deteccao de paradas como evidencia, nao so ponto atual no mapa.
- Alertas persistentes ate confirmacao manual do administrador.
- Relatorio PDF estruturado para uso externo.
- Experiencia visual de central de monitoramento, com mapa dominante e leitura operacional.
- Modelo self-hosted orientado a soberania do usuario.
- Trilha de auditoria separando eventos observados, comandos solicitados, comandos entregues, comandos executados e confirmacoes recebidas.

## Nao E

- Nao e uma landing page promocional.
- Nao e experiencia de entretenimento ou gamificacao.
- Nao e apenas "Find My Device" com outro visual.
- Nao deve copiar marca, narrativa, personagens ou objetivos da referencia Rastreador Aranha.
- Nao deve apagar dados automaticamente como resposta padrao.

## Fluxo Principal de Incidente

### 1. Preparacao Antes do Incidente

- Administrador cria conta ou instancia.
- Administrador configura senha mestra, biometria quando disponivel e chave fisica de recuperacao.
- Administrador cadastra e pareia dispositivos.
- Sistema valida permissao de localizacao e canal de comando remoto.
- Dispositivo passa a enviar localizacao e telemetria em intervalo alvo de 1 minuto.

### 2. Detecao ou Declaracao de Incidente

- Administrador abre o hub de monitoramento.
- Sistema exibe estado geral dos dispositivos no mapa.
- Administrador seleciona dispositivo perdido/roubado ou alerta ativo.
- Mapa centraliza o dispositivo e aproxima para contexto de rua quando disponivel.
- Painel tecnico mostra bateria, sinal, rede, coordenadas, endereco e ultima atualizacao.

### 3. Analise Operacional

- Administrador revisa ponto atual, historico recente e paradas.
- Sistema destaca permanencia de 10 minutos na mesma posicao como parada.
- Log cronologico apresenta eventos relevantes.
- Estados de conectividade e confiabilidade do dado ficam visiveis.

### 4. Resposta

- Administrador solicita bloqueio remoto.
- Sistema exige confirmacao explicita para acao critica.
- Comando entra como solicitado.
- Sistema registra entrega ao dispositivo quando possivel.
- Dispositivo executa bloqueio sem wipe automatico.
- Sistema registra confirmacao ou falha.

### 5. Acompanhamento

- Alerta permanece ativo ate confirmacao manual do administrador.
- Sistema continua registrando telemetria quando houver conectividade.
- Eventos de falha, bateria baixa, ausencia de GPS ou aparelho offline aparecem como estados esperados.
- Administrador pode acompanhar deslocamento e novas paradas.

### 6. Evidencia e Encerramento

- Administrador gera PDF do incidente.
- PDF inclui mapa, trajetoria, horarios, pontos de parada, tempo de permanencia e dados tecnicos.
- Relatorio separa dado factual, inferencia do sistema e acao do usuario.
- Administrador confirma ou encerra alerta manualmente.
- Trilha de auditoria permanece disponivel conforme regra de retencao ainda a definir.

Obs: Em caso de dispositvo sem internet, seria interessante ele ficar armazenando localmente esses dados, para quando a internet retornar
ele poder enviar esses dados

## Regras de Negocio Consolidadas

- Atualizacao de localizacao alvo: 1 minuto.
- Parada: permanencia na mesma posicao por 10 minutos.
- Bloqueio remoto: impedir uso e preservar dados.
- Wipe automatico: fora do comportamento padrao.
- Alertas: persistem ate confirmacao manual do administrador.
- Autenticacao: senha mestra, biometria quando disponivel e chave fisica de recuperacao.
- Rastreamento e comandos remotos: dependem de conectividade ativa.
- Falha de conectividade: estado normal do dominio, precisa ser representada na UX e na auditoria.

## MVP Proposto

- Plataforma web Angular para cadastro, visualizacao e operacao dos dispositivos.
- API Java/Spring Boot self-hosted.
- Agente Android Kotlin leve para telemetria e comandos minimos.
- Cadastro e pareamento de dispositivos.
- Mapa operacional com todos os dispositivos.
- Selecao de dispositivo ou alerta com centralizacao no mapa.
- Ingestao de localizacao e telemetria em intervalo alvo de 1 minuto.
- Painel tecnico com bateria, sinal, rede, coordenadas, endereco e ultima atualizacao.
- Historico de deslocamento.
- Deteccao e exibicao de paradas.
- Alertas persistentes.
- Bloqueio remoto com confirmacao e estado do comando.
- Log/auditoria de eventos e acoes administrativas.
- Geracao de relatorio PDF do incidente.

## Fase Posterior

- Usuarios secundarios autorizados.
- Niveis de permissao.
- Zonas seguras.
- Diagnostico remoto de GPS, rede movel, Wi-Fi, bateria e sensores.
- Modo Vigia ativado manualmente.
- Monitoramento inteligente por comportamento.
- Animacoes, sons e refinamentos visuais avancados.

## Lacunas que Precisam de Decisao

### Produto

- Definir se o MVP atende apenas uso pessoal ou tambem microempresas/equipes pequenas.
- Definir se usuarios secundarios entram no MVP.
- Definir quais papeis existem alem do administrador/proprietario.
- Definir quais comandos remotos existem alem de bloqueio.
- Definir criterio de sucesso do primeiro release.

### Plataforma

- Plataforma principal inicial decidida: web app Angular.
- Plataforma inicial de agente decidida: Android em Kotlin.
- Definir quais sistemas operacionais de computador entram no MVP ou fases posteriores.
- Definir tecnologia dos agentes de computador, mantendo escopo leve.
- Definir como o agente do dispositivo sera instalado, pareado e atualizado.
- Definir se o produto exige app nativo para bloqueio remoto ou se aceita capacidades limitadas por plataforma.

### Seguranca e Acesso

- Definir fluxo completo de recuperacao de acesso quando o celular principal foi roubado.
- Definir uso pratico da chave fisica de recuperacao.
- Definir politica de sessao, MFA e reautenticacao para comandos criticos.
- Definir modelo de autorizacao para terceiros.

### Dados e Evidencias

- Definir retencao de telemetria, logs e relatorios.
- Definir quais dados pessoais podem ser armazenados.
- Definir formato probatorio minimo do PDF.
- Definir se o relatorio precisa de assinatura, hash, carimbo temporal ou cadeia de custodia.
- Definir como separar dado coletado, dado inferido e acao manual.

### Operacao

- Definir comportamento quando dispositivo fica offline.
- Definir comportamento quando GPS esta desligado ou permissao e revogada.
- Definir comportamento quando bateria esta critica.
- Definir precisao geografica minima aceitavel.
- Definir tolerancia para falso positivo de parada.

### UX

- Definir grau de intensidade do estilo retro-tech no MVP.
- Definir se onboarding de inicializacao aparece sempre, so no primeiro acesso ou em modo demonstracao.
- Definir como evitar que a estetica atrase a tomada de decisao em emergencia.
- Definir estados visuais oficiais para seguro, atencao, alerta, monitoramento, offline e bloqueado.

## Decisoes Recomendadas Para Proximo Passo

- Tratar o MVP como produto pessoal/self-hosted para um administrador proprietario.
- Manter usuarios secundarios fora do MVP, mas preparar arquitetura de permissoes.
- Priorizar plataforma web Angular para o hub de monitoramento e agente Android Kotlin leve para a primeira POC de comandos remotos.
- Considerar bloqueio remoto como comando assincrono com estados auditaveis: solicitado, entregue, executado, confirmado ou falho.
- Definir PDF de evidencias como entregavel central do MVP, nao item secundario.
- Manter visual retro-tech/pixel-art como linguagem de interface, sem sacrificar legibilidade.

## Input Para Product Brief

O SimpleGuard resolve a resposta operacional a perda ou roubo de dispositivos para usuarios que valorizam privacidade, controle e evidencias. A primeira release deve focar no fluxo de incidente: localizar, entender estado tecnico, bloquear sem apagar dados, acompanhar eventos e gerar relatorio PDF. A experiencia deve parecer uma central de monitoramento tatica com mapa dominante, nao um dashboard SaaS nem uma campanha promocional. A superficie principal sera web com Angular e a API sera Java/Spring Boot. As principais incertezas restantes sao modelo de agente/dispositivo, agentes de computador, recuperacao de acesso, permissoes secundarias, retencao de dados e formato probatorio do PDF.
