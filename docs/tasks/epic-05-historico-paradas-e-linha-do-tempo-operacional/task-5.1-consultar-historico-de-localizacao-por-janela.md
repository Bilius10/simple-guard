# Task 5.1: Consultar Historico de Localizacao por Janela

Fase: MVP

Como administrador, quero filtrar historico de localizacao por periodo, para analisar o deslocamento relevante de um dispositivo.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`

Escopo:
- Criar consulta de historico por dispositivo e periodo.
- Ordenar por `collected_at`.
- Preservar pontos recebidos fora de ordem.

Testes unitarios:
- Backend: ordenacao, janela vazia e limites de periodo.

Cenarios de validacao manual:
- Consultar ultimas 24h.
- Consultar periodo sem dados.
- Consultar periodo com pontos fora de ordem.

Criterio de conclusao:
- Historico retorna pontos corretos e ordenados pelo horario real de coleta.
