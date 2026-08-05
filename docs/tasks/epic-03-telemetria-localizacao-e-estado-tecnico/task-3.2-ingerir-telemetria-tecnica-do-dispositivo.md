# Task 3.2: Ingerir Telemetria Tecnica do Dispositivo

Fase: MVP

Como administrador, quero receber bateria, rede, sinal e estado tecnico, para entender a confiabilidade operacional do dispositivo.

Referencias:
- `docs/produto/simpleguard-prd.md`
- `docs/arquitetura/simpleguard-arquitetura.md`

Escopo:
- Criar endpoint/servico de telemetria tecnica.
- Persistir bateria, rede, sinal e permissoes quando disponiveis.
- Representar dados ausentes como `null`.

Testes unitarios:
- Backend: valores validos, ausentes, fora de faixa e duplicados.

Cenarios de validacao manual:
- Enviar telemetria com bateria baixa.
- Confirmar estado tecnico salvo.

Criterio de conclusao:
- Dados tecnicos ficam persistidos e distinguem ausente de valor zero.
