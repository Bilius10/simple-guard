# Task 8.2: Gerar PDF Operacional com HTML e Playwright

Fase: MVP

Como administrador, quero baixar um PDF do incidente, para compartilhar evidencias operacionais com terceiros.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`

Escopo:
- Gerar HTML do relatorio.
- Renderizar PDF via Chromium/Playwright.
- Incluir resumo, mapa, trajetoria, paradas, dados tecnicos, timeline e limitacoes.
- Armazenar hash e metadados.

Testes unitarios:
- Backend: montagem de dados do relatorio e armazenamento de hash.

Cenarios de validacao manual:
- Gerar PDF de incidente com dados completos.
- Verificar secoes esperadas.

Criterio de conclusao:
- PDF operacional e gerado a partir do snapshot e possui hash armazenado.
