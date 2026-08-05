# Task 9.2: Configurar Retencao de Dados

Fase: fase posterior

Como administrador, quero configurar retencao de telemetria, auditoria e PDFs, para que a instancia respeite privacidade e operacao planejada.

Referencias:
- `docs/pesquisa/simpleguard-risk-stack-research.md`
- `docs/arquitetura/simpleguard-arquitetura.md`

Escopo:
- Definir configuracao de retencao por tipo de dado.
- Implementar dry-run.
- Preservar auditoria critica conforme regra.

Testes unitarios:
- Backend: calculo de expiracao, preservacao de auditoria e dry-run.

Cenarios de validacao manual:
- Configurar prazo curto em ambiente teste.
- Validar dados elegiveis para remocao.

Criterio de conclusao:
- Retencao e configuravel sem apagar evidencia critica indevidamente.
