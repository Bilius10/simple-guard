---
title: "UX Spec: SimpleGuard - App Computador / Agente Desktop"
project_name: "SimpleGuard"
surface: "app_computador_agente"
status: "draft"
created: "2026-08-04"
updated: "2026-08-04"
source_material:
  - "docs/contexto/project-context.md"
  - "docs/produto/simpleguard-prd.md"
  - "docs/produto/simpleguard-product-brief.md"
figma_ready: true
link: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

---

# UX Spec: SimpleGuard - App Computador / Agente Desktop

## 1. Objetivo da Superficie

O app de computador e um agente leve instalado no dispositivo monitorado. Ele nao e a plataforma de administracao. Sua funcao e manter o dispositivo pareado, enviar telemetria suportada pelo sistema operacional, reportar conectividade e executar comandos remotos possiveis.

Este documento define UX minima para Windows, Linux e macOS. A tecnologia definida para o agente de computador e Rust como servico/daemon nativo, com Tauri opcional apenas para tray/configuracao local.

## 2. Principios de UX

- Agente discreto, verificavel e leve.
- Interface minima: status, permissoes, sincronizacao, pareamento e despareamento.
- Sem mapa operacional completo.
- Sem dashboards, relatorios ou configuracoes avancadas.
- Sempre mostrar quando o agente esta ativo, limitado ou offline.
- Nunca parecer spyware: o usuario deve conseguir identificar que o agente existe e esta pareado.

## 3. Padrao Visual

- Janela compacta.
- Fundo escuro.
- Moldura ciano fina, inspirada no HUD da plataforma.
- Estados por cor.
- Tipografia tecnica legivel.
- Layout de densidade media, sem ornamento excessivo.

## 4. Formatos de UI

### 4.1 Janela Principal

- Frame Figma: `Desktop Agent / Window / 520x640`.
- Uso: abrir configuracoes e status do agente.

### 4.2 Tray/Menu Bar

- Frame Figma: `Desktop Agent / Tray Menu / 280x360`.
- Uso: acesso rapido a status, sincronizacao e abrir painel.

### 4.3 Notificacao do Sistema

- Frame Figma: `Desktop Agent / Notification / 360x120`.
- Uso: comando recebido, falha critica ou pareamento concluido.

## 5. Telas Para Figma

### 5.1 `Desktop Agent / 01 - Boas-vindas`

Objetivo: preparar pareamento.

Conteudo:

- Titulo: `Agente SimpleGuard`.
- Texto: `Este agente conecta este computador a sua instancia SimpleGuard.`
- Acoes:
  - `Iniciar pareamento`.
  - `Configurar manualmente`.

### 5.2 `Desktop Agent / 02 - Pareamento`

Objetivo: vincular computador a instancia.

Conteudo:

- URL da instancia.
- Codigo de pareamento.
- Nome do computador.
- Usuario/SO detectado.
- Estado de validacao.

Estados:

- Aguardando codigo.
- Conectando.
- Validando.
- Pareado.
- Falha.

### 5.3 `Desktop Agent / 03 - Permissoes e Capacidades`

Objetivo: mostrar o que o agente consegue fazer neste SO.

Conteudo:

- Coleta de telemetria: disponivel/limitada.
- Localizacao: disponivel/indisponivel.
- Rede: disponivel/limitada.
- Bateria: disponivel/nao aplicavel.
- Bloqueio: suportado/nao suportado/exige permissao.
- Alarme: suportado/nao suportado.
- Execucao em segundo plano: ativa/inativa.

Regra:

- Cada capacidade deve indicar disponibilidade real. Nao esconder limitacoes do sistema operacional.

### 5.4 `Desktop Agent / 04 - Status Ativo`

Objetivo: visao principal do agente.

Conteudo:

- Estado geral.
- Instancia pareada.
- Ultima sincronizacao.
- Ultimo pacote enviado.
- Eventos pendentes.
- Versao do agente.
- Estado da conexao.
- Capacidades ativas.

Acoes:

- `Sincronizar agora`.
- `Testar conexao`.
- `Ver capacidades`.
- `Abrir logs locais`.
- `Desparear`.

### 5.5 `Desktop Agent / 05 - Menu da Bandeja`

Objetivo: acesso rapido.

Itens:

- Estado atual.
- Ultima sincronizacao.
- Eventos pendentes.
- `Sincronizar agora`.
- `Abrir agente`.
- `Pausar temporariamente`, se permitido por regra de produto.
- `Sair`, se permitido.

Observacao:

- Pausar/sair pode comprometer monitoramento e precisa de decisao de produto. Se entrar, deve gerar evento auditavel.

### 5.6 `Desktop Agent / 06 - Comando Recebido`

Objetivo: comunicar comando remoto.

Conteudo:

- Tipo de comando.
- Estado de execucao.
- Resultado.
- Timestamp.
- Instancia solicitante.

Estados:

- Recebido.
- Executando.
- Executado.
- Falhou.
- Expirado.

### 5.7 `Desktop Agent / 07 - Offline`

Objetivo: mostrar perda de comunicacao.

Conteudo:

- Estado: `Sem conexao com a instancia`.
- Eventos pendentes.
- Ultima tentativa.
- Proxima tentativa.
- Botao `Tentar agora`.

### 5.8 `Desktop Agent / 08 - Despareamento`

Objetivo: remover vinculo.

Conteudo:

- Instancia atual.
- Nome do dispositivo.
- Aviso de impacto.
- Confirmacao explicita.
- Resultado.

## 6. Componentes Para Figma

- `DesktopAgentWindow`
- `TrayStatusMenu`
- `CapabilityRow`
- `AgentStatusHeader`
- `SyncQueueSummary`
- `LocalLogPreview`
- `PairingForm`
- `CommandStatusPanel`
- `DangerConfirmPanel`

## 7. Estados do Agente

| Estado | Texto na UI | Cor |
|---|---|---|
| Ativo | `Agente ativo` | Verde |
| Limitado | `Capacidades limitadas` | Amarelo |
| Offline | `Offline` | Cinza |
| Sincronizando | `Sincronizando` | Azul |
| Comando recebido | `Comando recebido` | Azul |
| Falha | `Falha operacional` | Vermelho |
| Bloqueado | `Bloqueado` | Roxo |

## 8. Regras de Conteudo

- Usar mensagens diretas.
- Mostrar limitacoes do SO explicitamente.
- Nao transformar o agente em painel administrativo.
- Nao usar linguagem promocional.
- Nao ocultar estado offline ou permissoes faltantes.

## 9. Regras de Produto Para Resolver

- Definir se o usuario pode pausar o agente.
- Definir se o agente pode ser encerrado livremente.
- Definir quais comandos cada sistema operacional suporta.
- Definir se bloqueio do computador entra no MVP ou fase posterior.
- Definir matriz de capacidades por SO.

## 10. O Que Nao Desenhar

- Mapa completo.
- Painel de incidentes.
- Relatorios PDF.
- Cadastro de outros dispositivos.
- Dashboard de metricas.
- UI rica ou pesada.
- Fluxo promocional.

## 11. Checklist Para Figma

- Criar janela principal.
- Criar menu de bandeja.
- Criar tela de pareamento.
- Criar tela de capacidades.
- Criar status ativo.
- Criar estado offline.
- Criar comando recebido.
- Criar despareamento.
- Manter agente visualmente leve e operacional.
