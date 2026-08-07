---
title: "UX Spec: SimpleGuard - App Celular / Agente Android"
project_name: "SimpleGuard"
surface: "app_celular_agente_android"
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

# UX Spec: SimpleGuard - App Celular / Agente Android

## 1. Objetivo da Superficie

O app celular no MVP e um agente Android leve. Ele nao e a central operacional principal. Sua funcao e parear o dispositivo com a instancia SimpleGuard, enviar telemetria, armazenar eventos minimos quando offline e executar comandos suportados.

A UI deve ser minima, clara e orientada a status. O administrador opera o incidente pela plataforma web.

## 2. Principios de UX

- Baixa complexidade: poucas telas, sem painel analitico.
- Transparencia: o usuario deve saber que o dispositivo esta pareado e monitorado.
- Confiabilidade: permissao ausente, sincronizacao pendente e falha devem ficar visiveis.
- Operacao silenciosa, mas nao oculta: evitar uso abusivo como vigilancia escondida.
- Estetica SimpleGuard: retro-tech discreto, alto contraste e estados claros.

## 3. Layout Mobile Base

### Frame Figma

- Frame: `Mobile Agent / Android / 390x844`.
- Topbar compacta com nome `SimpleGuard Agent`.
- Area principal com cartao tecnico de status.
- Lista curta de permissoes.
- Barra inferior com status de sincronizacao.

### Paleta

Usar a mesma paleta da plataforma web, com menos ornamento:

- Fundo escuro.
- Borda ciano discreta.
- Estados verde, amarelo, vermelho, cinza e roxo.
- Texto grande o suficiente para leitura rapida.

## 4. Telas Para Figma

### 4.1 `Mobile Agent / 01 - Boas-vindas`

Objetivo: explicar funcao do agente sem marketing.

Conteudo:

- Titulo: `Agente SimpleGuard`.
- Texto curto: `Este app conecta este dispositivo a sua instancia SimpleGuard.`
- Lista objetiva:
  - Envia localizacao e telemetria.
  - Recebe comandos remotos suportados.
  - Sincroniza eventos quando a conexao voltar.
- Acao: `Iniciar pareamento`.

Nao incluir:

- Promessas de recuperacao garantida.
- Hero visual promocional.
- Ilustracoes grandes.

### 4.2 `Mobile Agent / 02 - Pareamento`

Objetivo: vincular dispositivo a instancia.

Conteudo:

- Campo URL da instancia.
- Leitor de QR code.
- Codigo manual.
- Nome do dispositivo sugerido.
- Estado de conexao com API.

Estados:

- Aguardando codigo.
- Validando instancia.
- Codigo expirado.
- Pareado com sucesso.
- Falha de pareamento.

### 4.3 `Mobile Agent / 03 - Permissoes`

Objetivo: solicitar permissoes necessarias.

Permissoes:

- Localizacao.
- Localizacao em segundo plano, se aplicavel.
- Notificacoes.
- Execucao em segundo plano.
- Permissao de administrador/dispositivo, se necessaria para bloqueio.
- Bateria sem otimizacao agressiva, quando aplicavel.

Layout:

- Lista de permissoes com estado: `concedida`, `pendente`, `bloqueada`.
- Cada item tem acao curta: `Configurar`.
- Rodape indica que funcoes podem ficar limitadas sem permissoes.

### 4.4 `Mobile Agent / 04 - Status Ativo`

Objetivo: mostrar que o agente esta funcionando.

Conteudo:

- Estado geral: `Ativo`, `Atencao`, `Offline`, `Comando pendente`.
- Ultima sincronizacao.
- Ultima localizacao enviada.
- Bateria.
- Rede.
- Fila local de eventos.
- Instancia pareada.

Acoes:

- `Sincronizar agora`.
- `Testar conexao`.
- `Ver permissoes`.
- `Desparear dispositivo`.

Regra:

- Nao exibir mapa completo aqui. O mapa e da plataforma web.

### 4.5 `Mobile Agent / 05 - Comando Recebido`

Objetivo: informar execucao de comando remoto.

Conteudo:

- Tipo de comando: bloqueio ou alarme.
- Estado: recebido, executando, executado, falhou.
- Timestamp.
- Instancia solicitante.

Regras:

- Se o comando exigir acao local do usuario, mostrar instrucao direta.
- Se falhar, mostrar motivo tecnico simples.
- Registrar resultado automaticamente na API quando possivel.

### 4.6 `Mobile Agent / 06 - Offline / Fila Local`

Objetivo: deixar claro que eventos estao aguardando envio.

Conteudo:

- Estado: `Sem conexao com a instancia`.
- Quantidade de eventos locais pendentes.
- Ultima tentativa de envio.
- Proxima tentativa.
- Acao: `Tentar agora`.

Regra:

- Nao assustar o usuario com mensagem generica de erro. Offline e estado esperado.

### 4.7 `Mobile Agent / 07 - Despareamento`

Objetivo: remover vinculo de forma controlada.

Conteudo:

- Nome do dispositivo.
- Instancia atual.
- Consequencias do despareamento.
- Confirmacao explicita.

Estados:

- Despareamento solicitado.
- Despareado.
- Falha ao comunicar API.
- Despareado localmente com sincronizacao pendente.

## 5. Componentes Para Figma

- `AgentStatusCard`
- `PermissionRow`
- `SyncStatusBar`
- `PairingCodeInput`
- `QrScanPanel`
- `CommandReceivedPanel`
- `OfflineQueuePanel`
- `AgentActionButton`
- `DangerActionSheet`

## 6. Estados do Agente

| Estado | Texto na UI | Cor |
|---|---|---|
| Pareado | `Pareado` | Verde |
| Ativo | `Agente ativo` | Verde |
| Permissao pendente | `Permissao pendente` | Amarelo |
| Sincronizando | `Sincronizando` | Azul |
| Offline | `Offline` | Cinza |
| Comando pendente | `Comando pendente` | Amarelo |
| Falha | `Falha operacional` | Vermelho |
| Bloqueado | `Dispositivo bloqueado` | Roxo |

## 7. Regras de Conteudo

- Texto curto e tecnico.
- Evitar tom promocional.
- Evitar prometer rastreamento perfeito.
- Indicar limitacoes reais: GPS desligado, permissao removida, economia de bateria, sem internet.
- Diferenciar `enviado`, `aguardando envio` e `falha`.

## 8. O Que Nao Desenhar

- Feed, social, ranking ou gamificacao.
- Mapa completo do hub operacional.
- Loja, upgrade, CTA promocional.
- Tela cheia com ilustracao decorativa.
- Recursos administrativos complexos.

## 9. Checklist Para Figma

- Criar fluxo de pareamento.
- Criar tela de permissoes.
- Criar tela de status ativo.
- Criar estado offline com fila local.
- Criar estado de comando recebido.
- Criar despareamento.
- Criar componentes de status e permissoes.
- Garantir que o app pareca agente leve, nao plataforma completa.
