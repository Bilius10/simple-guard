---
title: "UX Spec: SimpleGuard - Plataforma Web"
project_name: "SimpleGuard"
surface: "plataforma_web"
status: "draft"
created: "2026-08-04"
updated: "2026-08-04"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/produto/simpleguard-prd.md"
  - "docs/produto/simpleguard-product-brief.md"
  - "docs/discovery/simpleguard-discovery.md"
  - "referencias/descricao_plataforma_referencia.md"
  - "referencias/*.png"
figma_ready: true
link: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

---

# UX Spec: SimpleGuard - Plataforma Web

## 1. Objetivo da Superficie

A plataforma web e a central operacional do administrador. Ela deve permitir cadastrar dispositivos, visualizar estado patrimonial em mapa, analisar incidentes, acionar comandos remotos e gerar evidencias.

Esta superficie nao e uma landing page, dashboard SaaS generico ou experiencia promocional. A primeira tela util deve ser o hub de monitoramento.

## 2. Stack e Contexto de Uso

- **Frontend:** Angular + TypeScript.
- **Uso principal:** computador/notebook em navegador moderno.
- **Uso secundario:** tablet ou celular em emergencia, com layout responsivo.
- **Integracao:** API Java/Spring Boot via HTTP e WebSocket/SSE.
- **Mapa:** MapLibre GL JS.

## 3. Principios de UX

- Mapa dominante: o mapa ocupa a maior area visual em todas as telas operacionais.
- Leitura sob pressao: estado, localizacao, ultima atualizacao e comandos criticos devem ser vistos em segundos.
- HUD retro-tech funcional: molduras, divisorias, cantos marcados e tipografia tecnica apoiam a operacao.
- Pixel-art moderado: usar como linguagem de iconografia, bordas e detalhes, sem reduzir legibilidade.
- Paineis laterais: informam e comandam, mas nao competem com o mapa.
- Log cronologico: sempre tratar eventos como trilha auditavel.
- Estados explicitos: nunca esconder offline, falha de GPS, comando pendente ou dado antigo.

## 4. Linguagem Visual

### 4.1 Direcao

- Fundo principal escuro, proximo de azul-marinho/preto.
- Mapa em tons escuros com vias e areas em azul, ciano e verde dessaturado.
- Moldura externa grossa em ciano/azul, com contorno escuro.
- Paineis com fundo azul muito escuro ou grafite, borda ciano e pequenos cantos mecanicos.
- Tipografia de interface com cara tecnica/terminal, mas legivel.
- Densidade alta: evitar espacamento excessivo, cards grandes e composicao de marketing.

### 4.2 Paleta Funcional

| Token | Uso | Cor sugerida |
|---|---|---|
| `bg-base` | Fundo geral | `#061326` |
| `bg-map` | Mapa escuro | `#071B33` |
| `panel-bg` | Paineis | `#0B1A24` |
| `frame-cyan` | Molduras e foco | `#31B7D7` |
| `text-primary` | Texto principal | `#D7F7FF` |
| `text-muted` | Texto secundario | `#7FA6B6` |
| `state-safe` | Seguro | `#39D98A` |
| `state-watch` | Monitoramento | `#2F80ED` |
| `state-attention` | Atencao | `#F2C94C` |
| `state-alert` | Alerta | `#EB5757` |
| `state-offline` | Offline | `#8A93A3` |
| `state-locked` | Bloqueado | `#B983FF` |

### 4.3 Tipografia

- Titulo compacto: fonte monoespacada ou pixel-like legivel.
- Corpo: monoespacada legivel ou sans tecnica.
- Nao usar fonte ornamental em dados criticos.
- Evitar textos em caixa alta para paragrafos longos; reservar caixa alta para labels, estados e comandos.

## 5. Layout Base da Plataforma

### 5.1 Frame Desktop Figma

- Frame: `Platform / Desktop / 1440x900`.
- Margem externa: 16 px.
- Moldura HUD: 8 px, com contorno interno escuro.
- Area principal: mapa full-height abaixo do topo operacional.
- Coluna esquerda: trilho de navegacao compacto.
- Coluna direita: painel tecnico contextual.
- Rodape operacional: barra de status, fila de eventos e estado da conexao.

### 5.2 Grade Recomendada

- Topbar: 48 px.
- Trilho esquerdo: 56 px recolhido; 240 px expandido.
- Painel direito: 360 px desktop; drawer em telas pequenas.
- Log inferior opcional: 220 px quando expandido.
- Mapa: ocupa todo o espaco restante.

### 5.3 Responsivo

- Desktop grande: mapa + trilho + painel direito + log inferior opcional.
- Notebook: painel direito pode sobrepor o mapa como drawer.
- Tablet: navegacao vira barra inferior ou drawer.
- Celular: uso emergencial; priorizar lista de alertas, mapa e comandos, nao toda a administracao.

## 6. Navegacao

### 6.1 Itens Principais

- Monitoramento
- Dispositivos
- Incidentes
- Evidencias
- Auditoria
- Configuracoes

### 6.2 Regra

A navegacao nao deve virar menu corporativo extenso. O fluxo de incidente sempre tem prioridade sobre configuracoes e administracao.

## 7. Telas Para Figma

### 7.1 `Platform / 01 - Boot Operacional`

Objetivo: inicializacao rapida da central.

Conteudo:

- Logo textual `SIMPLEGUARD`.
- Linha de status: `Inicializando central`, `Conectando API`, `Sincronizando eventos`, `Carregando mapa`.
- Botao: `Entrar na central`.
- Link discreto: `Modo emergencia`.

Regras:

- Deve durar pouco e nao bloquear emergencia.
- Nao usar narrativa, personagem, chamada promocional ou video.

### 7.2 `Platform / 02 - Login`

Objetivo: autenticar administrador.

Conteudo:

- Campo usuario/email.
- Campo senha mestra.
- Opcao de passkey/WebAuthn quando disponivel.
- Acao principal: `Acessar`.
- Acao secundaria: `Recuperar acesso`.

Estados:

- Padrao.
- Erro de credencial.
- Reautenticacao exigida.
- Instancia indisponivel.

### 7.3 `Platform / 03 - Hub de Monitoramento`

Objetivo: tela principal operacional.

Anatomia:

- Mapa dominante.
- Topbar compacta com nome da instancia, status da API e relogio.
- Trilho esquerdo com filtros e navegacao.
- Painel direito com detalhe do dispositivo selecionado.
- Barra inferior com log resumido e estado do sistema.

Elementos do mapa:

- Marcadores de dispositivos.
- Trajetoria recente.
- Pontos de parada.
- Raio de confiabilidade/precisao.
- Estados por cor.
- Botao centralizar dispositivo.
- Botao ajustar zoom ao incidente.

Estados visuais de dispositivo:

- Seguro: verde.
- Monitoramento: azul.
- Atencao: amarelo.
- Alerta: vermelho.
- Offline: cinza.
- Bloqueado: roxo.

### 7.4 `Platform / 04 - Painel Tecnico do Dispositivo`

Objetivo: mostrar estado tecnico e comandos do dispositivo.

Campos:

- Nome do dispositivo.
- Estado operacional.
- Ultima atualizacao.
- Bateria.
- Sinal.
- Tipo de rede.
- Coordenadas.
- Endereco quando disponivel.
- Precisao estimada.
- Estado do agente.
- Comando atual, se existir.

Comandos:

- Centralizar.
- Ver historico.
- Abrir incidente.
- Solicitar bloqueio.
- Acionar alarme.
- Gerar PDF.

Regras:

- Bloqueio e alarme devem ser visualmente perigosos, mas nao exagerados.
- Comandos criticos exigem confirmacao.
- Dado ausente deve aparecer como `indisponivel`, nunca como valor falso.

### 7.5 `Platform / 05 - Confirmacao de Comando Critico`

Objetivo: evitar acionamento acidental.

Conteudo:

- Tipo de comando: bloqueio ou alarme.
- Dispositivo alvo.
- Ultima localizacao e ultima atualizacao.
- Consequencia operacional.
- Estado de conectividade.
- Campo de confirmacao ou botao de seguranca.
- Acao primaria: `Confirmar comando`.
- Acao secundaria: `Cancelar`.

Estados:

- Dispositivo online.
- Dispositivo offline: comando sera enfileirado.
- Sessao exige reautenticacao.
- Comando indisponivel para a plataforma.

### 7.6 `Platform / 06 - Incidente Ativo`

Objetivo: acompanhar uma ocorrencia em andamento.

Anatomia:

- Mapa com foco no dispositivo.
- Linha do tempo lateral.
- Painel de acoes.
- Resumo do incidente.

Eventos da linha do tempo:

- Localizacao recebida.
- Parada detectada.
- Alerta aberto.
- Bloqueio solicitado.
- Bloqueio entregue.
- Bloqueio executado.
- Bloqueio confirmado.
- Alarme solicitado.
- Falha de conectividade.
- PDF gerado.
- Incidente encerrado.

### 7.7 `Platform / 07 - Historico e Paradas`

Objetivo: analisar deslocamento.

Conteudo:

- Filtro de periodo.
- Trajetoria no mapa.
- Lista de paradas.
- Duracao por parada.
- Endereco e coordenadas.
- Precisao estimada.

Regras:

- Diferenciar ponto coletado de parada inferida.
- Mostrar gaps offline.
- Permitir centralizar parada no mapa.

### 7.8 `Platform / 08 - Relatorio PDF`

Objetivo: preparar e gerar relatorio operacional.

Conteudo:

- Seletor de incidente.
- Intervalo incluido.
- Secoes do PDF.
- Preview simplificado.
- Aviso: relatorio operacional, nao prova juridica formal.
- Acao: `Gerar PDF`.

Secoes do PDF:

- Resumo do incidente.
- Mapa estatico.
- Trajetoria.
- Paradas.
- Dados tecnicos.
- Linha do tempo.
- Limitacoes e periodos sem dados.

### 7.9 `Platform / 09 - Cadastro e Pareamento`

Objetivo: cadastrar um novo dispositivo.

Conteudo:

- Nome do dispositivo.
- Tipo: celular, notebook, desktop, outro.
- Plataforma: Android, Windows, Linux, macOS, outro.
- Metodo de pareamento.
- Codigo/QR de pareamento.
- Estado de validacao do agente.

Estados:

- Aguardando agente.
- Pareamento recebido.
- Permissoes incompletas.
- Pareado.
- Falha.

### 7.10 `Platform / 10 - Auditoria`

Objetivo: consultar eventos do sistema.

Conteudo:

- Filtros: periodo, dispositivo, incidente, tipo de evento, resultado.
- Lista cronologica densa.
- Detalhe do evento.
- Exportacao futura.

Regra:

- Auditoria deve parecer registro operacional, nao feed social.

## 8. Componentes Para Figma

- `HudFrame`
- `HudPanel`
- `TopStatusBar`
- `LeftRail`
- `SystemStatusPill`
- `DeviceMarker`
- `CommandButton`
- `CriticalCommandDialog`
- `TimelineEvent`
- `TelemetryRow`
- `MapControlButton`
- `AlertBanner`
- `EmptyStateOperational`
- `OfflineState`

## 9. Variantes de Componentes

### 9.1 `DeviceMarker`

- `safe`
- `watch`
- `attention`
- `alert`
- `offline`
- `locked`
- `selected`
- `stale-data`

### 9.2 `CommandButton`

- `default`
- `warning`
- `danger`
- `disabled`
- `pending`
- `confirmed`
- `failed`

### 9.3 `TimelineEvent`

- `observed`
- `inferred`
- `user-action`
- `command-requested`
- `command-delivered`
- `command-executed`
- `command-confirmed`
- `failure`

## 10. Estados e Microcopy

### 10.1 Estados de Dispositivo

| Estado | Texto curto | Uso |
|---|---|---|
| Seguro | `Seguro` | Online sem alerta. |
| Monitoramento | `Monitorando` | Acompanhamento ativo. |
| Atencao | `Atencao` | Sinal fraco, bateria baixa ou dado antigo. |
| Alerta | `Alerta ativo` | Incidente aberto. |
| Offline | `Offline` | Sem comunicacao recente. |
| Bloqueado | `Bloqueado` | Bloqueio executado/confirmado. |

### 10.2 Estados de Comando

| Estado | Texto na UI |
|---|---|
| `requested` | `Comando solicitado` |
| `queued` | `Aguardando entrega` |
| `delivered` | `Entregue ao agente` |
| `executed` | `Executado no dispositivo` |
| `confirmed` | `Confirmacao recebida` |
| `failed` | `Falhou` |
| `expired` | `Expirado` |
| `cancelled` | `Cancelado` |

## 11. Regras de Interacao Critica

- Bloqueio remoto nunca pode ser acao de um clique simples.
- Alarme remoto nunca pode ser acao de um clique simples.
- Se o dispositivo estiver offline, a UI deve mostrar que o comando pode ficar pendente ou expirar.
- Se o agente nao suportar o comando, o botao deve explicar indisponibilidade.
- Confirmacao de comando deve registrar evento mesmo quando o comando falhar.

## 12. O Que Nao Desenhar

- Hero promocional.
- Cards de marketing.
- Depoimentos, social proof ou campanhas.
- Avatares, personagens, lore, UGC ou eventos promocionais.
- Gamificacao, badges, ranking ou recompensas.
- Painel SaaS generico com metricas decorativas.
- Ilustracoes que ocupem o lugar do mapa.

## 13. Checklist Para Figma

- Criar frames desktop, tablet e mobile para o hub.
- Criar variantes dos marcadores por estado.
- Criar dialog de comando critico.
- Criar linha do tempo de incidente.
- Criar painel tecnico.
- Criar tela de pareamento.
- Criar tela de relatorio PDF.
- Criar tokens de cor e componentes HUD.
- Garantir que o mapa permaneca dominante.
- Garantir que nenhum frame pareca landing page.
