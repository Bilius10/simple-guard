# Task 3.1: Ingerir Localizacao de Agente Pareado

Fase: MVP

Como administrador, quero receber localizacao de dispositivos pareados, para acompanhar onde cada dispositivo esteve.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`

Escopo:
- Criar endpoint de ingestao de localizacao.
- Validar autenticidade do agente.
- Persistir ponto bruto em PostGIS.
- Separar `collected_at` e `received_at`.

Testes unitarios:
- Backend: payload valido, assinatura invalida, dispositivo revogado e coordenada invalida.

Cenarios de validacao manual:
- Enviar ponto valido via cliente HTTP.
- Confirmar persistencia e resposta da API.

Criterio de conclusao:
- Localizacao autenticada e persistida sem inferencia destrutiva.
