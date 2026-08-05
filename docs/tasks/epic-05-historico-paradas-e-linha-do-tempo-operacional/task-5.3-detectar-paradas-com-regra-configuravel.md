# Task 5.3: Detectar Paradas com Regra Configuravel

Fase: MVP

Como administrador, quero que o sistema identifique paradas de 10 minutos, para saber onde o dispositivo permaneceu por tempo relevante.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/arquitetura/simpleguard-arquitetura.md`

Escopo:
- Implementar detector de parada.
- Usar duracao de 10 minutos.
- Manter raio/tolerancia como configuracao.
- Marcar parada como inferencia do sistema.

Testes unitarios:
- Backend: parada valida, jitter GPS, movimento real e dados insuficientes.

Cenarios de validacao manual:
- Carregar dataset com parada de 12 minutos.
- Confirmar parada detectada com inicio, fim, duracao e centroide.

Criterio de conclusao:
- Sistema detecta paradas sem alterar pontos brutos.
