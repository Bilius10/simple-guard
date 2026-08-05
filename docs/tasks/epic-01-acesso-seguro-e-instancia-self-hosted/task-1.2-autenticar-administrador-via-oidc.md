# Task 1.2: Autenticar Administrador via OIDC

Fase: MVP

Como administrador, quero acessar a plataforma com autenticacao segura, para que apenas usuarios autorizados operem dispositivos e incidentes.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Escopo:
- Integrar web/admin e API com IdP OIDC self-hosted.
- Bloquear API sem token valido.
- Implementar estados de login, erro e sessao expirada.

Testes unitarios:
- Backend: token valido, token expirado, token invalido e usuario inexistente.
- Frontend: login valido, erro de credencial e sessao expirada.

Cenarios de validacao manual:
- Acessar o hub sem login e confirmar bloqueio.
- Fazer login valido e acessar o hub.
- Fazer logout e confirmar remocao da sessao.

Criterio de conclusao:
- Administrador autentica via OIDC e endpoints protegidos rejeitam acesso anonimo.
