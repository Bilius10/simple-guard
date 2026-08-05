# Task 1.1: Inicializar Estrutura Self-Hosted do Produto

Fase: MVP

Como administrador tecnico, quero subir a base local do SimpleGuard com API, web, banco, IdP e proxy, para operar uma instancia self-hosted inicial de forma previsivel.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `.codex/bmad/output/planning-artifacts/architecture/architecture-SimpleGuard-2026-08-05/ARCHITECTURE-SPINE.md`

Escopo:
- Criar estrutura inicial de deploy self-hosted.
- Subir API, web admin, PostgreSQL/PostGIS, IdP e reverse proxy.
- Expor health checks basicos.
- Documentar variaveis de ambiente obrigatorias.
- Nao versionar segredos.

Testes unitarios:
- Validar configuracao obrigatoria da API.
- Validar falha clara quando variavel obrigatoria estiver ausente.

Cenarios de validacao manual:
- Dado um ambiente com Docker, quando subir a configuracao local, entao API, web, banco, IdP e proxy devem iniciar.
- Dado a stack local ativa, quando acessar a URL da web e o health da API, entao ambos devem responder corretamente.

Criterio de conclusao:
- Stack local sobe de forma reproduzivel e health checks basicos funcionam.
